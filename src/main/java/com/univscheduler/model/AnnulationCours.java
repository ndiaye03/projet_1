package com.univscheduler.model;

/**
 * Annulation ponctuelle d'un cours planifie a une date precise.
 */
public class AnnulationCours {

    private int id;
    private int creneauId;
    private String dateAnnulation;
    private String motif;
    private Integer annuleParId;
    private String dateCreation;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCreneauId() {
        return creneauId;
    }

    public void setCreneauId(int creneauId) {
        this.creneauId = creneauId;
    }

    public String getDateAnnulation() {
        return dateAnnulation;
    }

    public void setDateAnnulation(String dateAnnulation) {
        this.dateAnnulation = dateAnnulation;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public Integer getAnnuleParId() {
        return annuleParId;
    }

    public void setAnnuleParId(Integer annuleParId) {
        this.annuleParId = annuleParId;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }
}
