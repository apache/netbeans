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
package org.netbeans.modules.options.export;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.swing.outline.CheckRenderDataProvider;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RowModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.modules.Places;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;

/**
 * Export/import options panel.
 * @author Jiri Skrivanek
 */
public final class OptionsChooserPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(OptionsChooserPanel.class.getName());
    private static final Icon NODE_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/options/export/defaultNode.gif", true);  //NOI18N
    private static final Color DISABLED_COLOR = UIManager.getColor("Label.disabledForeground");  //NOI18N
    private DialogDescriptor dialogDescriptor;
    private PanelType panelType;
    private OptionsExportModel optionsExportModel;
    private static TreeModel treeModel;
    private static OptionsTreeDataProvider treeDataProvider;
    private static RequestProcessor.Task exportTask;
    private static final Icon OPTIONS_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/options/export/options.png", true);  //NOI18N

    /** To distinguish between import and export panels. */
    private enum PanelType {

        EXPORT, IMPORT
    };

    private OptionsChooserPanel() {
        initComponents();
        Mnemonics.setLocalizedText(btnBrowse, NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.btnBrowse"));
        Mnemonics.setLocalizedText(lblFile, NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.lblFile.text"));
        Mnemonics.setLocalizedText(lblHint, NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.lblHint.text"));
    }

    private void setOptionsExportModel(OptionsExportModel optionsExportModel) {
        this.optionsExportModel = optionsExportModel;
    }

    private OptionsExportModel getOptionsExportModel() {
        return optionsExportModel;
    }
    
    private static String getDefaultUserdirRoot() {
        String defaultUserdirRoot = System.getProperty("netbeans.default_userdir_root"); // NOI18N
        if (defaultUserdirRoot == null) {
	    defaultUserdirRoot = System.getProperty("user.home");  //NOI18N
	}
        return defaultUserdirRoot;
    }

    /** Shows panel for export of options. */
    @NbBundle.Messages({"ProgressHandle_Export_DisplayName=Exporting Options",
	"# {0} - path where the exported options are saved",
	"Export_Notification_DetailsText=File saved at {0}"})
    public static void showExportDialog() {
	if(exportTask != null && !exportTask.isFinished()) {
	    return;
	}
        LOGGER.fine("showExportDialog");  //NOI18N
	File sourceUserdir = Places.getUserDirectory();
        final OptionsChooserPanel optionsChooserPanel = new OptionsChooserPanel();
        optionsChooserPanel.panelType = PanelType.EXPORT;
        optionsChooserPanel.setOptionsExportModel(new OptionsExportModel(sourceUserdir));
        optionsChooserPanel.loadOptions();
        optionsChooserPanel.txtFile.setText(getDefaultUserdirRoot().concat(File.separator));
        optionsChooserPanel.txtFile.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                optionsChooserPanel.dialogDescriptor.setValid(optionsChooserPanel.isPanelValid());
            }

            public void removeUpdate(DocumentEvent e) {
                optionsChooserPanel.dialogDescriptor.setValid(optionsChooserPanel.isPanelValid());
            }

            public void changedUpdate(DocumentEvent e) {
                optionsChooserPanel.dialogDescriptor.setValid(optionsChooserPanel.isPanelValid());
            }
        });

        DialogDescriptor dd = new DialogDescriptor(
                optionsChooserPanel,
                NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.export.title"),
                true,
                new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        // add bottom user notification area
        dd.createNotificationLineSupport();
        dd.setValid(false);
        ExportConfirmationPanel exportConfirmationPanel = null;
        if (!ExportConfirmationPanel.getSkipOption()) {
            exportConfirmationPanel = new ExportConfirmationPanel();
            final ExportConfirmationPanel finalExportConfirmationPanel = exportConfirmationPanel;
            dd.setButtonListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (e.getSource() == DialogDescriptor.OK_OPTION) {
                        String passwords = NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.export.passwords.displayName");
                        Enumeration dfs = ((DefaultMutableTreeNode) treeModel.getRoot()).depthFirstEnumeration();
                        while (dfs.hasMoreElements()) {
                            Object nodeObj = dfs.nextElement();
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode)nodeObj;
                            Object userObject = node.getUserObject();
                            if (userObject instanceof OptionsExportModel.Item) {
                                if(((OptionsExportModel.Item) userObject).getDisplayName().equals(passwords)) {
                                    if(treeDataProvider.isSelected(nodeObj)) {
                                        // show confirmation dialog when user click OK and All/Passwords/Passwords item is selected
                                        finalExportConfirmationPanel.showConfirmation();
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
        optionsChooserPanel.setDialogDescriptor(dd);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);

        if (DialogDescriptor.OK_OPTION.equals(dd.getValue())) {
            if (exportConfirmationPanel != null && !exportConfirmationPanel.confirmed()) {
                LOGGER.fine("Export canceled.");  //NOI18N
                return;
            }
            
            Action save = Actions.forID("Window", "org.netbeans.core.windows.actions.SaveWindowsAction"); // NOI18N
            if (save != null) {
                save.actionPerformed(new ActionEvent(optionsChooserPanel, 0, ""));
            }
            
            String selectedFilePath = optionsChooserPanel.getSelectedFilePath();
            if (selectedFilePath.endsWith("/")) {  //NOI18N
                //name zip file after last folder
                selectedFilePath = selectedFilePath.substring(0, selectedFilePath.lastIndexOf("/"));  //NOI18N
                String zipName = selectedFilePath.substring(selectedFilePath.lastIndexOf("/") + 1);  //NOI18N
                selectedFilePath = selectedFilePath.concat("/").concat(zipName).concat(".zip");  //NOI18N
            }
            if (!selectedFilePath.endsWith(".zip")) {  //NOI18N
                selectedFilePath = selectedFilePath.concat(".zip");  //NOI18N
            }
            final String targetPath = selectedFilePath;
            RequestProcessor RP = new RequestProcessor("OptionsChooserPanel Export", 1); // NOI18N
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // to avoid false possitives during import, find the items that are explicitly selected by the user for export
                    Enumeration dfs = ((DefaultMutableTreeNode) treeModel.getRoot()).depthFirstEnumeration();
                    ArrayList<String> enabledItems = new ArrayList<String>();
                    while (dfs.hasMoreElements()) {
                        Object userObject = ((DefaultMutableTreeNode) dfs.nextElement()).getUserObject();
                        if (userObject instanceof OptionsExportModel.Category) {
                            OptionsExportModel.Category category = (OptionsExportModel.Category) userObject;
                            if(!category.getState().equals(OptionsExportModel.State.DISABLED)) {
                                List<OptionsExportModel.Item> items = ((OptionsExportModel.Category) userObject).getItems();
                                for(OptionsExportModel.Item item : items) {
                                    if(item.isEnabled()) {
                                        enabledItems.add(category.getDisplayName().concat(item.getDisplayName()));
                                    }
                                }
                            }
                        }
                    }
                    optionsChooserPanel.getOptionsExportModel().doExport(new File(targetPath), enabledItems);
                    NotificationDisplayer.getDefault().notify(
                        NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.export.status.text"), //NOI18N
                        OPTIONS_ICON, Bundle.Export_Notification_DetailsText(targetPath), null);
                    LOGGER.fine("Export finished.");  //NOI18N
                }
            };
	    exportTask = RP.create(runnable);

	    final ProgressHandle ph = ProgressHandle.createHandle(Bundle.ProgressHandle_Export_DisplayName(), exportTask);
	    exportTask.addTaskListener(new TaskListener() {
		@Override
		public void taskFinished(org.openide.util.Task task) {
		    ph.finish();
		}
	    });

	    ph.start();
	    exportTask.schedule(0);
        }
    }

    @NbBundle.Messages({
        "OPT_RestartAfterImport=false"
    })
    /** Shows panel for import of options. */
    public static void showImportDialog() {
        LOGGER.fine("showImportDialog");  //NOI18N
        OptionsChooserPanel optionsChooserPanel = new OptionsChooserPanel();
        optionsChooserPanel.txtFile.setEditable(false);
        Mnemonics.setLocalizedText(optionsChooserPanel.lblFile, NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.lblFile.text"));
        Mnemonics.setLocalizedText(optionsChooserPanel.lblHint, NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.lblHint.text"));
        optionsChooserPanel.panelType = PanelType.IMPORT;

        DialogDescriptor dd = new DialogDescriptor(
                optionsChooserPanel,
                NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.title"),
                true,
                new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        dd.createNotificationLineSupport();
        dd.setValid(false);
        boolean ok;
        final boolean willRestart = "true".equals(Bundle.OPT_RestartAfterImport()); // NOI18N
        final ImportConfirmationPanel confirmationPanel = new ImportConfirmationPanel();
        dd.setButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (willRestart && e.getSource() == DialogDescriptor.OK_OPTION) {
                    // show confirmation dialog when user click OK
                    confirmationPanel.showConfirmation();
                }
            }
        });
        optionsChooserPanel.setDialogDescriptor(dd);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        ok = DialogDescriptor.OK_OPTION.equals(dd.getValue());
        if (willRestart) {
            if (!confirmationPanel.confirmed()) {
                LOGGER.fine("Import canceled.");  //NOI18N
                ok = false;
            }
        }

        if (ok) {
            // do import
            File targetUserdir = Places.getUserDirectory();
            try {
                optionsChooserPanel.getOptionsExportModel().doImport(targetUserdir);
            } catch (IOException ioe) {
                // report exception and return if import failed
                Exceptions.attachLocalizedMessage(ioe,
                        NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.error"));
                LOGGER.log(Level.SEVERE, ioe.getMessage(), ioe);
                return;
            }
            LOGGER.fine("Import finished.");  //NOI18N
            if (willRestart) { // NOI18N
                // restart IDE
                LifecycleManager.getDefault().markForRestart();
                LifecycleManager.getDefault().exit();
            }
            try {
                FileUtil.getConfigRoot().getFileSystem().refresh(true);
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
            Action reload = Actions.forID("Window", "org.netbeans.core.windows.actions.ReloadWindowsAction");
            if (reload != null) {
                reload.actionPerformed(new ActionEvent(optionsChooserPanel, 0, ""));
            }
        }
    }

    /** Loading of available options for export/import moved from AWT thread
     * and a message is shown in the meantime (see #163142). */
    private void loadOptions() {
        assert SwingUtilities.isEventDispatchThread() : "Should be called from AWT thread only.";  //NOI18N
        JLabel loadingLabel = new JLabel(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.loading"));
        loadingLabel.setHorizontalAlignment(JLabel.CENTER);
        scrollPaneOptions.setViewportView(loadingLabel);
        Thread loadingThread = new Thread("Export/import options loading") {  //NOI18N

            @Override
            public void run() {
                OptionsExportModel model = getOptionsExportModel();
                LOGGER.fine("Loading started: " + getOptionsExportModel());  //NOI18N
                final TreeModel treeModel = createOptionsTreeModel();
                LOGGER.fine("Loading finished: " + getOptionsExportModel());  //NOI18N
                // change UI only if model not changed in between
                if (model == getOptionsExportModel()) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            LOGGER.fine("Changing options.");
                            scrollPaneOptions.setViewportView(getOutline(treeModel));
                            if (panelType == PanelType.IMPORT) { // Check All checkboxes by default when importing
                                Object root = treeModel.getRoot();
                                if (root != null) {
                                    treeDataProvider.setSelected(root, Boolean.TRUE);
                                }
                            }
                            dialogDescriptor.setValid(isPanelValid());
                        }
                    });
                }
            }
        };
        loadingThread.start();
    }

    /** Returns outline view for displaying options for export/import. */
    private Outline getOutline(TreeModel treeModel) {
        Outline outline = new Outline();
        outline.setModel(DefaultOutlineModel.createOutlineModel(
                treeModel,
                new OptionsRowModel(),
                true,
                NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.outline.header.tree")));
        treeDataProvider = new OptionsTreeDataProvider();
        outline.setRenderDataProvider(treeDataProvider);
        //outline.setRootVisible(false);
        outline.getTableHeader().setReorderingAllowed(false);
        outline.setColumnHidingAllowed(false);
        // a11y
        outline.getAccessibleContext().setAccessibleName(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.outline.AN"));
        outline.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.outline.AD"));
        lblHint.setLabelFor(outline);

        final Outline out = outline;
        outline.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    int[] rows = out.getSelectedRows();
                    for (int row : rows) {
                        if (row >= 0) {
                            Object node = out.getValueAt(row, 0);
                            Boolean isSelected = treeDataProvider.isSelected(node);
                            if(isSelected == null) { // node is Category or Root and is partially selected
                                treeDataProvider.setSelected(node, Boolean.FALSE);
                            } else if (treeDataProvider.isCheckEnabled(node)) {
                                treeDataProvider.setSelected(node, !isSelected);
                            }
                        }
                    }
                }
            }
        });

        return outline;
    }

    /** Returns tree model based on current state of OptionsExportModel. Sets treeModel field. */
    private TreeModel createOptionsTreeModel() {
        LOGGER.fine("getOptionsTreeModel - " + getOptionsExportModel());  //NOI18N
        String allLabel = NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.outline.all");
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(allLabel);
        ArrayList<String> enabledItems = new ArrayList<String>();
        double buildNumberDuringExport = 0;
        double currentBuildNumber;
        String nbBuildNumber = System.getProperty("netbeans.buildnumber"); // NOI18N
        try {
            currentBuildNumber = Double.parseDouble(getOptionsExportModel().parseBuildNumber(nbBuildNumber));
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.INFO, "Could not parse netbeans.buildnumber: {0}", nbBuildNumber);  //NOI18N
            currentBuildNumber = 201403101706.0;  // default to build date of 8.0 version
        }
        if (panelType == PanelType.IMPORT) {
            // If the returned value is null, it means that there is no enabledItems.info in the importing zip file
            // indicating it was created from a version prior to 7.4
            enabledItems = getOptionsExportModel().getEnabledItemsDuringExport(new File(txtFile.getText()));
            // If the returned value is -1, it means that there is no build.info in the importing zip file or userdir
            // or that there was an exception while trying to parse the build number
            buildNumberDuringExport = getOptionsExportModel().getBuildNumberDuringExport(new File(txtFile.getText()));
        }
        for (OptionsExportModel.Category category : getOptionsExportModel().getCategories()) {
            LOGGER.fine("category=" + category);  //NOI18N
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category);
            List<OptionsExportModel.Item> items = category.getItems();
            for (OptionsExportModel.Item item : items) {
                LOGGER.fine("    item=" + item);  //NOI18N
                if (panelType == PanelType.EXPORT || item.isApplicable()) {
                    // do not show not applicable items for import
                    if (panelType == PanelType.IMPORT) {
                        // avoid false possitives, check the items that were explicitly selected by the user during export
                        if (enabledItems == null || enabledItems.contains(category.getDisplayName().concat(item.getDisplayName()))
                                // special treatment as Projects category was introduced after 7.4, so when trying to import options,
                                // exported from 7.4, into 8.0 or later there would be no Project category in enabledItems.info file.
                                // There must be GeneralAll Other Unspecified present in the extracted enabledItems.info file though.
                                || (category.getDisplayName().equals("Projects") && enabledItems.contains("GeneralAll Other Unspecified")  // NOI18N
                                    && buildNumberDuringExport >= 201310111528.0 // build date of 7.4 version
                                    && currentBuildNumber >= 201403101706.0)) { // build date of 8.0 version
                            categoryNode.add(new DefaultMutableTreeNode(item));
                        }
                    } else {
                        categoryNode.add(new DefaultMutableTreeNode(item));
                    }
                }
            }
            if (categoryNode.getChildCount() != 0) {
                // do not show category node if it has no children
                rootNode.add(categoryNode);
                updateCategoryNode(categoryNode);
            }
        }
        if (rootNode.getChildCount() == 0) {
            rootNode = null;
        }
        treeModel = new DefaultTreeModel(rootNode);
        return treeModel;
    }

    private String getSelectedFilePath() {
        return txtFile.getText();
    }

    private void setDialogDescriptor(DialogDescriptor dd) {
        this.dialogDescriptor = dd;
    }

    /** Returns true if all user inputs in this panel are valid. */
    private boolean isPanelValid() {
        if (panelType == PanelType.IMPORT) {
            if (txtFile.getText().length() == 0) {
                dialogDescriptor.getNotificationLineSupport().setWarningMessage(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.file.warning"));
            } else if (getOptionsExportModel().getState() == OptionsExportModel.State.DISABLED) {
                dialogDescriptor.getNotificationLineSupport().setWarningMessage(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.nooption.warning"));
            } else {
                dialogDescriptor.getNotificationLineSupport().clearMessages();
                return true;
            }
        } else {
            if (txtFile.getText().length() == 0) {  //NOI18N
                dialogDescriptor.getNotificationLineSupport().setWarningMessage(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.file.warning"));
            } else if (getOptionsExportModel().getState() == OptionsExportModel.State.DISABLED) {
                dialogDescriptor.getNotificationLineSupport().setWarningMessage(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.nooption.warning"));
            } else {
                String text = txtFile.getText();
                File parent = text.endsWith("/") ? new File(text) : new File(text).getParentFile();
                if(parent == null) {
                    dialogDescriptor.getNotificationLineSupport().setWarningMessage(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.noparent.warning"));
                } else {
                    if(parent.canWrite()) {
                        dialogDescriptor.getNotificationLineSupport().clearMessages();
                        return true;
                    } else {
                        dialogDescriptor.getNotificationLineSupport().setWarningMessage(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.nowrite.warning"));
                    }
                }                
            }
        }
        return false;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblHint = new javax.swing.JLabel();
        scrollPaneOptions = new javax.swing.JScrollPane();
        lblFile = new javax.swing.JLabel();
        txtFile = new javax.swing.JTextField();
        btnBrowse = new javax.swing.JButton();

        lblHint.setText(org.openide.util.NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.lblHint.text")); // NOI18N

        lblFile.setLabelFor(txtFile);
        lblFile.setText(org.openide.util.NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.lblFile.text")); // NOI18N

        btnBrowse.setText(org.openide.util.NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.btnBrowse")); // NOI18N
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPaneOptions, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFile, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBrowse))
                    .addComponent(lblHint, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFile)
                    .addComponent(txtFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowse))
                .addGap(18, 18, 18)
                .addComponent(lblHint)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPaneOptions, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                .addContainerGap())
        );

        txtFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.txtFile.AD")); // NOI18N
        btnBrowse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.btnBrowse.AN")); // NOI18N
        btnBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.btnBrowse.AD")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.AD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        FileChooserBuilder fileChooserBuilder = new FileChooserBuilder(OptionsChooserPanel.class);
	String defaultUserdirRoot = getDefaultUserdirRoot(); // NOI18N
        fileChooserBuilder.setDefaultWorkingDirectory(new File(defaultUserdirRoot));
	fileChooserBuilder.setFileFilter(new FileNameExtensionFilter("*.zip", "zip"));  //NOI18N
        fileChooserBuilder.setAcceptAllFileFilterUsed(false);
        String approveText = NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.file.chooser.approve");
        fileChooserBuilder.setApproveText(approveText);
        if (panelType == PanelType.IMPORT) {
            fileChooserBuilder.setTitle(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.file.chooser.title"));
            File selectedFile = fileChooserBuilder.showOpenDialog();
            if (selectedFile != null) {
                if (selectedFile.isDirectory() && !new File(selectedFile, "config").exists()) {  //NOI18N
                    // #248610 - notify the user that the selected folder is not a valid userdir, as trying to load it might cause OOME
                    String message = NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.invalid.userdir", selectedFile);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                    return;
                }
                txtFile.setText(selectedFile.getAbsolutePath());
                setOptionsExportModel(new OptionsExportModel(selectedFile));
                loadOptions();
            }
        } else {
            fileChooserBuilder.setTitle(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.file.chooser.title"));
            File selectedFile = fileChooserBuilder.showSaveDialog();
            if (selectedFile != null) {
                String selectedFileName = selectedFile.getAbsolutePath();
                if (!selectedFileName.endsWith(".zip")) {  //NOI18N
                    selectedFileName += ".zip";  //NOI18N
                }
                txtFile.setText(selectedFileName);
                dialogDescriptor.setValid(isPanelValid());
            }
        }
    }//GEN-LAST:event_btnBrowseActionPerformed


    /** Defines presentation of table. */
    private class OptionsRowModel implements RowModel {

        public Class getColumnClass(int column) {
            return null;
        }

        public int getColumnCount() {
            return 0;
        }

        public String getColumnName(int column) {
            return null;
        }

        public Object getValueFor(Object node, int column) {
            return null;
        }

        public boolean isCellEditable(Object node, int column) {
            return false;
        }

        public void setValueFor(Object node, int column, Object value) {
        }
    }

    /** Update state of category node according to state of sub items. */
    private static void updateCategoryNode(DefaultMutableTreeNode categoryNode) {
        int enabledCount = 0;
        int applicableCount = 0;
        for (int i = 0; i < categoryNode.getChildCount(); i++) {
            Object userObject = ((DefaultMutableTreeNode) categoryNode.getChildAt(i)).getUserObject();
            OptionsExportModel.Item item = (OptionsExportModel.Item) userObject;
            if (item.isApplicable()) {
                applicableCount++;
                if (item.isEnabled()) {
                    enabledCount++;
                }
            }
        }
        Object userObject = categoryNode.getUserObject();
        OptionsExportModel.Category category = ((OptionsExportModel.Category) userObject);
        if (enabledCount == 0) {
            category.setState(OptionsExportModel.State.DISABLED);
        } else if (enabledCount == applicableCount) {
            category.setState(OptionsExportModel.State.ENABLED);
        } else {
            category.setState(OptionsExportModel.State.PARTIAL);
        }
    }

    /** Defines visual appearance of tree. */
    private class OptionsTreeDataProvider implements CheckRenderDataProvider {

        public Color getBackground(Object node) {
            return null;
        }

        public String getDisplayName(Object node) {
            if (node == null) {
                return null;
            }
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
            if (userObject instanceof OptionsExportModel.Category) {
                return ((OptionsExportModel.Category) userObject).getDisplayName();
            }
            if (userObject instanceof OptionsExportModel.Item) {
                return ((OptionsExportModel.Item) userObject).getDisplayName();
            }
            // root node
            return node.toString();
        }

        /** Return like disabled color for not applicable items. */
        public Color getForeground(Object node) {
            if (node == null) {
                return null;
            }
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
            if (userObject instanceof OptionsExportModel.Category) {
                if (!((OptionsExportModel.Category) userObject).isApplicable()) {
                    return DISABLED_COLOR;
                }
            } else if (userObject instanceof OptionsExportModel.Item) {
                if (!((OptionsExportModel.Item) userObject).isApplicable()) {
                    return DISABLED_COLOR;
                }
            }
            return null;
        }

        public Icon getIcon(Object o) {
            return NODE_ICON;
        }

        public String getTooltipText(Object o) {
            return null;
        }

        public boolean isHtmlDisplayName(Object o) {
            return false;
        }

        public boolean isCheckable(Object node) {
            return true;
        }

        /** Disabled for not applicable items. */
        public boolean isCheckEnabled(Object node) {
            if (node == null) {
                return true;
            }
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
            if (userObject instanceof OptionsExportModel.Category) {
                if (!((OptionsExportModel.Category) userObject).isApplicable()) {
                    return false;
                }
            } else if (userObject instanceof OptionsExportModel.Item) {
                if (!((OptionsExportModel.Item) userObject).isApplicable()) {
                    return false;
                }
            }
            return true;
        }

        public Boolean isSelected(Object node) {
            if (node == null) {
                return false;
            }
            if (((DefaultMutableTreeNode) node).isRoot()) {
                return getOptionsExportModel().getState().toBoolean();
            }
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
            if (userObject instanceof OptionsExportModel.Category) {
                return ((OptionsExportModel.Category) userObject).getState().toBoolean();
            } else if (userObject instanceof OptionsExportModel.Item) {
                return ((OptionsExportModel.Item) userObject).isEnabled();
            }
            // should not happen
            assert false : "Node not recognized " + node;  //NOI18N
            return false;
        }

        public void setSelected(Object node, Boolean selected) {
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
            if (((DefaultMutableTreeNode) node).isRoot()) {
                getOptionsExportModel().setState(OptionsExportModel.State.valueOf(selected));
            } else if (userObject instanceof OptionsExportModel.Category) {
                ((OptionsExportModel.Category) userObject).setState(OptionsExportModel.State.valueOf(selected));
            } else if (userObject instanceof OptionsExportModel.Item) {
                ((OptionsExportModel.Item) userObject).setEnabled(selected);
                // update parent category
                Object parent = ((TreeNode) node).getParent();
                updateCategoryNode((DefaultMutableTreeNode) parent);
            }
            // fire an event to refresh parent or child nodes
            ((DefaultTreeModel) treeModel).nodeChanged((TreeNode) node);
            dialogDescriptor.setValid(isPanelValid());
            scrollPaneOptions.repaint();
        }
    }

    /** FileFile for single extension. Remove it when JDK5 is obsolete and
     * use FileNameExtensionFilter from JDK6. */
    private static class FileNameExtensionFilter extends FileFilter {

        private final String description;
        private final String lowerCaseExtension;

        public FileNameExtensionFilter(String description, String extension) {
            assert extension != null;
            this.description = description;
            this.lowerCaseExtension = extension.toLowerCase();
        }

        @Override
        public boolean accept(File f) {
            if (f != null) {
                if (f.isDirectory()) {
                    return true;
                }
                String fileName = f.getName();
                int i = fileName.lastIndexOf('.');
                if (i > 0 && i < fileName.length() - 1) {
                    String desiredExtension = fileName.substring(i + 1).toLowerCase();
                    if (desiredExtension.equals(lowerCaseExtension)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.JLabel lblFile;
    private javax.swing.JLabel lblHint;
    private javax.swing.JScrollPane scrollPaneOptions;
    private javax.swing.JTextField txtFile;
    // End of variables declaration//GEN-END:variables
}
