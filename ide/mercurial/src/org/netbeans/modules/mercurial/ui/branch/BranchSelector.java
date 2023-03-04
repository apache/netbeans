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
package org.netbeans.modules.mercurial.ui.branch;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import java.io.File;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Padraig O'Briain
 */
public class BranchSelector implements ListSelectionListener, DocumentListener {

    private BranchSelectorPanel panel;
    private JButton okButton;
    private JButton cancelButton;
    private final File repository;
    private static final RequestProcessor   rp = new RequestProcessor("BranchPicker", 1, true);  // NOI18N
    private boolean bGettingRevisions = false;
    private InitialLoadingProgressSupport loadingSupport;
    private static final String MARK_ACTIVE_HEAD = "*"; //NOI18N
    
    private static final String INITIAL_REVISION = NbBundle.getMessage(BranchSelectorPanel.class, "MSG_Revision_Loading"); //NOI18N
    private static final String NO_BRANCH = NbBundle.getMessage(BranchSelectorPanel.class, "MSG_Revision_NoRevision"); //NOI18N
    private HgLogMessage.HgRevision parentRevision;
    private final Timer filterTimer;
    private HgBranch[] branches;
    private final Object LOCK = new Object();
    
    public BranchSelector (File repository) {
        this.repository = repository;
        panel = new BranchSelectorPanel();
        panel.branchList.setCellRenderer(new RevisionRenderer());
        
        filterTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                filterTimer.stop();
                applyFilter();
            }
        });
        panel.txtFilter.getDocument().addDocumentListener(this);
        panel.branchList.addListSelectionListener(this);
        panel.jPanel1.setVisible(false);
        cancelButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(BranchSelector.class, "CTL_BranchSelector_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSD_BranchSelector_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSN_BranchSelector_Action_Cancel")); // NOI18N
    } 
    
    public boolean showDialog (JButton okButton, String title, String branchListDescription) {
        this.okButton = okButton;
        org.openide.awt.Mnemonics.setLocalizedText(panel.jLabel1, branchListDescription);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, title, true, new Object[] {okButton, cancelButton}, 
                okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(this.getClass()), null);
        
        dialogDescriptor.setValid(false);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription(title);
        loadRevisions();
        dialog.setVisible(true);
        HgProgressSupport supp = loadingSupport;
        if (supp != null) {
            supp.cancel();
        }
        boolean ret = dialogDescriptor.getValue() == okButton;
        return ret;
    }
    
    public boolean showGeneralDialog () {
        JButton btn = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(btn, org.openide.util.NbBundle.getMessage(BranchSelector.class, "CTL_BranchSelectorPanel_Action_OK")); // NOI18N
        btn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSD_BranchSelectorPanel_Action_OK")); // NOI18N
        btn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSN_BranchSelectorPanel_Action_OK")); // NOI18N
        btn.setEnabled(false);
        return showDialog(btn, NbBundle.getMessage(BranchSelector.class, "CTL_BranchSelectorPanel.title", repository.getName()), //NOI18N
                NbBundle.getMessage(BranchSelector.class, "BranchSelectorPanel.infoLabel.text")); //NOI18N
    }

    public String getBranchName () {
        HgBranch selectedBranch = getSelectedBranch();
        return selectedBranch == null ? null : selectedBranch.getName();
    }

    private HgBranch getSelectedBranch () {
        if (panel.branchList.getSelectedValue() instanceof HgBranch) {
            return (HgBranch) panel.branchList.getSelectedValue();
        } else {
            return null;
        }
    }

    private String getRefreshLabel () {
        return NbBundle.getMessage(BranchSelectorPanel.class, "MSG_BranchSelector_Refreshing_Branches"); //NOI18N
    }

    private void loadRevisions () {
        loadingSupport = new InitialLoadingProgressSupport();
        loadingSupport.start(rp, repository, getRefreshLabel()); //NOI18N
    }

    public void setOptionsPanel (JPanel optionsPanel, Border parentPanelBorder) {
        if (optionsPanel == null) {
            panel.jPanel1.setVisible(false);
        } else {
            if (parentPanelBorder != null) {
                panel.jPanel1.setBorder(parentPanelBorder);
            }
            panel.jPanel1.removeAll();
            panel.jPanel1.add(optionsPanel, BorderLayout.NORTH);
            panel.jPanel1.setVisible(true);
        }
    }

    @Override
    public void valueChanged (ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting()) {
            HgBranch branch = null;
            if (panel.branchList.getSelectedValue() instanceof HgBranch) {
                branch = (HgBranch) panel.branchList.getSelectedValue();
            }
            if (branch == null) {
                okButton.setEnabled(false);
            } else {
                okButton.setEnabled(true);
                panel.changesetPanel1.setInfo(branch.getRevisionInfo());
            }
        }
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        if (!bGettingRevisions) {
            filterTimer.restart();
        }
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        if (!bGettingRevisions) {
            filterTimer.restart();
        }
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
    }
    
    private class InitialLoadingProgressSupport extends HgProgressSupport {
        @Override
        public void perform () {
            try {
                final DefaultListModel targetsModel = new DefaultListModel();
                targetsModel.addElement(INITIAL_REVISION);
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        panel.branchList.setModel(targetsModel);
                        if (!targetsModel.isEmpty()) {
                            panel.branchList.setSelectedIndex(0);
                        }
                    }
                });
                refreshRevisions(this);
            } finally {
                loadingSupport = null;
            }
        }

        private void refreshRevisions (HgProgressSupport supp) {
            bGettingRevisions = true;
            OutputLogger logger = Mercurial.getInstance().getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
            HgBranch[] fetchedBranches;
            try {
                fetchedBranches = HgCommand.getBranches(repository, logger);
            } catch (HgException ex) {
                Mercurial.LOG.log(Level.INFO, null, ex);
                fetchedBranches = null;
            }
            if( fetchedBranches == null) {
                fetchedBranches = new HgBranch[0];
            }

            if (!supp.isCanceled() && fetchedBranches.length > 0) {
                try {
                    parentRevision = HgCommand.getParent(repository, null, null);
                } catch (HgException ex) {
                    Mercurial.LOG.log(Level.FINE, null, ex);
                }
            }

            if (!supp.isCanceled()) {
                Arrays.sort(fetchedBranches, new Comparator<HgBranch>() {
                    @Override
                    public int compare (HgBranch b1, HgBranch b2) {
                        return b1.getName().compareTo(b2.getName());
                    }
                });
                synchronized (LOCK) {
                    branches = fetchedBranches;
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        applyFilter();
                        bGettingRevisions = false;
                    }
                });
            }
        }
    }

    private class RevisionRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof HgBranch) {
                HgBranch branch = (HgBranch) value;
                StringBuilder sb = new StringBuilder().append(branch.getName());
                HgLogMessage.HgRevision parent = parentRevision;
                if (parent != null && parent.getRevisionNumber().equals(branch.getRevisionInfo().getRevisionNumber())) {
                    sb.append(MARK_ACTIVE_HEAD);
                }
                sb.append(" (").append(branch.getRevisionInfo().getCSetShortID().substring(0, 7));
                if (!branch.isActive()) {
                    sb.append(" - ").append(NbBundle.getMessage(BranchSelector.class, "LBL_BranchSelector.branch.inactive")); //NOI18N
                }
                sb.append(")");
                value = sb.toString();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private void applyFilter () {
        HgBranch selectedBranch = getSelectedBranch();
        DefaultListModel targetsModel = new DefaultListModel();
        targetsModel.removeAllElements();
        HgBranch toSelect = null;
        String filter = panel.txtFilter.getText();
        synchronized (LOCK) {
            for (HgBranch branch : branches) {
                if (applies(filter, branch)) {
                    if (selectedBranch != null && branch.getRevisionInfo().getCSetShortID().equals(selectedBranch.getRevisionInfo().getCSetShortID())) {
                        toSelect = branch;
                    } else if (parentRevision != null && branch.getRevisionInfo().getCSetShortID().equals(parentRevision.getChangesetId())) {
                        toSelect = branch;
                    }
                    targetsModel.addElement(branch);
                }
            }
        }
        if (targetsModel.isEmpty()) {
            targetsModel.addElement(NO_BRANCH);
        }
        if (!Arrays.equals(targetsModel.toArray(), ((DefaultListModel) panel.branchList.getModel()).toArray())) {
            panel.branchList.setModel(targetsModel);
            if (toSelect != null) {
                panel.branchList.setSelectedValue(toSelect, true);
            } else if (targetsModel.size() > 0) {
                panel.branchList.setSelectedIndex(0);
            }
        }
    }

    private boolean applies (String filter, HgBranch branch) {
        boolean applies = filter.isEmpty();
        filter = filter.toLowerCase();
        String inactiveLabel = NbBundle.getMessage(BranchSelector.class, "LBL_BranchSelector.branch.inactive"); //NOI18N
        if (!applies) {
            HgLogMessage message = branch.getRevisionInfo();
            if (branch.getName().contains(filter)
                    || branch.isActive() && "active".startsWith(filter) //NOI18N
                    || !branch.isActive() && inactiveLabel.startsWith(filter) //NOI18N
                    || message.getRevisionNumber().contains(filter)
                    || message.getAuthor().toLowerCase().contains(filter)
                    || message.getCSetShortID().toLowerCase().contains(filter)
                    || message.getMessage().toLowerCase().contains(filter)
                    || message.getUsername().toLowerCase().contains(filter)
                    || applies(filter, message.getBranches())
                    || applies(filter, message.getTags())
                    || DateFormat.getDateTimeInstance().format(message.getDate()).toLowerCase().contains(filter)
                    ) {
                applies = true;
            }
        }
        return applies;        
    }
    
    private boolean applies (String format, String[] array) {
        for (String v : array) {
            if (v.toLowerCase().contains(format)) {
                return true;
            }
        }
        return false;
    }
}
