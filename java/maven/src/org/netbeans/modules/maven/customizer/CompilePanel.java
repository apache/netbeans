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

package org.netbeans.modules.maven.customizer;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.UIResource;
import javax.xml.namespace.QName;
import org.apache.maven.model.InputLocation;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.customizer.support.CheckBoxUpdater;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.maven.classpath.BootClassPathImpl;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.options.DontShowAgainSettings;
import org.netbeans.modules.maven.options.MavenVersionSettings;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Union2;
import org.openide.util.WeakListeners;

import static org.netbeans.modules.maven.api.Constants.GROUP_APACHE_PLUGINS;
import static org.netbeans.modules.maven.api.Constants.HINT_COMPILE_ON_SAVE;
import static org.netbeans.modules.maven.api.Constants.HINT_JDK_PLATFORM;
import static org.netbeans.modules.maven.api.Constants.PLUGIN_COMPILER;
import static org.netbeans.modules.maven.api.Constants.SOURCE_PARAM;

/**
 *
 * @author mkleint
 */
public class CompilePanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private static final Logger LOG = Logger.getLogger(CompilePanel.class.getName());

    private final ModelHandle2 handle;
    private final Project project;
    private static boolean warningShown = false;
    
    public CompilePanel(ModelHandle2 handle, Project prj, MavenProjectPropertiesUiSupport uiSupport) {
        initComponents();
        this.handle = handle;
        project = prj;
        comJavaPlatform.setModel(uiSupport.getPlatformComboBoxModel());
        comJavaPlatform.setRenderer(new PlatformsRenderer());

        comSourceLevel.setEditable(false);
        comSourceLevel.setModel(uiSupport.getSourceLevelComboBoxModel());
        
        initValues();
    }
    
    private void initValues() {

        String sourceLevel = SourceLevelQuery.getSourceLevel(project.getProjectDirectory());
        // --release supports only integers, versions >8 are already normalized
        if ("1.8".equals(sourceLevel)) {
            sourceLevel = "8";
        }
        comSourceLevel.setSelectedItem(sourceLevel);
        // TODO use ComboBoxUpdater to boldface the label when not an inherited default
        comSourceLevel.addActionListener(new ActionListener() {
            private ModelOperation<POMModel> operation;
            @Override
            public void actionPerformed(ActionEvent ae) {
                handle.removePOMModification(operation);
                String sourceLevel = (String)comSourceLevel.getSelectedItem();
                String source = PluginPropertyUtils.getPluginProperty(
                        handle.getProject(), GROUP_APACHE_PLUGINS, PLUGIN_COMPILER, SOURCE_PARAM, "compile", null); //NOI18N
                if (source != null && source./*XXX not equals?*/contains(sourceLevel)) {
                    return;
                }
                operation = new SourceLevelOperation(handle, sourceLevel);
                handle.addPOMModification(operation);
            }
        });

        boolean cosSupported = BuildArtifactMapper.isCompileOnSaveSupported();
        if (!cosSupported) {
            cbCompileOnSave.setEnabled(false);
        }
        new CheckBoxUpdater(cbCompileOnSave) {
            private String modifiedValue;
            private ModelOperation<POMModel> operation;

            @Override
            public boolean getDefaultValue() {
                return false; // see org.netbeans.modules.maven.api.execute.RunUtils#isCompileOnSaveEnabled
            }

            @Override
            public Boolean getValue() {
                if (!cosSupported) {
                    return false;
                }
                String val = modifiedValue;
                if (val == null) {
                    val = handle.getRawAuxiliaryProperty(HINT_COMPILE_ON_SAVE, true);
                }
                if (val == null) {
                    java.util.Properties props = handle.getProject().getProperties();
                    if (props != null) {
                        val = props.getProperty(HINT_COMPILE_ON_SAVE);
                    }
                }             
                return "all".equals(val) ? true : null;
            }

            @Override
            public void setValue(Boolean v) {
                handle.removePOMModification(operation);
                modifiedValue = null;
                boolean cosEnabled = v != null ? v : getDefaultValue();
                String value = cosEnabled ? "all" : "none";
                if ("all".equals(value)) {
                    if (!warningShown && DontShowAgainSettings.getDefault().showWarningAboutApplicationCoS()) {
                        EventQueue.invokeLater(() -> {
                            WarnPanel panel = new WarnPanel(NbBundle.getMessage(CompilePanel.class, "HINT_ApplicationCoS"));
                            NotifyDescriptor dd = new NotifyDescriptor.Message(panel, NotifyDescriptor.PLAIN_MESSAGE);
                            DialogDisplayer.getDefault().notify(dd);
                            if (panel.disabledWarning()) {
                                DontShowAgainSettings.getDefault().dontshowWarningAboutApplicationCoSAnymore();
                            }
                        });
                        warningShown = true;
                    }
                }

                boolean hasConfig = handle.getRawAuxiliaryProperty(HINT_COMPILE_ON_SAVE, true) != null;
                org.netbeans.modules.maven.model.pom.Project p = handle.getPOMModel().getProject();
                if (p.getProperties() != null && p.getProperties().getProperty(HINT_COMPILE_ON_SAVE) != null) {
                    modifiedValue = value;
                    operation = new ModelPropertyOperation(HINT_COMPILE_ON_SAVE, modifiedValue);
                    handle.addPOMModification(operation);
                    if (hasConfig) {
                        // in this case clean up the auxiliary config
                        handle.setRawAuxiliaryProperty(HINT_COMPILE_ON_SAVE, null, true);
                    }
                } else {
                    handle.setRawAuxiliaryProperty(HINT_COMPILE_ON_SAVE, value, true);
                }
            }
        };

        new CheckBoxUpdater(cbDebug) {
            private static final String PARAM_DEBUG = "debug";
            @Override
            public Boolean getValue() {
                String val = getCompilerParam(handle, PARAM_DEBUG);
                if (val != null && Boolean.parseBoolean(val) != getDefaultValue()) {
                    return Boolean.valueOf(val);
                }
                return null;
            }

            @Override
            public void setValue(Boolean value) {
                String text;
                if (value == null) {
                    //TODO we should attempt to remove the configuration
                    // from pom if this parameter is the only one defined.
                    text = String.valueOf(getDefaultValue());
                } else {
                    text = value.toString();
                }
                modifyCompilerParamOperation(handle, PARAM_DEBUG, text, String.valueOf(getDefaultValue()));
            }

            @Override
            public boolean getDefaultValue() {
                return true;
            }
        };

        new CheckBoxUpdater(cbLint) {
            private final boolean currentlyEnabled;
            private final boolean notInParentPom;
            private ModelOperation<POMModel> operation;
            {
                CompilerArgsQueryResult result = CompilerArgsQuery.getCompilerArgs(handle);
                if (result != null && isLintEnabled(result.args)) { 
                    currentlyEnabled = true;
                    notInParentPom = getModelIdWithoutPackaging(handle).equals(result.modelId);
                } else {
                    currentlyEnabled = false;
                    notInParentPom = false;
                }
            }

            @Override
            public Boolean getValue() {
                return currentlyEnabled && notInParentPom ? true : null;
            }

            @Override
            public boolean getDefaultValue() {
                return currentlyEnabled;
            }

            private static String getModelIdWithoutPackaging(ModelHandle2 handle) {
                MavenProject project = handle.getProject();
                String modelId = project.getModel().getId();
                String packaging = project.getModel().getPackaging();
                if (packaging != null && !packaging.isEmpty()) {
                    modelId = modelId.replace(":"+packaging+":", ":");
                }
                return modelId;
            }

            // returns true when all or most lint categories are enabled
            private static boolean isLintEnabled(List<String> args) {
                // -Xlint and -Xlint:all are equivalent as of JDK 23 (Lint.java)
                if (args.contains("-Xlint")) {
                    return true;
                }
                for (String arg : args) {
                    if (arg.startsWith("-Xlint:") && List.of(arg.substring(7).split(",")).contains("all")) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void setValue(Boolean value) {
                if (operation != null) {
                    handle.removePOMModification(operation);
                }
                if (value == null) {
                    return;
                }
                operation = new CompilerLintArgOperation(value);
                handle.addPOMModification(operation);
            }
        };

        // java platform updater
        ComboBoxUpdater<Union2<JavaPlatform, String>> platformComboBoxUpdater = new ComboBoxUpdater<>(comJavaPlatform, lblJavaPlatform) {
            private String modifiedValue;
            private static final String DEFAULT_PLATFORM_VALUE = "@@DEFAULT@@";
            private ModelOperation<POMModel> operation;
            
            @Override
            public Union2<JavaPlatform, String> getValue() {
                String val = modifiedValue;
                if (val == null) {
                    Properties props = handle.getPOMModel().getProject().getProperties();
                    if (props != null) {
                        val = props.getProperty(HINT_JDK_PLATFORM);
                    }
                }
                if (val == null) {
                    val = handle.getRawAuxiliaryProperty(HINT_JDK_PLATFORM, true);
                }
                if (val != null) {
                    if (val.equals(DEFAULT_PLATFORM_VALUE)) {
                        return Union2.createFirst(JavaPlatformManager.getDefault().getDefaultPlatform());
                    }
                    return Optional.ofNullable(BootClassPathImpl.getActivePlatform(val))
                            .filter(JavaPlatform::isValid)
                            .map((jp) -> Union2.<JavaPlatform,String>createFirst(jp))
                            .orElse(Union2.createSecond(val));
                } else {
                    final Pair<String,JavaPlatform> nameJpP = getSelPlatform();
                    return Optional.ofNullable(nameJpP.second())
                            .filter(JavaPlatform::isValid)
                            .map((jp) -> Union2.<JavaPlatform,String>createFirst(jp))
                            .orElseGet(() -> Union2.<JavaPlatform,String>createSecond(nameJpP.first()));
                }
            }

            @Override
            public Union2<JavaPlatform, String> getDefaultValue() {
                return Union2.createFirst(JavaPlatformManager.getDefault().getDefaultPlatform());
            }

            @Override
            public void setValue(Union2<JavaPlatform, String> value) {
                handle.removePOMModification(operation);
                modifiedValue = null;
                final Union2<JavaPlatform, String> platf = value == null ?
                        Union2.createFirst(JavaPlatformManager.getDefault().getDefaultPlatform()) :
                        value;
                final String platformId;
                if (platf.hasFirst()) {
                    final JavaPlatform jp = platf.first();
                    platformId = JavaPlatformManager.getDefault().getDefaultPlatform().equals(jp) ?
                            null :
                            jp.getProperties().get("platform.ant.name"); //NOI18N
                } else {
                    platformId = platf.second();
                }

                boolean hasConfig = handle.getRawAuxiliaryProperty(HINT_JDK_PLATFORM, true) != null;
                //TODO also try to take the value in pom vs inherited pom value into account.
                modifiedValue = platformId == null ? DEFAULT_PLATFORM_VALUE : platformId;
                if (handle.getProject().getProperties().containsKey(HINT_JDK_PLATFORM)) {
                    operation = new ModelPropertyOperation(HINT_JDK_PLATFORM, modifiedValue);
                    handle.addPOMModification(operation);
                    if (hasConfig) {
                        // in this case clean up the auxiliary config
                        handle.setRawAuxiliaryProperty(HINT_JDK_PLATFORM, null, true);
                    }
                } else {
                    handle.setRawAuxiliaryProperty(HINT_JDK_PLATFORM, platformId, true);
                }
            }
        };
        // the selected item is not set until the compile panel is shown
        // so, invoke these methods for setting it
        platformComboBoxUpdater.ancestorAdded(null);
        platformComboBoxUpdater.ancestorRemoved(null);
    }

    private Pair<String,JavaPlatform> getSelPlatform () {
        String platformId = project.getLookup().lookup(AuxiliaryProperties.class).get(HINT_JDK_PLATFORM, true);
        return Pair.of(platformId,BootClassPathImpl.getActivePlatform(platformId));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel idePanel = new javax.swing.JPanel();
        cbCompileOnSave = new javax.swing.JCheckBox();
        lblHint1 = new javax.swing.JLabel();
        lblHint2 = new javax.swing.JLabel();
        javax.swing.JPanel compilerPanel = new javax.swing.JPanel();
        cbDebug = new javax.swing.JCheckBox();
        cbLint = new javax.swing.JCheckBox();
        javax.swing.JPanel jdkPanel = new javax.swing.JPanel();
        lblJavaPlatform = new javax.swing.JLabel();
        lblJavaPlatform1 = new javax.swing.JLabel();
        comJavaPlatform = new javax.swing.JComboBox();
        comSourceLevel = new javax.swing.JComboBox<>();
        btnMngPlatform = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(576, 303));

        idePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.idePanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbCompileOnSave, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.cbCompileOnSave.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint1, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblHint1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint2, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblHint2.text")); // NOI18N

        javax.swing.GroupLayout idePanelLayout = new javax.swing.GroupLayout(idePanel);
        idePanel.setLayout(idePanelLayout);
        idePanelLayout.setHorizontalGroup(
            idePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(idePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(idePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, idePanelLayout.createSequentialGroup()
                        .addComponent(cbCompileOnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(66, 66, 66))
                    .addComponent(lblHint2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
                    .addGroup(idePanelLayout.createSequentialGroup()
                        .addComponent(lblHint1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        idePanelLayout.setVerticalGroup(
            idePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(idePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbCompileOnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblHint1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblHint2, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        compilerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.compilerPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbDebug, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.cbDebug.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbLint, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.cbLint.text")); // NOI18N

        javax.swing.GroupLayout compilerPanelLayout = new javax.swing.GroupLayout(compilerPanel);
        compilerPanel.setLayout(compilerPanelLayout);
        compilerPanelLayout.setHorizontalGroup(
            compilerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(compilerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(compilerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbDebug)
                    .addComponent(cbLint))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        compilerPanelLayout.setVerticalGroup(
            compilerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(compilerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbDebug)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbLint)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jdkPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.jdkPanel.border.title"))); // NOI18N

        lblJavaPlatform.setLabelFor(comJavaPlatform);
        org.openide.awt.Mnemonics.setLocalizedText(lblJavaPlatform, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblJavaPlatform.text")); // NOI18N

        lblJavaPlatform1.setLabelFor(comJavaPlatform);
        org.openide.awt.Mnemonics.setLocalizedText(lblJavaPlatform1, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblJavaPlatform1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnMngPlatform, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.btnMngPlatform.text")); // NOI18N
        btnMngPlatform.addActionListener(this::btnMngPlatformActionPerformed);

        javax.swing.GroupLayout jdkPanelLayout = new javax.swing.GroupLayout(jdkPanel);
        jdkPanel.setLayout(jdkPanelLayout);
        jdkPanelLayout.setHorizontalGroup(
            jdkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdkPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jdkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblJavaPlatform)
                    .addComponent(lblJavaPlatform1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jdkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(comSourceLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jdkPanelLayout.createSequentialGroup()
                        .addComponent(comJavaPlatform, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMngPlatform)))
                .addContainerGap())
        );
        jdkPanelLayout.setVerticalGroup(
            jdkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jdkPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jdkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblJavaPlatform)
                    .addComponent(comJavaPlatform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMngPlatform))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jdkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblJavaPlatform1)
                    .addComponent(comSourceLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnMngPlatform.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.btnMngPlatform.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(idePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jdkPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(compilerPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jdkPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compilerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(idePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnMngPlatformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMngPlatformActionPerformed
        PlatformsCustomizer.showCustomizer(getSelPlatform().second());
}//GEN-LAST:event_btnMngPlatformActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMngPlatform;
    private javax.swing.JCheckBox cbCompileOnSave;
    private javax.swing.JCheckBox cbDebug;
    private javax.swing.JCheckBox cbLint;
    private javax.swing.JComboBox comJavaPlatform;
    private javax.swing.JComboBox<String> comSourceLevel;
    private javax.swing.JLabel lblHint1;
    private javax.swing.JLabel lblHint2;
    private javax.swing.JLabel lblJavaPlatform;
    private javax.swing.JLabel lblJavaPlatform1;
    // End of variables declaration//GEN-END:variables


    static final class SourceLevelComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {

        private final ComboBoxModel platformComboBoxModel;
        private String selectedSourceLevel;
        private List<Integer> sourceLevelRange;

        private static final long serialVersionUID = 1L;

        public SourceLevelComboBoxModel(ComboBoxModel platformComboBoxModel, String selectedSourceLevel) {
            this.selectedSourceLevel = selectedSourceLevel;
            this.platformComboBoxModel = platformComboBoxModel;
            this.platformComboBoxModel.addListDataListener(new ListDataListener() {
                @Override public void contentsChanged(ListDataEvent e) {
                    resetCache();
                    validateSelection();
                }
                @Override public void intervalAdded(ListDataEvent e) {}
                @Override public void intervalRemoved(ListDataEvent e) {}
            });
        }

        @Override
        public int getSize() {
            return getSourceLevelRange().size();
        }

        @Override
        public String getElementAt(int index) {
            List<Integer> sourceLevels = getSourceLevelRange();
            assert index >= 0 && index < sourceLevels.size();
            return versionToString(sourceLevels.get(index));
        }

        @Override
        public void setSelectedItem(Object obj) {
            selectedSourceLevel = (obj == null ? null : (String) obj);
            fireContentsChanged(this, 0, getSize());
        }

        @Override
        public Object getSelectedItem() {
            return selectedSourceLevel;
        }

        private void resetCache() {
            synchronized (this) {
                sourceLevelRange = null;
            }
            fireContentsChanged(this, 0, getSize());
        }

        public void validateSelection() {
            List<Integer> range = getSourceLevelRange();
            if (selectedSourceLevel != null && !range.isEmpty()) {
                int selected = stringToVersion(selectedSourceLevel);
                if (selected > range.get(0)) {
                    setSelectedItem(versionToString(range.get(0)));
                } else if (selected < range.get(range.size() - 1)) {
                    setSelectedItem(versionToString(range.get(range.size() - 1)));
                }
            }
        }

        // newest first
        private synchronized List<Integer> getSourceLevelRange() {
            if (sourceLevelRange == null) {
                Union2<JavaPlatform, String> union = (Union2<JavaPlatform, String>) platformComboBoxModel.getSelectedItem();
                if (!union.hasFirst()) {
                    return List.of();
                }
                JavaPlatform platform = union.first();
                if (platform != null) {
                    int max = stringToVersion(platform.getSpecification().getVersion().toString());
                    int min = minSourceLevelFor(max);
                    sourceLevelRange = IntStream.range(min, max + 1)
                                                .map(i -> max - i + min) // .reversed()
                                                .boxed().toList();
                } else {
                    sourceLevelRange = List.of();
                }
            }
            return sourceLevelRange;
        }

        private static int minSourceLevelFor(int version) {
            if (version >= 20) return 8;
            if (version >= 14) return 7;
            if (version >= 9)  return 6;
            if (version >= 8)  return 2;
            return 1;
        }

        // 8+ produces integers to be compatible with --release
        private static String versionToString(int version) {
            return version < 8 ? "1." + version : String.valueOf(version);
        }

        private static int stringToVersion(String version) {
            return version.startsWith("1.") ? Integer.parseInt(version.substring(2)) : Integer.parseInt(version);
        }
    }
        
    private final Map<String, CompilerParamOperation> operations = new HashMap<>();

    private void modifyCompilerParamOperation(ModelHandle2 handle, String param, String value, String defaultValue) {
        String current = PluginPropertyUtils.getPluginProperty(
                handle.getProject(), GROUP_APACHE_PLUGINS, PLUGIN_COMPILER, param, "compile", null); //NOI18N
        if ((current != null && current.contains(value)) || (current == null && defaultValue.equals(value))) {
            ModelOperation<POMModel> removed = operations.remove(param);
            if (removed != null) {
                handle.removePOMModification(removed);
            }
            return;
        }
        ModelOperation<POMModel> removed = operations.remove(param);
        if (removed != null) {
            handle.removePOMModification(removed);
        }
        CompilerParamOperation added = new CompilerParamOperation(param, value);
        operations.put(param, added);
        handle.addPOMModification(added);
    }

    private record CompilerParamOperation(String param, String value) implements ModelOperation<POMModel> {
        @Override
        public void performOperation(POMModel model) {
            Configuration config = getOrCreateCompilerConfig(model);
            config.setSimpleParameter(param, value);
        }
    }

    private record ModelPropertyOperation(String key, String value) implements ModelOperation<POMModel> {
        @Override
        public void performOperation(POMModel model) {
            Properties modprops = model.getProject().getProperties();
            if (modprops == null) {
                modprops = model.getFactory().createProperties();
                model.getProject().setProperties(modprops);
            }
            modprops.setProperty(key, value);
        }
    }

    private record SourceLevelOperation(ModelHandle2 handle, String sourceLevel) implements ModelOperation<POMModel> {
        @Override
        public void performOperation(POMModel model) {

            // TODO this should be moved into ModelUtils.setSourceLevel at some point
            // notes:
            // - plugin config wins over properties when both exist
            // - the default parent pom has a compiler plugin config but does not set the lang level via pom
            // this means:
            // - if a custom parent pom sets the lang level via plugin config, this should overwrite it with another plugin config
            // - in all other cases it should set/update the lang level via properties

            org.netbeans.modules.maven.model.pom.Project project = model.getProject();
            if (project != null && project.getProperties() != null) {
                Properties prop = project.getProperties();
                if (   prop.getProperty("maven.compiler.release") != null
                    || prop.getProperty("maven.compiler.source") != null
                    || prop.getProperty("maven.compiler.target") != null) {
                    // update existing properties
                    updateInProperties(prop);
                    return;
                }
            }

            String s = PluginPropertyUtils.getPluginProperty(handle.getProject(), GROUP_APACHE_PLUGINS, PLUGIN_COMPILER, "source", null, null);
            String t = PluginPropertyUtils.getPluginProperty(handle.getProject(), GROUP_APACHE_PLUGINS, PLUGIN_COMPILER, "target", null, null);
            String r = PluginPropertyUtils.getPluginProperty(handle.getProject(), GROUP_APACHE_PLUGINS, PLUGIN_COMPILER, "release", null, null);
            if (s == null && t == null && r == null) {
                // set in project properties (plugin config does not exist)
                if (project != null) {
                    Properties prop = project.getProperties();
                    if (prop == null) {
                        prop = model.getFactory().createProperties();
                        project.setProperties(prop);
                    }
                    updateInProperties(prop);
                }
            } else {
                // set in plugin config
                ModelUtils.setSourceLevel(model, sourceLevel);
                // clear props, just in case
                if (project != null && project.getProperties() != null) {
                    Properties prop = project.getProperties();
                    prop.setProperty("maven.compiler.source", null);
                    prop.setProperty("maven.compiler.target", null);
                    prop.setProperty("maven.compiler.release", null);
                }
            }
        }

        private void updateInProperties(Properties prop) {
            if (prop.getProperty("maven.compiler.release") != null ||
                    (!sourceLevel.contains(".") && prop.getProperty("maven.compiler.source") == null && prop.getProperty("maven.compiler.target") == null)) {
                prop.setProperty("maven.compiler.release", sourceLevel);
                prop.setProperty("maven.compiler.source", null);
                prop.setProperty("maven.compiler.target", null);
            } else {
                prop.setProperty("maven.compiler.source", sourceLevel);
                prop.setProperty("maven.compiler.target", sourceLevel);
            }
        }
    }

    /**
     * Sets or unsets -Xlint in the compilerArgs list.
     */
    private record CompilerLintArgOperation(boolean add) implements ModelOperation<POMModel> {
        @Override
        public void performOperation(POMModel model) {
            Configuration config = getOrCreateCompilerConfig(model);
            POMExtensibilityElement args = getOrCreateElement(model, config, "compilerArgs", null);
            
            boolean updated = false;
            for (POMExtensibilityElement arg : args.getExtensibilityElements()) {
                String text = arg.getElementText();
                if (arg.getQName().getLocalPart().equals("arg") && text != null) {
                    if (text.equals("-Xlint")) {
                        if (!add) {
                            updated |= true;
                            args.removeExtensibilityElement(arg);
                        }
                    } else if (text.startsWith("-Xlint:")) {
                        if (add) {
                            updated |= removeOption("none", arg);
                        } else {
                            updated |= removeOption("all", arg);
                        }
                    }
                }
            }
            
            if (!updated) {
                if (add) {
                    createElement(model, args, "arg", "-Xlint");
                } else {
                    createElement(model, args, "arg", "-Xlint:none");
                }
            }
            
            if (args.getExtensibilityElements().isEmpty()) {
                config.removeExtensibilityElement(args);
            }
            if (config.getExtensibilityElements().isEmpty()) {
                ((Plugin)config.getParent()).setConfiguration(null);
            }
        }

        private static boolean removeOption(String option, POMExtensibilityElement arg) {
            String text = arg.getElementText();
            if (text.equals("-Xlint:" + option)) {
                arg.getParent().removeExtensibilityElement(arg);
                return true;
            } else if (text.contains(option + ",")) {
                arg.setElementText(text.replace(option + ",", ""));
                return true;
            } else if (text.endsWith("," + option)) {
                arg.setElementText(text.substring(0, text.length() - (option.length() + 1)));
                return true;
            }
            return false;
        }
    }

    record CompilerArgsQueryResult(List<String> args, String modelId) {}

    private static class CompilerArgsQuery implements PluginPropertyUtils.ConfigurationBuilder<CompilerArgsQueryResult> {

        private static CompilerArgsQueryResult getCompilerArgs(ModelHandle2 handle) {
            return PluginPropertyUtils.getPluginPropertyBuildable(
                    handle.getProject(), GROUP_APACHE_PLUGINS, PLUGIN_COMPILER, "compile", new CompilerArgsQuery());
        }

        @Override
        public CompilerArgsQueryResult build(Xpp3Dom config, ExpressionEvaluator eval) {
            if (config != null) {
                Xpp3Dom container = config.getChild("compilerArgs");
                if (container != null) {
                    Xpp3Dom[] args = container.getChildren("arg");
                    if (args != null) {
                        if (container.getInputLocation() instanceof InputLocation location) {
                            return new CompilerArgsQueryResult(
                                Stream.of(args).map(a -> a.getValue()).toList(),
                                location.getSource().getModelId()
                            );
                        }
                    }
                }
            }
            return null;
        }
    }

    String getCompilerParam(ModelHandle2 handle, String param) {
        CompilerParamOperation oper = (CompilerParamOperation) operations.get(param);
        if (oper != null) {
            return oper.value;
        }
        return PluginPropertyUtils.getPluginProperty(
            handle.getProject(), GROUP_APACHE_PLUGINS, PLUGIN_COMPILER, param, "compile", null); //NOI18N
    }

    // TODO could be moved to utilities at some point
    private static Configuration getOrCreateCompilerConfig(POMModel model) {
        Plugin old = null;
        Build build = model.getProject().getBuild();
        if (build != null) {
            old = build.findPluginById(GROUP_APACHE_PLUGINS, PLUGIN_COMPILER);
            if (old == null && build.getPluginManagement() != null) {
                old = build.getPluginManagement().findPluginById(GROUP_APACHE_PLUGINS, PLUGIN_COMPILER);
            }
        } else {
            build = model.getFactory().createBuild();
            model.getProject().setBuild(build);
        }
        Plugin plugin;
        if (old != null) {
            plugin = old;
        } else {
            plugin = model.getFactory().createPlugin();
            plugin.setGroupId(GROUP_APACHE_PLUGINS);
            plugin.setArtifactId(PLUGIN_COMPILER);
            plugin.setVersion(MavenVersionSettings.getDefault().getVersion(GROUP_APACHE_PLUGINS, PLUGIN_COMPILER));
            build.addPlugin(plugin);
        }
        Configuration config = plugin.getConfiguration();
        if (config == null) {
            config = model.getFactory().createConfiguration();
            plugin.setConfiguration(config);
        }
        return config;
    }

    private static POMExtensibilityElement getOrCreateElement(POMModel model, POMComponent parent, String name, String text) {
        POMExtensibilityElement elem = getElement(parent, name, text);
        if (elem != null) {
            return elem;
        }
        return createElement(model, parent, name, text);
    }

    private static POMExtensibilityElement createElement(POMModel model, POMComponent parent, String name, String text) {
        POMExtensibilityElement elem = model.getFactory().createPOMExtensibilityElement(QName.valueOf(name));
        if (text != null) {
            elem.setElementText(text);
        }
        parent.addExtensibilityElement(elem);
        return elem;
    }

    private static POMExtensibilityElement getElement(POMComponent parent, String name, String text) {
        for (POMExtensibilityElement element : parent.getExtensibilityElements()) {
            if (element.getQName().getLocalPart().equals(name) && (text == null || text.equals(element.getElementText()))) {
                return element;
            }
        }
        return null;
    }

    static class PlatformsModel extends AbstractListModel implements ComboBoxModel, PropertyChangeListener {

        private static final long serialVersionUID = 1L;

        private List<Union2<JavaPlatform, String>> data;
        private Union2<JavaPlatform, String> sel;
        private final Project project;
        private final ModelHandle2 handle;

        public PlatformsModel(Project project, ModelHandle2 handle) {
            this.project = project;
            this.handle = handle;
            JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            getPlatforms(jpm);
            jpm.addPropertyChangeListener(WeakListeners.propertyChange(this, jpm));
            sel = Union2.createFirst(jpm.getDefaultPlatform());
        }

        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public Object getElementAt(int index) {
            return data.get(index);
        }

        @Override
        public void setSelectedItem(Object anItem) {
            sel = (Union2<JavaPlatform, String>)anItem;
            fireContentsChanged(this, 0, data.size());
        }

        @Override
        public Union2<JavaPlatform, String> getSelectedItem() {
            return sel;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            getPlatforms(jpm);
            fireContentsChanged(this, 0, data.size());
        }

        private void getPlatforms(JavaPlatformManager jpm) {
            List<Union2<JavaPlatform, String>> tmp = Arrays.stream(jpm.getPlatforms(null, new Specification(CommonProjectUtils.J2SE_PLATFORM_TYPE, null)))
                    .filter(JavaPlatform::isValid)
                    .peek((jp) -> LOG.log(Level.FINE, "Adding JavaPlaform: {0}", jp.getDisplayName()))  //NOI18N
                    .map((jp) -> Union2.<JavaPlatform, String>createFirst(jp))
                    .sorted((jp1, jp2) -> displayName(jp2).compareTo(displayName(jp1)))
                    .collect(Collectors.toCollection(ArrayList::new));
            String val = null;
            final Properties props = handle.getPOMModel().getProject().getProperties();
            if (props != null) {
                val = props.getProperty(HINT_JDK_PLATFORM);
            }
            if (val == null) {
                val = handle.getRawAuxiliaryProperty(HINT_JDK_PLATFORM, true);
            }
            String broken = null;
            if (val != null) {
                JavaPlatform jp = BootClassPathImpl.getActivePlatform(val);
                if (jp == null || !jp.isValid()) {
                    broken = val;
                }
            } else {
                Pair<String, JavaPlatform> nameJpP = getSelPlatform();
                if (nameJpP.second() == null || !nameJpP.second().isValid()) {
                    broken = nameJpP.first();
                }
            }
            if (broken != null) {
                tmp.add(Union2.createSecond(broken));
            }
            data = tmp;
        }

        private static String displayName(Union2<JavaPlatform, String> item) {
            return item.hasFirst() ? item.first().getDisplayName() : item.second();
        }

        private Pair<String, JavaPlatform> getSelPlatform() {
            String platformId = project.getLookup().lookup(AuxiliaryProperties.class).get(HINT_JDK_PLATFORM, true);
            return Pair.of(platformId, BootClassPathImpl.getActivePlatform(platformId));
        }
    }

    private class PlatformsRenderer extends JLabel implements ListCellRenderer, UIResource {

        public PlatformsRenderer() {
            setOpaque(true);
        }

        @NbBundle.Messages({
            "# {0} - platform name",
            "TXT_BrokenPlatformFmt=Missing platform: {0}"
        })
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            final String strValue;
            if (value instanceof Union2) {
                final Union2<JavaPlatform,String> u2 = (Union2<JavaPlatform,String>) value;
                if (u2.hasFirst()) {
                    strValue = u2.first().getDisplayName();
                } else {
                    strValue = "<html><font color=\"#A40000\">" //NOI18N
                            + Bundle.TXT_BrokenPlatformFmt(u2.second());
                }
            } else {
                strValue = Optional.ofNullable(value)
                        .map(Object::toString)
                        .orElse("");    //NOI18N
            }
            setText(strValue);

            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
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
    
    @Override
    public HelpCtx getHelpCtx() {
        return CustomizerProviderImpl.HELP_CTX;
    }
}
