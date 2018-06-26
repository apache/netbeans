/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.ide.ergonomics.newproject;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;

public final class InstallPanel extends JPanel {
    private String name = null;

    /** Creates new form InstallMissingModulesWizardVisualPanel1 */
    public InstallPanel (String name) {
        //initComponents();
        this.name = name;
    }

    public @Override String getName() {
        return name;
    }

    public void displayInstallTask (
                final JComponent downloadMainLabel,
                final JComponent downloadDetailLabel,
                final JComponent downloadProgress,
                final JComponent verifyMainLabel,
                final JComponent verifyDetailLabel,
                final JComponent verifyProgress,
                final JComponent installMainLabel,
                final JComponent installDetailLabel,
                final JComponent installProgress
            ) {
        if (SwingUtilities.isEventDispatchThread ()) {
            doDisplayInstallTask (
                    downloadMainLabel,
                    downloadDetailLabel,
                    downloadProgress,
                    verifyMainLabel,
                    verifyDetailLabel,
                    verifyProgress,
                    installMainLabel,
                    installDetailLabel,
                    installProgress);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    doDisplayInstallTask (
                            downloadMainLabel,
                            downloadDetailLabel,
                            downloadProgress,
                            verifyMainLabel,
                            verifyDetailLabel,
                            verifyProgress,
                            installMainLabel,
                            installDetailLabel,
                            installProgress);
                }
            });
        }
    }
    
    private void doDisplayInstallTask (
                JComponent downloadMainLabel,
                JComponent downloadDetailLabel,
                JComponent downloadProgress,
                JComponent verifyMainLabel,
                JComponent verifyDetailLabel,
                JComponent verifyProgress,
                JComponent installMainLabel,
                JComponent installDetailLabel,
                JComponent installProgress
            ) {
        
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(downloadMainLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downloadDetailLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(54, 54, 54))
                    .addComponent(downloadProgress, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(verifyMainLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(verifyDetailLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(54, 54, 54))
                    .addComponent(verifyProgress, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(installMainLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(installDetailLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(54, 54, 54))
                    .addComponent(installProgress, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(downloadMainLabel)
                    .addComponent(downloadDetailLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(downloadProgress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(verifyMainLabel)
                    .addComponent(verifyDetailLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(verifyProgress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(installMainLabel)
                    .addComponent(installDetailLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(installProgress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
    }
    
    public void displayEnableTask (
            final JComponent enableMainLabel,
            final JComponent enableDetailLabel,
            final JComponent enableComponent) {
        if (SwingUtilities.isEventDispatchThread ()) {
            doDisplayEnableTask (enableMainLabel, enableDetailLabel, enableComponent);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    doDisplayEnableTask (enableMainLabel, enableDetailLabel, enableComponent);
                }
            });
        }
    }
    
    private void doDisplayEnableTask (
                JComponent enableMainLabel,
                JComponent enableDetailLabel,
                JComponent enableProgress
            ) {
        
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(enableMainLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(enableDetailLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(54, 54, 54))
                    .addComponent(enableProgress, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(enableMainLabel)
                    .addComponent(enableDetailLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(enableProgress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pCentral = new javax.swing.JPanel();

        pCentral.setLayout(new javax.swing.BoxLayout(pCentral, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pCentral, javax.swing.GroupLayout.DEFAULT_SIZE, 12, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pCentral, javax.swing.GroupLayout.DEFAULT_SIZE, 12, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pCentral;
    // End of variables declaration//GEN-END:variables

}

