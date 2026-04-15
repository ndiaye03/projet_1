package com.univscheduler.view;

import com.univscheduler.dao.UtilisateurDAO;
import com.univscheduler.model.RoleUtilisateur;
import com.univscheduler.model.Utilisateur;
import com.univscheduler.util.UIUtils;
import com.univscheduler.view.panels.BatimentsPanel;
import com.univscheduler.view.panels.CommunicationPanel;
import com.univscheduler.view.panels.CoursPanel;
import com.univscheduler.view.panels.DashboardPanel;
import com.univscheduler.view.panels.EmploiDuTempsPanel;
import com.univscheduler.view.panels.ReservationsPanel;
import com.univscheduler.view.panels.SallesPanel;
import com.univscheduler.view.panels.SignalementsPanel;
import com.univscheduler.view.panels.UtilisateursPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Fenetre principale avec navigation laterale.
 * Gere l'affichage des panneaux selon le role.
 */
public class MainView extends JFrame {

    private final Utilisateur utilisateur;
    private final boolean accesDashboard;
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private JPanel contentPanel;
    private CardLayout cardLayout;

    private DashboardPanel dashboardPanel;
    private SallesPanel sallesPanel;
    private BatimentsPanel batimentsPanel;
    private CoursPanel coursPanel;
    private EmploiDuTempsPanel emploiPanel;
    private UtilisateursPanel utilisateursPanel;
    private ReservationsPanel reservationsPanel;
    private CommunicationPanel communicationPanel;
    private SignalementsPanel signalementsPanel;

    public MainView(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        this.accesDashboard = utilisateur.getRole() == RoleUtilisateur.ADMIN
                || utilisateur.getRole() == RoleUtilisateur.GESTIONNAIRE;
        setTitle("UNIV-SCHEDULER - " + utilisateur.getNomComplet() + " [" + utilisateur.getRole() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        UIUtils.centrer(this);
        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.COULEUR_FOND);
        root.add(creerHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.add(creerSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIUtils.COULEUR_FOND);
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        dashboardPanel = new DashboardPanel(utilisateur);
        sallesPanel = new SallesPanel(utilisateur);
        batimentsPanel = new BatimentsPanel(utilisateur);
        coursPanel = new CoursPanel(utilisateur);
        emploiPanel = new EmploiDuTempsPanel(utilisateur);
        reservationsPanel = new ReservationsPanel(utilisateur);
        if (utilisateur.getRole() == RoleUtilisateur.ADMIN || utilisateur.getRole() == RoleUtilisateur.ENSEIGNANT) {
            signalementsPanel = new SignalementsPanel(utilisateur);
        }

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(emploiPanel, "emploi");
        contentPanel.add(sallesPanel, "salles");
        contentPanel.add(batimentsPanel, "batiments");
        contentPanel.add(coursPanel, "cours");
        contentPanel.add(reservationsPanel, "reservations");
        if (signalementsPanel != null) {
            contentPanel.add(signalementsPanel, "signalements");
        }

        if (utilisateur.getRole() == RoleUtilisateur.ADMIN) {
            utilisateursPanel = new UtilisateursPanel(utilisateur);
            communicationPanel = new CommunicationPanel(utilisateur);
            contentPanel.add(utilisateursPanel, "utilisateurs");
            contentPanel.add(communicationPanel, "communication");
        }

        body.add(contentPanel, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
        add(root);

        cardLayout.show(contentPanel, accesDashboard ? "dashboard" : "emploi");
    }

    private JPanel creerHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UIUtils.COULEUR_SECONDAIRE,
                        getWidth(), 0, new Color(41, 128, 185));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel lblApp = new JLabel("UNIV-SCHEDULER");
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblApp.setForeground(Color.WHITE);

        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userInfo.setOpaque(false);

        JLabel lblUser = new JLabel(utilisateur.getNomComplet());
        lblUser.setFont(UIUtils.FONT_LABEL);
        lblUser.setForeground(new Color(200, 225, 255));

        JLabel lblRole = new JLabel(utilisateur.getRole().getLibelle());
        lblRole.setFont(UIUtils.FONT_SMALL);
        lblRole.setForeground(UIUtils.COULEUR_ACCENT);

        JButton btnMotDePasse = new JButton("Changer mot de passe");
        btnMotDePasse.setFont(UIUtils.FONT_SMALL);
        btnMotDePasse.setForeground(Color.WHITE);
        btnMotDePasse.setBackground(UIUtils.COULEUR_PRIMAIRE);
        btnMotDePasse.setBorder(new EmptyBorder(5, 12, 5, 12));
        btnMotDePasse.setFocusPainted(false);
        btnMotDePasse.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMotDePasse.addActionListener(e -> ouvrirDialogueMotDePasse());

        JButton btnDeconnexion = new JButton("Deconnexion");
        btnDeconnexion.setFont(UIUtils.FONT_SMALL);
        btnDeconnexion.setForeground(Color.WHITE);
        btnDeconnexion.setBackground(new Color(231, 76, 60));
        btnDeconnexion.setBorder(new EmptyBorder(5, 12, 5, 12));
        btnDeconnexion.setFocusPainted(false);
        btnDeconnexion.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDeconnexion.addActionListener(e -> seDeconnecter());

        userInfo.add(lblRole);
        userInfo.add(lblUser);
        userInfo.add(Box.createHorizontalStrut(15));
        userInfo.add(btnMotDePasse);
        userInfo.add(btnDeconnexion);

        header.add(lblApp, BorderLayout.WEST);
        header.add(userInfo, BorderLayout.EAST);
        return header;
    }

