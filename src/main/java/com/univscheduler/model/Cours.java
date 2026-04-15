package com.univscheduler.model;

/**
 * Représente un cours universitaire.
 */
public class Cours {

    private int id;
    private String nom;
    private String description;
    private int enseignantId;
    private String nomEnseignant;
    private String couleur; // couleur d'affichage dans l'emploi du temps (hex)

    public Cours() {}

    public Cours(int id, String nom, String description,
                 int enseignantId, String couleur) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.enseignantId = enseignantId;
        this.couleur = couleur;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getEnseignantId() { return enseignantId; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }

    public String getNomEnseignant() { return nomEnseignant; }
    public void setNomEnseignant(String nomEnseignant) { this.nomEnseignant = nomEnseignant; }

    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }

    @Override
    public String toString() {
        return nom + (nomEnseignant != null ? " (" + nomEnseignant + ")" : "");
    }
}
