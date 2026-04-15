package com.univscheduler.dao;

import com.univscheduler.model.AnnulationCours;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class AnnulationCoursDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public boolean ajouter(AnnulationCours annulation) {
        String sql = """
            INSERT INTO annulations_cours
                (creneau_id, date_annulation, motif, annule_par_id)
            VALUES (?, ?, ?, ?)
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, annulation.getCreneauId());
            ps.setString(2, annulation.getDateAnnulation());
            ps.setString(3, annulation.getMotif());
            if (annulation.getAnnuleParId() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, annulation.getAnnuleParId());
            }
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean estCreneauAnnule(int creneauId, String dateAnnulation) {
        String sql = """
            SELECT 1
            FROM annulations_cours
            WHERE creneau_id = ? AND date_annulation = ?
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, creneauId);
            ps.setString(2, dateAnnulation);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
