package com.univscheduler.view.panels;

import com.univscheduler.dao.EquipementDAO;
import com.univscheduler.model.*;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dialog de gestion des équipements d'une salle.
 */
public class EquipementsDialog extends JDialog {

    private final int salleId;
    private final String numeroSalle;
    private final Utilisateur utilisateur;
    private final EquipementDAO equipementDAO = new EquipementDAO();
    private JTable table;
    private DefaultTableModel tableModel;

    public EquipementsDialog(JFrame parent, int salleId, String numeroSalle, Utilisateur utilisateur) {
        super(parent, "Équipements — Salle " + numeroSalle, true);
        this.salleId = salleId;
        this.numeroSalle = numeroSalle;
        this.utilisateur = utilisateur;
        setSize(550, 400);
        initComponents();
        charger();
        UIUtils.centrer(this);
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(15, 15, 15, 15));
        main.setBackground(Color.WHITE);

        main.add(UIUtils.creerSousTitre("🔧 Équipements - Salle " + numeroSalle), BorderLayout.NORTH);

        String[] cols = {"ID", "Équipement", "Description", "État"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styliserTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(3).setMaxWidth(80);
        main.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(Color.WHITE);

        if (utilisateur.getRole() == RoleUtilisateur.ADMIN ||
                utilisateur.getRole() == RoleUtilisateur.GESTIONNAIRE) {
            JButton btnAjouter   = UIUtils.creerBouton("+ Ajouter", UIUtils.COULEUR_ACCENT);
            JButton btnSupprimer = UIUtils.creerBouton("🗑 Supprimer", UIUtils.COULEUR_DANGER);
            JButton btnToggle    = UIUtils.creerBouton("⟳ État", UIUtils.COULEUR_PRIMAIRE);
            btnAjouter.addActionListener(e -> ajouterEquipement());
            btnSupprimer.addActionListener(e -> supprimerEquipement());
            btnToggle.addActionListener(e -> toggleEtat());
            btns.add(btnAjouter); btns.add(btnToggle); btns.add(btnSupprimer);
        }
        JButton btnFermer = UIUtils.creerBouton("Fermer", UIUtils.COULEUR_TEXTE_CLAIR);
        btnFermer.addActionListener(e -> dispose());
        btns.add(btnFermer);
        main.add(btns, BorderLayout.SOUTH);
        add(main);
    }

    private void charger() {
        tableModel.setRowCount(0);
        for (Equipement e : equipementDAO.getParSalle(salleId)) {
            tableModel.addRow(new Object[]{
                e.getId(), e.getNom(), e.getDescription() != null ? e.getDescription() : "",
                e.isFonctionnel() ? "✓ Fonctionnel" : "✗ Hors service"
            });
        }
    }

    private void ajouterEquipement() {
        JTextField txtNom = UIUtils.creerTextField(15);
        JTextField txtDesc = UIUtils.creerTextField(20);
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.add(new JLabel("Nom :")); p.add(txtNom);
        p.add(new JLabel("Description :")); p.add(txtDesc);
        int res = JOptionPane.showConfirmDialog(this, p, "Ajouter un équipement", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION && !txtNom.getText().trim().isEmpty()) {
            Equipement eq = new Equipement(0, txtNom.getText().trim(), txtDesc.getText().trim(), salleId, true);
            if (equipementDAO.ajouter(eq)) charger();
        }
    }

    private void supprimerEquipement() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        if (UIUtils.confirmer(this, "Supprimer cet équipement ?")) {
            equipementDAO.supprimer(id);
            charger();
        }
    }

    private void toggleEtat() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        String etat = (String) tableModel.getValueAt(row, 3);
        boolean nouvelEtat = etat.contains("Hors service");
        // Mise à jour en base
        List<Equipement> eqs = equipementDAO.getParSalle(salleId);
        eqs.stream().filter(e -> e.getId() == id).findFirst().ifPresent(e -> {
            e.setFonctionnel(nouvelEtat);
            equipementDAO.modifier(e);
        });
        charger();
    }
}
