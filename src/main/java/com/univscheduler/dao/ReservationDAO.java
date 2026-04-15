package com.univscheduler.dao;

import com.univscheduler.model.Reservation;
import com.univscheduler.model.Reservation.StatutReservation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public List<Reservation> getTous() {
        return requete("""
            SELECT r.*, s.numero AS nom_salle,
                   u.nom || ' ' || u.prenom AS nom_utilisateur
            FROM reservations r
            LEFT JOIN salles s ON r.salle_id=s.id
            LEFT JOIN utilisateurs u ON r.utilisateur_id=u.id
            ORDER BY r.date DESC, r.heure_debut
        """);
    }

    public List<Reservation> getParUtilisateur(int userId) {
        List<Reservation> liste = new ArrayList<>();
        String sql = """
            SELECT r.*, s.numero AS nom_salle,
                   u.nom || ' ' || u.prenom AS nom_utilisateur
            FROM reservations r
            LEFT JOIN salles s ON r.salle_id=s.id
            LEFT JOIN utilisateurs u ON r.utilisateur_id=u.id
            WHERE r.utilisateur_id=? ORDER BY r.date DESC
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public boolean ajouter(Reservation r) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "INSERT INTO reservations (salle_id, utilisateur_id, date, heure_debut, heure_fin, motif, statut) VALUES (?,?,?,?,?,?,?)")) {
            ps.setInt(1, r.getSalleId());
            ps.setInt(2, r.getUtilisateurId());
            ps.setString(3, r.getDate());
            ps.setString(4, r.getHeureDebut());
            ps.setString(5, r.getHeureFin());
            ps.setString(6, r.getMotif());
            ps.setString(7, r.getStatut().name());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean changerStatut(int id, StatutReservation statut) {
        try (PreparedStatement ps = dbManager.getConnection()
                .prepareStatement("UPDATE reservations SET statut=? WHERE id=?")) {
            ps.setString(1, statut.name());
            ps.setInt(2, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean supprimer(int id) {
        try (PreparedStatement ps = dbManager.getConnection()
                .prepareStatement("DELETE FROM reservations WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private List<Reservation> requete(String sql) {
        List<Reservation> liste = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    private Reservation mapper(ResultSet rs) throws SQLException {
        Reservation r = new Reservation(
            rs.getInt("id"), rs.getInt("salle_id"),
            rs.getInt("utilisateur_id"), rs.getString("date"),
            rs.getString("heure_debut"), rs.getString("heure_fin"),
            rs.getString("motif"),
            StatutReservation.valueOf(rs.getString("statut")));
        r.setNomSalle(rs.getString("nom_salle"));
        r.setNomUtilisateur(rs.getString("nom_utilisateur"));
        return r;
    }
}
