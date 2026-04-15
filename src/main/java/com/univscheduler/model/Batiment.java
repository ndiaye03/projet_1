package com.univscheduler.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un bâtiment universitaire.
 */
public class Batiment {

    private int id;
    private String nom;
    private String adresse;
    private int nombreEtages;
    private List<Salle> salles;

    public Batiment() {
        this.salles = new ArrayList<>();
    }

    public Batiment(int id, String nom, String adresse, int nombreEtages) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.nombreEtages = nombreEtages;
        this.salles = new ArrayList<>();
    }

    public void ajouterSalle(Salle salle) {
        if (!salles.contains(salle)) {
            salles.add(salle);
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public int getNombreEtages() { return nombreEtages; }
    public void setNombreEtages(int nombreEtages) { this.nombreEtages = nombreEtages; }

    public List<Salle> getSalles() { return salles; }
    public void setSalles(List<Salle> salles) { this.salles = salles; }

    @Override
    public String toString() {
        return nom + " (" + adresse + ")";
    }
}
