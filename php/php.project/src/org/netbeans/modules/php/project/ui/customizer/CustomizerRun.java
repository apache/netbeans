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
package org.netbeans.modules.php.project.ui.customizer;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.UIResource;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.ConfigManager.Configuration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author  Radek Matous
 */
public class CustomizerRun extends JPanel implements HelpCtx.Provider {
    private static final long serialVersionUID = -5494488817914071L;
    private final ConfigComboBoxModel comboModel;
    private final ConfigManager manager;
    private final RunAsPanel.InsidePanel[] insidePanels;

    public CustomizerRun(PhpProjectProperties properties, final Category category) {
        manager = properties.getConfigManager();
        insidePanels = new RunAsPanel.InsidePanel[] {
            new RunAsLocalWeb(properties, manager, category),
            new RunAsRemoteWeb(properties, manager, category),
            new RunAsScript(properties.getProject(), manager, category),
            new RunAsInternalServer(properties, manager, category),
        };
        initComponents();
        comboModel = new ConfigComboBoxModel();
        configCombo.setModel(comboModel);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        configCombo.setSelectedItem(manager.currentConfiguration().getName());
    }

    private Configuration configurationFor(String configName) {
        return manager.configurationFor(configName);
    }

    private void selectCurrentItem() {
        final Configuration config = manager.currentConfiguration();
        configCombo.setSelectedItem(config.getName());
        configDel.setEnabled(!config.isDefault());
    }

    private class ConfigComboBoxModel extends DefaultComboBoxModel<String> {

        private static final long serialVersionUID = -65849874546546871L;


        public ConfigComboBoxModel() {
            Set<String> alphaConfigs = new TreeSet<>(getComparator());
            alphaConfigs.addAll(manager.configurationNames());
            for (String config : alphaConfigs) {
                this.addElement(config);
            }
        }

        private Comparator<String> getComparator() {
            return new Comparator<String>() {
                Collator coll = Collator.getInstance();

                @Override
                public int compare(String s1, String s2) {
                    String lbl1 = configurationFor(s1).getDisplayName();
                    String lbl2 = configurationFor(s2).getDisplayName();
                    return coll.compare(lbl1, lbl2);
                }
            };
        }
    }

    private final class ConfigListCellRenderer extends JLabel implements ListCellRenderer<String>, UIResource {

        private static final long serialVersionUID = 165786424165431675L;


        public ConfigListCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            String config = value;
            //String label = (config != null) ? configurationFor(config).getDisplayName() : null;
            String label = configurationFor(config).getDisplayName();
            setText(label);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }

        // #93658: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N

        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configLabel = new JLabel();
        configCombo = new JComboBox<String>();
        configNew = new JButton();
        configDel = new JButton();
        separator = new JSeparator();
        runPanel = new RunAsPanel(insidePanels);

        configLabel.setLabelFor(configCombo);
        Mnemonics.setLocalizedText(configLabel, NbBundle.getMessage(CustomizerRun.class, "LBL_Configuration")); // NOI18N

        configCombo.setRenderer(new ConfigListCellRenderer());
        configCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configComboActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(configNew, NbBundle.getMessage(CustomizerRun.class, "LBL_New")); // NOI18N
        configNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configNewActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(configDel, NbBundle.getMessage(CustomizerRun.class, "LBL_Delete")); // NOI18N
        configDel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configDelActionPerformed(evt);
            }
        });

        runPanel.setLayout(new CardLayout());

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(runPanel, GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
            .addComponent(separator, GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(configLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(configCombo, 0, 1, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(configNew)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(configDel))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(configLabel)
                    .addComponent(configCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(configNew)
                    .addComponent(configDel))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(separator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(runPanel, GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                .addContainerGap())
        );

        configLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configLabel.AccessibleContext.accessibleName")); // NOI18N
        configLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configLabel.AccessibleContext.accessibleDescription")); // NOI18N
        configCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configCombo.AccessibleContext.accessibleName")); // NOI18N
        configCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configCombo.AccessibleContext.accessibleDescription")); // NOI18N
        configNew.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configNew.AccessibleContext.accessibleName")); // NOI18N
        configNew.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configNew.AccessibleContext.accessibleDescription")); // NOI18N
        configDel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configDel.AccessibleContext.accessibleName")); // NOI18N
        configDel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configDel.AccessibleContext.accessibleDescription")); // NOI18N
        separator.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.separator.AccessibleContext.accessibleName")); // NOI18N
        separator.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.separator.AccessibleContext.accessibleDescription")); // NOI18N
        runPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.runPanel.AccessibleContext.accessibleName")); // NOI18N
        runPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.runPanel.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void configComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configComboActionPerformed
        String config = (String) configCombo.getSelectedItem();
        manager.markAsCurrentConfiguration(config == null || config.length() == 0 ? null : config);
        selectCurrentItem();
    }//GEN-LAST:event_configComboActionPerformed

    private void configNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configNewActionPerformed
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(CustomizerRun.class, "LBL_ConfigurationName"),
                NbBundle.getMessage(CustomizerRun.class, "LBL_CreateNewConfiguration"));

        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
            String name = d.getInputText();
            if (name.trim().length() == 0) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(CustomizerRun.class, "MSG_ConfigurationNameBlank"),
                        NotifyDescriptor.WARNING_MESSAGE));
                return;
            }
            String config = name.replaceAll("[^a-zA-Z0-9_.-]", "_"); // NOI18N

            if (manager.exists(config)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(CustomizerRun.class, "MSG_ConfigurationExists", config),
                        NotifyDescriptor.WARNING_MESSAGE));
                return;
            }
            manager.createNew(config, name);
            comboModel.addElement(config);
            manager.markAsCurrentConfiguration(config);
            selectCurrentItem();
        }
    }//GEN-LAST:event_configNewActionPerformed

    private void configDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configDelActionPerformed
        String config = (String) configCombo.getSelectedItem();
        assert config != null;
        comboModel.removeElement(config);
        configurationFor(config).delete();
        selectCurrentItem();
    }//GEN-LAST:event_configDelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox<String> configCombo;
    private JButton configDel;
    private JLabel configLabel;
    private JButton configNew;
    private JPanel runPanel;
    private JSeparator separator;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.project.ui.customizer.CustomizerRun"); // NOI18N
    }

}
