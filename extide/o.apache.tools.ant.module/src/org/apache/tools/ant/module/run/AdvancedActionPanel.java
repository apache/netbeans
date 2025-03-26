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

package org.apache.tools.ant.module.run;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.EditorKit;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.Parameters;

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

    /** String to used to replace password (concealed) property values */
    private static final String PASSWORD_REPLACEMENT = "*****";    //NOI18N
    
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
    private Set<? extends String> antConcealedProperties = Collections.<String>emptySet();
    private Properties antProperties = new Properties();
    
    public AdvancedActionPanel(AntProjectCookie project, Set<TargetLister.Target> allTargets) {
        this.project = project;
        this.allTargets = allTargets;
        initComponents();
        
        getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(AdvancedActionPanel.class,"AdvancedActionsPanel.acsd.title"));
        
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
        SortedSet<String> relevantTargets = new TreeSet<String>(Collator.getInstance());
        for (TargetLister.Target target : allTargets) {
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
        if (initialProperties != null) {
            try {
                antProperties = parseProperties(initialProperties);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        } else {
            antProperties = new Properties();
            antProperties.putAll(AntSettings.getProperties());
        }
        propertiesPane.setText(propertiesToString(antProperties, antConcealedProperties));
        Integer verbosity = (Integer) script.getAttribute(ATTR_VERBOSITY);
        if (verbosity == null) {
            verbosity = AntSettings.getVerbosity();
        }
        verbosityComboBox.setModel(new DefaultComboBoxModel(VERBOSITIES));
        setVerbosity(verbosity);
    }

    private String propertiesToString(
            @NonNull final Properties props,
            @NonNull final Set<? extends String> concealedProperties) {
        final Properties newProps = new Properties();
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            final Object key = e.getKey();
            Object value = e.getValue();
            if (concealedProperties.contains(key)) {
                value = PASSWORD_REPLACEMENT;
            }
            newProps.put(key,value);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            newProps.store(baos, null);
            String text = baos.toString("ISO-8859-1"); // NOI18N
            // Strip the annoying initial comment:
            return text.replaceFirst("^#.*\n", ""); // NOI18N
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public void setTargets(List<String> targetNames) {
        StringBuilder targets = new StringBuilder();
        if (targetNames != null) {
            for (String target : targetNames) {
                if (targets.length() > 0) {
                    targets.append(' ');
                }
                targets.append(target);
            }
        }
        targetComboBox.setSelectedItem(targets.toString());

    }

    public void setVerbosity(int verbosity) {
        for (int i = 0; i < VERBOSITY_LEVELS.length; i++) {
            if (VERBOSITY_LEVELS[i] == verbosity) {
                verbosityComboBox.setSelectedItem(VERBOSITIES[i]);
                break;
            }
        }
    }

    public void setConcealedProperties(@NonNull final Set<? extends String> concealedProperties) {
        Parameters.notNull("concealedProperties", concealedProperties); //NOI18N
        antConcealedProperties = new HashSet<String>(concealedProperties);
        propertiesPane.setText(propertiesToString(antProperties, antConcealedProperties));
    }

    public void setProperties(Map<String,String> properties) {
        antProperties = new Properties();
        antProperties.putAll(properties);
        propertiesPane.setText(propertiesToString(antProperties, antConcealedProperties));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        targetLabel = new javax.swing.JLabel();
        targetComboBox = new javax.swing.JComboBox();
        targetDescriptionLabel = new javax.swing.JLabel();
        targetDescriptionField = new javax.swing.JTextField();
        propertiesLabel = new javax.swing.JLabel();
        propertiesScrollPane = new javax.swing.JScrollPane();
        propertiesPane = new javax.swing.JEditorPane();
        verbosityLabel = new javax.swing.JLabel();
        verbosityComboBox = new javax.swing.JComboBox();

        targetLabel.setLabelFor(targetComboBox);
        targetLabel.setText("Select target(s) to run:");

        targetComboBox.setEditable(true);
        targetComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "sampleTarget1", "sampleTarget2", "sampleTarget3" }));
        targetComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetComboBoxActionPerformed(evt);
            }
        });

        targetDescriptionLabel.setLabelFor(targetDescriptionField);
        targetDescriptionLabel.setText("Target description:");

        targetDescriptionField.setEditable(false);
        targetDescriptionField.setText("Sample description here.");

        propertiesLabel.setLabelFor(propertiesPane);
        propertiesLabel.setText("Special Ant properties:");

        propertiesScrollPane.setMinimumSize(new java.awt.Dimension(400, 150));
        propertiesScrollPane.setPreferredSize(new java.awt.Dimension(400, 150));

        propertiesPane.setContentType("text/x-properties");
        propertiesScrollPane.setViewportView(propertiesPane);

        verbosityLabel.setLabelFor(verbosityComboBox);
        verbosityLabel.setText("Verbosity level:");

        verbosityComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Errors only [SAMPLE]", "Normal [SAMPLE]", "Verbose [SAMPLE]" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(verbosityLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(propertiesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(targetDescriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(targetLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(targetDescriptionField, javax.swing.GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE)
                    .addComponent(targetComboBox, 0, 624, Short.MAX_VALUE)
                    .addComponent(propertiesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE)
                    .addComponent(verbosityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetLabel)
                    .addComponent(targetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetDescriptionLabel)
                    .addComponent(targetDescriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(propertiesLabel)
                    .addComponent(propertiesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(verbosityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(verbosityLabel)))
                .addContainerGap())
        );

        targetComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedActionPanel.class, "ACS_SelectTarget")); // NOI18N
        targetComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedActionPanel.class, "ACSD_SelectTarget")); // NOI18N
        targetDescriptionField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedActionPanel.class, "ACSD_TargetDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void targetComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetComboBoxActionPerformed
        String selection = (String) targetComboBox.getSelectedItem();
        if (selection == null) {
            // Why? Not sure. #45097.
            selection = "";
        }
        StringTokenizer tok = new StringTokenizer(selection, " ,"); // NOI18N
        List<String> targetsL = Collections.list(NbCollections.checkedEnumerationByFilter(tok, String.class, true));
        String description = "";
        if (targetsL.size() == 1) {
            String targetName = targetsL.get(0);
            for (TargetLister.Target target : allTargets) {
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
    private void run() throws IOException {
        // Read settings from the dialog.
        String selection = (String) targetComboBox.getSelectedItem();
        String[] targets = null; // default target unless otherwise specified
        if (selection != null) {
            StringTokenizer tok = new StringTokenizer(selection, " ,"); // NOI18N
            List<String> targetsL = Collections.list(NbCollections.checkedEnumerationByFilter(tok, String.class, true));
            if (!targetsL.isEmpty()) {
                targets = targetsL.toArray(new String[0]);
            }
        }
        Properties props = parseProperties(propertiesPane.getText());
        for (Map.Entry<Object,Object> e : props.entrySet()) {
            final Object key = e.getKey();
            final Object value = e.getValue();
            if (antConcealedProperties.contains(key) &&
                PASSWORD_REPLACEMENT.equals(value)) {
                e.setValue(antProperties.get(key));
            }
        }
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
        if (targets == null || (targets.length == 1 && targets[0].equals(defaultTarget))) {
            script.setAttribute(ATTR_TARGETS, null);
        } else {
            StringBuffer targetsSpaceSep = new StringBuffer();
            for (int i = 0; i < targets.length; i++) {
                if (i > 0) {
                    targetsSpaceSep.append(' ');
                }
                targetsSpaceSep.append(targets[i]);
            }
            script.setAttribute(ATTR_TARGETS, targetsSpaceSep.toString());
        }
        if (((Map) props).equals(AntSettings.getProperties())) {
            script.setAttribute(ATTR_PROPERTIES, null);
        } else {
            script.setAttribute(ATTR_PROPERTIES, propertiesPane.getText());
        }
        if (verbosity == AntSettings.getVerbosity()) {
            script.setAttribute(ATTR_VERBOSITY, null);
        } else {
            script.setAttribute(ATTR_VERBOSITY, verbosity);
        }
        // Actually run the target(s).
        TargetExecutor exec = new TargetExecutor(project, targets);
        exec.setProperties(NbCollections.checkedMapByCopy(props, String.class, String.class, true));
        exec.setConcealedProperties(antConcealedProperties);
        exec.setVerbosity(verbosity);
        exec.execute();
    }

    /** Displays dialog. */
    public boolean display() {
        String title = NbBundle.getMessage(RunTargetsAction.class, "TITLE_run_advanced");
        DialogDescriptor dd = new DialogDescriptor(this, title);
        dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        JButton run = new JButton(NbBundle.getMessage(RunTargetsAction.class, "LBL_run_advanced_run"));
        run.setDefaultCapable(true);
        JButton cancel = new JButton(NbBundle.getMessage(RunTargetsAction.class, "LBL_run_advanced_cancel"));
        dd.setOptions(new Object[] {run, cancel});
        dd.setModal(true);
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result.equals(run)) {
            try {
                run();
                return true;
            } catch (IOException x) {
                AntModule.err.notify(x);
            }
        }
        return false;
    }

    @NonNull
    private static Properties parseProperties(@NonNull final String text) throws IOException {
        final Properties props = new Properties();
        final ByteArrayInputStream bais = new ByteArrayInputStream(text.getBytes(StandardCharsets.ISO_8859_1));
        props.load(bais);
        return props;
    }
    
}
