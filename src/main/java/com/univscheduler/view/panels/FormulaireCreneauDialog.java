package com.univscheduler.view.panels;

import com.univscheduler.dao.*;
import com.univscheduler.model.*;
import com.univscheduler.model.Creneau.Jour;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Dialog d'ajout d'un créneau horaire avec détection de conflits.
 */
public class FormulaireCreneauDialog extends JDialog {

    private boolean confirme = false;
    private final int coursId;
    private final Creneau creneau;
    private final CreneauDAO creneauDAO = new CreneauDAO();
    private final SalleDAO salleDAO = new SalleDAO();
    private final UtilisateurDAO userDAO = new UtilisateurDAO();
    private final CoursDAO coursDAO = new CoursDAO();

    private JComboBox<String> cmbJour, cmbSalle, cmbEnseignant;
    private JTextField txtDeb, txtFin, txtGroupe;
    private List<Salle> salles;
    private List<Utilisateur> enseignants;

    public FormulaireCreneauDialog(JFrame parent, int coursId, Creneau creneau) {
        super(parent, creneau == null ? "Ajouter un créneau" : "Modifier le créneau", true);
        this.coursId = coursId;
        this.creneau = creneau;
        initComponents();
        if (creneau != null) chargerDonnees();
        pack();
        setResizable(false);
        UIUtils.centrer(this);
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(20, 25, 20, 25));
        main.setBackground(Color.WHITE);

        // Afficher le nom du cours
        Cours cours = coursDAO.getParId(coursId);
        String nomCours = cours != null ? cours.getNom() : "#" + coursId;
        main.add(UIUtils.creerSousTitre("Créneau pour : " + nomCours), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(Color.WHITE);

        // Jours
        String[] joursLib = { "LUNDI","MARDI","MERCREDI","JEUDI","VENDREDI","SAMEDI" };
        cmbJour = new JComboBox<>(joursLib);
        cmbJour.setFont(UIUtils.FONT_LABEL);

        txtDeb = UIUtils.creerTextField(6); txtDeb.setText("08:00");
        txtFin = UIUtils.creerTextField(6); txtFin.setText("10:00");
        txtGroupe = UIUtils.creerTextField(15); txtGroupe.setText("L2-INFO-A");

        // Salles
        salles = salleDAO.getTous();
        cmbSalle = new JComboBox<>(salles.stream().map(Salle::toString).toArray(String[]::new));
        cmbSalle.setFont(UIUtils.FONT_LABEL);

        // Enseignants
        enseignants = userDAO.getParRole(RoleUtilisateur.ENSEIGNANT);
        cmbEnseignant = new JComboBox<>(enseignants.stream()
                .map(Utilisateur::getNomComplet).toArray(String[]::new));
        if (cours != null && cours.getEnseignantId() > 0) {
            for (int i = 0; i < enseignants.size(); i++) {
                if (enseignants.get(i).getId() == cours.getEnseignantId()) {
                    cmbEnseignant.setSelectedIndex(i); break;
                }
            }
        }
        cmbEnseignant.setFont(UIUtils.FONT_LABEL);

        form.add(new JLabel("Jour :")); form.add(cmbJour);
        form.add(new JLabel("Heure début (HH:MM) :")); form.add(txtDeb);
        form.add(new JLabel("Heure fin (HH:MM) :")); form.add(txtFin);
        form.add(new JLabel("Groupe :")); form.add(txtGroupe);
        form.add(new JLabel("Salle :")); form.add(cmbSalle);
        form.add(new JLabel("Enseignant :")); form.add(cmbEnseignant);

        // Bouton vérifier conflits
        JButton btnConflits = UIUtils.creerBouton("🔍 Vérifier conflits", UIUtils.COULEUR_WARNING);
        btnConflits.setForeground(UIUtils.COULEUR_TEXTE);
        btnConflits.addActionListener(e -> verifierConflits());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(Color.WHITE);
        JButton btnOk = UIUtils.creerBouton("Enregistrer", UIUtils.COULEUR_ACCENT);
        JButton btnAnnuler = UIUtils.creerBouton("Annuler", UIUtils.COULEUR_TEXTE_CLAIR);
        btnOk.addActionListener(e -> enregistrer());
        btnAnnuler.addActionListener(e -> dispose());
        btns.add(btnConflits); btns.add(btnOk); btns.add(btnAnnuler);

        main.add(form, BorderLayout.CENTER);
        main.add(btns, BorderLayout.SOUTH);
        add(main);
    }

