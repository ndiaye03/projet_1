package com.univscheduler.view.panels;

import com.univscheduler.dao.BatimentDAO;
import com.univscheduler.model.*;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panneau de gestion des bâtiments.
 */
public class BatimentsPanel extends JPanel {

    private final Utilisateur utilisateur;
    private final BatimentDAO batimentDAO = new BatimentDAO();
    private JTable table;
    private DefaultTableModel tableModel;

    public BatimentsPanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        setBackground(UIUtils.COULEUR_FOND);
        setLayout(new BorderLayout(0, 10));
        initComponents();
        rafraichir();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIUtils.COULEUR_FOND);
        topPanel.add(UIUtils.creerSousTitre("🏛  Gestion des Bâtiments"), BorderLayout.WEST);

        if (utilisateur.getRole() == RoleUtilisateur.ADMIN) {
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            btns.setBackground(UIUtils.COULEUR_FOND);
            JButton btnAjouter   = UIUtils.creerBouton("+ Ajouter", UIUtils.COULEUR_ACCENT);
            JButton btnModifier  = UIUtils.creerBouton("✏ Modifier", UIUtils.COULEUR_PRIMAIRE);
            JButton btnSupprimer = UIUtils.creerBouton("🗑 Supprimer", UIUtils.COULEUR_DANGER);
            btnAjouter.addActionListener(e -> ouvrirFormulaireDialog(null));
            btnModifier.addActionListener(e -> modifier());
            btnSupprimer.addActionListener(e -> supprimer());
            btns.add(btnAjouter); btns.add(btnModifier); btns.add(btnSupprimer);
            topPanel.add(btns, BorderLayout.EAST);
        }
        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Nom", "Adresse", "Étages"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styliserTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(70);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE));
        add(scroll, BorderLayout.CENTER);
    }

    public void rafraichir() {
        tableModel.setRowCount(0);
        for (Batiment b : batimentDAO.getTous()) {
            tableModel.addRow(new Object[]{b.getId(), b.getNom(), b.getAdresse(), b.getNombreEtages()});
        }
    }

    private void ouvrirFormulaireDialog(Batiment b) {
        JTextField txtNom    = UIUtils.creerTextField(20);
        JTextField txtAdresse = UIUtils.creerTextField(30);
        JSpinner spEtages    = new JSpinner(new SpinnerNumberModel(1, 0, 50, 1));
        if (b != null) {
            txtNom.setText(b.getNom());
            txtAdresse.setText(b.getAdresse());
            spEtages.setValue(b.getNombreEtages());
        }
        JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
        p.add(new JLabel("Nom :")); p.add(txtNom);
        p.add(new JLabel("Adresse :")); p.add(txtAdresse);
        p.add(new JLabel("Nbre d'étages :")); p.add(spEtages);
        String titre = b == null ? "Ajouter un bâtiment" : "Modifier le bâtiment";
        int res = JOptionPane.showConfirmDialog(this, p, titre, JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION || txtNom.getText().trim().isEmpty()) return;
        Batiment bat = b != null ? b : new Batiment();
        bat.setNom(txtNom.getText().trim());
        bat.setAdresse(txtAdresse.getText().trim());
        bat.setNombreEtages((int) spEtages.getValue());
        boolean ok = b == null ? batimentDAO.ajouter(bat) : batimentDAO.modifier(bat);
        if (ok) rafraichir();
        else UIUtils.messageErreur(this, "Erreur lors de l'enregistrement.");
    }

    private void modifier() {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.messageErreur(this, "Sélectionnez un bâtiment."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        ouvrirFormulaireDialog(batimentDAO.getParId(id));
    }

    private void supprimer() {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.messageErreur(this, "Sélectionnez un bâtiment."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        String nom = (String) tableModel.getValueAt(row, 1);
        if (UIUtils.confirmer(this, "Supprimer le bâtiment « " + nom + " » et toutes ses salles ?")) {
            if (batimentDAO.supprimer(id)) rafraichir();
        }
    }
}
