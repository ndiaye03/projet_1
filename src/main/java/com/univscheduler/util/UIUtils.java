package com.univscheduler.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Classe utilitaire centralisant les constantes de style et les helpers UI.
 */
public class UIUtils {

    // ─── Palette de couleurs ────────────────────────────────────────────────
    public static final Color COULEUR_PRIMAIRE   = new Color(41, 128, 185);
    public static final Color COULEUR_SECONDAIRE = new Color(52, 73, 94);
    public static final Color COULEUR_ACCENT     = new Color(46, 204, 113);
    public static final Color COULEUR_DANGER     = new Color(231, 76, 60);
    public static final Color COULEUR_WARNING    = new Color(241, 196, 15);
    public static final Color COULEUR_FOND       = new Color(245, 248, 252);
    public static final Color COULEUR_FOND_PANEL = new Color(255, 255, 255);
    public static final Color COULEUR_TEXTE      = new Color(44, 62, 80);
    public static final Color COULEUR_TEXTE_CLAIR= new Color(127, 140, 141);
    public static final Color COULEUR_BORDURE    = new Color(218, 224, 232);
    public static final Color COULEUR_HEADER_TABLE = new Color(52, 73, 94);
    public static final Color COULEUR_ROW_ALT    = new Color(235, 242, 250);

    // ─── Polices ─────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_TABLE   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BTN     = new Font("Segoe UI", Font.BOLD, 12);

    /**
     * Configure le Look and Feel Nimbus.
     */
    public static void setLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    // Personnalisation Nimbus
                    UIManager.put("nimbusBase", COULEUR_PRIMAIRE);
                    UIManager.put("nimbusBlueGrey", COULEUR_SECONDAIRE);
                    UIManager.put("control", COULEUR_FOND);
                    UIManager.put("Table.alternateRowColor", COULEUR_ROW_ALT);
                    break;
                }
            }
        } catch (Exception e) {
            // Fallback : Look and Feel système
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }
    }

    /**
     * Crée un bouton stylisé avec couleur de fond.
     */
    public static JButton creerBouton(String texte, Color couleur) {
        JButton btn = new JButton(texte) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(couleur.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(couleur.brighter());
                } else {
                    g2.setColor(couleur);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(130, 34));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Crée un label titre.
     */
    public static JLabel creerTitre(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(FONT_TITLE);
        label.setForeground(COULEUR_SECONDAIRE);
        return label;
    }

    /**
     * Crée un label sous-titre.
     */
    public static JLabel creerSousTitre(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(FONT_HEADING);
        label.setForeground(COULEUR_PRIMAIRE);
        return label;
    }

    /**
     * Style une JTable avec l'apparence définie.
     */
    public static void styliserTable(JTable table) {
        table.setFont(FONT_TABLE);
        table.setRowHeight(30);
        table.setGridColor(COULEUR_BORDURE);
        table.setSelectionBackground(COULEUR_PRIMAIRE);
        table.setSelectionForeground(Color.WHITE);
        table.setBackground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(COULEUR_HEADER_TABLE);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);

        // Renderer avec lignes alternées
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : COULEUR_ROW_ALT);
                    setForeground(COULEUR_TEXTE);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    /**
     * Crée un panel avec un titre en bordure stylisée.
     */
    public static JPanel creerPanelTitre(String titre) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COULEUR_FOND_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, COULEUR_PRIMAIRE),
            new EmptyBorder(8, 10, 8, 10)
        ));
        JLabel lblTitre = new JLabel(titre);
        lblTitre.setFont(FONT_HEADING);
        lblTitre.setForeground(COULEUR_SECONDAIRE);
        panel.add(lblTitre, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Crée un JTextField stylisé.
     */
    public static JTextField creerTextField(int colonnes) {
        JTextField field = new JTextField(colonnes);
        field.setFont(FONT_LABEL);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COULEUR_BORDURE, 1),
            new EmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    /**
     * Affiche un message de succès.
     */
    public static void messageSucces(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Succès",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Affiche un message d'erreur.
     */
    public static void messageErreur(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Erreur",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Demande une confirmation.
     */
    public static boolean confirmer(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                == JOptionPane.YES_OPTION;
    }

    /**
     * Centre une fenêtre sur l'écran.
     */
    public static void centrer(Window window) {
        Dimension ecran = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (ecran.width - window.getWidth()) / 2;
        int y = (ecran.height - window.getHeight()) / 2;
        window.setLocation(x, y);
    }
}