    private void chargerDonnees() {
        cmbJour.setSelectedItem(creneau.getJour().name());
        txtDeb.setText(creneau.getHeureDebut());
        txtFin.setText(creneau.getHeureFin());
        txtGroupe.setText(creneau.getGroupe());
        for (int i = 0; i < salles.size(); i++) {
            if (salles.get(i).getId() == creneau.getSalleId()) {
                cmbSalle.setSelectedIndex(i); break;
            }
        }
        for (int i = 0; i < enseignants.size(); i++) {
            if (enseignants.get(i).getId() == creneau.getEnseignantId()) {
                cmbEnseignant.setSelectedIndex(i); break;
            }
        }
    }

    /**
     * Vérifie les conflits avant enregistrement.
     */
    private boolean verifierConflits() {
        String jour    = (String) cmbJour.getSelectedItem();
        String debut   = txtDeb.getText().trim();
        String fin     = txtFin.getText().trim();
        String groupe  = txtGroupe.getText().trim();
        int excl = creneau != null ? creneau.getId() : -1;

        int idxSalle = cmbSalle.getSelectedIndex();
        int salleId  = (idxSalle >= 0 && idxSalle < salles.size()) ? salles.get(idxSalle).getId() : 0;
        int idxEns   = cmbEnseignant.getSelectedIndex();
        int ensId    = (idxEns >= 0 && idxEns < enseignants.size()) ? enseignants.get(idxEns).getId() : 0;

        List<Creneau> conflitsSalle = creneauDAO.detecterConflitsSalle(salleId, jour, debut, fin, excl);
        List<Creneau> conflitsEns   = creneauDAO.detecterConflitsEnseignant(ensId, jour, debut, fin, excl);
        List<Creneau> conflitsGrp   = creneauDAO.detecterConflitsGroupe(groupe, jour, debut, fin, excl);

        if (conflitsSalle.isEmpty() && conflitsEns.isEmpty() && conflitsGrp.isEmpty()) {
            UIUtils.messageSucces(this, "✅ Aucun conflit détecté !");
            return false;
        }

        StringBuilder sb = new StringBuilder("⚠️ CONFLITS DÉTECTÉS :\n\n");
        if (!conflitsSalle.isEmpty()) {
            sb.append("🚪 Salle occupée :\n");
            conflitsSalle.forEach(cr -> sb.append("  • ").append(cr).append("\n"));
            sb.append("\n");
        }
        if (!conflitsEns.isEmpty()) {
            sb.append("👤 Enseignant déjà occupé :\n");
            conflitsEns.forEach(cr -> sb.append("  • ").append(cr).append("\n"));
            sb.append("\n");
        }
        if (!conflitsGrp.isEmpty()) {
            sb.append("👥 Groupe déjà en cours :\n");
            conflitsGrp.forEach(cr -> sb.append("  • ").append(cr).append("\n"));
        }

        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false); ta.setFont(UIUtils.FONT_LABEL);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta),
                "Conflits détectés", JOptionPane.WARNING_MESSAGE);
        return true;
    }

    private void enregistrer() {
        if (!validerFormat()) return;

        boolean aDesConflits = verifierConflits();
        if (aDesConflits) {
            int choix = JOptionPane.showConfirmDialog(this,
                    "Des conflits ont été détectés. Forcer l'enregistrement ?",
                    "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choix != JOptionPane.YES_OPTION) return;
        }

        String jour   = (String) cmbJour.getSelectedItem();
        int salleId   = salles.get(Math.max(0, cmbSalle.getSelectedIndex())).getId();
        int ensId     = enseignants.get(Math.max(0, cmbEnseignant.getSelectedIndex())).getId();

        Creneau cr = creneau != null ? creneau : new Creneau();
        cr.setCoursId(coursId);
        cr.setSalleId(salleId);
        cr.setEnseignantId(ensId);
        cr.setJour(Jour.valueOf(jour));
        cr.setHeureDebut(txtDeb.getText().trim());
        cr.setHeureFin(txtFin.getText().trim());
        cr.setGroupe(txtGroupe.getText().trim());

        boolean ok = creneau == null ? creneauDAO.ajouter(cr) : creneauDAO.modifier(cr);
        if (ok) { confirme = true; dispose(); }
        else UIUtils.messageErreur(this, "Erreur lors de l'enregistrement.");
    }

    private boolean validerFormat() {
        String debut = txtDeb.getText().trim();
        String fin   = txtFin.getText().trim();
        if (!debut.matches("\\d{2}:\\d{2}") || !fin.matches("\\d{2}:\\d{2}")) {
            UIUtils.messageErreur(this, "Format horaire invalide. Utilisez HH:MM (ex: 08:00).");
            return false;
        }
        if (txtGroupe.getText().trim().isEmpty()) {
            UIUtils.messageErreur(this, "Le groupe est requis.");
            return false;
        }
        return true;
    }

    public boolean isConfirme() { return confirme; }
}
