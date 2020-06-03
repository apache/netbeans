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

/*
 * SelectBinaryPanelVisual.java
 *
 * Created on Sep 22, 2010, 12:24:57 PM
 */

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.wizards.CommonUtilities;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.ui.utils.ExpandableEditableComboBox;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.ui.DocumentAdapter;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class SelectBinaryPanelVisual extends javax.swing.JPanel {

    private final SelectBinaryPanel controller;
    private static final RequestProcessor RP = new RequestProcessor("Binary Artifact Discovery", 1); // NOI18N
    private final AtomicInteger checking = new AtomicInteger(0);
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.cnd.discovery.projectimport.ImportExecutable"); // NOI18N
    private DefaultTableModel tableModel;
    private static final String BINARY_FILE_KEY = "binaryField"; // NOI18N
    private final List<AtomicBoolean> cancelable = new ArrayList<>();
    private static final class Lock {}
    private final Object lock = new Lock();
    private final AtomicBoolean searching = new AtomicBoolean(false);
    private ExecutionEnvironment env;
    private FileSystem fileSystem;
    private volatile boolean firstTime = true;

    /** Creates new form SelectBinaryPanelVisual */
    public SelectBinaryPanelVisual(SelectBinaryPanel controller) {
        this.controller = controller;
        initComponents();
        dependeciesComboBox.removeAllItems();
        dependeciesComboBox.addItem(new ProjectKindItem(IteratorExtension.ProjectKind.Minimal));
        dependeciesComboBox.addItem(new ProjectKindItem(IteratorExtension.ProjectKind.IncludeDependencies));
        dependeciesComboBox.addItem(new ProjectKindItem(IteratorExtension.ProjectKind.CreateDependencies));
        dependeciesComboBox.setSelectedIndex(1);
        viewComboBox.removeAllItems();
        viewComboBox.addItem(new ProjectView(false));
        viewComboBox.addItem(new ProjectView(true));
        addListeners();
    }

    private void addListeners(){
        ((ExpandableEditableComboBox)binaryField).addChangeListener((ActionEvent e) -> {
            String path = ((ExpandableEditableComboBox)binaryField).getText().trim();
            controller.getWizardStorage().setBinaryPath(fileSystem, path);
            updateRoot();
        });
        sourcesField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void update(DocumentEvent e) {
                String path = sourcesField.getText().trim();
                controller.getWizardStorage().setSourceFolderPath(new FSPath(fileSystem, path));
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickedLine = table.rowAtPoint(e.getPoint());

                if (clickedLine != -1) {
                    if ((e.getModifiers() == InputEvent.BUTTON1_MASK)){
                        if (e.getClickCount() == 1){
                            onClickAction(e);
                        }
                    }
                }
            }
        });
        dependeciesComboBox.addItemListener((ItemEvent e) -> {
            validateController();
        });
        updateRoot();
    }

    private void validateController() {
        controller.getWizardStorage().validate();
    }

    private void updateRoot() {
        sourcesField.setEnabled(false);
        sourcesButton.setEnabled(false);
        dependeciesComboBox.setEnabled(false);
        viewComboBox.setEnabled(false);
        table.setModel(new DefaultTableModel(0, 0));
        final String path = ((ExpandableEditableComboBox) binaryField).getText().trim();
        RP.post(() -> {
            if (validBinary()) {
                if (env.isRemote()) {
                    // TODO check java on remote host
                    //controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, getString("ERROR_FIND_PROJECT_CREATOR", env.getDisplayName()));  // NOI18N
                    //return;
                }
                controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "");
                checking.incrementAndGet();
                validateController();
                final IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
                final Map<String, Object> map = new HashMap<>();
                List<FSPath> binaries = controller.getWizardStorage().getBinaryPath();
                if (binaries.size() == 1) {
                    WizardConstants.DISCOVERY_BUILD_RESULT.toMap(map, binaries.get(0).getPath());
                } else {
                    WizardConstants.DISCOVERY_BUILD_RESULT.toMap(map, binaries.get(0).getPath());
                    StringBuilder buf = new StringBuilder();
                    for (int i = 1; i < binaries.size(); i++) {
                        if (buf.length() > 0) {
                            buf.append(';');
                        }
                        buf.append(binaries.get(i).getPath());
                    }
                    WizardConstants.DISCOVERY_LIBRARIES.toMap(map, buf.toString());
                }
                WizardConstants.DISCOVERY_RESOLVE_LINKS.toMap(map, MakeProjectOptions.getResolveSymbolicLinks());
                if (env.isRemote()) {
                    WizardConstants.DISCOVERY_BINARY_FILESYSTEM.toMap(map, fileSystem);
                }
                if (extension != null) {
                    extension.discoverArtifacts(map);
                    List<String> dlls = WizardConstants.DISCOVERY_BINARY_DEPENDENCIES.fromMap(map);
                    String root = WizardConstants.DISCOVERY_ROOT_FOLDER.fromMap(map);
                    if (root == null) {
                        root = "";
                    }
                    List<String> searchPaths = WizardConstants.DISCOVERY_BINARY_SEARCH_PATH.fromMap(map);
                    final Map<String, String> resolvedDlls = searchingTable(dlls);
                    updateArtifacts(root, map, resolvedDlls);
                    checkDll(resolvedDlls, root, searchPaths, controller.getWizardStorage().getBinaryPath());
                }
            } else {
                if (!path.isEmpty() && controller.getWizardDescriptor() != null) {
                    if (CndPathUtilities.isPathAbsolute(path)) {
                        String nPath = CndFileUtils.normalizeAbsolutePath(path);
                        FileObject fo = CndFileUtils.toFileObject(nPath);
                        if (fo == null || !fo.isValid()) {
                            controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, getString("SelectBinaryPanelVisual.FileNotFound", nPath));  // NOI18N
                        } else {
                            controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, getString("SelectBinaryPanelVisual.Unsupported.Binary", nPath));  // NOI18N
                        }
                    } else {
                        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, getString("SelectBinaryPanelVisual.FileNotFound", path));  // NOI18N
                    }
                }
            }
        });
    }

    private void updateArtifacts(final String root, final Map<String, Object> map, final Map<String, String> dlls){
        SwingUtilities.invokeLater(() -> {
            if (env.isLocal()) {
                CompilerSet compiler = detectCompilerSet(WizardConstants.DISCOVERY_COMPILER.fromMap(map));
                if (compiler != null) {
                    WizardConstants.PROPERTY_TOOLCHAIN.put(controller.getWizardDescriptor(), compiler);
                    WizardConstants.PROPERTY_HOST_UID.put(controller.getWizardDescriptor(), ExecutionEnvironmentFactory.getLocal().getHost());
                    // allow user to select right tool collection if discovery detected wrong one
                    WizardConstants.PROPERTY_READ_ONLY_TOOLCHAIN.put(controller.getWizardDescriptor(), Boolean.FALSE);
                } else {
                    WizardConstants.PROPERTY_READ_ONLY_TOOLCHAIN.put(controller.getWizardDescriptor(), Boolean.FALSE);
                }
                sourcesField.setText(root);
                int i = checking.decrementAndGet();
                if (i == 0) {
                    boolean validBinary = validBinary();
                    List<FSPath> validBinaryPath = getValidBinaryPath();
                    sourcesField.setEnabled(validBinary);
                    sourcesButton.setEnabled(validBinary);
                    dependeciesComboBox.setEnabled(validBinary);
                    viewComboBox.setEnabled(validBinary);
                    if (validBinary && validBinaryPath != null) {
                        String binaryRoot = CndPathUtilities.getDirName(validBinaryPath.get(0).getPath());
                        if (binaryRoot != null) {
                            if (binaryRoot.startsWith(root) || root.startsWith(binaryRoot)) {
                                binaryRoot = null;
                            }
                        }
                        updateTableModel(dlls, root, binaryRoot, true);
                    } else {
                        updateTableModel(Collections.<String, String>emptyMap(), root, null, true);
                    }
                }
                List<String> errors = WizardConstants.DISCOVERY_ERRORS.fromMap(map);
                if (errors != null && errors.size() > 0) {
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errors.get(0));
                } else {
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "");
                }
            } else {
                sourcesField.setText(root);
                int i = checking.decrementAndGet();
                if (i == 0) {
                    boolean validBinary = validBinary();
                    List<FSPath> validBinaryPath = getValidBinaryPath();
                    sourcesField.setEnabled(validBinary);
                    sourcesButton.setEnabled(validBinary);
                    dependeciesComboBox.setEnabled(true);
                    viewComboBox.setEnabled(validBinary);
                    if (validBinary && validBinaryPath != null) {
                        String binaryRoot = CndPathUtilities.getDirName(validBinaryPath.get(0).getPath());
                        if (binaryRoot != null) {
                            if (binaryRoot.startsWith(root) || root.startsWith(binaryRoot)) {
                                binaryRoot = null;
                            }
                        }
                        updateTableModel(dlls, root, binaryRoot, true);
                    } else {
                        updateTableModel(Collections.<String, String>emptyMap(), root, null, true);
                    }
                }
                controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "");
            }
            validateController();
        });
    }

    private void updateDllArtifacts(final String root, final Map<String, String> checkDll, final boolean searching){
        SwingUtilities.invokeLater(() -> {
            int i = checking.get();
            if (i == 0) {
                boolean validBinary = validBinary();
                List<FSPath> validBinaryPath = getValidBinaryPath();
                if (validBinary && validBinaryPath != null) {
                    String binaryRoot = CndPathUtilities.getDirName(validBinaryPath.get(0).getPath());
                    if (binaryRoot != null) {
                        if (binaryRoot.startsWith(root) || root.startsWith(binaryRoot)) {
                            binaryRoot = null;
                        }
                    }
                    updateTableModel(checkDll, root, binaryRoot, searching);
                } else {
                    updateTableModel(Collections.<String, String>emptyMap(), root, null, searching);
                }
            }
            validateController();
        });
    }

    private void updateTableModel(Map<String, String> dlls, String root, String binaryRoot, boolean searching) {
        tableModel = new MyDefaultTableModel(this, dlls, root, binaryRoot, searching);
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(20);
        table.getColumnModel().getColumn(0).setMinWidth(15);
        table.getColumnModel().getColumn(0).setCellRenderer(new CheckBoxCellRenderer());
        table.getColumnModel().getColumn(0).setCellEditor(new CheckBoxTableCellEditor());
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setMinWidth(50);
        if (table.getWidth() > 200) {
            table.getColumnModel().getColumn(2).setPreferredWidth(table.getWidth()-100);
        } else {
            table.getColumnModel().getColumn(2).setPreferredWidth(100);
        }
        table.getColumnModel().getColumn(2).setCellRenderer(new PathCellRenderer(fileSystem));
    }

    private void cancelSearch() {
        cancelable.forEach((cancel) -> {
            cancel.set(true);
        });
    }

    private Map<String,String> searchingTable(List<String> dlls) {
        Map<String,String> dllPaths = new TreeMap<>();
        if (dlls != null) {
            dlls.forEach((dll) -> {
                dllPaths.put(dll, null);
            });
        }
        return dllPaths; 
    }

    private void checkDll(Map<String, String> dllPaths, String root, List<String> searchPaths, List<FSPath> binary) {
        cancelSearch();
        if (validBinary()) {
            searching.set(true);
            validateController();
            synchronized (lock) {
                final AtomicBoolean cancel = new AtomicBoolean(false);
                cancelable.add(cancel);
                ActionListener actionListener = (ActionEvent e) -> {
                    cancel.set(true);
                };
                cancelSearch.addActionListener(actionListener);
                SwingUtilities.invokeLater(() -> {
                    cancelSearch.setEnabled(true);
                });
                processDlls(searchPaths, binary, dllPaths, cancel, root);
                cancelSearch.removeActionListener(actionListener);
                SwingUtilities.invokeLater(() -> {
                    cancelSearch.setEnabled(false);
                });
            }
            searching.set(false);
            validateController();
        }
    }
    
    private void processDlls(List<String> searchPaths, List<FSPath> binaries, Map<String, String> dllPaths, final AtomicBoolean cancel, String root) {
        Set<String> checkedDll = new HashSet<>();
        for(FSPath binary : binaries) {
            checkedDll.add(binary.getPath());
            String ldLibPath = CommonUtilities.getLdLibraryPath(env);
            ldLibPath = CommonUtilities.addSearchPaths(ldLibPath, searchPaths, binary.getPath());
            for(String dll : dllPaths.keySet()) {
                if (cancel.get()) {
                    break;
                }
                String p = findLocation(dll, ldLibPath);
                if (p != null) {
                    dllPaths.put(dll, p);
                } else {
                    dllPaths.put(dll, null);
                }
            }
            while(true) {
                List<String> secondary = new ArrayList<>();
                for(Map.Entry<String,String> entry : dllPaths.entrySet()) {
                    if (cancel.get()) {
                        break;
                    }
                    if (entry.getValue() != null) {
                        if (!checkedDll.contains(entry.getValue())) {
                            checkedDll.add(entry.getValue());
                            final IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
                            final Map<String, Object> map = new HashMap<>();
                            WizardConstants.DISCOVERY_BUILD_RESULT.toMap(map, entry.getValue());
                            WizardConstants.DISCOVERY_RESOLVE_LINKS.toMap(map, Boolean.TRUE);
                            if (env.isRemote()) {
                                WizardConstants.DISCOVERY_BINARY_FILESYSTEM.toMap(map, fileSystem);
                            }
                            if (extension != null) {
                                extension.discoverArtifacts(map);
                                List<String> dlls = WizardConstants.DISCOVERY_BINARY_DEPENDENCIES.fromMap(map);
                                if (dlls != null) {
                                    for(String so : dlls) {
                                        if (!dllPaths.containsKey(so)) {
                                            secondary.add(so);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                for(String so : secondary) {
                    if (cancel.get()) {
                        break;
                    }
                    dllPaths.put(so, findLocation(so, ldLibPath));
                }
                int search = 0;
                for(Map.Entry<String,String> entry : dllPaths.entrySet()) {
                    if (entry.getValue() == null) {
                        search++;
                    }
                }
                updateDllArtifacts(root, dllPaths, search > 0);
                if (!cancel.get() && search > 0 && root.length() > 1) {
                    ProgressHandle progress = ProgressHandleFactory.createHandle(getString("SearchForUnresolvedDLL")); //NOI18N
                    progress.start();
                    try {
                        gatherSubFolders(fileSystem.findResource(root), new HashSet<String>(), dllPaths, cancel);
                    } finally {
                        progress.finish();
                    }
                    updateDllArtifacts(root, dllPaths, false);
                }
                int newSearch = 0;
                for(Map.Entry<String,String> entry : dllPaths.entrySet()) {
                    if (entry.getValue() == null) {
                        newSearch++;
                    }
                }
                if (newSearch == search && secondary.isEmpty()) {
                    break;
                }
            }
        }
    }

    private void gatherSubFolders(FileObject startRoot, HashSet<String> set, Map<String, String> result, AtomicBoolean cancel) {
        List<FileObject> down = new ArrayList<>();
        down.add(startRoot);
        while (!down.isEmpty()) {
            ArrayList<FileObject> next = new ArrayList<>();
            for (FileObject folder : down) {
                if (cancel.get()) {
                    return;
                }
                if (folder != null && folder.isFolder() && folder.canRead()) {
                    String path;
                    try {
                        path = FileSystemProvider.getCanonicalPath(folder);
                    } catch (IOException ex) {
                        continue;
                    }
                    path = path.replace('\\', '/'); // NOI18N
                    if (!set.contains(path)) {
                        set.add(path);
                        FileObject[] fileList = folder.getChildren();
                        if (fileList != null) {
                            for (int i = 0; i < fileList.length; i++) {
                                if (cancel.get()) {
                                    return;
                                }
                                String ffPath = fileList[i].getPath();
                                if (set.contains(ffPath)) {
                                    continue;
                                }
                                if (fileList[i].isFolder()) {
                                    next.add(fileList[i]);
                                } else {
                                    String name = fileList[i].getNameExt();
                                    if (result.containsKey(name)) {
                                        result.put(name, ffPath);
                                        boolean finished = true;
                                        for (Map.Entry<String, String> entry : result.entrySet()) {
                                            if (entry.getValue() == null) {
                                                finished = false;
                                                break;
                                            }
                                        }
                                        if (finished) {
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            down = next;
        }
    }

    private String findLocation(String dll, String ldPath){
        if (ldPath != null) {
            String pathSepararor = ":"; // NOI18N
            if (ldPath.indexOf(';')>0) {
                pathSepararor = ";"; // NOI18N
            }
            Set<String> visited = new HashSet<>();
            for(String search :  ldPath.split(pathSepararor)) {
                if (visited.contains(search)) {
                    continue;
                }
                visited.add(search);
                FileObject file = fileSystem.findResource(search+"/"+dll);
                if (file != null && file.isValid() && file.isData()) {
                    String path = file.getPath();
                    return path.replace('\\', '/');
                }
            }
        }
        return null;
    }

    private CompilerSet detectCompilerSet(String compiler){
        boolean isSunStudio = true;
        if (compiler != null) {
            isSunStudio = compiler.contains("Sun"); // NOI18N
        }
        CompilerSetManager manager = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal());
        if (isSunStudio) {
            CompilerSet def = manager.getDefaultCompilerSet();
            if (def != null && def.getCompilerFlavor().isSunStudioCompiler()) {
                return def;
            }
            def = null;
            for(CompilerSet set : manager.getCompilerSets()) {
                if (set.getCompilerFlavor().isSunStudioCompiler()) {
                    if ("OracleSolarisStudio".equals(set.getName())) { // NOI18N
                        def = set;
                    }
                    if (def == null) {
                        def = set;
                    }
                }
            }
            return def;
        } else {
            CompilerSet def = manager.getDefaultCompilerSet();
            if (def != null && !def.getCompilerFlavor().isSunStudioCompiler()) {
                return def;
            }
            def = null;
            for(CompilerSet set : manager.getCompilerSets()) {
                if (!set.getCompilerFlavor().isSunStudioCompiler()) {
                    if (def == null) {
                        def = set;
                    }
                }
            }
            return def;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        binaryLabel = new javax.swing.JLabel();
        binaryButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        sourcesLabel = new javax.swing.JLabel();
        sourcesField = new javax.swing.JTextField();
        sourcesButton = new javax.swing.JButton();
        dependenciesLabel = new javax.swing.JLabel();
        dependeciesComboBox = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        viewLabel = new javax.swing.JLabel();
        viewComboBox = new javax.swing.JComboBox();
        binaryField = new ExpandableEditableComboBox();
        cancelSearch = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(450, 350));
        setLayout(new java.awt.GridBagLayout());

        binaryLabel.setLabelFor(binaryField);
        org.openide.awt.Mnemonics.setLocalizedText(binaryLabel, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.binaryLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(binaryLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(binaryButton, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.binaryButton.text")); // NOI18N
        binaryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                binaryButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(binaryButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        add(jSeparator1, gridBagConstraints);

        sourcesLabel.setLabelFor(sourcesField);
        org.openide.awt.Mnemonics.setLocalizedText(sourcesLabel, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.sourcesLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(sourcesLabel, gridBagConstraints);

        sourcesField.setText(org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.sourcesField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(sourcesField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(sourcesButton, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.sourcesButton.text")); // NOI18N
        sourcesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourcesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(sourcesButton, gridBagConstraints);

        dependenciesLabel.setLabelFor(dependeciesComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(dependenciesLabel, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.dependenciesLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(dependenciesLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(dependeciesComboBox, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(300, 200));

        table.setModel(new DefaultTableModel());
        jScrollPane1.setViewportView(table);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(jScrollPane1, gridBagConstraints);

        viewLabel.setLabelFor(viewComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(viewLabel, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.viewLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(viewLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(viewComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(binaryField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cancelSearch, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.cancelSearch.text")); // NOI18N
        cancelSearch.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        add(cancelSearch, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void binaryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_binaryButtonActionPerformed
        final String oldPath = ((ExpandableEditableComboBox)binaryField).getText();
        //if (oldPath.isEmpty()) {
        //    String path = selectBinaryFile(oldPath, true);
        //    if (path == null) {
        //        return;
        //    }
        //    ((EditableComboBox)binaryField).setText(path);
        //} else {
            FileListEditorPanel panel = new FileListEditorPanel(oldPath, env, fileSystem);
            JButton jOK = new JButton(NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.Browse.OK.Button")); // NOI18N
            panel.setPreferredSize(new Dimension(450, 200));
            DialogDescriptor dd = new DialogDescriptor(panel, getString("SelectBinaryPanelVisual.Browse.Multi.Title"), true, 
                new Object[] { jOK, DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
            try {
                dialog.setVisible(true);
            } finally {
                dialog.dispose();
            }
            if (dd.getValue() == jOK) {
                StringBuilder buf = new StringBuilder();
                panel.getFileList().forEach((s) -> {
                    if (buf.length() > 0) {
                        buf.append(';');
                    }
                    buf.append(s);
            });
                ((ExpandableEditableComboBox)binaryField).setText(buf.toString());
            }
        //}
    }//GEN-LAST:event_binaryButtonActionPerformed

    private void sourcesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourcesButtonActionPerformed
        String seed = sourcesField.getText();
        JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(
                env,
                getString("SelectBinaryPanelVisual.Source.Browse.Title"), // NOI18N
                getString("SelectBinaryPanelVisual.Source.Browse.Select"), // NOI18N
                JFileChooser.DIRECTORIES_ONLY,
                null,
                seed,
                false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile != null) { // seems paranoidal, but once I've seen NPE otherwise 8-()
            String path = selectedFile.getPath();
            sourcesField.setText(path);
        }
    }//GEN-LAST:event_sourcesButtonActionPerformed

    void read(WizardDescriptor wizardDescriptor) {
        env = WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wizardDescriptor);
        if (env == null) {
            env = ExecutionEnvironmentFactory.getLocal();
        } else {
            WizardConstants.PROPERTY_HOST_UID.put(wizardDescriptor, ExecutionEnvironmentFactory.toUniqueID(env));
        }
        fileSystem = FileSystemProvider.getFileSystem(env);

        ((ExpandableEditableComboBox)binaryField).setStorage(BINARY_FILE_KEY, NbPreferences.forModule(SelectBinaryPanelVisual.class));
        if (firstTime) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    if (!SwingUtilities.isEventDispatchThread()) {
                        // init host info if it has not been inited yet.
                        try {
                            HostInfoUtils.getHostInfo(env);
                        } catch (IOException | ConnectionManager.CancellationException ex) {
                            // do nothing
                        }
                        SwingUtilities.invokeLater(this);
                    } else {
                        ((ExpandableEditableComboBox)binaryField).setEnv(env);
                    }
                }
            });
        }
        firstTime = false;
        String binary = WizardConstants.PROPERTY_BUILD_RESULT.get(wizardDescriptor);
        if (binary == null) {
            binary = ""; // NOI18N
        }
        ((ExpandableEditableComboBox)binaryField).read(binary);
    }

    void store(WizardDescriptor wizardDescriptor) {
        cancelSearch();
        String binary = ((ExpandableEditableComboBox)binaryField).getText().trim();
        WizardConstants.PROPERTY_BUILD_RESULT.put(wizardDescriptor, binary);
        String[] split = binary.split(";"); // NOI18N
        String aBinary = binary;
        if (split.length > 0) {
            aBinary = split[0];
        }
        WizardConstants.PROPERTY_PREFERED_PROJECT_NAME.put(wizardDescriptor, CndPathUtilities.getBaseName(aBinary));
        WizardConstants.PROPERTY_SOURCE_FOLDER_PATH.put(wizardDescriptor,  sourcesField.getText().trim());
        WizardConstants.PROPERTY_DEPENDENCY_KIND.put(wizardDescriptor, ((ProjectKindItem)dependeciesComboBox.getSelectedItem()).kind);
        WizardConstants.PROPERTY_DEPENDENCIES.put(wizardDescriptor,  getDlls());
        WizardConstants.PROPERTY_TRUE_SOURCE_ROOT.put(wizardDescriptor,  ((ProjectView)viewComboBox.getSelectedItem()).isSourceRoot);
        ((ExpandableEditableComboBox)binaryField).setStorage(BINARY_FILE_KEY, NbPreferences.forModule(SelectBinaryPanelVisual.class));
        ((ExpandableEditableComboBox)binaryField).store();
        if (WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wizardDescriptor) != null) {
            // forbid tool collection selection
            // project creator detect real tool collection
            WizardConstants.PROPERTY_READ_ONLY_TOOLCHAIN.put(wizardDescriptor, Boolean.TRUE);
        }
        // TODO should be inited
        WizardConstants.PROPERTY_USER_MAKEFILE_PATH.put(wizardDescriptor,  ""); // NOI18N
    }

    private ArrayList<String> getDlls(){
        ArrayList<String> dlls = new ArrayList<>();
        if (((ProjectKindItem)dependeciesComboBox.getSelectedItem()).kind == IteratorExtension.ProjectKind.Minimal) {
            return dlls;
        }
        for(int i = 0; i < table.getModel().getRowCount(); i++) {
            if ((Boolean)table.getModel().getValueAt(i, 0)){
                dlls.add((String)table.getModel().getValueAt(i, 2));
            }
        }
        return dlls;
    }

    boolean valid() {
        return !searching.get() && checking.get()==0 && validBinary() && validSourceRoot() && validDlls();
    }

    private List<FSPath> getValidBinaryPath() {
        List<FSPath> binaries = controller.getWizardStorage().getBinaryPath();
        if (binaries == null || binaries.isEmpty()) {
            return null;
        }
        List<FSPath> res = new ArrayList<>();
        for(FSPath path : binaries) {
            if (!CndPathUtilities.isAbsolute(path.getFileSystem(), path.getPath())) {
                return null;
            }
            res.add(new FSPath(fileSystem, CndFileUtils.normalizeAbsolutePath(path.getFileSystem(), path.getPath())));
        }
        return res;
    }
    
    private boolean validBinary() {
        List<FSPath> validBinaryPath = getValidBinaryPath();
        if (validBinaryPath != null) {
            for(FSPath fsPath : validBinaryPath) {
                FileObject fo = fsPath.getFileObject();
                if (fo != null && fo.isValid() && MIMENames.isBinary(fo.getMIMEType())){
                    continue;
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean validSourceRoot() {
        String path = sourcesField.getText().trim();
        if (path.isEmpty()) {
            return false;
        }
        if (CndPathUtilities.isPathAbsolute(path)) {
            FileObject fo = fileSystem.findResource(CndFileUtils.normalizeAbsolutePath(path));
            if (fo == null || !fo.isValid()) {
                return false;
            }
            return fo.isFolder();
        } else {
            return false;
        }
    }

    private boolean validDlls() {
        for(String dll : getDlls()) {
            FileObject fo = fileSystem.findResource(dll);
            if(fo == null || !fo.isValid()) {
                return false;
            }
        }
        return true;
    }

    private void onClickAction(MouseEvent e) {
        int rowIndex = table.rowAtPoint(e.getPoint());
        if (rowIndex >= 0) {
            TableColumnModel columnModel = table.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int col = table.convertColumnIndexToModel(viewColumn);
            if (col == 2){
                Rectangle rect = table.getCellRect(rowIndex, viewColumn, false);
                Point point = new Point(e.getPoint().x - rect.x, e.getPoint().y - rect.y);
                //System.err.println("Action for row "+rowIndex+" rect "+rect+" point "+point);
                if (rect.width - BUTTON_WIDTH <= point.x && point.x <= rect.width ) {
                    tableButtonActionPerformed(rowIndex);
                }
            }
        }
    }

    private String selectBinaryFile(String path, boolean muliSelection) {
        FileFilter[] filters = FileFilterFactory.getBinaryFilters(fileSystem);
        if (path.isEmpty()) { 
            path = SelectModePanel.getDefaultDirectory(env);
        } else {
            if (path.startsWith("\"")) { //NOI18N
                int i = path.indexOf('"', 0);
                int j = path.indexOf('"', 1);
                if (i >= 0 && j > i) {
                    path = path.substring(i+1, j);
                }
            }
        }
        
        JFileChooser fileChooser = NewProjectWizardUtils.createFileChooser(
                controller.getWizardDescriptor(),
                muliSelection?getString("SelectBinaryPanelVisual.Browse.Multi.Title"):getString("SelectBinaryPanelVisual.Browse.Title"), // NOI18N
                muliSelection?getString("SelectBinaryPanelVisual.Browse.Multi.Select"):getString("SelectBinaryPanelVisual.Browse.Select"), // NOI18N
                JFileChooser.FILES_ONLY,
                filters,
                path,
                false
                );
        if (muliSelection) {
            fileChooser.setMultiSelectionEnabled(true);
        }
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return null;
        }
        if (!muliSelection) {
            return fileChooser.getSelectedFile().getPath();
        }
        File[] selected = fileChooser.getSelectedFiles();
        if (selected == null || selected.length == 0) {
            return null;
        }
        if (selected.length == 1) {
            return selected[0].getPath();
        } else {
            StringBuilder buf = new StringBuilder();
            for(File f : selected) {
                if (buf.length() > 0) {
                    buf.append(';');
                }
                buf.append(f.getPath());
            }
            return buf.toString();
        }
    }

    private void tableButtonActionPerformed(int row) {
        String path = selectBinaryFile((String) table.getModel().getValueAt(row, 2), false);
        if (path == null) {
            return;
        }
        table.getModel().setValueAt(path, row, 2);
    }

    private static String getString(String key) {
        return NbBundle.getMessage(SelectBinaryPanelVisual.class, key);
    }

    private static String getString(String key, String arg) {
        return NbBundle.getMessage(SelectBinaryPanelVisual.class, key, arg);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton binaryButton;
    private javax.swing.JComboBox binaryField;
    private javax.swing.JLabel binaryLabel;
    private javax.swing.JButton cancelSearch;
    private javax.swing.JComboBox dependeciesComboBox;
    private javax.swing.JLabel dependenciesLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton sourcesButton;
    private javax.swing.JTextField sourcesField;
    private javax.swing.JLabel sourcesLabel;
    private javax.swing.JTable table;
    private javax.swing.JComboBox viewComboBox;
    private javax.swing.JLabel viewLabel;
    // End of variables declaration//GEN-END:variables

    private static final class ProjectKindItem {
        private final IteratorExtension.ProjectKind kind;
        ProjectKindItem(IteratorExtension.ProjectKind kind) {
            this.kind = kind;
        }

        @Override
        public String toString() {
            return getString("ProjectItemKind_"+kind);
        }
    }

    private static final class ProjectView {
        private boolean isSourceRoot;
        ProjectView(boolean isSourceRoot) {
            this.isSourceRoot = isSourceRoot;
        }

        @Override
        public String toString() {
            if (isSourceRoot) {
                return getString("ProjectViewSource");
            } else {
                return getString("ProjectViewLogical");
            }
        }
    }

    private static final class CheckBoxCellRenderer extends JCheckBox implements TableCellRenderer {
        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        private final JLabel emptyLabel = new JLabel();

	public CheckBoxCellRenderer() {
	    super();
	    setHorizontalAlignment(JLabel.CENTER);
            setBorderPainted(true);
            emptyLabel.setBorder(noFocusBorder);
            emptyLabel.setOpaque(true);
	}

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JComponent result;
            if (value == null) {
                result = emptyLabel;
            } else {
                setSelected(((Boolean)value));
                setEnabled(table.getModel().isCellEditable(row, column));
                result = this;
            }
            result.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            result.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            result.setBorder(hasFocus ? UIManager.getBorder("Table.focusCellHighlightBorder") : noFocusBorder); // NOI18N
            return result;
        }
    }

    private static final int BUTTON_WIDTH = 20;
    private static final class PathCellRenderer extends JPanel implements TableCellRenderer {
        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        private static final Border noFocusButtonBorder = new LineBorder(Color.GRAY, 1);
        private final JTextField field = new JTextField();
        private final JButton button = new JButton("..."); // NOI18N
        private final Color textFieldColor;
        private final Color redTextFieldColor;
        private final FileSystem fileSystem;

	public PathCellRenderer(FileSystem fileSystem) {
	    super();
            setLayout(new BorderLayout());
            add(field, BorderLayout.CENTER);
            field.setBorder(noFocusBorder);
            textFieldColor = field.getForeground();
            redTextFieldColor = new Color(field.getBackground().getRed(), textFieldColor.getGreen(), textFieldColor.getBlue());
            add(button, BorderLayout.EAST);
            button.setPreferredSize(new Dimension(BUTTON_WIDTH,5));
            button.setMaximumSize(new Dimension(BUTTON_WIDTH,20));
            button.setBorder(noFocusButtonBorder);
            this.fileSystem = fileSystem;
	}

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, final int row, final int column) {
            field.setText(value.toString());
            if (table.getModel().isCellEditable(row, column)) {
                field.setEnabled(true);
                button.setEnabled(true);
            } else {
                field.setEnabled(false);
                button.setEnabled(false);
            }
            FileObject dll = fileSystem.findResource(value.toString());
            if (dll != null && dll.isValid()) {
                field.setForeground(textFieldColor);
            } else {
                field.setForeground(redTextFieldColor);
            }
            setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            setBorder(hasFocus ? UIManager.getBorder("Table.focusCellHighlightBorder") : noFocusBorder); // NOI18N
            return this;
        }
    }

    private static final class CheckBoxTableCellEditor extends DefaultCellEditor {

        private CheckBoxTableCellEditor() {
            super(new JCheckBox());
	    ((JCheckBox)getEditorComponent()).setHorizontalAlignment(JLabel.CENTER);
            ((JCheckBox)getEditorComponent()).setBorderPainted(true);
        }

        public final JComponent getEditorComponent() {
            return editorComponent;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }

    private static final class MyDefaultTableModel extends DefaultTableModel {
        private final List<Boolean> uses = new ArrayList<>();
        private final List<String> names = new ArrayList<>();
        private final List<String> paths = new ArrayList<>();
        private final SelectBinaryPanelVisual parent;
        private final boolean searching;
        private MyDefaultTableModel(SelectBinaryPanelVisual parent, Map<String, String> dlls, String root, String binaryRoot, boolean searching){
            super(new String[] {
                SelectBinaryPanelVisual.getString("SelectBinaryPanelVisual.col0"), //NOI18N
                SelectBinaryPanelVisual.getString("SelectBinaryPanelVisual.col1"), //NOI18N
                SelectBinaryPanelVisual.getString("SelectBinaryPanelVisual.col2"), //NOI18N
            }, 0);
            this.searching = searching;
            for(Map.Entry<String,String> entry : dlls.entrySet()) {
                String dll = entry.getKey();
                names.add(dll);
                String path = entry.getValue();
                if (path == null) {
                    uses.add(Boolean.FALSE);
                    if (searching) {
                        paths.add(SelectBinaryPanelVisual.getString("SelectBinaryPanelVisual.col.searching")); //NOI18N
                    } else {
                        paths.add(SelectBinaryPanelVisual.getString("SelectBinaryPanelVisual.col.notfound")); //NOI18N
                    }
                } else {
                    if (isMyDll(path, root) || isMyDll(path, binaryRoot)) {
                        uses.add(Boolean.TRUE);
                    } else {
                        uses.add(Boolean.FALSE);
                    }
                    paths.add(path);
                }
            }
            this.parent = parent;
        }

        private boolean isMyDll(String path, String root) {
            if (root == null) {
                return false;
            }
            path = path.replace('\\','/'); //NOI18N
            root = root.replace('\\','/'); //NOI18N
            if (path.startsWith("/usr/lib/")) { //NOI18N
                return false;
            } else if (path.startsWith("/lib/")) { //NOI18N
                return false;
            } else if (path.startsWith("/usr/local/lib/")) { //NOI18N
                return false;
            } else if (path.startsWith(root)) {
                return true;
            } else {
                String[] p1 = path.split("/");  // NOI18N
                String[] p2 = root.split("/");  // NOI18N
                for(int i = 0; i < Math.min(p1.length - 1, p2.length); i++) {
                    if (!p1[i].equals(p2[i])) {
                        if (i > 3) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    if (i > 3) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Object getValueAt(int row, int column) {
            switch(column) {
                case 0: return uses.get(row);
                case 1: return names.get(row);
                case 2: return paths.get(row);
            }
            return super.getValueAt(row, column);
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            switch(column) {
                case 0:
                    uses.set(row, (Boolean)value);
                    parent.validateController();
                    return;
                case 1:
                    names.set(row, (String)value);
                    return;
                case 2:
                    paths.set(row, (String)value);
                    parent.validateController();
                    return;
            }
            super.setValueAt(value, row, column);
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch(column) {
                case 0: return Boolean.class;
                case 1: return String.class;
                case 2: return String.class;
            }
            return super.getColumnClass(column);
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getRowCount() {
            if (uses == null) {
                return 0;
            } else {
                return uses.size();
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (searching) {
                return false;
            }
            if (col == 1) {
                return false;
            } else {
                return true;
            }
        }
    }
}
