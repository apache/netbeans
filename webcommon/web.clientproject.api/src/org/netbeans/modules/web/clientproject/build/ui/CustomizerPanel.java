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
package org.netbeans.modules.web.clientproject.build.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.web.clientproject.api.build.BuildTools.CustomizerSupport;
import org.netbeans.modules.web.clientproject.spi.build.CustomizerPanelImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

public final class CustomizerPanel extends JPanel implements ChangeListener {

    private final ProjectCustomizer.Category category;
    private final CustomizerSupport customizerSupport;
    @NullAllowed
    private final CustomizerPanelImplementation customizerPanel;
    private final List<BuildTask> buildTasks = new CopyOnWriteArrayList<>();


    public CustomizerPanel(CustomizerSupport customizerSupport) {
        assert EventQueue.isDispatchThread();
        assert customizerSupport != null;

        this.customizerSupport = customizerSupport;
        category = customizerSupport.getCategory();
        customizerPanel = customizerSupport.getCustomizerPanel();

        initComponents();
        init();
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE",
            justification = "It is SPI, so no real control over the returned value")
    private void init() {
        assignLabel.setText(customizerSupport.getHeader());
        buildTasks.add(new BuildTask(ActionProvider.COMMAND_BUILD, "build", buildCheckBox, buildTextField)); // NOI18N
        buildTasks.add(new BuildTask(ActionProvider.COMMAND_CLEAN, "clean", cleanCheckBox, cleanTextField)); // NOI18N
        buildTasks.add(new BuildTask(ActionProvider.COMMAND_REBUILD, "clean build", rebuildCheckBox, rebuildTextField)); // NOI18N
        buildTasks.add(new BuildTask(ActionProvider.COMMAND_RUN, "run", runProjectCheckBox, runProjectTextField)); // NOI18N
        buildTasks.add(new BuildTask(ActionProvider.COMMAND_DEBUG, "debug", debugProjectCheckBox, debugProjectTextField)); // NOI18N
        buildTasks.add(new BuildTask(ActionProvider.COMMAND_TEST, "test", testProjectCheckBox, testProjectTextField)); // NOI18N
        buildTasks.add(new BuildTask(ActionProvider.COMMAND_RUN_SINGLE, "runfile", runFileCheckBox, runFileTextField)); // NOI18N
        buildTasks.add(new BuildTask(ActionProvider.COMMAND_DEBUG_SINGLE, "debugfile", debugFileCheckBox, debugFileTextField)); // NOI18N
        buildTasks.add(new BuildTask(ActionProvider.COMMAND_TEST_SINGLE, "testfile", testFileCheckBox, testFileTextField)); // NOI18N
        // default values
        for (BuildTask buildTask : buildTasks) {
            buildTask.setText(customizerSupport.getTask(buildTask.getCommandId()));
        }
        // panel
        if (customizerPanel != null) {
            JComponent component = customizerPanel.getComponent();
            assert component != null : "Non-null component must be returned for panel of " + category.getDisplayName();
            extenderPanel.add(component, BorderLayout.CENTER);
            extenderPanel.revalidate();
            extenderPanel.repaint();
        }
        // listeners
        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveData();
            }
        });
        if (customizerPanel != null) {
            customizerPanel.addChangeListener(this);
        }
    }

    @NbBundle.Messages("CustomizerPanel.error.field.empty=Field cannot be empty")
    void validateData() {
        assert EventQueue.isDispatchThread();
        for (BuildTask buildTask : buildTasks) {
            String text = buildTask.getText();
            if (text != null
                    && text.isEmpty()) {
                category.setErrorMessage(Bundle.CustomizerPanel_error_field_empty());
                category.setValid(false);
                return;
            }
        }
        String message  = " "; // NOI18N
        if (customizerPanel != null) {
            if (!customizerPanel.isValid()) {
                String error = customizerPanel.getErrorMessage();
                assert error != null : "Non-null error message must be returned for invalid panel of " + category.getDisplayName();
                category.setErrorMessage(error);
                category.setValid(false);
                return;
            }
            String warning = customizerPanel.getWarningMessage();
            if (warning != null) {
                message = warning;
            }
        }
        category.setErrorMessage(message);
        category.setValid(true);
    }

    void saveData() {
        assert !EventQueue.isDispatchThread();
        for (BuildTask buildTask : buildTasks) {
            customizerSupport.setTask(buildTask.getCommandId(), buildTask.getText());
        }
        if (customizerPanel != null) {
            customizerPanel.save();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        validateData();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        assignLabel = new JLabel();
        buildCheckBox = new JCheckBox();
        buildTextField = new JTextField();
        cleanCheckBox = new JCheckBox();
        cleanTextField = new JTextField();
        rebuildCheckBox = new JCheckBox();
        rebuildTextField = new JTextField();
        runProjectCheckBox = new JCheckBox();
        runProjectTextField = new JTextField();
        debugProjectCheckBox = new JCheckBox();
        debugProjectTextField = new JTextField();
        testProjectCheckBox = new JCheckBox();
        testProjectTextField = new JTextField();
        runFileCheckBox = new JCheckBox();
        runFileTextField = new JTextField();
        debugFileCheckBox = new JCheckBox();
        debugFileTextField = new JTextField();
        testFileCheckBox = new JCheckBox();
        testFileTextField = new JTextField();
        extenderPanel = new JPanel();

        Mnemonics.setLocalizedText(assignLabel, "TITLE"); // NOI18N

        Mnemonics.setLocalizedText(buildCheckBox, NbBundle.getMessage(CustomizerPanel.class, "CustomizerPanel.buildCheckBox.text")); // NOI18N

        buildTextField.setColumns(20);

        Mnemonics.setLocalizedText(cleanCheckBox, NbBundle.getMessage(CustomizerPanel.class, "CustomizerPanel.cleanCheckBox.text")); // NOI18N

        cleanTextField.setColumns(20);

        Mnemonics.setLocalizedText(rebuildCheckBox, NbBundle.getMessage(CustomizerPanel.class, "CustomizerPanel.rebuildCheckBox.text")); // NOI18N

        rebuildTextField.setColumns(20);

        Mnemonics.setLocalizedText(runProjectCheckBox, NbBundle.getMessage(CustomizerPanel.class, "CustomizerPanel.runProjectCheckBox.text")); // NOI18N

        runProjectTextField.setColumns(20);

        Mnemonics.setLocalizedText(debugProjectCheckBox, NbBundle.getMessage(CustomizerPanel.class, "CustomizerPanel.debugProjectCheckBox.text")); // NOI18N

        debugProjectTextField.setColumns(20);

        Mnemonics.setLocalizedText(testProjectCheckBox, NbBundle.getMessage(CustomizerPanel.class, "CustomizerPanel.testProjectCheckBox.text")); // NOI18N

        testProjectTextField.setColumns(20);

        Mnemonics.setLocalizedText(runFileCheckBox, NbBundle.getMessage(CustomizerPanel.class, "CustomizerPanel.runFileCheckBox.text")); // NOI18N

        runFileTextField.setColumns(20);

        Mnemonics.setLocalizedText(debugFileCheckBox, NbBundle.getMessage(CustomizerPanel.class, "CustomizerPanel.debugFileCheckBox.text")); // NOI18N

        debugFileTextField.setColumns(20);

        Mnemonics.setLocalizedText(testFileCheckBox, NbBundle.getMessage(CustomizerPanel.class, "CustomizerPanel.testFileCheckBox.text")); // NOI18N

        testFileTextField.setColumns(20);

        extenderPanel.setLayout(new BorderLayout());

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(cleanCheckBox)
                    .addComponent(rebuildCheckBox)
                    .addComponent(buildCheckBox)
                    .addComponent(runProjectCheckBox)
                    .addComponent(debugProjectCheckBox)
                    .addComponent(testProjectCheckBox)
                    .addComponent(runFileCheckBox)
                    .addComponent(debugFileCheckBox)
                    .addComponent(testFileCheckBox))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(runFileTextField, GroupLayout.Alignment.TRAILING)
                    .addComponent(rebuildTextField, GroupLayout.Alignment.TRAILING)
                    .addComponent(cleanTextField, GroupLayout.Alignment.TRAILING)
                    .addComponent(buildTextField, GroupLayout.Alignment.TRAILING)
                    .addComponent(runProjectTextField, GroupLayout.Alignment.TRAILING)
                    .addComponent(debugProjectTextField, GroupLayout.Alignment.TRAILING)
                    .addComponent(testProjectTextField)
                    .addComponent(debugFileTextField, GroupLayout.Alignment.TRAILING)
                    .addComponent(testFileTextField, GroupLayout.Alignment.TRAILING)))
            .addGroup(layout.createSequentialGroup()
                .addComponent(assignLabel)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(extenderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(assignLabel)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(buildCheckBox)
                    .addComponent(buildTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(cleanCheckBox)
                    .addComponent(cleanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(rebuildCheckBox)
                    .addComponent(rebuildTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(runProjectCheckBox)
                    .addComponent(runProjectTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(debugProjectCheckBox)
                    .addComponent(debugProjectTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(testProjectCheckBox)
                    .addComponent(testProjectTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(runFileCheckBox)
                    .addComponent(runFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(debugFileCheckBox)
                    .addComponent(debugFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(testFileCheckBox)
                    .addComponent(testFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(extenderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel assignLabel;
    private JCheckBox buildCheckBox;
    private JTextField buildTextField;
    private JCheckBox cleanCheckBox;
    private JTextField cleanTextField;
    private JCheckBox debugFileCheckBox;
    private JTextField debugFileTextField;
    private JCheckBox debugProjectCheckBox;
    private JTextField debugProjectTextField;
    private JPanel extenderPanel;
    private JCheckBox rebuildCheckBox;
    private JTextField rebuildTextField;
    private JCheckBox runFileCheckBox;
    private JTextField runFileTextField;
    private JCheckBox runProjectCheckBox;
    private JTextField runProjectTextField;
    private JCheckBox testFileCheckBox;
    private JTextField testFileTextField;
    private JCheckBox testProjectCheckBox;
    private JTextField testProjectTextField;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class BuildTask {

        private final String commandId;
        private final String defaultValue;
        private final JCheckBox checkBox;
        private final JTextField textField;


        BuildTask(String commandId, String defaultValue, JCheckBox checkBox, JTextField textField) {
            assert commandId != null;
            assert defaultValue != null;
            assert checkBox != null;
            assert textField != null;
            this.commandId = commandId;
            this.defaultValue = defaultValue;
            this.checkBox = checkBox;
            this.textField = textField;
            init();
        }

        private void init() {
            checkBox.addItemListener(new DefaultItemListener(textField));
            textField.getDocument().addDocumentListener(new DefaultDocumentListener());
        }

        public String getCommandId() {
            return commandId;
        }

        @CheckForNull
        public String getText() {
            if (!checkBox.isSelected()) {
                return null;
            }
            return textField.getText().trim();
        }

        public void setText(@NullAllowed String text) {
            boolean hasText = text != null;
            checkBox.setSelected(hasText);
            textField.setText(hasText ? text : defaultValue);
            textField.setEnabled(hasText);
        }

    }

    private final class DefaultItemListener implements ItemListener {

        private final JTextField textField;


        public DefaultItemListener(JTextField textField) {
            assert textField != null;
            this.textField = textField;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            textField.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            validateData();
        }

    }

    private final class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processChange();
        }

        private void processChange() {
            validateData();
        }

    }

}
