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
package org.netbeans.modules.cnd.toolchain.ui.options;

import java.awt.Color;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.spi.toolchain.CompilerSetFactory;
import org.netbeans.modules.cnd.toolchain.support.ToolchainUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 */
public final class AddCompilerSetPanel extends javax.swing.JPanel implements DocumentListener, Runnable {

    private DialogDescriptor dialogDescriptor = null;
    private final CompilerSetManager csm;
    private final boolean local;

    private final Object lastFoundLock = new Object();
    private CompilerSet lastFoundRemoteCompilerSet;
    private final List<CompilerSet> lastFoundRemoteCompilerSets = new ArrayList<CompilerSet>();

    private final Object compilerCheckExecutorLock = new Object();
    private final ScheduledExecutorService compilerCheckExecutor;
    private ScheduledFuture<?> compilerCheckTask;

    private final Color defaultLbErrColor;

    private static final Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N

    /** Creates new form AddCompilerSetPanel */
    public AddCompilerSetPanel(CompilerSetManager csm, String predefinedPath) {
        initComponents();
        lbError.setText(""); //NOI18N // in design mode the text present just to be visible :)
        defaultLbErrColor = lbError.getForeground();
        this.csm =  csm;
        this.local = csm.getExecutionEnvironment().isLocal();

        List<CompilerFlavor> list = CompilerFlavor.getFlavors(csm.getPlatform());
        for (CompilerFlavor cf : list) {
            if (cf.getToolchainDescriptor().isAbstract()) {
                continue;
            }
            cbFamily.addItem(new FlavorWrapper(cf));
        }
        // add unknown as well
        cbFamily.addItem(new FlavorWrapper(CompilerFlavor.getUnknown(csm.getPlatform())));
        tfName.setText(""); // NOI18N
        validateData();

        tfBaseDirectory.getDocument().addDocumentListener(AddCompilerSetPanel.this);
        tfName.getDocument().addDocumentListener(AddCompilerSetPanel.this);

        compilerCheckExecutor = Executors.newScheduledThreadPool(1);

        if (predefinedPath != null) {
            tfBaseDirectory.setText(predefinedPath);
        }
    }

