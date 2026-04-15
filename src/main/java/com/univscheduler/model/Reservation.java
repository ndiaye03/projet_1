package com.univscheduler.model;

/**
 * Représente une réservation ponctuelle d'une salle par un enseignant.
 */
public class Reservation {

    public enum StatutReservation {
        EN_ATTENTE("En attente"),
        APPROUVEE("Approuvée"),
        REFUSEE("Refusée"),
        ANNULEE("Annulée");

        private final String libelle;
        StatutReservation(String libelle) { this.libelle = libelle; }
        public String getLibelle() { return libelle; }

        @Override
        public String toString() { return libelle; }
    }

    private int id;
    private int salleId;
    private String nomSalle;
    private int utilisateurId;
    private String nomUtilisateur;
    private String date;       // format "YYYY-MM-DD"
    private String heureDebut; // format "HH:MM"
    private String heureFin;   // format "HH:MM"
    private String motif;
    private StatutReservation statut;

    public Reservation() {
        this.statut = StatutReservation.EN_ATTENTE;
    }

    public Reservation(int id, int salleId, int utilisateurId,
                       String date, String heureDebut, String heureFin,
                       String motif, StatutReservation statut) {
        this.id = id;
        this.salleId = salleId;
        this.utilisateurId = utilisateurId;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.motif = motif;
        this.statut = statut;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSalleId() { return salleId; }
    public void setSalleId(int salleId) { this.salleId = salleId; }

    public String getNomSalle() { return nomSalle; }
    public void setNomSalle(String nomSalle) { this.nomSalle = nomSalle; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getHeureDebut() { return heureDebut; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }

    public String getHeureFin() { return heureFin; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public StatutReservation getStatut() { return statut; }
    public void setStatut(StatutReservation statut) { this.statut = statut; }

    @Override
    public String toString() {
        return date + " " + heureDebut + "-" + heureFin
                + " | Salle: " + nomSalle
                + " | " + nomUtilisateur
                + " | " + statut;
    }
}
