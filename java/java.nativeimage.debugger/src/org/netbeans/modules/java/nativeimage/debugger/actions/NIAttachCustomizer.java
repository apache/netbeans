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
package org.netbeans.modules.java.nativeimage.debugger.actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.netbeans.api.debugger.Properties;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.nativeimage.debugger.actions.Processes.ProcessInfo;
import org.netbeans.modules.java.nativeimage.debugger.api.NIDebugRunner;
import org.netbeans.modules.nativeimage.api.debug.StartDebugParameters;
import org.netbeans.spi.debugger.ui.Controller;
import static org.netbeans.spi.debugger.ui.Controller.PROP_VALID;
import org.netbeans.spi.debugger.ui.PersistentController;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author martin
 */
public class NIAttachCustomizer extends javax.swing.JPanel {

    private final ConnectController controller;
    private final ValidityDocumentListener validityDocumentListener = new ValidityDocumentListener();
    private final RequestProcessor RP = new RequestProcessor(NIAttachCustomizer.class.getName(), 2);
    private ProcessInfo processInfo = null;

    /**
     * Creates new form NIAttachCustomizer
     */
    public NIAttachCustomizer() {
        controller = new ConnectController();
        initComponents();
        fileTextField.getDocument().addDocumentListener(validityDocumentListener);
        initNIFile();
        initProcesses();
    }

    private void initNIFile() {
        RP.post(() -> {
            FileObject currentFO = Utilities.actionsGlobalContext().lookup(FileObject.class);
            if (currentFO != null) {
                File currentFile = FileUtil.toFile(currentFO);
                String path;
                if (currentFile != null && currentFile.canExecute()) {
                    path = currentFile.getAbsolutePath();
                } else {
                    Project project = FileOwnerQuery.getOwner(currentFO);
                    if (project != null) {
                        currentFO = project.getProjectDirectory();
                        currentFile = FileUtil.toFile(currentFO);
                        path = currentFile.getAbsolutePath();
                    } else {
                        path = null;
                    }
                }
                if (path != null) {
                    SwingUtilities.invokeLater(() -> {
                        fileTextField.setText(path);
                    });
                }
            }
        });
    }

    private void initProcesses() {
        processTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final int spacing = 8;
        processTable.setIntercellSpacing(new Dimension(spacing, 0));
        Mnemonics.setLocalizedText(attachLabel, org.openide.util.NbBundle.getMessage(NIAttachCustomizer.class, "NIAttachCustomizer.attachLabel.text", 0l)); // NOI18N
        RP.post(() -> {
            List<ProcessInfo> processes = Processes.getAllProcesses();
            int size = processes.size();
            Object[][] processValues = new Object[size][];
            for (int i = 0; i < size; i++) {
                ProcessInfo info = processes.get(i);
                processValues[i] = new Object[] {
                    info.getPid(),
                    info.getCommand()
                };
            }
            SwingUtilities.invokeLater(() -> {
                processTable.setModel(new javax.swing.table.DefaultTableModel(
                    processValues,
                    new String [] {
                        NbBundle.getMessage(NIAttachCustomizer.class, "NIAttachCustomizer.processPID.text"), // NOI18N
                        NbBundle.getMessage(NIAttachCustomizer.class, "NIAttachCustomizer.processCommand.text"), // NOI18N
                    }
                ) {
                    Class<?>[] types = new Class<?>[] {
                        Long.class, String.class
                    };

                    @Override
                    public Class<?> getColumnClass(int columnIndex) {
                        return types[columnIndex];
                    }

                    @Override
                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return false;
                    }
                });
                if (!processes.isEmpty()) {
                    int width = processTable.getGraphics().getFontMetrics().stringWidth(Long.toString(processes.get(0).getPid()));
                    width += 2*spacing;
                    processTable.getColumnModel().getColumn(0).setPreferredWidth(width);
                    processTable.getColumnModel().getColumn(0).setMaxWidth(width);
                    processTable.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
                        int index = processTable.getSelectedRow();
                        if (index < 0) {
                            Mnemonics.setLocalizedText(attachLabel, NbBundle.getMessage(NIAttachCustomizer.class, "NIAttachCustomizer.attachLabel.text", 0l)); // NOI18N
                            processInfo = null;
                        } else {
                            ProcessInfo info = processes.get(index);
                            Mnemonics.setLocalizedText(attachLabel, NbBundle.getMessage(NIAttachCustomizer.class, "NIAttachCustomizer.attachLabel.text", info.getPid())); // NOI18N
                            fileTextField.setText(info.getExecutable());
                            processInfo = info;
                        }
                    });
                }
            });
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        fileButton = new javax.swing.JButton();
        dbgLabel = new javax.swing.JLabel();
        dbgTextField = new javax.swing.JTextField();
        attachLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        processTable = new javax.swing.JTable();

