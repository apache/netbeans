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
package org.netbeans.modules.php.dbgp.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.dbgp.breakpoints.LineBreakpoint;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

public class DbgpLineBreakpointCustomizerPanel extends JPanel implements ControllerProvider {

    private static final Logger LOGGER = Logger.getLogger(DbgpLineBreakpointCustomizerPanel.class.getName());

    private static final int MAX_SAVED_CONDITIONS = 10;
    private static final String BP_CONDITIONS = "BPConditions"; // NOI18N
    private static final String DEBUGGER_PHP = "debugger.php"; // NOI18N
    private static final long serialVersionUID = 6364512868561614302L;

    private final LineBreakpoint lineBreakpoint;
    private final CustomizerController controller;
    private boolean createBreakpoint;

    private static LineBreakpoint createLineBreakpoint() {
        Line currentLine = Utils.getCurrentLine();
        return createLineBreakpoint(currentLine);
    }

    private static LineBreakpoint createLineBreakpoint(Line line) {
        if (line != null) {
            return new LineBreakpoint(line);
        }
        return null;
    }

    public DbgpLineBreakpointCustomizerPanel() {
        this(createLineBreakpoint(), true);
        createBreakpoint = true;
    }

    public DbgpLineBreakpointCustomizerPanel(Line line) {
        this(createLineBreakpoint(line), true);
        createBreakpoint = true;
    }

    public DbgpLineBreakpointCustomizerPanel(LineBreakpoint lineBreakpoint) {
        this(lineBreakpoint, false);
    }

    private DbgpLineBreakpointCustomizerPanel(LineBreakpoint lineBreakpoint, boolean isEditable) {
        this.lineBreakpoint = lineBreakpoint;
        controller = createController();
        initComponents();

        DocumentListener defaultDocumentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                processUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                processUpdate();
            }

