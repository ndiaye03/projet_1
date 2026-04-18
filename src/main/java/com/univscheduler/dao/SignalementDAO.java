package com.univscheduler.dao;

import com.univscheduler.model.Signalement;
import com.univscheduler.model.Signalement.StatutSignalement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO des signalements utilisateur.
 */
public class SignalementDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public boolean ajouter(Signalement signalement) {
        String sql = """
            INSERT INTO signalements (utilisateur_id, sujet, description, statut, date_creation)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, signalement.getUtilisateurId());
            ps.setString(2, signalement.getSujet());
            ps.setString(3, signalement.getDescription());
            ps.setString(4, signalement.getStatut().name());
            ps.setString(5, signalement.getDateCreation());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Signalement> getTous() {
        List<Signalement> liste = new ArrayList<>();
        String sql = """
            SELECT s.*, u.nom || ' ' || u.prenom AS nom_utilisateur
            FROM signalements s
            JOIN utilisateurs u ON u.id = s.utilisateur_id
            ORDER BY s.date_creation DESC, s.id DESC
        """;
        try (Statement st = dbManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public List<Signalement> getParUtilisateur(int utilisateurId) {
        List<Signalement> liste = new ArrayList<>();
        String sql = """
            SELECT s.*, u.nom || ' ' || u.prenom AS nom_utilisateur
            FROM signalements s
            JOIN utilisateurs u ON u.id = s.utilisateur_id
            WHERE s.utilisateur_id = ?
            ORDER BY s.date_creation DESC, s.id DESC
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public boolean modifierStatut(int id, String statut) {
        try (PreparedStatement ps = dbManager.getConnection()
                .prepareStatement("UPDATE signalements SET statut=? WHERE id=?")) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Signalement mapper(ResultSet rs) throws SQLException {
        Signalement signalement = new Signalement(
                rs.getInt("id"),
                rs.getInt("utilisateur_id"),
                rs.getString("sujet"),
                rs.getString("description"),
                convertirStatut(rs.getString("statut")),
                rs.getString("date_creation")
        );
        signalement.setNomUtilisateur(rs.getString("nom_utilisateur"));
        return signalement;
    }

    private StatutSignalement convertirStatut(String statut) {
        if (statut == null || statut.isBlank()) {
            return StatutSignalement.EN_ATTENTE;
        }

        return switch (statut.trim().toUpperCase()) {
            case "NOUVEAU", "EN_ATTENTE" -> StatutSignalement.EN_ATTENTE;
            case "TRAITE", "APPROUVEE" -> StatutSignalement.APPROUVEE;
            case "REJETE", "REJETEE" -> StatutSignalement.REJETEE;
            default -> StatutSignalement.EN_ATTENTE;
        };
    }
}
