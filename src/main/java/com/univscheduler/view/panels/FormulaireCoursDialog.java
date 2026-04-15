package com.univscheduler.view.panels;

import com.univscheduler.dao.CoursDAO;
import com.univscheduler.dao.UtilisateurDAO;
import com.univscheduler.model.*;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Dialog d'ajout/modification d'un cours.
 */
public class FormulaireCoursDialog extends JDialog {

    private boolean confirme = false;
    private final Cours cours;
    private final CoursDAO coursDAO = new CoursDAO();
    private final UtilisateurDAO userDAO = new UtilisateurDAO();

    private JTextField txtNom, txtDesc;
    private JComboBox<String> cmbEnseignant;
    private JTextField txtCouleur;
    private List<Utilisateur> enseignants;

    public FormulaireCoursDialog(JFrame parent, Cours cours) {
        super(parent, cours == null ? "Nouveau cours" : "Modifier le cours", true);
        this.cours = cours;
        initComponents();
        if (cours != null) chargerDonnees();
        pack();
        setResizable(false);
        UIUtils.centrer(this);
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(20, 25, 15, 25));
        main.setBackground(Color.WHITE);

        enseignants = userDAO.getParRole(RoleUtilisateur.ENSEIGNANT);
        String[] noms = enseignants.stream()
                .map(Utilisateur::getNomComplet).toArray(String[]::new);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(Color.WHITE);

        txtNom      = UIUtils.creerTextField(20);
        txtDesc     = UIUtils.creerTextField(30);
        cmbEnseignant = new JComboBox<>(noms);
        cmbEnseignant.setFont(UIUtils.FONT_LABEL);
        txtCouleur  = UIUtils.creerTextField(8);
        txtCouleur.setText("#4A90D9");

        form.add(new JLabel("Nom du cours :")); form.add(txtNom);
        form.add(new JLabel("Description :")); form.add(txtDesc);
        form.add(new JLabel("Enseignant :")); form.add(cmbEnseignant);
        form.add(new JLabel("Couleur (hex) :")); form.add(txtCouleur);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(Color.WHITE);
        JButton btnOk = UIUtils.creerBouton("Enregistrer", UIUtils.COULEUR_ACCENT);
        JButton btnAnnuler = UIUtils.creerBouton("Annuler", UIUtils.COULEUR_TEXTE_CLAIR);
        btnOk.addActionListener(e -> enregistrer());
        btnAnnuler.addActionListener(e -> dispose());
        btns.add(btnOk); btns.add(btnAnnuler);

        main.add(UIUtils.creerSousTitre(cours == null ? "Nouveau cours" : "Modifier"), BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);
        main.add(btns, BorderLayout.SOUTH);
        add(main);
    }

    private void chargerDonnees() {
        txtNom.setText(cours.getNom());
        txtDesc.setText(cours.getDescription() != null ? cours.getDescription() : "");
        txtCouleur.setText(cours.getCouleur() != null ? cours.getCouleur() : "#4A90D9");
        for (int i = 0; i < enseignants.size(); i++) {
            if (enseignants.get(i).getId() == cours.getEnseignantId()) {
                cmbEnseignant.setSelectedIndex(i); break;
            }
        }
    }

    private void enregistrer() {
        String nom = txtNom.getText().trim();
        if (nom.isEmpty()) { UIUtils.messageErreur(this, "Nom requis."); return; }
        int ensId = enseignants.isEmpty() ? 0 : enseignants.get(cmbEnseignant.getSelectedIndex()).getId();
        Cours c = cours != null ? cours : new Cours();
        c.setNom(nom);
        c.setDescription(txtDesc.getText().trim());
        c.setEnseignantId(ensId);
        c.setCouleur(txtCouleur.getText().trim());
        boolean ok = cours == null ? coursDAO.ajouter(c) : coursDAO.modifier(c);
        if (ok) { confirme = true; dispose(); }
        else UIUtils.messageErreur(this, "Erreur lors de l'enregistrement.");
    }

    public boolean isConfirme() { return confirme; }
}
