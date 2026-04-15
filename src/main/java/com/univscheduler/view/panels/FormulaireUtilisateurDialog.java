package com.univscheduler.view.panels;

import com.univscheduler.dao.UtilisateurDAO;
import com.univscheduler.model.Admin;
import com.univscheduler.model.Enseignant;
import com.univscheduler.model.Etudiant;
import com.univscheduler.model.Gestionnaire;
import com.univscheduler.model.RoleUtilisateur;
import com.univscheduler.model.Utilisateur;
import com.univscheduler.util.EmailService;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Dialog d'ajout/modification d'un utilisateur.
 */
public class FormulaireUtilisateurDialog extends JDialog {

    private static final String MOT_DE_PASSE_PAR_DEFAUT = "UnivScheduler2026!";

    private boolean confirme = false;
    private final Utilisateur utilisateur;
    private final UtilisateurDAO userDAO = new UtilisateurDAO();

    private JTextField txtNom;
    private JTextField txtPrenom;
    private JTextField txtEmail;
    private JTextField txtLogin;
    private JPasswordField txtMdp;
    private JComboBox<String> cmbRole;
    private JTextField txtDept;
    private JTextField txtSpecialite;
    private JTextField txtGroupe;
    private JTextField txtFiliere;
    private JSpinner spNiveau;

    public FormulaireUtilisateurDialog(JFrame parent, Utilisateur utilisateur) {
        super(parent, utilisateur == null ? "Nouvel utilisateur" : "Modifier l'utilisateur", true);
        this.utilisateur = utilisateur;
        initComponents();
        if (utilisateur != null) {
            chargerDonnees();
        }
        pack();
        setResizable(false);
        UIUtils.centrer(this);
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(20, 25, 15, 25));
        main.setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.setBackground(Color.WHITE);

