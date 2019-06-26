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
package org.netbeans.modules.payara.common.ui;

import org.netbeans.modules.payara.common.PayaraSettings;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import static org.openide.NotifyDescriptor.YES_OPTION;
import org.openide.util.NbBundle;

/**
 * Warning panel showing warning message and check box to let user enable
 * or disable showing this warning again in the future.
 * <p/>
 * @author Tomas Kraus
 */
public class WarnPanel extends javax.swing.JPanel {

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Display warning message and handle <i>Show this warning next time</i> check box.
     * <p/>
     * @param serverName  Payara server display name.
     * @param installRoot Payara server installation root.
     */
    public static void pfUnknownVersionWarning(final String serverName,
            final String installRoot) {
        if (PayaraSettings.showWindowSystem()) {
            String warning = NbBundle.getMessage(
                    WarnPanel.class, "WarnPanel.gfUnknownVersionWarning",
                    new String[]{serverName, installRoot});
            NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Message(
                    warning, NotifyDescriptor.PLAIN_MESSAGE);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
        }
    }

    /**
     * Display Payara process kill warning message and handle <i>Show this
     * warning next time</i> check box.
     * <p/>
     * @param serverName Payara server display name.
     * @return Value of <code>true</code> when <code>YES</code> button
     *         was selected or Value of <code>false</code> when <code>NO/code>
     *         button was selected. Always returns true after <i>Show this
     *         warning next time</i> check box was turned on.
     */
    public static boolean gfKillWarning(final String serverName) {
        boolean showAgain = PayaraSettings.getGfKillWarningShowAgain();
        if (showAgain) {
            String warning = NbBundle.getMessage(
                    WarnPanel.class, "WarnPanel.GfKillWarning", serverName);
            String title = NbBundle.getMessage(
                    WarnPanel.class, "WarnPanel.GfKillTitle");
            WarnPanel panel =  new WarnPanel(warning, showAgain);
            NotifyDescriptor notifyDescriptor = new NotifyDescriptor(
                panel, title, NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE, null, null);
            Object button
                    = DialogDisplayer.getDefault().notify(notifyDescriptor);
            PayaraSettings.setGfKillWarningShowAgain(panel.showAgain());
            return button == YES_OPTION;
        } else {
            return true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Warning message to be shown in the panel. */
    private final String warning;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates new warning panel with show again check box.
     * <p/>
     * @param warning   Warning text.
     * @param showAgain Show again selection.
     */
    public WarnPanel(String warning, boolean showAgain) {
        this.warning = warning;
        initComponents();
        this.showAgain.setSelected(showAgain);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get value of show again check box.
     * <p/>
     * @return Value of show again check box.
     */
    public boolean showAgain() {
        return showAgain.isSelected();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Generated GUI code                                                     //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        warningLabel = new javax.swing.JLabel();
        showAgain = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(400, 125));

        warningLabel.setText(this.warning);
        warningLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        warningLabel.setMaximumSize(new java.awt.Dimension(51, 15));
        warningLabel.setMinimumSize(new java.awt.Dimension(51, 15));
        warningLabel.setPreferredSize(new java.awt.Dimension(51, 15));

        org.openide.awt.Mnemonics.setLocalizedText(showAgain, org.openide.util.NbBundle.getMessage(WarnPanel.class, "WarnPanel.showAgain.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(warningLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(showAgain)
                        .addGap(0, 149, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(warningLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showAgain)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox showAgain;
    private javax.swing.JLabel warningLabel;
    // End of variables declaration//GEN-END:variables
}