        fileLabel.setLabelFor(fileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fileLabel, org.openide.util.NbBundle.getMessage(NIAttachCustomizer.class, "NIAttachCustomizer.fileLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fileButton, org.openide.util.NbBundle.getMessage(NIAttachCustomizer.class, "NIAttachCustomizer.fileButton.text")); // NOI18N
        fileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileButtonActionPerformed(evt);
            }
        });

        dbgLabel.setLabelFor(dbgTextField);
        org.openide.awt.Mnemonics.setLocalizedText(dbgLabel, org.openide.util.NbBundle.getMessage(NIAttachCustomizer.class, "NIAttachCustomizer.dbgLabel.text")); // NOI18N

        dbgTextField.setText("gdb");

        attachLabel.setLabelFor(processTable);
        org.openide.awt.Mnemonics.setLocalizedText(attachLabel, org.openide.util.NbBundle.getMessage(NIAttachCustomizer.class, "NIAttachCustomizer.attachLabel.text")); // NOI18N

        jScrollPane1.setViewportView(processTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fileLabel)
                            .addComponent(dbgLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dbgTextField)
                            .addComponent(fileTextField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(attachLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileLabel)
                    .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dbgLabel)
                    .addComponent(dbgTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(attachLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages({"CTL_ExecutableFiles=Executable Files"})
    private void fileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileButtonActionPerformed
        JFileChooser chooser = new JFileChooser(fileTextField.getText());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.canExecute();
            }

            @Override
            public String getDescription() {
                return Bundle.CTL_ExecutableFiles();
            }
        });
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_fileButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel attachLabel;
    private javax.swing.JLabel dbgLabel;
    private javax.swing.JTextField dbgTextField;
    private javax.swing.JButton fileButton;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable processTable;
    // End of variables declaration//GEN-END:variables

    RequestProcessor.Task validationTask = new RequestProcessor(NIAttachCustomizer.class).create(new FileValidationTask());

    @NbBundle.Messages({"MSG_NoFile=Native Imige File is missing."})
    private void checkValid() {
        assert SwingUtilities.isEventDispatchThread() : "Called outside of AWT.";
        if (fileTextField.getText().isEmpty()) {
            controller.setInformationMessage(Bundle.MSG_NoFile());
            controller.setValid(false);
            return ;
        }
        validationTask.schedule(200);
    }

    private class FileValidationTask implements Runnable {

        @Override
        public void run() {
            String filePath = fileTextField.getText();
            File file = new File(filePath);
            boolean canExecute = file.isFile() && file.canExecute();
            SwingUtilities.invokeLater(() -> {
                controller.setValid(canExecute);
                String message = canExecute ? null : Bundle.MSG_NoFile();
                controller.setInformationMessage(message);
            });
        }
    }

    private class ValidityDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            checkValid();
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
            checkValid();
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
            checkValid();
        }
    }

    Controller getController() {
        return controller;
    }

    private static final String CPPLITE_DEBUGGER = "org.netbeans.modules.cpplite.debugger"; // NOI18N

    @NbBundle.Messages({"# {0} - Name of the module that needs to be enabled.",
                        "MSG_EnableNativeDebugger=Enable {0} in Plugins Manager",
                        "TTL_EnableNativeDebugger=Native Debugger Dependency"})
    private static boolean checkCPPLite() {
        ModuleInfo cppliteDebugger = Modules.getDefault().findCodeNameBase(CPPLITE_DEBUGGER);
        if (cppliteDebugger != null && !cppliteDebugger.isEnabled()) {
            Action pluginsManager = Actions.forID("System", "org.netbeans.modules.autoupdate.ui.actions.PluginManagerAction");  // NOI18N
            String moduleDisplayName = cppliteDebugger.getDisplayName();
            NotifyDescriptor messageDescriptor = new NotifyDescriptor.Confirmation(
                    Bundle.MSG_EnableNativeDebugger(moduleDisplayName),
                    Bundle.TTL_EnableNativeDebugger(),
                    NotifyDescriptor.OK_CANCEL_OPTION);
            if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(messageDescriptor))) {
                SwingUtilities.invokeLater(() -> {
                    ActionEvent ev = new ActionEvent(pluginsManager, 100, "installed");
                    pluginsManager.actionPerformed(ev);
                });
            }
            return false;
        }
        return true;
    }

    public class ConnectController implements PersistentController {

        private static final String NI_ATTACH_PROPERTIES = "native_image_attach_settings";  // NOI18N
        private static final String PROP_NI_FILE = "niFile";                                // NOI18N
        private static final String PROP_DBG = "debugger";                                  // NOI18N

        private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private boolean valid = true;

        @Override
        public String getDisplayName() {
            return dbgTextField.getText() + " " + new File(fileTextField.getText()).getName();
        }

        @Override
        public boolean load(Properties props) {
            assert !SwingUtilities.isEventDispatchThread();
            final Properties attachProps = props.getProperties(NI_ATTACH_PROPERTIES);
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        fileTextField.setText(attachProps.getString(PROP_NI_FILE, ""));
                        dbgTextField.setText(attachProps.getString(PROP_DBG, "DBG"));
                    }
                });
            } catch (InterruptedException | InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            return true;
        }

        @Override
        public void save(Properties props) {
            final Properties attachProps = props.getProperties(NI_ATTACH_PROPERTIES);
            if (SwingUtilities.isEventDispatchThread()) {
                saveToProps(attachProps);
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            saveToProps(attachProps);

                        }
                    });
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        private void saveToProps(Properties attachProps) {
            attachProps.setString(PROP_NI_FILE, fileTextField.getText());
            attachProps.setString(PROP_DBG, dbgTextField.getText());
        }

        @Override
        public boolean ok() {
            String filePath = fileTextField.getText();
            String debuggerCommand = dbgTextField.getText();
            ProcessInfo attach2Process = processInfo;
            RP.post(() -> {
                if (!checkCPPLite()) {
                    return ;
                }
                File file = new File(filePath);
                String displayName = COMMAND_DEBUG + " " + file.getName();
                StartDebugParameters startParams = StartDebugParameters.newBuilder(Collections.singletonList(file.getAbsolutePath()))
                        .debugger(debuggerCommand)
                        .debuggerDisplayObjects(false)
                        .displayName(displayName)
                        .processID(attach2Process != null ? attach2Process.getPid() : null)
                        .workingDirectory(new File(System.getProperty("user.dir", ""))) // NOI18N
                        .build();
                NIDebugRunner.start(file, startParams, null, null);
            });
            return true;
        }

        @Override
        public boolean cancel() {
            return true;
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        void setValid(boolean valid) {
            this.valid = valid;
            firePropertyChange(PROP_VALID, !valid, valid);
        }

        void setErrorMessage(String msg) {
            firePropertyChange(NotifyDescriptor.PROP_ERROR_NOTIFICATION, null, msg);
        }

        void setInformationMessage(String msg) {
            firePropertyChange(NotifyDescriptor.PROP_INFO_NOTIFICATION, null, msg);
        }

        private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            pcs.firePropertyChange(propertyName, oldValue, newValue);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

    }

}
