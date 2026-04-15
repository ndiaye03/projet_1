package com.univscheduler.model;

/**
 * Enum représentant les types de salles disponibles dans l'université.
 */
public enum TypeSalle {
    COURS("Salle de cours"),
    AMPHI("Amphithéâtre"),
    LABORATOIRE("Laboratoire informatique"),
    TD("Salle de TD"),
    TP("Salle de TP"),
    CONFERENCE("Salle de conférence"),
    SPORT("Salle de sport");

    private final String libelle;

    TypeSalle(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() { return libelle; }

    @Override
    public String toString() { return libelle; }
}
