package com.univscheduler.view;

import com.univscheduler.dao.UtilisateurDAO;
import com.univscheduler.model.Utilisateur;
import com.univscheduler.util.UIUtils;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;

/**
 * Fenetre de connexion.
 */
public class LoginView extends JFrame {

    private static final Dimension WINDOW_SIZE = new Dimension(600, 700);
    private static final Dimension CARD_SIZE = new Dimension(390, 405);

    private JTextField txtLogin;
    private JPasswordField txtMotDePasse;
    private JButton btnConnexion;
    private JLabel lblMessage;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final Image backgroundImage = chargerImageFond();

    public LoginView() {
        setTitle("UNIV-SCHEDULER - Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setResizable(true);
        initComponents();
        pack();
        setSize(WINDOW_SIZE);
        setMinimumSize(WINDOW_SIZE);
        UIUtils.centrer(this);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new java.awt.GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                if (backgroundImage != null) {
                    int imageWidth = backgroundImage.getWidth(this);
                    int imageHeight = backgroundImage.getHeight(this);
                    double scale = Math.max(getWidth() / (double) imageWidth, getHeight() / (double) imageHeight);
                    int drawWidth = (int) Math.round(imageWidth * scale);
                    int drawHeight = (int) Math.round(imageHeight * scale);
                    int x = (getWidth() - drawWidth) / 2;
                    int y = (getHeight() - drawHeight) / 2;
                    g2.drawImage(backgroundImage, x, y, drawWidth, drawHeight, this);
                } else {
                    g2.setPaint(new java.awt.GradientPaint(
                        0, 0, new Color(21, 57, 91),
                        getWidth(), getHeight(), new Color(6, 23, 40)
                    ));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                g2.setColor(new Color(6, 18, 30, 130));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        mainPanel.setPreferredSize(WINDOW_SIZE);
        setContentPane(mainPanel);

        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 228));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.setColor(new Color(255, 255, 255, 120));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new EmptyBorder(28, 30, 26, 30));
        cardPanel.setPreferredSize(CARD_SIZE);
        cardPanel.setMinimumSize(CARD_SIZE);
        cardPanel.setMaximumSize(CARD_SIZE);

        JLabel lblAppName = new JLabel("UNIV-SCHEDULER");
        lblAppName.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28));
        lblAppName.setForeground(new Color(17, 49, 79));
        lblAppName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Connexion a votre espace");
        lblSubtitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(74, 95, 114));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblLoginLabel = new JLabel("Identifiant");
        lblLoginLabel.setFont(UIUtils.FONT_BOLD);
        lblLoginLabel.setForeground(UIUtils.COULEUR_TEXTE);
        lblLoginLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtLogin = UIUtils.creerTextField(20);
        txtLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblMdpLabel = new JLabel("Mot de passe");
        lblMdpLabel.setFont(UIUtils.FONT_BOLD);
        lblMdpLabel.setForeground(UIUtils.COULEUR_TEXTE);
        lblMdpLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtMotDePasse = new JPasswordField(20);
        txtMotDePasse.setFont(UIUtils.FONT_LABEL);
        txtMotDePasse.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtMotDePasse.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtMotDePasse.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(193, 205, 214), 1),
            new EmptyBorder(7, 10, 7, 10)
        ));

        btnConnexion = UIUtils.creerBouton("Se connecter", UIUtils.COULEUR_PRIMAIRE);
        btnConnexion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnConnexion.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblMessage = new JLabel(" ");
        lblMessage.setFont(UIUtils.FONT_SMALL);
        lblMessage.setForeground(UIUtils.COULEUR_DANGER);
        lblMessage.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHint = new JLabel(
            "<html>Comptes : papalyndiaye0@gmail.com/admin123<br/>" +
            "gestionnaire/gest123 | sbernard/ens123 | lsimon/etu123</html>"
        );
        lblHint.setFont(UIUtils.FONT_SMALL);
        lblHint.setForeground(new Color(87, 101, 115));
        lblHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardPanel.add(lblAppName);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(lblSubtitle);
        cardPanel.add(Box.createVerticalStrut(28));
        cardPanel.add(lblLoginLabel);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(txtLogin);
        cardPanel.add(Box.createVerticalStrut(16));
        cardPanel.add(lblMdpLabel);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(txtMotDePasse);
        cardPanel.add(Box.createVerticalStrut(24));
        cardPanel.add(btnConnexion);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(lblMessage);
        cardPanel.add(Box.createVerticalGlue());
        cardPanel.add(new javax.swing.JSeparator());
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(lblHint);

        mainPanel.add(cardPanel, new java.awt.GridBagConstraints());

        btnConnexion.addActionListener(e -> seConnecter());
        txtMotDePasse.addActionListener(e -> seConnecter());
        txtLogin.addActionListener(e -> txtMotDePasse.requestFocus());
    }

    private Image chargerImageFond() {
        try (InputStream inputStream = LoginView.class.getClassLoader().getResourceAsStream("images/logo.jpg")) {
            if (inputStream != null) {
                return ImageIO.read(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void seConnecter() {
        String login = txtLogin.getText().trim();
        String mdp = new String(txtMotDePasse.getPassword());

        if (login.isEmpty() || mdp.isEmpty()) {
            lblMessage.setText("Veuillez remplir tous les champs.");
            return;
        }

        lblMessage.setText("Connexion en cours...");
        btnConnexion.setEnabled(false);

        SwingWorker<Utilisateur, Void> worker = new SwingWorker<>() {
            @Override
            protected Utilisateur doInBackground() {
                return utilisateurDAO.authentifier(login, mdp);
            }

            @Override
            protected void done() {
                btnConnexion.setEnabled(true);
                try {
                    Utilisateur utilisateur = get();
                    if (utilisateur != null) {
                        lblMessage.setText(" ");
                        ouvrirTableauDeBord(utilisateur);
                    } else {
                        lblMessage.setText("Identifiant ou mot de passe incorrect.");
                        txtMotDePasse.setText("");
                        txtLogin.requestFocus();
                    }
                } catch (Exception ex) {
                    lblMessage.setText("Erreur de connexion.");
                }
            }
        };
        worker.execute();
    }

    private void ouvrirTableauDeBord(Utilisateur utilisateur) {
        dispose();
        SwingUtilities.invokeLater(() -> {
            MainView mainView = new MainView(utilisateur);
            mainView.setVisible(true);
        });
    }
}
