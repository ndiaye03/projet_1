package com.univscheduler.view.panels;

import com.univscheduler.dao.AnnulationCoursDAO;
import com.univscheduler.dao.CoursDAO;
import com.univscheduler.dao.CreneauDAO;
import com.univscheduler.dao.NotificationDAO;
import com.univscheduler.dao.UtilisateurDAO;
import com.univscheduler.model.AnnulationCours;
import com.univscheduler.model.Cours;
import com.univscheduler.model.Creneau;
import com.univscheduler.model.Etudiant;
import com.univscheduler.model.NotificationUtilisateur;
import com.univscheduler.model.RoleUtilisateur;
import com.univscheduler.model.Utilisateur;
import com.univscheduler.util.EmailService;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * Panneau de gestion des cours et des creneaux horaires.
 */
public class CoursPanel extends JPanel {

    private final Utilisateur utilisateur;
    private final CoursDAO coursDAO = new CoursDAO();
    private final CreneauDAO creneauDAO = new CreneauDAO();
    private final AnnulationCoursDAO annulationCoursDAO = new AnnulationCoursDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    private JTable tableCours;
    private DefaultTableModel modelCours;
    private JTable tableCreneaux;
    private DefaultTableModel modelCreneaux;
    private List<Cours> coursList;

    public CoursPanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        setBackground(UIUtils.COULEUR_FOND);
        setLayout(new BorderLayout(0, 10));
        initComponents();
        rafraichir();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIUtils.COULEUR_FOND);
        topPanel.add(UIUtils.creerSousTitre("Cours et creneaux"), BorderLayout.WEST);

        boolean peutEditer = utilisateur.getRole() == RoleUtilisateur.ADMIN
                || utilisateur.getRole() == RoleUtilisateur.GESTIONNAIRE;

        if (peutEditer) {
            JPanel btnsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            btnsPanel.setBackground(UIUtils.COULEUR_FOND);

            JButton btnAjCours = UIUtils.creerBouton("+ Cours", UIUtils.COULEUR_ACCENT);
            JButton btnModCours = UIUtils.creerBouton("Modifier cours", UIUtils.COULEUR_PRIMAIRE);
            JButton btnSupCours = UIUtils.creerBouton("Supprimer cours", UIUtils.COULEUR_DANGER);
            JButton btnAjCren = UIUtils.creerBouton("+ Creneau", new Color(155, 89, 182));
            JButton btnModCren = UIUtils.creerBouton("Modifier le creneau", new Color(142, 68, 173));
            JButton btnSupCren = UIUtils.creerBouton("Supprimer le creneau", new Color(192, 57, 43));
            JButton btnAnnulerDate = UIUtils.creerBouton("Annuler a une date", UIUtils.COULEUR_WARNING);

            btnAjCours.addActionListener(e -> ajouterCours());
            btnModCours.addActionListener(e -> modifierCours());
            btnSupCours.addActionListener(e -> supprimerCours());
            btnAjCren.addActionListener(e -> ajouterCreneau());
            btnModCren.addActionListener(e -> modifierCreneau());
            btnSupCren.addActionListener(e -> supprimerCreneau());
            btnAnnulerDate.addActionListener(e -> annulerCreneauPourUneDate());

            btnsPanel.add(btnAjCours);
            btnsPanel.add(btnModCours);
            btnsPanel.add(btnSupCours);
            btnsPanel.add(Box.createHorizontalStrut(10));
            btnsPanel.add(btnAjCren);
            btnsPanel.add(btnModCren);
            btnsPanel.add(btnSupCren);
            btnsPanel.add(btnAnnulerDate);
            topPanel.add(btnsPanel, BorderLayout.EAST);
        }
        add(topPanel, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(380);
        split.setResizeWeight(0.35);
        split.setBorder(null);

        String[] colsCours = {"ID", "Nom du cours", "Enseignant"};
        modelCours = new DefaultTableModel(colsCours, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tableCours = new JTable(modelCours);
        UIUtils.styliserTable(tableCours);
        tableCours.getColumnModel().getColumn(0).setMaxWidth(40);
        tableCours.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCours.getSelectionModel().addListSelectionListener(e -> afficherCreneaux());

        JPanel leftPanel = new JPanel(new BorderLayout(0, 5));
        leftPanel.setBorder(new EmptyBorder(0, 0, 0, 5));
        leftPanel.setBackground(UIUtils.COULEUR_FOND);
        leftPanel.add(UIUtils.creerPanelTitre("Liste des cours"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(tableCours), BorderLayout.CENTER);

        String[] colsCren = {"ID", "Jour", "Debut", "Fin", "Salle", "Groupe", "Enseignant"};
        modelCreneaux = new DefaultTableModel(colsCren, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tableCreneaux = new JTable(modelCreneaux);
        UIUtils.styliserTable(tableCreneaux);
        tableCreneaux.getColumnModel().getColumn(0).setMaxWidth(40);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 5));
        rightPanel.setBackground(UIUtils.COULEUR_FOND);
        rightPanel.add(UIUtils.creerPanelTitre("Creneaux du cours selectionne"), BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(tableCreneaux), BorderLayout.CENTER);

        split.setLeftComponent(leftPanel);
        split.setRightComponent(rightPanel);
        add(split, BorderLayout.CENTER);
    }

    public void rafraichir() {
        modelCours.setRowCount(0);
        modelCreneaux.setRowCount(0);

        if (utilisateur.getRole() == RoleUtilisateur.ENSEIGNANT) {
            coursList = coursDAO.getParEnseignant(utilisateur.getId());
        } else {
            coursList = coursDAO.getTous();
        }

        for (Cours cours : coursList) {
            modelCours.addRow(new Object[]{
                    cours.getId(),
                    cours.getNom(),
                    cours.getNomEnseignant() != null ? cours.getNomEnseignant() : "-"
            });
        }
    }

    private void afficherCreneaux() {
        modelCreneaux.setRowCount(0);
        int row = tableCours.getSelectedRow();
        if (row < 0) {
            return;
        }

        int coursId = (int) modelCours.getValueAt(row, 0);
        List<Creneau> creneaux = creneauDAO.getTous().stream()
                .filter(cr -> cr.getCoursId() == coursId)
                .toList();

        for (Creneau cr : creneaux) {
            modelCreneaux.addRow(new Object[]{
                    cr.getId(),
                    cr.getJour().getLibelle(),
                    cr.getHeureDebut(),
                    cr.getHeureFin(),
                    cr.getNomSalle() != null ? cr.getNomSalle() : "-",
                    cr.getGroupe(),
                    cr.getNomEnseignant() != null ? cr.getNomEnseignant() : "-"
            });
        }
    }

    private void ajouterCours() {
        FormulaireCoursDialog dialog = new FormulaireCoursDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isConfirme()) {
            rafraichir();
        }
    }

    private void modifierCours() {
        int row = tableCours.getSelectedRow();
        if (row < 0) {
            UIUtils.messageErreur(this, "Selectionnez un cours.");
            return;
        }
        int id = (int) modelCours.getValueAt(row, 0);
        Cours cours = coursDAO.getParId(id);
        FormulaireCoursDialog dialog = new FormulaireCoursDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), cours);
        dialog.setVisible(true);
        if (dialog.isConfirme()) {
            rafraichir();
        }
    }

    private void supprimerCours() {
        int row = tableCours.getSelectedRow();
        if (row < 0) {
            UIUtils.messageErreur(this, "Selectionnez un cours.");
            return;
        }
        int id = (int) modelCours.getValueAt(row, 0);
        String nom = (String) modelCours.getValueAt(row, 1);
        if (UIUtils.confirmer(this, "Supprimer le cours \"" + nom + "\" et tous ses creneaux ?")) {
            if (coursDAO.supprimer(id)) {
                UIUtils.messageSucces(this, "Cours supprime.");
                rafraichir();
            }
        }
    }

    private void ajouterCreneau() {
        int row = tableCours.getSelectedRow();
        if (row < 0) {
            UIUtils.messageErreur(this, "Selectionnez d'abord un cours.");
            return;
        }
        int coursId = (int) modelCours.getValueAt(row, 0);
        FormulaireCreneauDialog dialog = new FormulaireCreneauDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), coursId, null);
        dialog.setVisible(true);
        if (dialog.isConfirme()) {
            afficherCreneaux();
        }
    }

    private void supprimerCreneau() {
        int row = tableCreneaux.getSelectedRow();
        if (row < 0) {
            UIUtils.messageErreur(this, "Selectionnez un creneau.");
            return;
        }
        int id = (int) modelCreneaux.getValueAt(row, 0);
        String jour = String.valueOf(modelCreneaux.getValueAt(row, 1));
        String debut = String.valueOf(modelCreneaux.getValueAt(row, 2));
        String salle = String.valueOf(modelCreneaux.getValueAt(row, 4));
        String message = "Supprimer le creneau du " + jour + " a " + debut + " en salle " + salle + " ?";
        if (UIUtils.confirmer(this, message)) {
            if (creneauDAO.supprimer(id)) {
                UIUtils.messageSucces(this, "Creneau supprime.");
                afficherCreneaux();
            }
        }
    }

    private void modifierCreneau() {
        int row = tableCreneaux.getSelectedRow();
        if (row < 0) {
            UIUtils.messageErreur(this, "Selectionnez un creneau.");
            return;
        }

        int id = (int) modelCreneaux.getValueAt(row, 0);
        Creneau creneau = creneauDAO.getParId(id);
        if (creneau == null) {
            UIUtils.messageErreur(this, "Creneau introuvable.");
            return;
        }

        FormulaireCreneauDialog dialog = new FormulaireCreneauDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), creneau.getCoursId(), creneau);
        dialog.setVisible(true);
        if (dialog.isConfirme()) {
            afficherCreneaux();
        }
    }

    private void annulerCreneauPourUneDate() {
        int row = tableCreneaux.getSelectedRow();
        if (row < 0) {
            UIUtils.messageErreur(this, "Selectionnez un creneau.");
            return;
        }

        int id = (int) modelCreneaux.getValueAt(row, 0);
        Creneau creneau = creneauDAO.getParId(id);
        if (creneau == null) {
            UIUtils.messageErreur(this, "Creneau introuvable.");
            return;
        }

        JTextField txtDate = UIUtils.creerTextField(12);
        txtDate.setText(prochaineOccurrence(creneau.getJour()).toString());

        JTextArea txtMotif = new JTextArea(5, 30);
        txtMotif.setFont(UIUtils.FONT_LABEL);
        txtMotif.setLineWrap(true);
        txtMotif.setWrapStyleWord(true);
        txtMotif.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JPanel formulaire = new JPanel(new BorderLayout(0, 10));
        JPanel champs = new JPanel(new GridLayout(0, 2, 8, 8));
        champs.add(new JLabel("Date (YYYY-MM-DD) :"));
        champs.add(txtDate);
        formulaire.add(champs, BorderLayout.NORTH);
        formulaire.add(new JScrollPane(txtMotif), BorderLayout.CENTER);

        int res = JOptionPane.showConfirmDialog(
                this,
                formulaire,
                "Annuler le cours a une date precise",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        String dateTexte = txtDate.getText().trim();
        String motif = txtMotif.getText().trim();
        if (dateTexte.isEmpty() || motif.isEmpty()) {
            UIUtils.messageErreur(this, "La date et le motif sont obligatoires.");
            return;
        }

        LocalDate dateAnnulation;
        try {
            dateAnnulation = LocalDate.parse(dateTexte);
        } catch (Exception e) {
            UIUtils.messageErreur(this, "Date invalide. Utilisez le format YYYY-MM-DD.");
            return;
        }

        if (!dateCorrespondAuCreneau(dateAnnulation, creneau.getJour())) {
            UIUtils.messageErreur(this,
                    "La date choisie doit correspondre a un " + creneau.getJour().getLibelle() + ".");
            return;
        }

        if (annulationCoursDAO.estCreneauAnnule(creneau.getId(), dateTexte)) {
            UIUtils.messageErreur(this, "Ce cours est deja annule pour cette date.");
            return;
        }

        AnnulationCours annulation = new AnnulationCours();
        annulation.setCreneauId(creneau.getId());
        annulation.setDateAnnulation(dateTexte);
        annulation.setMotif(motif);
        annulation.setAnnuleParId(utilisateur.getId());

        if (!annulationCoursDAO.ajouter(annulation)) {
            UIUtils.messageErreur(this, "Impossible d'enregistrer l'annulation.");
            return;
        }

        int notifications = notifierEtudiants(creneau, dateTexte, motif);
        UIUtils.messageSucces(this,
                "Cours annule pour le " + dateTexte + ". " + notifications + " notification(s) envoyee(s).");
    }

    private int notifierEtudiants(Creneau creneau, String dateAnnulation, String motif) {
        List<Etudiant> etudiants = utilisateurDAO.getEtudiantsParGroupe(creneau.getGroupe());
        int notifications = 0;
        String nomCours = creneau.getNomCours() != null ? creneau.getNomCours() : "Cours";
        String salle = creneau.getNomSalle() != null ? creneau.getNomSalle() : "non attribuee";

        for (Etudiant etudiant : etudiants) {
            String messageInterface = "Le cours " + nomCours
                    + " du " + dateAnnulation
                    + " de " + creneau.getHeureDebut() + " a " + creneau.getHeureFin()
                    + " en salle " + salle
                    + " est annule. Motif : " + motif;

            NotificationUtilisateur notification = new NotificationUtilisateur();
            notification.setUtilisateurId(etudiant.getId());
            notification.setTitre("Cours annule - " + nomCours);
            notification.setMessage(messageInterface);
            notification.setType("ANNULATION_COURS");
            notification.setLue(false);
            notificationDAO.ajouter(notification);

            if (etudiant.getEmail() == null || etudiant.getEmail().isBlank()) {
                notifications++;
                continue;
            }
            String sujet = "Annulation de cours - " + nomCours + " - " + dateAnnulation;
            String contenu = """
                    Bonjour %s,

                    Votre cours "%s" prevu le %s de %s a %s en salle %s est annule.

                    Motif : %s

                    La salle est donc libre sur ce creneau.

                    Administration UNIV-SCHEDULER
                    """.formatted(
                    etudiant.getNomComplet(),
                    nomCours,
                    dateAnnulation,
                    creneau.getHeureDebut(),
                    creneau.getHeureFin(),
                    salle,
                    motif
            );
            EmailService.envoyerEmail(etudiant.getEmail(), sujet, contenu);
            notifications++;
        }
        return notifications;
    }

    private boolean dateCorrespondAuCreneau(LocalDate date, Creneau.Jour jour) {
        return date.getDayOfWeek() == versDayOfWeek(jour);
    }

    private LocalDate prochaineOccurrence(Creneau.Jour jour) {
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() != versDayOfWeek(jour)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private DayOfWeek versDayOfWeek(Creneau.Jour jour) {
        return switch (jour) {
            case LUNDI -> DayOfWeek.MONDAY;
            case MARDI -> DayOfWeek.TUESDAY;
            case MERCREDI -> DayOfWeek.WEDNESDAY;
            case JEUDI -> DayOfWeek.THURSDAY;
            case VENDREDI -> DayOfWeek.FRIDAY;
            case SAMEDI -> DayOfWeek.SATURDAY;
        };
    }
}
