package com.univscheduler.view.panels;

import com.univscheduler.dao.ReservationDAO;
import com.univscheduler.dao.SalleDAO;
import com.univscheduler.model.*;
import com.univscheduler.model.Reservation.StatutReservation;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panneau de gestion des réservations ponctuelles de salles.
 */
public class ReservationsPanel extends JPanel {

    private final Utilisateur utilisateur;
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final SalleDAO salleDAO = new SalleDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    public ReservationsPanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        setBackground(UIUtils.COULEUR_FOND);
        setLayout(new BorderLayout(0, 10));
        initComponents();
        rafraichir();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIUtils.COULEUR_FOND);
        topPanel.add(UIUtils.creerSousTitre("🔖  Réservations de salles"), BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(UIUtils.COULEUR_FOND);

        // Tout le monde peut réserver (sauf étudiant)
        if (utilisateur.getRole() != RoleUtilisateur.ETUDIANT) {
            JButton btnNouvelle = UIUtils.creerBouton("+ Réserver", UIUtils.COULEUR_ACCENT);
            btnNouvelle.addActionListener(e -> nouvelleReservation());
            btns.add(btnNouvelle);
        }

        // Admin / Gestionnaire : approuver/refuser
        if (utilisateur.getRole() == RoleUtilisateur.ADMIN ||
                utilisateur.getRole() == RoleUtilisateur.GESTIONNAIRE) {
            JButton btnApprouver = UIUtils.creerBouton("✓ Approuver", UIUtils.COULEUR_ACCENT);
            JButton btnRefuser   = UIUtils.creerBouton("✗ Refuser", UIUtils.COULEUR_DANGER);
            JButton btnSupprimer = UIUtils.creerBouton("🗑 Supprimer", new Color(192, 57, 43));
            btnApprouver.addActionListener(e -> changerStatut(StatutReservation.APPROUVEE));
            btnRefuser.addActionListener(e -> changerStatut(StatutReservation.REFUSEE));
            btnSupprimer.addActionListener(e -> supprimer());
            btns.add(btnApprouver); btns.add(btnRefuser); btns.add(btnSupprimer);
        } else {
            // Enseignant : annuler ses propres réservations
            JButton btnAnnuler = UIUtils.creerBouton("✗ Annuler", UIUtils.COULEUR_DANGER);
            btnAnnuler.addActionListener(e -> changerStatut(StatutReservation.ANNULEE));
            btns.add(btnAnnuler);
        }
        topPanel.add(btns, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Date", "Début", "Fin", "Salle", "Demandeur", "Motif", "Statut"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styliserTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE));
        add(scroll, BorderLayout.CENTER);
    }

    public void rafraichir() {
        tableModel.setRowCount(0);
        List<Reservation> reservations;
        if (utilisateur.getRole() == RoleUtilisateur.ADMIN ||
                utilisateur.getRole() == RoleUtilisateur.GESTIONNAIRE) {
            reservations = reservationDAO.getTous();
        } else {
            reservations = reservationDAO.getParUtilisateur(utilisateur.getId());
        }
        for (Reservation r : reservations) {
            tableModel.addRow(new Object[]{
                r.getId(), r.getDate(), r.getHeureDebut(), r.getHeureFin(),
                r.getNomSalle() != null ? r.getNomSalle() : "-",
                r.getNomUtilisateur() != null ? r.getNomUtilisateur() : "-",
                r.getMotif(), r.getStatut().getLibelle()
            });
        }
    }

    private void nouvelleReservation() {
        List<Salle> salles = salleDAO.getTous();
        JComboBox<String> cmbSalle = new JComboBox<>(
                salles.stream().map(Salle::toString).toArray(String[]::new));
        cmbSalle.setFont(UIUtils.FONT_LABEL);
        JTextField txtDate  = UIUtils.creerTextField(10); txtDate.setText(LocalDate.now().toString());
        JTextField txtDeb   = UIUtils.creerTextField(6);  txtDeb.setText("08:00");
        JTextField txtFin   = UIUtils.creerTextField(6);  txtFin.setText("10:00");
        JTextField txtMotif = UIUtils.creerTextField(30);

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Salle :")); p.add(cmbSalle);
        p.add(new JLabel("Date (YYYY-MM-DD) :")); p.add(txtDate);
        p.add(new JLabel("Heure début :")); p.add(txtDeb);
        p.add(new JLabel("Heure fin :")); p.add(txtFin);
        p.add(new JLabel("Motif :")); p.add(txtMotif);

        int res = JOptionPane.showConfirmDialog(this, p, "Nouvelle réservation",
                JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        if (txtDate.getText().trim().isEmpty() || txtDeb.getText().trim().isEmpty()) {
            UIUtils.messageErreur(this, "Champs obligatoires manquants.");
            return;
        }
        int salleId = salles.get(Math.max(0, cmbSalle.getSelectedIndex())).getId();
        String dateIso;
        try {
            dateIso = UIUtils.normaliserDateIso(txtDate.getText());
        } catch (IllegalArgumentException e) {
            UIUtils.messageErreur(this, "Date invalide. Utilisez le format YYYY-MM-DD.");
            return;
        }
        try {
            boolean disponible = salleDAO.getSallesDisponiblesPourDate(
                    dateIso,
                    txtDeb.getText().trim(),
                    txtFin.getText().trim()
            ).stream().anyMatch(salle -> salle.getId() == salleId);
            if (!disponible) {
                UIUtils.messageErreur(this, "La salle n'est pas disponible sur ce creneau.");
                return;
            }
        } catch (IllegalArgumentException e) {
            UIUtils.messageErreur(this, e.getMessage());
            return;
        }

        Reservation r = new Reservation();
        r.setSalleId(salleId);
        r.setUtilisateurId(utilisateur.getId());
        r.setDate(dateIso);
        r.setHeureDebut(txtDeb.getText().trim());
        r.setHeureFin(txtFin.getText().trim());
        r.setMotif(txtMotif.getText().trim());
        r.setStatut(StatutReservation.EN_ATTENTE);

        if (reservationDAO.ajouter(r)) {
            UIUtils.messageSucces(this, "Réservation créée (en attente de validation).");
            rafraichir();
        }
    }

    private void changerStatut(StatutReservation statut) {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.messageErreur(this, "Sélectionnez une réservation."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        if (reservationDAO.changerStatut(id, statut)) rafraichir();
    }

    private void supprimer() {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.messageErreur(this, "Sélectionnez une réservation."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        if (UIUtils.confirmer(this, "Supprimer cette réservation ?")) {
            if (reservationDAO.supprimer(id)) rafraichir();
        }
    }
}
