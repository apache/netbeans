/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.mercurial.ui.repository;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.WorkingCopyInfo;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.ui.log.RepositoryRevision;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.util.HgCommand;

/**
 *
 * @author  Padraig O'Briain
 */
public abstract class ChangesetPickerPanel extends javax.swing.JPanel {

    private File                            repository;
    private File[]                          roots; // MAY be null
    private static final RequestProcessor   rp = new RequestProcessor("ChangesetPicker", 1, true);  // NOI18N
    private int fetchRevisionLimit = Mercurial.HG_NUMBER_TO_FETCH_DEFAULT;
    private boolean bGettingRevisions = false;
    public static final String HG_TIP = "tip"; // NOI18N
    private final MessageInfoFetcher defaultMessageInfoFetcher;
    private MessageInfoFetcher messageInfofetcher;
    private HgProgressSupport hgProgressSupport;
    private InitialLoadingProgressSupport initialProgressSupport;
    private static final String MARK_ACTIVE_HEAD = "*"; //NOI18N
    public static final String PROP_VALID = "prop.valid"; //NOI18N
    
    protected static final HgLogMessage TIP = new HgLogMessage(null, Collections.<String>emptyList(), HG_TIP, 
            null, null, null, Long.toString(new Date().getTime()), HG_TIP, 
            null, null, null, null, null, "", ""); //NOI18N
    protected static final HgLogMessage NO_REVISION = new HgLogMessage(null, Collections.<String>emptyList(), null, 
            null, null, null, Long.toString(new Date().getTime()), NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Revision_Default"), //NOI18N
            null, null, null, null, null, "", ""); //NOI18N
    private HgLogMessage parentRevision;
    private boolean validSelection;
    private final Timer filterTimer;
    private HgLogMessage[] messages;
    private final Object LOCK = new Object();

