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
package org.netbeans.modules.subversion.ui.copy;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.PanelProgressSupport;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 *
 * @author ondra
 */
class BranchPicker {

    private final BranchPickerPanel panel;
    private final RepositoryFile repositoryFile;
    private String selectedPath;
    private static final String ITEM_BRANCHES = NbBundle.getMessage(BranchPicker.class, "BranchPickerPanel.item.branches"); //NOI18N
    private static final String ITEM_TAGS = NbBundle.getMessage(BranchPicker.class, "BranchPickerPanel.item.tags"); //NOI18N
    private static final String ITEM_LOADING = NbBundle.getMessage(BranchPicker.class, "BranchPickerPanel.item.loading"); //NOI18N
    private static final String ITEM_SEP = "   "; //NOI18N
    private static final Set<Object> FORBIDDEN_SELECTION = new HashSet<Object>(Arrays.asList(new Object[] { null, ITEM_BRANCHES, ITEM_TAGS, ITEM_LOADING, ITEM_SEP }));
    private static final String PREFIX_BRANCHES = "branches"; //NOI18N
    private static final String PREFIX_TAGS = "tags"; //NOI18N
    private SvnProgressSupport loadingSupport;
    private final String branchesFolderPrefix;

    public BranchPicker (RepositoryFile repositoryFile, String branchesFolderPrefix) {
        this.repositoryFile = repositoryFile;
        this.branchesFolderPrefix = branchesFolderPrefix;
        this.panel = new BranchPickerPanel();
        this.panel.lstBranches.setCellRenderer(new Renderer(branchesFolderPrefix));
    }

    boolean openDialog () {
        final JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, NbBundle.getMessage(BranchPicker.class, "LBL_BranchPicker.okButton.text")); //NOI18N
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(BranchPicker.class, "LBL_BranchPicker.title"), //NOI18N
                true, new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(BranchPicker.class), null);
        okButton.setEnabled(false);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        ListSelectionListener list = new ListSelectionListener() {
            @Override
            public void valueChanged (ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String selected = (String) panel.lstBranches.getSelectedValue();
                    selectedPath = null;
                    if (!FORBIDDEN_SELECTION.contains(selected)) {
                        selectedPath = selected;
                    }
                    okButton.setEnabled(selectedPath != null);
                }
            }
        };
        panel.lstBranches.addListSelectionListener(list);
        initializeItems();
        dialog.setVisible(true);
        SvnProgressSupport supp = loadingSupport;
        if (supp != null) {
            supp.cancel();
        }
        panel.lstBranches.removeListSelectionListener(list);
        return dd.getValue() == okButton;
    }

    String getSelectedPath () {
        return selectedPath;
    }

    private void initializeItems () {
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement(ITEM_BRANCHES);
        model.addElement(ITEM_LOADING);
        model.addElement(ITEM_SEP);
        model.addElement(ITEM_TAGS);
        model.addElement(ITEM_LOADING);
        panel.lstBranches.setModel(model);
        SvnProgressSupport supp = new PanelProgressSupport(panel.pnlProgress) {
            @Override
            protected void perform () {
                try {
                    SvnClient client = Subversion.getInstance().getClient(repositoryFile.getRepositoryUrl());
                    final Map<String, ISVNDirEntry[]> entries = new HashMap<String, ISVNDirEntry[]>(2);
                    for (String pathName : new String[] { PREFIX_BRANCHES, PREFIX_TAGS }) {
                        if (isCanceled()) {
                            return;
                        }
                        try {
                            entries.put(pathName, client.getList(repositoryFile.getRepositoryUrl().appendPath(branchesFolderPrefix + pathName), SVNRevision.HEAD, false));
                        } catch (SVNClientException ex) {
                            if (SvnClientExceptionHandler.isWrongURLInRevision(ex.getMessage())) {
                                entries.put(pathName, new ISVNDirEntry[0]);
                            } else {
                                throw ex;
                            }
                        }
                    }
                    if (!isCanceled()) {
                        EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run () {
                                DefaultListModel<String> model = new DefaultListModel<>();
                                model.addElement(ITEM_BRANCHES);
                                for (ISVNDirEntry e : entries.get(PREFIX_BRANCHES)) {
                                    model.addElement(branchesFolderPrefix + PREFIX_BRANCHES + "/" + e.getPath()); //NOI18N
                                }
                                model.addElement(ITEM_SEP);
                                model.addElement(ITEM_TAGS);
                                for (ISVNDirEntry e : entries.get(PREFIX_TAGS)) {
                                    model.addElement(branchesFolderPrefix + PREFIX_TAGS + "/" + e.getPath()); //NOI18N
                                }
                                panel.lstBranches.setModel(model);
                            }
                        });
                    }
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, true, false);
                }
            }
        };
        // null as the repository root prevents logging in the output window
        supp.start(Subversion.getInstance().getRequestProcessor(repositoryFile.getRepositoryUrl()), null, 
                NbBundle.getMessage(BranchPicker.class, "BranchPickerPanel.loading.progress")); //NOI18N
        loadingSupport = supp;
    }
    
    private static class Renderer extends DefaultListCellRenderer {
        private final String branchesFolderPrefix;

        public Renderer (String branchesFolderPrefix) {
            this.branchesFolderPrefix = branchesFolderPrefix;
        }
        
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == ITEM_BRANCHES || value == ITEM_TAGS) {
                value = "<html><strong>" + value + "</strong></html>"; //NOI18N
            } else {
                String sValue = (String) value;
                for (String pref : new String[] { branchesFolderPrefix + PREFIX_BRANCHES + "/", branchesFolderPrefix + PREFIX_TAGS + "/" }) {
                    if (sValue.startsWith(pref)) {
                        value = "<html>" + pref + "<strong>" + sValue.substring(pref.length()) + "</strong></html>"; //NOI18N
                    }
                }
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
