package com.univscheduler.model;

/**
 * Représente un créneau horaire d'un cours dans l'emploi du temps.
 * Contient les informations de planification : jour, heure, salle, groupe.
 */
public class Creneau {

    public enum Jour {
        LUNDI("Lundi"), MARDI("Mardi"), MERCREDI("Mercredi"),
        JEUDI("Jeudi"), VENDREDI("Vendredi"), SAMEDI("Samedi");

        private final String libelle;
        Jour(String libelle) { this.libelle = libelle; }
        public String getLibelle() { return libelle; }

        @Override
        public String toString() { return libelle; }
    }

    private int id;
    private int coursId;
    private String nomCours;
    private int salleId;
    private String nomSalle;
    private int enseignantId;
    private String nomEnseignant;
    private Jour jour;
    private String heureDebut; // format "HH:MM"
    private String heureFin;   // format "HH:MM"
    private String groupe;     // ex: "L1-INFO-A"
    private String couleurCours;

    public Creneau() {}

    public Creneau(int id, int coursId, int salleId, int enseignantId,
                   Jour jour, String heureDebut, String heureFin, String groupe) {
        this.id = id;
        this.coursId = coursId;
        this.salleId = salleId;
        this.enseignantId = enseignantId;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.groupe = groupe;
    }

    /**
     * Vérifie si ce créneau est en conflit avec un autre
     * (même jour, même salle ou même enseignant, chevauchement horaire).
     */
    public boolean conflictsWithSalle(Creneau autre) {
        if (this.salleId != autre.salleId) return false;
        if (this.jour != autre.jour) return false;
        return chevauchement(this.heureDebut, this.heureFin,
                             autre.heureDebut, autre.heureFin);
    }

    public boolean conflictsWithEnseignant(Creneau autre) {
        if (this.enseignantId != autre.enseignantId) return false;
        if (this.jour != autre.jour) return false;
        return chevauchement(this.heureDebut, this.heureFin,
                             autre.heureDebut, autre.heureFin);
    }

    public boolean conflictsWithGroupe(Creneau autre) {
        if (!this.groupe.equals(autre.groupe)) return false;
        if (this.jour != autre.jour) return false;
        return chevauchement(this.heureDebut, this.heureFin,
                             autre.heureDebut, autre.heureFin);
    }

    /**
     * Détecte le chevauchement entre deux plages horaires.
     */
    private boolean chevauchement(String debut1, String fin1, String debut2, String fin2) {
        int d1 = heureEnMinutes(debut1);
        int f1 = heureEnMinutes(fin1);
        int d2 = heureEnMinutes(debut2);
        int f2 = heureEnMinutes(fin2);
        return d1 < f2 && d2 < f1;
    }

    private int heureEnMinutes(String heure) {
        String[] parts = heure.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    // Getters / Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCoursId() { return coursId; }
    public void setCoursId(int coursId) { this.coursId = coursId; }

    public String getNomCours() { return nomCours; }
    public void setNomCours(String nomCours) { this.nomCours = nomCours; }

    public int getSalleId() { return salleId; }
    public void setSalleId(int salleId) { this.salleId = salleId; }

    public String getNomSalle() { return nomSalle; }
    public void setNomSalle(String nomSalle) { this.nomSalle = nomSalle; }

    public int getEnseignantId() { return enseignantId; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }

    public String getNomEnseignant() { return nomEnseignant; }
    public void setNomEnseignant(String nomEnseignant) { this.nomEnseignant = nomEnseignant; }

    public Jour getJour() { return jour; }
    public void setJour(Jour jour) { this.jour = jour; }

    public String getHeureDebut() { return heureDebut; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }

    public String getHeureFin() { return heureFin; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }

    public String getGroupe() { return groupe; }
    public void setGroupe(String groupe) { this.groupe = groupe; }

    public String getCouleurCours() { return couleurCours; }
    public void setCouleurCours(String couleurCours) { this.couleurCours = couleurCours; }

    @Override
    public String toString() {
        return jour + " " + heureDebut + "-" + heureFin
                + " | " + nomCours
                + " | Salle: " + nomSalle
                + " | Groupe: " + groupe;
    }
}
