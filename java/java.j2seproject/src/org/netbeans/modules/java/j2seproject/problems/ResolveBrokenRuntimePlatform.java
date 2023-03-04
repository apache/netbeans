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

package org.netbeans.modules.java.j2seproject.problems;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.j2seproject.api.J2SERuntimePlatformProvider;
import org.openide.modules.SpecificationVersion;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Union2;

/**
 *
 * @author Tomas Zezula
 */
final class ResolveBrokenRuntimePlatform extends javax.swing.JPanel {

    private static enum Type {
        MISSING_PLATFORM,
        INVALID_PLATFORM
    }

    
    private final Type type;
    private final Project prj;
    private final Union2<String,RuntimePlatformProblemsProvider.InvalidPlatformData> data;
    private final ChangeSupport changeSupport;

    /**
     * Creates new form ResolveMissingRuntimePlatform
     */
    private ResolveBrokenRuntimePlatform(
            @NonNull final Type type,
            @NonNull final Project prj,
            @NonNull final Union2<String,RuntimePlatformProblemsProvider.InvalidPlatformData> data) {
        Parameters.notNull("type", type);   //NOI18N
        Parameters.notNull("prj", prj);   //NOI18N
        Parameters.notNull("data", data);   //NOI18N
        this.type = type;
        this.prj = prj;
        this.data = data;
        this.changeSupport = new ChangeSupport(this);
        initComponents();
        platforms.setRenderer(new PlatformRenderer());
        platforms.setModel(new DefaultComboBoxModel<JavaPlatform>());
        updatePlatforms();
        final ActionListener specificPlatformListener = new ActionListener() {
            @Override
            public void actionPerformed(@NullAllowed final ActionEvent e) {
                platforms.setEnabled(specificPlatform.isSelected());
                create.setEnabled(specificPlatform.isSelected());
                sourceLevelWarning.setEnabled(sourceLevel.isSelected());
                changeSupport.fireChange();
            }
        };
        specificPlatform.addActionListener(specificPlatformListener);
        projectPlatform.addActionListener(specificPlatformListener);
        sourceLevel.addActionListener(specificPlatformListener);
        specificPlatformListener.actionPerformed(null);
        projectPlatform.setSelected(true);
        if (type == Type.MISSING_PLATFORM) {
            sourceLevel.setVisible(false);
            sourceLevelWarning.setVisible(false);
        }
    }

