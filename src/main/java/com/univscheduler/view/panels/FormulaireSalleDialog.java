package com.univscheduler.view.panels;

import com.univscheduler.dao.BatimentDAO;
import com.univscheduler.dao.SalleDAO;
import com.univscheduler.model.*;
import com.univscheduler.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Formulaire d'ajout/modification d'une salle (Dialog).
 */
public class FormulaireSalleDialog extends JDialog {

    private boolean confirme = false;
    private final Salle salle;
    private final SalleDAO salleDAO = new SalleDAO();
    private final BatimentDAO batimentDAO = new BatimentDAO();

    private JTextField txtNumero;
    private JSpinner spCapacite;
    private JComboBox<String> cmbType;
    private JComboBox<String> cmbBatiment;
    private JCheckBox chkAccessible;
    // Champs spécifiques
    private JSpinner spPostes;
    private JTextField txtOS;
    private JCheckBox chkSono, chkRetrans;
    private List<com.univscheduler.model.Batiment> batiments;

    public FormulaireSalleDialog(JFrame parent, Salle salle) {
        super(parent, salle == null ? "Ajouter une salle" : "Modifier la salle", true);
        this.salle = salle;
        initComponents();
        if (salle != null) chargerDonnees();
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
        String[] nomsBat = batiments.stream().map(com.univscheduler.model.Batiment::getNom)
                .toArray(String[]::new);

        txtNumero    = UIUtils.creerTextField(15);
        spCapacite   = new JSpinner(new SpinnerNumberModel(30, 1, 1000, 1));
        cmbType      = new JComboBox<>();
        for (TypeSalle t : TypeSalle.values()) cmbType.addItem(t.getLibelle());
        cmbBatiment  = new JComboBox<>(nomsBat);
        chkAccessible = new JCheckBox("Oui");
        spPostes     = new JSpinner(new SpinnerNumberModel(0, 0, 500, 1));
        txtOS        = UIUtils.creerTextField(15);
        chkSono      = new JCheckBox("Sonorisation");
        chkRetrans   = new JCheckBox("Retransmission vidéo");

        form.add(new JLabel("Numéro :")); form.add(txtNumero);
        form.add(new JLabel("Capacité :")); form.add(spCapacite);
        form.add(new JLabel("Type :")); form.add(cmbType);
        form.add(new JLabel("Bâtiment :")); form.add(cmbBatiment);
        form.add(new JLabel("Accessible PMR :")); form.add(chkAccessible);
        form.add(new JLabel("Nb postes (labo) :")); form.add(spPostes);
        form.add(new JLabel("Système OS (labo) :")); form.add(txtOS);
        form.add(chkSono); form.add(chkRetrans);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setBackground(Color.WHITE);
        JButton btnOk = UIUtils.creerBouton("Enregistrer", UIUtils.COULEUR_ACCENT);
        JButton btnAnnuler = UIUtils.creerBouton("Annuler", UIUtils.COULEUR_TEXTE_CLAIR);
        btnOk.addActionListener(e -> enregistrer());
        btnAnnuler.addActionListener(e -> dispose());
        btns.add(btnOk); btns.add(btnAnnuler);

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
                cmbBatiment.setSelectedIndex(i); break;
            }
        }
        if (salle instanceof Laboratoire l) {
            spPostes.setValue(l.getNombrePostes());
            txtOS.setText(l.getSystemeExploitation() != null ? l.getSystemeExploitation() : "");
        }
        if (salle instanceof Amphi a) {
            chkSono.setSelected(a.isSonorisation());
            chkRetrans.setSelected(a.isRetransmission());
        }
    }

    private void enregistrer() {
        String numero = txtNumero.getText().trim();
        if (numero.isEmpty()) { UIUtils.messageErreur(this, "Numéro requis."); return; }
        int cap = (int) spCapacite.getValue();
        int idxBat = cmbBatiment.getSelectedIndex();
        int batId = (idxBat >= 0 && idxBat < batiments.size()) ? batiments.get(idxBat).getId() : 0;
        String typeLibelle = (String) cmbType.getSelectedItem();
        TypeSalle type = null;
        for (TypeSalle t : TypeSalle.values()) {
            if (t.getLibelle().equals(typeLibelle)) { type = t; break; }
        }

        Salle s;
        if (type == TypeSalle.AMPHI) {
            s = new Amphi(salle != null ? salle.getId() : 0, numero, cap, batId,
                    chkAccessible.isSelected(), chkSono.isSelected(), chkRetrans.isSelected());
        } else if (type == TypeSalle.LABORATOIRE) {
            s = new Laboratoire(salle != null ? salle.getId() : 0, numero, cap, batId,
                    chkAccessible.isSelected(), (int)spPostes.getValue(), txtOS.getText().trim());
        } else {
            s = new Salle(salle != null ? salle.getId() : 0, numero, cap,
                    type, batId, chkAccessible.isSelected());
        }

        boolean ok = salle == null ? salleDAO.ajouter(s) : salleDAO.modifier(s);
        if (ok) { confirme = true; dispose(); }
        else UIUtils.messageErreur(this, "Erreur lors de l'enregistrement.");
    }

    public boolean isConfirme() { return confirme; }
}
