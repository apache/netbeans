/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mercurial.ui.tag;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.update.UpdateAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ondra
 */
class TagManager implements ListSelectionListener, DocumentListener, ActionListener {
    private final File repository;
    private final TagManagerPanel panel;
    private final Timer filterTimer;
    private boolean bGettingRevisions = false;
    private HgProgressSupport backgroundSupport;
    private static final String MARK_ACTIVE_HEAD = "*"; //NOI18N
    
    private static final String INITIAL_MESSAGE = NbBundle.getMessage(TagManager.class, "MSG_Tag_Loading"); //NOI18N
    private HgLogMessage.HgRevision parentRevision;
    private HgTag[] tags;
    private final Object LOCK = new Object();
    private Dialog dialog;
    private RemoveTagPanel removePanel;

    TagManager (File repository) {
        this.repository = repository;
        this.panel = new TagManagerPanel();
        panel.tagList.setCellRenderer(new TagRenderer());
        filterTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                filterTimer.stop();
                applyFilter();
            }
        });
        attachListeners();
    }

    void showDialog () {
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(CreateTag.class, "LBL_TagManagerPanel.title", repository.getName()), //NOI18N
                true, new Object[] { DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION },
                DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx("org.netbeans.modules.mercurial.ui.tag.TagManagerPanel"), null); //NOI18N
        dialog = DialogDisplayer.getDefault().createDialog(dd);
        loadRevisions();
        dialog.setVisible(true);
        HgProgressSupport supp = backgroundSupport;
        if (supp != null) {
            supp.cancel();
        }
    }
    
    private void loadRevisions () {
        backgroundSupport = new InitialLoadingProgressSupport();
        backgroundSupport.start(Mercurial.getInstance().getRequestProcessor(repository), repository, INITIAL_MESSAGE);
    }

    private HgTag getSelectedTag () {
        if (panel.tagList.getSelectedValue() instanceof HgTag) {
            return (HgTag) panel.tagList.getSelectedValue();
        } else {
            return null;
        }
    }
    
    @Override
    public void valueChanged (ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting()) {
            HgTag tag = null;
            if (panel.tagList.getSelectedValue() instanceof HgTag) {
                tag = (HgTag) panel.tagList.getSelectedValue();
            }
            if (tag == null) {
                panel.btnRemove.setEnabled(false);
                panel.btnUpdate.setEnabled(false);
            } else {
                panel.btnRemove.setEnabled(tag.canRemove());
                panel.btnUpdate.setEnabled(true);
                panel.changesetPanel1.setInfo(tag.getRevisionInfo());
                panel.txtTagName.setText(tag.getName());
                panel.txtTagName.setCaretPosition(0);
                panel.txtTaggedRevision.setText(annotateRevision(tag.getRevisionInfo()));
                panel.txtTaggedRevision.setCaretPosition(0);
                panel.lblLocal.setVisible(tag.isLocal());
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

    private String annotateRevision (HgLogMessage revisionInfo) {
        StringBuilder sb = new StringBuilder().append(revisionInfo.getRevisionNumber());
        HgLogMessage.HgRevision parent = parentRevision;
        if (parent != null && parent.getRevisionNumber().equals(revisionInfo.getRevisionNumber())) {
            sb.append(MARK_ACTIVE_HEAD);
        }
        StringBuilder labels = new StringBuilder();
        for (String branch : revisionInfo.getBranches()) {
            labels.append(branch).append(' ');
        }
        for (String tag : revisionInfo.getTags()) {
            labels.append(tag).append(' ');
            break; // just one tag
        }
        sb.append(" (").append(labels).append(labels.length() == 0 ? "" : "- ").append(revisionInfo.getCSetShortID().substring(0, 7)).append(")"); //NOI18N
        return sb.toString();
    }

    private void attachListeners () {
        panel.txtFilter.getDocument().addDocumentListener(this);
        panel.tagList.addListSelectionListener(this);
        panel.btnRemove.addActionListener(this);
        panel.btnUpdate.addActionListener(this);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.btnRemove) {
            removeTag(getSelectedTag());
        } else if (e.getSource() == panel.btnUpdate) {
            dialog.setVisible(false);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run () {
                    SystemAction.get(UpdateAction.class).update(repository, getSelectedTag().getRevisionInfo());
                }
            });
        }
    }

    private void removeTag (final HgTag tagToRemove) {
        JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, NbBundle.getMessage(CreateTag.class, "CTL_TagManagerPanel.removeTag.okButton.text", tagToRemove.getName())); //NOI18N
        String title = NbBundle.getMessage(CreateTag.class, "LBL_TagManagerPanel.removeTag.title", tagToRemove.getName()); //NOI18N
        boolean remove = false;
        final String removeMessage;
        if (tagToRemove.isLocal()) {
            NotifyDescriptor nd = new NotifyDescriptor(NbBundle.getMessage(CreateTag.class, "CTL_TagManagerPanel.removeTag.confirmation.message", tagToRemove.getName()), //NOI18N 
                    title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE, new Object[] { okButton, NotifyDescriptor.CANCEL_OPTION }, okButton);
            remove = okButton == DialogDisplayer.getDefault().notify(nd);
            removeMessage = null;
        } else {
            if (removePanel == null) {
                removePanel = new RemoveTagPanel();
            }
            removePanel.lblText.setText(NbBundle.getMessage(CreateTag.class, "CTL_TagManagerPanel.removeTagPanel.text", tagToRemove.getName())); //NOI18N
            DialogDescriptor dd = new DialogDescriptor(removePanel, title,
                    true, new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, DialogDescriptor.OK_OPTION, 
                    DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(CreateTagPanel.class), null);
            Dialog removeDlg = DialogDisplayer.getDefault().createDialog(dd);
            removeDlg.setVisible(true);
            remove = dd.getValue() == okButton;
            removeMessage = removePanel.txtMessage.getText().trim();
        }
        if (remove) {
            enableControls(false);
            HgProgressSupport supp = new HgProgressSupport() {
                @Override
                protected void perform () {
                    OutputLogger logger = getLogger();
                    try {
                        logger.outputInRed(NbBundle.getMessage(TagManager.class, "MSG_DELETE_TAG_TITLE")); //NOI18N
                        logger.outputInRed(NbBundle.getMessage(TagManager.class, "MSG_DELETE_TAG_TITLE_SEP")); //NOI18N
                        logger.output(NbBundle.getMessage(TagManager.class, "MSG_DELETE_TAG_INFO_SEP", tagToRemove.getName(), repository.getAbsolutePath())); //NOI18N
                        HgCommand.removeTag(repository, tagToRemove.getName(), tagToRemove.isLocal(), removeMessage, getLogger());
                        HgTag[] toReorg = tags;
                        List<HgTag> newTags = new ArrayList<HgTag>(toReorg.length);
                        for (HgTag tag : toReorg) {
                            if (isCanceled()) {
                                return;
                            }
                            if (tag != tagToRemove) {
                                newTags.add(tag);
                            }
                        }
                        synchronized (LOCK) {
                            tags = newTags.toArray(new HgTag[newTags.size()]);
                        }
                    } catch (HgException ex) {
                        HgUtils.notifyException(ex);
                    } finally {
                        logger.outputInRed(NbBundle.getMessage(TagManager.class, "MSG_DELETE_TAG_DONE")); //NOI18N
                        logger.output(""); //NOI18N
                        backgroundSupport = null;
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                enableControls(true);
                                applyFilter();
                            }
                        });
                    }
                }
            };
            backgroundSupport = supp;
            supp.start(Mercurial.getInstance().getRequestProcessor(repository), repository,
                    NbBundle.getMessage(CreateTag.class, "MSG_TagManagerPanel.removing.progressName", tagToRemove.getName())); //NOI18N
        }
    }

    private void enableControls (boolean enabled) {
        for (JComponent c : new JComponent[] {
                panel.txtFilter,
                panel.btnUpdate,
                panel.tagList,
                panel.btnRemove }) {
            c.setEnabled(enabled);
        }
    }
    
    private class InitialLoadingProgressSupport extends HgProgressSupport {
        @Override
        public void perform () {
            try {
                final DefaultListModel targetsModel = new DefaultListModel();
                targetsModel.addElement(INITIAL_MESSAGE);
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        panel.tagList.setModel(targetsModel);
                        if (!targetsModel.isEmpty()) {
                            panel.tagList.setSelectedIndex(0);
                        }
                    }
                });
                refreshRevisions(this);
            } finally {
                backgroundSupport = null;
            }
        }

        private void refreshRevisions (HgProgressSupport supp) {
            bGettingRevisions = true;
            OutputLogger logger = Mercurial.getInstance().getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
            HgTag[] fetchedTags;
            try {
                fetchedTags = HgCommand.getTags(repository, logger);
            } catch (HgException ex) {
                Mercurial.LOG.log(Level.INFO, null, ex);
                fetchedTags = null;
            }
            if (fetchedTags == null) {
                fetchedTags = new HgTag[0];
            }

            if (!supp.isCanceled() && fetchedTags.length > 0) {
                try {
                    parentRevision = HgCommand.getParent(repository, null, null);
                } catch (HgException ex) {
                    Mercurial.LOG.log(Level.FINE, null, ex);
                }
            }

            if (!supp.isCanceled()) {
                Arrays.sort(fetchedTags, new Comparator<HgTag>() {
                    @Override
                    public int compare (HgTag t1, HgTag t2) {
                        return t1.getName().compareTo(t2.getName());
                    }
                });
                synchronized (LOCK) {
                    tags = fetchedTags;
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

    private class TagRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof HgTag) {
                HgTag tag = (HgTag) value;
                StringBuilder sb = new StringBuilder().append(tag.getName());
                HgLogMessage.HgRevision parent = parentRevision;
                if (parent != null && parent.getRevisionNumber().equals(tag.getRevisionInfo().getRevisionNumber())) {
                    sb.append(MARK_ACTIVE_HEAD);
                }
                if (tag.isLocal()) {
                    sb.append(" - ").append(NbBundle.getMessage(TagManager.class, "LBL_TagManager.tag.local")); //NOI18N
                }
                value = sb.toString();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private void applyFilter () {
        HgTag selectedBranch = getSelectedTag();
        DefaultListModel targetsModel = new DefaultListModel();
        targetsModel.removeAllElements();
        HgTag toSelect = null;
        String filter = panel.txtFilter.getText();
        synchronized (LOCK) {
            for (HgTag tag : tags) {
                if (applies(filter, tag)) {
                    if (selectedBranch != null && tag.getRevisionInfo().getCSetShortID().equals(selectedBranch.getRevisionInfo().getCSetShortID())) {
                        toSelect = tag;
                    } else if (parentRevision != null && tag.getRevisionInfo().getCSetShortID().equals(parentRevision.getChangesetId())) {
                        toSelect = tag;
                    }
                    targetsModel.addElement(tag);
                }
            }
        }
        if (!Arrays.equals(targetsModel.toArray(), ((DefaultListModel) panel.tagList.getModel()).toArray())) {
            panel.tagList.setModel(targetsModel);
            if (toSelect != null) {
                panel.tagList.setSelectedValue(toSelect, true);
            } else if (targetsModel.size() > 0) {
                panel.tagList.setSelectedIndex(0);
            }
        }
    }

    private boolean applies (String filter, HgTag tag) {
        boolean applies = filter.isEmpty();
        filter = filter.toLowerCase();
        String localLabel = NbBundle.getMessage(TagManager.class, "LBL_TagManager.tag.local"); //NOI18N
        if (!applies) {
            HgLogMessage message = tag.getRevisionInfo();
            if (tag.getName().contains(filter)
                    || !tag.isLocal() && "global".startsWith(filter) //NOI18N
                    || tag.isLocal() && localLabel.startsWith(filter)
                    || message.getRevisionNumber().contains(filter)
                    || message.getAuthor().toLowerCase().contains(filter)
                    || message.getCSetShortID().toLowerCase().contains(filter)
                    || message.getMessage().toLowerCase().contains(filter)
                    || message.getUsername().toLowerCase().contains(filter)
                    || DateFormat.getDateTimeInstance().format(message.getDate()).toLowerCase().contains(filter)
                    ) {
                applies = true;
            }
        }
        return applies;        
    }
}
