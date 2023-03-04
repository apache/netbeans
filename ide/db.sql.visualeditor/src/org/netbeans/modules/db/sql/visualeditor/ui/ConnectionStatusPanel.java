/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * ConnectionStatusPanl.java
 *
 * Created on March 16, 2005, 4:15 PM
 */

package org.netbeans.modules.db.sql.visualeditor.ui;

import org.netbeans.modules.db.sql.visualeditor.Log;
import java.awt.Image;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
/**
 *  Shows the status of a connction in a dialog.
 *  This just presentss the results of a testConnection(), it
 *  does not do the actual test.
 *
 * @author  jfbrown
 */
public class ConnectionStatusPanel extends javax.swing.JPanel {

    private JDialog dialog;
    private DialogDescriptor dlg = null;

    private JButton okButton = new JButton(NbBundle.getMessage(ConnectionStatusPanel.class, "OK")); // NOI18N

    // private Image conGood = Utilities.loadImage("org/netbeans/modules/db/sql/visualeditor/resources/started.png");  // NOI18N
    // private Image conFailed = Utilities.loadImage("org/netbeans/modules/db/sql/visualeditor/resources/disconnected.png");  // NOI18N
    private Image conGood = ImageUtilities.loadImage("org/netbeans/modules/db/sql/visualeditor/resources/ok.gif");  // NOI18N
    private Image conFailed = ImageUtilities.loadImage("org/netbeans/modules/db/sql/visualeditor/resources/error.gif");  // NOI18N
    private Image conWarning = ImageUtilities.loadImage("org/netbeans/modules/db/sql/visualeditor/resources/warning.gif");  // NOI18N
    /** Creates new form ConnectionStatusDialog */
    public ConnectionStatusPanel() {
        initComponents();
        connectionStatusMessage.setBackground( connectionStatusText.getBackground() ) ;
        valStatusMessage.setBackground( valStatusText.getBackground() ) ;
        generalInfo.setBackground( connectionStatusText.getBackground()) ;
        footerInfo.setBackground( connectionStatusText.getBackground()) ;
        generalInfo.setVisible(false) ;
        footerSeparator.setVisible(false) ;
        footerInfo.setVisible(false) ;
        okButton.getAccessibleContext().setAccessibleName(okButton.getText() ) ;
        okButton.getAccessibleContext().setAccessibleDescription(okButton.getText() ) ;
    }
    public void setGeneralInfo(String info) {
        generalInfo.setText(info) ;
        generalInfo.setVisible(true) ;
    }
    public void setFooterInfo(String info) {
        footerInfo.setText(info) ;
        footerInfo.setVisible(true) ;
        footerSeparator.setVisible(true) ;
    }
    public void showDialog( String dsName, boolean connected, String sqlException ) {
        showDialog( dsName, connected, sqlException, null, 0, false ) ;
    }
    public void showDialog( String dsName, boolean connected, String sqlException, String tableName, int rows ) {
        showDialog( dsName, connected, sqlException, tableName, rows, true ) ;
    }
    public void showDialog( String dsName, boolean connected, String sqlException, String tableName, int rows, boolean showValTableInfo ) {

        /* calculate the displayd values based on this method's input parameters.
         */
        configureDisplay(dsName, connected, sqlException, tableName, rows, showValTableInfo) ;

        displayDialog(dsName) ;
    }

