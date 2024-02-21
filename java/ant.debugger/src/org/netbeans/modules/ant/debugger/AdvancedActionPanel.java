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

package org.netbeans.modules.ant.debugger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.EditorKit;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.openide.awt.Mnemonics;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Panel for advanced Ant target invocation.
 * @author Jesse Glick
 */
final class AdvancedActionPanel extends javax.swing.JPanel {
    
    /** File attribute storing last-run target(s). Format: space-separated list. */
    private static final String ATTR_TARGETS = "org.apache.tools.ant.module.preferredTargets"; // NOI18N
    /** File attribute storing last-run properties. Format: newline-delimited name=value pairs. */
    private static final String ATTR_PROPERTIES = "org.apache.tools.ant.module.preferredProperties"; // NOI18N
    /** File attribute storing last-run verbosity. Format: int. */
    private static final String ATTR_VERBOSITY = "org.apache.tools.ant.module.preferredVerbosity"; // NOI18N
    
    private static final String[] VERBOSITIES = {
        /* #45482: this one is really useless:
        NbBundle.getMessage(AdvancedActionPanel.class, "LBL_verbosity_err"),
         */
        NbBundle.getMessage(AdvancedActionPanel.class, "LBL_verbosity_warn"),
        NbBundle.getMessage(AdvancedActionPanel.class, "LBL_verbosity_info"),
        NbBundle.getMessage(AdvancedActionPanel.class, "LBL_verbosity_verbose"),
        NbBundle.getMessage(AdvancedActionPanel.class, "LBL_verbosity_debug"),
    };
    private static final int[] VERBOSITY_LEVELS = {
        // no Project.MSG_ERR exposed in GUI
        1 /*Project.MSG_WARN*/,
        2 /*Project.MSG_INFO*/,
        3 /*Project.MSG_VERBOSE*/,
        4 /*Project.MSG_DEBUG*/,
    };
    
    private final AntProjectCookie project;
    private final Set<TargetLister.Target> allTargets;
    private String defaultTarget = null;
    
    public AdvancedActionPanel(AntProjectCookie project, Set/*<TargetLister.Target>*/ allTargets) {
        this.project = project;
        this.allTargets = allTargets;
        initComponents();
        Mnemonics.setLocalizedText(targetLabel, NbBundle.getMessage(AdvancedActionPanel.class, "AdvancedActionsPanel.targetLabel.text"));
        Mnemonics.setLocalizedText(targetDescriptionLabel, NbBundle.getMessage(AdvancedActionPanel.class, "AdvancedActionsPanel.targetDescriptionLabel.text"));
        Mnemonics.setLocalizedText(propertiesLabel, NbBundle.getMessage(AdvancedActionPanel.class, "AdvancedActionsPanel.propertiesLabel.text"));
        Mnemonics.setLocalizedText(verbosityLabel, NbBundle.getMessage(AdvancedActionPanel.class, "AdvancedActionsPanel.verbosityLabel.text"));
        // Hack; EditorKit does not permit "fallback" kits, so we have to
        // mimic what the IDE itself does:
        EditorKit kit = propertiesPane.getEditorKit();
        String clazz = kit.getClass().getName();
        if (clazz.equals("javax.swing.text.DefaultEditorKit") || // NOI18N
                clazz.equals("javax.swing.JEditorPane$PlainEditorKit")) { // NOI18N
            propertiesPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/plain")); // NOI18N
        }
        // Make ENTER run OK, not change the combo box.
        targetComboBox.getInputMap().remove(KeyStroke.getKeyStroke("ENTER")); // NOI18N
        initializeFields();
    }
    