    public static CompilerSet invokeMe(CompilerSetManager csm, String predefinedPath) {
        AddCompilerSetPanel panel = new AddCompilerSetPanel(csm, predefinedPath);
        ExecutionEnvironment execEnv = csm.getExecutionEnvironment();
        String title = execEnv.isRemote()
                ? NbBundle.getMessage(AddCompilerSetPanel.class, "NEW_TOOL_SET_TITLE_REMOTE", ExecutionEnvironmentFactory.toUniqueID(execEnv)) // NOI18N
                : NbBundle.getMessage(AddCompilerSetPanel.class, "NEW_TOOL_SET_TITLE"); // NOI18N
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, title);
        panel.setDialogDescriptor(dialogDescriptor);
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        return dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION ? panel.getCompilerSet() : null;
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                return compilerCheckExecutor.shutdownNow();
            }
        });
    }

    public void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
        this.dialogDescriptor = dialogDescriptor;
        dialogDescriptor.setValid(false);
    }

    private void handleBaseDirUpdate() {
        if (this.dialogDescriptor != null) {
            this.dialogDescriptor.setValid(false);
        }
        lastFoundRemoteCompilerSet = null;
        lastFoundRemoteCompilerSets.clear();
        final String path = tfBaseDirectory.getText().trim();
        if (path.length() > 0) {
            //go to non UI thread
            synchronized (compilerCheckExecutorLock) {
                if (compilerCheckTask != null) {
                    log.log(Level.FINEST, "Cancelling check for {0}", path);
                    compilerCheckTask.cancel(true);
                }
                log.log(Level.FINEST, "Submitting check for {0}", path);
                compilerCheckTask = compilerCheckExecutor.schedule(this,
                        local ? 500 : 1000, TimeUnit.MILLISECONDS);
            }
        } else {
            showError(NbBundle.getMessage(getClass(), "BASE_EMPTY"));
        }
    }

    /** check data dir */
    @Override
    public void run() {
        try {
            final String path = tfBaseDirectory.getText().trim();
            log.log(Level.FINEST, "Running check for {0}", path);
            showStatus(NbBundle.getMessage(getClass(), "CHECK_IN_PROGRESS", path));
            long time = System.currentTimeMillis();
            if (path.length() == 0) {
                log.log(Level.FINEST, "Done check for {0} - the path is empty", path);
                showError(NbBundle.getMessage(getClass(), "BASE_EMPTY"));
                return;
            }        
            if (!FileSystemProvider.isAbsolute(csm.getExecutionEnvironment(), path)) {
                showError(NbBundle.getMessage(getClass(), "BASE_RELATIVE"));
                return;
            }
            FileObject fileObject = FileSystemProvider.getFileObject(csm.getExecutionEnvironment(), path);
            if (fileObject == null || !fileObject.isValid() || !fileObject.isFolder()) {
                showError(NbBundle.getMessage(getClass(), "REMOTEBASE_INVALID_FOLDER", path));
                return;
            }
            if (local) {
                List<CompilerFlavor> flavors = CompilerSetFactory.getCompilerSetFlavor(path, csm.getPlatform());
                if (flavors.isEmpty() && CndFileUtils.createLocalFile(path).exists()) {
                    CompilerFlavor flavor = CompilerFlavor.getUnknown(csm.getPlatform());
                    if (flavor != null) {
                        flavors = Collections.<CompilerFlavor>singletonList(flavor);
                    }
                }
                if (flavors.size() > 0) {
                    flavors = moveBestFlavorFirst(flavors);
                    //String compilerSetName = getCompilerSetName().trim();
                    //CompilerSet cs = ToolchainUtilities.getCustomCompilerSet(path, flavors.get(0), compilerSetName, csm.getExecutionEnvironment());
                    CompilerSet cs = ToolchainUtilities.initCompilerSet(csm, csm.getExecutionEnvironment(), flavors.get(0), path);
                    synchronized (lastFoundLock) {
                        lastFoundRemoteCompilerSet = cs;
                        lastFoundRemoteCompilerSets.clear();
                        lastFoundRemoteCompilerSets.add(cs);
                        for(int i = 1; i < flavors.size(); i++) {
                            //cs = ToolchainUtilities.getCustomCompilerSet(path, flavors.get(i), compilerSetName, csm.getExecutionEnvironment());
                            cs = ToolchainUtilities.initCompilerSet(csm, csm.getExecutionEnvironment(), flavors.get(i), path);
                            lastFoundRemoteCompilerSets.add(cs);
                        }
                    }
                }
            } else {
                if (!checkConnection()) {
                    log.log(Level.FINEST, "Done check for {0} - no connection to host", path);
                    showError(NbBundle.getMessage(getClass(), "CANNOT_CONNECT"));
                    return;
                }
                List<CompilerSet> css = ToolchainUtilities.findRemoteCompilerSets(csm, path);
                //check if we are not shutdowned already
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    log.log(Level.FINEST, "Interrupted (1) check for {0}", path);
                }
                if (Thread.interrupted()) {
                    log.log(Level.FINEST, "Interrupted (2) check for {0}", path);
                    showError(NbBundle.getMessage(getClass(), "CANCELLED"));
                    return;
                }
                if (css.size() > 0) {
                    css = moveBestCompilerSetFirst(css);
                    synchronized (lastFoundLock) {
                        lastFoundRemoteCompilerSet = css.get(0);
                        lastFoundRemoteCompilerSets.clear();
                        for(int i = 0; i < css.size(); i++) {
                            lastFoundRemoteCompilerSets.add(css.get(i));
                        }
                    }
                } else {
                    CompilerFlavor flavor = CompilerFlavor.getUnknown(csm.getPlatform());
                    if (flavor != null) {
                        String baseDirectory = getBaseDirectory();
                        String compilerSetName = getCompilerSetName().trim();
                        CompilerSet cs = CompilerSetFactory.getCustomCompilerSet(baseDirectory, flavor, compilerSetName, csm.getExecutionEnvironment());
                        synchronized (lastFoundLock) {
                            lastFoundRemoteCompilerSet = cs;
                            lastFoundRemoteCompilerSets.clear();
                        }
                    }
                }
            }

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    showStatus(""); //NOI18N
                    if (lastFoundRemoteCompilerSet != null) {
                        cbFamily.setSelectedItem(new FlavorWrapper(lastFoundRemoteCompilerSet.getCompilerFlavor()));
                    } else {
                        cbFamily.setSelectedItem(new FlavorWrapper(CompilerFlavor.getUnknown(csm.getPlatform())));
                    }
                    updateDataFamily();
                    if (!dialogDescriptor.isValid()) {
                        tfName.setText("");
                    }
                }
            });
            if (log.isLoggable(Level.FINEST)) {
                time = System.currentTimeMillis() - time;
                log.log(Level.FINEST, "Done check for {0}; check took {1} ms", new Object[] { path, time });
            }
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
        }
    }

    private List<CompilerFlavor> moveBestFlavorFirst(List<CompilerFlavor> flavors) {
        CompilerFlavor best = null;
        for (CompilerFlavor flavor : flavors) {
            boolean found = true;
            for (CompilerFlavor flavor2 : flavors) {
                if (flavor2 != flavor) {
                    String subsitute = flavor2.getToolchainDescriptor().getSubstitute();
                    if (subsitute != null) {
                        if (subsitute.equals(flavor.getToolchainDescriptor().getName())) {
                            found = false;
                        }
                    }
                }
            }
            if (found) {
                best = flavor;
                break;
            }
        }
        if (best == null) {
            best = flavors.get(0);
        }
        List<CompilerFlavor> res = new ArrayList<CompilerFlavor>();
        res.add(best);
        for (CompilerFlavor flavor : flavors) {
            if (flavor != best) {
                res.add(flavor);
            }
        }
        return res;
    }
    
    private List<CompilerSet> moveBestCompilerSetFirst(List<CompilerSet> sets) {
        CompilerSet best = null;
        for (CompilerSet set : sets) {
            boolean found = true;
            for (CompilerSet set2 : sets) {
                if (set2 != set) {
                    String subsitute = set2.getCompilerFlavor().getToolchainDescriptor().getSubstitute();
                    if (subsitute != null) {
                        if (subsitute.equals(set.getCompilerFlavor().getToolchainDescriptor().getName())) {
                            found = false;
                        }
                    }
                }
            }
            if (found) {
                best = set;
                break;
            }
        }
        if (best == null) {
            best = sets.get(0);
        }
        List<CompilerSet> res = new ArrayList<CompilerSet>();
        res.add(best);
        for (CompilerSet flavor : sets) {
            if (flavor != best) {
                res.add(flavor);
            }
        }
        return res;
    }

    private boolean checkConnection() {
        return ConnectionManager.getInstance().connect(csm.getExecutionEnvironment());
    }

    private void updateBaseDirectory() {
        String seed = null;
        final String chooser_key = "AddCompilerSet"; //NOI18N
        if (tfBaseDirectory.getText().trim().length() > 0) {
            seed = tfBaseDirectory.getText().trim();
        } else if (RemoteFileChooserUtil.getCurrentChooserFile(chooser_key, csm.getExecutionEnvironment()) != null) {
            seed = RemoteFileChooserUtil.getCurrentChooserFile(chooser_key, csm.getExecutionEnvironment());
        } else {
            seed = ToolsUtils.getDefaultDirectory(csm.getExecutionEnvironment());
        }
        JFileChooser fileChooser = new FileChooserBuilder(csm.getExecutionEnvironment()).createFileChooser(seed);
        fileChooser.setDialogTitle(NbBundle.getMessage(getClass(), "SELECT_BASE_DIRECTORY_TITLE"));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String dirPath = fileChooser.getSelectedFile().getPath();
        RemoteFileChooserUtil.setCurrentChooserFile(chooser_key, dirPath, csm.getExecutionEnvironment());
        tfBaseDirectory.setText(dirPath);
        //updateDataBaseDir();
    }

    private void updateDataFamily() {
        CompilerFlavor flavor = ((FlavorWrapper) cbFamily.getSelectedItem()).flavor;
        String template;
        synchronized (lastFoundLock) {
            if (lastFoundRemoteCompilerSet != null && !CompilerSet.UNKNOWN.equals(lastFoundRemoteCompilerSet.getName())) {
                template = lastFoundRemoteCompilerSet.getName();
            } else {
                template = flavor.getToolchainDescriptor().getName();
            }
        }
        String suggestedName;
        int n = 0;
        while (true) {
            suggestedName = template + (n > 0 ? ("_" + n) : ""); // NOI18N
            if (csm.getCompilerSet(suggestedName) != null) {
                n++;
            } else {
                break;
            }
        }
        tfName.setText(suggestedName);
        synchronized (lastFoundLock) {
            if (lastFoundRemoteCompilerSet != null &&  !CompilerSet.UNKNOWN.equals(lastFoundRemoteCompilerSet.getName())){
                boolean foundFlavor = false;
                for(CompilerSet cs : lastFoundRemoteCompilerSets) {
                    if (flavor.equals(cs.getCompilerFlavor())) {
                        lastFoundRemoteCompilerSet = cs;
                        foundFlavor = true;
                        break;
                    }
                }
                String compilerSetName = getCompilerSetName().trim();
                if (foundFlavor) {
                    CompilerSet cs = ToolchainUtilities.createCopy(lastFoundRemoteCompilerSet, 
                            csm.getExecutionEnvironment(), flavor, lastFoundRemoteCompilerSet.getDirectory(), compilerSetName, null,
                                    lastFoundRemoteCompilerSet.isAutoGenerated(), false, null, null);
                    lastFoundRemoteCompilerSet = cs;
                } else {
                    CompilerSet cs = CompilerSetFactory.getCustomCompilerSet(lastFoundRemoteCompilerSet.getDirectory(), flavor, compilerSetName, csm.getExecutionEnvironment());
                    lastFoundRemoteCompilerSet = cs;
                }
            } else if (lastFoundRemoteCompilerSet != null) {
                String compilerSetName = getCompilerSetName().trim();
                CompilerSet cs = CompilerSetFactory.getCustomCompilerSet(lastFoundRemoteCompilerSet.getDirectory(), flavor, compilerSetName, csm.getExecutionEnvironment());
                lastFoundRemoteCompilerSet = cs;
                
            }
        }
        validateData();
    }

    private void validateData() {
        boolean valid = true;
        boolean enableFamily = true;
        final String path = tfBaseDirectory.getText().trim();
        showStatus(""); // NOI18N
        synchronized (lastFoundLock) {
            if (lastFoundRemoteCompilerSet == null) {
                valid = false;
                enableFamily = false;
                showError(NbBundle.getMessage(getClass(), path.length() == 0 ? "BASE_EMPTY" : "REMOTEBASE_INVALID", path));
            } else {
                if (CompilerSet.UNKNOWN.equals(lastFoundRemoteCompilerSet.getName())) {
                    valid = false;
                    showError(NbBundle.getMessage(getClass(), "REMOTEBASE_INVALID", path));
                }
            }
        }
        cbFamily.setEnabled(enableFamily);
        tfName.setEnabled(valid);

        String compilerSetName = CndPathUtilities.replaceOddCharacters(tfName.getText().trim(), '_');
        if (valid) {
            if (compilerSetName.length() == 0) { // NOI18N
                valid = false;
                showError(NbBundle.getMessage(getClass(),"NAME_EMPTY"));
            }
            else if (compilerSetName.contains("|")) { // NOI18N
                valid = false;
                showError(NbBundle.getMessage(getClass(),"NAME_INVALID", compilerSetName));
            }
        }

        if (valid && csm.getCompilerSet(compilerSetName.trim()) != null) {
            valid = false;
            showError(NbBundle.getMessage(getClass(),"TOOLNAME_ALREADY_EXISTS", compilerSetName));
        }

        if (dialogDescriptor != null) {
            dialogDescriptor.setValid(valid);
        }
    }

    private void showError(String message) {
        lbError.setForeground(Color.RED);
        lbError.setText(message);
    }

    private void showStatus(String message) {
        lbError.setForeground(defaultLbErrColor);
        lbError.setText(message);
    }

    private void handleUpdate(DocumentEvent e) {
        if (e.getDocument() == tfBaseDirectory.getDocument()) {
            handleBaseDirUpdate();
        } else {
            validateData();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        handleUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        handleUpdate(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    public String getBaseDirectory() {
        return tfBaseDirectory.getText();
    }

    private String getCompilerSetName() {
        return CndPathUtilities.replaceOddCharacters(tfName.getText().trim(), '_');
    }

    public CompilerSet getCompilerSet() {
        String compilerSetName = getCompilerSetName().trim();
        synchronized (lastFoundLock) {
            if (lastFoundRemoteCompilerSet != null){
                ToolchainUtilities.setCSName(lastFoundRemoteCompilerSet, compilerSetName);
                return lastFoundRemoteCompilerSet;
            }
            return null;
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

        infoLabel = new javax.swing.JLabel();
        lbBaseDirectory = new javax.swing.JLabel();
        tfBaseDirectory = new javax.swing.JTextField();
        btBaseDirectory = new javax.swing.JButton();
        lbFamily = new javax.swing.JLabel();
        cbFamily = new javax.swing.JComboBox();
        lbName = new javax.swing.JLabel();
        tfName = new javax.swing.JTextField();
        lbError = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(580, 190));
        setPreferredSize(new java.awt.Dimension(580, 190));
        setLayout(new java.awt.GridBagLayout());

        infoLabel.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.taInfo.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 12);
        add(infoLabel, gridBagConstraints);

        lbBaseDirectory.setLabelFor(tfBaseDirectory);
        org.openide.awt.Mnemonics.setLocalizedText(lbBaseDirectory, org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.lbBaseDirectory.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 6);
        add(lbBaseDirectory, gridBagConstraints);

        tfBaseDirectory.setColumns(40);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 283;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(tfBaseDirectory, gridBagConstraints);
        tfBaseDirectory.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.tfBaseDirectory.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btBaseDirectory, org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.btBaseDirectory.text")); // NOI18N
        btBaseDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btBaseDirectoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipady = -6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 12);
        add(btBaseDirectory, gridBagConstraints);

        lbFamily.setLabelFor(cbFamily);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/options/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lbFamily, bundle.getString("AddCompilerSetPanel.lbFamily.text")); // NOI18N
        lbFamily.setToolTipText(bundle.getString("AddCompilerSetPanel.lbFamily.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 6);
        add(lbFamily, gridBagConstraints);

        cbFamily.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbFamilyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = -4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 12);
        add(cbFamily, gridBagConstraints);

        lbName.setLabelFor(tfName);
        org.openide.awt.Mnemonics.setLocalizedText(lbName, org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.lbName.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 6);
        add(lbName, gridBagConstraints);

        tfName.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 12);
        add(tfName, gridBagConstraints);
        tfName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.tfName.AccessibleContext.accessibleDescription")); // NOI18N

        lbError.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.lbError.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 12);
        add(lbError, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void cbFamilyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbFamilyActionPerformed
    updateDataFamily();
}//GEN-LAST:event_cbFamilyActionPerformed

private void btBaseDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBaseDirectoryActionPerformed
    updateBaseDirectory();
}//GEN-LAST:event_btBaseDirectoryActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btBaseDirectory;
    private javax.swing.JComboBox cbFamily;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel lbBaseDirectory;
    private javax.swing.JLabel lbError;
    private javax.swing.JLabel lbFamily;
    private javax.swing.JLabel lbName;
    private javax.swing.JTextField tfBaseDirectory;
    private javax.swing.JTextField tfName;
    // End of variables declaration//GEN-END:variables

    private static final class FlavorWrapper {
        private final CompilerFlavor flavor;
        private FlavorWrapper(CompilerFlavor flavor) {
            this.flavor = flavor;
        }

        @Override
        public String toString() {
            return flavor.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FlavorWrapper) {
                return flavor == ((FlavorWrapper)obj).flavor;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 31 * hash + (this.flavor != null ? this.flavor.hashCode() : 0);
            return hash;
        }
    }
}
