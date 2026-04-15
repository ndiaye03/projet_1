package com.univscheduler.model;

/**
 * Represente un signalement envoye par un utilisateur.
 */
public class Signalement {

    private int id;
    private int utilisateurId;
    private String nomUtilisateur;
    private String sujet;
    private String description;
    private String statut;
    private String dateCreation;

    public Signalement() {
    }

    public Signalement(int id, int utilisateurId, String sujet, String description, String statut, String dateCreation) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.sujet = sujet;
        this.description = description;
        this.statut = statut;
        this.dateCreation = dateCreation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }
}
