package com.univscheduler.model;

/**
 * Gestionnaire d'emplois du temps : crée, modifie et organise les cours.
 * Hérite de Utilisateur.
 */
public class Gestionnaire extends Utilisateur {

    public Gestionnaire() {
        this.role = RoleUtilisateur.GESTIONNAIRE;
    }

    public Gestionnaire(int id, String nom, String prenom, String email,
                        String login, String motDePasse) {
        super(id, nom, prenom, email, login, motDePasse, RoleUtilisateur.GESTIONNAIRE);
    }

    @Override
    public String[] getDroits() {
        return new String[]{
            "Créer / modifier des cours",
            "Attribuer des salles aux cours",
            "Gérer les créneaux horaires",
            "Détecter les conflits",
            "Consulter tous les emplois du temps"
        };
    }

    @Override
    public String getDescriptionRole() {
        return "Gestionnaire d'emploi du temps : responsable de la planification des cours et des attributions de salles.";
    }
}
