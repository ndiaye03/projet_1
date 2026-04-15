package com.univscheduler.model;

/**
 * Amphithéâtre : sous-classe de Salle avec des attributs supplémentaires.
 * Illustre le principe d'héritage.
 */
public class Amphi extends Salle {

    private boolean sonorisation;
    private boolean retransmission; // système de retransmission vidéo

    public Amphi() {
        super();
        this.type = TypeSalle.AMPHI;
    }

    public Amphi(int id, String numero, int capacite, int batimentId,
                 boolean accessible, boolean sonorisation, boolean retransmission) {
        super(id, numero, capacite, TypeSalle.AMPHI, batimentId, accessible);
        this.sonorisation = sonorisation;
        this.retransmission = retransmission;
    }

    public boolean isSonorisation() { return sonorisation; }
    public void setSonorisation(boolean sonorisation) { this.sonorisation = sonorisation; }

    public boolean isRetransmission() { return retransmission; }
    public void setRetransmission(boolean retransmission) { this.retransmission = retransmission; }

    @Override
    public String toString() {
        return "Amphi " + numero + " - Cap. " + capacite
                + (sonorisation ? " [Sono]" : "")
                + (retransmission ? " [Retrans.]" : "")
                + (nomBatiment != null ? " - " + nomBatiment : "");
    }
}
