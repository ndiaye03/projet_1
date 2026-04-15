package com.univscheduler.model;

/**
 * Laboratoire informatique : sous-classe de Salle.
 */
public class Laboratoire extends Salle {

    private int nombrePostes;
    private String systemeExploitation;

    public Laboratoire() {
        super();
        this.type = TypeSalle.LABORATOIRE;
    }

    public Laboratoire(int id, String numero, int capacite, int batimentId,
                       boolean accessible, int nombrePostes, String systemeExploitation) {
        super(id, numero, capacite, TypeSalle.LABORATOIRE, batimentId, accessible);
        this.nombrePostes = nombrePostes;
        this.systemeExploitation = systemeExploitation;
    }

    public int getNombrePostes() { return nombrePostes; }
    public void setNombrePostes(int nombrePostes) { this.nombrePostes = nombrePostes; }

    public String getSystemeExploitation() { return systemeExploitation; }
    public void setSystemeExploitation(String se) { this.systemeExploitation = se; }

    @Override
    public String toString() {
        return "Labo " + numero + " [" + nombrePostes + " postes - " + systemeExploitation + "]"
                + (nomBatiment != null ? " - " + nomBatiment : "");
    }
}
