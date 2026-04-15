package com.univscheduler.view.panels;

import com.univscheduler.dao.CreneauDAO;
import com.univscheduler.dao.NotificationDAO;
import com.univscheduler.model.Creneau;
import com.univscheduler.model.Etudiant;
import com.univscheduler.model.NotificationUtilisateur;
import com.univscheduler.model.RoleUtilisateur;
import com.univscheduler.model.Utilisateur;
import com.univscheduler.util.PdfExportService;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Panneau d'affichage de l'emploi du temps hebdomadaire.
 */
public class EmploiDuTempsPanel extends JPanel {

    private static final String[] HEURES = {
            "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"
    };
    private static final Creneau.Jour[] JOURS = {
            Creneau.Jour.LUNDI, Creneau.Jour.MARDI, Creneau.Jour.MERCREDI,
            Creneau.Jour.JEUDI, Creneau.Jour.VENDREDI
    };

    private final Utilisateur utilisateur;
    private final CreneauDAO creneauDAO = new CreneauDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private JPanel grillePanel;
    private JComboBox<String> cmbGroupe;

    public EmploiDuTempsPanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        setLayout(new BorderLayout());
        setBackground(UIUtils.COULEUR_FOND);
        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIUtils.COULEUR_FOND);
        topPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        topPanel.add(UIUtils.creerSousTitre("Emploi du temps hebdomadaire"), BorderLayout.WEST);

        JPanel filtresPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filtresPanel.setOpaque(false);

        cmbGroupe = new JComboBox<>(new String[]{
                "Tous les groupes", "L1-INFO-A", "L1-INFO-B",
                "L2-INFO-A", "L2-INFO-B", "M1-GL", "M2-GL"
        });
        if (utilisateur.getRole() == RoleUtilisateur.ETUDIANT) {
            cmbGroupe.setSelectedItem(((Etudiant) utilisateur).getGroupe());
            cmbGroupe.setEnabled(false);
        }

        JButton btnRafraichir = UIUtils.creerBouton("Rafraichir", UIUtils.COULEUR_PRIMAIRE);
        btnRafraichir.addActionListener(e -> rafraichir());

        JButton btnExportPdf = UIUtils.creerBouton("Export PDF", new Color(46, 204, 113));
        btnExportPdf.addActionListener(e -> exporterPdf());

        if (utilisateur.getRole() != RoleUtilisateur.ENSEIGNANT) {
            filtresPanel.add(new JLabel("Groupe :"));
            filtresPanel.add(cmbGroupe);
        }
        filtresPanel.add(btnRafraichir);
        filtresPanel.add(btnExportPdf);
        topPanel.add(filtresPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        grillePanel = new JPanel(new BorderLayout());
        grillePanel.setBackground(UIUtils.COULEUR_FOND);
        JScrollPane scroll = new JScrollPane(grillePanel);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE));
        add(scroll, BorderLayout.CENTER);

        rafraichir();
    }

    private void exporterPdf() {
        String groupe = utilisateur.getRole() == RoleUtilisateur.ENSEIGNANT
                ? "enseignant_" + utilisateur.getLogin()
                : (String) cmbGroupe.getSelectedItem();

        List<Creneau> creneaux = chargerCreneaux();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer l'emploi du temps PDF");
        fileChooser.setSelectedFile(new java.io.File("Emploi_du_temps_" + groupe + ".pdf"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                PdfExportService.exporterEmploiDuTemps(
                        fileChooser.getSelectedFile().getAbsolutePath(),
                        "Emploi du temps - " + groupe,
                        creneaux
                );
                UIUtils.messageSucces(this, "Export PDF reussi.");
            } catch (Exception ex) {
                ex.printStackTrace();
                UIUtils.messageErreur(this, "Erreur lors de l'exportation PDF : " + ex.getMessage());
            }
        }
    }

    public void rafraichir() {
        grillePanel.removeAll();
        List<Creneau> creneaux = chargerCreneaux();

        JPanel contenu = new JPanel(new BorderLayout(0, 12));
        contenu.setOpaque(false);

        JPanel table = new JPanel(new GridLayout(HEURES.length, JOURS.length + 1));
        table.setBackground(Color.WHITE);

        table.add(creerCelluleHeader("Heure"));
        for (Creneau.Jour jour : JOURS) {
            table.add(creerCelluleHeader(jour.getLibelle()));
        }

        for (int h = 0; h < HEURES.length - 1; h++) {
            String hDebut = HEURES[h];
            String hFin = HEURES[h + 1];
            table.add(creerCelluleHeure(hDebut + " - " + hFin));

            for (Creneau.Jour jour : JOURS) {
                Creneau cr = trouverCreneau(creneaux, jour, hDebut);
                table.add(cr != null ? creerCelluleCreneau(cr) : creerCelluleVide());
            }
        }

        contenu.add(table, BorderLayout.CENTER);
        if (utilisateur.getRole() == RoleUtilisateur.ETUDIANT) {
            contenu.add(creerBlocNotifications(), BorderLayout.SOUTH);
        }
        grillePanel.add(contenu, BorderLayout.CENTER);
        grillePanel.revalidate();
        grillePanel.repaint();
    }

    private List<Creneau> chargerCreneaux() {
        String groupe = (String) cmbGroupe.getSelectedItem();
        if (utilisateur.getRole() == RoleUtilisateur.ENSEIGNANT) {
            return creneauDAO.getParEnseignant(utilisateur.getId());
        }
        if (utilisateur.getRole() == RoleUtilisateur.ETUDIANT) {
            return creneauDAO.getParGroupe(((Etudiant) utilisateur).getGroupe());
        }
        if ("Tous les groupes".equals(groupe)) {
            return creneauDAO.getTous();
        }
        return creneauDAO.getParGroupe(groupe != null ? groupe : "");
    }

    private JPanel creerCelluleHeader(String texte) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.COULEUR_SECONDAIRE);
        panel.setBorder(BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE));

        JLabel label = new JLabel(texte, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(UIUtils.FONT_BOLD);
        panel.add(label);
        return panel;
    }

    private JPanel creerCelluleHeure(String texte) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE));

        JLabel label = new JLabel(texte, SwingConstants.CENTER);
        label.setFont(UIUtils.FONT_SMALL);
        panel.add(label);
        return panel;
    }

    private JPanel creerCelluleVide() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE));
        return panel;
    }

    private JPanel creerCelluleCreneau(Creneau cr) {
        Color couleur;
        try {
            couleur = Color.decode(cr.getCouleurCours() != null ? cr.getCouleurCours() : "#4A90D9");
        } catch (Exception e) {
            couleur = UIUtils.COULEUR_PRIMAIRE;
        }

        final Color couleurFinale = couleur;
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(couleurFinale.getRed(), couleurFinale.getGreen(), couleurFinale.getBlue(), 220));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE),
                new EmptyBorder(4, 6, 4, 6)
        ));

        JLabel lNom = new JLabel(cr.getNomCours());
        lNom.setFont(UIUtils.FONT_BOLD);
        lNom.setForeground(Color.WHITE);

        JLabel lSalle = new JLabel("Salle " + cr.getNomSalle());
        lSalle.setFont(UIUtils.FONT_SMALL);
        lSalle.setForeground(Color.WHITE);

        JLabel lEns = new JLabel(cr.getNomEnseignant());
        lEns.setFont(UIUtils.FONT_SMALL);
        lEns.setForeground(Color.WHITE);

        panel.add(lNom);
        panel.add(lSalle);
        panel.add(lEns);
        return panel;
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

    private Creneau trouverCreneau(List<Creneau> creneaux, Creneau.Jour jour, String debut) {
        for (Creneau cr : creneaux) {
            if (cr.getJour() == jour && cr.getHeureDebut().startsWith(debut.substring(0, 2))) {
                return cr;
            }
        }
        return null;
    }
}