    /** Creates new form ReverModificationsPanel */
    public ChangesetPickerPanel(File repo, File[] files) {
        repository = repo;
        roots = files;
        initComponents();
        jPanel1.setVisible(false);
        revisionsComboBox.setCellRenderer(new RevisionRenderer());
        defaultMessageInfoFetcher = new MessageInfoFetcher();
        
        filterTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                filterTimer.stop();
                applyFilter();
            }
        });
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
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
        });
    }

    public File[] getRootFiles () {
        return roots;
    }

    /**
     * Returns a selected revision or null if no revision is selected.
     * @return
     */
    public HgLogMessage getSelectedRevision() {
        HgLogMessage rev = (HgLogMessage) revisionsComboBox.getSelectedValue();
        return rev;
    }

    public String getSelectedRevisionCSetId () {
        HgLogMessage selectedRevision = getSelectedRevision();
        return selectedRevision == NO_REVISION // has a label instead of a cset id
                ? null 
                : selectedRevision.getCSetShortID();
    }

    protected String getRefreshLabel () {
        return NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Refreshing_Revisions"); //NOI18N
    }

    protected HgLogMessage getDisplayedRevision() {
        return null;
    }

    protected void loadRevisions () {
        initialProgressSupport = new InitialLoadingProgressSupport();
        initialProgressSupport.start(rp, repository, getRefreshLabel()); //NOI18N
    }

    protected void setOptionsPanel (JPanel optionsPanel, Border parentPanelBorder) {
        if (optionsPanel == null) {
            jPanel1.setVisible(false);
        } else {
            if (parentPanelBorder != null) {
                jPanel1.setBorder(parentPanelBorder);
            }
            jPanel1.removeAll();
            jPanel1.add(optionsPanel, BorderLayout.NORTH);
            jPanel1.setVisible(true);
        }
    }

    protected String getRevisionLabel (RepositoryRevision rev) {
        return new StringBuilder(rev.getLog().getRevisionNumber()).append(" (").append(rev.getLog().getCSetShortID()).append(")").toString(); //NOI18N
    }

    protected void setInitMessageInfoFetcher (MessageInfoFetcher fetcher) {
        this.messageInfofetcher = fetcher;
    }

    @Override
    public void removeNotify() {
        HgProgressSupport supp = initialProgressSupport;
        if (supp != null) {
            supp.cancel();
        }
        supp = hgProgressSupport;
        if (supp != null) {
            supp.cancel();
        }
        super.removeNotify();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        changesetPanel1 = new org.netbeans.modules.mercurial.ui.repository.ChangesetPanel();
        jPanel2 = new javax.swing.JPanel();
        revisionsLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        revisionsComboBox = new javax.swing.JList();
        panelSearchOptions = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        btnFetchAll = new org.netbeans.modules.versioning.history.LinkButton();
        btnFetch50 = new org.netbeans.modules.versioning.history.LinkButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btnFetch20 = new org.netbeans.modules.versioning.history.LinkButton();
        jLabel6 = new javax.swing.JLabel();
        txtFilter = new javax.swing.JTextField();

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        jSplitPane1.setBorder(null);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.75);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.options"))); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(changesetPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(changesetPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane1.setBottomComponent(jPanel3);

        org.openide.awt.Mnemonics.setLocalizedText(revisionsLabel, org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.revisionsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, null);
        jLabel2.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, null);

        revisionsComboBox.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        revisionsComboBox.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                revisionsComboBoxValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(revisionsComboBox);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnFetchAll, org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.btnFetchAll.text")); // NOI18N
        btnFetchAll.setToolTipText(org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.btnFetchAll.toolTipText")); // NOI18N
        btnFetchAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFetchAllActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnFetch50, org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.btnFetch50.text")); // NOI18N
        btnFetch50.setToolTipText(org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.btnFetch50.toolTipText")); // NOI18N
        btnFetch50.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFetch50ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "|");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnFetch20, org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.btnFetch20.text")); // NOI18N
        btnFetch20.setToolTipText(org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.btnFetch20.toolTipText")); // NOI18N
        btnFetch20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFetch20ActionPerformed(evt);
            }
        });

        jLabel6.setLabelFor(txtFilter);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.jLabel6.text")); // NOI18N
        jLabel6.setToolTipText(org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.jLabel6.toolTipText")); // NOI18N

        txtFilter.setText(org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.txtFilter.text")); // NOI18N

        javax.swing.GroupLayout panelSearchOptionsLayout = new javax.swing.GroupLayout(panelSearchOptions);
        panelSearchOptions.setLayout(panelSearchOptionsLayout);
        panelSearchOptionsLayout.setHorizontalGroup(
            panelSearchOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSearchOptionsLayout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(5, 5, 5)
                .addComponent(btnFetch20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel4)
                .addGap(5, 5, 5)
                .addComponent(btnFetch50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel5)
                .addGap(5, 5, 5)
                .addComponent(btnFetchAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
        );
        panelSearchOptionsLayout.setVerticalGroup(
            panelSearchOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSearchOptionsLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(panelSearchOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(btnFetch20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(btnFetch50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(btnFetchAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                        .addGap(24, 24, 24))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(panelSearchOptions, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(revisionsLabel)
                            .addComponent(jLabel2))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(revisionsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addComponent(panelSearchOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane1.setTopComponent(jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

private void revisionsComboBoxValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_revisionsComboBoxValueChanged
    if (!evt.getValueIsAdjusting()) {
        HgLogMessage rev = (HgLogMessage) revisionsComboBox.getSelectedValue();
        boolean oldValid = validSelection;
        validSelection = acceptSelection(rev);
        if (oldValid != validSelection) {
            firePropertyChange(PROP_VALID, oldValid, validSelection);
        }

        if (validSelection) {
            changesetPanel1.setInfo(rev);
        }
    }
}//GEN-LAST:event_revisionsComboBoxValueChanged

private void btnFetch20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFetch20ActionPerformed
    getMore(Mercurial.HG_FETCH_20_REVISIONS);
}//GEN-LAST:event_btnFetch20ActionPerformed

private void btnFetch50ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFetch50ActionPerformed
    getMore(Mercurial.HG_FETCH_50_REVISIONS);
}//GEN-LAST:event_btnFetch50ActionPerformed

private void btnFetchAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFetchAllActionPerformed
    getMore(Mercurial.HG_FETCH_ALL_REVISIONS);
}//GEN-LAST:event_btnFetchAllActionPerformed

    private boolean getMore (int limit) {
        if (bGettingRevisions) return false;

        if (limit == Mercurial.HG_FETCH_ALL_REVISIONS) {
            btnFetchAll.setEnabled(false);
            btnFetch50.setEnabled(false);
            btnFetch20.setEnabled(false);
        }
        messageInfofetcher = defaultMessageInfoFetcher;
        fetchRevisionLimit = limit;
        if (limit > 0 && messages != null) {
            fetchRevisionLimit += messages.length;
        }
        filterTimer.stop();
        hgProgressSupport = new HgProgressSupport() {
            @Override
            public void perform() {
                refreshRevisions(this);
                hgProgressSupport = null;
            }
        };
        hgProgressSupport.start(rp, repository, org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Fetching_Revisions")); //NOI18N
        return true;
    }

    private class InitialLoadingProgressSupport extends HgProgressSupport {
        @Override
        public void perform () {
            try {
                final DefaultListModel targetsModel = new DefaultListModel();
                final HgLogMessage displayedRevision = getDisplayedRevision();
                if (displayedRevision == null) {
                    if (acceptSelection(NO_REVISION)) {
                        targetsModel.addElement(NO_REVISION);
                    }
                    if (acceptSelection(TIP)) {
                        targetsModel.addElement(TIP);
                    }
                } else {
                    targetsModel.addElement(displayedRevision);
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        revisionsComboBox.setModel(targetsModel);
                        if (!targetsModel.isEmpty()) {
                            revisionsComboBox.setSelectedIndex(0);
                        }
                    }
                });
                if (displayedRevision == null) {
                    refreshRevisions(this);
                } else {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            revisionsComboBox.setSelectedValue(displayedRevision, true);
                            revisionsComboBox.setEnabled(false);
                            panelSearchOptions.setVisible(false);
                        }
                    });
                }
            } finally {
                initialProgressSupport = null;
            }
        }
    }

    private void refreshRevisions (HgProgressSupport supp) {
        bGettingRevisions = true;

        OutputLogger logger = Mercurial.getInstance().getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
        MessageInfoFetcher fetcher = getMessageInfoFetcher();
        HgLogMessage[] fetchedMessages = fetcher.getMessageInfo(repository, roots == null ? null : new HashSet<File>(Arrays.asList(roots)), fetchRevisionLimit, logger);
        if (!supp.isCanceled() && fetchedMessages.length > 0) {
            WorkingCopyInfo wcInfo = WorkingCopyInfo.getInstance(repository);
            wcInfo.refresh();
            HgLogMessage[] parents = wcInfo.getWorkingCopyParents();
            if (parents.length > 0) {
                parentRevision = parents[0];
            }
        }

        if (!supp.isCanceled()) {
            if( fetchedMessages == null || fetchedMessages.length == 0){
                fetchedMessages = new HgLogMessage[] { NO_REVISION };
            } else if (parentRevision != null && acceptSelection(parentRevision)) {
                // parent revision should always be loaded and displaed
                boolean containsParent = false;
                for (HgLogMessage msg : fetchedMessages) {
                    if (msg.getCSetShortID().equals(parentRevision.getCSetShortID())) {
                        containsParent = true;
                        break;
                    }
                }
                if (!containsParent) {
                    fetchedMessages = Arrays.copyOf(fetchedMessages, fetchedMessages.length + 1);
                    fetchedMessages[fetchedMessages.length - 1] = parentRevision;
                }
            }
            synchronized (LOCK) {
                messages = fetchedMessages;
            }
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run () {
                    if (fetchRevisionLimit > messages.length && messageInfofetcher == defaultMessageInfoFetcher) {
                        btnFetch20.setEnabled(false);
                        btnFetch50.setEnabled(false);
                        btnFetchAll.setEnabled(false);
                    }
                    applyFilter();
                    bGettingRevisions = false;
                }
            });
        }
    }

    private MessageInfoFetcher getMessageInfoFetcher() {
        MessageInfoFetcher f = messageInfofetcher;
        if (f == null) {
            f = defaultMessageInfoFetcher;
        }
        return f;
    }

    protected boolean acceptSelection (HgLogMessage rev) {
        return rev != null;
    }
    
    protected final HgRevision getParentRevision () {
        return parentRevision == null ? null : parentRevision.getHgRevision();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.versioning.history.LinkButton btnFetch20;
    private org.netbeans.modules.versioning.history.LinkButton btnFetch50;
    private org.netbeans.modules.versioning.history.LinkButton btnFetchAll;
    private org.netbeans.modules.mercurial.ui.repository.ChangesetPanel changesetPanel1;
    protected final javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    protected final javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel panelSearchOptions;
    private javax.swing.JList revisionsComboBox;
    protected javax.swing.JLabel revisionsLabel;
    private javax.swing.JTextField txtFilter;
    // End of variables declaration//GEN-END:variables

    protected static class MessageInfoFetcher {
        protected HgLogMessage[] getMessageInfo(File repository, Set<File> setRoots, int fetchRevisionLimit, OutputLogger logger) {
            return HgCommand.getLogMessagesNoFileInfo(repository, setRoots, fetchRevisionLimit, logger);
        }
    }

    private class RevisionRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof HgLogMessage) {
                HgLogMessage message = (HgLogMessage) value;
                if (message == TIP || message == NO_REVISION) {
                    value = message.getCSetShortID();
                } else {
                    StringBuilder sb = new StringBuilder().append(message.getRevisionNumber());
                    HgLogMessage parent = parentRevision;
                    if (parent != null && parent.getRevisionNumber().equals(message.getRevisionNumber())) {
                        sb.append(MARK_ACTIVE_HEAD);
                    }
                    StringBuilder labels = new StringBuilder();
                    for (String branch : message.getBranches()) {
                        labels.append(branch).append(' ');
                    }
                    for (String tag : message.getTags()) {
                        labels.append(tag).append(' ');
                        break; // just one tag
                    }
                    sb.append(" (").append(labels).append(labels.length() == 0 ? "" : "- ").append(message.getCSetShortID().substring(0, 7)).append(")"); //NOI18N
                    if (!message.getShortMessage().isEmpty()) {
                        sb.append(" - ").append(message.getShortMessage()); //NOI18N
                    }
                    value = sb.toString();
                }
            }
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (isSelected && !list.isEnabled() && c instanceof JLabel) {
                // hack for unreadable combination: disabled, selectedForegroud, selectedBackground on dark LAF
                ((JLabel) c).setEnabled(true);
            }
            return c;
        }
    }

    private void applyFilter () {
        HgLogMessage selectedRevision = getSelectedRevision();
        DefaultListModel targetsModel = new DefaultListModel();
        targetsModel.removeAllElements();
        HgLogMessage toSelectRevision = null;
        String filter = txtFilter.getText();
        synchronized (LOCK) {
            for (HgLogMessage message : messages) {
                if (applies(filter, message)) {
                    if (selectedRevision != null && message.getCSetShortID().equals(selectedRevision.getCSetShortID())) {
                        toSelectRevision = message;
                    } else if (parentRevision != null && message.getCSetShortID().equals(parentRevision.getCSetShortID())) {
                        toSelectRevision = message;
                    }
                    targetsModel.addElement(message);
                }
            }
        }
        if (!Arrays.equals(targetsModel.toArray(), ((DefaultListModel) revisionsComboBox.getModel()).toArray())) {
            revisionsComboBox.setModel(targetsModel);
            if (toSelectRevision != null) {
                revisionsComboBox.setSelectedValue(toSelectRevision, true);
            } else if (targetsModel.size() > 0) {
                revisionsComboBox.setSelectedIndex(0);
            }
        }
    }

    private boolean applies (String filter, HgLogMessage message) {
        boolean applies = filter.isEmpty();
        filter = filter.toLowerCase();
        if (!applies) {
            if (message.getRevisionNumber().contains(filter)
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
