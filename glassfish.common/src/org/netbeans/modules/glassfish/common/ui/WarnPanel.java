/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.ui;

import org.netbeans.modules.glassfish.common.GlassFishSettings;
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
     * Display GlassFish 3.1.2 WS bug warning message and handle <i>Show this
     * warning next time</i> check box.
     * <p/>
     * @param serverName GlassFish server display name.
     */
    public static void gf312WSWarning(final String serverName) {
        boolean showAgain = GlassFishSettings.getGf312WarningShowAgain();
        if (showAgain) {
            String warning = NbBundle.getMessage(
                    WarnPanel.class, "WarnPanel.Gf312WSWarning", serverName);
            WarnPanel panel =  new WarnPanel(warning, showAgain);
            NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Message(
                    panel, NotifyDescriptor.PLAIN_MESSAGE);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            GlassFishSettings.setGf312WarningShowAgain(panel.showAgain());
        }
    }

    /**
     * Display GlassFish 3.1.2 WS bug warning message and handle <i>Show this
     * warning next time</i> check box.
     * <p/>
     * @param serverName  GlassFish server display name.
     * @param installRoot GlassFish server installation root.
     */
    public static void gfUnknownVersionWarning(final String serverName,
            final String installRoot) {
        if (GlassFishSettings.showWindowSystem()) {
            String warning = NbBundle.getMessage(
                    WarnPanel.class, "WarnPanel.gfUnknownVersionWarning",
                    new String[]{serverName, installRoot});
            NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Message(
                    warning, NotifyDescriptor.PLAIN_MESSAGE);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
        }
    }

    /**
     * Display GlassFish process kill warning message and handle <i>Show this
     * warning next time</i> check box.
     * <p/>
     * @param serverName GlassFish server display name.
     * @return Value of <code>true</code> when <code>YES</code> button
     *         was selected or Value of <code>false</code> when <code>NO/code>
     *         button was selected. Always returns true after <i>Show this
     *         warning next time</i> check box was turned on.
     */
    public static boolean gfKillWarning(final String serverName) {
        boolean showAgain = GlassFishSettings.getGfKillWarningShowAgain();
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
            GlassFishSettings.setGfKillWarningShowAgain(panel.showAgain());
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
