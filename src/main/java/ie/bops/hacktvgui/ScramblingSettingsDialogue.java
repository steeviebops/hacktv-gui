/*
 * Copyright (C) 2026 Stephen McGarry
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ie.bops.hacktvgui;

import static ie.bops.hacktvgui.MainWindow.PREFS;
import ie.bops.hacktvgui.ScramblingSettings.VideoCryptEmmState;
import java.awt.CardLayout;
import javax.swing.JOptionPane;

public class ScramblingSettingsDialogue extends javax.swing.JDialog {
    
    private ScramblingInfo selectedCA;
    private String selectedKey;
    private ScramblingSettings settings;
    
    public ScramblingSettingsDialogue(java.awt.Frame parent, boolean modal, ScramblingSettings s) {
        super(parent, modal);
        initComponents();
        settings = s;
    }
    
    public void postInit(ScramblingInfo ca, String key) {
        if (ca == null || ca.id() == null) return;
        selectedCA = ca;
        selectedKey = key;
        chkShowECM.setEnabled(ca.ecmSupported() && !key.equals("free"));
       
        // EuroCrypt and Syster options
        chkScrambleAudio.setEnabled(ca.scrambleAudioSupported());
        lblSysterPermTable.setEnabled(ca.systerFeatures());
        cmbSysterPermTable.setEnabled(ca.systerFeatures());
        lblECMaturity.setEnabled(ca.eurocryptFeatures());
        cmbECMaturity.setEnabled(ca.eurocryptFeatures());
        chkECppv.setEnabled(ca.eurocryptFeatures());
        chkECNoDate.setEnabled(ca.eurocryptFeatures());
        
        // VideoCrypt options
        chkShowCardSerial.setEnabled(ca.videocryptFeatures());
        chkFindKeys.setEnabled(ca.videocryptFeatures() && key.equals("ppv"));
        
        // Select the correct panel in the CardLayout
        var cl = (CardLayout) scramblingCard.getLayout();
        cl.show(scramblingCard, ca.videocryptFeatures() ? "vc" : "nonvc");
        pack();
        
        // EMM options
        boolean emm = (ca.id().equals("videocrypt") && Shared.EMM_KEYS.contains(key)) ||
                (ca.id().equals("videocrypt2") && key.equals("conditional"));
        radNoEmm.setEnabled(emm);
        radActivateCard.setEnabled(emm);
        radDeactivateCard.setEnabled(emm);
        
        // Set focus on Cancel button
        btnCancel.requestFocusInWindow();
        
        // Get current settings
        if (settings == null) {
            // Enable defaults
            radNoEmm.setSelected(true);
            cmbSysterPermTable.setSelectedIndex(ca.systerFeatures() ? 0 : -1);
            lblECMaturity.setEnabled(ca.eurocryptFeatures());
            cmbECMaturity.setEnabled(ca.eurocryptFeatures());
            cmbECMaturity.setSelectedIndex(ca.eurocryptFeatures() ? 0 : -1);
            return;
        }
        chkShowECM.setSelected(settings.showECM());
        
        chkScrambleAudio.setSelected(settings.scrambleAudio());
        
        cmbSysterPermTable.setSelectedIndex(settings.systerPermTable());
        
        cmbECMaturity.setSelectedIndex(settings.eurocryptMaturityRating());
        if (settings.eurocryptPpv()) {
            chkECppv.doClick();
            String pn = settings.eurocryptProgNumber();
            String pc = settings.eurocryptProgCost();
            if (pn != null) txtECProgNumber.setText(pn);
            if (pc != null) txtECProgCost.setText(pc);
        }
        chkECNoDate.setSelected(settings.eurocryptNoDate());
        
        if (!emm) {
            // The settings may contain invalid data from a
            // previous state, so don't use it.
            radNoEmm.setSelected(true);
            txtCardNumber.setText("");
            txtCardNumber.setEnabled(false);
        } else {
            switch (settings.videocryptEmmState()) {
                case NO_EMM:
                default:
                    radNoEmm.doClick();
                    break;
                case ENABLE_EMM:
                    radActivateCard.doClick();
                    String cn = settings.videocryptCardNumber();
                    if (cn != null) txtCardNumber.setText(cn);
                    break;
                case DISABLE_EMM:
                    radDeactivateCard.doClick();
                    String cn2 = settings.videocryptCardNumber();
                    if (cn2 != null) txtCardNumber.setText(cn2);
                    break;
            }
        }
        chkShowCardSerial.setSelected(ca.videocryptFeatures() && settings.showCardSerial());
        chkFindKeys.setSelected(ca.videocryptFeatures() && key.equals("ppv") && settings.findKeys());
    }

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        vcButtonGroup = new javax.swing.ButtonGroup();
        containerPanel = new javax.swing.JPanel();
        chkShowECM = new javax.swing.JCheckBox();
        scramblingCard = new javax.swing.JPanel();
        scramblingOptionsPanel = new javax.swing.JPanel();
        chkScrambleAudio = new javax.swing.JCheckBox();
        cmbSysterPermTable = new javax.swing.JComboBox<>();
        lblSysterPermTable = new javax.swing.JLabel();
        lblECMaturity = new javax.swing.JLabel();
        cmbECMaturity = new javax.swing.JComboBox<>();
        chkECppv = new javax.swing.JCheckBox();
        lblECProgNumber = new javax.swing.JLabel();
        lblECProgCost = new javax.swing.JLabel();
        txtECProgNumber = new javax.swing.JTextField();
        txtECProgCost = new javax.swing.JTextField();
        chkECNoDate = new javax.swing.JCheckBox();
        vcPanel = new javax.swing.JPanel();
        lblCardNumber = new javax.swing.JLabel();
        txtCardNumber = new javax.swing.JTextField();
        chkShowCardSerial = new javax.swing.JCheckBox();
        chkFindKeys = new javax.swing.JCheckBox();
        radNoEmm = new javax.swing.JRadioButton();
        radActivateCard = new javax.swing.JRadioButton();
        radDeactivateCard = new javax.swing.JRadioButton();
        buttonPanel = new javax.swing.JPanel();
        btnOK = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Scrambling options");
        setResizable(false);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        containerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Advanced scrambling options"));
        containerPanel.setLayout(new java.awt.GridBagLayout());

        chkShowECM.setText("Show ECMs on console");
        chkShowECM.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 5, 0);
        containerPanel.add(chkShowECM, gridBagConstraints);

        scramblingCard.setLayout(new java.awt.CardLayout());

        scramblingOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Format-specific options"));
        scramblingOptionsPanel.setLayout(new java.awt.GridBagLayout());

        chkScrambleAudio.setText("Scramble audio");
        chkScrambleAudio.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        scramblingOptionsPanel.add(chkScrambleAudio, gridBagConstraints);

        cmbSysterPermTable.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Default", "1", "2" }));
        cmbSysterPermTable.setSelectedIndex(-1);
        cmbSysterPermTable.setEnabled(false);
        cmbSysterPermTable.addMouseWheelListener(this::cmbSysterPermTableMouseWheelMoved);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        scramblingOptionsPanel.add(cmbSysterPermTable, gridBagConstraints);

        lblSysterPermTable.setText("Syster permutation table");
        lblSysterPermTable.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        scramblingOptionsPanel.add(lblSysterPermTable, gridBagConstraints);

        lblECMaturity.setText("EuroCrypt maturity rating");
        lblECMaturity.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        scramblingOptionsPanel.add(lblECMaturity, gridBagConstraints);

        cmbECMaturity.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
        cmbECMaturity.setSelectedIndex(-1);
        cmbECMaturity.setEnabled(false);
        cmbECMaturity.addMouseWheelListener(this::cmbECMaturityMouseWheelMoved);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        scramblingOptionsPanel.add(cmbECMaturity, gridBagConstraints);

        chkECppv.setText("EuroCrypt pay-per-view mode");
        chkECppv.setEnabled(false);
        chkECppv.addActionListener(this::chkECppvActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        scramblingOptionsPanel.add(chkECppv, gridBagConstraints);

        lblECProgNumber.setText("Programme number");
        lblECProgNumber.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        scramblingOptionsPanel.add(lblECProgNumber, gridBagConstraints);

        lblECProgCost.setText("Programme cost");
        lblECProgCost.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        scramblingOptionsPanel.add(lblECProgCost, gridBagConstraints);

        txtECProgNumber.setEnabled(false);
        txtECProgNumber.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtECProgNumberKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 41;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        scramblingOptionsPanel.add(txtECProgNumber, gridBagConstraints);

        txtECProgCost.setEnabled(false);
        txtECProgCost.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtECProgCostKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 41;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        scramblingOptionsPanel.add(txtECProgCost, gridBagConstraints);

        chkECNoDate.setText("No date");
        chkECNoDate.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        scramblingOptionsPanel.add(chkECNoDate, gridBagConstraints);

        scramblingCard.add(scramblingOptionsPanel, "nonVC");

        vcPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("VideoCrypt options"));
        vcPanel.setLayout(new java.awt.GridBagLayout());

        lblCardNumber.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCardNumber.setText("Card number");
        lblCardNumber.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        vcPanel.add(lblCardNumber, gridBagConstraints);

        txtCardNumber.setEditable(false);
        txtCardNumber.setEnabled(false);
        txtCardNumber.addMouseListener(new ContextMenuListener());
        txtCardNumber.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCardNumberKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 93;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        vcPanel.add(txtCardNumber, gridBagConstraints);

        chkShowCardSerial.setText("Show card serial");
        chkShowCardSerial.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        vcPanel.add(chkShowCardSerial, gridBagConstraints);

        chkFindKeys.setText("Find keys on PPV card");
        chkFindKeys.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        vcPanel.add(chkFindKeys, gridBagConstraints);

        vcButtonGroup.add(radNoEmm);
        radNoEmm.setText("No EMMs");
        radNoEmm.setEnabled(false);
        radNoEmm.addActionListener(this::radNoEmmActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        vcPanel.add(radNoEmm, gridBagConstraints);

        vcButtonGroup.add(radActivateCard);
        radActivateCard.setText("Activate card");
        radActivateCard.setEnabled(false);
        radActivateCard.addActionListener(this::radActivateCardActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        vcPanel.add(radActivateCard, gridBagConstraints);

        vcButtonGroup.add(radDeactivateCard);
        radDeactivateCard.setText("Deactivate card");
        radDeactivateCard.setEnabled(false);
        radDeactivateCard.addActionListener(this::radDeactivateCardActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        vcPanel.add(radDeactivateCard, gridBagConstraints);

        scramblingCard.add(vcPanel, "vc");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 5, 0);
        containerPanel.add(scramblingCard, gridBagConstraints);

        getContentPane().add(containerPanel);

        btnOK.setText("OK");
        btnOK.addActionListener(this::btnOKActionPerformed);
        buttonPanel.add(btnOK);

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(this::btnCancelActionPerformed);
        buttonPanel.add(btnCancel);

        getContentPane().add(buttonPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    public ScramblingSettings getSettings() {
        return settings;
    }
    
    private void showEMMWarning() {
        if (!isVisible()) return;
        if (PREFS.get("SuppressWarnings", "0").equals("1")) return;
        Shared.messageBox("""
                          Care is advised when using this option.
                          Incorrect use may permanently damage the viewing card.
                          Do not use this option on an issue number other than the one selected.
                          """,
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    private void cmbSysterPermTableMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbSysterPermTableMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbSysterPermTable);
    }//GEN-LAST:event_cmbSysterPermTableMouseWheelMoved

    private void cmbECMaturityMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cmbECMaturityMouseWheelMoved
        Shared.mouseWheelComboBoxHandler(evt.getWheelRotation(), cmbECMaturity);
    }//GEN-LAST:event_cmbECMaturityMouseWheelMoved

    private void chkECppvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkECppvActionPerformed
        boolean b = chkECppv.isSelected();
        lblECProgNumber.setEnabled(b);
        txtECProgNumber.setEnabled(b);
        txtECProgNumber.setEditable(b);
        lblECProgCost.setEnabled(b);
        txtECProgCost.setEnabled(b);
        txtECProgCost.setEditable(b);
        if (!b) txtECProgNumber.setText("");
        if (!b) txtECProgCost.setText("");
    }//GEN-LAST:event_chkECppvActionPerformed

    private void txtECProgNumberKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtECProgNumberKeyTyped
        if (!Shared.isNumeric(String.valueOf(evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtECProgNumberKeyTyped

    private void txtECProgCostKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtECProgCostKeyTyped
        if (!Shared.isNumeric(String.valueOf(evt.getKeyChar()))) {
            evt.consume();
        }
    }//GEN-LAST:event_txtECProgCostKeyTyped

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        boolean showECMs;
        boolean scrambleAudio;
        int systerPermTable;
        int eurocryptMaturityRating;
        boolean eurocryptPpv;
        boolean eurocryptNoDate;
        String eurocryptProgNumber = null;
        String eurocryptProgCost = null;
        VideoCryptEmmState videocryptEmmState;
        String videocryptCardNumber = null;
        boolean showCardSerial;
        boolean findKeys;
        // Check settings that require validation first...
        // EuroCrypt programme number and cost
        if (chkECppv.isSelected()) {
            if (Shared.isNumeric(txtECProgNumber.getText())) {
                eurocryptProgNumber = txtECProgNumber.getText();
            } else {
                Shared.messageBox("Please enter a numeric programme number value.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (Shared.isNumeric(txtECProgCost.getText())) {
                eurocryptProgCost = txtECProgCost.getText();
            } else {
                Shared.messageBox("Please enter a numeric programme cost value.", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        // VideoCrypt viewing card number
        if (radActivateCard.isSelected()) {
            videocryptEmmState = VideoCryptEmmState.ENABLE_EMM;
        } else if (radDeactivateCard.isSelected()) {
            videocryptEmmState = VideoCryptEmmState.DISABLE_EMM;
        } else {
            videocryptEmmState = VideoCryptEmmState.NO_EMM;
        }
        if (videocryptEmmState != VideoCryptEmmState.NO_EMM) {
            if (Shared.checkCardNumber(txtCardNumber.getText(), selectedCA, selectedKey) != null) {
                videocryptCardNumber = txtCardNumber.getText();
            } else {
                return;
            }
        }
        // Apply other options
        showECMs = chkShowECM.isSelected();
        scrambleAudio = chkScrambleAudio.isSelected();
        systerPermTable = cmbSysterPermTable.getSelectedIndex();
        eurocryptMaturityRating = cmbECMaturity.getSelectedIndex();
        eurocryptPpv = chkECppv.isSelected();
        eurocryptNoDate = chkECNoDate.isSelected();
        // VideoCrypt options
        showCardSerial = chkShowCardSerial.isSelected();
        findKeys = chkFindKeys.isSelected();
        settings = new ScramblingSettings(
                showECMs,
                scrambleAudio,
                systerPermTable,
                eurocryptMaturityRating,
                eurocryptPpv,
                eurocryptNoDate,
                eurocryptProgNumber,
                eurocryptProgCost,
                videocryptEmmState,
                videocryptCardNumber,
                showCardSerial,
                findKeys
        );
        dispose();
    }//GEN-LAST:event_btnOKActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void txtCardNumberKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCardNumberKeyTyped
        if (!Shared.isNumeric(String.valueOf(evt.getKeyChar()))) {
            evt.consume();
        }
        else if (txtCardNumber.getText().length() >= 13) {
            evt.consume();
        }
    }//GEN-LAST:event_txtCardNumberKeyTyped

    private void radNoEmmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radNoEmmActionPerformed
        lblCardNumber.setEnabled(false);
        txtCardNumber.setEnabled(false);
        txtCardNumber.setEditable(false);
        txtCardNumber.setText("");
    }//GEN-LAST:event_radNoEmmActionPerformed

    private void radActivateCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radActivateCardActionPerformed
        showEMMWarning();
        lblCardNumber.setEnabled(true);
        txtCardNumber.setEditable(true);
        txtCardNumber.setEnabled(true);
    }//GEN-LAST:event_radActivateCardActionPerformed

    private void radDeactivateCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radDeactivateCardActionPerformed
        showEMMWarning();
        lblCardNumber.setEnabled(true);
        txtCardNumber.setEditable(true);
        txtCardNumber.setEnabled(true);
    }//GEN-LAST:event_radDeactivateCardActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JCheckBox chkECNoDate;
    private javax.swing.JCheckBox chkECppv;
    private javax.swing.JCheckBox chkFindKeys;
    private javax.swing.JCheckBox chkScrambleAudio;
    private javax.swing.JCheckBox chkShowCardSerial;
    private javax.swing.JCheckBox chkShowECM;
    private javax.swing.JComboBox<String> cmbECMaturity;
    private javax.swing.JComboBox<String> cmbSysterPermTable;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JLabel lblCardNumber;
    private javax.swing.JLabel lblECMaturity;
    private javax.swing.JLabel lblECProgCost;
    private javax.swing.JLabel lblECProgNumber;
    private javax.swing.JLabel lblSysterPermTable;
    private javax.swing.JRadioButton radActivateCard;
    private javax.swing.JRadioButton radDeactivateCard;
    private javax.swing.JRadioButton radNoEmm;
    private javax.swing.JPanel scramblingCard;
    private javax.swing.JPanel scramblingOptionsPanel;
    private javax.swing.JTextField txtCardNumber;
    private javax.swing.JTextField txtECProgCost;
    private javax.swing.JTextField txtECProgNumber;
    private javax.swing.ButtonGroup vcButtonGroup;
    private javax.swing.JPanel vcPanel;
    // End of variables declaration//GEN-END:variables
}
