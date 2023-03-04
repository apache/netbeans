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

package org.netbeans.modules.java.j2semodule.ui.customizer;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.java.j2semodule.J2SEModularProject;
import org.openide.util.HelpCtx;
//import org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider;

/** Customizer for general project attributes.
 */
public class CustomizerJar extends JPanel implements HelpCtx.Provider {

    private J2SEModularProject project;
    private final Map<JComponent,Collection<Supplier<Boolean>>> jLinkComponents;

    public CustomizerJar( J2SEModularProjectProperties uiProperties ) {
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
        addPanelFiller(nextExtensionYPos);
        excludeField.setDocument(uiProperties.BUILD_CLASSES_EXCLUDES_MODEL);

        uiProperties.JAR_COMPRESS_MODEL.setMnemonic(compressCheckBox.getMnemonic());
        compressCheckBox.setModel(uiProperties.JAR_COMPRESS_MODEL);

        uiProperties.DO_JAR_MODEL.setMnemonic(doJarCheckBox.getMnemonic());
        doJarCheckBox.setModel(uiProperties.DO_JAR_MODEL);

        uiProperties.JLINK_MODEL.setMnemonic(jlink.getMnemonic());
        jlink.setModel(uiProperties.JLINK_MODEL);
        uiProperties.JLINK_STRIP_MODEL.setMnemonic(jlinkStrip.getMnemonic());
        jlinkStrip.setModel(uiProperties.JLINK_STRIP_MODEL);
        uiProperties.JLINK_LAUNCHER_MODEL.setMnemonic(jLinkCreateLaucher.getMnemonic());
        jLinkCreateLaucher.setModel(uiProperties.JLINK_LAUNCHER_MODEL);
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

        mainPanel = new javax.swing.JPanel();
        excludeLabel = new javax.swing.JLabel();
        excludeField = new javax.swing.JTextField();
        excludeMessage = new javax.swing.JLabel();
        compressCheckBox = new javax.swing.JCheckBox();
        doJarCheckBox = new javax.swing.JCheckBox();
        jlink = new javax.swing.JCheckBox();
        jlinkStrip = new javax.swing.JCheckBox();
        jLinkCreateLaucher = new javax.swing.JCheckBox();
        jLinkLauncherNameLabel = new javax.swing.JLabel();
        jLinkLaucherName = new javax.swing.JTextField();
        extPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        mainPanel.setPreferredSize(new java.awt.Dimension(427, 290));

        excludeLabel.setLabelFor(excludeField);
        org.openide.awt.Mnemonics.setLocalizedText(excludeLabel, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "LBL_CustomizeJar_Excludes_JTextField")); // NOI18N

        excludeMessage.setLabelFor(excludeField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2semodule/ui/customizer/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(excludeMessage, bundle.getString("LBL_CustomizerJar_ExcludeMessage")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(compressCheckBox, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "LBL_CustomizeJar_Commpres_JCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(doJarCheckBox, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "CustomizerJar.doJarCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jlink, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "TXT_Jar_JLink")); // NOI18N
        jlink.setActionCommand("Create J&LINK Distribution");

        org.openide.awt.Mnemonics.setLocalizedText(jlinkStrip, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "TXT_Jar_JLinkStrip")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLinkCreateLaucher, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "TXT_Jar_JLink_CreateLaucher")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLinkLauncherNameLabel, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "TXT_Jar_JLink_LaucherName")); // NOI18N

        jLinkLaucherName.setText("jTextField1");

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(excludeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(excludeMessage)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(excludeField, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)))
                    .addComponent(jlink, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(excludeLabel)
                    .addComponent(excludeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(excludeMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(compressCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(doJarCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jlink)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLinkCreateLaucher)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLinkLauncherNameLabel)
                    .addComponent(jLinkLaucherName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jlinkStrip)
                .addContainerGap(53, Short.MAX_VALUE))
        );

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
    private javax.swing.JCheckBox doJarCheckBox;
    private javax.swing.JTextField excludeField;
    private javax.swing.JLabel excludeLabel;
    private javax.swing.JLabel excludeMessage;
    private javax.swing.JPanel extPanel;
    private javax.swing.JCheckBox jLinkCreateLaucher;
    private javax.swing.JTextField jLinkLaucherName;
    private javax.swing.JLabel jLinkLauncherNameLabel;
    private javax.swing.JCheckBox jlink;
    private javax.swing.JCheckBox jlinkStrip;
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables

//    private boolean addExtPanel(Project p, J2SECategoryExtensionProvider compProvider, int gridY) {
//        if (compProvider != null) {
//            JComponent comp = compProvider.createComponent(p, null);
//            if (comp != null) {
//                java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
//                constraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//                constraints.gridx = 0;
//                constraints.gridy = gridY;
//                constraints.weightx = 1.0;
//                extPanel.add(comp, constraints);
//                return true;
//            }
//        }
//        return false;
//    }

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
    
    private boolean isJLinkOptionsEnabled() {
        return jlink.isSelected();
    }

    private boolean isJLinkLauncherEnabled() {
        return jLinkCreateLaucher.isSelected();
    }

    private void enableJLink() {
        for (Map.Entry<JComponent,Collection<Supplier<Boolean>>> e : jLinkComponents.entrySet()) {
            final JComponent c = e.getKey();
            c.setEnabled(e.getValue().stream().map(Supplier::get).reduce(true, (a,b) -> a&&b));
        }
    }
}