    public void displayDialog(String dsName) {
	
	Log.getLogger().entering("ConnectionStatusPanel", "displayDialog", dsName);

        // Add a listener to the dialog's buttons
        ActionListener listener = new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialog.dispose();
            }
        };
        dlg = new DialogDescriptor(this,
                getMsg( "ConStat_title", dsName), // NOI18N
                true, listener);
        dlg.setOptions(new Object[] { okButton });
        dlg.setClosingOptions( null );
        dlg.setValid(false);
        
        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dlg);
        dialog.getAccessibleContext().setAccessibleName(getMsg( "ConStat_title", dsName)) ;
        dialog.getAccessibleContext().setAccessibleDescription(getMsg( "ConStat_title", dsName)) ;
        this.getAccessibleContext().setAccessibleName(getMsg( "ConStat_title", dsName)) ;
        this.getAccessibleContext().setAccessibleDescription(getMsg( "ConStat_title", dsName)) ;
        dialog.setResizable(true);
        dialog.pack() ;

        dialog.show();
    }

    public void configureDisplay( String dsName, boolean connected, String sqlException, String tableName, 
            int rows, boolean showValTableInfo ) {
        /* calculate the displayd values based on this method's input parameters.
         */
        if ( connected ) {
            connectionStatusIcon.setIcon( new ImageIcon(conGood) ) ;
            connectionStatusText.setText(getMsg("ConStat_succeeded_msg")) ; // NOI18N
            connectionStatusMessage.setVisible(false) ;
            validationInfo.setVisible(true) ;
            if ( sqlException == null ) {
                valStatusText.setText(getMsg("ConStat_rows_selected_msg", tableName, Integer.valueOf(rows)) ) ; // NOI18N
                valStatusIcon.setIcon( new ImageIcon(conGood) ) ;
                String valMsg ;
                if (rows > 1 ) {
                    
                    valMsg = getMsg("ConStat_valtable_bad_msg") ;// NOI18N
                    valStatusMessageIcon.setIcon( new ImageIcon(conWarning) ) ;
                } else {
                    valMsg = getMsg("ConStat_valtable_good_msg") ;// NOI18N
                    valStatusMessageIcon.setIcon( new ImageIcon(conGood) ) ;
                }
                valMsg = valMsg + "\n" + getMsg("ConStat_validationTableInfo") ; // NOI18N
                valStatusMessage.setText(valMsg) ;
                
            } else if (tableName != null) {
                // validation table test failed.
                valStatusIcon.setIcon( new ImageIcon(conFailed) ) ;

                // validation failed.
                valStatusText.setText(getMsg("ConStat_failed_msg")) ;// NOI18N

                String valMsg ;
                if ( tableName != null && tableName.trim().length() < 1 ) {
                    valMsg = getMsg("ConStat_valtable_needtable_msg" ) ;
                } else {
                    valMsg = sqlException ;
                }
                valMsg +=  "\n" +  getMsg("ConStat_validationTableInfo") ;
                valStatusMessage.setText(valMsg) ;
                
            } else {
                validationInfo.setVisible(false) ;
                valStatusLabel.setVisible(false) ;
            }
        } else {
            // connection failed.
            connectionStatusIcon.setIcon( new ImageIcon(conFailed) ) ;
            connectionStatusText.setText(getMsg("ConStat_failed_msg")) ;// NOI18N
            connectionStatusMessage.setText(sqlException) ;
            validationInfo.setVisible(false) ;
            valStatusLabel.setVisible(false) ;
        }        
        generalInfo.setVisible(false) ;
        footerSeparator.setVisible(false) ;
        footerInfo.setVisible(false) ;

    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        generalInfo = new javax.swing.JTextArea();
        connectionStatusLabel = new javax.swing.JLabel();
        connectionInfo = new javax.swing.JPanel();
        connectionStatusIcon = new javax.swing.JLabel();
        connectionStatusText = new javax.swing.JTextField();
        connectionStatusMessage = new javax.swing.JTextArea();
        valStatusLabel = new javax.swing.JLabel();
        validationInfo = new javax.swing.JPanel();
        valStatusIcon = new javax.swing.JLabel();
        valStatusText = new javax.swing.JTextField();
        valStatusMessageIcon = new javax.swing.JLabel();
        valStatusMessage = new javax.swing.JTextArea();
        footerSeparator = new javax.swing.JSeparator();
        footerInfo = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConnectionStatusPanel.class, "ConStat_title", new Object[] {}));
        generalInfo.setColumns(50);
        generalInfo.setEditable(false);
        generalInfo.setLineWrap(true);
        generalInfo.setText("General info text goes here");
        generalInfo.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        add(generalInfo, gridBagConstraints);

        connectionStatusLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ConnectionStatusPanel.class, "ConStat_test_mnemonic", new Object[] {}).charAt(0));
        connectionStatusLabel.setLabelFor(connectionStatusText);
        connectionStatusLabel.setText(org.openide.util.NbBundle.getMessage(ConnectionStatusPanel.class, "ConStat_test_label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(connectionStatusLabel, gridBagConstraints);
        connectionStatusLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionStatusPanel.class, "ConStat_test_label"));

        connectionInfo.setLayout(new java.awt.GridBagLayout());

        connectionStatusIcon.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        connectionInfo.add(connectionStatusIcon, gridBagConstraints);

        connectionStatusText.setEditable(false);
        connectionStatusText.setText("Succeeded");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        connectionInfo.add(connectionStatusText, gridBagConstraints);

        connectionStatusMessage.setColumns(50);
        connectionStatusMessage.setEditable(false);
        connectionStatusMessage.setLineWrap(true);
        connectionStatusMessage.setText("SQL exception goes here.");
        connectionStatusMessage.setWrapStyleWord(true);
        connectionStatusMessage.setMargin(new java.awt.Insets(1, 5, 2, 4));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        connectionInfo.add(connectionStatusMessage, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 0);
        add(connectionInfo, gridBagConstraints);

        valStatusLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ConnectionStatusPanel.class, "ConStat_valtable_mnemonic", new Object[] {}).charAt(0));
        valStatusLabel.setLabelFor(valStatusText);
        valStatusLabel.setText(org.openide.util.NbBundle.getMessage(ConnectionStatusPanel.class, "ConStat_valtable_label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(valStatusLabel, gridBagConstraints);
        valStatusLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConnectionStatusPanel.class, "ConStat_valtable_label"));
        valStatusLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConnectionStatusPanel.class, "ConStat_valtable_label"));

        validationInfo.setLayout(new java.awt.GridBagLayout());

        valStatusIcon.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        validationInfo.add(valStatusIcon, gridBagConstraints);

        valStatusText.setEditable(false);
        valStatusText.setText("Succeeded");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        validationInfo.add(valStatusText, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        validationInfo.add(valStatusMessageIcon, gridBagConstraints);

        valStatusMessage.setColumns(50);
        valStatusMessage.setEditable(false);
        valStatusMessage.setLineWrap(true);
        valStatusMessage.setText("SQL exception goes here.");
        valStatusMessage.setWrapStyleWord(true);
        valStatusMessage.setMargin(new java.awt.Insets(1, 5, 2, 4));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        validationInfo.add(valStatusMessage, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 0);
        add(validationInfo, gridBagConstraints);

        footerSeparator.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(footerSeparator, gridBagConstraints);

        footerInfo.setColumns(25);
        footerInfo.setEditable(false);
        footerInfo.setLineWrap(true);
        footerInfo.setText("footer text goes here");
        footerInfo.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        add(footerInfo, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel connectionInfo;
    private javax.swing.JLabel connectionStatusIcon;
    private javax.swing.JLabel connectionStatusLabel;
    private javax.swing.JTextArea connectionStatusMessage;
    private javax.swing.JTextField connectionStatusText;
    private javax.swing.JTextArea footerInfo;
    private javax.swing.JSeparator footerSeparator;
    private javax.swing.JTextArea generalInfo;
    private javax.swing.JLabel valStatusIcon;
    private javax.swing.JLabel valStatusLabel;
    private javax.swing.JTextArea valStatusMessage;
    private javax.swing.JLabel valStatusMessageIcon;
    private javax.swing.JTextField valStatusText;
    private javax.swing.JPanel validationInfo;
    // End of variables declaration//GEN-END:variables
    
    private String getMsg(String x) {
        return NbBundle.getMessage(ConnectionStatusPanel.class, x) ;
    }
    private String getMsg(String x, Object y) {
        return NbBundle.getMessage(ConnectionStatusPanel.class, x, y) ;
    }
    private String getMsg(String x, Object y, Object z ) {
        return NbBundle.getMessage(ConnectionStatusPanel.class, x, y, z) ;
    }
}
