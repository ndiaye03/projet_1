package com.univscheduler.dao;

import com.univscheduler.model.Creneau;
import com.univscheduler.model.Creneau.Jour;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CreneauDAO {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public List<Creneau> getTous() {
        return requete("SELECT cr.*, c.nom AS nom_cours, c.couleur AS couleur_cours, "
                + "s.numero AS nom_salle, u.nom || ' ' || u.prenom AS nom_enseignant "
                + "FROM creneaux cr "
                + "LEFT JOIN cours c ON cr.cours_id=c.id "
                + "LEFT JOIN salles s ON cr.salle_id=s.id "
                + "LEFT JOIN utilisateurs u ON cr.enseignant_id=u.id "
                + "ORDER BY cr.jour, cr.heure_debut", null);
    }

    public List<Creneau> getParGroupe(String groupe) {
        return requeteAvecParam(
                "SELECT cr.*, c.nom AS nom_cours, c.couleur AS couleur_cours, "
                + "s.numero AS nom_salle, u.nom || ' ' || u.prenom AS nom_enseignant "
                + "FROM creneaux cr "
                + "LEFT JOIN cours c ON cr.cours_id=c.id "
                + "LEFT JOIN salles s ON cr.salle_id=s.id "
                + "LEFT JOIN utilisateurs u ON cr.enseignant_id=u.id "
                + "WHERE cr.groupe=? ORDER BY cr.jour, cr.heure_debut", groupe);
    }

    public List<Creneau> getParEnseignant(int enseignantId) {
        List<Creneau> liste = new ArrayList<>();
        String sql = "SELECT cr.*, c.nom AS nom_cours, c.couleur AS couleur_cours, "
                + "s.numero AS nom_salle, u.nom || ' ' || u.prenom AS nom_enseignant "
                + "FROM creneaux cr "
                + "LEFT JOIN cours c ON cr.cours_id=c.id "
                + "LEFT JOIN salles s ON cr.salle_id=s.id "
                + "LEFT JOIN utilisateurs u ON cr.enseignant_id=u.id "
                + "WHERE cr.enseignant_id=? ORDER BY cr.jour, cr.heure_debut";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, enseignantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public List<Creneau> getParSalle(int salleId) {
        List<Creneau> liste = new ArrayList<>();
        String sql = "SELECT cr.*, c.nom AS nom_cours, c.couleur AS couleur_cours, "
                + "s.numero AS nom_salle, u.nom || ' ' || u.prenom AS nom_enseignant "
                + "FROM creneaux cr "
                + "LEFT JOIN cours c ON cr.cours_id=c.id "
                + "LEFT JOIN salles s ON cr.salle_id=s.id "
                + "LEFT JOIN utilisateurs u ON cr.enseignant_id=u.id "
                + "WHERE cr.salle_id=? ORDER BY cr.jour, cr.heure_debut";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, salleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public Creneau getParId(int id) {
        String sql = "SELECT cr.*, c.nom AS nom_cours, c.couleur AS couleur_cours, "
                + "s.numero AS nom_salle, u.nom || ' ' || u.prenom AS nom_enseignant "
                + "FROM creneaux cr "
                + "LEFT JOIN cours c ON cr.cours_id=c.id "
                + "LEFT JOIN salles s ON cr.salle_id=s.id "
                + "LEFT JOIN utilisateurs u ON cr.enseignant_id=u.id "
                + "WHERE cr.id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapper(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Détecte les conflits de salle pour un nouveau créneau.
     */
    public List<Creneau> detecterConflitsSalle(int salleId, String jour,
                                                String heureDebut, String heureFin,
                                                int excludeId) {
        List<Creneau> conflits = new ArrayList<>();
        String sql = "SELECT cr.*, c.nom AS nom_cours, c.couleur AS couleur_cours, "
                + "s.numero AS nom_salle, u.nom || ' ' || u.prenom AS nom_enseignant "
                + "FROM creneaux cr "
                + "LEFT JOIN cours c ON cr.cours_id=c.id "
                + "LEFT JOIN salles s ON cr.salle_id=s.id "
                + "LEFT JOIN utilisateurs u ON cr.enseignant_id=u.id "
                + "WHERE cr.salle_id=? AND cr.jour=? "
                + "AND cr.heure_debut < ? AND cr.heure_fin > ? "
                + "AND cr.id != ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, salleId);
            ps.setString(2, jour);
            ps.setString(3, heureFin);
            ps.setString(4, heureDebut);
            ps.setInt(5, excludeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) conflits.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return conflits;
    }

    /**
     * Détecte les conflits enseignant.
     */
    public List<Creneau> detecterConflitsEnseignant(int enseignantId, String jour,
                                                      String heureDebut, String heureFin,
                                                      int excludeId) {
        List<Creneau> conflits = new ArrayList<>();
        String sql = "SELECT cr.*, c.nom AS nom_cours, c.couleur AS couleur_cours, "
                + "s.numero AS nom_salle, u.nom || ' ' || u.prenom AS nom_enseignant "
                + "FROM creneaux cr "
                + "LEFT JOIN cours c ON cr.cours_id=c.id "
                + "LEFT JOIN salles s ON cr.salle_id=s.id "
                + "LEFT JOIN utilisateurs u ON cr.enseignant_id=u.id "
                + "WHERE cr.enseignant_id=? AND cr.jour=? "
                + "AND cr.heure_debut < ? AND cr.heure_fin > ? "
                + "AND cr.id != ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, enseignantId);
            ps.setString(2, jour);
            ps.setString(3, heureFin);
            ps.setString(4, heureDebut);
            ps.setInt(5, excludeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) conflits.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return conflits;
    }

    /**
     * Détecte les conflits de groupe.
     */
    public List<Creneau> detecterConflitsGroupe(String groupe, String jour,
                                                  String heureDebut, String heureFin,
                                                  int excludeId) {
        List<Creneau> conflits = new ArrayList<>();
        String sql = "SELECT cr.*, c.nom AS nom_cours, c.couleur AS couleur_cours, "
                + "s.numero AS nom_salle, u.nom || ' ' || u.prenom AS nom_enseignant "
                + "FROM creneaux cr "
                + "LEFT JOIN cours c ON cr.cours_id=c.id "
                + "LEFT JOIN salles s ON cr.salle_id=s.id "
                + "LEFT JOIN utilisateurs u ON cr.enseignant_id=u.id "
                + "WHERE cr.groupe=? AND cr.jour=? "
                + "AND cr.heure_debut < ? AND cr.heure_fin > ? "
                + "AND cr.id != ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, groupe);
            ps.setString(2, jour);
            ps.setString(3, heureFin);
            ps.setString(4, heureDebut);
            ps.setInt(5, excludeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) conflits.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return conflits;
    }

    public boolean ajouter(Creneau cr) {
        String sql = "INSERT INTO creneaux (cours_id, salle_id, enseignant_id, jour, heure_debut, heure_fin, groupe) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, cr.getCoursId());
            ps.setInt(2, cr.getSalleId());
            ps.setInt(3, cr.getEnseignantId());
            ps.setString(4, cr.getJour().name());
            ps.setString(5, cr.getHeureDebut());
            ps.setString(6, cr.getHeureFin());
            ps.setString(7, cr.getGroupe());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean modifier(Creneau cr) {
        String sql = "UPDATE creneaux SET cours_id=?, salle_id=?, enseignant_id=?, jour=?, heure_debut=?, heure_fin=?, groupe=? WHERE id=?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, cr.getCoursId());
            ps.setInt(2, cr.getSalleId());
            ps.setInt(3, cr.getEnseignantId());
            ps.setString(4, cr.getJour().name());
            ps.setString(5, cr.getHeureDebut());
            ps.setString(6, cr.getHeureFin());
            ps.setString(7, cr.getGroupe());
            ps.setInt(8, cr.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean supprimer(int id) {
        try (PreparedStatement ps = dbManager.getConnection()
                .prepareStatement("DELETE FROM creneaux WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<String> getGroupes() {
        List<String> groupes = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT groupe FROM creneaux ORDER BY groupe")) {
            while (rs.next()) groupes.add(rs.getString("groupe"));
        } catch (SQLException e) { e.printStackTrace(); }
        return groupes;
    }

    private List<Creneau> requete(String sql, Object param) {
        List<Creneau> liste = new ArrayList<>();
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    private List<Creneau> requeteAvecParam(String sql, String param) {
        List<Creneau> liste = new ArrayList<>();
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    private Creneau mapper(ResultSet rs) throws SQLException {
        Creneau cr = new Creneau();
        cr.setId(rs.getInt("id"));
        cr.setCoursId(rs.getInt("cours_id"));
        cr.setSalleId(rs.getInt("salle_id"));
        cr.setEnseignantId(rs.getInt("enseignant_id"));
        cr.setJour(Jour.valueOf(rs.getString("jour")));
        cr.setHeureDebut(rs.getString("heure_debut"));
        cr.setHeureFin(rs.getString("heure_fin"));
        cr.setGroupe(rs.getString("groupe"));
        cr.setNomCours(rs.getString("nom_cours"));
        cr.setNomSalle(rs.getString("nom_salle"));
        cr.setNomEnseignant(rs.getString("nom_enseignant"));
        cr.setCouleurCours(rs.getString("couleur_cours"));
        return cr;
    }
}
