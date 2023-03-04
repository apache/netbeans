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
package org.netbeans.modules.apisupport.project.ui.customizer;

import javax.swing.JPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.util.NbBundle;

final class ClusterizeVisualPanel2 extends JPanel
implements ExplorerManager.Provider {
    private final ExplorerManager em;
    private final ClusterizeWizardPanel2 panel;

    ClusterizeVisualPanel2(ClusterizeWizardPanel2 panel) {
        em = new ExplorerManager();
        em.setRootContext(panel.settings.modules);
        this.panel = panel;

        initComponents();
        view.addPropertyColumn("cnb", NbBundle.getMessage(ClusterizeInfo.class, "MSG_ClusterizeCodeNameBase")); // NOI18N
        view.addPropertyColumn("action", NbBundle.getMessage(ClusterizeInfo.class, "MSG_ClusterizeActivateAs")); // NOI18N
        view.setPopupAllowed(false);
        
        putClientProperty("WizardPanel_contentSelectedIndex", Integer.valueOf(1));
        putClientProperty("WizardPanel_contentData", panel.settings.getStep(1));
        putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
        putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
        putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
    }

    @Override
    public String getName() {
        if (panel == null || panel.settings == null) {
            return "";
        }
        return panel.settings.getStep(1);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        view = new org.openide.explorer.view.OutlineView();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 496, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(view, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 221, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(view, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.openide.explorer.view.OutlineView view;
    // End of variables declaration//GEN-END:variables

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
}

