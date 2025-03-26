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

package org.netbeans.modules.versioning.util;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 * 
 * @author Tomas Stupka
 */
public abstract class ExportDiffSupport {
    private AbstractExportDiffPanel panel;
    private DialogDescriptor dd;
    private Preferences preferences;
    private Dialog dialog;
    private ExportDiffProvider exportDiffProvider;
    private File[] files;

    public ExportDiffSupport(File[] files, final Preferences preferences) {
        this.preferences = preferences;
        this.files = files;
    }

    /**
     * Override this to provide your own top diff dialog.
     * This dialog will be displayed if any registered ExportDiffProvider is found.
     * @param insidePanel an inside panel with diff provider UI components, include this panel inside yor provided top panel
     */
    protected void createComplexDialog (AbstractExportDiffPanel insidePanel) {
        dd = new DialogDescriptor(insidePanel, NbBundle.getMessage(ExportDiffSupport.class, "CTL_Export_Title"));
    }

    /**
     * Override this to provide your own implementation of the simple dialog.
     * This dialog will be displayed if no registered ExportDiffProvider has been found.
     * <strong></strong>
     * @param currentFilePath folder location to save diff into as it is present in user configuration
     * @return simple export file diff panel
     */
    protected AbstractExportDiffPanel createSimpleDialog(final String currentFilePath) {
        dd = new DialogDescriptor(createFileChooser(new File(currentFilePath)), NbBundle.getMessage(ExportDiffSupport.class, "CTL_Export_Title"));
        dd.setOptions(new Object[0]);
        // XXX try better
        panel = new ExportDiffPanel(new JPanel());
        panel.setOutputFileText("");
        return panel;
    }

    /**
     * Override this to provide the descriptor of the top panel.
     * @return the descriptor of the top dialog
     */
    protected DialogDescriptor getDialogDescriptor () {
        return dd;
    }

