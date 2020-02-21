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
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.utils.ui.ListEditorPanel;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.PackagerFileElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerFileElement.FileType;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.ui.utils.PathPanel;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

public class PackagingFilesPanel extends ListEditorPanel<PackagerFileElement> {

    private final FSPath baseDir;
    private JTable targetList;
    private final MyTableCellRenderer myTableCellRenderer = new MyTableCellRenderer();
    private final JButton addButton;
    private final JButton addFileOrDirectoryButton;
    private final JButton addFilesButton;
    private final JButton addLinkButton;
    private PackagingFilesOuterPanel packagingFilesOuterPanel;

    public PackagingFilesPanel(List<PackagerFileElement> fileList, FSPath baseDir) {
        super(fileList, new JButton[]{new JButton(), new JButton(), new JButton(), new JButton()});
        getAddButton().setVisible(false);
        this.baseDir = baseDir;
        this.addButton = extraButtons[0];
        this.addFileOrDirectoryButton = extraButtons[1];
        this.addFilesButton = extraButtons[2];
        this.addLinkButton = extraButtons[3];

        addButton.setText(getString("PackagingFilesPanel.addButton.text"));
        addButton.setMnemonic(getString("PackagingFilesPanel.addButton.mn").charAt(0));
        addButton.getAccessibleContext().setAccessibleDescription(getString("PackagingFilesPanel.addButton.ad"));
        addButton.addActionListener(new AddButtonAction());

        addFileOrDirectoryButton.setText(getString("PackagingFilesPanel.addFileOrDirButton.text"));
        addFileOrDirectoryButton.setMnemonic(getString("PackagingFilesPanel.addFileOrDirButton.mn").charAt(0));
        addFileOrDirectoryButton.getAccessibleContext().setAccessibleDescription(getString("PackagingFilesPanel.addFileOrDirButton.ad"));
        addFileOrDirectoryButton.addActionListener(new AddFileOrDirectoryButtonAction());

        addFilesButton.setText(getString("PackagingFilesPanel.addFilesButton.text"));
        addFilesButton.setMnemonic(getString("PackagingFilesPanel.addFilesButton.mn").charAt(0));
        addFilesButton.getAccessibleContext().setAccessibleDescription(getString("PackagingFilesPanel.addFilesButton.ad"));
        addFilesButton.addActionListener(new AddFilesButtonAction());

        addLinkButton.setText(getString("PackagingFilesPanel.addLinkButton.text"));
        addLinkButton.setMnemonic(getString("PackagingFilesPanel.addLinkButton.mn").charAt(0));
        addLinkButton.getAccessibleContext().setAccessibleDescription(getString("PackagingFilesPanel.addLinkButton.ad"));
        addLinkButton.addActionListener(new AddLinkButtonAction());

        getEditButton().setVisible(false);
        getDefaultButton().setVisible(false);
    }

    private void refresh() {
        packagingFilesOuterPanel.getPackagingConfiguration().getTopDir().setValue(packagingFilesOuterPanel.getTopDirectoryTextField().getText());
        getTargetList().validate();
        getTargetList().repaint();
    }

