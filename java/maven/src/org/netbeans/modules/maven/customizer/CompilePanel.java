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

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
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
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.UIResource;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.customizer.support.CheckBoxUpdater;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.maven.classpath.BootClassPathImpl;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
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

/**
 *
 * @author mkleint
 */
public class CompilePanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private static final Logger LOG = Logger.getLogger(CompilePanel.class.getName());
    
    private static final String PARAM_DEBUG = "debug";//NOI18N
    private static final String PARAM_DEPRECATION = "showDeprecation";

    private final ModelHandle2 handle;
    private final Project project;
    private static boolean warningShown = false;

    private Color origComPlatformFore;

    /** Creates new form CompilePanel */
    public CompilePanel(ModelHandle2 handle, Project prj, MavenProjectPropertiesUiSupport uiSupport) {
        initComponents();
        this.handle = handle;
        project = prj;
        comJavaPlatform.setModel(uiSupport.getPlatformComboBoxModel());
        comJavaPlatform.setRenderer(new PlatformsRenderer());

        origComPlatformFore = comJavaPlatform.getForeground();
        /* @TODO reinstate if a new link created, or remove
        btnLearnMore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLearnMore.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://wiki.netbeans.org/FaqCompileOnSave#Using_Compile_on_Save_in_Maven_Projects"));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        */
        btnLearnMore.setVisible(false);
        initValues();
    }

    private void initValues() {
        boolean cosSupported = BuildArtifactMapper.isCompileOnSaveSupported();
        if (!cosSupported) {
            cbCompileOnSave.setEnabled(false);
        }
        new CheckBoxUpdater(cbCompileOnSave) {
            private String modifiedValue;

            private ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {

                @Override
                public void performOperation(POMModel model) {
                    Properties modprops = model.getProject().getProperties();
                    if (modprops == null) {
                        modprops = model.getFactory().createProperties();
                        model.getProject().setProperties(modprops);
                    }
                    modprops.setProperty(Constants.HINT_COMPILE_ON_SAVE, modifiedValue); //NOI18N
                }
            };

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
                    val = handle.getRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, true);
                }
                if (val == null) {
                    java.util.Properties props = handle.getProject().getProperties();
                    if (props != null) {
                        val = props.getProperty(Constants.HINT_COMPILE_ON_SAVE);
                    }
                }             
                return val != null ? (!"none".equals(val)) : null;
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

                boolean hasConfig = handle.getRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, true) != null;
                org.netbeans.modules.maven.model.pom.Project p = handle.getPOMModel().getProject();
                if (p.getProperties() != null && p.getProperties().getProperty(Constants.HINT_COMPILE_ON_SAVE) != null) {
                    modifiedValue = value;
                    handle.addPOMModification(operation);
                    if (hasConfig) {
                        // in this case clean up the auxiliary config
                        handle.setRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, null, true);
                    }
                } else {
                    handle.setRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, value, true);
                }
            }
        };
        new CheckBoxUpdater(cbDebug) {
            @Override
            public Boolean getValue() {
                String val = getCompilerParam(handle,PARAM_DEBUG);
                if (val != null) {
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
                    text = "" + getDefaultValue();
                } else {
                    text = value.toString();
                }
                modifyCompilerParamOperation(handle, PARAM_DEBUG, text);
            }

            @Override
            public boolean getDefaultValue() {
                return true;
            }
        };

        new CheckBoxUpdater(cbDeprecate) {
            @Override
            public Boolean getValue() {
                String val = getCompilerParam(handle,PARAM_DEPRECATION);
                if (val != null) {
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
                    text = "" + getDefaultValue();
                } else {
                    text = value.toString();
                }
                modifyCompilerParamOperation(handle, PARAM_DEPRECATION, text);
            }

            @Override
            public boolean getDefaultValue() {
                return false;
            }
        };

        // java platform updater
        ComboBoxUpdater<Union2<JavaPlatform,String>> compleComboBoxUpdater = new ComboBoxUpdater<Union2<JavaPlatform,String>>(comJavaPlatform, lblJavaPlatform) {
            private String modifiedValue;
            private String DEFAULT_PLATFORM_VALUE = "@@DEFAULT@@";
            private ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {

            @Override
                public void performOperation(POMModel model) {
                    Properties modprops = model.getProject().getProperties();
                    if (modprops == null) {
                        modprops = model.getFactory().createProperties();
                        model.getProject().setProperties(modprops);
                    }
                    modprops.setProperty(Constants.HINT_JDK_PLATFORM, modifiedValue); //NOI18N
                }
            };
            
            @Override
            public Union2<JavaPlatform,String> getValue() {
                String val = modifiedValue;
                if (val == null) {
                    Properties props = handle.getPOMModel().getProject().getProperties();
                    if (props != null) {
                        val = props.getProperty(Constants.HINT_JDK_PLATFORM);
                    }
                }
                if (val == null) {
                    val = handle.getRawAuxiliaryProperty(Constants.HINT_JDK_PLATFORM, true);
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
            public Union2<JavaPlatform,String> getDefaultValue() {
                return Union2.createFirst(JavaPlatformManager.getDefault().getDefaultPlatform());
            }

            @Override
            public void setValue(Union2<JavaPlatform,String> value) {
                handle.removePOMModification(operation);
                modifiedValue = null;
                final Union2<JavaPlatform,String> platf = value == null ?
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

                boolean hasConfig = handle.getRawAuxiliaryProperty(Constants.HINT_JDK_PLATFORM, true) != null;
                //TODO also try to take the value in pom vs inherited pom value into account.
                modifiedValue = platformId == null ? DEFAULT_PLATFORM_VALUE : platformId;
                if (handle.getProject().getProperties().containsKey(Constants.HINT_JDK_PLATFORM)) {
                    
                    handle.addPOMModification(operation);
                    if (hasConfig) {
                        // in this case clean up the auxiliary config
                        handle.setRawAuxiliaryProperty(Constants.HINT_JDK_PLATFORM, null, true);
                    }
                } else {
                    handle.setRawAuxiliaryProperty(Constants.HINT_JDK_PLATFORM, platformId, true);
                }
            }
        };
        // the selected item is not set until the compile panel is shown
        // so, invoke these methods for setting it
        compleComboBoxUpdater.ancestorAdded(null);
        compleComboBoxUpdater.ancestorRemoved(null);
    }

    private Pair<String,JavaPlatform> getSelPlatform () {
        String platformId = project.getLookup().lookup(AuxiliaryProperties.class).
                get(Constants.HINT_JDK_PLATFORM, true);
        return Pair.of(platformId,BootClassPathImpl.getActivePlatform(platformId));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblJavaPlatform = new javax.swing.JLabel();
        comJavaPlatform = new javax.swing.JComboBox();
        btnMngPlatform = new javax.swing.JButton();
        lblHint1 = new javax.swing.JLabel();
        lblHint2 = new javax.swing.JLabel();
        cbDebug = new javax.swing.JCheckBox();
        cbDeprecate = new javax.swing.JCheckBox();
        cbCompileOnSave = new javax.swing.JCheckBox();
        btnLearnMore = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(576, 303));

        lblJavaPlatform.setLabelFor(comJavaPlatform);
        org.openide.awt.Mnemonics.setLocalizedText(lblJavaPlatform, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblJavaPlatform.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnMngPlatform, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.btnMngPlatform.text")); // NOI18N
        btnMngPlatform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMngPlatformActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblHint1, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblHint1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint2, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblHint2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbDebug, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.cbDebug.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbDeprecate, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.cbDeprecate.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbCompileOnSave, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.cbCompileOnSave.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnLearnMore, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.btnLearnMore.text")); // NOI18N
        btnLearnMore.setBorderPainted(false);
        btnLearnMore.setContentAreaFilled(false);
        btnLearnMore.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbDebug)
                            .addComponent(cbDeprecate)
                            .addComponent(lblHint1)
                            .addComponent(lblHint2))
                        .addGap(0, 24, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cbCompileOnSave, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(lblJavaPlatform)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comJavaPlatform, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMngPlatform)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(btnLearnMore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblJavaPlatform)
                    .addComponent(comJavaPlatform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMngPlatform))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbCompileOnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblHint1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblHint2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLearnMore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbDebug)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbDeprecate)
                .addContainerGap(108, Short.MAX_VALUE))
        );

        btnMngPlatform.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.btnMngPlatform.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void btnMngPlatformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMngPlatformActionPerformed
        // TODO add your handling code here:
        PlatformsCustomizer.showCustomizer(getSelPlatform().second());
}//GEN-LAST:event_btnMngPlatformActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLearnMore;
    private javax.swing.JButton btnMngPlatform;
    private javax.swing.JCheckBox cbCompileOnSave;
    private javax.swing.JCheckBox cbDebug;
    private javax.swing.JCheckBox cbDeprecate;
    private javax.swing.JComboBox comJavaPlatform;
    private javax.swing.JLabel lblHint1;
    private javax.swing.JLabel lblHint2;
    private javax.swing.JLabel lblJavaPlatform;
    // End of variables declaration//GEN-END:variables

    private static final String CONFIGURATION_EL = "configuration";//NOI18N

    private final Map<String, CompilerParamOperation> operations = new HashMap<String, CompilerParamOperation>();

    /**
     * update the debug param of project to given value.
     *
     * @param handle handle which models are to be updated
     * @param sourceLevel the sourcelevel to set
     */
    private void modifyCompilerParamOperation(ModelHandle2 handle, String param, String value) {
        String debug = PluginPropertyUtils.getPluginProperty(handle.getProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, param,
                "compile"); //NOI18N
        if (debug != null && debug.contains(value)) {
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
    
    private class CompilerParamOperation implements ModelOperation<POMModel> {
        private final String value;
        private final String param;

        public CompilerParamOperation(String param, String value) {
            this.param = param;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public void performOperation(POMModel model) {
            Plugin old = null;
            Plugin plugin;
            Build bld = model.getProject().getBuild();
            if (bld != null) {
                old = bld.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
            } else {
                bld = model.getFactory().createBuild();
                model.getProject().setBuild(bld);
            }
            if (old != null) {
                plugin = old;
            } else {
                plugin = model.getFactory().createPlugin();
                plugin.setGroupId(Constants.GROUP_APACHE_PLUGINS);
                plugin.setArtifactId(Constants.PLUGIN_COMPILER);
                plugin.setVersion(MavenVersionSettings.getDefault().getVersion(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER));
                bld.addPlugin(plugin);
            }
            Configuration config = plugin.getConfiguration();
            if (config == null) {
                config = model.getFactory().createConfiguration();
                plugin.setConfiguration(config);
            }
            config.setSimpleParameter(param, value);
        }

    }

    String getCompilerParam(ModelHandle2 handle, String param) {
        CompilerParamOperation oper = operations.get(param);
        if (oper != null) {
            return oper.getValue();
                }

        String value = PluginPropertyUtils.getPluginProperty(handle.getProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, param,
                "compile"); //NOI18N
        if (value != null) {
            return value;
        }
        return null;
    }

    static class PlatformsModel extends AbstractListModel implements ComboBoxModel, PropertyChangeListener {

        private static final long serialVersionUID = 1L;

        private List<Union2<JavaPlatform,String>> data;
        private Union2<JavaPlatform,String> sel;
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
            sel = (Union2<JavaPlatform,String>)anItem;
            fireContentsChanged(this, 0, data.size());
        }

        @Override
        public Union2<JavaPlatform,String> getSelectedItem() {
            return sel;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            getPlatforms(jpm);
            fireContentsChanged(this, 0, data.size());
        }

        private void getPlatforms(JavaPlatformManager jpm) {
            final List<Union2<JavaPlatform,String>> tmp = new ArrayList<>();
            Arrays.stream(jpm.getPlatforms(null, new Specification (CommonProjectUtils.J2SE_PLATFORM_TYPE, null)))
                    .filter(JavaPlatform::isValid)
                    .peek((jp) -> LOG.log(Level.FINE, "Adding JavaPlaform: {0}", jp.getDisplayName()))  //NOI18N
                    .map((jp) -> Union2.<JavaPlatform,String>createFirst(jp))
                    .forEach(tmp::add);
            String val = null;
            final Properties props = handle.getPOMModel().getProject().getProperties();
            if (props != null) {
                val = props.getProperty(Constants.HINT_JDK_PLATFORM);
            }
            if (val == null) {
                val = handle.getRawAuxiliaryProperty(Constants.HINT_JDK_PLATFORM, true);
            }
            String broken = null;
            if (val != null) {
                JavaPlatform jp;
                if ((jp = BootClassPathImpl.getActivePlatform(val)) == null || !jp.isValid()) {
                    broken = val;
                }
            } else {
                final Pair<String,JavaPlatform> nameJpP = getSelPlatform();
                if (nameJpP.second() == null || !nameJpP.second().isValid()) {
                    broken = nameJpP.first();
                }
            }
            if (broken != null) {
                tmp.add(Union2.createSecond(broken));
            }
            data = tmp;
        }

        private Pair<String, JavaPlatform> getSelPlatform() {
            String platformId = project.getLookup().lookup(AuxiliaryProperties.class).
                    get(Constants.HINT_JDK_PLATFORM, true);
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
