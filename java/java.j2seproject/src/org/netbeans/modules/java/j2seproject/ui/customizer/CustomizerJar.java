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

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.openide.util.HelpCtx;
import org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider;
import org.openide.modules.SpecificationVersion;
import org.openide.util.ChangeSupport;

/** Customizer for general project attributes.
 */
public class CustomizerJar extends JPanel implements HelpCtx.Provider {

    private J2SEProject project;
    private java.util.List<J2SECategoryExtensionProvider> compProviders = new LinkedList<J2SECategoryExtensionProvider>();
    private final ComboBoxModel<?> sourceLevel;
    private final Map<JComponent,Collection<Supplier<Boolean>>> jLinkComponents;
    
    public CustomizerJar( J2SEProjectProperties uiProperties ) {
        initComponents();
        final Map<JComponent,Collection<Supplier<Boolean>>> m = new LinkedHashMap<>();
        m.put(jlink, Collections.emptySet());
        m.put(jlinkStrip, Collections.singleton(this::isJLinkOptionsEnabled));
        m.put(jLinkCreateLaucher, Collections.singleton(this::isJLinkOptionsEnabled));
        m.put(jLinkLaucherName, Arrays.asList(this::isJLinkOptionsEnabled, this::isJLinkLauncherEnabled));
        m.put(jLinkLauncherNameLabel, Arrays.asList(this::isJLinkOptionsEnabled, this::isJLinkLauncherEnabled));
        this.jLinkComponents = Collections.unmodifiableMap(m);
        int nextExtensionYPos = 0;
        this.project = uiProperties.getProject();
        for (J2SECategoryExtensionProvider compProvider : project.getLookup().lookupAll(J2SECategoryExtensionProvider.class)) {
            if( compProvider.getCategory() == J2SECategoryExtensionProvider.ExtensibleCategory.PACKAGING ) {
                if( addExtPanel(project,compProvider,nextExtensionYPos) ) {
                    compProviders.add(compProvider);
                    nextExtensionYPos++;
                }
            }
        }
        addPanelFiller(nextExtensionYPos);
        
        distDirField.setDocument(uiProperties.DIST_JAR_MODEL);
        excludeField.setDocument(uiProperties.BUILD_CLASSES_EXCLUDES_MODEL);

        uiProperties.JAR_COMPRESS_MODEL.setMnemonic(compressCheckBox.getMnemonic());
        compressCheckBox.setModel(uiProperties.JAR_COMPRESS_MODEL);

        uiProperties.DO_JAR_MODEL.setMnemonic(doJarCheckBox.getMnemonic());
        doJarCheckBox.setModel(uiProperties.DO_JAR_MODEL);

        uiProperties.COPY_LIBS_MODEL.setMnemonic(copyLibs.getMnemonic());
        copyLibs.setModel(uiProperties.COPY_LIBS_MODEL);
        this.sourceLevel = uiProperties.JAVAC_SOURCE_MODEL;
        this.sourceLevel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
            }
            @Override
            public void intervalRemoved(ListDataEvent e) {
            }
            @Override
            public void contentsChanged(ListDataEvent e) {
                enableJLink();
            }
        });
        uiProperties.JLINK_MODEL.setMnemonic(jlink.getMnemonic());
        jlink.setModel(new ButtonModelDecorator(uiProperties.JLINK_MODEL));
        uiProperties.JLINK_STRIP_MODEL.setMnemonic(jlinkStrip.getMnemonic());
        jlinkStrip.setModel(new ButtonModelDecorator(uiProperties.JLINK_STRIP_MODEL));
        uiProperties.JLINK_LAUNCHER_MODEL.setMnemonic(jLinkCreateLaucher.getMnemonic());
        jLinkCreateLaucher.setModel(new ButtonModelDecorator(uiProperties.JLINK_LAUNCHER_MODEL));
        jLinkLaucherName.setDocument(uiProperties.JLINK_LAUNCHER_NAME_MODEL);
        doJarCheckBox.addActionListener((e)->{
            if (!doJarCheckBox.isSelected()) {
                jlink.setSelected(false);
                enableJLink();
            }
        });
        jlink.addActionListener((e)->{
            if(jlink.isSelected()) {
                doJarCheckBox.setSelected(true);
            }
            enableJLink();
        });
        jLinkCreateLaucher.addActionListener((e) -> {
            enableJLink();
        });
        enableJLink();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx( CustomizerJar.class );
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jCheckBox1 = new javax.swing.JCheckBox();
        mainPanel = new javax.swing.JPanel();
        distDirLabel = new javax.swing.JLabel();
        distDirField = new javax.swing.JTextField();
        excludeLabel = new javax.swing.JLabel();
        excludeField = new javax.swing.JTextField();
        excludeMessage = new javax.swing.JLabel();
        compressCheckBox = new javax.swing.JCheckBox();
        doJarCheckBox = new javax.swing.JCheckBox();
        copyLibs = new javax.swing.JCheckBox();
        jlink = new javax.swing.JCheckBox();
        jLinkCreateLaucher = new javax.swing.JCheckBox();
        jLinkLauncherNameLabel = new javax.swing.JLabel();
        jLinkLaucherName = new javax.swing.JTextField();
        jlinkStrip = new javax.swing.JCheckBox();
        extPanel = new javax.swing.JPanel();

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, "jCheckBox1");

        setLayout(new java.awt.GridBagLayout());

        mainPanel.setPreferredSize(new java.awt.Dimension(435, 290));

        distDirLabel.setLabelFor(distDirField);
        org.openide.awt.Mnemonics.setLocalizedText(distDirLabel, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "LBL_CustomizeJar_DistDir_JTextField")); // NOI18N

        distDirField.setEditable(false);

        excludeLabel.setLabelFor(excludeField);
        org.openide.awt.Mnemonics.setLocalizedText(excludeLabel, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "LBL_CustomizeJar_Excludes_JTextField")); // NOI18N

        excludeMessage.setLabelFor(excludeField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(excludeMessage, bundle.getString("LBL_CustomizerJar_ExcludeMessage")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(compressCheckBox, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "LBL_CustomizeJar_Commpres_JCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(doJarCheckBox, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "CustomizerJar.doJarCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(copyLibs, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "TXT_CopyLibraries")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jlink, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "TXT_Jar_JLink")); // NOI18N
        jlink.setActionCommand("Create J&LINK Distribution");

        org.openide.awt.Mnemonics.setLocalizedText(jLinkCreateLaucher, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "TXT_Jar_JLink_CreateLaucher")); // NOI18N

        jLinkLauncherNameLabel.setLabelFor(jLinkLaucherName);
        org.openide.awt.Mnemonics.setLocalizedText(jLinkLauncherNameLabel, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "TXT_Jar_JLink_LaucherName")); // NOI18N

        jLinkLaucherName.setText("jTextField1");

        org.openide.awt.Mnemonics.setLocalizedText(jlinkStrip, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "TXT_Jar_JLinkStrip")); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(distDirLabel)
                            .addComponent(excludeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(excludeMessage)
                                .addGap(0, 112, Short.MAX_VALUE))
                            .addComponent(excludeField)
                            .addComponent(distDirField)))
                    .addComponent(jlink, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(copyLibs)
                    .addComponent(compressCheckBox)
                    .addComponent(doJarCheckBox)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(jLinkLauncherNameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLinkLaucherName))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jlinkStrip)
                                    .addComponent(jLinkCreateLaucher))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(distDirLabel)
                    .addComponent(distDirField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(excludeLabel)
                    .addComponent(excludeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(excludeMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compressCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(doJarCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(copyLibs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jlink)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLinkCreateLaucher)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLinkLauncherNameLabel)
                    .addComponent(jLinkLaucherName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addComponent(jlinkStrip))
        );

        distDirField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerJar.class).getString("AD_jTextFieldDistDir")); // NOI18N
        excludeField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerJar.class).getString("AD_jTextFieldExcludes")); // NOI18N
        compressCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerJar.class).getString("AD_jCheckBoxCompress")); // NOI18N
        doJarCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJar.class, "ACSD_BuildJarAfterCompile")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(mainPanel, gridBagConstraints);

        extPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(extPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox compressCheckBox;
    private javax.swing.JCheckBox copyLibs;
    private javax.swing.JTextField distDirField;
    private javax.swing.JLabel distDirLabel;
    private javax.swing.JCheckBox doJarCheckBox;
    private javax.swing.JTextField excludeField;
    private javax.swing.JLabel excludeLabel;
    private javax.swing.JLabel excludeMessage;
    private javax.swing.JPanel extPanel;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jLinkCreateLaucher;
    private javax.swing.JTextField jLinkLaucherName;
    private javax.swing.JLabel jLinkLauncherNameLabel;
    private javax.swing.JCheckBox jlink;
    private javax.swing.JCheckBox jlinkStrip;
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables

    private boolean addExtPanel(Project p, J2SECategoryExtensionProvider compProvider, int gridY) {
        if (compProvider != null) {
            JComponent comp = compProvider.createComponent(p, null);
            if (comp != null) {
                java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
                constraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                constraints.gridx = 0;
                constraints.gridy = gridY;
                constraints.weightx = 1.0;
                extPanel.add(comp, constraints);
                return true;
            }
        }
        return false;
    }

    private void addPanelFiller(int gridY) {
        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        constraints.gridx = 0;
        constraints.gridy = gridY;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        extPanel.add( new Box.Filler(
                new Dimension(), 
                new Dimension(),
                new Dimension(10000,10000) ),
                constraints);
    }

    private boolean isJLinkEnabled() {
        final SpecificationVersion sl = PlatformUiSupport.getSourceLevel(this.sourceLevel.getSelectedItem());
        return J2SEProjectProperties.JDK9.compareTo(sl) <= 0;
    }

    private boolean isJLinkOptionsEnabled() {
        return jlink.isSelected();
    }

    private boolean isJLinkLauncherEnabled() {
        return jLinkCreateLaucher.isSelected();
    }

    private void enableJLink() {
        final boolean correctSourceLevel = isJLinkEnabled();
        for (Map.Entry<JComponent,Collection<Supplier<Boolean>>> e : jLinkComponents.entrySet()) {
            final JComponent c = e.getKey();
            c.setEnabled(e.getValue().stream().map(Supplier::get).reduce(correctSourceLevel, (a,b) -> a&&b));
            if (c instanceof AbstractButton) {
                ButtonModelDecorator.cast(((AbstractButton)c).getModel())
                        .ifPresent((model) -> model.setOverride(correctSourceLevel ? null : false));
            }
        }
    }

    private static final class ButtonModelDecorator implements ButtonModel {
        private final ButtonModel delegate;
        private final List<ActionListener> actionListeners;
        private final List<ItemListener> itemListeners;
        private final ChangeSupport changeListeners;
        private Boolean override;

        ButtonModelDecorator(@NonNull final ButtonModel delegate) {
            this.delegate = delegate;
            this.actionListeners = new CopyOnWriteArrayList<>();
            this.itemListeners = new CopyOnWriteArrayList<>();
            this.changeListeners = new ChangeSupport(this);
            this.delegate.addActionListener((e) -> {
                final ActionEvent ne = new ActionEvent(
                        this,
                        e.getID(),
                        e.getActionCommand(),
                        e.getWhen(),
                        e.getModifiers());
                for (ActionListener l : actionListeners) {
                    l.actionPerformed(ne);
                }
            });
            this.delegate.addItemListener((e) -> {
                final ItemEvent ne = new ItemEvent(
                        this,
                        e.getID(),
                        e.getItem(),
                        e.getStateChange());
                for (ItemListener l : itemListeners) {
                    l.itemStateChanged(ne);
                }
            });
            this.delegate.addChangeListener((e) -> changeListeners.fireChange());
        }

        void setOverride(Boolean value) {
            this.override = value;
        }

        @NonNull
        static Optional<ButtonModelDecorator> cast(Object obj) {
            return obj instanceof ButtonModelDecorator ?
                    Optional.of((ButtonModelDecorator) obj) :
                    Optional.empty();
        }

        @Override
        public boolean isArmed() {
            return delegate.isArmed();
        }

        @Override
        public boolean isSelected() {
            return override != null ?
                    override :
                    delegate.isSelected();
        }

        @Override
        public boolean isEnabled() {
            return delegate.isEnabled();
        }

        @Override
        public boolean isPressed() {
            return delegate.isPressed();
        }

        @Override
        public boolean isRollover() {
            return delegate.isRollover();
        }

        @Override
        public void setArmed(boolean b) {
            this.delegate.setArmed(b);
        }

        @Override
        public void setSelected(boolean b) {
            this.delegate.setSelected(b);
        }

        @Override
        public void setEnabled(boolean b) {
            this.delegate.setEnabled(b);
        }

        @Override
        public void setPressed(boolean b) {
            this.delegate.setPressed(b);
        }

        @Override
        public void setRollover(boolean b) {
            this.delegate.setRollover(b);
        }

        @Override
        public void setMnemonic(int key) {
            this.delegate.setMnemonic(key);
        }

        @Override
        public int getMnemonic() {
            return this.delegate.getMnemonic();
        }

        @Override
        public void setActionCommand(String s) {
            this.delegate.setActionCommand(s);
        }

        @Override
        public String getActionCommand() {
            return this.delegate.getActionCommand();
        }

        @Override
        public void setGroup(ButtonGroup group) {
            this.delegate.setGroup(group);
        }

        @Override
        public void addActionListener(ActionListener l) {
            this.actionListeners.add(l);
        }

        @Override
        public void removeActionListener(ActionListener l) {
            this.actionListeners.remove(l);
        }

        @Override
        public void addItemListener(ItemListener l) {
            this.itemListeners.add(l);
        }

        @Override
        public void removeItemListener(ItemListener l) {
            this.itemListeners.remove(l);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            this.changeListeners.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            this.changeListeners.removeChangeListener(l);
        }

        @Override
        public Object[] getSelectedObjects() {
            return this.delegate.getSelectedObjects();
        }
    }

}
