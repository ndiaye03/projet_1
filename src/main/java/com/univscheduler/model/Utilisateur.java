package com.univscheduler.model;

/**
 * Classe abstraite de base représentant un utilisateur du système.
 * Démontre le concept d'héritage et de polymorphisme en POO.
 */
public abstract class Utilisateur {

    protected int id;
    protected String nom;
    protected String prenom;
    protected String email;
    protected String login;
    protected String motDePasse;
    protected RoleUtilisateur role;

    public Utilisateur() {}

    public Utilisateur(int id, String nom, String prenom, String email,
                       String login, String motDePasse, RoleUtilisateur role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.login = login;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // Méthode abstraite : chaque sous-classe définit ses droits
    public abstract String[] getDroits();

    // Méthode abstraite : description du rôle
    public abstract String getDescriptionRole();

    // Getters / Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public RoleUtilisateur getRole() { return role; }
    public void setRole(RoleUtilisateur role) { this.role = role; }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return "[" + role + "] " + getNomComplet() + " (" + login + ")";
    }
}
