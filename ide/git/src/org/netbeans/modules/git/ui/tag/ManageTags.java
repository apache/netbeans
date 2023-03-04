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
package org.netbeans.modules.git.ui.tag;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitObjectType;
import org.netbeans.libs.git.GitTag;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.ui.repository.RevisionInfoPanel;
import org.netbeans.modules.git.ui.repository.RevisionInfoPanelController;
import org.netbeans.modules.git.ui.tag.CreateTagAction.CreateTagProcess;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.git.utils.WizardStepProgressSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.QuickSearch;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
class ManageTags implements ListSelectionListener, ActionListener {

    private final File repository;
    private final Map<String, GitTag> tags;
    private final RevisionInfoPanelController revisionInfoController;
    private final ManageTagsPanel panel;
    private final RevisionInfoPanel revisionInfoPanel;
    private Dialog dialog;
    private QuickSearch quickSearch;

    public ManageTags (File repository, Map<String, GitTag> tags, String preselectedTag) {
        this.repository = repository;
        this.tags = new HashMap<>(tags);
        this.revisionInfoController = new RevisionInfoPanelController(repository);
        this.revisionInfoPanel = revisionInfoController.getPanel();
        this.panel = new ManageTagsPanel(revisionInfoPanel);
        attachListeners();
        initTagInfo();
        initTags(preselectedTag);
        panel.lstTags.setCellRenderer(new TagRenderer());
    }

