package com.univscheduler.model;

/**
 * Represente un signalement envoye par un utilisateur.
 */
public class Signalement {

    public enum StatutSignalement {
        EN_ATTENTE("En attente"),
        APPROUVEE("Approuve"),
        REJETEE("Rejete");

        private final String libelle;

        StatutSignalement(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    private int id;
    private int utilisateurId;
    private String nomUtilisateur;
    private String sujet;
    private String description;
    private StatutSignalement statut;
    private String dateCreation;

    public Signalement() {
    }

    public Signalement(int id, int utilisateurId, String sujet, String description,
                       StatutSignalement statut, String dateCreation) {
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

    public StatutSignalement getStatut() {
        return statut;
    }

    public void setStatut(StatutSignalement statut) {
        this.statut = statut;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }
}
