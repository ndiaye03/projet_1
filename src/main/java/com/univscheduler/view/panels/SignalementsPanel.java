package com.univscheduler.view.panels;

import com.univscheduler.dao.SignalementDAO;
import com.univscheduler.model.RoleUtilisateur;
import com.univscheduler.model.Signalement;
import com.univscheduler.model.Utilisateur;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panneau de suivi des signalements.
 */
public class SignalementsPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Utilisateur utilisateur;
    private final SignalementDAO signalementDAO = new SignalementDAO();

    private JTextField txtSujet;
    private JTextArea txtDescription;
    private JTable table;
    private DefaultTableModel tableModel;

    public SignalementsPanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        setLayout(new BorderLayout(0, 12));
        setBackground(UIUtils.COULEUR_FOND);
        initComponents();
        rafraichir();
    }

    private void initComponents() {
        add(UIUtils.creerSousTitre(utilisateur.getRole() == RoleUtilisateur.ADMIN
                ? "Signalements recus"
                : "Signaler un probleme"), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        if (utilisateur.getRole() == RoleUtilisateur.ENSEIGNANT) {
            centerPanel.add(creerFormulaireSignalement(), BorderLayout.NORTH);
        }

        centerPanel.add(creerTableau(), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel creerFormulaireSignalement() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE),
                new EmptyBorder(16, 16, 16, 16)
        ));

        txtSujet = UIUtils.creerTextField(25);
        txtSujet.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        txtDescription = new JTextArea(5, 25);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setFont(UIUtils.FONT_LABEL);
        txtDescription.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JButton btnEnvoyer = UIUtils.creerBouton("Envoyer le signalement", UIUtils.COULEUR_WARNING);
        btnEnvoyer.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnEnvoyer.addActionListener(e -> envoyerSignalement());

        form.add(new JLabel("Sujet"));
        form.add(Box.createVerticalStrut(5));
        form.add(txtSujet);
        form.add(Box.createVerticalStrut(12));
        form.add(new JLabel("Description du probleme"));
        form.add(Box.createVerticalStrut(5));
        form.add(new JScrollPane(txtDescription));
        form.add(Box.createVerticalStrut(12));
        form.add(btnEnvoyer);
        return form;
    }

    private JPanel creerTableau() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnRafraichir = UIUtils.creerBouton("Rafraichir", UIUtils.COULEUR_PRIMAIRE);
        btnRafraichir.addActionListener(e -> rafraichir());
        actions.add(btnRafraichir);

        if (utilisateur.getRole() == RoleUtilisateur.ADMIN) {
            JButton btnTraiter = UIUtils.creerBouton("Marquer traite", UIUtils.COULEUR_ACCENT);
            btnTraiter.addActionListener(e -> marquerCommeTraite());
            actions.add(btnTraiter);
        }

        String[] cols = {"ID", "Date", "Auteur", "Sujet", "Statut", "Description"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UIUtils.styliserTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE));

        panel.add(actions, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    public void rafraichir() {
        tableModel.setRowCount(0);
        List<Signalement> signalements = utilisateur.getRole() == RoleUtilisateur.ADMIN
                ? signalementDAO.getTous()
                : signalementDAO.getParUtilisateur(utilisateur.getId());

        for (Signalement signalement : signalements) {
            tableModel.addRow(new Object[]{
                    signalement.getId(),
                    signalement.getDateCreation(),
                    signalement.getNomUtilisateur(),
                    signalement.getSujet(),
                    signalement.getStatut(),
                    signalement.getDescription()
            });
        }
    }

    private void envoyerSignalement() {
        String sujet = txtSujet.getText().trim();
        String description = txtDescription.getText().trim();
        if (sujet.isEmpty() || description.isEmpty()) {
            UIUtils.messageErreur(this, "Le sujet et la description sont obligatoires.");
            return;
        }

        Signalement signalement = new Signalement();
        signalement.setUtilisateurId(utilisateur.getId());
        signalement.setSujet(sujet);
        signalement.setDescription(description);
        signalement.setStatut("NOUVEAU");
        signalement.setDateCreation(LocalDateTime.now().format(DATE_FORMAT));

        if (signalementDAO.ajouter(signalement)) {
            txtSujet.setText("");
            txtDescription.setText("");
            UIUtils.messageSucces(this, "Signalement envoye a l'administration.");
            rafraichir();
        } else {
            UIUtils.messageErreur(this, "Impossible d'envoyer le signalement.");
        }
    }

    private void marquerCommeTraite() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.messageErreur(this, "Selectionnez un signalement.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        if (signalementDAO.modifierStatut(id, "TRAITE")) {
            UIUtils.messageSucces(this, "Signalement marque comme traite.");
            rafraichir();
        } else {
            UIUtils.messageErreur(this, "Impossible de modifier le statut.");
        }
    }
}