    public void show () {
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ManageTags.class, "LBL_ManageTags.title"), true,  //NOI18N
                new Object[] { DialogDescriptor.OK_OPTION }, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(ManageTags.class), null);
        dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
    }

    private void attachListeners () {
        panel.lstTags.addListSelectionListener(this);
        panel.btnDelete.addActionListener(this);
        panel.btnCreate.addActionListener(this);
    }

    @Override
    public void valueChanged (ListSelectionEvent e) {
        if (e.getSource() == panel.lstTags && !e.getValueIsAdjusting()) {
            GitTag selectedTag = null;
            Object selectedObject = panel.lstTags.getSelectedValue();
            if (selectedObject instanceof GitTag) {
                selectedTag = (GitTag) panel.lstTags.getSelectedValue();
            }
            updateTagInfo(selectedTag);
        }
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        Object selectedTag = panel.lstTags.getSelectedValue();
        if (e.getSource() == panel.btnDelete && selectedTag instanceof GitTag) {
            GitTag tag = ((GitTag) selectedTag);
            if (JOptionPane.showConfirmDialog(dialog, NbBundle.getMessage(ManageTags.class, "MSG_ManageTags.deleteTag.confirmation", tag.getTagName()), //NOI18N
                    NbBundle.getMessage(ManageTags.class, "LBL_ManageTags.deleteTag.confirmation"), //NOI18N
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                DeleteTagProgressSupport supp = new DeleteTagProgressSupport(panel.panelProgress, tag);
                supp.setEnabled(false);
                supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(ManageTags.class, "MSG_ManageTags.deleteTag.progress")); //NOI18N
            }
        } else if (e.getSource() == panel.btnCreate) {
            String initialRev = "";
            if (selectedTag instanceof GitTag) {
                initialRev = ((GitTag) selectedTag).getTagName();
            }
            final CreateTag createTag = new CreateTag(repository, initialRev, initialRev);
            if (createTag.show()) {
                WizardStepProgressSupport supp = new CreateTagProgressSupport(panel.panelProgress, createTag);
                supp.setEnabled(false);
                supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(CreateTagAction.class, "LBL_CreateTagAction.progressName")); //NOI18N
            }
        }
    }

    private void updateTagInfo (GitTag selectedTag) {
        if (selectedTag != null) {
            panel.txtTagName.setText(selectedTag.getTagName());
            if (selectedTag.isLightWeight()) {
                displayObjectInfo(false);
            } else {
                displayObjectInfo(true);
                setText(panel.txtTagId, selectedTag.getTagId());
                setText(panel.txtTagMessage, selectedTag.getMessage());
                setText(panel.txtTagger, selectedTag.getTagger().toString());
            }
            if (selectedTag.getTaggedObjectType() == GitObjectType.COMMIT) {
                revisionInfoController.loadInfo(selectedTag.getTaggedObjectId());
                revisionInfoPanel.setVisible(true);
                panel.lblTaggedObject.setVisible(false);
                panel.txtTaggedObject.setVisible(false);
            } else {
                revisionInfoPanel.setVisible(false);
                panel.lblTaggedObject.setVisible(true);
                panel.txtTaggedObject.setVisible(true);
                setText(panel.txtTaggedObject, selectedTag.getTaggedObjectType().toString() + " - " + selectedTag.getTaggedObjectId());
            }
        }
    }

    private void initTagInfo () {
        revisionInfoController.loadInfo(null);
        displayObjectInfo(false);
        panel.lblTaggedObject.setVisible(false);
        panel.txtTaggedObject.setVisible(false);
        revisionInfoController.loadInfo(null);
        panel.txtTagName.setText(NbBundle.getMessage(ManageTags.class, "MSG_ManageTags.noTagSelected")); //NOI18N
    }

    private void initTags (String preselectedTag) {
        DefaultListModel model = new DefaultListModel();
        GitTag selected = null;
        GitTag[] tagArray = tags.values().toArray(new GitTag[tags.values().size()]);
        Arrays.sort(tagArray, new Comparator<GitTag>() {
            @Override
            public int compare (GitTag o1, GitTag o2) {
                return o1.getTagName().compareTo(o2.getTagName());
            }
        });
        for (GitTag tag : tagArray) {
            if (tag.getTagName().equals(preselectedTag)) {
                selected = tag;
            }
            model.addElement(tag);
        }
        panel.lstTags.setModel(model);
        if (selected != null) {
            panel.lstTags.setSelectedValue(selected, true);
        }
        if (quickSearch != null) {
            quickSearch.detach();
            quickSearch = null;
        }
        if (!model.isEmpty()) {
            quickSearch = GitUtils.attachQuickSearch(Arrays.asList(tagArray), panel.tagsPanel, panel.lstTags, model, new GitUtils.SearchCallback<GitTag>() {

                @Override
                public boolean contains (GitTag item, String needle) {
                    return item.getTagName().toLowerCase().contains(needle.toLowerCase());
                }
            });
        }
    }

    private void setText (JTextComponent comp, String text) {
        comp.setText(text);
        comp.setCaretPosition(comp.getText().length());
        comp.moveCaretPosition(0);
    }
    
    private static class TagRenderer extends DefaultListCellRenderer {

        private static final Icon TAG_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/git/resources/icons/tag.png", true); //NOI18N
        private static final JLabel renderer = new JLabel("", TAG_ICON, SwingConstants.LEADING); //NOI18N
        
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof GitTag) {
                GitTag tag = (GitTag) value;
                renderer.setText(tag.getTagName());
                renderer.setFont(list.getFont());
                renderer.setOpaque(true);
                renderer.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                renderer.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                renderer.setEnabled(list.isEnabled());
                Border border = null;
                if (cellHasFocus) {
                    if (isSelected) {
                        border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
                    }
                    if (border == null) {
                        border = UIManager.getBorder("List.focusCellHighlightBorder");
                    }
                } else {
                    border = new EmptyBorder(1, 1, 1, 1);
                }
                renderer.setBorder(border);
                return renderer;
            } else {
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }
        
    }
    
    private class DeleteTagProgressSupport extends WizardStepProgressSupport {
        private final GitTag tag;

        public DeleteTagProgressSupport (JPanel panel, GitTag tag) {
            super(panel, false);
            this.tag = tag;
        }

        @Override
        public void perform() {
            try {
                getClient().deleteTag(tag.getTagName(), GitUtils.NULL_PROGRESS_MONITOR);
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        tags.remove(tag.getTagName());
                        initTags(null);
                    }
                });
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, false);
            }
        }

        @Override
        public void setEnabled (boolean editable) {
            panel.btnDelete.setEnabled(editable);
            panel.btnCreate.setEnabled(editable);
        }        
    };
    
    private class CreateTagProgressSupport extends WizardStepProgressSupport {
        private final CreateTag createTag;

        public CreateTagProgressSupport (JPanel panel, CreateTag createTag) {
            super(panel, false);
            this.createTag = createTag;
        }

        @Override
        public void perform() {
            try {
                final GitTag newTag = new CreateTagProcess(createTag, this, getClient()).call();
                if (newTag != null) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            tags.put(newTag.getTagName(), newTag);
                            String selected = null;
                            if (panel.lstTags.getSelectedValue() instanceof GitTag) {
                                selected = ((GitTag) panel.lstTags.getSelectedValue()).getTagName();
                            }
                            initTags(selected);
                        }
                    });
                }
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, false);
            }
        }

        @Override
        public void setEnabled (boolean editable) {
            panel.btnDelete.setEnabled(editable);
            panel.btnCreate.setEnabled(editable);
        }        
    };
    
    private void displayObjectInfo (boolean visible) {
        for (JComponent comp : new JComponent[] {
            panel.lblTagId, panel.lblTagger, panel.lblTagMessage,
            panel.txtTagId, panel.txtTagger, panel.jScrollPane2
        }) {
            comp.setVisible(visible);
        }
    }
}
