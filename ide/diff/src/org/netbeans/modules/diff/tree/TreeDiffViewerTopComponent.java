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

package org.netbeans.modules.diff.tree;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.TreePath;
import org.netbeans.api.diff.DiffController;
import org.netbeans.modules.diff.DiffModuleConfig;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;

@TopComponent.Description(
    preferredID = "TreeDiffViewerTopComponent",
    persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(
        mode = "editor",
        openAtStartup = false
)
@Messages({
    "CTL_TreeDiffViewerTopComponent=Tree diff",
    "HINT_TreeDiffViewerTopComponent=This is a TreeDiffViewer window"
})
public final class TreeDiffViewerTopComponent extends TopComponent {
    private static final RequestProcessor IOHANDLER = new RequestProcessor("TreeDiffViewerTopComponentIOHandler");
    private static final TreeEntryRenderDataProvider FILENAME_DATA_PROVIDER = new TreeEntryRenderDataProvider(false);
    private static final TreeEntryRenderDataProvider PATH_DATA_PROVIDER = new TreeEntryRenderDataProvider(true);

    private final RecursiveDiffer recursiveDiffer;
    private final TreeEntryRowModel rowModel = new TreeEntryRowModel();
    private final TreeEntryTreeModel treeModel = new TreeEntryTreeModel(null);
    private final OutlineModel outlineModel = DefaultOutlineModel.createOutlineModel(treeModel, rowModel, false, "Files");
    private final JPopupMenu contextMenu;
    private List<ExclusionPattern> filterPatterns = new ArrayList<>();
    private List<TreeEntry> selectedTreeEntries = Collections.emptyList();

    public TreeDiffViewerTopComponent(RecursiveDiffer recursiveDiffer) {
        this.contextMenu = new JPopupMenu();
        JMenuItem deleteSource = new JMenuItem("Delete Source");
        JMenuItem deleteTarget = new JMenuItem("Delete Target");
        JMenuItem copySourceTarget = new JMenuItem("Copy Source to Target");
        JMenuItem copyTargetSource = new JMenuItem("Copy Target to Source");
        JMenuItem openSource = new JMenuItem("Open Source");
        JMenuItem openTarget = new JMenuItem("Open Target");
        openSource.addActionListener((ActionEvent ae) -> {
            for (TreeEntry selectedTreeEntry : selectedTreeEntries) {
                try {
                    DataObject.find(selectedTreeEntry.getFile1())
                            .getLookup()
                            .lookup(OpenCookie.class)
                            .open();
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        openTarget.addActionListener((ActionEvent ae) -> {
            for (TreeEntry selectedTreeEntry : selectedTreeEntries) {
                try {
                    DataObject.find(selectedTreeEntry.getFile2())
                            .getLookup()
                            .lookup(OpenCookie.class)
                            .open();
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        deleteSource.addActionListener((ActionEvent es) -> {
            List<TreeEntry> targetEntries = new ArrayList<>(selectedTreeEntries);
            selectedTreeEntries.clear();
            diffOutput.removeAll();
            IOHANDLER.execute(() -> {
                for (TreeEntry selectedTreeEntry : targetEntries) {
                    try {
                        selectedTreeEntry.getFile1().delete();
                        recursiveDiffer.removeTreeEntry(selectedTreeEntry);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        });
        deleteTarget.addActionListener((ActionEvent es) -> {
            List<TreeEntry> targetEntries = new ArrayList<>(selectedTreeEntries);
            selectedTreeEntries.clear();
            diffOutput.removeAll();
            IOHANDLER.execute(() -> {
                for (TreeEntry selectedTreeEntry : targetEntries) {
                    try {
                        selectedTreeEntry.getFile2().delete();
                        recursiveDiffer.removeTreeEntry(selectedTreeEntry);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        });
        copySourceTarget.addActionListener((ActionEvent es) -> {
            IOHANDLER.execute(() -> {
                for (TreeEntry selectedTreeEntry : selectedTreeEntries) {
                    try {
                        if (selectedTreeEntry.getFile2() != null) {
                            selectedTreeEntry.getFile2().delete();
                        }
                        FileObject targetFolder = selectedTreeEntry.getBasePath2()
                                .getFileObject(selectedTreeEntry.getRelativeParent());
                        if (targetFolder == null) {
                            targetFolder = FileUtil.createFolder(
                                    selectedTreeEntry.getBasePath2(),
                                    selectedTreeEntry.getRelativeParent()
                            );
                        }
                        selectedTreeEntry.getFile1().copy(
                                targetFolder,
                                selectedTreeEntry.getFile1().getName(),
                                selectedTreeEntry.getFile1().getExt()
                        );
                        recursiveDiffer.removeTreeEntry(selectedTreeEntry);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        });
        copyTargetSource.addActionListener((ActionEvent es) -> {
            IOHANDLER.execute(() -> {
                for (TreeEntry selectedTreeEntry : selectedTreeEntries) {
                    try {
                        if (selectedTreeEntry.getFile1() != null) {
                            selectedTreeEntry.getFile1().delete();
                        }
                        FileObject targetFolder = selectedTreeEntry.getBasePath1()
                                .getFileObject(selectedTreeEntry.getRelativeParent());
                        if (targetFolder == null) {
                            targetFolder = FileUtil.createFolder(
                                    selectedTreeEntry.getBasePath1(),
                                    selectedTreeEntry.getRelativeParent()
                            );
                        }
                        selectedTreeEntry.getFile2().copy(
                                targetFolder,
                                selectedTreeEntry.getFile2().getName(),
                                selectedTreeEntry.getFile2().getExt()
                        );
                        recursiveDiffer.removeTreeEntry(selectedTreeEntry);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        });
        this.contextMenu.add(openSource);
        this.contextMenu.add(openTarget);
        this.contextMenu.add(copySourceTarget);
        this.contextMenu.add(copyTargetSource);
        this.contextMenu.add(deleteSource);
        this.contextMenu.add(deleteTarget);
        this.contextMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
                deleteSource.setVisible(!selectedTreeEntries.isEmpty()
                        && selectedTreeEntries.stream().allMatch(ste -> (ste.getFile1() != null && ste.getFile2() == null)));
                deleteTarget.setVisible(!selectedTreeEntries.isEmpty()
                        && selectedTreeEntries.stream().allMatch(ste -> (ste.getFile1() == null && ste.getFile2() != null)));
                copySourceTarget.setVisible(!selectedTreeEntries.isEmpty()
                        && selectedTreeEntries.stream().allMatch(ste -> ste.getFile1() != null));
                copyTargetSource.setVisible(!selectedTreeEntries.isEmpty()
                        && selectedTreeEntries.stream().allMatch(ste -> ste.getFile2() != null));
                openSource.setVisible(copySourceTarget.isVisible());
                openTarget.setVisible(copyTargetSource.isVisible());
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent pme) {
            }
        });

        this.recursiveDiffer = recursiveDiffer;
        initComponents();
        filterPatterns = DiffModuleConfig.getDefault().getTreeExclusionList();
        setName(Bundle.CTL_TreeDiffViewerTopComponent());
        try {
            StringBuilder tooltipText = new StringBuilder();
            tooltipText.append("<html>Tree diff:<br>Source: ");
            tooltipText.append(XMLUtil.toElementContent(recursiveDiffer.getDir1().getPath()));
            tooltipText.append("<br>Target: ");
            tooltipText.append(XMLUtil.toElementContent(recursiveDiffer.getDir2().getPath()));
            tooltipText.append("</html>");
            setToolTipText(tooltipText.toString());
        } catch (CharConversionException ex) {
            Exceptions.printStackTrace(ex);
        }
        flattenResult.setSelected(recursiveDiffer.isFlatten());
        flattenResult.addActionListener((ActionEvent ae) -> {
            recursiveDiffer.setFlatten(flattenResult.isSelected());
        });
        fileTree.setComponentPopupMenu(contextMenu);
        fileTree.setModel(outlineModel);
        fileTree.setRootVisible(false);
        fileTree.setRenderDataProvider(FILENAME_DATA_PROVIDER);
        fileTree.getSelectionModel().addListSelectionListener((ListSelectionEvent lse) -> {
            if (!lse.getValueIsAdjusting()) {
                int selectedRowIdx = fileTree.getSelectedRow();
                if(selectedRowIdx >= 0) {
                    List<TreeEntry> selectedEntries = new ArrayList<>(fileTree.getSelectedRowCount());
                    int selectedModelRow = fileTree.convertRowIndexToModel(selectedRowIdx);
                    TreeEntry te = (TreeEntry) fileTree.getModel().getValueAt(selectedModelRow, 0);
                    selectedEntries.add(te);
                    for(int viewIdx: fileTree.getSelectedRows()) {
                        if(viewIdx == selectedRowIdx) {
                            continue;
                        }
                        selectedModelRow = fileTree.convertRowIndexToModel(selectedRowIdx);
                        te = (TreeEntry) fileTree.getModel().getValueAt(selectedModelRow, 0);
                        selectedEntries.add(te);
                    }
                    this.selectedTreeEntries = selectedEntries;
                    TreeDiffViewerTopComponent.this.setActivatedNodes(new Node[]{
                        TreeEntryNode.create(selectedEntries.get(0))});
                    updateDiff();
                } else {
                    selectedTreeEntries = Collections.emptyList();
                    TreeDiffViewerTopComponent.this.setActivatedNodes(null);
                    updateDiff();
                }
            }
        });
        fileTree.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        ETableColumnModel tcm = (ETableColumnModel) fileTree.getColumnModel();
        for (int i = tcm.getColumnCount() - 1; i > 0; i--) {
            tcm.removeColumn(tcm.getColumn(i));
        }
        tcm.setColumnSorted((ETableColumn) tcm.getColumn(0), true, 1);
        recursiveDiffer.addPropertyChangeListener("filteredResult", (pce) -> updateResultFromDiffer());
        updateResultFromDiffer();
        updateDiff();
        recursiveDiffer.addPropertyChangeListener("scanning", (pce) -> updateScanningPane());
        updateScanningPane();
        TreeDiffViewerTopComponent.this.setActivatedNodes(null);
    }

    private final PropertyChangeListener modifiedListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            TreeEntry te = (TreeEntry) pce.getSource();
            List<TreeEntry> path = new ArrayList<>();
            for(TreeEntry nextElement = te; nextElement != null; nextElement = nextElement.getParent()) {
                path.add(0, nextElement);
            }
            TreePath tp = new TreePath(path.toArray(TreeEntry[]::new));
            ((OutlineModel) fileTree.getModel()).valueForPathChanged(tp, te);
        }
    };

    @Override
    public boolean canClose() {
        return true;
    }

    private void updateScanningPane() {
        if(recursiveDiffer.isScanning()) {
            ((CardLayout) getLayout()).show(this, "scanningPanelWrapper");
        } else {
            ((CardLayout) getLayout()).show(this, "diffPanelWrapper");
        }
    }

    private void updateDiff() {
        diffOutput.removeAll();
        if(! selectedTreeEntries.isEmpty()) {
            try {
                DiffController diff = DiffController.createEnhanced(
                    FileStreamSource.create(
                            selectedTreeEntries.get(0).getFile1(),
                            selectedTreeEntries.get(0).getBasePath1()
                    ),
                    FileStreamSource.create(
                            selectedTreeEntries.get(0).getFile2(),
                            selectedTreeEntries.get(0).getBasePath2()
                    )
                );
                diffOutput.add(diff.getJComponent());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void updateResultFromDiffer() {
        TreeEntry te = this.recursiveDiffer.getFilteredResult();
        if (te != null) {
            TreeEntry oldRoot = (TreeEntry) treeModel.getRoot();
            if(oldRoot != null) {
                unregisterModifiedChangeLister(oldRoot);
            }
            treeModel.setRoot(te);
            fileTree.setRenderDataProvider(this.recursiveDiffer.isFlatten()
                    ? PATH_DATA_PROVIDER
                    : FILENAME_DATA_PROVIDER);
            basepathValue.setText(te.getFile1() != null ? te.getFile1().getPath() : "-");
            targetpathValue.setText(te.getFile2() != null ? te.getFile2().getPath() : "-");
            outputPane.setDividerLocation(0.25);
            List<TreePath> queue = new ArrayList<>();
            queue.add(new TreePath(te));
            while (!queue.isEmpty()) {
                TreePath tp = queue.remove(0);
                fileTree.expandPath(tp);
                if (tp.getPathCount() < 4) {
                    TreeEntry lte = (TreeEntry) tp.getLastPathComponent();
                    for (TreeEntry cte : lte.getChildren()) {
                        queue.add(tp.pathByAddingChild(cte));
                    }
                }
            }
            registerModifiedChangeLister(te);
        }
    }

    private void unregisterModifiedChangeLister(TreeEntry te) {
        List<TreeEntry> queue2 = new ArrayList<>();
        queue2.add(te);
        while (!queue2.isEmpty()) {
            TreeEntry wte = queue2.remove(0);
            wte.removePropertyChangeListener("modified", modifiedListener);
            for (TreeEntry cte : wte.getChildren()) {
                queue2.add(cte);
            }
        }
    }

    private void registerModifiedChangeLister(TreeEntry te) {
        List<TreeEntry> queue2 = new ArrayList<>();
        queue2.add(te);
        while(! queue2.isEmpty()) {
            TreeEntry wte = queue2.remove(0);
            wte.addPropertyChangeListener("modified", modifiedListener);
            for(TreeEntry cte: wte.getChildren()) {
                queue2.add(cte);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        diffPanelWrapper = new javax.swing.JPanel();
        infoPanel = new javax.swing.JPanel();
        basepathLabel = new javax.swing.JLabel();
        basepathValue = new javax.swing.JLabel();
        targetpathLabel = new javax.swing.JLabel();
        targetpathValue = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        flattenResult = new javax.swing.JCheckBox();
        swapPathsButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        applyFilterButton = new javax.swing.JCheckBox();
        editFilterButton = new javax.swing.JButton();
        outputPane = new javax.swing.JSplitPane();
        fileTreeScrollPane = new javax.swing.JScrollPane();
        fileTree = new org.netbeans.swing.outline.Outline();
        diffOutput = new javax.swing.JPanel();
        scanningPanelWrapper = new javax.swing.JPanel();
        scanningLabel = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();

        setLayout(new java.awt.CardLayout());

        diffPanelWrapper.setLayout(new java.awt.BorderLayout());

        infoPanel.setLayout(new java.awt.GridBagLayout());

        basepathLabel.setFont(basepathLabel.getFont().deriveFont(basepathLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(basepathLabel, org.openide.util.NbBundle.getMessage(TreeDiffViewerTopComponent.class, "TreeDiffViewerTopComponent.basepathLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        infoPanel.add(basepathLabel, gridBagConstraints);

        basepathValue.setFont(basepathValue.getFont().deriveFont(basepathValue.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(basepathValue, org.openide.util.NbBundle.getMessage(TreeDiffViewerTopComponent.class, "TreeDiffViewerTopComponent.basepathValue.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        infoPanel.add(basepathValue, gridBagConstraints);

        targetpathLabel.setFont(targetpathLabel.getFont().deriveFont(targetpathLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(targetpathLabel, org.openide.util.NbBundle.getMessage(TreeDiffViewerTopComponent.class, "TreeDiffViewerTopComponent.targetpathLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        infoPanel.add(targetpathLabel, gridBagConstraints);

        targetpathValue.setFont(targetpathValue.getFont().deriveFont(targetpathValue.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(targetpathValue, org.openide.util.NbBundle.getMessage(TreeDiffViewerTopComponent.class, "TreeDiffViewerTopComponent.targetpathValue.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        infoPanel.add(targetpathValue, gridBagConstraints);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(flattenResult, org.openide.util.NbBundle.getMessage(TreeDiffViewerTopComponent.class, "TreeDiffViewerTopComponent.flattenResult.text")); // NOI18N
        flattenResult.setFocusable(false);
        flattenResult.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(flattenResult);

        org.openide.awt.Mnemonics.setLocalizedText(swapPathsButton, org.openide.util.NbBundle.getMessage(TreeDiffViewerTopComponent.class, "TreeDiffViewerTopComponent.swapPathsButton.text")); // NOI18N
        swapPathsButton.setFocusable(false);
        swapPathsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        swapPathsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        swapPathsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                swapPathsButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(swapPathsButton);

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(TreeDiffViewerTopComponent.class, "TreeDiffViewerTopComponent.refreshButton.text")); // NOI18N
        refreshButton.setFocusable(false);
        refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(refreshButton);

        org.openide.awt.Mnemonics.setLocalizedText(applyFilterButton, org.openide.util.NbBundle.getMessage(TreeDiffViewerTopComponent.class, "TreeDiffViewerTopComponent.applyFilterButton.text")); // NOI18N
        applyFilterButton.setFocusable(false);
        applyFilterButton.setHideActionText(true);
        applyFilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyFilterButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(applyFilterButton);

        org.openide.awt.Mnemonics.setLocalizedText(editFilterButton, org.openide.util.NbBundle.getMessage(TreeDiffViewerTopComponent.class, "TreeDiffViewerTopComponent.editFilterButton.text")); // NOI18N
        editFilterButton.setFocusable(false);
        editFilterButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editFilterButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editFilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editFilterButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(editFilterButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        infoPanel.add(jToolBar1, gridBagConstraints);

        diffPanelWrapper.add(infoPanel, java.awt.BorderLayout.NORTH);

        outputPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        outputPane.setResizeWeight(0.5);

        fileTreeScrollPane.setViewportView(fileTree);

        outputPane.setLeftComponent(fileTreeScrollPane);

        diffOutput.setLayout(new java.awt.BorderLayout());
        outputPane.setRightComponent(diffOutput);

        diffPanelWrapper.add(outputPane, java.awt.BorderLayout.CENTER);

        add(diffPanelWrapper, "diffPanelWrapper");

        scanningPanelWrapper.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(scanningLabel, org.openide.util.NbBundle.getMessage(TreeDiffViewerTopComponent.class, "TreeDiffViewerTopComponent.scanningLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        scanningPanelWrapper.add(scanningLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(TreeDiffViewerTopComponent.class, "TreeDiffViewerTopComponent.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        scanningPanelWrapper.add(cancelButton, gridBagConstraints);

        add(scanningPanelWrapper, "scanningPanelWrapper");
    }// </editor-fold>//GEN-END:initComponents

    private void swapPathsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_swapPathsButtonActionPerformed
        FileObject fo1 = recursiveDiffer.getDir1();
        FileObject fo2 = recursiveDiffer.getDir2();
        recursiveDiffer.setDir1(fo2);
        recursiveDiffer.setDir2(fo1);
        recursiveDiffer.startScan();
    }//GEN-LAST:event_swapPathsButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        recursiveDiffer.cancelScan();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        recursiveDiffer.startScan();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void editFilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editFilterButtonActionPerformed
        FilterPanel fp = new FilterPanel();
        fp.setPatterns(this.filterPatterns);
        JDialog dialog = new JDialog(WindowManager.getDefault().getMainWindow(), "Filter settings");
        dialog.setContentPane(fp);
        fp.addActionListener((ae) -> {
            switch (ae.getActionCommand()) {
                case FilterPanel.ACTION_ACCEPT -> {
                    this.filterPatterns = fp.getPatterns();
                    if (fp.isUpdateGlobalList()) {
                        DiffModuleConfig.getDefault().setTreeExclusionList(this.filterPatterns);
                    }
                    updateDiffer();
                }
                case FilterPanel.ACTION_DISCARD -> {
                }
            }
            dialog.setVisible(false);
        });
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }//GEN-LAST:event_editFilterButtonActionPerformed

    private void applyFilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyFilterButtonActionPerformed
        updateDiffer();
    }//GEN-LAST:event_applyFilterButtonActionPerformed

    private void updateDiffer() {
        List<Pattern> existingList = recursiveDiffer.getExclusionPatterns();
        List<Pattern> patterns;
        if (applyFilterButton.isSelected() && filterPatterns != null) {
            patterns = filterPatterns.stream().map(ep -> ep.asPattern()).toList();
        } else {
            patterns = Collections.emptyList();
        }
        if (!Objects.equals(existingList, patterns)) {
            recursiveDiffer.setExclusionPatterns(patterns);
            recursiveDiffer.startScan();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox applyFilterButton;
    private javax.swing.JLabel basepathLabel;
    private javax.swing.JLabel basepathValue;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel diffOutput;
    private javax.swing.JPanel diffPanelWrapper;
    private javax.swing.JButton editFilterButton;
    private org.netbeans.swing.outline.Outline fileTree;
    private javax.swing.JScrollPane fileTreeScrollPane;
    private javax.swing.JCheckBox flattenResult;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JSplitPane outputPane;
    private javax.swing.JButton refreshButton;
    private javax.swing.JLabel scanningLabel;
    private javax.swing.JPanel scanningPanelWrapper;
    private javax.swing.JButton swapPathsButton;
    private javax.swing.JLabel targetpathLabel;
    private javax.swing.JLabel targetpathValue;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        TreeEntry oldRoot = (TreeEntry) treeModel.getRoot();
        if (oldRoot != null) {
            unregisterModifiedChangeLister(oldRoot);
        }
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

}
