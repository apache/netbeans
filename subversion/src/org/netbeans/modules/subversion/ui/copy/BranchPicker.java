/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
        DefaultListModel model = new DefaultListModel();
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
                                DefaultListModel model = new DefaultListModel();
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
