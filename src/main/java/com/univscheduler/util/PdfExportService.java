package com.univscheduler.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.univscheduler.model.Creneau;
import com.univscheduler.model.Creneau.Jour;

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Service pour l'exportation de l'emploi du temps au format PDF (A4 Paysage).
 */
public class PdfExportService {

    private static final String[] HEURES = {
        "08:00","09:00","10:00","11:00","12:00",
        "13:00","14:00","15:00","16:00","17:00"
    };
    private static final Jour[] JOURS = {
        Jour.LUNDI, Jour.MARDI, Jour.MERCREDI, Jour.JEUDI, Jour.VENDREDI
    };

    /**
     * Génère un fichier PDF pour un groupe donné.
     */
    public static void exporterEmploiDuTemps(String cheminFichier, String titre, List<Creneau> creneaux) throws Exception {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
        document.open();

        // Polices
        Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, UIUtils.COULEUR_PRIMAIRE);
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA, 10, UIUtils.COULEUR_TEXTE);
        Font fontBold  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Font fontCren  = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.WHITE);

        // Titre
        Paragraph p = new Paragraph("UNIV-SCHEDULER — " + titre, fontTitre);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(20);
        document.add(p);

        // Table (Heures + 5 jours = 6 colonnes)
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setWidths(new float[]{1.5f, 2f, 2f, 2f, 2f, 2f});

        // En-têtes des jours
        table.addCell(creerCelluleHeader("", fontBold));
        for (Jour j : JOURS) {
            table.addCell(creerCelluleHeader(j.getLibelle(), fontBold));
        }

        // Lignes horaires
        for (int h = 0; h < HEURES.length - 1; h++) {
            String hDebut = HEURES[h];
            String hFin   = HEURES[h+1];

            // Colonne Heure
            table.addCell(creerCelluleHeure(hDebut + " - " + hFin, fontLabel));

            for (Jour j : JOURS) {
                Creneau cr = trouverCreneau(creneaux, j, hDebut, hFin);
                if (cr != null) {
                    table.addCell(creerCelluleCreneau(cr, fontCren));
                } else {
                    table.addCell("");
                }
            }
        }

        document.add(table);
        document.close();
    }

    private static PdfPCell creerCelluleHeader(String texte, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, font));
        cell.setBackgroundColor(UIUtils.COULEUR_SECONDAIRE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        return cell;
    }

    private static PdfPCell creerCelluleHeure(String texte, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, font));
        cell.setBackgroundColor(new Color(240, 244, 250));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private static PdfPCell creerCelluleCreneau(Creneau cr, Font font) {
        Color couleur;
        try { couleur = Color.decode(cr.getCouleurCours() != null ? cr.getCouleurCours() : "#4A90D9"); }
        catch (Exception e) { couleur = UIUtils.COULEUR_PRIMAIRE; }

        String texte = cr.getNomCours() + "\n" + cr.getNomSalle() + "\n" + cr.getNomEnseignant();
        PdfPCell cell = new PdfPCell(new Phrase(texte, font));
        cell.setBackgroundColor(couleur);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private static Creneau trouverCreneau(List<Creneau> creneaux, Jour jour, String debut, String fin) {
        for (Creneau cr : creneaux) {
            if (cr.getJour() == jour) {
                int d = hMin(cr.getHeureDebut());
                int f = hMin(cr.getHeureFin());
                int d2 = hMin(debut);
                int f2 = hMin(fin);
                if (d < f2 && d2 < f) return cr;
            }
        }
        return null;
    }

    private static int hMin(String h) {
        String[] p = h.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }
}
