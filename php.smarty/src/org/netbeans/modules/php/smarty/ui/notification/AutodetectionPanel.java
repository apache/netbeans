/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.smarty.ui.notification;

import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.smarty.SmartyPhpFrameworkProvider;
import static org.netbeans.modules.php.smarty.SmartyPhpFrameworkProvider.PROP_SMARTY_AVAILABLE;
import org.openide.util.NbBundle;

public class AutodetectionPanel extends JPanel {

    private final PhpModule phpModule;

    @NbBundle.Messages({
        "AutodetectionPanel.msg.autodetect=Smarty templates detected in project {0}. Click to enable its support.",
        "AutodetectionPanel.lbl.ignore=Do not show this again"
    })
    public AutodetectionPanel(PhpModule phpModule) {
        this.phpModule = phpModule;
        // UI roughly copied from org.netbeans.core.ui.notifications.NotificationDisplayerImpl
        initComponents();
        descriptionLabel.setText("<html><a href=\"#\">" + Bundle.AutodetectionPanel_msg_autodetect(phpModule.getDisplayName())); //NOI18N
        descriptionLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ignoreLabel.setText("<html><a href=\"#\">" + Bundle.AutodetectionPanel_lbl_ignore()); //NOI18N
        ignoreLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        descriptionLabel = new javax.swing.JButton();
        ignoreLabel = new javax.swing.JButton();

        setOpaque(false);

        descriptionLabel.setText("<html><u>There was found ..."); // NOI18N
        descriptionLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        descriptionLabel.setBorderPainted(false);
        descriptionLabel.setContentAreaFilled(false);
        descriptionLabel.setFocusPainted(false);
        descriptionLabel.setFocusable(false);
        descriptionLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        descriptionLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                descriptionLabelActionPerformed(evt);
            }
        });

        ignoreLabel.setText(org.openide.util.NbBundle.getMessage(AutodetectionPanel.class, "AutodetectionPanel.ignoreLabel.text")); // NOI18N
        ignoreLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ignoreLabel.setBorderPainted(false);
        ignoreLabel.setContentAreaFilled(false);
        ignoreLabel.setFocusPainted(false);
        ignoreLabel.setFocusable(false);
        ignoreLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ignoreLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreLabelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(ignoreLabel)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ignoreLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void descriptionLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_descriptionLabelActionPerformed
        Preferences preferences = phpModule.getPreferences(SmartyPhpFrameworkProvider.class, true);
        preferences.putBoolean(PROP_SMARTY_AVAILABLE, true);
        phpModule.notifyPropertyChanged(new PropertyChangeEvent(this, PhpModule.PROPERTY_FRAMEWORKS, null, null));
    }//GEN-LAST:event_descriptionLabelActionPerformed

    private void ignoreLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreLabelActionPerformed
        Preferences preferences = phpModule.getPreferences(SmartyPhpFrameworkProvider.class, true);
        preferences.putBoolean(PROP_SMARTY_AVAILABLE, false);
    }//GEN-LAST:event_ignoreLabelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton descriptionLabel;
    private javax.swing.JButton ignoreLabel;
    // End of variables declaration//GEN-END:variables

}