            private void processUpdate() {
                controller.checkValid();
            }
        };
        fileTextField.setEditable(isEditable);
        fileTextField.getDocument().addDocumentListener(defaultDocumentListener);
        lineNumberTextField.setEditable(isEditable);
        lineNumberTextField.getDocument().addDocumentListener(defaultDocumentListener);
        Object[] conditions = getSavedConditions();
        conditionComboBox.setModel(new DefaultComboBoxModel(conditions));

        if (lineBreakpoint != null) {
            Line line = lineBreakpoint.getLine();
            FileObject fo = line.getLookup().lookup(FileObject.class);
            updateComponents(fo, line.getLineNumber() + 1, lineBreakpoint.getCondition());
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                controller.checkValid();
            }
        });
    }

    private void updateComponents(FileObject fileObject, int lineNumber, String condition) {
        assert SwingUtilities.isEventDispatchThread();
        if (fileObject != null) {
            File file = FileUtil.toFile(fileObject);
            if (file != null) {
                fileTextField.setText(file.getAbsolutePath());
            } else {
                fileTextField.setText(fileObject.toURL().toExternalForm());
            }
        }

        lineNumberTextField.setText(Integer.toString(lineNumber));

        if (condition != null && !condition.isEmpty()) {
            conditionCheckBox.setSelected(true);
            conditionComboBox.setEnabled(true);
            conditionComboBox.getEditor().setItem(condition);
        } else {
            conditionCheckBox.setSelected(false);
            conditionComboBox.setEnabled(false);
        }
    }

    private static Object[] getSavedConditions() {
        return Properties.getDefault()
                .getProperties(DEBUGGER_PHP)
                .getArray(BP_CONDITIONS, new Object[0]);
    }

    private static void saveCondition(String condition) {
        Object[] savedConditions = getSavedConditions();
        Object[] conditions = null;
        boolean containsCondition = false;
        for (int i = 0; i < savedConditions.length; i++) {
            Object c = savedConditions[i];
            if (condition.equals(c)) {
                containsCondition = true;
                conditions = savedConditions;
                if (i > 0) {
                    System.arraycopy(conditions, 0, conditions, 1, i);
                    conditions[0] = condition;
                }
                break;
            }
        }
        if (!containsCondition) {
            if (savedConditions.length < MAX_SAVED_CONDITIONS) {
                conditions = new Object[savedConditions.length + 1];
                conditions[0] = condition;
                System.arraycopy(savedConditions, 0, conditions, 1, savedConditions.length);
            } else {
                conditions = savedConditions;
                System.arraycopy(conditions, 0, conditions, 1, conditions.length - 1);
                conditions[0] = condition;
            }
        }
        Properties.getDefault()
                .getProperties(DEBUGGER_PHP)
                .setArray(BP_CONDITIONS, conditions);
    }

    private CustomizerController createController() {
        return new CustomizerController();
    }

    @Override
    public Controller getController() {
        return controller;
    }

    private static int findNumLines(FileObject file) {
        DataObject dataObject;
        try {
            dataObject = DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Can''t find DataObject for {0}", file.getPath()); // NOI18N
            return 0;
        }
        EditorCookie editortCookie = (EditorCookie) dataObject.getCookie(EditorCookie.class);
        if (editortCookie == null) {
            return 0;
        }
        editortCookie.prepareDocument().waitFinished();
        Document document = editortCookie.getDocument();
        if (!(document instanceof StyledDocument)) {
            return 0;
        }
        StyledDocument styledDocument = (StyledDocument) document;
        //NbDocument.findLineNumber() always returns a line number one less than the actual line number.
        return NbDocument.findLineNumber(styledDocument, styledDocument.getLength()) + 1;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        settingsPanel = new javax.swing.JPanel();
        fileLabel = new javax.swing.JLabel();
        lineNumberLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        lineNumberTextField = new javax.swing.JTextField();
        conditionPanel = new javax.swing.JPanel();
        conditionCheckBox = new javax.swing.JCheckBox();
        conditionComboBox = new javax.swing.JComboBox<>();

        settingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DbgpLineBreakpointCustomizerPanel.class, "LBL_Settings"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fileLabel, org.openide.util.NbBundle.getMessage(DbgpLineBreakpointCustomizerPanel.class, "DbgpLineBreakpointCustomizerPanel.fileLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lineNumberLabel, org.openide.util.NbBundle.getMessage(DbgpLineBreakpointCustomizerPanel.class, "DbgpLineBreakpointCustomizerPanel.lineNumberLabel.text")); // NOI18N

        fileTextField.setText(org.openide.util.NbBundle.getMessage(DbgpLineBreakpointCustomizerPanel.class, "DbgpLineBreakpointCustomizerPanel.fileTextField.text")); // NOI18N

        lineNumberTextField.setText(org.openide.util.NbBundle.getMessage(DbgpLineBreakpointCustomizerPanel.class, "DbgpLineBreakpointCustomizerPanel.lineNumberTextField.text")); // NOI18N

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lineNumberLabel)
                    .addComponent(fileLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fileTextField)
                    .addComponent(lineNumberTextField)))
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileLabel)
                    .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lineNumberLabel)
                    .addComponent(lineNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        conditionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DbgpLineBreakpointCustomizerPanel.class, "LBL_Condition"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(conditionCheckBox, org.openide.util.NbBundle.getMessage(DbgpLineBreakpointCustomizerPanel.class, "DbgpLineBreakpointCustomizerPanel.conditionCheckBox.text")); // NOI18N
        conditionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conditionCheckBoxActionPerformed(evt);
            }
        });

        conditionComboBox.setEditable(true);
        conditionComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(DbgpLineBreakpointCustomizerPanel.class, "DbgpLineBreakpointCustomizerPanel.conditionComboBox.toolTipText")); // NOI18N

        javax.swing.GroupLayout conditionPanelLayout = new javax.swing.GroupLayout(conditionPanel);
        conditionPanel.setLayout(conditionPanelLayout);
        conditionPanelLayout.setHorizontalGroup(
            conditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(conditionPanelLayout.createSequentialGroup()
                .addComponent(conditionCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(conditionComboBox, 0, 281, Short.MAX_VALUE))
        );
        conditionPanelLayout.setVerticalGroup(
            conditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(conditionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(conditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(conditionCheckBox)
                    .addComponent(conditionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(conditionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(settingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(conditionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void conditionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conditionCheckBoxActionPerformed
        conditionComboBox.setEnabled(conditionCheckBox.isSelected());
        if (conditionCheckBox.isSelected()) {
            conditionComboBox.requestFocusInWindow();
        }
    }//GEN-LAST:event_conditionCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox conditionCheckBox;
    private javax.swing.JComboBox<String> conditionComboBox;
    private javax.swing.JPanel conditionPanel;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel lineNumberLabel;
    private javax.swing.JTextField lineNumberTextField;
    private javax.swing.JPanel settingsPanel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes
    private class CustomizerController implements Controller {

        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
        private String errorMessage;
        private volatile boolean valid;

        @Override
        public boolean ok() {
            if (!valid) {
                final String message = getErrorMessage();
                if (message != null) {
                    if (SwingUtilities.isEventDispatchThread()) {
                        showMessageDialog(message);
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            showMessageDialog(message);
                        });
                    }
                }
                return false;
            }

            String condition = null;
            if (conditionCheckBox.isSelected()) {
                condition = conditionComboBox.getSelectedItem().toString().trim();
            }

            if (createBreakpoint) {
                String fileName = fileTextField.getText();
                String lineNumberString = lineNumberTextField.getText();
                if (fileName == null) {
                    return false;
                }
                File file = new File(fileName.trim());
                FileObject fileObject = FileUtil.toFileObject(file);
                if (fileObject == null) {
                    return false;
                }
                Line line = Utils.getLine(fileObject, Integer.parseInt(lineNumberString) - 1);
                LineBreakpoint lb = createLineBreakpoint(line);
                setCondition(lb, condition);
                DebuggerManager.getDebuggerManager().addBreakpoint(lb);
            } else {
                setCondition(lineBreakpoint, condition);
            }

            return true;
        }

        private void setCondition(LineBreakpoint lb, String condition) {
            if (condition != null && !condition.isEmpty()) {
                lb.setCondition(condition);
                saveCondition(condition);
            } else {
                lb.setCondition(null);
            }
        }

        @Override
        public boolean cancel() {
            return true;
        }

        @NbBundle.Messages({
            "CustomizerController.invalid.file=Existing file must be set.",
            "CustomizerController.non.php.file=PHP source file is expected.",
            "CustomizerController.invalid.line=Valid line number must be set.",
            "# {0} - entered line number",
            "# {1} - maximum line number in file",
            "CustomizerController.too.big.line.number=The line number {0} is too big. Maximum line number is {1}."
        })
        public void checkValid() {
            boolean isValid = true;
            // file
            String fileName = fileTextField.getText();
            if (fileName == null || fileName.trim().length() == 0) {
                setErrorMessage(Bundle.CustomizerController_invalid_file());
                setValid(false);
                return;
            }
            File file = new File(fileName.trim());
            if (!file.exists()) {
                setErrorMessage(Bundle.CustomizerController_invalid_file());
                setValid(false);
                return;
            }
            FileObject fileObject = FileUtil.toFileObject(file);
            if (fileObject == null) {
                setErrorMessage(Bundle.CustomizerController_invalid_file());
                setValid(false);
                return;
            } else if (!FileUtils.isPhpFile(fileObject)) {
                setErrorMessage(Bundle.CustomizerController_non_php_file());
                setValid(false);
                return;
            }

            // line number
            String lineNumberString = lineNumberTextField.getText();
            if (lineNumberString == null || lineNumberString.trim().length() == 0) {
                setErrorMessage(Bundle.CustomizerController_invalid_line());
                setValid(false);
                return;
            }
            try {
                int lineNumber = Integer.parseInt(lineNumberTextField.getText());
                if (lineNumber <= 0) {
                    setErrorMessage(Bundle.CustomizerController_invalid_line());
                    setValid(false);
                    return;
                }
                int maxLine = findNumLines(fileObject);
                if (maxLine == 0) { // Not found
                    maxLine = Integer.MAX_VALUE - 1; // Not to bother the user when we did not find it
                }
                if (lineNumber > maxLine) {
                        setErrorMessage(Bundle.CustomizerController_too_big_line_number(Integer.toString(lineNumber), Integer.toString(maxLine)));
                        setValid(false);
                        return;
                }
            } catch (NumberFormatException nfe) {
                setErrorMessage(Bundle.CustomizerController_invalid_line());
                setValid(false);
                isValid = false;
            }

            if (isValid) {
                setErrorMessage(null);
                setValid(true);
            }
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            propertyChangeSupport.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            propertyChangeSupport.removePropertyChangeListener(l);
        }

        void setErrorMessage(String message) {
            errorMessage = message;
            propertyChangeSupport.firePropertyChange(NotifyDescriptor.PROP_ERROR_NOTIFICATION, null, message);
        }

        private void setValid(boolean valid) {
            this.valid = valid;
            propertyChangeSupport.firePropertyChange(PROP_VALID, !valid, valid);
        }

        String getErrorMessage() {
            return errorMessage;
        }

        private void showMessageDialog(String message) {
            NotifyDescriptor descr = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notify(descr);
        }

    }

}