    public void setOuterPanel(PackagingFilesOuterPanel packagingFilesOuterPanel) {
        this.packagingFilesOuterPanel = packagingFilesOuterPanel;
        DocumentListener documentListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh();
            }
        };
        packagingFilesOuterPanel.getTopDirectoryTextField().getDocument().addDocumentListener(documentListener);
    }

    private final class AddButtonAction implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            String topFolder = "${PACKAGE_TOP_DIR}"; // NOI18N
            addObjectAction(new PackagerFileElement(FileType.UNKNOWN, "", topFolder)); // NOI18N
        }
    }

    private final class AddLinkButtonAction implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            PackagingNewLinkPanel packagingNewEntryPanel = new PackagingNewLinkPanel(packagingFilesOuterPanel.getTopDirectoryTextField().getText());
            DialogDescriptor dialogDescriptor = new DialogDescriptor(packagingNewEntryPanel, getString("AddNewLinkDialogTitle"));
            packagingNewEntryPanel.setDialogDesriptor(dialogDescriptor);
            DialogDisplayer.getDefault().notify(dialogDescriptor);
            if (dialogDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
                return;
            }
            addObjectAction(new PackagerFileElement(
                    FileType.SOFTLINK,
                    packagingNewEntryPanel.getLink(),
                    packagingNewEntryPanel.getLinkName(),
                    "", // packagingFilesOuterPanel.getFilePermTextField().getText(),
                    packagingFilesOuterPanel.getOwnerTextField().getText(),
                    packagingFilesOuterPanel.getGroupTextField().getText()));
        }
    }

    private final class AddFileOrDirectoryButtonAction implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final String chooser_key = "packaging.AddFileOrDirectory"; //NOI18N
            ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(baseDir.getFileObject());
            String seed = RemoteFileChooserUtil.getCurrentChooserFile(chooser_key, env);
            if (seed == null) {
                seed = baseDir.getPath();
            }
            JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(env, 
                                       getString("FileChooserFileTitle"), getString("FileChooserButtonText"), JFileChooser.FILES_AND_DIRECTORIES, null, seed, false);
            PathPanel pathPanel = new PathPanel();
            fileChooser.setAccessory(pathPanel);
            fileChooser.setMultiSelectionEnabled(true);
            int ret = fileChooser.showOpenDialog(null);
            if (ret == JFileChooser.CANCEL_OPTION) {
                return;
            }            
            File[] files = fileChooser.getSelectedFiles();
            if (files == null || files.length == 0) {
                return;
            }
            File selectedFolder = files[0].isFile() ? files[0].getParentFile() : files[0];
            RemoteFileChooserUtil.setCurrentChooserFile(chooser_key, selectedFolder.getPath(), env);            
            for (int i = 0; i < files.length; i++) {
                String itemPath = ProjectSupport.toProperPath(baseDir.getFileObject(), files[i].getPath(), MakeProjectOptions.getPathMode()); // XXX:fillRemote: changeto project dependent value
                itemPath = CndPathUtilities.normalizeSlashes(itemPath);
                String topFolder = "${PACKAGE_TOP_DIR}"; // NOI18N
                if (files[i].isDirectory()) {
                    addObjectAction(new PackagerFileElement(
                            FileType.DIRECTORY,
                            "", // NOI18N
                            topFolder + files[i].getName(),
                            packagingFilesOuterPanel.getDirPermTextField().getText(),
                            packagingFilesOuterPanel.getOwnerTextField().getText(),
                            packagingFilesOuterPanel.getGroupTextField().getText())); // FIXUP: softlink
                } else {
                    // Regular file
                    String perm;
                    if (isExecutable(files[i])) {
                        perm = packagingFilesOuterPanel.getDirPermTextField().getText();
                    } else {
                        perm = packagingFilesOuterPanel.getFilePermTextField().getText();
                    }
                    addObjectAction(new PackagerFileElement(
                            FileType.FILE,
                            itemPath,
                            topFolder + files[i].getName(),
                            perm,
                            packagingFilesOuterPanel.getOwnerTextField().getText(),
                            packagingFilesOuterPanel.getGroupTextField().getText()));
                }
            }
        }
    }

    /*
     * Return true if file is an executable
     */
    private boolean isExecutable(File file) {
        FileObject fo = null;

        if (file.getName().endsWith(".exe")) { //NOI18N
            return true;
        }

        try {
            fo = CndFileUtils.toFileObject(file.getCanonicalFile());
        } catch (IOException e) {
            return false;
        }
        if (fo == null || !fo.isValid()) { // 149058
            return false;
        }
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(fo);
        } catch (DataObjectNotFoundException e) {
            return false;
        }
        final String mime = dataObject.getPrimaryFile().getMIMEType();
        return mime.equals(MIMENames.SHELL_MIME_TYPE) || 
               mime.equals(MIMENames.BAT_MIME_TYPE) || 
               mime.equals(MIMENames.ELF_SHOBJ_MIME_TYPE) || 
               MIMENames.isBinaryExecutable(mime);
    }

    private final class AddFilesButtonAction implements java.awt.event.ActionListener {
//        private PackagingAddingFilesProgressPanel progressPanel;

        private boolean cancelled;

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final String chooser_key = "packaging.AddFileOrDirectory"; //NOI18N
            ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(baseDir.getFileObject());
            String seed = RemoteFileChooserUtil.getCurrentChooserFile(chooser_key, env);
            if (seed == null) {
                seed = baseDir.getPath();
            }
            JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(env, 
                                       getString("FileChooserFilesTitle"), getString("FileChooserButtonText"), JFileChooser.DIRECTORIES_ONLY, null, seed, false);
            PathPanel pathPanel = new PathPanel();
            fileChooser.setAccessory(pathPanel);
            fileChooser.setMultiSelectionEnabled(false);
            int ret = fileChooser.showOpenDialog(null);
            if (ret == JFileChooser.CANCEL_OPTION) {
                return;
            }
            final File dir = fileChooser.getSelectedFile();
            RemoteFileChooserUtil.setCurrentChooserFile(chooser_key, dir.getPath(), env);
            cancelled = false;
            JButton stopButton = new JButton(getString("PackagingAddingFilesProgressPanel.Stop.Button.text"));
            stopButton.setMnemonic(getString("PackagingAddingFilesProgressPanel.Stop.Button.text").charAt(0));
            stopButton.addActionListener(new StopButtonAction());
            final PackagingAddingFilesProgressPanel progressPanel = new PackagingAddingFilesProgressPanel(stopButton);
            DialogDescriptor dialogDescriptor = new DialogDescriptor(progressPanel, getString("PackagingAddingFilesProgressPanel.title"), true, new JButton[]{stopButton}, stopButton, DialogDescriptor.RIGHT_ALIGN, null, null);
            final Dialog progressDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
            progressDialog.addWindowListener(new WindowAdapter() {

                @Override
                public void windowOpened(WindowEvent e) {
                    new AddFilesFromDir(dir, progressPanel, progressDialog).start();
                }
            });
            try {
                progressDialog.setVisible(true);
            } catch (Throwable th) {
                if (!(th.getCause() instanceof InterruptedException)) {
                    throw new RuntimeException(th);
                }
                dialogDescriptor.setValue(DialogDescriptor.CLOSED_OPTION);
            } finally {
                progressDialog.dispose();
            }
        //addFilesFromDirectory(dir, dir);
        }

        private final class AddFilesFromDir extends Thread {

            private final PackagingAddingFilesProgressPanel progressPanel;
            private final Dialog progressDialog;
            private final File dir;

            AddFilesFromDir(File dir, PackagingAddingFilesProgressPanel progressPanel, Dialog progressDialog) {
                this.progressPanel = progressPanel;
                this.progressDialog = progressDialog;
                this.dir = dir;
            }

            @Override
            public void run() {
                final ArrayList<PackagerFileElement> listToAdd = new ArrayList<>();
                addFilesFromDirectory(listToAdd, dir, dir, progressPanel);

                SwingUtilities.invokeLater(() -> {
                    addObjectsAction(listToAdd);
                    progressDialog.setVisible(false);
                });
            }
        }

        private final class StopButtonAction implements java.awt.event.ActionListener {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                cancelled = true;
            }
        }

        private void addFilesFromDirectory(ArrayList<PackagerFileElement> listToAdd, File origDir, File dir, PackagingAddingFilesProgressPanel progressPanel) {
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }
            for (int i = 0; i < files.length; i++) {
                if (cancelled) {
                    break;
                }
                if (files[i].isDirectory()) {
                    addFilesFromDirectory(listToAdd, origDir, files[i], progressPanel);
                } else {
                    String path = ProjectSupport.toProperPath(baseDir.getFileObject(), files[i].getPath(), MakeProjectOptions.getPathMode()); // XXX:fillRemote: changeto project dependent value
                    if (MakeProjectOptions.getPathMode() == MakeProjectOptions.PathMode.REL_OR_ABS) {
                        path = CndPathUtilities.toAbsoluteOrRelativePath(baseDir.getFileObject(), files[i].getPath());
                    } else if (MakeProjectOptions.getPathMode() == MakeProjectOptions.PathMode.REL) {
                        path = CndPathUtilities.toRelativePath(baseDir.getFileObject(), files[i].getPath());
                    } else {
                        path = files[i].getPath();
                    }
                    path = CndPathUtilities.normalizeSlashes(path);
                    String toFile = CndPathUtilities.toRelativePath(origDir.getParentFile().getAbsolutePath(), files[i].getPath());
                    toFile = CndPathUtilities.normalizeSlashes(toFile);
                    String topFolder = "${PACKAGE_TOP_DIR}"; // NOI18N
                    String perm;
                    if (files[i].getName().endsWith(".exe") || files[i].isDirectory() || isExecutable(files[i])) { //NOI18N
                        perm = packagingFilesOuterPanel.getDirPermTextField().getText();
                    } else {
                        perm = packagingFilesOuterPanel.getFilePermTextField().getText();
                    }
//                    addObjectAction(new FileElement(
                    listToAdd.add(new PackagerFileElement(
                            FileType.FILE,
                            path,
                            topFolder + toFile,
                            perm,
                            packagingFilesOuterPanel.getOwnerTextField().getText(),
                            packagingFilesOuterPanel.getGroupTextField().getText()));
                    progressPanel.setProgress(path);
                }
            }
        }
    }

    @Override
    public PackagerFileElement copyAction(PackagerFileElement o) {
        PackagerFileElement elem = o;
        PackagerFileElement copy;
        copy = new PackagerFileElement(elem.getType(), new String(elem.getFrom()), new String(elem.getTo()));
        copy.setOwner(elem.getOwner());
        copy.setPermission(elem.getPermission());
        copy.setGroup(elem.getGroup());
        return copy;
    }

    @Override
    public String getCopyButtonText() {
        return getString("PackagingFilesPanel.duplicateButton.text");
    }

    @Override
    public char getCopyButtonMnemonics() {
        return getString("PackagingFilesPanel.duplicateButton.mn").charAt(0);
    }

    @Override
    public String getCopyButtonAD() {
        return getString("PackagingFilesPanel.duplicateButton.ad");
    }

    @Override
    public String getListLabelText() {
        return getString("PackagingFilesPanel.listlabel.text");
    }

    @Override
    public char getListLabelMnemonic() {
        return getString("PackagingFilesPanel.listlabel.mn").charAt(0);
    }

    // Overrides ListEditorPanel
    @Override
    public int getSelectedIndex() {
        int index = getTargetList().getSelectedRow();
        if (index >= 0 && index < getListDataSize()) {
            return index;
        } else {
            return 0;
        }
    }

    @Override
    protected void setSelectedIndex(int i) {
        getTargetList().getSelectionModel().setSelectionInterval(i, i);
    }

    @Override
    protected void setData(List<PackagerFileElement> data) {
        getTargetList().setModel(new MyTableModel());
        // Set column sizes
        if (getTargetList().getColumnModel().getColumnCount() >= 4) {
            getTargetList().getColumnModel().getColumn(0).setPreferredWidth(40);
            getTargetList().getColumnModel().getColumn(0).setMaxWidth(100);
            getTargetList().getColumnModel().getColumn(1).setPreferredWidth(40);
            getTargetList().getColumnModel().getColumn(1).setMaxWidth(300);
            getTargetList().getColumnModel().getColumn(2).setPreferredWidth(40);
            getTargetList().getColumnModel().getColumn(2).setMaxWidth(300);
            getTargetList().getColumnModel().getColumn(3).setPreferredWidth(50);
            getTargetList().getColumnModel().getColumn(3).setMaxWidth(100);
        }
        if (getTargetList().getColumnModel().getColumnCount() >= 6) {
            getTargetList().getColumnModel().getColumn(4).setPreferredWidth(40);
            getTargetList().getColumnModel().getColumn(4).setMaxWidth(100);
            getTargetList().getColumnModel().getColumn(5).setPreferredWidth(40);
            getTargetList().getColumnModel().getColumn(5).setMaxWidth(100);
        }
        //
        // Left align table header
        ((DefaultTableCellRenderer) getTargetList().getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    private class TargetSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            checkSelection();
        }
    }

    @Override
    protected void ensureIndexIsVisible(int selectedIndex) {
        Rectangle rect = getTargetList().getCellRect(selectedIndex, 0, true);
        getTargetList().scrollRectToVisible(rect);
    }

    @Override
    protected Component getViewComponent() {
        return getTargetList();
    }

    private JTable getTargetList() {
        if (targetList == null) {
            targetList = new MyTable();
            setData(null);
            getListLabel().setLabelFor(targetList);
            getTargetList().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            getTargetList().getSelectionModel().addListSelectionListener(new TargetSelectionListener());
        }
        return targetList;
    }

    private final class MyTable extends JTable {

        public MyTable() {
//	    //setTableHeader(null); // Hides table headers
//	    if (getRowHeight() < 19)
//		setRowHeight(19);
            getAccessibleContext().setAccessibleDescription(""); // NOI18N
            getAccessibleContext().setAccessibleName(""); // NOI18N

            putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        }

        @Override
        public Color getGridColor() {
            return new Color(225, 225, 225);
        }

//        @Override
//        public boolean getShowHorizontalLines() {
//            return false;
//        }
//
//        @Override
//        public boolean getShowVerticalLines() {
//            return false;
//        }
        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            return myTableCellRenderer;
        }

        @Override
        public TableCellEditor getCellEditor(int row, int col) {
            if (col == 0) {
                PackagerFileElement elem = getElementAt(row);

                JComboBox comboBox = new JComboBox();
                comboBox.addItem(FileType.FILE);
                comboBox.addItem(FileType.DIRECTORY);
                comboBox.addItem(FileType.SOFTLINK);
                if (elem.getType() == PackagerFileElement.FileType.DIRECTORY) {
                    comboBox.setSelectedIndex(1);
                } else if (elem.getType() == PackagerFileElement.FileType.SOFTLINK) {
                    comboBox.setSelectedIndex(2);
                } else {
                    comboBox.setSelectedIndex(0);
                }
                return new DefaultCellEditor(comboBox);
            } else {
                return super.getCellEditor(row, col);
            }
        }
    }

    private final class MyTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, col);
            PackagerFileElement elem = getElementAt(row);
            if (col == 0) {
                label.setText(elem.getType().toString());
            } else if (col == 1) {
                if (elem.getType() == PackagerFileElement.FileType.SOFTLINK) {
                    String msg = getString("Softlink_tt", elem.getTo() + "->" + elem.getFrom()); // NOI18N
                    label.setToolTipText(msg);
                } else if (elem.getType() == PackagerFileElement.FileType.DIRECTORY) {
                    String msg = getString("Directory_tt", elem.getTo()); // NOI18N
                    label.setToolTipText(msg);
                } else if (elem.getType() == PackagerFileElement.FileType.FILE) {
                    String msg = getString("File_tt", (new File(CndPathUtilities.toAbsolutePath(baseDir.getFileObject(), elem.getFrom())).getAbsolutePath())); // NOI18N
                    label.setToolTipText(msg);
                }
                String val = elem.getTo();
                if (val.contains("${")) { // NOI18N
                    String expandedVal = packagingFilesOuterPanel.getPackagingConfiguration().expandMacros(val);
                    label.setText(expandedVal);
                }
            } else if (col == 2) {
                String val = elem.getFrom();
                if (val.contains("${")) { // NOI18N
                    String expandedVal = packagingFilesOuterPanel.getPackagingConfiguration().expandMacros(val);
                    label.setText(expandedVal);
                }
            }
            return label;
        }
    }

    /*
     * Can be overridden to show fewer colums
     */
    public int getActualColumnCount() {
        return 6;
    }

    private final class MyTableModel extends DefaultTableModel {

        private final String[] columnNames = {
            getString("PackagingFilesOuterPanel.column.0.text"),
            getString("PackagingFilesOuterPanel.column.1.text"),
            getString("PackagingFilesOuterPanel.column.2.text"),
            getString("PackagingFilesOuterPanel.column.3.text"),
            getString("PackagingFilesOuterPanel.column.4.text"),
            getString("PackagingFilesOuterPanel.column.5.text")
        };

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getColumnCount() {
            return getActualColumnCount();
        }

        @Override
        public int getRowCount() {
            return getListDataSize();
        }

        @Override
        public Object getValueAt(int row, int col) {
//            return listData.elementAt(row);
            PackagerFileElement elem = getElementAt(row);
            if (col == 0) {
                return elem.getType();
            }
            if (col == 2) {
                if (elem.getType() == PackagerFileElement.FileType.DIRECTORY) {
                    return ""; // NOI18N
                } else {
                    return elem.getFrom();
                }
            }
            if (col == 1) {
                return elem.getTo();
            }
            if (col == 3) {
                return elem.getPermission();
            }
            if (col == 4) {
                return elem.getOwner();
            }
            if (col == 5) {
                return elem.getGroup();
            }
            assert false;
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == 0) {
                return true;
            } else {
                return true;
            }
        }

        @Override
        public void setValueAt(Object val, int row, int col) {
            PackagerFileElement elem = getElementAt(row);
            if (col == 0) {
                FileType fileType = (FileType) val;
                if (fileType == FileType.FILE) {
                    elem.setType(fileType);
                    elem.setPermission(packagingFilesOuterPanel.getFilePermTextField().getText());
                    elem.setOwner(packagingFilesOuterPanel.getOwnerTextField().getText());
                    elem.setGroup(packagingFilesOuterPanel.getGroupTextField().getText());
                } else if (fileType == FileType.DIRECTORY) {
                    elem.setType(fileType);
                    elem.setPermission(packagingFilesOuterPanel.getDirPermTextField().getText());
                    elem.setOwner(packagingFilesOuterPanel.getOwnerTextField().getText());
                    elem.setGroup(packagingFilesOuterPanel.getGroupTextField().getText());
                } else if (fileType == FileType.SOFTLINK) {
                    elem.setType(fileType);
                    elem.setPermission(""); // NOI18N
                    elem.setOwner(""); // NOI18N
                    elem.setGroup(""); // NOI18N
                } else {
                    assert false;
                }

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else if (col == 2) {
                elem.setFrom((String) val);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else if (col == 1) {
                elem.setTo((String) val);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else if (col == 3) {
                elem.setPermission((String) val);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else if (col == 4) {
                elem.setOwner((String) val);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else if (col == 5) {
                elem.setGroup((String) val);

                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else {
                assert false;
            }
        }
    }
    
    private static String getString(String s) {
        return NbBundle.getBundle(PackagingFilesPanel.class).getString(s);
    }

    private static String getString(String s, String a1) {
        return NbBundle.getMessage(PackagingFilesPanel.class, s, a1);
    }
}
