package com.univscheduler.dao;

import com.univscheduler.model.Cours;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public List<Cours> getTous() {
        List<Cours> liste = new ArrayList<>();
        String sql = """
            SELECT c.*, u.nom || ' ' || u.prenom AS nom_enseignant
            FROM cours c
            LEFT JOIN utilisateurs u ON c.enseignant_id = u.id
            ORDER BY c.nom
        """;
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public List<Cours> getParEnseignant(int enseignantId) {
        List<Cours> liste = new ArrayList<>();
        String sql = """
            SELECT c.*, u.nom || ' ' || u.prenom AS nom_enseignant
            FROM cours c LEFT JOIN utilisateurs u ON c.enseignant_id=u.id
            WHERE c.enseignant_id=? ORDER BY c.nom
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, enseignantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public Cours getParId(int id) {
        String sql = "SELECT c.*, u.nom || ' ' || u.prenom AS nom_enseignant FROM cours c LEFT JOIN utilisateurs u ON c.enseignant_id=u.id WHERE c.id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapper(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean ajouter(Cours c) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "INSERT INTO cours (nom, description, enseignant_id, couleur) VALUES (?,?,?,?)")) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getEnseignantId());
            ps.setString(4, c.getCouleur() != null ? c.getCouleur() : "#4A90D9");
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean modifier(Cours c) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "UPDATE cours SET nom=?, description=?, enseignant_id=?, couleur=? WHERE id=?")) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getEnseignantId());
            ps.setString(4, c.getCouleur());
            ps.setInt(5, c.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean supprimer(int id) {
        try (PreparedStatement ps = dbManager.getConnection()
                .prepareStatement("DELETE FROM cours WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Cours mapper(ResultSet rs) throws SQLException {
        Cours c = new Cours(rs.getInt("id"), rs.getString("nom"),
                rs.getString("description"), rs.getInt("enseignant_id"),
                rs.getString("couleur"));
        c.setNomEnseignant(rs.getString("nom_enseignant"));
        return c;
    }
}
