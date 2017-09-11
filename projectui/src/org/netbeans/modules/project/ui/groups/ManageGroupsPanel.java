/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.project.ui.groups;

import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkozeny
 */
public class ManageGroupsPanel extends javax.swing.JPanel implements PropertyChangeListener{

    private static final RequestProcessor RP = new RequestProcessor(ManageGroupsPanel.class.getName());
    
    private static final String NONE_GOUP = "(none)";
    
    /**
     * Creates new form ManageGroupPanel
     */
    public ManageGroupsPanel() {
        initComponents();
        DefaultListModel model = new DefaultListModel();
        String selectedValue = null;
        for (final Group g : Group.allGroups()) {
            model.addElement(g.getName());
            if(g.equals(Group.getActiveGroup())) {
                selectedValue = g.getName();
            }
        }
        model.addElement(NONE_GOUP);
        groupList.setModel(model);
        groupList.setSelectedValue(selectedValue == null? NONE_GOUP : selectedValue, true);
        groupList.setEnabled(model.getSize() > 0);
        groupList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        groupList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                firePropertyChange("selection", null, null);
            }
        });
        groupList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            Group.setActiveGroup(getSelectedGroups()[0], false);
                        }
                    });
                    final Window w = SwingUtilities.getWindowAncestor(ManageGroupsPanel.this);
                    if (w != null) {
                        w.setVisible(false);
                        w.dispose();
                    }
                }
            }
        });
        final boolean isReady = isReady();
        final boolean isNoneGroupSelected = isNoneGroupSelected();
        removeButton.setEnabled(isReady && isAtLeastOneGroupSelected() && !isNoneGroupSelected);
        removeAllButton.setEnabled(isReady && model.getSize() > 1);
        propertiesButton.setEnabled(isReady && isExactlyOneGroupSelected() &&  !isNoneGroupSelected);
        addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("selection")) {
                    final boolean isNoneGroupSelected = isNoneGroupSelected();
                    removeButton.setEnabled(isAtLeastOneGroupSelected() && !isNoneGroupSelected);
                    removeAllButton.setEnabled(groupList.getModel().getSize() > 1);
                    propertiesButton.setEnabled(isExactlyOneGroupSelected() && !isNoneGroupSelected);
                    groupList.setEnabled(groupList.getModel().getSize() > 0);
                }
            }
        });
    }
    
    private boolean isReady() {
        ListModel model = groupList.getModel ();
        if(model.getSize() != Group.allGroups().size() + 1 ) {
            return false;
        }
        return true;
    }
    
    private boolean isNoneGroupSelected() {
        final Group [] selectedGroups = getSelectedGroups();
        for(Group groupIter:selectedGroups) {
            if(groupIter == null) {
                return true;
            }
        }
        return false;
    }

    private boolean isAtLeastOneGroupSelected() {
        return groupList.getSelectedValuesList().size() >= 1;
    }

    final boolean isExactlyOneGroupSelected() {
        return groupList.getSelectedValuesList().size() == 1;
    }

    Group[] getSelectedGroups() {
        Group[] selection = new Group[groupList.getSelectedValuesList().size()];
        for (int i = 0; i < groupList.getSelectedValuesList().size(); i++) {
            String groupName = (String) groupList.getSelectedValuesList().get(i);
            for (Group g : Group.allGroups()) {
                if (g.getName().equals(groupName)) {
                    selection[i] = g;
                } else if (NONE_GOUP.equals(groupName)) {
                    selection[i] = null;
                }
            }
        }
        return selection;
    }
    
    private void removeGroups(Iterable<Group> groups) {
        DefaultListModel model = (DefaultListModel) groupList.getModel();
        for (final Group groupIter : groups) {
            if(groupIter != null) {
                model.removeElement(groupIter.getName());
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        groupIter.destroy();
                    }
                });
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectionLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        groupList = new javax.swing.JList();
        propertiesButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();

        setMinimumSize(new java.awt.Dimension(600, 250));
        setPreferredSize(new java.awt.Dimension(600, 250));

        selectionLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        org.openide.awt.Mnemonics.setLocalizedText(selectionLabel, org.openide.util.NbBundle.getMessage(ManageGroupsPanel.class, "ManageGroupsPanel.selectionLabel.text")); // NOI18N
        selectionLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(300, 130));
        jScrollPane1.setViewportView(groupList);

        org.openide.awt.Mnemonics.setLocalizedText(propertiesButton, org.openide.util.NbBundle.getMessage(ManageGroupsPanel.class, "ManageGroupsPanel.propertiesButton.text")); // NOI18N
        propertiesButton.setMaximumSize(new java.awt.Dimension(105, 29));
        propertiesButton.setMinimumSize(new java.awt.Dimension(105, 29));
        propertiesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                propertiesButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(ManageGroupsPanel.class, "ManageGroupsPanel.removeButton.text")); // NOI18N
        removeButton.setMaximumSize(new java.awt.Dimension(87, 29));
        removeButton.setMinimumSize(new java.awt.Dimension(87, 29));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeAllButton, org.openide.util.NbBundle.getMessage(ManageGroupsPanel.class, "ManageGroupsPanel.removeAllButton.text")); // NOI18N
        removeAllButton.setMaximumSize(new java.awt.Dimension(87, 29));
        removeAllButton.setMinimumSize(new java.awt.Dimension(87, 29));
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(removeAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(propertiesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jSeparator2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(selectionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1)))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(selectionLabel)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(propertiesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 101, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void propertiesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertiesButtonActionPerformed
        Group selectedGroup = getSelectedGroups()[0];
        selectedGroup.addChangeListener(this);
        GroupsMenu.openProperties(selectedGroup);
        selectedGroup.removeChangeListener(this);
    }//GEN-LAST:event_propertiesButtonActionPerformed

    @Messages({"ManageGroupsPanel.wrn_remove_selected_groups_msg=Are you sure to remove selected groups?",
            "ManageGroupsPanel.wrn_remove_selected_groups_title=Confirm remove groups"})
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(Bundle.ManageGroupsPanel_wrn_remove_selected_groups_msg(), Bundle.ManageGroupsPanel_wrn_remove_selected_groups_title(), NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
            removeGroups(Arrays.asList(getSelectedGroups()));
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    @Messages({"ManageGroupsPanel.wrn_remove_all_groups_msg=Are you sure to remove all groups?",
            "ManageGroupsPanel.wrn_remove_all_groups_title=Confirm remove all groups"})
    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(Bundle.ManageGroupsPanel_wrn_remove_all_groups_msg(), Bundle.ManageGroupsPanel_wrn_remove_all_groups_title(), NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
            removeGroups(Group.allGroups());
        }
    }//GEN-LAST:event_removeAllButtonActionPerformed

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getPropertyName().equals("groupRename")) {
            String oldGroupName = (String)event.getOldValue();
            String newGroupName = (String)event.getNewValue();
            DefaultListModel model = (DefaultListModel) groupList.getModel();
            for(int i = 0; i < model.getSize(); i++) {
                if(((String)model.getElementAt(i)).equals(oldGroupName)) {
                    model.setElementAt(newGroupName, i);
                }
            }
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList groupList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton propertiesButton;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JLabel selectionLabel;
    // End of variables declaration//GEN-END:variables
}
