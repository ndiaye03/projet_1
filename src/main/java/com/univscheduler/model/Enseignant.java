package com.univscheduler.model;

/**
 * Enseignant : consulte son emploi du temps et effectue des réservations.
 * Hérite de Utilisateur.
 */
public class Enseignant extends Utilisateur {

    private String departement;
    private String specialite;

    public Enseignant() {
        this.role = RoleUtilisateur.ENSEIGNANT;
    }

    public Enseignant(int id, String nom, String prenom, String email,
                      String login, String motDePasse,
                      String departement, String specialite) {
        super(id, nom, prenom, email, login, motDePasse, RoleUtilisateur.ENSEIGNANT);
        this.departement = departement;
        this.specialite = specialite;
    }

    @Override
    public String[] getDroits() {
        return new String[]{
            "Consulter son emploi du temps",
            "Réserver une salle ponctuellement",
            "Consulter les salles disponibles"
        };
    }

    @Override
    public String getDescriptionRole() {
        return "Enseignant : peut consulter son planning et réserver des salles.";
    }

    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
}
