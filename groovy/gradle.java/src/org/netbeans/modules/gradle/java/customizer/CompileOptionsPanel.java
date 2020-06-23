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

package org.netbeans.modules.gradle.java.customizer;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import org.netbeans.api.java.platform.*;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;

/**
 *
 * @author Laszlo Kishalmi
 */
public class CompileOptionsPanel extends javax.swing.JPanel {

    private static final Logger LOG = Logger.getLogger(CompileOptionsPanel.class.getName());
    public static final String HINT_JDK_PLATFORM = "netbeans.hint.jdkPlatform"; //NOI18N
    private static final String PROP_PLATFORM_ID = "platform.ant.name"; //NOI18N

    final Project project;
    private final ActionListener storeListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            save();
        }
    };

    /**
     * Creates new form CompileOptionsPanel
     */
    public CompileOptionsPanel() {
        this(null);
    }

    private CompileOptionsPanel(Project project) {
        this.project = project;
        initComponents();
        setupCheckBox(cbCompileOnSave, RunUtils.PROP_COMPILE_ON_SAVE, false);
        setupCheckBox(cbAugmentedBuild, RunUtils.PROP_AUGMENTED_BUILD, true);
        setupCheckBox(cbIncludeOpenProjects, RunUtils.PROP_INCLUDE_OPEN_PROJECTS, false);
        setupPlatform();
    }

    @Messages({
        "# {0} - the name of the setting property",
        "COMPILE_DISABLED_HINT=<html>This option is currently specificly controlled"
        + " through your Gradle project (most likely through "
        + "<b>gradle.properties</b>) by <br/> <b>netbeans.{0}</b> property."
    })
    private void setupCheckBox(JCheckBox check, String property, boolean defaultValue) {
        GradleBaseProject gbp = project != null ? GradleBaseProject.get(project) : null;
        if (gbp != null) {
            if (gbp.getNetBeansProperty(property) != null) {
                check.setEnabled(false);
                check.setSelected(Boolean.parseBoolean(gbp.getNetBeansProperty(property)));
                check.setToolTipText(Bundle.COMPILE_DISABLED_HINT(property));
            } else {
                Preferences prefs = NbGradleProject.getPreferences(project, false);
                check.setSelected(prefs.getBoolean(property, defaultValue));
            }
        }
    }

    private void setupPlatform() {

        GradleBaseProject gbp = project != null ? GradleBaseProject.get(project) : null;
        if (gbp != null) {
            String platformId = gbp.getNetBeansProperty(RunUtils.PROP_JDK_PLATFORM);
            if (platformId != null) {
                cbPlatform.setEnabled(false);
                cbPlatform.setToolTipText(Bundle.COMPILE_DISABLED_HINT(RunUtils.PROP_JDK_PLATFORM));
                lbPlatform.setEnabled(false);
                lbPlatform.setToolTipText(Bundle.COMPILE_DISABLED_HINT(RunUtils.PROP_JDK_PLATFORM));
            } else {
               Preferences prefs = NbGradleProject.getPreferences(project, false);
               platformId = prefs.get(RunUtils.PROP_JDK_PLATFORM, null);
            }

            JavaPlatform sel = RunUtils.getActivePlatform(platformId).second();

            PlatformsModel model = new PlatformsModel();
            model.setSelectedItem(sel != null ? sel : platformId);
            cbPlatform.setModel(model);
            cbPlatform.setRenderer(new PlatformsRenderer());
        } 
    }
    
    private void saveCheckBox(JCheckBox check, String property) {
        GradleBaseProject gbp = project != null ? GradleBaseProject.get(project) : null;
        if ((gbp != null) && (gbp.getNetBeansProperty(property) == null)) {
            Preferences prefs = NbGradleProject.getPreferences(project, false);
            prefs.putBoolean(property, check.isSelected());
        }
    }

    private void savePlatform() {
        GradleBaseProject gbp = project != null ? GradleBaseProject.get(project) : null;
        if ((gbp != null) && (gbp.getNetBeansProperty(RunUtils.PROP_JDK_PLATFORM) == null)) {
            Preferences prefs = NbGradleProject.getPreferences(project, false);
            Object sel = cbPlatform.getModel().getSelectedItem();
            if (sel != null) {
                String platformId = sel instanceof JavaPlatform ? ((JavaPlatform)sel).getProperties().get(PROP_PLATFORM_ID) : sel.toString();
                prefs.put(RunUtils.PROP_JDK_PLATFORM, platformId);
            }
        }
        
    }
    
    private void save() {
        saveCheckBox(cbCompileOnSave, RunUtils.PROP_COMPILE_ON_SAVE);
        saveCheckBox(cbAugmentedBuild, RunUtils.PROP_AUGMENTED_BUILD);
        saveCheckBox(cbIncludeOpenProjects, RunUtils.PROP_INCLUDE_OPEN_PROJECTS);
        savePlatform();
        try {
            NbGradleProject.getPreferences(project, false).flush();
        } catch (BackingStoreException ex) {}
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbCompileOnSave = new javax.swing.JCheckBox();
        lbCompileOnSave = new javax.swing.JLabel();
        cbAugmentedBuild = new javax.swing.JCheckBox();
        lbPlatform = new javax.swing.JLabel();
        cbPlatform = new javax.swing.JComboBox<>();
        btManagePlatforms = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        cbIncludeOpenProjects = new javax.swing.JCheckBox();
        lbIncludeOpenProjects = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(cbCompileOnSave, org.openide.util.NbBundle.getMessage(CompileOptionsPanel.class, "CompileOptionsPanel.cbCompileOnSave.text")); // NOI18N
        cbCompileOnSave.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(lbCompileOnSave, org.openide.util.NbBundle.getMessage(CompileOptionsPanel.class, "CompileOptionsPanel.lbCompileOnSave.text")); // NOI18N
        lbCompileOnSave.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        org.openide.awt.Mnemonics.setLocalizedText(cbAugmentedBuild, org.openide.util.NbBundle.getMessage(CompileOptionsPanel.class, "CompileOptionsPanel.cbAugmentedBuild.text")); // NOI18N

        lbPlatform.setLabelFor(cbPlatform);
        org.openide.awt.Mnemonics.setLocalizedText(lbPlatform, org.openide.util.NbBundle.getMessage(CompileOptionsPanel.class, "CompileOptionsPanel.lbPlatform.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btManagePlatforms, org.openide.util.NbBundle.getMessage(CompileOptionsPanel.class, "CompileOptionsPanel.btManagePlatforms.text")); // NOI18N
        btManagePlatforms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btManagePlatformsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CompileOptionsPanel.class, "CompileOptionsPanel.jLabel2.text")); // NOI18N
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        org.openide.awt.Mnemonics.setLocalizedText(cbIncludeOpenProjects, org.openide.util.NbBundle.getMessage(CompileOptionsPanel.class, "CompileOptionsPanel.cbIncludeOpenProjects.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbIncludeOpenProjects, org.openide.util.NbBundle.getMessage(CompileOptionsPanel.class, "CompileOptionsPanel.lbIncludeOpenProjects.text")); // NOI18N
        lbIncludeOpenProjects.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cbCompileOnSave)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lbPlatform)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbPlatform, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btManagePlatforms))
                            .addComponent(cbAugmentedBuild, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addComponent(lbCompileOnSave, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(lbIncludeOpenProjects, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cbIncludeOpenProjects)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbPlatform)
                    .addComponent(cbPlatform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btManagePlatforms))
                .addGap(18, 18, 18)
                .addComponent(cbCompileOnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbCompileOnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbAugmentedBuild)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cbIncludeOpenProjects)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbIncludeOpenProjects, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(67, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private JavaPlatform getSelPlatform () {
        String platformId = project.getLookup().lookup(AuxiliaryProperties.class).
                get(HINT_JDK_PLATFORM, true);
        return RunUtils.getActivePlatform(platformId).second();
    }
    
    
    private void btManagePlatformsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btManagePlatformsActionPerformed
        PlatformsCustomizer.showCustomizer(getSelPlatform());
    }//GEN-LAST:event_btManagePlatformsActionPerformed

    @NbBundle.Messages("category.BuildCompile=Compile")
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = NbGradleProject.GRADLE_PROJECT_TYPE,
            category = "build/compile",
            categoryLabel = "#category.BuildCompile",
            position = 300)
    public static ProjectCustomizer.CompositeCategoryProvider buildCompileCustomizerProvider() {
        return new ProjectCustomizer.CompositeCategoryProvider() {
            @Override
            public ProjectCustomizer.Category createCategory(Lookup context) {
                return null;
            }

            @Override
            public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
                Project project = context.lookup(Project.class);
                CompileOptionsPanel customizer = new CompileOptionsPanel(project);
                category.setStoreListener(customizer.storeListener);
                return customizer;
            }
        };
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btManagePlatforms;
    private javax.swing.JCheckBox cbAugmentedBuild;
    private javax.swing.JCheckBox cbCompileOnSave;
    private javax.swing.JCheckBox cbIncludeOpenProjects;
    private javax.swing.JComboBox<JavaPlatform> cbPlatform;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lbCompileOnSave;
    private javax.swing.JLabel lbIncludeOpenProjects;
    private javax.swing.JLabel lbPlatform;
    // End of variables declaration//GEN-END:variables

    private static class PlatformsModel extends AbstractListModel<JavaPlatform> implements ComboBoxModel<JavaPlatform>, PropertyChangeListener {

        private JavaPlatform[] data;
        private Object sel;

        @SuppressWarnings("LeakingThisInConstructor")
        public PlatformsModel() {
            JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            getPlatforms(jpm);
            jpm.addPropertyChangeListener(WeakListeners.propertyChange(this, jpm));
        }

        @Override
        public int getSize() {
            return data.length;
        }

        @Override
        public JavaPlatform getElementAt(int index) {
            return data[index];
        }

        @Override
        public void setSelectedItem(Object anItem) {
            sel = anItem;
            fireContentsChanged(this, 0, data.length);
        }

        @Override
        public Object getSelectedItem() {
            return sel;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String current = sel instanceof JavaPlatform ? ((JavaPlatform)sel).getProperties().get(PROP_PLATFORM_ID):sel.toString();
            JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            getPlatforms(jpm);
            JavaPlatform found = null;
            for (int i = 0; i < data.length; i++) {
                JavaPlatform pf = data[i];
                if (current.equals(pf.getProperties().get(PROP_PLATFORM_ID))) {
                    found = pf;
                    break;
                }
            }
            setSelectedItem(found != null ? found : current);
        }

        private void getPlatforms(JavaPlatformManager jpm) {
            data = jpm.getPlatforms(null, new Specification ("j2se", null)); //NOI18N
            if(LOG.isLoggable(Level.FINE)) {
                for (JavaPlatform jp : data) {
                    LOG.log(Level.FINE, "Adding JavaPlaform: {0}", jp.getDisplayName());
                }
            }
        }

    }

    private class PlatformsRenderer extends JLabel implements ListCellRenderer, UIResource {

        @SuppressWarnings("OverridableMethodCallInConstructor")
        public PlatformsRenderer() {
            setOpaque(true);
        }

        @Override
        @NbBundle.Messages({"# {0} - platformId", "LBL_MissingPlatform=Missing platform: {0}"})
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            if (value instanceof JavaPlatform) {
                JavaPlatform jp = (JavaPlatform)value;
                setText(jp.getDisplayName());
                if ( isSelected ) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
            } else {
                if (value == null) {
                    setText("");
                } else {
                    setText(Bundle.LBL_MissingPlatform(value));
                    setForeground(UIManager.getColor("nb.errorForeground")); //NOI18N
                }
            }
            return this;
        }

        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    } // end of PlatformsRenderer
    
}
