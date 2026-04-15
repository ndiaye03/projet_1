package com.univscheduler.dao;

import com.univscheduler.model.NotificationUtilisateur;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public boolean ajouter(NotificationUtilisateur notification) {
        String sql = """
            INSERT INTO notifications (utilisateur_id, titre, message, type, lue)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, notification.getUtilisateurId());
            ps.setString(2, notification.getTitre());
            ps.setString(3, notification.getMessage());
            ps.setString(4, notification.getType());
            ps.setInt(5, notification.isLue() ? 1 : 0);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<NotificationUtilisateur> getParUtilisateur(int utilisateurId, int limite) {
        List<NotificationUtilisateur> liste = new ArrayList<>();
        String sql = """
            SELECT *
            FROM notifications
            WHERE utilisateur_id = ?
            ORDER BY date_creation DESC, id DESC
            LIMIT ?
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapper(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    private NotificationUtilisateur mapper(ResultSet rs) throws SQLException {
        NotificationUtilisateur notification = new NotificationUtilisateur();
        notification.setId(rs.getInt("id"));
        notification.setUtilisateurId(rs.getInt("utilisateur_id"));
        notification.setTitre(rs.getString("titre"));
        notification.setMessage(rs.getString("message"));
        notification.setType(rs.getString("type"));
        notification.setLue(rs.getInt("lue") == 1);
        notification.setDateCreation(rs.getString("date_creation"));
        return notification;
    }
}