    private void initializePanels() {
        exportDiffProvider = Lookup.getDefault().lookup(ExportDiffProvider.class);
        String currentFilePath = preferences.get("ExportDiff.saveFolder", System.getProperty("user.home"));
        if(exportDiffProvider == null) {
            panel = createSimpleDialog(currentFilePath);
            dd = getDialogDescriptor();
        } else {
            exportDiffProvider.setContext(files);
            ExportDiffPanel edPanel = new ExportDiffPanel(exportDiffProvider.createComponent());
            edPanel.setOutputFileText(currentFilePath);

            exportDiffProvider.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if(evt.getPropertyName().equals(ExportDiffProvider.EVENT_DATA_CHANGED)) {
                        validate();
                    }
                }
            });
            edPanel.asFileRadioButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { validate(); }
            });
            edPanel.attachRadioButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { validate(); }
            });
            panel = edPanel;
            createComplexDialog(panel);
            dd = getDialogDescriptor();
        }
        panel.addOutputFileTextDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                validate();
            }
            public void removeUpdate(DocumentEvent e) {
                validate();
            }
            public void changedUpdate(DocumentEvent e) {
                validate();
            }
        });
        panel.addBrowseActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onChooseFile(new File(panel.getOutputFileText()));
            }
        });
    }

    private void validate() {
        assert panel != null;
        if (exportDiffProvider == null || panel.isFileOutputSelected()) {
            String path = panel.getOutputFileText().trim();
            File f = new File(panel.getOutputFileText().trim());
            dd.setValid(!path.equals("") && !f.isDirectory());
        } else {
            dd.setValid(exportDiffProvider.isValid());
        }
    }

    public void export() {
        initializePanels();
        validate();

        dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        if(dd.getValue() == DialogDescriptor.OK_OPTION) {
            if(exportDiffProvider == null || ((ExportDiffPanel)panel).asFileRadioButton.isSelected()) {
                final File toFile = new File(panel.getOutputFileText());
                Utils.createTask(new Runnable() {
                    public void run() {
                        writeDiffFile(FileUtil.normalizeFile(toFile));
                    }
                }).schedule(0);
            } else {
                final Task[] t = new Task[1];
                Cancellable c = new Cancellable() {
                    public boolean cancel() {
                        if(t[0] != null) {
                            return t[0].cancel();
                        }
                        return true; 
                    }
                };
                final ProgressHandle handle = ProgressHandleFactory.createHandle(getMessage("CTL_Attaching"), c);
                handle.start();
                t[0] = Utils.createTask(new Runnable() {
                    public void run() {
                        try {
                            File toFile;
                            try {
                                toFile = createTempFile();
                            } catch (IOException ex) {
                                // XXX
                                return;
                            }
                            toFile.deleteOnExit();
                            writeDiffFile(toFile);
                            if (toFile.exists()) {
                                exportDiffProvider.handleDiffFile(toFile);
                            }
                        } finally {
                            handle.finish();
                        }
                    }
                });
                t[0].schedule(0);
            }
        }
    }

    protected File createTempFile () throws IOException {
        return Files.createTempFile("vcs-diff", ".patch").toFile(); // NOI18N
    }

    protected String getMessage (String resourceName) {
        return NbBundle.getMessage(ExportDiffSupport.class, resourceName);
    }

    protected javax.swing.filechooser.FileFilter getFileFilter () {
        return new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith("diff") || f.getName().endsWith("patch") || f.isDirectory();  // NOI18N
            }
            @Override
            public String getDescription() {
                return NbBundle.getMessage(ExportDiffSupport.class, "BK3002");
            }
        };
    }

    protected File getTargetFile (File target) {
        String name = target.getName();
        boolean requiredExt = false;
        requiredExt |= name.endsWith(".diff");  // NOI18N
        requiredExt |= name.endsWith(".dif");   // NOI18N
        requiredExt |= name.endsWith(".patch"); // NOI18N
        if (requiredExt == false) {
            File parent = target.getParentFile();
            target = new File(parent, name + ".patch"); // NOI18N
        }
        return target;
    }

    /**
     * Synchronously writtes the changes to the given file
     * @param file
     */
    public abstract void writeDiffFile(File file);

    private void onChooseFile(File currentDir) {
        final JFileChooser chooser = createFileChooser(currentDir);

        DialogDescriptor chooseFileDescriptor = new DialogDescriptor(chooser, getMessage("CTL_Export_Title"));
        chooseFileDescriptor.setOptions(new Object[0]);
        dialog = DialogDisplayer.getDefault().createDialog(chooseFileDescriptor);
        dialog.setVisible(true);
    }

    private JFileChooser createFileChooser(File curentDir) {
        final JFileChooser chooser = new AccessibleJFileChooser(NbBundle.getMessage(ExportDiffSupport.class, "ACSD_Export"));
        chooser.setDialogTitle(NbBundle.getMessage(ExportDiffSupport.class, "CTL_Export_Title"));
        chooser.setMultiSelectionEnabled(false);
        javax.swing.filechooser.FileFilter[] old = chooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            javax.swing.filechooser.FileFilter fileFilter = old[i];
            chooser.removeChoosableFileFilter(fileFilter);

        }
        chooser.setCurrentDirectory(curentDir); // NOI18N
        chooser.addChoosableFileFilter(getFileFilter());

        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(ExportDiffSupport.class, "MNE_Export_ExportAction").charAt(0));
        chooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String state = e.getActionCommand();
                if (state.equals(JFileChooser.APPROVE_SELECTION)) {
                    File destination = chooser.getSelectedFile();
                    destination = getTargetFile(destination);
                    if (destination.exists()) {
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(ExportDiffSupport.class, "BK3005", destination.getAbsolutePath()));
                        nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
                        DialogDisplayer.getDefault().notify(nd);
                        if (nd.getValue().equals(NotifyDescriptor.OK_OPTION) == false) {
                            return;
                        }
                    }
                    preferences.put("ExportDiff.saveFolder", destination.getParent()); // NOI18N
                    panel.setOutputFileText(destination.getAbsolutePath());
                } else {
                    dd.setValue(null);
                }
                if(dialog != null) {
                    dialog.dispose();
                }
            }
        });
        return chooser;
    }

    public abstract static class ExportDiffProvider {
        private PropertyChangeSupport support = new PropertyChangeSupport(this);
        private static final String EVENT_DATA_CHANGED = "ExportDiff.data.changed";

        /**
         * Sets the files for which this provider should provide
         * @return
         */
        protected abstract void setContext(File[] files);

        /**
         * Handles the given diff file
         * @param file
         */
        public abstract void handleDiffFile(File file);

        /**
         * Return a visual component representing this ExportDiffProvider
         * @return
         */
        public abstract JComponent createComponent();

        /**
         * Returns true if the user intput in this ExportDiffProvider-s
         * component isValid, oherwise false
         * @return
         */
        public abstract boolean isValid();

        /**
         * To be called if there was a change made in this ExportDiffProvider-s
         * visual components data
         */
        protected void fireDataChanged() {
            support.firePropertyChange(EVENT_DATA_CHANGED, null, null);
        }
        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }
        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }
    }

    /**
     * Abstract ancestor of an output diff panel.
     * @author Ondra Vrabec
     */
    public abstract static class AbstractExportDiffPanel extends JPanel {

        /**
         * Implement this method to access diff output file path field's text.
         * @return value of the diff output file path field.
         */
        public abstract String getOutputFileText();

        /**
         * Implement this to set the diff output file path field' text.
         * @param text
         */
        public abstract void setOutputFileText(final String text);

        /**
         * Implement this to add a listener to the diff output file path field.
         * @param list
         */
        public abstract void addOutputFileTextDocumentListener(final DocumentListener list);

        /**
         * Implement to add a listener on the browse button
         * @param actionListener
         */
        public abstract void addBrowseActionListener(ActionListener actionListener);

        /**
         * Override to specify if the diff output is set to file. Implicitly returns true
         * @return true if the file output is selected, false otherwise
         */
        public boolean isFileOutputSelected() {
            return true;
        }
    }
}
