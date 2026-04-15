package com.univscheduler.view.panels;

import com.univscheduler.dao.UtilisateurDAO;
import com.univscheduler.model.*;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panneau de gestion des utilisateurs (Admin uniquement).
 */
public class UtilisateursPanel extends JPanel {

    private final Utilisateur utilisateur;
    private final UtilisateurDAO userDAO = new UtilisateurDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbFiltreRole;

    public UtilisateursPanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        setBackground(UIUtils.COULEUR_FOND);
        setLayout(new BorderLayout(0, 10));
        initComponents();
        rafraichir();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBackground(UIUtils.COULEUR_FOND);
        topPanel.add(UIUtils.creerSousTitre("👥  Gestion des Utilisateurs"), BorderLayout.WEST);

        JPanel filtrePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtrePanel.setBackground(UIUtils.COULEUR_FOND);
        cmbFiltreRole = new JComboBox<>(new String[]{"Tous", "ADMIN", "GESTIONNAIRE", "ENSEIGNANT", "ETUDIANT"});
        cmbFiltreRole.setFont(UIUtils.FONT_LABEL);
        cmbFiltreRole.addActionListener(e -> rafraichir());
        filtrePanel.add(new JLabel("Rôle :"));
        filtrePanel.add(cmbFiltreRole);
        topPanel.add(filtrePanel, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(UIUtils.COULEUR_FOND);
        JButton btnAjouter   = UIUtils.creerBouton("+ Ajouter", UIUtils.COULEUR_ACCENT);
        JButton btnModifier  = UIUtils.creerBouton("✏ Modifier", UIUtils.COULEUR_PRIMAIRE);
        JButton btnSupprimer = UIUtils.creerBouton("🗑 Supprimer", UIUtils.COULEUR_DANGER);
        JButton btnMdp       = UIUtils.creerBouton("🔑 MdP", new Color(230, 126, 34));
        btnAjouter.addActionListener(e -> ouvrirFormulaireUtilisateur(null));
        btnModifier.addActionListener(e -> modifierUtilisateur());
        btnSupprimer.addActionListener(e -> supprimerUtilisateur());
        btnMdp.addActionListener(e -> changerMotDePasse());
        btns.add(btnAjouter); btns.add(btnModifier); btns.add(btnSupprimer); btns.add(btnMdp);
        topPanel.add(btns, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Nom", "Prénom", "Login", "Email", "Rôle", "Infos"};
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
        String roleFiltre = (String) cmbFiltreRole.getSelectedItem();
        List<Utilisateur> users = "Tous".equals(roleFiltre)
                ? userDAO.getTous()
                : userDAO.getParRole(RoleUtilisateur.valueOf(roleFiltre));
        for (Utilisateur u : users) {
            String infos = "";
            if (u instanceof Enseignant e) infos = e.getDepartement() != null ? e.getDepartement() : "";
            else if (u instanceof Etudiant et) infos = (et.getGroupe() != null ? et.getGroupe() : "") + " " + et.getNiveauLibelle();
            tableModel.addRow(new Object[]{
                u.getId(), u.getNom(), u.getPrenom(), u.getLogin(),
                u.getEmail() != null ? u.getEmail() : "-",
                u.getRole().getLibelle(), infos
            });
        }
    }

    private void ouvrirFormulaireUtilisateur(Utilisateur u) {
        FormulaireUtilisateurDialog dialog = new FormulaireUtilisateurDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), u);
        dialog.setVisible(true);
        if (dialog.isConfirme()) rafraichir();
    }

    private void modifierUtilisateur() {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.messageErreur(this, "Sélectionnez un utilisateur."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        ouvrirFormulaireUtilisateur(userDAO.getParId(id));
    }

    private void supprimerUtilisateur() {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.messageErreur(this, "Sélectionnez un utilisateur."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        if (id == utilisateur.getId()) { UIUtils.messageErreur(this, "Impossible de supprimer votre propre compte."); return; }
        String login = (String) tableModel.getValueAt(row, 3);
        if (UIUtils.confirmer(this, "Supprimer l'utilisateur « " + login + " » ?")) {
            if (userDAO.supprimer(id)) rafraichir();
        }
    }

    private void changerMotDePasse() {
        int row = table.getSelectedRow();
        if (row < 0) { UIUtils.messageErreur(this, "Sélectionnez un utilisateur."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        JPasswordField pf = new JPasswordField(15);
        int ok = JOptionPane.showConfirmDialog(this, pf, "Nouveau mot de passe", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            String mdp = new String(pf.getPassword()).trim();
            if (mdp.length() >= 4) { userDAO.modifierMotDePasse(id, mdp); UIUtils.messageSucces(this, "Mot de passe modifié."); }
            else UIUtils.messageErreur(this, "Mot de passe trop court (min 4 caractères).");
        }
    }
}