        txtNom = UIUtils.creerTextField(20);
        txtPrenom = UIUtils.creerTextField(20);
        txtEmail = UIUtils.creerTextField(25);
        txtLogin = UIUtils.creerTextField(25);
        txtMdp = new JPasswordField(20);
        txtMdp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE),
                new EmptyBorder(5, 8, 5, 8)
        ));
        cmbRole = new JComboBox<>(new String[]{"ADMIN", "GESTIONNAIRE", "ENSEIGNANT", "ETUDIANT"});
        cmbRole.setFont(UIUtils.FONT_LABEL);

        txtDept = UIUtils.creerTextField(15);
        txtSpecialite = UIUtils.creerTextField(15);
        txtGroupe = UIUtils.creerTextField(15);
        txtFiliere = UIUtils.creerTextField(15);
        spNiveau = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));

        configurerModeCreation();

        form.add(new JLabel("Nom :"));
        form.add(txtNom);
        form.add(new JLabel("Prenom :"));
        form.add(txtPrenom);
        form.add(new JLabel("Email :"));
        form.add(txtEmail);
        form.add(new JLabel("Login :"));
        form.add(txtLogin);
        form.add(new JLabel("Mot de passe" + (utilisateur != null ? " (laisser vide)" : "") + " :"));
        form.add(txtMdp);
        form.add(new JLabel("Role :"));
        form.add(cmbRole);
        form.add(new JLabel("Departement (ens.) :"));
        form.add(txtDept);
        form.add(new JLabel("Specialite (ens.) :"));
        form.add(txtSpecialite);
        form.add(new JLabel("Groupe (etu.) :"));
        form.add(txtGroupe);
        form.add(new JLabel("Filiere (etu.) :"));
        form.add(txtFiliere);
        form.add(new JLabel("Niveau (etu.) :"));
        form.add(spNiveau);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(Color.WHITE);
        JButton btnOk = UIUtils.creerBouton("Enregistrer", UIUtils.COULEUR_ACCENT);
        JButton btnAnnuler = UIUtils.creerBouton("Annuler", UIUtils.COULEUR_TEXTE_CLAIR);
        btnOk.addActionListener(e -> enregistrer());
        btnAnnuler.addActionListener(e -> dispose());
        btns.add(btnOk);
        btns.add(btnAnnuler);

        String titre = utilisateur == null
                ? "Nouvel utilisateur - login par email et mot de passe par defaut"
                : "Modifier utilisateur";
        main.add(UIUtils.creerSousTitre(titre), BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);
        main.add(btns, BorderLayout.SOUTH);
        add(main);
    }

    private void configurerModeCreation() {
        if (utilisateur != null) {
            return;
        }

        txtLogin.setEditable(false);
        txtLogin.setBackground(new Color(245, 245, 245));
        txtMdp.setText(MOT_DE_PASSE_PAR_DEFAUT);
        txtMdp.setEditable(false);
        txtMdp.setBackground(new Color(245, 245, 245));

        txtEmail.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                synchroniserLoginAvecEmail();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                synchroniserLoginAvecEmail();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                synchroniserLoginAvecEmail();
            }
        });
    }

    private void synchroniserLoginAvecEmail() {
        if (utilisateur == null) {
            txtLogin.setText(txtEmail.getText().trim());
        }
    }

    private void chargerDonnees() {
        txtNom.setText(utilisateur.getNom());
        txtPrenom.setText(utilisateur.getPrenom());
        txtEmail.setText(utilisateur.getEmail() != null ? utilisateur.getEmail() : "");
        txtLogin.setText(utilisateur.getLogin());
        cmbRole.setSelectedItem(utilisateur.getRole().name());

        if (utilisateur instanceof Enseignant e) {
            txtDept.setText(e.getDepartement() != null ? e.getDepartement() : "");
            txtSpecialite.setText(e.getSpecialite() != null ? e.getSpecialite() : "");
        } else if (utilisateur instanceof Etudiant et) {
            txtGroupe.setText(et.getGroupe() != null ? et.getGroupe() : "");
            txtFiliere.setText(et.getFiliere() != null ? et.getFiliere() : "");
            spNiveau.setValue(et.getNiveau());
        }
    }

    private void enregistrer() {
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email = txtEmail.getText().trim();
        String login = utilisateur == null ? email : txtLogin.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty()) {
            UIUtils.messageErreur(this, "Nom et prenom sont obligatoires.");
            return;
        }

        if (utilisateur == null && email.isEmpty()) {
            UIUtils.messageErreur(this, "L'email est obligatoire pour creer un utilisateur.");
            return;
        }

        if (login.isEmpty()) {
            UIUtils.messageErreur(this, "Le login est obligatoire.");
            return;
        }

        String mdp = new String(txtMdp.getPassword()).trim();
        if (utilisateur == null) {
            mdp = MOT_DE_PASSE_PAR_DEFAUT;
        }

        RoleUtilisateur role = RoleUtilisateur.valueOf((String) cmbRole.getSelectedItem());
        Utilisateur u = construireUtilisateur(role, nom, prenom, email, login, mdp);

        boolean ok = utilisateur == null ? userDAO.ajouter(u) : userDAO.modifier(u);
        if (!ok && utilisateur != null && !mdp.isEmpty()) {
            userDAO.modifierMotDePasse(utilisateur.getId(), mdp);
        }

        if (ok) {
            if (utilisateur == null && !email.isBlank()) {
                envoyerEmailBienvenue(u, MOT_DE_PASSE_PAR_DEFAUT);
            }
            confirme = true;
            dispose();
            return;
        }

        UIUtils.messageErreur(this, "Erreur lors de l'enregistrement.");
    }

    private Utilisateur construireUtilisateur(RoleUtilisateur role,
                                              String nom,
                                              String prenom,
                                              String email,
                                              String login,
                                              String mdp) {
        return switch (role) {
            case ENSEIGNANT -> new Enseignant(
                    utilisateur != null ? utilisateur.getId() : 0,
                    nom, prenom, email, login,
                    mdp.isEmpty() && utilisateur != null ? utilisateur.getMotDePasse() : mdp,
                    txtDept.getText().trim(), txtSpecialite.getText().trim());
            case ETUDIANT -> new Etudiant(
                    utilisateur != null ? utilisateur.getId() : 0,
                    nom, prenom, email, login,
                    mdp.isEmpty() && utilisateur != null ? utilisateur.getMotDePasse() : mdp,
                    txtGroupe.getText().trim(), txtFiliere.getText().trim(), (int) spNiveau.getValue());
            case ADMIN -> new Admin(
                    utilisateur != null ? utilisateur.getId() : 0,
                    nom, prenom, email, login,
                    mdp.isEmpty() && utilisateur != null ? utilisateur.getMotDePasse() : mdp);
            default -> new Gestionnaire(
                    utilisateur != null ? utilisateur.getId() : 0,
                    nom, prenom, email, login,
                    mdp.isEmpty() && utilisateur != null ? utilisateur.getMotDePasse() : mdp);
        };
    }

    private void envoyerEmailBienvenue(Utilisateur utilisateurCree, String motDePasse) {
        String sujet = "Bienvenue sur UNIV-SCHEDULER";
        String contenu = String.format("""
                Bonjour %s %s,

                Votre compte UNIV-SCHEDULER a été créé avec succés par l'administration.
                
                ======================================================================

                Vos identifiants de connexion sont :
                
                -> Login : %s
                
                -> Mot de passe provisoire : %s
                
                -> Role : %s

                Merci de vous connecter puis de changer votre mot de passe dès que possible.

                
                
                Cordialement,
                
                Administration UNIV-SCHEDULER
                ===================================================================
                """,
                utilisateurCree.getPrenom(),
                utilisateurCree.getNom(),
                utilisateurCree.getLogin(),
                motDePasse,
                utilisateurCree.getRole().getLibelle());

        EmailService.envoyerEmail(utilisateurCree.getEmail(), sujet, contenu);
    }

    public boolean isConfirme() {
        return confirme;
    }
}
