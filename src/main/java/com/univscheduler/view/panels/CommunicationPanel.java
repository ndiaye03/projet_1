package com.univscheduler.view.panels;

import com.univscheduler.dao.UtilisateurDAO;
import com.univscheduler.model.*;
import com.univscheduler.util.EmailService;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Panneau de communication pour l'envoi d'annonces par email.
 */
public class CommunicationPanel extends JPanel {

    private final Utilisateur utilisateur;
    private final UtilisateurDAO userDAO = new UtilisateurDAO();
    
    private JTextField txtSujet;
    private JTextArea txtMessage;
    private JComboBox<String> cmbCible;
    private JButton btnEnvoyer;
    private JProgressBar progress;

    public CommunicationPanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        setBackground(UIUtils.COULEUR_FOND);
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initComponents();
    }

    private void initComponents() {
        // En-tête
        add(UIUtils.creerSousTitre("✉  Communication & Annonces"), BorderLayout.NORTH);

        // Formulaire
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE),
            new EmptyBorder(20, 20, 20, 20)
        ));

        cmbCible = new JComboBox<>(new String[]{
            "Tous les utilisateurs", 
            "Enseignants uniquement", 
            "Étudiants uniquement"
        });
        cmbCible.setFont(UIUtils.FONT_LABEL);
        cmbCible.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        txtSujet = UIUtils.creerTextField(30);
        txtSujet.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        txtMessage = new JTextArea(10, 30);
        txtMessage.setFont(UIUtils.FONT_LABEL);
        txtMessage.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE),
            new EmptyBorder(8, 8, 8, 8)
        ));

        btnEnvoyer = UIUtils.creerBouton("🚀 Diffuser l'annonce", UIUtils.COULEUR_PRIMAIRE);
        btnEnvoyer.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnEnvoyer.addActionListener(e -> diffuserAnnonce());

        progress = new JProgressBar();
        progress.setVisible(false);
        progress.setStringPainted(true);

        form.add(new JLabel("Cible de l'annonce :"));
        form.add(Box.createVerticalStrut(5));
        form.add(cmbCible);
        form.add(Box.createVerticalStrut(15));
        form.add(new JLabel("Sujet :"));
        form.add(Box.createVerticalStrut(5));
        form.add(txtSujet);
        form.add(Box.createVerticalStrut(15));
        form.add(new JLabel("Message :"));
        form.add(Box.createVerticalStrut(5));
        form.add(new JScrollPane(txtMessage));
        form.add(Box.createVerticalStrut(20));
        form.add(btnEnvoyer);
        form.add(Box.createVerticalStrut(10));
        form.add(progress);

        add(form, BorderLayout.CENTER);

        // Aide
        JPanel help = new JPanel(new BorderLayout());
        help.setBackground(new Color(235, 245, 255));
        help.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel lblHelp = new JLabel("<html><b>Note :</b> L'envoi des emails est simulé dans la console si le serveur SMTP n'est pas configuré.</html>");
        lblHelp.setFont(UIUtils.FONT_SMALL);
        help.add(lblHelp);
        add(help, BorderLayout.SOUTH);
    }

    private void diffuserAnnonce() {
        String sujet = txtSujet.getText().trim();
        String message = txtMessage.getText().trim();
        if (sujet.isEmpty() || message.isEmpty()) {
            UIUtils.messageErreur(this, "Veuillez remplir le sujet et le message.");
            return;
        }

        String cible = (String) cmbCible.getSelectedItem();
        List<Utilisateur> destinataires;
        if (cible.contains("Enseignants")) destinataires = userDAO.getParRole(RoleUtilisateur.ENSEIGNANT);
        else if (cible.contains("Étudiants")) destinataires = userDAO.getParRole(RoleUtilisateur.ETUDIANT);
        else destinataires = userDAO.getTous();

        if (destinataires.isEmpty()) {
            UIUtils.messageErreur(this, "Aucun destinataire trouvé.");
            return;
        }

        btnEnvoyer.setEnabled(false);
        progress.setVisible(true);
        progress.setMaximum(destinataires.size());
        progress.setValue(0);

        SwingWorker<Integer, Integer> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                int count = 0;
                for (Utilisateur u : destinataires) {
                    if (u.getEmail() != null && !u.getEmail().isEmpty()) {
                        EmailService.envoyerEmail(u.getEmail(), sujet, "Bonjour " + u.getNomComplet() + ",\n\n" + message);
                    }
                    count++;
                    publish(count);
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {} // Simuler délai
                }
                return count;
            }

            @Override
            protected void process(List<Integer> chunks) {
                progress.setValue(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                btnEnvoyer.setEnabled(true);
                progress.setVisible(false);
                UIUtils.messageSucces(CommunicationPanel.this, "Annonce diffusée à " + destinataires.size() + " utilisateurs.");
                txtSujet.setText("");
                txtMessage.setText("");
            }
        };
        worker.execute();
    }
}
