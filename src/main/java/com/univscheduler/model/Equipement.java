package com.univscheduler.model;

/**
 * Représente un équipement disponible dans une salle.
 * (Projecteur, tableau blanc, climatisation, etc.)
 */
public class Equipement {

    private int id;
    private String nom;
    private String description;
    private int salleId;
    private boolean fonctionnel;

    public Equipement() {}

    public Equipement(int id, String nom, String description, int salleId, boolean fonctionnel) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.salleId = salleId;
        this.fonctionnel = fonctionnel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSalleId() { return salleId; }
    public void setSalleId(int salleId) { this.salleId = salleId; }

    public boolean isFonctionnel() { return fonctionnel; }
    public void setFonctionnel(boolean fonctionnel) { this.fonctionnel = fonctionnel; }

    @Override
    public String toString() {
        return nom + (fonctionnel ? " ✓" : " ✗ (hors service)");
    }
}
