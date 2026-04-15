package com.univscheduler.view.panels;

import com.univscheduler.dao.DatabaseManager;
import com.univscheduler.model.RoleUtilisateur;
import com.univscheduler.model.Utilisateur;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Tableau de bord reserve aux administrateurs et gestionnaires.
 * Affiche un diagramme du taux d'occupation des salles.
 */
public class DashboardPanel extends JPanel {

    private static final double HEURES_OUVERTURE_PAR_JOUR = 10.0;
    private static final double JOURS_OUVERTURE_PAR_SEMAINE = 5.0;
    private static final double CAPACITE_HEBDO_PAR_SALLE =
            HEURES_OUVERTURE_PAR_JOUR * JOURS_OUVERTURE_PAR_SEMAINE;

    private final Utilisateur utilisateur;
    private JPanel statsPanel;
    private OccupationChartPanel chartPanel;
    private JLabel lblBienvenue;
    private JLabel lblSynthese;

    public DashboardPanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        setBackground(UIUtils.COULEUR_FOND);
        setLayout(new BorderLayout(0, 15));
        initComponents();
        rafraichir();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(0, 6));
        topPanel.setBackground(UIUtils.COULEUR_FOND);

        lblBienvenue = new JLabel("Dashboard occupation des salles");
        lblBienvenue.setFont(UIUtils.FONT_TITLE);
        lblBienvenue.setForeground(UIUtils.COULEUR_SECONDAIRE);

        lblSynthese = new JLabel();
        lblSynthese.setFont(UIUtils.FONT_LABEL);
        lblSynthese.setForeground(UIUtils.COULEUR_TEXTE_CLAIR);

        topPanel.add(lblBienvenue, BorderLayout.NORTH);
        topPanel.add(lblSynthese, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 110));

        chartPanel = new OccupationChartPanel();
        chartPanel.setBackground(UIUtils.COULEUR_FOND_PANEL);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE),
                new EmptyBorder(16, 16, 16, 16)
        ));

        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(chartPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void rafraichir() {
        if (!peutVoirDashboard()) {
            afficherAccesRefuse();
            return;
        }

        statsPanel.removeAll();
        try {
            DashboardMetrics metrics = chargerMetrics();

            lblBienvenue.setText("Bonjour, " + utilisateur.getNomComplet());
            lblSynthese.setText(String.format(Locale.US,
                    "Suivi hebdomadaire base sur %.0f h ouvrables par salle. Taux global: %.1f%%",
                    CAPACITE_HEBDO_PAR_SALLE,
                    metrics.tauxGlobalOccupation));

            statsPanel.add(creerCarteStats("Occupation moyenne",
                    String.format(Locale.US, "%.1f%%", metrics.tauxGlobalOccupation),
                    UIUtils.COULEUR_PRIMAIRE));
            statsPanel.add(creerCarteStats("Salle la plus occupee",
                    metrics.salleLaPlusOccupee,
                    new Color(46, 204, 113)));
            statsPanel.add(creerCarteStats("Heures planifiees",
                    String.format(Locale.US, "%.1f h", metrics.totalHeuresOccupees),
                    new Color(230, 126, 34)));

            chartPanel.setData(metrics.occupations);
        } catch (SQLException e) {
            lblSynthese.setText("Impossible de charger les statistiques d'occupation.");
            chartPanel.setData(List.of());
            UIUtils.messageErreur(this, "Erreur lors du chargement du dashboard.");
        }

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private boolean peutVoirDashboard() {
        return utilisateur.getRole() == RoleUtilisateur.ADMIN
                || utilisateur.getRole() == RoleUtilisateur.GESTIONNAIRE;
    }

    private void afficherAccesRefuse() {
        removeAll();

        JPanel deniedPanel = new JPanel(new GridBagLayout());
        deniedPanel.setBackground(UIUtils.COULEUR_FOND);

        JLabel label = new JLabel("Acces reserve a l'administration et au gestionnaire.");
        label.setFont(UIUtils.FONT_HEADING);
        label.setForeground(UIUtils.COULEUR_DANGER);
        deniedPanel.add(label);

        add(deniedPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private DashboardMetrics chargerMetrics() throws SQLException {
        List<RoomOccupation> occupations = new ArrayList<>();
        Connection conn = DatabaseManager.getInstance().getConnection();

        String sql = """
            SELECT s.numero,
                   COALESCE(SUM(
                       (CAST(substr(c.heure_fin, 1, 2) AS INTEGER) * 60 + CAST(substr(c.heure_fin, 4, 2) AS INTEGER))
                     - (CAST(substr(c.heure_debut, 1, 2) AS INTEGER) * 60 + CAST(substr(c.heure_debut, 4, 2) AS INTEGER))
                   ), 0) / 60.0 AS heures_occupees
            FROM salles s
            LEFT JOIN creneaux c ON c.salle_id = s.id
            GROUP BY s.id, s.numero
            ORDER BY heures_occupees DESC, s.numero ASC
        """;

        double totalHeures = 0.0;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String numero = rs.getString("numero");
                double heuresOccupees = rs.getDouble("heures_occupees");
                double taux = CAPACITE_HEBDO_PAR_SALLE == 0.0
                        ? 0.0
                        : (heuresOccupees / CAPACITE_HEBDO_PAR_SALLE) * 100.0;
                occupations.add(new RoomOccupation(numero, heuresOccupees, taux));
                totalHeures += heuresOccupees;
            }
        }

        occupations.sort(Comparator.comparingDouble(RoomOccupation::tauxOccupation).reversed()
                .thenComparing(RoomOccupation::numero));

        double capaciteTotale = occupations.size() * CAPACITE_HEBDO_PAR_SALLE;
        double tauxGlobal = capaciteTotale == 0.0 ? 0.0 : (totalHeures / capaciteTotale) * 100.0;
        String salleLaPlusOccupee = occupations.isEmpty()
                ? "-"
                : String.format(Locale.US, "%s (%.1f%%)", occupations.get(0).numero(), occupations.get(0).tauxOccupation());

        return new DashboardMetrics(occupations, totalHeures, tauxGlobal, salleLaPlusOccupee);
    }

    private JPanel creerCarteStats(String libelle, String valeur, Color couleur) {
        JPanel carte = new JPanel(new BorderLayout(0, 8));
        carte.setBackground(UIUtils.COULEUR_FOND_PANEL);
        carte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COULEUR_BORDURE),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel accent = new JPanel();
        accent.setBackground(couleur);
        accent.setPreferredSize(new Dimension(10, 10));

        JLabel lblLibelle = new JLabel(libelle);
        lblLibelle.setFont(UIUtils.FONT_SMALL);
        lblLibelle.setForeground(UIUtils.COULEUR_TEXTE_CLAIR);

        JLabel lblValeur = new JLabel(valeur);
        lblValeur.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblValeur.setForeground(UIUtils.COULEUR_TEXTE);

        carte.add(accent, BorderLayout.NORTH);
        carte.add(lblLibelle, BorderLayout.CENTER);
        carte.add(lblValeur, BorderLayout.SOUTH);
        return carte;
    }

    private record RoomOccupation(String numero, double heuresOccupees, double tauxOccupation) {}

    private record DashboardMetrics(List<RoomOccupation> occupations,
                                    double totalHeuresOccupees,
                                    double tauxGlobalOccupation,
                                    String salleLaPlusOccupee) {}

    private static class OccupationChartPanel extends JPanel {

        private List<RoomOccupation> data = List.of();

        public void setData(List<RoomOccupation> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int left = 70;
            int right = 30;
            int top = 40;
            int bottom = 55;

            g2.setColor(UIUtils.COULEUR_TEXTE);
            g2.setFont(UIUtils.FONT_HEADING);
            g2.drawString("Diagramme du taux d'occupation des salles", left, 24);

            if (data == null || data.isEmpty()) {
                g2.setFont(UIUtils.FONT_LABEL);
                g2.setColor(UIUtils.COULEUR_TEXTE_CLAIR);
                g2.drawString("Aucune salle ou aucune donnee de planification.", left, height / 2);
                g2.dispose();
                return;
            }

            int chartWidth = Math.max(1, width - left - right);
            int chartHeight = Math.max(1, height - top - bottom);
            int baseY = top + chartHeight;

            g2.setFont(UIUtils.FONT_SMALL);
            for (int pct = 0; pct <= 100; pct += 25) {
                int y = baseY - (int) Math.round((pct / 100.0) * chartHeight);
                g2.setColor(new Color(220, 227, 235));
                g2.drawLine(left, y, left + chartWidth, y);
                g2.setColor(UIUtils.COULEUR_TEXTE_CLAIR);
                g2.drawString(pct + "%", 30, y + 4);
            }

            g2.setColor(UIUtils.COULEUR_SECONDAIRE);
            g2.drawLine(left, top, left, baseY);
            g2.drawLine(left, baseY, left + chartWidth, baseY);

            int count = data.size();
            int slotWidth = Math.max(1, chartWidth / count);
            int barWidth = Math.max(18, Math.min(42, slotWidth - 12));

            for (int i = 0; i < count; i++) {
                RoomOccupation occupation = data.get(i);
                double taux = Math.max(0.0, Math.min(occupation.tauxOccupation(), 100.0));
                int barHeight = (int) Math.round((taux / 100.0) * chartHeight);
                int x = left + (i * slotWidth) + ((slotWidth - barWidth) / 2);
                int y = baseY - barHeight;

                g2.setColor(couleurPourTaux(taux));
                g2.fillRoundRect(x, y, barWidth, barHeight, 12, 12);

                g2.setColor(UIUtils.COULEUR_TEXTE);
                g2.setFont(UIUtils.FONT_SMALL);
                String value = String.format(Locale.US, "%.0f%%", occupation.tauxOccupation());
                int valueWidth = g2.getFontMetrics().stringWidth(value);
                g2.drawString(value, x + (barWidth - valueWidth) / 2, Math.max(top - 8, y - 6));

                String label = occupation.numero();
                int labelWidth = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, x + (barWidth - labelWidth) / 2, baseY + 18);

                String hours = String.format(Locale.US, "%.1fh", occupation.heuresOccupees());
                int hoursWidth = g2.getFontMetrics().stringWidth(hours);
                g2.setColor(UIUtils.COULEUR_TEXTE_CLAIR);
                g2.drawString(hours, x + (barWidth - hoursWidth) / 2, baseY + 34);
            }

            g2.dispose();
        }

        private Color couleurPourTaux(double taux) {
            if (taux >= 70.0) {
                return UIUtils.COULEUR_DANGER;
            }
            if (taux >= 40.0) {
                return UIUtils.COULEUR_WARNING;
            }
            return UIUtils.COULEUR_ACCENT;
        }
    }
}