    private void initializeFields() {
        FileObject script = project.getFileObject();
        assert script != null : "No file found for " + project;
        String initialTargets = (String) script.getAttribute(ATTR_TARGETS);
        SortedSet<String> relevantTargets = new TreeSet<>(Collator.getInstance());
        Iterator<TargetLister.Target> it = allTargets.iterator();
        while (it.hasNext()) {
            TargetLister.Target target = it.next();
            if (!target.isOverridden() && !target.isInternal()) {
                relevantTargets.add(target.getName());
                if (defaultTarget == null && target.isDefault()) {
                    defaultTarget = target.getName();
                }
            }
        }
        targetComboBox.setModel(new DefaultComboBoxModel(relevantTargets.toArray()));
        if (initialTargets != null) {
            targetComboBox.setSelectedItem(initialTargets);
        } else {
            targetComboBox.setSelectedItem(defaultTarget);
        }
        // Initialize description field:
        targetComboBoxActionPerformed(null);
        String initialProperties = (String) script.getAttribute(ATTR_PROPERTIES);
        if (initialProperties == null) {
            Properties props = new Properties();
            props.putAll(AntSettings.getProperties());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                props.store(baos, null);
                String text = baos.toString("ISO-8859-1"); // NOI18N
                // Strip the annoying initial comment:
                initialProperties = text.replaceFirst("^#.*\n", ""); // NOI18N
            } catch (IOException e) {
                assert false : e;
            }
        }
        propertiesPane.setText(initialProperties);
        Integer verbosity = (Integer) script.getAttribute(ATTR_VERBOSITY);
        if (verbosity == null) {
            verbosity = new Integer(AntSettings.getVerbosity());
        }
        verbosityComboBox.setModel(new DefaultComboBoxModel(VERBOSITIES));
        for (int i = 0; i < VERBOSITY_LEVELS.length; i++) {
            if (VERBOSITY_LEVELS[i] == verbosity.intValue()) {
                verbosityComboBox.setSelectedItem(VERBOSITIES[i]);
                break;
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        targetLabel = new javax.swing.JLabel();
        targetComboBox = new javax.swing.JComboBox();
        targetDescriptionLabel = new javax.swing.JLabel();
        targetDescriptionField = new javax.swing.JTextField();
        propertiesLabel = new javax.swing.JLabel();
        propertiesScrollPane = new javax.swing.JScrollPane();
        propertiesPane = new javax.swing.JEditorPane();
        verbosityLabel = new javax.swing.JLabel();
        verbosityComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        targetLabel.setText("Select target(s) to run:");
        targetLabel.setLabelFor(targetComboBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(targetLabel, gridBagConstraints);

        targetComboBox.setEditable(true);
        targetComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "sampleTarget1", "sampleTarget2", "sampleTarget3" }));
        targetComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(targetComboBox, gridBagConstraints);

