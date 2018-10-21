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

package org.netbeans.modules.gradle.execute;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.swing.text.JTextComponent;

import static org.netbeans.modules.gradle.api.execute.GradleCommandLine.Flag.*;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.openide.text.CloneableEditorSupport;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleExecutorOptionsPanel extends javax.swing.JPanel {

    public GradleExecutorOptionsPanel() {
        this(null);
    }
    
    /**
     * Creates new form GradleExecutorOptionsPanel
     */
    public GradleExecutorOptionsPanel(GradleBaseProject gbp) {
        initComponents();
        EditorKit kit = CloneableEditorSupport.getEditorKit(GradleCliEditorKit.MIME_TYPE);
        tfTasks.setEditorKit(kit);
        if (gbp != null) {
            tfTasks.getDocument().putProperty(Document.StreamDescriptionProperty, gbp);
        }
    }

    public void setCommandLine(GradleCommandLine cmd) {
        cbConfigureOnDemand.setSelected(cmd.hasFlag(CONFIGURE_ON_DEMAND));
        cbContinueOnError.setSelected(cmd.hasFlag(CONTINUE));
        cbDryRun.setSelected(cmd.hasFlag(DRY_RUN));
        cbNoRebuild.setSelected(cmd.hasFlag(NO_REBUILD));
        cbOffline.setSelected(cmd.hasFlag(OFFLINE));
        cbParallel.setSelected(cmd.hasFlag(PARALLEL));
        cbRecompileScripts.setSelected(cmd.hasFlag(RECOMPILE_SCRIPTS));
        cbRefreshDependencies.setSelected(cmd.hasFlag(REFRESH_DEPENDENCIES));
        cbRerunTasks.setSelected(cmd.hasFlag(RERUN_TASKS));

        cbLogLevel.setSelectedIndex(cmd.getLoglevel().ordinal());
        cbStackTrace.setSelectedIndex(cmd.getStackTrace().ordinal());

        tfTasks.setText(join(" ", cmd.getTasks()));
        tfExcludes.setText(join(" ", cmd.getExcludedTasks()));
    }

    public void applyChanges(GradleCommandLine cmd) {
        cmd.setFlag(CONFIGURE_ON_DEMAND, cbConfigureOnDemand.isSelected());
        cmd.setFlag(CONTINUE, cbContinueOnError.isSelected());
        cmd.setFlag(DRY_RUN, cbDryRun.isSelected());
        cmd.setFlag(NO_REBUILD, cbNoRebuild.isSelected());
        cmd.setFlag(OFFLINE, cbOffline.isSelected());
        cmd.setFlag(PARALLEL, cbParallel.isSelected());
        cmd.setFlag(RECOMPILE_SCRIPTS, cbRecompileScripts.isSelected());
        cmd.setFlag(REFRESH_DEPENDENCIES, cbRefreshDependencies.isSelected());
        cmd.setFlag(RERUN_TASKS, cbRerunTasks.isSelected());

        cmd.setTasks(textToTasks(tfTasks.getText()));

        cmd.setExcludedTasks(textToTasks(tfExcludes.getText()));

        int logLevel = cbLogLevel.getSelectedIndex();
        if ((logLevel >= 0) && (logLevel < GradleCommandLine.LogLevel.values().length)) {
            cmd.setLogLevel(GradleCommandLine.LogLevel.values()[logLevel]);
        }

        int stackTrace = cbStackTrace.getSelectedIndex();
        if ((stackTrace >= 0) && (stackTrace < GradleCommandLine.StackTrace.values().length)) {
            cmd.setStackTrace(GradleCommandLine.StackTrace.values()[stackTrace]);
        }

        //cfg.setSystemProperties(loadFromTextComponent(edSystemProps));
        //cfg.setProjectProperties(loadFromTextComponent(edProjectProps));
    }

    private Properties loadFromTextComponent(JTextComponent comp) {
        Properties props = new Properties();
        try {
            props.load(new StringReader((comp.getText())));
        } catch (IOException ex) {
        }
        return props;
    }

    private List<String> textToTasks(String text) {
        text = text.trim();
        List<String> ret = Collections.emptyList();
        if (!text.isEmpty()) {
            String[] tasks = text.split("\\s+");
            ret = Arrays.asList(tasks);
        }
        return ret;
    }

    private String join(String delim, Collection<String> parts) {
        StringBuilder b = new StringBuilder();
        String prep = "";
        for (String part : parts) {
            b.append(prep).append(part);
            prep = delim;
        }
        return b.toString();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbTasks = new javax.swing.JLabel();
        lbExcludes = new javax.swing.JLabel();
        tfExcludes = new javax.swing.JTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        spProjectProps = new javax.swing.JScrollPane();
        edProjectProps = new javax.swing.JEditorPane();
        spSystemProps = new javax.swing.JScrollPane();
        edSystemProps = new javax.swing.JEditorPane();
        pnOptionsPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        cbOffline = new javax.swing.JCheckBox();
        cbConfigureOnDemand = new javax.swing.JCheckBox();
        cbNoRebuild = new javax.swing.JCheckBox();
        cbContinueOnError = new javax.swing.JCheckBox();
        cbParallel = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        cbRefreshDependencies = new javax.swing.JCheckBox();
        cbRecompileScripts = new javax.swing.JCheckBox();
        cbRerunTasks = new javax.swing.JCheckBox();
        cbDryRun = new javax.swing.JCheckBox();
        lbLogLevel = new javax.swing.JLabel();
        cbLogLevel = new javax.swing.JComboBox<>();
        lbStackTrace = new javax.swing.JLabel();
        cbStackTrace = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JSeparator();
        tfTasks = new javax.swing.JEditorPane();

        org.openide.awt.Mnemonics.setLocalizedText(lbTasks, org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.lbTasks.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbExcludes, org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.lbExcludes.text")); // NOI18N

        tfExcludes.setText(org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.tfExcludes.text")); // NOI18N

        spProjectProps.setToolTipText(org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.spProjectProps.toolTipText")); // NOI18N

        edProjectProps.setContentType("text/x-properties"); // NOI18N
        spProjectProps.setViewportView(edProjectProps);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.spProjectProps.TabConstraints.tabTitle"), spProjectProps); // NOI18N

        edSystemProps.setContentType("text/x-properties"); // NOI18N
        edSystemProps.setToolTipText(org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.edSystemProps.toolTipText")); // NOI18N
        edSystemProps.setEnabled(false);
        spSystemProps.setViewportView(edSystemProps);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.spSystemProps.TabConstraints.tabTitle"), spSystemProps); // NOI18N

        pnOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.pnOptionsPanel.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbOffline, org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.cbOffline.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbConfigureOnDemand, org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.cbConfigureOnDemand.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbNoRebuild, org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.cbNoRebuild.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbContinueOnError, org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.cbContinueOnError.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbParallel, org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.cbParallel.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbOffline, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbConfigureOnDemand, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbNoRebuild, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(cbContinueOnError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbParallel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbOffline)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbConfigureOnDemand)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbNoRebuild)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbContinueOnError)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbParallel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(cbRefreshDependencies, org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.cbRefreshDependencies.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbRecompileScripts, org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.cbRecompileScripts.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbRerunTasks, org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.cbRerunTasks.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbDryRun, org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.cbDryRun.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbLogLevel, org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.lbLogLevel.text")); // NOI18N

        cbLogLevel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Debug", "Info", "Normal", "Quiet" }));
        cbLogLevel.setSelectedIndex(2);

        org.openide.awt.Mnemonics.setLocalizedText(lbStackTrace, org.openide.util.NbBundle.getMessage(GradleExecutorOptionsPanel.class, "GradleExecutorOptionsPanel.lbStackTrace.text")); // NOI18N

        cbStackTrace.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Simple", "Full" }));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbRefreshDependencies, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lbLogLevel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbStackTrace, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbLogLevel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbStackTrace, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(cbRecompileScripts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbRerunTasks, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbDryRun, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbRefreshDependencies, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbRecompileScripts)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbRerunTasks)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbDryRun)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbLogLevel)
                    .addComponent(cbLogLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbStackTrace)
                    .addComponent(cbStackTrace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout pnOptionsPanelLayout = new javax.swing.GroupLayout(pnOptionsPanel);
        pnOptionsPanel.setLayout(pnOptionsPanelLayout);
        pnOptionsPanelLayout.setHorizontalGroup(
            pnOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnOptionsPanelLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnOptionsPanelLayout.setVerticalGroup(
            pnOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnOptionsPanelLayout.createSequentialGroup()
                .addComponent(jSeparator1)
                .addGap(100, 100, 100))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnOptionsPanelLayout.createSequentialGroup()
                .addGroup(pnOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(87, 87, 87))
        );

        tfTasks.setContentType("text/x-gradle-cli"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lbExcludes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbTasks, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfExcludes)
                            .addComponent(tfTasks)))
                    .addComponent(pnOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lbTasks)
                    .addComponent(tfTasks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbExcludes)
                    .addComponent(tfExcludes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbConfigureOnDemand;
    private javax.swing.JCheckBox cbContinueOnError;
    private javax.swing.JCheckBox cbDryRun;
    private javax.swing.JComboBox<String> cbLogLevel;
    private javax.swing.JCheckBox cbNoRebuild;
    private javax.swing.JCheckBox cbOffline;
    private javax.swing.JCheckBox cbParallel;
    private javax.swing.JCheckBox cbRecompileScripts;
    private javax.swing.JCheckBox cbRefreshDependencies;
    private javax.swing.JCheckBox cbRerunTasks;
    private javax.swing.JComboBox<String> cbStackTrace;
    private javax.swing.JEditorPane edProjectProps;
    private javax.swing.JEditorPane edSystemProps;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lbExcludes;
    private javax.swing.JLabel lbLogLevel;
    private javax.swing.JLabel lbStackTrace;
    private javax.swing.JLabel lbTasks;
    private javax.swing.JPanel pnOptionsPanel;
    private javax.swing.JScrollPane spProjectProps;
    private javax.swing.JScrollPane spSystemProps;
    private javax.swing.JTextField tfExcludes;
    private javax.swing.JEditorPane tfTasks;
    // End of variables declaration//GEN-END:variables
}
