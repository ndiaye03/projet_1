package com.univscheduler.view.panels;

import com.univscheduler.dao.BatimentDAO;
import com.univscheduler.dao.EquipementDAO;
import com.univscheduler.dao.NotificationDAO;
import com.univscheduler.dao.SalleDAO;
import com.univscheduler.model.NotificationUtilisateur;
import com.univscheduler.model.RoleUtilisateur;
import com.univscheduler.model.Salle;
import com.univscheduler.model.TypeSalle;
import com.univscheduler.model.Utilisateur;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panneau de consultation des salles.
 */
public class SallesPanel extends JPanel {

    private final Utilisateur utilisateur;
    private final SalleDAO salleDAO = new SalleDAO();
    private final BatimentDAO batimentDAO = new BatimentDAO();
    private final EquipementDAO equipementDAO = new EquipementDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private List<Salle> salles;
    private JComboBox<String> cmbBatiment;
    private JComboBox<String> cmbType;
    private JTextField txtCapaciteMin;

    public SallesPanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        setBackground(UIUtils.COULEUR_FOND);
        setLayout(new BorderLayout(0, 10));
        initComponents();
        rafraichir();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBackground(UIUtils.COULEUR_FOND);
        topPanel.add(UIUtils.creerSousTitre("Salles"), BorderLayout.WEST);

        if (utilisateur.getRole() == RoleUtilisateur.ADMIN) {
            JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actionsPanel.setBackground(UIUtils.COULEUR_FOND);

            JButton btnAjouter = UIUtils.creerBouton("+ Ajouter", UIUtils.COULEUR_ACCENT);
            JButton btnModifier = UIUtils.creerBouton("Modifier", UIUtils.COULEUR_PRIMAIRE);
            JButton btnSupprimer = UIUtils.creerBouton("Supprimer", UIUtils.COULEUR_DANGER);

            btnAjouter.addActionListener(e -> ouvrirFormulaire(null));
            btnModifier.addActionListener(e -> modifierSalle());
            btnSupprimer.addActionListener(e -> supprimerSalle());

            actionsPanel.add(btnAjouter);
            actionsPanel.add(btnModifier);
            actionsPanel.add(btnSupprimer);
            topPanel.add(actionsPanel, BorderLayout.EAST);
        }

        JPanel filtresPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtresPanel.setBackground(UIUtils.COULEUR_FOND);

        cmbBatiment = new JComboBox<>();
        cmbBatiment.addItem("Tous les batiments");
        batimentDAO.getTous().forEach(b -> cmbBatiment.addItem(b.getNom()));
        cmbBatiment.setFont(UIUtils.FONT_LABEL);
        cmbBatiment.addActionListener(e -> rafraichir());

        cmbType = new JComboBox<>();
        cmbType.addItem("Tous les types");
        for (TypeSalle typeSalle : TypeSalle.values()) {
            cmbType.addItem(typeSalle.getLibelle());
        }
        cmbType.setFont(UIUtils.FONT_LABEL);
        cmbType.addActionListener(e -> rafraichir());

        txtCapaciteMin = UIUtils.creerTextField(4);
        txtCapaciteMin.setMaximumSize(new Dimension(60, 30));
        txtCapaciteMin.setToolTipText("Capacite minimale");
        txtCapaciteMin.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                rafraichir();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                rafraichir();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        filtresPanel.add(new JLabel("Batiment:"));
        filtresPanel.add(cmbBatiment);
        filtresPanel.add(new JLabel("Type:"));
        filtresPanel.add(cmbType);
        filtresPanel.add(new JLabel("Cap. min:"));
        filtresPanel.add(txtCapaciteMin);
        topPanel.add(filtresPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        String[] colonnes = {"ID", "Numero", "Type", "Capacite", "Batiment", "Equipements"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UIUtils.styliserTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(80);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE));
        add(scroll, BorderLayout.CENTER);

        JPanel piedPanel = new JPanel(new BorderLayout(0, 10));
        piedPanel.setBackground(UIUtils.COULEUR_FOND);

        JPanel actionsBas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsBas.setBackground(UIUtils.COULEUR_FOND);
        JButton btnDisponibles = UIUtils.creerBouton("Salles disponibles", new Color(39, 174, 96));
        btnDisponibles.addActionListener(e -> rechercherDisponibles());
        actionsBas.add(btnDisponibles);
        piedPanel.add(actionsBas, BorderLayout.NORTH);

