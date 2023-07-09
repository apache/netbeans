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

package org.netbeans.modules.profiler.heapwalk;

import org.netbeans.modules.profiler.*;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@NbBundle.Messages({
    "CompareSnapshotsHelper_HeapSnapshotDisplayName=[heap] {0}",
    "CompareSnapshotsHelper_SelectDialogCaption=Select Heap Dump to Compare",
    "CompareSnapshotsHelper_OpenChooserCaption=Open Heap Dump",
    "CompareSnapshotsHelper_OpenChooserFilter=Heap Dump Files",
    "CompareSnapshotsHelper_NoComparableSnapshotsFoundMsg=<No comparable heap dumps found>",
    "CompareSnapshotsHelper_ComparingSameSnapshotsMsg=The heap dump cannot be compared to itself.",
    "CompareSnapshotsHelper_InvalidFileMsg=Invalid heap dump file",
    "CompareSnapshotsHelper_EnterFileMsg=Enter heap dump file",
    "CompareSnapshotsHelper_OkButtonText=OK",
    "CompareSnapshotsHelper_SelectSnapshotString=<html><b><nobr>Select heap dump to compare:</nobr></b></html>",
    "CompareSnapshotsHelper_FromProjectRadioText=From &project:",
    "CompareSnapshotsHelper_FromCurrentLocationRadioText=From &current location:",
    "CompareSnapshotsHelper_FromFileRadioText=From &file:",
    "CompareSnapshotsHelper_BrowseButtonText=&Browse",
    "CompareSnapshotsHelper_BrowseButtonAccessDescr=Select heap dump file",
    "CompareSnapshotsHelper_SnapshotsListAccessDescr=List of comparable heap dumps in current project",
    "CompareSnapshotsHelper_CompareRetainedRadio=Compare &retained sizes",
    "CompareSnapshotsHelper_CompareRetainedRadioAccessDescr=Compute and compare retained sizes by class",
    "CompareSnapshotsHelper_CompareRetainedHint=Comparing retained sizes can take a significant amount of time!",
    "CompareSnapshotsHelper_CaptionWarning=Warning",
    "CompareSnapshotsHelper_DifferentObjectSize=<html><b>Object sizes are different.</b><br><br>Size of the same objects differ for each heap dump and their comparison is invalid.<br>The heap dumps have likely been taken on different architectures (32bit vs. 64bit).</html>"
})
class CompareSnapshotsHelper {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class SelectSecondSnapshotPanel extends JPanel {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private JButton externalFileButton;
        private JButton okButton;
        private JLabel externalFileHintLabel;
        private JLabel projectSnapshotsHintLabel;
        private JLabel selectSnapshotLabel;
        private JList projectSnapshotsList;
        private JRadioButton fromFileRadio;
        private JRadioButton fromProjectRadio;
        private JTextField externalFileField;
        private JCheckBox compareRetainedCheckBox;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        SelectSecondSnapshotPanel() {
            initComponents();
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        JButton getOKButton() {
            return okButton;
        }

        File getSnapshot() {
            if (fromProjectRadio.isSelected()) {
                Object selectedItem = projectSnapshotsList.getSelectedValue();

                if (!(selectedItem instanceof FileObject)) {
                    return null;
                }

                return FileUtil.toFile((FileObject)selectedItem);
            } else if (fromFileRadio.isSelected()) {
                String sf = externalFileField.getText();

                if (sf.length() > 0) {
                    File s = new File(sf);

                    if (s.exists() && s.isFile()) {
                        return s;
                    }
                }

                return null;
            } else {
                return null;
            }
        }
        
        boolean computeRetained() {
            return compareRetainedCheckBox.isSelected();
        }

        void populateSnapshotsList() {
            // Get list model
            DefaultListModel listModel = (DefaultListModel) projectSnapshotsList.getModel();

            // Clear the list
            listModel.removeAllElements();

            // Add saved snapshots
            final Lookup.Provider project = heapWalker.getHeapDumpProject();
            File heapdumpFile = heapWalker.getHeapDumpFile();
            final File heapdumpDir = heapdumpFile != null ? heapdumpFile.getParentFile() : null;
            FileObject[] snapshotsOnDisk = ResultsManager.getDefault().listSavedHeapdumps(project, heapdumpDir);
            FileObject snapshotFile = (heapdumpFile != null) ? FileUtil.toFileObject(heapdumpFile) : null;

            for (int i = 0; i < snapshotsOnDisk.length; i++) {
                if (((snapshotFile == null) || !snapshotsOnDisk[i].equals(snapshotFile))) {
                    listModel.addElement(snapshotsOnDisk[i]);
                }
            }

            if (listModel.getSize() == 0) {
                listModel.addElement(Bundle.CompareSnapshotsHelper_NoComparableSnapshotsFoundMsg());
                fromFileRadio.setSelected(true);
                externalFileField.addHierarchyListener(new HierarchyListener() {
                    public void hierarchyChanged(HierarchyEvent e) {
                        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && externalFileField.isShowing()) {
                            externalFileField.removeHierarchyListener(this);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    externalFileField.requestFocusInWindow();
                                }
                            });
                        }
                    }
                });
            } else {
                projectSnapshotsList.setSelectedIndex(0);
                projectSnapshotsList.addHierarchyListener(new HierarchyListener() {
                    public void hierarchyChanged(HierarchyEvent e) {
                        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && externalFileField.isShowing()) {
                            projectSnapshotsList.removeHierarchyListener(this);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    projectSnapshotsList.requestFocusInWindow();
                                }
                            });
                        }
                    }
                });
            }
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (project != null) {
                        org.openide.awt.Mnemonics.setLocalizedText(fromProjectRadio, Bundle.CompareSnapshotsHelper_FromProjectRadioText());
                        fromProjectRadio.setToolTipText(null);
                    } else {
                        org.openide.awt.Mnemonics.setLocalizedText(fromProjectRadio, Bundle.CompareSnapshotsHelper_FromCurrentLocationRadioText());
                        fromProjectRadio.setToolTipText(heapdumpDir != null ?
                                heapdumpDir.getAbsolutePath() : null);
                    }
                }
            });
        }

        private void initComponents() {
            okButton = new JButton(Bundle.CompareSnapshotsHelper_OkButtonText());

            setLayout(new GridBagLayout());

            GridBagConstraints c;
            ButtonGroup group = new ButtonGroup();

            selectSnapshotLabel = new JLabel(Bundle.CompareSnapshotsHelper_SelectSnapshotString());
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(15, 10, 8, 10);
            add(selectSnapshotLabel, c);

            fromProjectRadio = new JRadioButton();
            org.openide.awt.Mnemonics.setLocalizedText(fromProjectRadio, Bundle.CompareSnapshotsHelper_FromProjectRadioText());
            group.add(fromProjectRadio);
            fromProjectRadio.getAccessibleContext().setAccessibleDescription(Bundle.CompareSnapshotsHelper_SelectSnapshotString() + Bundle.CompareSnapshotsHelper_FromProjectRadioText());
            fromProjectRadio.setSelected(true);
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(0, 15, 0, 10);
            add(fromProjectRadio, c);

            projectSnapshotsList = new JList(new DefaultListModel());
            projectSnapshotsList.getAccessibleContext().setAccessibleName(Bundle.CompareSnapshotsHelper_SnapshotsListAccessDescr());
            projectSnapshotsList.setVisibleRowCount(5);
            projectSnapshotsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane projectSnapshotsListScroll = new JScrollPane(projectSnapshotsList,
                                                                     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                                     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            projectSnapshotsListScroll.setPreferredSize(new Dimension(1, projectSnapshotsListScroll.getPreferredSize().height));
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 2;
            c.weighty = 1d;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(0, 15 + new JRadioButton("").getPreferredSize().width, 5, 10); // NOI18N
            add(projectSnapshotsListScroll, c);

            projectSnapshotsHintLabel = new JLabel(" "); // NOI18N
            projectSnapshotsHintLabel.setForeground(Color.darkGray);
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 3;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(0, 15 + new JRadioButton("").getPreferredSize().width, 5, 10); // NOI18N
            add(projectSnapshotsHintLabel, c);

            fromFileRadio = new JRadioButton(Bundle.CompareSnapshotsHelper_FromFileRadioText());
            org.openide.awt.Mnemonics.setLocalizedText(fromFileRadio, Bundle.CompareSnapshotsHelper_FromFileRadioText());
            group.add(fromFileRadio);
            fromProjectRadio.getAccessibleContext().setAccessibleDescription(Bundle.CompareSnapshotsHelper_SelectSnapshotString() + Bundle.CompareSnapshotsHelper_FromFileRadioText());
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 4;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(5, 15, 0, 10);
            add(fromFileRadio, c);

            externalFileField = new JTextField();
            externalFileField.setPreferredSize(new Dimension(250, externalFileField.getPreferredSize().height));
            externalFileField.setEnabled(false);
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 5;
            c.weightx = 1d;
            c.gridwidth = 2;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 15 + new JRadioButton("").getPreferredSize().width, 5, 5); // NOI18N
            add(externalFileField, c);

            externalFileButton = new JButton();
            org.openide.awt.Mnemonics.setLocalizedText(externalFileButton, Bundle.CompareSnapshotsHelper_BrowseButtonText());
            externalFileButton.getAccessibleContext().setAccessibleDescription(Bundle.CompareSnapshotsHelper_BrowseButtonAccessDescr());
            externalFileButton.setEnabled(false);
            c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = 5;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(0, 5, 5, 10);
            add(externalFileButton, c);

            externalFileHintLabel = new JLabel(" "); // NOI18N
            externalFileHintLabel.setForeground(Color.darkGray);
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 6;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(0, 15 + new JRadioButton("").getPreferredSize().width, 5, 10); // NOI18N
            add(externalFileHintLabel, c);
            
            compareRetainedCheckBox = new JCheckBox();
            org.openide.awt.Mnemonics.setLocalizedText(compareRetainedCheckBox, Bundle.CompareSnapshotsHelper_CompareRetainedRadio());
            compareRetainedCheckBox.getAccessibleContext().setAccessibleDescription(Bundle.CompareSnapshotsHelper_CompareRetainedRadioAccessDescr());
            compareRetainedCheckBox.setSelected(compareRetained);
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 7;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(5, 15, 0, 10);
            add(compareRetainedCheckBox, c);
            
            JLabel compareRetainedHintLabel = new JLabel(Bundle.CompareSnapshotsHelper_CompareRetainedHint());
            compareRetainedHintLabel.setForeground(Color.darkGray);
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 8;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 15 + new JRadioButton("").getPreferredSize().width, 5, 10); // NOI18N
            add(compareRetainedHintLabel, c);

            projectSnapshotsList.setCellRenderer(new DefaultListCellRenderer() {
                    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                                  boolean cellHasFocus) {
                        JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                        if (value instanceof FileObject) {
                            FileObject fo = (FileObject) value;
                            c.setIcon(memoryIcon);
                            c.setText(ResultsManager.getDefault().
                                    getHeapDumpDisplayName(fo.getName()));

                            File f = FileUtil.toFile(fo);
                            Set<TopComponent> tcs = WindowManager.getDefault().getRegistry().getOpened();
                            for (TopComponent tc : tcs) {
                                if (f.equals(tc.getClientProperty(ProfilerTopComponent.RECENT_FILE_KEY))) {
                                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                                    break;
                                }
                            }
                        }

                        return c;
                    }
                });

            projectSnapshotsList.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        updateOKButton();
                    }
                });

            projectSnapshotsList.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)
                                && (projectSnapshotsList.getSelectedValue() != null)) {
                            SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        okButton.doClick();
                                    }
                                });
                        }
                    }
                });

            fromProjectRadio.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        projectSnapshotsList.setEnabled(fromProjectRadio.isSelected());
                        projectSnapshotsHintLabel.setText(" "); // NOI18N
                        updateOKButton();
                    }
                });

            fromFileRadio.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        externalFileField.setEnabled(fromFileRadio.isSelected());
                        externalFileButton.setEnabled(fromFileRadio.isSelected());

                        if (!fromFileRadio.isSelected()) {
                            externalFileHintLabel.setText(" "); // NOI18N
                        }

                        updateOKButton();
                    }
                });

            DocumentListener documentListener = new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    updateOKButton();
                }

                public void removeUpdate(DocumentEvent e) {
                    updateOKButton();
                }

                public void changedUpdate(DocumentEvent e) {
                    updateOKButton();
                }
            };

            externalFileField.getDocument().addDocumentListener(documentListener);

            externalFileButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser snapshotChooser = CompareSnapshotsHelper.getSnapshotFileChooser();
                        snapshotChooser.setCurrentDirectory(new File(externalFileField.getText()));

                        if (snapshotChooser.showOpenDialog(WindowManager.getDefault().getMainWindow()) == 0) {
                            File file = snapshotChooser.getSelectedFile();

                            if (file != null) {
                                externalFileField.setText(file.getAbsolutePath());
                            }
                        }
                    }
                });

            updateOKButton();
        }

        private void updateOKButton() {
            if (fromProjectRadio.isSelected()) {
                // Snapshot from project
                Object selectedItem = projectSnapshotsList.getSelectedValue();
                okButton.setEnabled((selectedItem != null) && !(selectedItem instanceof String));
            } else if (fromFileRadio.isSelected()) {
                // Snapshot from file
                String sf = externalFileField.getText();

                if (sf.length() > 0) {
                    // filename not empty string
                    File s = new File(sf);

                    if (s.exists() && ResultsManager.checkHprofFile(s)) {
                        // file exists
                        if (s.equals(heapWalker.getHeapDumpFile())) {
                            // comparing snapshot with itself
                            externalFileHintLabel.setText(Bundle.CompareSnapshotsHelper_ComparingSameSnapshotsMsg());
                            okButton.setEnabled(false);
                        } else {
                            // comparing different snapshots
                            externalFileHintLabel.setText(" "); // NOI18N
                            okButton.setEnabled(true);
                        }
                    } else {
                        // file doesn't exist or not a .hprof file
                        externalFileHintLabel.setText(Bundle.CompareSnapshotsHelper_InvalidFileMsg());
                        okButton.setEnabled(false);
                    }
                } else {
                    // filename is empty string
                    externalFileHintLabel.setText(Bundle.CompareSnapshotsHelper_EnterFileMsg());
                    okButton.setEnabled(false);
                }
            } else {
                okButton.setEnabled(false);
            }
        }
    }
    
    static class Result {
        private File file;
        private boolean compareRetained;
        
        Result(File file, boolean compareRetained) {
            this.file = file;
            this.compareRetained = compareRetained;
        }
        
        File getFile() { return file; }
        boolean compareRetained() { return compareRetained; }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final Icon memoryIcon = Icons.getIcon(ProfilerIcons.HEAP_DUMP);
    private static JFileChooser snapshotFileChooser;
    private static HelpCtx HELP_CTX = new HelpCtx("SelectSecondSnapshot.HelpCtx"); // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private HeapFragmentWalker heapWalker;
    private boolean compareRetained;
    private SelectSecondSnapshotPanel secondSnapshotSelector;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    private CompareSnapshotsHelper(HeapFragmentWalker heapWalker, boolean compareRetained) {
        this.heapWalker = heapWalker;
        this.compareRetained = compareRetained;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    static void checkObjectSizes(Heap heap1, Heap heap2) {
        JavaClass objcls1 = heap1.getJavaClassByName("java.lang.Object"); // NOI18N
        JavaClass objcls2 = heap2.getJavaClassByName("java.lang.Object"); // NOI18N
        
        if (objcls1.getInstanceSize() != objcls2.getInstanceSize())
            ProfilerDialogs.displayWarningDNSA(Bundle.CompareSnapshotsHelper_DifferentObjectSize(),
                                               Bundle.CompareSnapshotsHelper_CaptionWarning(), null,
                                               "CompareSnapshotsHelper.checkObjectSizes", false); // NOI18N
    }

    static Result selectSnapshot(HeapFragmentWalker heapWalker, boolean compareRetained) {
        CompareSnapshotsHelper helper = new CompareSnapshotsHelper(heapWalker, compareRetained);
        SelectSecondSnapshotPanel panel = helper.getSecondSnapshotSelector();
        panel.populateSnapshotsList();

        DialogDescriptor desc = new DialogDescriptor(panel, Bundle.CompareSnapshotsHelper_SelectDialogCaption(), true,
                                                     new Object[] {
                                                         panel.getOKButton(), DialogDescriptor.CANCEL_OPTION
                                                     }, DialogDescriptor.OK_OPTION, 0, HELP_CTX, null);
        Object res = DialogDisplayer.getDefault().notify(desc);

        return !res.equals(panel.getOKButton()) ? null :
                new Result(panel.getSnapshot(), panel.computeRetained());
    }

    private static JFileChooser getSnapshotFileChooser() {
        if (snapshotFileChooser == null) {
            snapshotFileChooser = new JFileChooser();
            snapshotFileChooser.setAcceptAllFileFilterUsed(false);
            snapshotFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            snapshotFileChooser.setMultiSelectionEnabled(false);
            snapshotFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            snapshotFileChooser.setDialogTitle(Bundle.CompareSnapshotsHelper_OpenChooserCaption());
            snapshotFileChooser.setFileFilter(new FileFilter() {
                    public boolean accept(File f) {
                        return f.isDirectory() || ResultsManager.checkHprofFile(f);
                    }

                    public String getDescription() {
                        return Bundle.CompareSnapshotsHelper_OpenChooserFilter();
                    }
                });
        }

        return snapshotFileChooser;
    }
    
    private SelectSecondSnapshotPanel getSecondSnapshotSelector() {
        if (secondSnapshotSelector == null) {
            secondSnapshotSelector = new SelectSecondSnapshotPanel();
        }

        return secondSnapshotSelector;
    }
}