    private JPanel creerSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIUtils.COULEUR_SECONDAIRE);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(10, 0, 10, 0));

        if (accesDashboard) {
            ajouterBoutonNav(sidebar, "Tableau de bord", "dashboard");
        }
        if (utilisateur.getRole() != RoleUtilisateur.ADMIN) {
            ajouterBoutonNav(sidebar, "Emploi du temps", "emploi");
        }
        ajouterSeparateur(sidebar);

        if (utilisateur.getRole() != RoleUtilisateur.ETUDIANT) {
            ajouterBoutonNav(sidebar, "Cours", "cours");
        }
        if (utilisateur.getRole() == RoleUtilisateur.ADMIN || utilisateur.getRole() == RoleUtilisateur.ENSEIGNANT) {
            ajouterBoutonNav(sidebar,
                    utilisateur.getRole() == RoleUtilisateur.ADMIN ? "Signalements" : "Signaler un probleme",
                    "signalements");
        }

        if (utilisateur.getRole() == RoleUtilisateur.ADMIN
                || utilisateur.getRole() == RoleUtilisateur.GESTIONNAIRE) {
            ajouterBoutonNav(sidebar, "Batiments", "batiments");
            ajouterBoutonNav(sidebar, "Salles", "salles");
        } else {
            ajouterBoutonNav(sidebar, "Salles disponibles", "salles");
        }

        if (utilisateur.getRole() != RoleUtilisateur.ETUDIANT
                && utilisateur.getRole() != RoleUtilisateur.ADMIN) {
            ajouterBoutonNav(sidebar, "Reservations", "reservations");
        }

        if (utilisateur.getRole() == RoleUtilisateur.ADMIN) {
            ajouterSeparateur(sidebar);
            ajouterBoutonNav(sidebar, "Utilisateurs", "utilisateurs");
            ajouterBoutonNav(sidebar, "Communication", "communication");
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(creerDroitsPanel());
        return sidebar;
    }

    private JPanel creerDroitsPanel() {
        JPanel droitsPanel = new JPanel();
        droitsPanel.setLayout(new BoxLayout(droitsPanel, BoxLayout.Y_AXIS));
        droitsPanel.setBackground(new Color(44, 62, 80));
        droitsPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        droitsPanel.setMaximumSize(new Dimension(220, 500));

        JLabel lblDroits = new JLabel("Mes droits");
        lblDroits.setFont(UIUtils.FONT_SMALL);
        lblDroits.setForeground(new Color(150, 170, 190));
        droitsPanel.add(lblDroits);

        for (String droit : utilisateur.getDroits()) {
            JLabel lbl = new JLabel("- " + droit);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lbl.setForeground(new Color(120, 150, 170));
            droitsPanel.add(lbl);
        }

        return droitsPanel;
    }

    private void ajouterBoutonNav(JPanel sidebar, String texte, String card) {
        JButton btn = new JButton(texte) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover() || getModel().isPressed()) {
                    g2.setColor(new Color(52, 152, 219, 80));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(189, 215, 240));
        btn.setBackground(UIUtils.COULEUR_SECONDAIRE);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 10));
        btn.setMaximumSize(new Dimension(220, 45));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, card);
            rafraichirPanel(card);
        });
        sidebar.add(btn);
    }

    private void ajouterSeparateur(JPanel sidebar) {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(70, 90, 110));
        sep.setMaximumSize(new Dimension(220, 1));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(5));
    }

    private void rafraichirPanel(String card) {
        switch (card) {
            case "emploi" -> emploiPanel.rafraichir();
            case "salles" -> sallesPanel.rafraichir();
            case "batiments" -> batimentsPanel.rafraichir();
            case "cours" -> coursPanel.rafraichir();
            case "reservations" -> reservationsPanel.rafraichir();
            case "utilisateurs" -> {
                if (utilisateursPanel != null) {
                    utilisateursPanel.rafraichir();
                }
            }
            case "signalements" -> {
                if (signalementsPanel != null) {
                    signalementsPanel.rafraichir();
                }
            }
            case "dashboard" -> {
                if (accesDashboard) {
                    dashboardPanel.rafraichir();
                }
            }
            default -> {
            }
        }
    }

    private void seDeconnecter() {
        if (UIUtils.confirmer(this, "Voulez-vous vous deconnecter ?")) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginView loginView = new LoginView();
                loginView.setVisible(true);
            });
        }
    }

    private void ouvrirDialogueMotDePasse() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        JPasswordField ancienMdp = new JPasswordField(18);
        JPasswordField nouveauMdp = new JPasswordField(18);
        JPasswordField confirmationMdp = new JPasswordField(18);

        panel.add(new JLabel("Mot de passe actuel"));
        panel.add(ancienMdp);
        panel.add(new JLabel("Nouveau mot de passe"));
        panel.add(nouveauMdp);
        panel.add(new JLabel("Confirmer le nouveau mot de passe"));
        panel.add(confirmationMdp);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Changer mon mot de passe",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String ancien = new String(ancienMdp.getPassword()).trim();
        String nouveau = new String(nouveauMdp.getPassword()).trim();
        String confirmation = new String(confirmationMdp.getPassword()).trim();

        if (ancien.isEmpty() || nouveau.isEmpty() || confirmation.isEmpty()) {
            UIUtils.messageErreur(this, "Tous les champs sont obligatoires.");
            return;
        }

        if (!ancien.equals(utilisateur.getMotDePasse())) {
            UIUtils.messageErreur(this, "Le mot de passe actuel est incorrect.");
            return;
        }

        if (nouveau.length() < 4) {
            UIUtils.messageErreur(this, "Le nouveau mot de passe doit contenir au moins 4 caracteres.");
            return;
        }

        if (!nouveau.equals(confirmation)) {
            UIUtils.messageErreur(this, "La confirmation du mot de passe ne correspond pas.");
            return;
        }

        if (ancien.equals(nouveau)) {
            UIUtils.messageErreur(this, "Le nouveau mot de passe doit etre different de l'ancien.");
            return;
        }

        if (utilisateurDAO.modifierMotDePasse(utilisateur.getId(), nouveau)) {
            utilisateur.setMotDePasse(nouveau);
            UIUtils.messageSucces(this, "Mot de passe modifie.");
        } else {
            UIUtils.messageErreur(this, "Impossible de modifier le mot de passe.");
        }
    }
}
