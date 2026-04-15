package com.univscheduler.dao;

import com.univscheduler.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des utilisateurs.
 */
public class UtilisateurDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public Utilisateur authentifier(String login, String motDePasse) {
        String sql = "SELECT * FROM utilisateurs WHERE login = ? AND mot_de_passe = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, login);
            pstmt.setString(2, motDePasse);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapperUtilisateur(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Utilisateur> getTous() {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs ORDER BY nom, prenom";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapperUtilisateur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public List<Utilisateur> getParRole(RoleUtilisateur role) {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs WHERE role = ? ORDER BY nom, prenom";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, role.name());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                liste.add(mapperUtilisateur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public List<Etudiant> getEtudiantsParGroupe(String groupe) {
        List<Etudiant> liste = new ArrayList<>();
        String sql = """
            SELECT * FROM utilisateurs
            WHERE role = 'ETUDIANT' AND groupe = ?
            ORDER BY nom, prenom
        """;
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, groupe);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                liste.add((Etudiant) mapperUtilisateur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public Utilisateur getParId(int id) {
        String sql = "SELECT * FROM utilisateurs WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapperUtilisateur(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean ajouter(Utilisateur u) {
        String sql = """
            INSERT INTO utilisateurs (nom, prenom, email, login, mot_de_passe, role,
                departement, specialite, groupe, filiere, niveau)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, u.getNom());
            pstmt.setString(2, u.getPrenom());
            pstmt.setString(3, u.getEmail());
            pstmt.setString(4, u.getLogin());
            pstmt.setString(5, u.getMotDePasse());
            pstmt.setString(6, u.getRole().name());
            if (u instanceof Enseignant e) {
                pstmt.setString(7, e.getDepartement());
                pstmt.setString(8, e.getSpecialite());
                pstmt.setNull(9, Types.VARCHAR);
                pstmt.setNull(10, Types.VARCHAR);
                pstmt.setNull(11, Types.INTEGER);
            } else if (u instanceof Etudiant et) {
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setNull(8, Types.VARCHAR);
                pstmt.setString(9, et.getGroupe());
                pstmt.setString(10, et.getFiliere());
                pstmt.setInt(11, et.getNiveau());
            } else {
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setNull(8, Types.VARCHAR);
                pstmt.setNull(9, Types.VARCHAR);
                pstmt.setNull(10, Types.VARCHAR);
                pstmt.setNull(11, Types.INTEGER);
            }
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean modifier(Utilisateur u) {
        String sql = """
            UPDATE utilisateurs SET nom=?, prenom=?, email=?, login=?, role=?,
                departement=?, specialite=?, groupe=?, filiere=?, niveau=?
            WHERE id=?
        """;
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, u.getNom());
            pstmt.setString(2, u.getPrenom());
            pstmt.setString(3, u.getEmail());
            pstmt.setString(4, u.getLogin());
            pstmt.setString(5, u.getRole().name());
            if (u instanceof Enseignant e) {
                pstmt.setString(6, e.getDepartement());
                pstmt.setString(7, e.getSpecialite());
                pstmt.setNull(8, Types.VARCHAR);
                pstmt.setNull(9, Types.VARCHAR);
                pstmt.setNull(10, Types.INTEGER);
            } else if (u instanceof Etudiant et) {
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setString(8, et.getGroupe());
                pstmt.setString(9, et.getFiliere());
                pstmt.setInt(10, et.getNiveau());
            } else {
                pstmt.setNull(6, Types.VARCHAR); pstmt.setNull(7, Types.VARCHAR);
                pstmt.setNull(8, Types.VARCHAR); pstmt.setNull(9, Types.VARCHAR);
                pstmt.setNull(10, Types.INTEGER);
            }
            pstmt.setInt(11, u.getId());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimer(int id) {
        try (PreparedStatement pstmt = dbManager.getConnection()
                .prepareStatement("DELETE FROM utilisateurs WHERE id=?")) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean modifierMotDePasse(int id, String nouveauMdp) {
        try (PreparedStatement pstmt = dbManager.getConnection()
                .prepareStatement("UPDATE utilisateurs SET mot_de_passe=? WHERE id=?")) {
            pstmt.setString(1, nouveauMdp);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Utilisateur mapperUtilisateur(ResultSet rs) throws SQLException {
        RoleUtilisateur role = RoleUtilisateur.valueOf(rs.getString("role"));
        return switch (role) {
            case ADMIN -> new Admin(
                rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"),
                rs.getString("email"), rs.getString("login"), rs.getString("mot_de_passe"));
            case GESTIONNAIRE -> new Gestionnaire(
                rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"),
                rs.getString("email"), rs.getString("login"), rs.getString("mot_de_passe"));
            case ENSEIGNANT -> new Enseignant(
                rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"),
                rs.getString("email"), rs.getString("login"), rs.getString("mot_de_passe"),
                rs.getString("departement"), rs.getString("specialite"));
            case ETUDIANT -> new Etudiant(
                rs.getInt("id"), rs.getString("nom"), rs.getString("prenom"),
                rs.getString("email"), rs.getString("login"), rs.getString("mot_de_passe"),
                rs.getString("groupe"), rs.getString("filiere"), rs.getInt("niveau"));
        };
    }
}
