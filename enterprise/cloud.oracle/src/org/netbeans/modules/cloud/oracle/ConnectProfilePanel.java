/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cloud.oracle;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class ConnectProfilePanel extends javax.swing.JPanel{
    private List<OCIProfile> allProfiles;
    
    private DefaultListModel<OCIProfile> model = new DefaultListModel<>();
    /**
     * Creates new form AddProfilePanel
     */
    public ConnectProfilePanel() {
        initComponents();
        ((CardLayout)cards.getLayout()).show(cards, "msg");
        lblMessageIcon.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/cloud/oracle/resources/info.png", false));
        lstProfiles.setCellRenderer(new R());

        textMessage.setEditable(false);
        textMessage.addHyperlinkListener(new HyperlinkListener() {
             @Override
             public void hyperlinkUpdate(HyperlinkEvent hle) {
                 if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                     Desktop desktop = Desktop.getDesktop();
                     try {
                         desktop.browse(hle.getURL().toURI());
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
             }
         });
    }
    
    public void setProfiles(List<OCIProfile> profiles) {
        DefaultListModel mdl = new DefaultListModel();
        for (OCIProfile p : profiles) {
            mdl.addElement(p);
        }
        lstProfiles.setModel(mdl);
        model = mdl;
        
        // switch the card:
        ((CardLayout)cards.getLayout()).show(cards, "profiles");
    }
    
    public void showErrorMessage(String msg) {
        lblMessageIcon.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/cloud/oracle/resources/error.png", false));
        textMessage.setContentType("text/html");
        textMessage.setText(msg);
        ((CardLayout)cards.getLayout()).show(cards, "msg");
    }
    
    public void setSelectedProfiles(List<OCIProfile> profiles) {
        lstProfiles.clearSelection();
        for (OCIProfile p : profiles) {
            int index = model.indexOf(p);
            if (index >= 0) {
                lstProfiles.getSelectionModel().addSelectionInterval(index, index);
            }
        }
    }
    
    public boolean isContentValid() {
        return !lstProfiles.getSelectedValuesList().isEmpty();
    }
    
    public List<OCIProfile> getSelectedProfiles() {
        return new ArrayList<>(lstProfiles.getSelectedValuesList());
    }

    @NbBundle.Messages({
        "# {0} tenancy name",
        "# {1} profile name",
        "LBL_DefaultConfigProfile={0} ({1})",
        "# {0} tenancy name",
        "# {1} profile",
        "# {2} custom config filename",
        "LBL_CustomConfigProfile={2}: {0} ({1})"
    })
    static class R extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            OCIProfile p = (OCIProfile)value;
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (!(c instanceof JLabel)) {
                return c;
            }
            JLabel l = (JLabel)c;
            l.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/cloud/oracle/resources/tenancy.svg", false));
            // note: the tenancy data should have been fetched before setProfiles() is called to validate
            // tenancy presence
            if (p.isDefaultConfig()) {
                l.setText(Bundle.LBL_DefaultConfigProfile(p.getTenancyData().getName(), p.getId()));
            } else {
                l.setText(Bundle.LBL_CustomConfigProfile(p.getTenancyData().getName(), p.getId(), p.getConfigPath().getFileName().toString()));
                l.setToolTipText(p.getConfigPath().toString());
            }
            l.setToolTipText(p.getTenancy().get().getDescription());
            return c;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txConfigPath = new javax.swing.JTextField();
        lblConfig = new javax.swing.JLabel();
        cards = new javax.swing.JPanel();
        profilesSelection = new javax.swing.JPanel();
        lblProfiles = new javax.swing.JLabel();
        scrProfiles = new javax.swing.JScrollPane();
        lstProfiles = new javax.swing.JList<>();
        message = new javax.swing.JPanel();
        lblMessageIcon = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textMessage = new javax.swing.JTextPane();

        txConfigPath.setEditable(false);
        txConfigPath.setText(org.openide.util.NbBundle.getMessage(ConnectProfilePanel.class, "ConnectProfilePanel.txConfigPath.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblConfig, org.openide.util.NbBundle.getMessage(ConnectProfilePanel.class, "ConnectProfilePanel.lblConfig.text")); // NOI18N

        cards.setLayout(new java.awt.CardLayout());

        org.openide.awt.Mnemonics.setLocalizedText(lblProfiles, org.openide.util.NbBundle.getMessage(ConnectProfilePanel.class, "ConnectProfilePanel.lblProfiles.text")); // NOI18N

        lstProfiles.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstProfilesValueChanged(evt);
            }
        });
        scrProfiles.setViewportView(lstProfiles);

        javax.swing.GroupLayout profilesSelectionLayout = new javax.swing.GroupLayout(profilesSelection);
        profilesSelection.setLayout(profilesSelectionLayout);
        profilesSelectionLayout.setHorizontalGroup(
            profilesSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(profilesSelectionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblProfiles)
                .addGap(61, 61, 61)
                .addComponent(scrProfiles, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                .addContainerGap())
        );
        profilesSelectionLayout.setVerticalGroup(
            profilesSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(profilesSelectionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(profilesSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrProfiles, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                    .addGroup(profilesSelectionLayout.createSequentialGroup()
                        .addComponent(lblProfiles)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        cards.add(profilesSelection, "profiles");

        org.openide.awt.Mnemonics.setLocalizedText(lblMessageIcon, org.openide.util.NbBundle.getMessage(ConnectProfilePanel.class, "ConnectProfilePanel.lblMessageIcon.text")); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        textMessage.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        textMessage.setText(org.openide.util.NbBundle.getMessage(ConnectProfilePanel.class, "ConnectProfilePanel.textMessage.text")); // NOI18N
        jScrollPane1.setViewportView(textMessage);

        javax.swing.GroupLayout messageLayout = new javax.swing.GroupLayout(message);
        message.setLayout(messageLayout);
        messageLayout.setHorizontalGroup(
            messageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(messageLayout.createSequentialGroup()
                .addGap(97, 97, 97)
                .addComponent(lblMessageIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        messageLayout.setVerticalGroup(
            messageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(messageLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(messageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(messageLayout.createSequentialGroup()
                        .addComponent(lblMessageIcon)
                        .addContainerGap(112, Short.MAX_VALUE))
                    .addComponent(jScrollPane1)))
        );

        cards.add(message, "msg");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cards, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblConfig)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txConfigPath)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txConfigPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblConfig))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cards, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void lstProfilesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstProfilesValueChanged
        firePropertyChange("contentValid", null, null);
    }//GEN-LAST:event_lstProfilesValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cards;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblConfig;
    private javax.swing.JLabel lblMessageIcon;
    private javax.swing.JLabel lblProfiles;
    private javax.swing.JList<OCIProfile> lstProfiles;
    private javax.swing.JPanel message;
    private javax.swing.JPanel profilesSelection;
    private javax.swing.JScrollPane scrProfiles;
    private javax.swing.JTextPane textMessage;
    private javax.swing.JTextField txConfigPath;
    // End of variables declaration//GEN-END:variables
}