        if (utilisateur.getRole() == RoleUtilisateur.ETUDIANT) {
            piedPanel.add(creerBlocNotifications(), BorderLayout.SOUTH);
        }
        add(piedPanel, BorderLayout.SOUTH);
    }

    public void rafraichir() {
        tableModel.setRowCount(0);
        salles = salleDAO.getTous();

        String batimentFiltre = (String) cmbBatiment.getSelectedItem();
        String typeFiltre = (String) cmbType.getSelectedItem();
        String capMin = txtCapaciteMin.getText().trim();
        int capaciteMin = 0;
        try {
            capaciteMin = Integer.parseInt(capMin);
        } catch (NumberFormatException ignored) {
        }

        for (Salle salle : salles) {
            boolean ok = true;
            if (!"Tous les batiments".equals(batimentFiltre)
                    && !batimentFiltre.equals(salle.getNomBatiment())) {
                ok = false;
            }
            if (!"Tous les types".equals(typeFiltre)
                    && !typeFiltre.equals(salle.getType().getLibelle())) {
                ok = false;
            }
            if (salle.getCapacite() < capaciteMin) {
                ok = false;
            }
            if (!ok) {
                continue;
            }

            List<com.univscheduler.model.Equipement> eq = equipementDAO.getParSalle(salle.getId());
            String eqStr = eq.stream()
                    .map(com.univscheduler.model.Equipement::getNom)
                    .limit(3)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("-");
            if (eq.size() > 3) {
                eqStr += "...";
            }

            tableModel.addRow(new Object[]{
                    salle.getId(),
                    salle.getNumero(),
                    salle.getType().getLibelle(),
                    salle.getCapacite(),
                    salle.getNomBatiment() != null ? salle.getNomBatiment() : "-",
                    eqStr
            });
        }
    }

    private void rechercherDisponibles() {
        JTextField txtDate = UIUtils.creerTextField(10);
        txtDate.setText(LocalDate.now().toString());
        JTextField txtDeb = UIUtils.creerTextField(5);
        txtDeb.setText("08:00");
        JTextField txtFin = UIUtils.creerTextField(5);
        txtFin.setText("10:00");

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Date (YYYY-MM-DD) :"));
        panel.add(txtDate);
        panel.add(new JLabel("Heure debut :"));
        panel.add(txtDeb);
        panel.add(new JLabel("Heure fin :"));
        panel.add(txtFin);

        int res = JOptionPane.showConfirmDialog(this, panel, "Rechercher salles disponibles",
                JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        String dateIso;
        try {
            dateIso = UIUtils.normaliserDateIso(txtDate.getText());
        } catch (IllegalArgumentException e) {
            UIUtils.messageErreur(this, "Date invalide. Utilisez le format YYYY-MM-DD.");
            return;
        }

        List<Salle> dispo;
        try {
            dispo = salleDAO.getSallesDisponiblesPourDate(
                    dateIso, txtDeb.getText().trim(), txtFin.getText().trim());
        } catch (IllegalArgumentException e) {
            UIUtils.messageErreur(this, e.getMessage());
            return;
        }

        StringBuilder sb = new StringBuilder(
                "Salles disponibles le " + dateIso + " (" + dispo.size() + ") :\n\n");
        dispo.forEach(s -> sb.append("- ").append(s).append("\n"));
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(UIUtils.FONT_LABEL);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Resultats",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void ouvrirFormulaire(Salle salle) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JFrame frameParent = parent instanceof JFrame ? (JFrame) parent : null;
        FormulaireSalleDialog dialog = new FormulaireSalleDialog(frameParent, salle);
        dialog.setVisible(true);
        if (dialog.isConfirme()) {
            rafraichir();
        }
    }

    private void modifierSalle() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.messageErreur(this, "Selectionnez une salle.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        Salle salle = salleDAO.getParId(id);
        if (salle == null) {
            UIUtils.messageErreur(this, "Salle introuvable.");
            return;
        }

        ouvrirFormulaire(salle);
    }

    private void supprimerSalle() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.messageErreur(this, "Selectionnez une salle.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String numero = (String) tableModel.getValueAt(row, 1);
        if (UIUtils.confirmer(this, "Supprimer la salle \"" + numero + "\" ?")) {
            if (salleDAO.supprimer(id)) {
                rafraichir();
            } else {
                UIUtils.messageErreur(this, "Impossible de supprimer la salle.");
            }
        }
    }

    private JComponent creerBlocNotifications() {
        List<NotificationUtilisateur> notifications = notificationDAO.getParUtilisateur(utilisateur.getId(), 5);

        JPanel bloc = new JPanel(new BorderLayout(0, 8));
        bloc.setBackground(new Color(255, 250, 235));
        bloc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(241, 196, 15)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel titre = new JLabel("Notifications de cours");
        titre.setFont(UIUtils.FONT_HEADING);
        titre.setForeground(new Color(176, 117, 0));
        bloc.add(titre, BorderLayout.NORTH);

        JPanel liste = new JPanel();
        liste.setLayout(new BoxLayout(liste, BoxLayout.Y_AXIS));
        liste.setOpaque(false);

        if (notifications.isEmpty()) {
            JLabel vide = new JLabel("Aucune notification recente.");
            vide.setFont(UIUtils.FONT_LABEL);
            vide.setForeground(UIUtils.COULEUR_TEXTE_CLAIR);
            liste.add(vide);
        } else {
            for (NotificationUtilisateur notification : notifications) {
                JPanel item = new JPanel(new BorderLayout(0, 4));
                item.setOpaque(false);
                item.setBorder(new EmptyBorder(0, 0, 8, 0));

                JLabel lblTitre = new JLabel(notification.getTitre());
                lblTitre.setFont(UIUtils.FONT_BOLD);
                lblTitre.setForeground(UIUtils.COULEUR_TEXTE);

                JLabel lblMessage = new JLabel("<html>" + notification.getMessage() + "</html>");
                lblMessage.setFont(UIUtils.FONT_LABEL);
                lblMessage.setForeground(UIUtils.COULEUR_TEXTE);

                JLabel lblDate = new JLabel(notification.getDateCreation());
                lblDate.setFont(UIUtils.FONT_SMALL);
                lblDate.setForeground(UIUtils.COULEUR_TEXTE_CLAIR);

                item.add(lblTitre, BorderLayout.NORTH);
                item.add(lblMessage, BorderLayout.CENTER);
                item.add(lblDate, BorderLayout.SOUTH);
                liste.add(item);
            }
        }

        bloc.add(liste, BorderLayout.CENTER);
        return bloc;
    }
}