    public void addChangeListener(@NonNull final ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(@NonNull final ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    boolean hasValidData() {
        return projectPlatform.isSelected() ||
           sourceLevel.isSelected() ||
           (specificPlatform.isSelected() && platforms.getSelectedItem() != null);
    }

    boolean isProjectPlatform() {
        return projectPlatform.isSelected();
    }

    boolean isSpecificPlatform() {
        return specificPlatform.isSelected();
    }

    boolean isDowngradeSourceLevel() {
        return sourceLevel.isSelected();
    }

    @NonNull
    JavaPlatform getRuntimePlatform() {
        return (JavaPlatform) platforms.getSelectedItem();
    }


    private void updatePlatforms() {
        final SourceLevelQuery.Result slqr = SourceLevelQuery.getSourceLevel2(prj.getProjectDirectory());
        final String sl = slqr.getSourceLevel();
        final SourceLevelQuery.Profile profile = slqr.getProfile();
        final DefaultComboBoxModel<Object> model = (DefaultComboBoxModel<Object>) platforms.getModel();
        model.removeAllElements();
        for (J2SERuntimePlatformProvider pp : prj.getLookup().lookupAll(J2SERuntimePlatformProvider.class)) {
            for (JavaPlatform jp : pp.getPlatformType(new SpecificationVersion(sl), profile)) {
                model.addElement(jp);
            }
        }
    }

    @NonNull
    private static String getMessage(
        @NonNull final Type type,
        @NonNull final Project project,
        @NonNull final Union2<String,RuntimePlatformProblemsProvider.InvalidPlatformData> data) {
        switch (type) {
            case MISSING_PLATFORM:
                return NbBundle.getMessage(
                    ResolveBrokenRuntimePlatform.class,
                    "LBL_ResolveMissingRuntimePlatform",
                    data.first());
            case INVALID_PLATFORM:
                return NbBundle.getMessage(
                    ResolveBrokenRuntimePlatform.class,
                    "LBL_ResolveInvalidRuntimePlatform",
                    ProjectUtils.getInformation(project).getDisplayName(),
                    data.second().getTargetLevel(),
                    data.second().getProfile().getDisplayName(),
                    data.second().getJavaPlatform().getDisplayName(),
                    data.second().getJavaPlatform().getSpecification().getVersion(),
                    RuntimePlatformProblemsProvider.getPlatformProfile(data.second().getJavaPlatform()).getDisplayName());
            default:
                throw new IllegalArgumentException(String.valueOf(type));
        }
    }

    @NonNull
    private static String getPlatformSourceLevelMessage(@NonNull final JavaPlatform jp) {
        return NbBundle.getMessage(
            ResolveBrokenRuntimePlatform.class,
            "LBL_DowngradeSourceLevel",
            jp.getSpecification().getVersion(),
            RuntimePlatformProblemsProvider.getPlatformProfile(jp).getDisplayName());
    }    


    static ResolveBrokenRuntimePlatform createMissingPlatform(
            @NonNull final Project project,
            @NonNull final String platformId) {
        return new ResolveBrokenRuntimePlatform(
                Type.MISSING_PLATFORM,
                project,
                Union2.<String, RuntimePlatformProblemsProvider.InvalidPlatformData>createFirst(platformId));
    }

    static ResolveBrokenRuntimePlatform createInvalidPlatform(
            @NonNull final Project project,
            @NonNull final RuntimePlatformProblemsProvider.InvalidPlatformData data) {
        return new ResolveBrokenRuntimePlatform(
            Type.INVALID_PLATFORM,
            project,
            Union2.<String, RuntimePlatformProblemsProvider.InvalidPlatformData>createSecond(data));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        actions = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        projectPlatform = new javax.swing.JRadioButton();
        specificPlatform = new javax.swing.JRadioButton();
        platforms = new javax.swing.JComboBox();
        create = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        sourceLevel = new javax.swing.JRadioButton();
        sourceLevelWarning = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, getMessage(this.type, prj, data));

        actions.add(projectPlatform);
        org.openide.awt.Mnemonics.setLocalizedText(projectPlatform, org.openide.util.NbBundle.getMessage(ResolveBrokenRuntimePlatform.class, "ResolveBrokenRuntimePlatform.projectPlatform.text")); // NOI18N

        actions.add(specificPlatform);
        org.openide.awt.Mnemonics.setLocalizedText(specificPlatform, org.openide.util.NbBundle.getMessage(ResolveBrokenRuntimePlatform.class, "ResolveBrokenRuntimePlatform.specificPlatform.text")); // NOI18N

        platforms.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(create, org.openide.util.NbBundle.getMessage(ResolveBrokenRuntimePlatform.class, "ResolveBrokenRuntimePlatform.create.text")); // NOI18N
        create.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                create(evt);
            }
        });

        jLabel2.setLabelFor(platforms);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ResolveBrokenRuntimePlatform.class, "ResolveBrokenRuntimePlatform.jLabel2.text")); // NOI18N

        actions.add(sourceLevel);
        sourceLevel.setMnemonic(org.openide.util.NbBundle.getMessage(ResolveBrokenRuntimePlatform.class, "MNE_DowngradeSourceLevel").charAt(0));
        sourceLevel.setText(type == Type.MISSING_PLATFORM ? "" : getPlatformSourceLevelMessage(data.second().getJavaPlatform()));

        org.openide.awt.Mnemonics.setLocalizedText(sourceLevelWarning, org.openide.util.NbBundle.getMessage(ResolveBrokenRuntimePlatform.class, "ResolveBrokenRuntimePlatform.sourceLevelWarning.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(sourceLevelWarning)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sourceLevel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(projectPlatform, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(specificPlatform, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(platforms, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(create))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(projectPlatform)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(specificPlatform)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(platforms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(create)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourceLevel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourceLevelWarning)
                .addContainerGap(24, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void create(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_create
        final JavaPlatform jp = (JavaPlatform) platforms.getSelectedItem();
        PlatformsCustomizer.showCustomizer(jp);
        updatePlatforms();
    }//GEN-LAST:event_create


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup actions;
    private javax.swing.JButton create;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox platforms;
    private javax.swing.JRadioButton projectPlatform;
    private javax.swing.JRadioButton sourceLevel;
    private javax.swing.JLabel sourceLevelWarning;
    private javax.swing.JRadioButton specificPlatform;
    // End of variables declaration//GEN-END:variables

    private static final class PlatformRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof JavaPlatform) {
                value = ((JavaPlatform)value).getDisplayName();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }        
    }
}
