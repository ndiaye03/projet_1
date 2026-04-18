package com.univscheduler.dao;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gestionnaire central de la base SQLite.
 */
public class DatabaseManager {

    private static final String DB_NAME = "univ_scheduler.db";
    private static final String SCHEMA_RESOURCE = "database/schema.sql";
    private static final Path DB_PATH = resolveDatabasePath();
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    public Path getDatabasePath() {
        return DB_PATH;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection = null;
        }
    }

    /**
     * Initialise la base. Si une ancienne base est incompatible, elle est
     * sauvegardee puis recreee depuis schema.sql.
     */
    public void initialiserBDD() {
        initialiserBDD(true);
    }

    private void initialiserBDD(boolean autoriserReparation) {
        try {
            Connection conn = getConnection();

            if (!schemaCompatible(conn)) {
                if (!autoriserReparation) {
                    throw new IllegalStateException("Le fichier SQLite existant est incompatible avec le schema attendu.");
                }
                reparerBaseExistante();
                return;
            }

            appliquerMigrationsSchema(conn);

            if (!schemaInitialise(conn)) {
                executerSchema(conn);
            } else {
                appliquerMigrationDonnees(conn);
            }
        } catch (SQLException | IOException e) {
            if (autoriserReparation && Files.exists(DB_PATH)) {
                try {
                    reparerBaseExistante();
                    return;
                } catch (SQLException | IOException reparationException) {
                    e.addSuppressed(reparationException);
                }
            }
            throw new IllegalStateException("Impossible d'initialiser la base de donnees SQLite.", e);
        }
    }

    private boolean schemaCompatible(Connection conn) {
        try {
            for (Map.Entry<String, Set<String>> entry : schemaAttendu().entrySet()) {
                String table = entry.getKey();
                if (!tableExiste(conn, table)) {
                    continue;
                }

                Set<String> colonnesReelles = colonnesTable(conn, table);
                if (!colonnesReelles.containsAll(entry.getValue())) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean schemaInitialise(Connection conn) throws SQLException {
        if (!tableExiste(conn, "utilisateurs")) {
            return false;
        }

        try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM utilisateurs");
             ResultSet rs = pstmt.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private boolean tableExiste(Connection conn, String nomTable) throws SQLException {
        String sql = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomTable);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Set<String> colonnesTable(Connection conn, String nomTable) throws SQLException {
        Set<String> colonnes = new java.util.HashSet<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + nomTable + ")")) {
            while (rs.next()) {
                colonnes.add(rs.getString("name"));
            }
        }
        return colonnes;
    }

    private void executerSchema(Connection conn) throws SQLException, IOException {
        String script = chargerSchema();
        List<String> statements = decouperStatements(script);
        boolean autoCommit = conn.getAutoCommit();

        conn.setAutoCommit(false);
        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                if (!sql.isBlank()) {
                    stmt.execute(sql);
                }
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    private String chargerSchema() throws IOException {
        try (InputStream inputStream = DatabaseManager.class.getClassLoader().getResourceAsStream(SCHEMA_RESOURCE)) {
            if (inputStream != null) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        Path fallbackPath = Paths.get("database", "schema.sql").toAbsolutePath().normalize();
        if (Files.exists(fallbackPath)) {
            return Files.readString(fallbackPath, StandardCharsets.UTF_8);
        }

        throw new IOException("Fichier schema introuvable: " + SCHEMA_RESOURCE);
    }

    private List<String> decouperStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder courant = new StringBuilder();

        for (String line : script.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }

            courant.append(line).append(System.lineSeparator());
            if (trimmed.endsWith(";")) {
                String sql = courant.toString().trim();
                if (sql.endsWith(";")) {
                    sql = sql.substring(0, sql.length() - 1).trim();
                }
                if (!sql.isBlank()) {
                    statements.add(sql);
                }
                courant.setLength(0);
            }
        }

        String reste = courant.toString().trim();
        if (!reste.isBlank()) {
            statements.add(reste);
        }

        return statements;
    }

    private void reparerBaseExistante() throws IOException, SQLException {
        closeConnection();

        if (Files.exists(DB_PATH)) {
            Files.move(DB_PATH, creerCheminSauvegarde(), StandardCopyOption.REPLACE_EXISTING);
        }

        initialiserBDD(false);
    }

    private Path creerCheminSauvegarde() {
        String nomSauvegarde = "univ_scheduler.backup-" + System.currentTimeMillis() + ".db";
        return DB_PATH.resolveSibling(nomSauvegarde);
    }

    private Map<String, Set<String>> schemaAttendu() {
        Map<String, Set<String>> schema = new LinkedHashMap<>();
        schema.put("utilisateurs", Set.of(
            "id", "nom", "prenom", "email", "login", "mot_de_passe",
            "role", "departement", "specialite", "groupe", "filiere", "niveau"
        ));
        schema.put("batiments", Set.of("id", "nom", "adresse", "nombre_etages"));
        schema.put("salles", Set.of(
            "id", "numero", "capacite", "type", "batiment_id", "accessible",
            "nb_postes", "os", "sonorisation", "retransmission"
        ));
        schema.put("equipements", Set.of("id", "nom", "description", "salle_id", "fonctionnel"));
        schema.put("cours", Set.of("id", "nom", "description", "enseignant_id", "couleur"));
        schema.put("creneaux", Set.of(
            "id", "cours_id", "salle_id", "enseignant_id", "jour",
            "heure_debut", "heure_fin", "groupe"
        ));
        schema.put("annulations_cours", Set.of(
            "id", "creneau_id", "date_annulation", "motif", "annule_par_id", "date_creation"
        ));
        schema.put("notifications", Set.of(
            "id", "utilisateur_id", "titre", "message", "type", "lue", "date_creation"
        ));
        schema.put("reservations", Set.of(
            "id", "salle_id", "utilisateur_id", "date", "heure_debut",
            "heure_fin", "motif", "statut"
        ));
        schema.put("signalements", Set.of(
            "id", "utilisateur_id", "sujet", "description", "statut", "date_creation"
        ));
        return schema;
    }

    /**
     * Met a jour une base deja creee avec les anciennes donnees de demo.
     */
    private void appliquerMigrationDonnees(Connection conn) throws SQLException {
        if (!ancienneBaseDemo(conn)) {
            return;
        }

        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "UPDATE utilisateurs " +
                "SET nom='Kali', prenom='François', email='papalyndiaye0@gmail.com', login='papalyndiaye0@gmail.com' " +
                "WHERE login='admin'"
            );
            stmt.executeUpdate(
                "UPDATE utilisateurs " +
                "SET nom='Konaté', prenom='Madame' " +
                "WHERE login='gestionnaire'"
            );
            stmt.executeUpdate(
                "UPDATE batiments " +
                "SET nom='Bâtiment C - Amphithéâtres' " +
                "WHERE nom='Bâtiment C - Amphi'"
            );
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    private boolean ancienneBaseDemo(Connection conn) throws SQLException {
        return existe(conn, "SELECT 1 FROM utilisateurs WHERE login = 'admin'")
            || existe(conn, "SELECT 1 FROM batiments WHERE nom = 'Bâtiment C - Amphi'");
    }

    private boolean existe(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next();
        }
    }

    private void appliquerMigrationsSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS annulations_cours (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    creneau_id       INTEGER NOT NULL REFERENCES creneaux(id) ON DELETE CASCADE,
                    date_annulation  TEXT    NOT NULL,
                    motif            TEXT    NOT NULL,
                    annule_par_id    INTEGER REFERENCES utilisateurs(id) ON DELETE SET NULL,
                    date_creation    TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE(creneau_id, date_annulation)
                )
            """);
            stmt.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS idx_annulations_cours_unique
                ON annulations_cours (creneau_id, date_annulation)
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    utilisateur_id  INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
                    titre           TEXT    NOT NULL,
                    message         TEXT    NOT NULL,
                    type            TEXT    NOT NULL DEFAULT 'INFO',
                    lue             INTEGER NOT NULL DEFAULT 0,
                    date_creation   TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS signalements (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    utilisateur_id  INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
                    sujet           TEXT    NOT NULL,
                    description     TEXT    NOT NULL,
                    statut          TEXT    NOT NULL DEFAULT 'EN_ATTENTE',
                    date_creation   TEXT    NOT NULL
                )
            """);
            stmt.executeUpdate("""
                UPDATE signalements
                SET statut = 'EN_ATTENTE'
                WHERE statut IS NULL OR TRIM(statut) = '' OR UPPER(statut) = 'NOUVEAU'
            """);
            stmt.executeUpdate("""
                UPDATE signalements
                SET statut = 'APPROUVEE'
                WHERE UPPER(statut) = 'TRAITE'
            """);
            stmt.executeUpdate("""
                UPDATE signalements
                SET statut = 'REJETEE'
                WHERE UPPER(statut) = 'REJETE'
            """);
        }
    }

    private static Path resolveDatabasePath() {
        Path baseDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

        try {
            Path codeSource = Paths.get(
                DatabaseManager.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            );

            if (Files.isRegularFile(codeSource)) {
                Path jarDir = codeSource.getParent();
                if (jarDir != null) {
                    baseDir = jarDir.toAbsolutePath().normalize();
                }
            }
        } catch (URISyntaxException | IllegalArgumentException ignored) {
            // Fallback sur user.dir.
        }

        try {
            Files.createDirectories(baseDir);
        } catch (IOException ignored) {
            // SQLite gerera l'erreur d'acces si le dossier est invalide.
        }

        return baseDir.resolve(DB_NAME);
    }
}
