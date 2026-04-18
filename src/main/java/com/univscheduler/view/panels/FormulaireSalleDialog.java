package com.univscheduler.view.panels;

import com.univscheduler.dao.BatimentDAO;
import com.univscheduler.dao.EquipementDAO;
import com.univscheduler.dao.SalleDAO;
import com.univscheduler.model.Amphi;
import com.univscheduler.model.Batiment;
import com.univscheduler.model.Equipement;
import com.univscheduler.model.Laboratoire;
import com.univscheduler.model.Salle;
import com.univscheduler.model.TypeSalle;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Formulaire d'ajout/modification d'une salle.
 */
public class FormulaireSalleDialog extends JDialog {

    private boolean confirme = false;
    private final Salle salle;
    private final SalleDAO salleDAO = new SalleDAO();
    private final BatimentDAO batimentDAO = new BatimentDAO();
    private final EquipementDAO equipementDAO = new EquipementDAO();

    private JTextField txtNumero;
    private JSpinner spCapacite;
    private JComboBox<String> cmbType;
    private JComboBox<String> cmbBatiment;
    private JCheckBox chkAccessible;
    private JTextField txtEquipements;
    private JSpinner spPostes;
    private JTextField txtOS;
    private JCheckBox chkSono;
    private JCheckBox chkRetrans;
    private List<Batiment> batiments;

    public FormulaireSalleDialog(JFrame parent, Salle salle) {
        super(parent, salle == null ? "Ajouter une salle" : "Modifier la salle", true);
        this.salle = salle;
        initComponents();
        if (salle != null) {
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

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(Color.WHITE);

        batiments = batimentDAO.getTous();
        String[] nomsBatiments = batiments.stream().map(Batiment::getNom).toArray(String[]::new);

        txtNumero = UIUtils.creerTextField(15);
        spCapacite = new JSpinner(new SpinnerNumberModel(30, 1, 1000, 1));
        cmbType = new JComboBox<>();
        for (TypeSalle typeSalle : TypeSalle.values()) {
            cmbType.addItem(typeSalle.getLibelle());
        }
        cmbBatiment = new JComboBox<>(nomsBatiments);
        chkAccessible = new JCheckBox("Oui");
        txtEquipements = UIUtils.creerTextField(25);
        txtEquipements.setToolTipText("Exemple: Projecteur, Tableau blanc, Climatisation");
        spPostes = new JSpinner(new SpinnerNumberModel(0, 0, 500, 1));
        txtOS = UIUtils.creerTextField(15);
        chkSono = new JCheckBox("Sonorisation");
        chkRetrans = new JCheckBox("Retransmission video");

        form.add(new JLabel("Numero :"));
        form.add(txtNumero);
        form.add(new JLabel("Capacite :"));
        form.add(spCapacite);
        form.add(new JLabel("Type :"));
        form.add(cmbType);
        form.add(new JLabel("Batiment :"));
        form.add(cmbBatiment);
        form.add(new JLabel("Accessible PMR :"));
        form.add(chkAccessible);
        form.add(new JLabel("Equipements :"));
        form.add(txtEquipements);
        form.add(new JLabel("Nb postes (labo) :"));
        form.add(spPostes);
        form.add(new JLabel("Systeme OS (labo) :"));
        form.add(txtOS);
        form.add(chkSono);
        form.add(chkRetrans);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setBackground(Color.WHITE);
        JButton btnOk = UIUtils.creerBouton("Enregistrer", UIUtils.COULEUR_ACCENT);
        JButton btnAnnuler = UIUtils.creerBouton("Annuler", UIUtils.COULEUR_TEXTE_CLAIR);
        btnOk.addActionListener(e -> enregistrer());
        btnAnnuler.addActionListener(e -> dispose());
        btns.add(btnOk);
        btns.add(btnAnnuler);

        main.add(UIUtils.creerSousTitre(salle == null ? "Nouvelle salle" : "Modifier salle"), BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);
        main.add(btns, BorderLayout.SOUTH);
        add(main);
    }

    private void chargerDonnees() {
        txtNumero.setText(salle.getNumero());
        spCapacite.setValue(salle.getCapacite());
        cmbType.setSelectedItem(salle.getType().getLibelle());
        chkAccessible.setSelected(salle.isAccessible());

        for (int i = 0; i < batiments.size(); i++) {
            if (batiments.get(i).getId() == salle.getBatimentId()) {
                cmbBatiment.setSelectedIndex(i);
                break;
            }
        }

        if (salle instanceof Laboratoire laboratoire) {
            spPostes.setValue(laboratoire.getNombrePostes());
            txtOS.setText(laboratoire.getSystemeExploitation() != null
                    ? laboratoire.getSystemeExploitation() : "");
        }

        if (salle instanceof Amphi amphi) {
            chkSono.setSelected(amphi.isSonorisation());
            chkRetrans.setSelected(amphi.isRetransmission());
        }

        txtEquipements.setText(
                equipementDAO.getParSalle(salle.getId()).stream()
                        .map(Equipement::getNom)
                        .collect(Collectors.joining(", "))
        );
    }

    private void enregistrer() {
        String numero = txtNumero.getText().trim();
        if (numero.isEmpty()) {
            UIUtils.messageErreur(this, "Numero requis.");
            return;
        }

        int capacite = (int) spCapacite.getValue();
        int indexBatiment = cmbBatiment.getSelectedIndex();
        int batimentId = (indexBatiment >= 0 && indexBatiment < batiments.size())
                ? batiments.get(indexBatiment).getId() : 0;

        TypeSalle type = Arrays.stream(TypeSalle.values())
                .filter(typeSalle -> typeSalle.getLibelle().equals(cmbType.getSelectedItem()))
                .findFirst()
                .orElse(TypeSalle.COURS);

        Salle salleCible;
        if (type == TypeSalle.AMPHI) {
            salleCible = new Amphi(
                    salle != null ? salle.getId() : 0,
                    numero,
                    capacite,
                    batimentId,
                    chkAccessible.isSelected(),
                    chkSono.isSelected(),
                    chkRetrans.isSelected()
            );
        } else if (type == TypeSalle.LABORATOIRE) {
            salleCible = new Laboratoire(
                    salle != null ? salle.getId() : 0,
                    numero,
                    capacite,
                    batimentId,
                    chkAccessible.isSelected(),
                    (int) spPostes.getValue(),
                    txtOS.getText().trim()
            );
        } else {
            salleCible = new Salle(
                    salle != null ? salle.getId() : 0,
                    numero,
                    capacite,
                    type,
                    batimentId,
                    chkAccessible.isSelected()
            );
        }

        boolean ok = salle == null ? salleDAO.ajouter(salleCible) : salleDAO.modifier(salleCible);
        if (!ok) {
            UIUtils.messageErreur(this, "Erreur lors de l'enregistrement.");
            return;
        }

        List<String> equipements = Arrays.stream(txtEquipements.getText().split(","))
                .map(String::trim)
                .filter(nom -> !nom.isEmpty())
                .collect(Collectors.toList());

        if (!equipementDAO.remplacerPourSalle(salleCible.getId(), equipements)) {
            UIUtils.messageErreur(this, "Salle enregistree, mais impossible de sauvegarder les equipements.");
            return;
        }

        confirme = true;
        dispose();
    }

    public boolean isConfirme() {
        return confirme;
    }
}
