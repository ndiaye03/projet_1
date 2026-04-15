package com.univscheduler.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une salle dans un bâtiment universitaire.
 * Classe de base pour différents types de salles (héritage).
 */
public class Salle {

    protected int id;
    protected String numero;
    protected int capacite;
    protected TypeSalle type;
    protected int batimentId;
    protected String nomBatiment;
    protected boolean accessible; // accessibilité PMR
    protected List<Equipement> equipements;

    public Salle() {
        this.equipements = new ArrayList<>();
    }

    public Salle(int id, String numero, int capacite, TypeSalle type,
                 int batimentId, boolean accessible) {
        this.id = id;
        this.numero = numero;
        this.capacite = capacite;
        this.type = type;
        this.batimentId = batimentId;
        this.accessible = accessible;
        this.equipements = new ArrayList<>();
    }

    /**
     * Vérifie si la salle dispose d'un équipement spécifique.
     */
    public boolean hasEquipement(String nomEquipement) {
        return equipements.stream()
                .anyMatch(e -> e.getNom().equalsIgnoreCase(nomEquipement));
    }

    /**
     * Ajoute un équipement à la salle.
     */
    public void ajouterEquipement(Equipement equipement) {
        if (!equipements.contains(equipement)) {
            equipements.add(equipement);
        }
    }

    // Getters / Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public TypeSalle getType() { return type; }
    public void setType(TypeSalle type) { this.type = type; }

    public int getBatimentId() { return batimentId; }
    public void setBatimentId(int batimentId) { this.batimentId = batimentId; }

    public String getNomBatiment() { return nomBatiment; }
    public void setNomBatiment(String nomBatiment) { this.nomBatiment = nomBatiment; }

    public boolean isAccessible() { return accessible; }
    public void setAccessible(boolean accessible) { this.accessible = accessible; }

    public List<Equipement> getEquipements() { return equipements; }
    public void setEquipements(List<Equipement> equipements) { this.equipements = equipements; }

    @Override
    public String toString() {
        return "Salle " + numero + " [" + type.getLibelle() + "] - Cap. " + capacite
                + (nomBatiment != null ? " - " + nomBatiment : "");
    }
}
