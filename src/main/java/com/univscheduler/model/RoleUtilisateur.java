package com.univscheduler.model;

/**
 * Enum représentant les différents rôles d'utilisateurs dans le système.
 */
public enum RoleUtilisateur {
    ADMIN("Administrateur"),
    GESTIONNAIRE("Gestionnaire d'emploi du temps"),
    ENSEIGNANT("Enseignant"),
    ETUDIANT("Étudiant");

    private final String libelle;

    RoleUtilisateur(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
