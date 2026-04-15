package com.univscheduler.dao;

import com.univscheduler.model.Batiment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BatimentDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public List<Batiment> getTous() {
        List<Batiment> liste = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM batiments ORDER BY nom")) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public Batiment getParId(int id) {
        try (PreparedStatement ps = dbManager.getConnection()
                .prepareStatement("SELECT * FROM batiments WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapper(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean ajouter(Batiment b) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "INSERT INTO batiments (nom, adresse, nombre_etages) VALUES (?,?,?)")) {
            ps.setString(1, b.getNom());
            ps.setString(2, b.getAdresse());
            ps.setInt(3, b.getNombreEtages());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean modifier(Batiment b) {
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(
                "UPDATE batiments SET nom=?, adresse=?, nombre_etages=? WHERE id=?")) {
            ps.setString(1, b.getNom());
            ps.setString(2, b.getAdresse());
            ps.setInt(3, b.getNombreEtages());
            ps.setInt(4, b.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean supprimer(int id) {
        try (PreparedStatement ps = dbManager.getConnection()
                .prepareStatement("DELETE FROM batiments WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Batiment mapper(ResultSet rs) throws SQLException {
        return new Batiment(
            rs.getInt("id"),
            rs.getString("nom"),
            rs.getString("adresse"),
            rs.getInt("nombre_etages")
        );
    }
}
