package com.univscheduler.dao;

import com.univscheduler.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalleDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public List<Salle> getTous() {
        List<Salle> liste = new ArrayList<>();
        String sql = """
            SELECT s.*, b.nom AS nom_batiment
            FROM salles s
            LEFT JOIN batiments b ON s.batiment_id = b.id
            ORDER BY b.nom, s.numero
        """;
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public List<Salle> getParBatiment(int batimentId) {
        List<Salle> liste = new ArrayList<>();
        String sql = """
            SELECT s.*, b.nom AS nom_batiment
            FROM salles s LEFT JOIN batiments b ON s.batiment_id=b.id
            WHERE s.batiment_id=?
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, batimentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public Salle getParId(int id) {
        String sql = "SELECT s.*, b.nom AS nom_batiment FROM salles s LEFT JOIN batiments b ON s.batiment_id=b.id WHERE s.id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapper(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Retourne les salles disponibles pour un créneau donné.
     */
    public List<Salle> getSallesDisponibles(String jour, String heureDebut, String heureFin) {
        List<Salle> liste = new ArrayList<>();
        String sql = """
            SELECT s.*, b.nom AS nom_batiment FROM salles s
            LEFT JOIN batiments b ON s.batiment_id=b.id
            WHERE s.id NOT IN (
                SELECT DISTINCT salle_id FROM creneaux
                WHERE jour=? AND heure_debut < ? AND heure_fin > ?
            )
            AND s.id NOT IN (
                SELECT DISTINCT salle_id FROM reservations
                WHERE statut='APPROUVEE' AND heure_debut < ? AND heure_fin > ?
            )
            ORDER BY s.numero
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, jour);
            ps.setString(2, heureFin);
            ps.setString(3, heureDebut);
            ps.setString(4, heureFin);
            ps.setString(5, heureDebut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public List<Salle> getSallesDisponiblesPourDate(String date, String heureDebut, String heureFin) {
        LocalDate dateRecherche = LocalDate.parse(date);
        String jour = convertirJour(dateRecherche.getDayOfWeek());
        List<Salle> liste = new ArrayList<>();
        String sql = """
            SELECT s.*, b.nom AS nom_batiment
            FROM salles s
            LEFT JOIN batiments b ON s.batiment_id = b.id
            WHERE s.id NOT IN (
                SELECT DISTINCT cr.salle_id
                FROM creneaux cr
                WHERE cr.salle_id IS NOT NULL
                  AND cr.jour = ?
                  AND cr.heure_debut < ?
                  AND cr.heure_fin > ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM annulations_cours ac
                      WHERE ac.creneau_id = cr.id
                        AND ac.date_annulation = ?
                  )
            )
            AND s.id NOT IN (
                SELECT DISTINCT r.salle_id
                FROM reservations r
                WHERE r.statut = 'APPROUVEE'
                  AND r.date = ?
                  AND r.heure_debut < ?
                  AND r.heure_fin > ?
            )
            ORDER BY s.numero
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, jour);
            ps.setString(2, heureFin);
            ps.setString(3, heureDebut);
            ps.setString(4, date);
            ps.setString(5, date);
            ps.setString(6, heureFin);
            ps.setString(7, heureDebut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public boolean ajouter(Salle s) {
        String sql = """
            INSERT INTO salles (numero, capacite, type, batiment_id, accessible,
                nb_postes, os, sonorisation, retransmission)
            VALUES (?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getNumero());
            ps.setInt(2, s.getCapacite());
            ps.setString(3, s.getType().name());
            ps.setInt(4, s.getBatimentId());
            ps.setInt(5, s.isAccessible() ? 1 : 0);
            if (s instanceof Laboratoire l) {
                ps.setInt(6, l.getNombrePostes());
                ps.setString(7, l.getSystemeExploitation());
            } else { ps.setInt(6, 0); ps.setNull(7, Types.VARCHAR); }
            if (s instanceof Amphi a) {
                ps.setInt(8, a.isSonorisation() ? 1 : 0);
                ps.setInt(9, a.isRetransmission() ? 1 : 0);
            } else { ps.setInt(8, 0); ps.setInt(9, 0); }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    s.setId(rs.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean modifier(Salle s) {
        String sql = """
            UPDATE salles SET numero=?, capacite=?, type=?, batiment_id=?,
                accessible=?, nb_postes=?, os=?, sonorisation=?, retransmission=?
            WHERE id=?
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, s.getNumero());
            ps.setInt(2, s.getCapacite());
            ps.setString(3, s.getType().name());
            ps.setInt(4, s.getBatimentId());
            ps.setInt(5, s.isAccessible() ? 1 : 0);
            if (s instanceof Laboratoire l) {
                ps.setInt(6, l.getNombrePostes()); ps.setString(7, l.getSystemeExploitation());
            } else { ps.setInt(6, 0); ps.setNull(7, Types.VARCHAR); }
            if (s instanceof Amphi a) {
                ps.setInt(8, a.isSonorisation() ? 1 : 0); ps.setInt(9, a.isRetransmission() ? 1 : 0);
            } else { ps.setInt(8, 0); ps.setInt(9, 0); }
            ps.setInt(10, s.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean supprimer(int id) {
        try (PreparedStatement ps = dbManager.getConnection()
                .prepareStatement("DELETE FROM salles WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Salle mapper(ResultSet rs) throws SQLException {
        TypeSalle type = TypeSalle.valueOf(rs.getString("type"));
        Salle salle = switch (type) {
            case AMPHI -> new Amphi(
                rs.getInt("id"), rs.getString("numero"), rs.getInt("capacite"),
                rs.getInt("batiment_id"), rs.getInt("accessible") == 1,
                rs.getInt("sonorisation") == 1, rs.getInt("retransmission") == 1);
            case LABORATOIRE -> new Laboratoire(
                rs.getInt("id"), rs.getString("numero"), rs.getInt("capacite"),
                rs.getInt("batiment_id"), rs.getInt("accessible") == 1,
                rs.getInt("nb_postes"), rs.getString("os"));
            default -> new Salle(
                rs.getInt("id"), rs.getString("numero"), rs.getInt("capacite"),
                type, rs.getInt("batiment_id"), rs.getInt("accessible") == 1);
        };
        salle.setNomBatiment(rs.getString("nom_batiment"));
        return salle;
    }

    private String convertirJour(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "LUNDI";
            case TUESDAY -> "MARDI";
            case WEDNESDAY -> "MERCREDI";
            case THURSDAY -> "JEUDI";
            case FRIDAY -> "VENDREDI";
            case SATURDAY -> "SAMEDI";
            case SUNDAY -> throw new IllegalArgumentException("Aucun cours planifie le dimanche.");
        };
    }
}
