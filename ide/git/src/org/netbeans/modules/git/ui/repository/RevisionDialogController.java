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

package org.netbeans.modules.git.ui.repository;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class RevisionDialogController implements ActionListener, DocumentListener, PropertyChangeListener, ListSelectionListener {
    private final RevisionDialog panel;
    private final File repository;
    private final RevisionInfoPanelController infoPanelController;
    private final PropertyChangeSupport support;
    public static final String PROP_VALID = "RevisionDialogController.valid"; //NOI18N
    public static final String PROP_REVISION_ACCEPTED = "RevisionDialogController.revisionAccepted"; //NOI18N
    private boolean valid;
    private final Timer t;
    private boolean internally;
    private final File[] roots;
    private String revisionString;
    private String mergingInto;
    private Revision revisionInfo;
    private DefaultListModel<Object> branchModel;

    public RevisionDialogController (File repository, File[] roots, String initialRevision) {
        this(repository, roots);
        panel.revisionField.setText(initialRevision);
        panel.revisionField.setCaretPosition(panel.revisionField.getText().length());
        panel.revisionField.moveCaretPosition(0);
        hideFields(new JComponent[] { panel.lblBranch, panel.branchesPanel });
    }

    /**
     * 
     * @param repository
     * @param roots
     * @param branches if this is an empty map, branches will be loaded in background
     * @param defaultBranchName branch you want to select by default or <code>null</code> to preselect the current branch
     */
    public RevisionDialogController (File repository, File[] roots, Map<String, GitBranch> branches, String defaultBranchName) {
        this(repository, roots);
        hideFields(new JComponent[] { panel.lblRevision, panel.revisionField, panel.btnSelectRevision });
        setModel(branches, defaultBranchName);
    }

    private RevisionDialogController (File repository, File[] roots) {
        infoPanelController = new RevisionInfoPanelController(repository);
        this.panel = new RevisionDialog(infoPanelController.getPanel());
        this.repository = repository;
        this.roots = roots;
        this.support = new PropertyChangeSupport(this);
        this.t = new Timer(500, this);
        t.stop();
        infoPanelController.loadInfo(revisionString = panel.revisionField.getText());
        attachListeners();
    }
    
    public RevisionDialog getPanel () {
        return panel;
    }

    public void setEnabled (boolean enabled) {
        panel.btnSelectRevision.setEnabled(enabled);
        panel.revisionField.setEnabled(enabled);
    }

    public Revision getRevision () {
        return revisionInfo == null ? new Revision(revisionString, revisionString) : revisionInfo;
    }
    
    public void addPropertyChangeListener (PropertyChangeListener list) {
        support.addPropertyChangeListener(list);
    }
    
    public void removePropertyChangeListener (PropertyChangeListener list) {
        support.removePropertyChangeListener(list);
    }

    public void setMergingInto (String revision) {
        mergingInto = revision;
        infoPanelController.displayMergedStatus(revision);
    }

    private void attachListeners () {
        panel.btnSelectRevision.addActionListener(this);
        panel.revisionField.getDocument().addDocumentListener(this);
        panel.lstBranches.addListSelectionListener(this);
        panel.lstBranches.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent e) {
                if (e.getSource() == panel.lstBranches) {
                    if (e.getClickCount() == 2 && revisionInfo != null) {
                        e.consume();
                        support.firePropertyChange(PROP_REVISION_ACCEPTED, null, revisionInfo);
                    }
                }
            }
        });
        infoPanelController.addPropertyChangeListener(this);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.btnSelectRevision) {
            openRevisionPicker();
        } else if (e.getSource() == t) {
            t.stop();
            infoPanelController.loadInfo(revisionString);
        }
    }

    @Override
    public void valueChanged (ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && e.getSource() == panel.lstBranches) {
            selectedBranchChanged();
        }
    }

    private void openRevisionPicker () {
        RevisionPicker picker = new RevisionPicker(repository, roots);
        picker.displayMergedStatus(mergingInto);
        if (picker.open()) {
            Revision selectedRevision = picker.getRevision();
            internally = true;
            try {
                panel.revisionField.setText(selectedRevision.getRevision());
                panel.revisionField.setCaretPosition(0);
            } finally {
                internally = false;
            }
            if (!selectedRevision.getRevision().equals(revisionString)) {
                revisionString = selectedRevision.getRevision();
                updateRevision();
            }
        }
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        revisionChanged();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        revisionChanged();
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
    }

    private void setValid (boolean flag, Revision revision) {
        boolean oldValue = valid;
        valid = flag;
        revisionInfo = revision;
        if (valid != oldValue) {
            support.firePropertyChange(PROP_VALID, oldValue, valid);
        }
    }

    private void revisionChanged () {
        if (!internally) {
            revisionString = panel.revisionField.getText();
            updateRevision();
        }
    }
    
    private void updateRevision () {
        setValid(false, null);
        t.restart();
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName() == RevisionInfoPanelController.PROP_VALID) {
            setValid(Boolean.TRUE.equals(evt.getNewValue()), infoPanelController.getInfo());
        }
    }

    private void hideFields (JComponent[] fields) {
        for (JComponent field : fields) {
            field.setVisible(false);
        }
    }

    @NbBundle.Messages({
        "MSG_RevisionDialog.noBranches=No Branches"
    })
    private void setModel (Map<String, GitBranch> branches, String toSelectBranchName) {
        if (branches.isEmpty()) {
            loadBranches(toSelectBranchName);
            return;
        }
        final List<Revision> branchList = new ArrayList<Revision>(branches.size());
        List<Revision> remoteBranchList = new ArrayList<Revision>(branches.size());
        Revision activeBranch = null;
        for (Map.Entry<String, GitBranch> e : branches.entrySet()) {
            GitBranch branch = e.getValue();
            Revision rev = null;
            if (branch.isRemote()) {
                rev = new Revision.BranchReference(branch);
                remoteBranchList.add(rev);
            } else if (branch.getName() != GitBranch.NO_BRANCH) {
                rev = new Revision.BranchReference(branch);
                branchList.add(rev);
            }
            if (rev != null && (toSelectBranchName != null && toSelectBranchName.equals(branch.getName())
                    || toSelectBranchName == null && branch.isActive())) {
                activeBranch = rev;
            }
        }
        Comparator<Revision> comp = new Comparator<Revision>() {
            @Override
            public int compare (Revision b1, Revision b2) {
                return b1.getRevision().compareTo(b2.getRevision());
            }
        };
        branchList.sort(comp);
        remoteBranchList.sort(comp);
        branchList.addAll(remoteBranchList);
        final Revision toSelect = activeBranch;
        branchModel = new DefaultListModel<Object>();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                if (branchList.isEmpty()) {
                    branchModel.addElement(Bundle.MSG_RevisionDialog_noBranches());
                } else {
                    for (Revision rev : branchList) {
                        branchModel.addElement(rev);
                    }
                }
                panel.lstBranches.setModel(branchModel);
                panel.lstBranches.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        return super.getListCellRendererComponent(list, value instanceof Revision ? ((Revision) value).getRevision() : value, index, isSelected, cellHasFocus);
                    }
                });
                if (toSelect != null) {
                    panel.lstBranches.setSelectedValue(toSelect, true);
                }
                selectedBranchChanged();
                if (!branchList.isEmpty()) {
                    GitUtils.<Revision>attachQuickSearch(branchList, panel.branchesPanel, panel.lstBranches, branchModel, new GitUtils.SearchCallback<Revision>() {

                        @Override
                        public boolean contains (Revision rev, String needle) {
                            return rev.getRevision().toLowerCase().contains(needle.toLowerCase());
                        }
                    });
                }
            }
        });
    }

    private void selectedBranchChanged () {
        Object activeBranch = panel.lstBranches.getSelectedValue();
        if (activeBranch instanceof Revision) {
            revisionString = ((Revision) activeBranch).getRevision();
            setValid(valid, (Revision) activeBranch);
            t.restart();
        } else {
            revisionString = activeBranch instanceof Revision ? ((Revision) activeBranch).getRevision() : Bundle.MSG_RevisionDialog_noBranches();
            updateRevision();
        }
    }

    @NbBundle.Messages({
        "RevisionDialogController.loadingBranches=Loading Branches..."
    })
    private void loadBranches (final String defaultBranch) {
        DefaultListModel model = new DefaultListModel();
        model.addElement(Bundle.RevisionDialogController_loadingBranches());
        panel.lstBranches.setModel(model);
        panel.lstBranches.setEnabled(false);
        new GitProgressSupport.NoOutputLogging() {
            
            @Override
            protected void perform () {
                final Map<String, GitBranch> branches = RepositoryInfo.getInstance(repository).getBranches();
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run () {
                        panel.lstBranches.setEnabled(true);
                        setModel(branches.isEmpty()
                                ? Collections.singletonMap(GitBranch.NO_BRANCH, GitBranch.NO_BRANCH_INSTANCE)
                                : branches, defaultBranch);
                    }
                });
            }
        }.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.RevisionDialogController_loadingBranches());
    }
}
