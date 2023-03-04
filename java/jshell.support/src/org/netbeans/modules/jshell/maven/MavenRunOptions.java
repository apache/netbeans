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
package org.netbeans.modules.jshell.maven;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.project.JShellOptions2;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.HelpCtx;

/**
 *
 * @author sdedic
 */
public class MavenRunOptions extends javax.swing.JPanel implements HelpCtx.Provider {
    private final Project       project;
    private final ModelHandle2  handle;
    private final Category      category;
    private JShellOptions2      nestedOptions;
    
    private NetbeansActionMapping run;
    private NetbeansActionMapping debug;
    /**
     * Creates new form MavenRunOptions
     */
    public MavenRunOptions(Project project, Category category, ModelHandle2 handle) {
        this.project = project;
        this.category = category;
        this.handle = handle;
        
        initComponents();

        cbConfiguration.setEditable(false);
        cbConfiguration.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component com = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (com instanceof JLabel) {
                    if (value == handle.getActiveConfiguration()) {
                        com.setFont(com.getFont().deriveFont(Font.BOLD));
                    }
                }
                return com;
            }
        });
        
        nestedOptions = (JShellOptions2)pOptions;
        
        setupConfigurations();
        
        loadOptions(null);
        
        nestedOptions.setConfigChangeListener(this::optionsChanged);
        cbConfiguration.addActionListener(this::loadOptions);
    }
    
    private void loadOptions(ActionEvent e) {
        ActionToGoalMapping mapp = handle.getActionMappings((ModelHandle2.Configuration) cbConfiguration.getSelectedItem());
        List<NetbeansActionMapping> lst = mapp.getActions();
        run = null;
        debug = null;
        for (NetbeansActionMapping m : lst) {
            if (ActionProvider.COMMAND_RUN.equals(m.getActionName())) {
                run = m;
            }
            if (ActionProvider.COMMAND_DEBUG.equals(m.getActionName())) {
                debug = m;
            }
        }
        if (run == null) {
            run = ModelHandle2.getDefaultMapping(ActionProvider.COMMAND_RUN, project);
        }
        if (debug == null) {
            debug = ModelHandle2.getDefaultMapping(ActionProvider.COMMAND_DEBUG, project);
        }
        /*
        if (run == null) {
            // disable
            Queue<JComponent> comps = new ArrayDeque<>();
            comps.add(nestedOptions);
            while (!comps.isEmpty()) {
                JComponent c = comps.poll();
                c.setEnabled(false);
                if (c.getComponentCount() > 0) {
                    Component[] children = c.getComponents();
                    for (Component cc : children) {
                        if (cc instanceof JComponent) {
                            comps.offer((JComponent)cc);
                        }
                    }
                }
            }
        } else {
            nestedOptions.readOptions(run.getProperties());
        }
        */
        CardLayout cl = (CardLayout)detailPanel.getLayout();
        if (run == null) {
            cl.show(detailPanel, "disabled");
        } else {
            nestedOptions.readOptions(run.getProperties());
            cl.show(detailPanel, "jshell");
        }
    }
    
    private boolean updateMessage() {
         if (!nestedOptions.isPanelValid()) {
            category.setErrorMessage(nestedOptions.getErrorMessage());
            return true;
        } else {
            category.setErrorMessage(null);
            return false;
         }
    }
    
    private void optionsChanged(ChangeEvent e) {
        if (updateMessage()) {
            return;
        }
        if (run == null) {
            return;
        }
        ActionToGoalMapping a2gm = handle.getActionMappings((ModelHandle2.Configuration) cbConfiguration.getSelectedItem());
        changeConfiguration(run, a2gm);
        changeConfiguration(debug, a2gm);
        handle.markAsModified(a2gm);
    }
    
    private void changeConfiguration(NetbeansActionMapping am, ActionToGoalMapping a2gm) {
        if (am == null) {
            return;
        }
        Map<String, String> opts = nestedOptions.getChangedOptions();
        for (Map.Entry<String, String> entry : opts.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            if (v != null) {
                am.addProperty(k, v);
            } else {
                am.getProperties().remove(k);
            }
        }
        ModelHandle2.setUserActionMapping(am, a2gm);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("maven_jshell_run");
    }

    private void setupConfigurations() {
        DefaultComboBoxModel<ModelHandle2.Configuration> comModel = new DefaultComboBoxModel<>();
        for (ModelHandle2.Configuration conf : handle.getConfigurations()) {
            comModel.addElement(conf);
        }
        cbConfiguration.setModel(comModel);
        cbConfiguration.setSelectedItem(handle.getActiveConfiguration());
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        cbConfiguration = new javax.swing.JComboBox();
        detailPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        pOptions = new JShellOptions2(project);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MavenRunOptions.class, "MavenRunOptions.jLabel1.text")); // NOI18N

        detailPanel.setLayout(new java.awt.CardLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(MavenRunOptions.class, "MavenRunOptions.jLabel2.text")); // NOI18N
        jLabel2.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 492, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel2)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 115, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel2)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        detailPanel.add(jPanel1, "disabled");
        detailPanel.add(pOptions, "jshell");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(detailPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbConfiguration, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cbConfiguration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(detailPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbConfiguration;
    private javax.swing.JPanel detailPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pOptions;
    // End of variables declaration//GEN-END:variables
}