        targetDescriptionLabel.setText("Target description:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(targetDescriptionLabel, gridBagConstraints);

        targetDescriptionField.setEditable(false);
        targetDescriptionField.setText("Sample description here.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(targetDescriptionField, gridBagConstraints);

        propertiesLabel.setText("Special Ant properties:");
        propertiesLabel.setLabelFor(propertiesPane);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        add(propertiesLabel, gridBagConstraints);

        propertiesScrollPane.setPreferredSize(new java.awt.Dimension(400, 150));
        propertiesScrollPane.setMinimumSize(new java.awt.Dimension(400, 150));
        propertiesPane.setText("# This is sample text for GUI design.\nsomeprop1=someval1\nsomeprop2=someval2\nsomeprop3=someval3\n");
        propertiesPane.setContentType("text/x-properties");
        propertiesScrollPane.setViewportView(propertiesPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(propertiesScrollPane, gridBagConstraints);

        verbosityLabel.setText("Verbosity level:");
        verbosityLabel.setLabelFor(verbosityComboBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(verbosityLabel, gridBagConstraints);

        verbosityComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Errors only [SAMPLE]", "Normal [SAMPLE]", "Verbose [SAMPLE]" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(verbosityComboBox, gridBagConstraints);

    }//GEN-END:initComponents

    private void targetComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetComboBoxActionPerformed
        String selection = (String) targetComboBox.getSelectedItem();
        if (selection == null) {
            // Why? Not sure. #45097.
            selection = "";
        }
        StringTokenizer tok = new StringTokenizer(selection, " ,"); // NOI18N
        List/*<String>*/ targetsL = Collections.list(tok);
        String description = "";
        if (targetsL.size() == 1) {
            String targetName = (String) targetsL.get(0);
            Iterator<TargetLister.Target> it = allTargets.iterator();
            while (it.hasNext()) {
                TargetLister.Target target = it.next();
                if (!target.isOverridden() && target.getName().equals(targetName)) {
                    description = target.getElement().getAttribute("description"); // NOI18N
                    // may still be "" if not defined
                    break;
                }
            }
        }
        targetDescriptionField.setText(description);
    }//GEN-LAST:event_targetComboBoxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel propertiesLabel;
    private javax.swing.JEditorPane propertiesPane;
    private javax.swing.JScrollPane propertiesScrollPane;
    private javax.swing.JComboBox targetComboBox;
    private javax.swing.JTextField targetDescriptionField;
    private javax.swing.JLabel targetDescriptionLabel;
    private javax.swing.JLabel targetLabel;
    private javax.swing.JComboBox verbosityComboBox;
    private javax.swing.JLabel verbosityLabel;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Try to run the selected target(s).
     */
    public void run() throws IOException {
        // Read settings from the dialog.
        String[] targets;
        String selected = (String) targetComboBox.getSelectedItem();
        if (selected == null) {
            // Run default target.
            targets = null;
        } else {
            StringTokenizer tok = new StringTokenizer(selected, " ,"); // NOI18N
            List/*<String>*/ targetsL = Collections.list(tok);
            if (targetsL.isEmpty()) {
                // Run default target.
                targets = null;
            } else {
                targets = (String[]) targetsL.toArray(new String[0]);
            }
        }
        Properties props = new Properties();
        ByteArrayInputStream bais = new ByteArrayInputStream(propertiesPane.getText().getBytes(StandardCharsets.ISO_8859_1));
        props.load(bais);
        int verbosity = 2;
        String verbosityString = (String) verbosityComboBox.getSelectedItem();
        for (int i = 0; i < VERBOSITIES.length; i++) {
            if (VERBOSITIES[i].equals(verbosityString)) {
                verbosity = VERBOSITY_LEVELS[i];
                break;
            }
        }
        // Remember these settings for next time.
        // Wherever the values used match the default, remove the attribute.
        FileObject script = project.getFileObject();
        assert script != null;
        if (targets == null || (targets.length == 1 && targets[0].equals(defaultTarget)) || targets.length == 0) {
            script.setAttribute(ATTR_TARGETS, null);
        } else {
            StringBuilder targetsSpaceSep = new StringBuilder();
            for (int i = 0; i < targets.length; i++) {
                if (i > 0) {
                    targetsSpaceSep.append(' ');
                }
                targetsSpaceSep.append(targets[i]);
            }
            script.setAttribute(ATTR_TARGETS, targetsSpaceSep.toString());
        }
        if (props.equals(AntSettings.getProperties())) {
            script.setAttribute(ATTR_PROPERTIES, null);
        } else {
            script.setAttribute(ATTR_PROPERTIES, propertiesPane.getText());
        }
        if (verbosity == AntSettings.getVerbosity()) {
            script.setAttribute(ATTR_VERBOSITY, null);
        } else {
            script.setAttribute(ATTR_VERBOSITY, new Integer(verbosity));
        }
        // Actually run the target(s).
        DebuggerAntLogger.getDefault ().debugFile (project.getFile ());
        AntTargetExecutor.Env env = new AntTargetExecutor.Env ();
        env.setProperties(props);
        env.setVerbosity(verbosity);
        AntTargetExecutor executor = AntTargetExecutor.createTargetExecutor(env);
        ExecutorTask executorTask = executor.execute(project, targets);
        DebuggerAntLogger.getDefault().fileExecutor(project.getFile(), executorTask);
    }
    
}
