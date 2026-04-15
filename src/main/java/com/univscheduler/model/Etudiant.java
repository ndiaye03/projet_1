package com.univscheduler.model;

/**
 * Étudiant : consulte l'emploi du temps de sa classe et recherche des salles.
 * Hérite de Utilisateur.
 */
public class Etudiant extends Utilisateur {

    private String groupe;
    private String filiere;
    private int niveau; // 1=L1, 2=L2, 3=L3, 4=M1, 5=M2...

    public Etudiant() {
        this.role = RoleUtilisateur.ETUDIANT;
    }

    public Etudiant(int id, String nom, String prenom, String email,
                    String login, String motDePasse,
                    String groupe, String filiere, int niveau) {
        super(id, nom, prenom, email, login, motDePasse, RoleUtilisateur.ETUDIANT);
        this.groupe = groupe;
        this.filiere = filiere;
        this.niveau = niveau;
    }

    @Override
    public String[] getDroits() {
        return new String[]{
            "Consulter l'emploi du temps de son groupe",
            "Rechercher des salles disponibles"
        };
    }

    @Override
    public String getDescriptionRole() {
        return "Étudiant : accès en lecture seule à l'emploi du temps de son groupe.";
    }

    public String getGroupe() { return groupe; }
    public void setGroupe(String groupe) { this.groupe = groupe; }

    public String getFiliere() { return filiere; }
   // public void setFiliere(String filiere) { this.filiere = filiere; }

    public int getNiveau() { return niveau; }
   // public void setNiveau(int niveau) { this.niveau = niveau; }

    public String getNiveauLibelle() {
        return switch (niveau) {
            case 1 -> "Licence 1";
            case 2 -> "Licence 2";
            case 3 -> "Licence 3";
            case 4 -> "Master 1";
            case 5 -> "Master 2";
            default -> "Niveau " + niveau;
        };
    }
}
