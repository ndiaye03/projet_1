package com.univscheduler.model;

/**
 * Administrateur : gère les utilisateurs et les infrastructures.
 * Hérite de Utilisateur.
 */
public class Admin extends Utilisateur {

    public Admin() {
        this.role = RoleUtilisateur.ADMIN;
    }

    public Admin(int id, String nom, String prenom, String email,
                 String login, String motDePasse) {
        super(id, nom, prenom, email, login, motDePasse, RoleUtilisateur.ADMIN);
    }

    @Override
    public String[] getDroits() {
        return new String[]{
            "Gérer les utilisateurs",
            "Gérer les bâtiments",
            "Gérer les salles",
            "Gérer les équipements",
            "Consulter tous les emplois du temps",
            "Générer des rapports"
        };
    }

    @Override
    public String getDescriptionRole() {
        return "Administrateur système : accès complet à la gestion des infrastructures et des comptes utilisateurs.";
    }
}
