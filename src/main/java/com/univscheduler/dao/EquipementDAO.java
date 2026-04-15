package com.univscheduler.dao;

import com.univscheduler.model.Equipement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipementDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public List<Equipement> getParSalle(int salleId) {
        List<Equipement> liste = new ArrayList<>();
        try (PreparedStatement ps = dbManager.getConnection()
                .prepareStatement("SELECT * FROM equipements WHERE salle_id=? ORDER BY nom")) {
            ps.setInt(1, salleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public boolean ajouter(Equipement e) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "INSERT INTO equipements (nom, description, salle_id, fonctionnel) VALUES (?,?,?,?)")) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getDescription());
            ps.setInt(3, e.getSalleId());
            ps.setInt(4, e.isFonctionnel() ? 1 : 0);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public boolean modifier(Equipement e) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "UPDATE equipements SET nom=?, description=?, fonctionnel=? WHERE id=?")) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getDescription());
            ps.setInt(3, e.isFonctionnel() ? 1 : 0);
            ps.setInt(4, e.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public boolean supprimer(int id) {
        try (PreparedStatement ps = dbManager.getConnection()
                .prepareStatement("DELETE FROM equipements WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Equipement mapper(ResultSet rs) throws SQLException {
        return new Equipement(
            rs.getInt("id"), rs.getString("nom"),
            rs.getString("description"), rs.getInt("salle_id"),
            rs.getInt("fonctionnel") == 1);
    }
}
