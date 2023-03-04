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

package org.netbeans.modules.bugzilla.query;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicTreeUI;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugzilla.query.QueryParameter.ParameterValueCellRenderer;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class QueryPanel extends javax.swing.JPanel {

    final ExpandablePanel byText;
    final ExpandablePanel byDetails;
    final ExpandablePanel byPeople;
    final ExpandablePanel byLastChange;
    private final Color defaultTextColor;

    /** Creates new form QueryPanel */
    public QueryPanel(JComponent tableComponent) {
        initComponents();

        Color bkColor = UIUtils.getSectionPanelBackground();
        gotoPanel.setBackground( bkColor );
        tablePanel.setBackground( bkColor );
        urlPanel.setBackground( bkColor );
        criteriaPanel.setBackground( bkColor );

        Font f = new JLabel().getFont();
        int s = f.getSize();
        nameLabel.setFont(jLabel1.getFont().deriveFont(s * 1.7f));
        defaultTextColor = noContentLabel.getForeground();

        tablePanel.add(tableComponent);

        JTree tv = new JTree();
        BasicTreeUI tvui = (BasicTreeUI) tv.getUI();
        Icon ei = tvui.getExpandedIcon();
        Icon ci = tvui.getCollapsedIcon();

        byTextContainer.add(byTextPanel);
        byDetailsContainer.add(byDetailsPanel);
        byPeopleContainer.add(byPeoplePanel);
        byLastChangeContainer.add(byLastChangePanel);

        byText = new ExpandablePanel(byTextLabel, byTextContainer, ei, ci);
        byDetails = new ExpandablePanel(byDetailsLabel, byDetailsContainer, ei, ci);
        byPeople = new ExpandablePanel(byPeopleLabel, byPeopleContainer, ei, ci);
        byLastChange = new ExpandablePanel(byLastChangeLabel, byLastChangeContainer, ei, ci);

        byText.expand();
        byDetails.expand();
        byPeople.colapse();
        byLastChange.colapse();

        urlPanel.setVisible(false);
        queryHeaderPanel.setVisible(false);
        tableFieldsPanel.setVisible(false);
        cancelChangesButton.setVisible(false);
        filterComboBox.setVisible(false);
        filterLabel.setVisible(false);
        noContentPanel.setVisible(false);
        
        saveChangesButton.setEnabled(false);

        bugAssigneeCheckBox.setOpaque(false);
        reporterCheckBox.setOpaque(false);
        ccCheckBox.setOpaque(false);
        commenterCheckBox.setOpaque(false);

        summaryComboBox.setModel(new DefaultComboBoxModel());
        commentComboBox.setModel(new DefaultComboBoxModel());
        keywordsComboBox.setModel(new DefaultComboBoxModel());
        peopleComboBox.setModel(new DefaultComboBoxModel());
        
        summaryComboBox.setRenderer(new ParameterValueCellRenderer());
        commentComboBox.setRenderer(new ParameterValueCellRenderer());
        whiteboardComboBox.setRenderer(new ParameterValueCellRenderer());
        keywordsComboBox.setRenderer(new ParameterValueCellRenderer());
        peopleComboBox.setRenderer(new ParameterValueCellRenderer());
        severityList.setCellRenderer(new ParameterValueCellRenderer());
        issueTypeList.setCellRenderer(new ParameterValueCellRenderer());
        productList.setCellRenderer(new ParameterValueCellRenderer());
        componentList.setCellRenderer(new ParameterValueCellRenderer());
        versionList.setCellRenderer(new ParameterValueCellRenderer());
        statusList.setCellRenderer(new ParameterValueCellRenderer());
        resolutionList.setCellRenderer(new ParameterValueCellRenderer());
        priorityList.setCellRenderer(new QueryParameter.PriorityRenderer());
        changedList.setCellRenderer(new ParameterValueCellRenderer());
        tmList.setCellRenderer(new ParameterValueCellRenderer());

        filterComboBox.setRenderer(new FilterCellRenderer());

        UIUtils.keepFocusedComponentVisible(this);
        UIUtils.keepComponentsWidthByVisibleArea(this, new UIUtils.SizeController() {
            @Override
            public void setWidth(int width) {
                setContainerSize(byTextContainer, width, byTextPanel.getPreferredSize().height);
                setContainerSize(byPeopleContainer, width, byPeoplePanel.getPreferredSize().height);
                setContainerSize(byLastChangeContainer, width, byLastChangePanel.getPreferredSize().height);
            }
            private void setContainerSize(JComponent cmp, int width, int height) {
                cmp.setPreferredSize(new Dimension(width, height));
                cmp.revalidate();
            }
        });
        
        validate();
        repaint();
    }

    void setQueryRunning(final boolean running) {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                modifyButton.setEnabled(!running);
                seenButton.setEnabled(!running);
                removeButton.setEnabled(!running);
                refreshButton.setEnabled(!running);
                filterLabel.setEnabled(!running);
                filterComboBox.setEnabled(!running);
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        byLastChangePanel = new javax.swing.JPanel();
        changedLabel = new javax.swing.JLabel();
        changedAndLabel = new javax.swing.JLabel();
        changedHintLabel = new javax.swing.JLabel();
        changedWhereLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        changedBlaBlaLabel = new javax.swing.JLabel();
        byPeoplePanel = new javax.swing.JPanel();
        byTextPanel = new javax.swing.JPanel();
        tableFieldsPanel = new javax.swing.JPanel();
        tableHeaderPanel = new javax.swing.JPanel();
        filterLabel = new javax.swing.JLabel();
        criteriaPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        separatorLabel2 = new javax.swing.JLabel();
        separatorLabel3 = new javax.swing.JLabel();
        queryHeaderPanel = new javax.swing.JPanel();
        lastRefreshLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        noContentPanel = new javax.swing.JPanel();
        noContentLabel = new javax.swing.JLabel();

        byLastChangePanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        changedLabel.setLabelFor(changedFromTextField);
        org.openide.awt.Mnemonics.setLocalizedText(changedLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedLabel.text_1")); // NOI18N

        changedFromTextField.setColumns(8);
        changedFromTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedFromTextField.text")); // NOI18N

        changedAndLabel.setLabelFor(changedToTextField);
        org.openide.awt.Mnemonics.setLocalizedText(changedAndLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedAndLabel.text")); // NOI18N

        changedToTextField.setColumns(8);
        changedToTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedToTextField.text")); // NOI18N

        changedHintLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("Label.disabledForeground"));
        org.openide.awt.Mnemonics.setLocalizedText(changedHintLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedHintLabel.text")); // NOI18N

        changedWhereLabel.setLabelFor(changedList);
        org.openide.awt.Mnemonics.setLocalizedText(changedWhereLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedWhereLabel.text")); // NOI18N

        jScrollPane1.setViewportView(changedList);
        changedList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedList.AccessibleContext.accessibleDescription")); // NOI18N

        changedBlaBlaLabel.setLabelFor(newValueTextField);
        org.openide.awt.Mnemonics.setLocalizedText(changedBlaBlaLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedBlaBlaLabel.text")); // NOI18N

        newValueTextField.setColumns(20);

        javax.swing.GroupLayout byLastChangePanelLayout = new javax.swing.GroupLayout(byLastChangePanel);
        byLastChangePanel.setLayout(byLastChangePanelLayout);
        byLastChangePanelLayout.setHorizontalGroup(
            byLastChangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byLastChangePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byLastChangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(byLastChangePanelLayout.createSequentialGroup()
                        .addComponent(changedLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(byLastChangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(changedHintLabel)
                            .addGroup(byLastChangePanelLayout.createSequentialGroup()
                                .addComponent(changedFromTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(changedAndLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(changedToTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 253, Short.MAX_VALUE))
                    .addGroup(byLastChangePanelLayout.createSequentialGroup()
                        .addComponent(changedWhereLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changedBlaBlaLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)))
                .addContainerGap())
        );
        byLastChangePanelLayout.setVerticalGroup(
            byLastChangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byLastChangePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byLastChangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(changedLabel)
                    .addComponent(changedAndLabel)
                    .addComponent(changedToTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changedFromTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addComponent(changedHintLabel)
                .addGap(18, 18, 18)
                .addGroup(byLastChangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(changedWhereLabel)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(byLastChangePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(changedBlaBlaLabel)
                        .addComponent(newValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        changedFromTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedFromTextField.AccessibleContext.accessibleDescription")); // NOI18N
        changedToTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedToTextField.AccessibleContext.accessibleDescription")); // NOI18N
        newValueTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.newValueTextField.AccessibleContext.accessibleDescription")); // NOI18N

        byPeoplePanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(peopleLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.peopleLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bugAssigneeCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.bugAssigneeCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(reporterCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.reporterCheckBox.text")); // NOI18N
        reporterCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reporterCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(ccCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.ccCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(commenterCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commenterCheckBox.text")); // NOI18N

        peopleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        peopleTextField.setColumns(17);

        javax.swing.GroupLayout byPeoplePanelLayout = new javax.swing.GroupLayout(byPeoplePanel);
        byPeoplePanel.setLayout(byPeoplePanelLayout);
        byPeoplePanelLayout.setHorizontalGroup(
            byPeoplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byPeoplePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(peopleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byPeoplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(commenterCheckBox)
                    .addComponent(ccCheckBox)
                    .addGroup(byPeoplePanelLayout.createSequentialGroup()
                        .addComponent(bugAssigneeCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(peopleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(peopleTextField))
                    .addComponent(reporterCheckBox))
                .addContainerGap())
        );
        byPeoplePanelLayout.setVerticalGroup(
            byPeoplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byPeoplePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byPeoplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(byPeoplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(peopleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(peopleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(byPeoplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(bugAssigneeCheckBox)
                        .addComponent(peopleLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reporterCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ccCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(commenterCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bugAssigneeCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.bugAssigneeCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        reporterCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.reporterCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        ccCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.ccCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        commenterCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commenterCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        peopleComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.peopleComboBox.AccessibleContext.accessibleName")); // NOI18N
        peopleComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.peopleComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        peopleTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.peopleTextField.AccessibleContext.accessibleName")); // NOI18N
        peopleTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.peopleTextField.AccessibleContext.accessibleDescription")); // NOI18N

        byDetailsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        productLabel.setFont(productLabel.getFont().deriveFont(productLabel.getFont().getStyle() | java.awt.Font.BOLD));
        productLabel.setLabelFor(productList);
        org.openide.awt.Mnemonics.setLocalizedText(productLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.productLabel.text")); // NOI18N

        versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() | java.awt.Font.BOLD));
        versionLabel.setLabelFor(versionList);
        org.openide.awt.Mnemonics.setLocalizedText(versionLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.versionLabel.text")); // NOI18N

        versionScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        versionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        versionList.setVisibleRowCount(6);
        versionScrollPane.setViewportView(versionList);
        versionList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.versionList.AccessibleContext.accessibleDescription")); // NOI18N

        statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getStyle() | java.awt.Font.BOLD));
        statusLabel.setLabelFor(statusList);
        org.openide.awt.Mnemonics.setLocalizedText(statusLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.statusLabel.text")); // NOI18N

        statusScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        statusList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        statusList.setVisibleRowCount(6);
        statusScrollPane.setViewportView(statusList);
        statusList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.statusList.AccessibleContext.accessibleDescription")); // NOI18N

        resolutionLabel.setFont(resolutionLabel.getFont().deriveFont(resolutionLabel.getFont().getStyle() | java.awt.Font.BOLD));
        resolutionLabel.setLabelFor(resolutionList);
        org.openide.awt.Mnemonics.setLocalizedText(resolutionLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.resolutionLabel.text")); // NOI18N

        priorityScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        priorityList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        priorityList.setVisibleRowCount(6);
        priorityScrollPane.setViewportView(priorityList);
        priorityList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.priorityList.AccessibleContext.accessibleDescription")); // NOI18N

        priorityLabel.setFont(priorityLabel.getFont().deriveFont(priorityLabel.getFont().getStyle() | java.awt.Font.BOLD));
        priorityLabel.setLabelFor(priorityList);
        org.openide.awt.Mnemonics.setLocalizedText(priorityLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.priorityLabel.text")); // NOI18N

        resolutionScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        resolutionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        resolutionList.setVisibleRowCount(6);
        resolutionScrollPane.setViewportView(resolutionList);
        resolutionList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.resolutionList.AccessibleContext.accessibleDescription")); // NOI18N

        componentLabel.setFont(componentLabel.getFont().deriveFont(componentLabel.getFont().getStyle() | java.awt.Font.BOLD));
        componentLabel.setLabelFor(componentList);
        org.openide.awt.Mnemonics.setLocalizedText(componentLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.componentLabel.text")); // NOI18N

        componentScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        componentList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        componentList.setVisibleRowCount(6);
        componentScrollPane.setViewportView(componentList);
        componentList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.componentList.AccessibleContext.accessibleDescription")); // NOI18N

        productScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        productList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        productList.setVisibleRowCount(6);
        productScrollPane.setViewportView(productList);
        productList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.productList.AccessibleContext.accessibleDescription")); // NOI18N

        severityLabel.setFont(severityLabel.getFont().deriveFont(severityLabel.getFont().getStyle() | java.awt.Font.BOLD));
        severityLabel.setLabelFor(severityList);
        org.openide.awt.Mnemonics.setLocalizedText(severityLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.severityLabel.text")); // NOI18N

        severityScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        severityList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        severityList.setVisibleRowCount(6);
        severityScrollPane.setViewportView(severityList);
        severityList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.severityList.AccessibleContext.accessibleDescription")); // NOI18N

        issueTypeLabel.setFont(issueTypeLabel.getFont().deriveFont(issueTypeLabel.getFont().getStyle() | java.awt.Font.BOLD));
        issueTypeLabel.setLabelFor(severityList);
        org.openide.awt.Mnemonics.setLocalizedText(issueTypeLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.issueTypeLabel.text")); // NOI18N

        issueTypeScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        issueTypeList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        issueTypeList.setVisibleRowCount(6);
        issueTypeScrollPane.setViewportView(issueTypeList);

        tmLabel.setFont(tmLabel.getFont().deriveFont(tmLabel.getFont().getStyle() | java.awt.Font.BOLD));
        tmLabel.setLabelFor(severityList);
        org.openide.awt.Mnemonics.setLocalizedText(tmLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.tmLabel.text")); // NOI18N

        tmScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        tmList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        tmList.setVisibleRowCount(6);
        tmScrollPane.setViewportView(tmList);

        javax.swing.GroupLayout byDetailsPanelLayout = new javax.swing.GroupLayout(byDetailsPanel);
        byDetailsPanel.setLayout(byDetailsPanelLayout);
        byDetailsPanelLayout.setHorizontalGroup(
            byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(productLabel)
                    .addComponent(productScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(componentLabel)
                    .addComponent(componentScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(versionLabel)
                    .addComponent(versionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusLabel)
                    .addComponent(statusScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resolutionLabel)
                    .addComponent(resolutionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(priorityScrollPane)
                    .addComponent(priorityLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(severityLabel)
                    .addComponent(severityScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(issueTypeLabel)
                    .addComponent(issueTypeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tmLabel)
                    .addComponent(tmScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        byDetailsPanelLayout.setVerticalGroup(
            byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(byDetailsPanelLayout.createSequentialGroup()
                        .addComponent(tmLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tmScrollPane))
                    .addGroup(byDetailsPanelLayout.createSequentialGroup()
                        .addComponent(issueTypeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(issueTypeScrollPane))
                    .addGroup(byDetailsPanelLayout.createSequentialGroup()
                        .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(productLabel)
                            .addComponent(componentLabel)
                            .addComponent(versionLabel)
                            .addComponent(statusLabel)
                            .addComponent(resolutionLabel)
                            .addComponent(priorityLabel)
                            .addComponent(severityLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(priorityScrollPane)
                            .addComponent(productScrollPane)
                            .addComponent(componentScrollPane)
                            .addComponent(versionScrollPane)
                            .addComponent(statusScrollPane)
                            .addComponent(resolutionScrollPane)
                            .addComponent(severityScrollPane))))
                .addContainerGap())
        );

        byTextPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        summaryLabel.setLabelFor(summaryComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(summaryLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.summaryLabel.text_1")); // NOI18N

        commentLabel.setLabelFor(commentComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(commentLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commentLabel.text")); // NOI18N

        keywordsLabel.setLabelFor(keywordsComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(keywordsLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsLabel.text")); // NOI18N

        summaryComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        commentComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        keywordsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        summaryTextField.setColumns(30);

        commentTextField.setColumns(30);

        keywordsTextField.setColumns(30);

        org.openide.awt.Mnemonics.setLocalizedText(keywordsButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsButton.text")); // NOI18N
        keywordsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keywordsButtonActionPerformed(evt);
            }
        });

        whiteboardLabel.setLabelFor(commentComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(whiteboardLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.whiteboardLabel.text")); // NOI18N

        whiteboardComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        whiteboardTextField.setColumns(30);

        javax.swing.GroupLayout byTextPanelLayout = new javax.swing.GroupLayout(byTextPanel);
        byTextPanel.setLayout(byTextPanelLayout);
        byTextPanelLayout.setHorizontalGroup(
            byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byTextPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(summaryLabel)
                    .addComponent(commentLabel)
                    .addComponent(whiteboardLabel)
                    .addComponent(keywordsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(commentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(summaryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(whiteboardComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keywordsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(commentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                    .addComponent(summaryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                    .addGroup(byTextPanelLayout.createSequentialGroup()
                        .addGroup(byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(keywordsTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                            .addComponent(whiteboardTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keywordsButton)))
                .addContainerGap())
        );
        byTextPanelLayout.setVerticalGroup(
            byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byTextPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(summaryLabel)
                    .addComponent(summaryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(summaryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(commentLabel)
                    .addComponent(commentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(commentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(whiteboardLabel)
                    .addComponent(whiteboardTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(whiteboardComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keywordsLabel)
                    .addComponent(keywordsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keywordsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keywordsButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        summaryComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.summaryComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        commentComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commentComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        keywordsComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        summaryTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.summaryTextField.AccessibleContext.accessibleName")); // NOI18N
        summaryTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.summaryTextField.AccessibleContext.accessibleDescription")); // NOI18N
        commentTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commentTextField.AccessibleContext.accessibleName")); // NOI18N
        commentTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commentTextField.AccessibleContext.accessibleDescription")); // NOI18N
        keywordsTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsTextField.AccessibleContext.accessibleName")); // NOI18N
        keywordsTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        keywordsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsButton.AccessibleContext.accessibleDescription")); // NOI18N

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        tableFieldsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        tablePanel.setBackground(new java.awt.Color(224, 224, 224));
        tablePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tablePanel.setMinimumSize(new java.awt.Dimension(100, 350));
        tablePanel.setLayout(new java.awt.BorderLayout());

        tableHeaderPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(tableSummaryLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.tableSummaryLabel.text_1")); // NOI18N

        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        filterLabel.setLabelFor(filterComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.filterLabel.text_1")); // NOI18N

        javax.swing.GroupLayout tableHeaderPanelLayout = new javax.swing.GroupLayout(tableHeaderPanel);
        tableHeaderPanel.setLayout(tableHeaderPanelLayout);
        tableHeaderPanelLayout.setHorizontalGroup(
            tableHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableHeaderPanelLayout.createSequentialGroup()
                .addComponent(tableSummaryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(filterLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        tableHeaderPanelLayout.setVerticalGroup(
            tableHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(tableSummaryLabel)
                .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(filterLabel))
        );

        filterComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.filterComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout tableFieldsPanelLayout = new javax.swing.GroupLayout(tableFieldsPanel);
        tableFieldsPanel.setLayout(tableFieldsPanelLayout);
        tableFieldsPanelLayout.setHorizontalGroup(
            tableFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableFieldsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tableFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tableHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        tableFieldsPanelLayout.setVerticalGroup(
            tableFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableFieldsPanelLayout.createSequentialGroup()
                .addComponent(tableHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addContainerGap())
        );

        searchPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(webButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.webButton.text")); // NOI18N
        webButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(urlToggleButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.urlToggleButton.textUrl")); // NOI18N
        urlToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                urlToggleButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.searchButton.text")); // NOI18N

        criteriaPanel.setBackground(new java.awt.Color(224, 224, 224));

        byTextLabel.setFont(byTextLabel.getFont().deriveFont(byTextLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(byTextLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byTextLabel.text_1")); // NOI18N

        byTextContainer.setLayout(new java.awt.BorderLayout());

        byDetailsContainer.setLayout(new java.awt.BorderLayout());

        byDetailsLabel.setFont(byDetailsLabel.getFont().deriveFont(byDetailsLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(byDetailsLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byDetailsLabel.text")); // NOI18N

        byPeopleLabel.setFont(byPeopleLabel.getFont().deriveFont(byPeopleLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(byPeopleLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byPeopleLabel.text")); // NOI18N

        byPeopleContainer.setLayout(new java.awt.BorderLayout());

        byLastChangeLabel.setFont(byLastChangeLabel.getFont().deriveFont(byLastChangeLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(byLastChangeLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byLastChangeLabel.text")); // NOI18N

        byLastChangeContainer.setLayout(new java.awt.BorderLayout());

        urlPanel.setBackground(new java.awt.Color(224, 224, 224));

        jLabel2.setLabelFor(urlTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel2.text_1")); // NOI18N

        urlTextField.setColumns(50);

        javax.swing.GroupLayout urlPanelLayout = new javax.swing.GroupLayout(urlPanel);
        urlPanel.setLayout(urlPanelLayout);
        urlPanelLayout.setHorizontalGroup(
            urlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(urlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(urlTextField)
                .addContainerGap())
        );
        urlPanelLayout.setVerticalGroup(
            urlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(urlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(urlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel2))
        );

        urlTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.urlTextField.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout criteriaPanelLayout = new javax.swing.GroupLayout(criteriaPanel);
        criteriaPanel.setLayout(criteriaPanelLayout);
        criteriaPanelLayout.setHorizontalGroup(
            criteriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(urlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(byDetailsContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(criteriaPanelLayout.createSequentialGroup()
                .addGroup(criteriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(byLastChangeContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(criteriaPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(criteriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(byTextLabel)
                            .addComponent(byDetailsLabel)
                            .addComponent(byPeopleLabel)
                            .addComponent(byLastChangeLabel)))
                    .addComponent(byTextContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(byPeopleContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        criteriaPanelLayout.setVerticalGroup(
            criteriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(criteriaPanelLayout.createSequentialGroup()
                .addComponent(byTextLabel)
                .addGap(0, 0, 0)
                .addComponent(byTextContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(byDetailsLabel)
                .addGap(0, 0, 0)
                .addComponent(byDetailsContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(byPeopleLabel)
                .addGap(0, 0, 0)
                .addComponent(byPeopleContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(byLastChangeLabel)
                .addGap(0, 0, 0)
                .addComponent(byLastChangeContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(urlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(cancelChangesButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.cancelChangesButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(saveChangesButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.saveChangesButton.text")); // NOI18N

        gotoPanel.setBackground(new java.awt.Color(224, 224, 224));

        jLabel1.setLabelFor(idTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel1.text_1")); // NOI18N

        idTextField.setColumns(6);

        org.openide.awt.Mnemonics.setLocalizedText(gotoIssueButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.gotoIssueButton.text")); // NOI18N

        javax.swing.GroupLayout gotoPanelLayout = new javax.swing.GroupLayout(gotoPanel);
        gotoPanel.setLayout(gotoPanelLayout);
        gotoPanelLayout.setHorizontalGroup(
            gotoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gotoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(idTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gotoIssueButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        gotoPanelLayout.setVerticalGroup(
            gotoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gotoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(gotoIssueButton)
                .addComponent(jLabel1)
                .addComponent(idTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        idTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.idTextField.AccessibleContext.accessibleDescription")); // NOI18N
        gotoIssueButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.gotoIssueButton.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(separatorLabel2, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.separatorLabel2.text")); // NOI18N
        separatorLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(refreshConfigurationButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshConfigurationButton.text")); // NOI18N
        refreshConfigurationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshConfigurationButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(separatorLabel3, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.separatorLabel3.text")); // NOI18N
        separatorLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gotoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(saveChangesButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cancelChangesButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 496, Short.MAX_VALUE)
                .addComponent(webButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(urlToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(refreshConfigurationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(criteriaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addComponent(gotoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(criteriaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(searchButton)
                        .addComponent(cancelChangesButton)
                        .addComponent(saveChangesButton))
                    .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(refreshConfigurationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(urlToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(webButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(separatorLabel2)
                        .addComponent(separatorLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        searchPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {separatorLabel2, urlToggleButton, webButton});

        webButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.webButton.AccessibleContext.accessibleDescription")); // NOI18N
        urlToggleButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.urlToggleButton.AccessibleContext.accessibleDescription")); // NOI18N
        searchButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.searchButton.AccessibleContext.accessibleDescription")); // NOI18N
        cancelChangesButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.cancelChangesButton.AccessibleContext.accessibleDescription")); // NOI18N
        saveChangesButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.saveChangesButton.AccessibleContext.accessibleDescription")); // NOI18N
        refreshConfigurationButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshConfigurationButton.AccessibleContext.accessibleDescription")); // NOI18N

        queryHeaderPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(seenButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.seenButton.text")); // NOI18N
        seenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seenButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lastRefreshLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.lastRefreshLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lastRefreshDateLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.lastRefreshDateLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.nameLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(modifyButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.modifyButton.text")); // NOI18N
        modifyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel4.text_1")); // NOI18N
        jLabel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel5.text")); // NOI18N
        jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel6.text")); // NOI18N
        jLabel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel7.text")); // NOI18N
        jLabel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(cloneQueryButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.cloneQueryButton.text")); // NOI18N
        cloneQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cloneQueryButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout queryHeaderPanelLayout = new javax.swing.GroupLayout(queryHeaderPanel);
        queryHeaderPanel.setLayout(queryHeaderPanelLayout);
        queryHeaderPanelLayout.setHorizontalGroup(
            queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lastRefreshLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lastRefreshDateLabel))
                    .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                        .addComponent(seenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(modifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cloneQueryButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        queryHeaderPanelLayout.setVerticalGroup(
            queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                .addGroup(queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(nameLabel))
                    .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lastRefreshDateLabel)
                            .addComponent(lastRefreshLabel))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(modifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(seenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(cloneQueryButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        queryHeaderPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel4, jLabel5, jLabel6, jLabel7, modifyButton, refreshButton, removeButton});

        seenButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshButton.AccessibleContext.accessibleDescription")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.removeButton.AccessibleContext.accessibleDescription")); // NOI18N
        refreshButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.seenButton.AccessibleContext.accessibleDescription")); // NOI18N
        modifyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.modifyButton.AccessibleContext.accessibleDescription")); // NOI18N

        noContentPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        noContentPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(noContentLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.noContentLabel.text")); // NOI18N
        noContentPanel.add(noContentLabel, new java.awt.GridBagConstraints());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(queryHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tableFieldsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(noContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(queryHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableFieldsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void seenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seenButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_seenButtonActionPerformed

    private void modifyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_modifyButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_refreshButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_removeButtonActionPerformed

    private void webButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_webButtonActionPerformed

    private void urlToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlToggleButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_urlToggleButtonActionPerformed

    private void keywordsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keywordsButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_keywordsButtonActionPerformed

    private void reporterCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reporterCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_reporterCheckBoxActionPerformed

    private void refreshConfigurationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshConfigurationButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_refreshConfigurationButtonActionPerformed

    private void cloneQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cloneQueryButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cloneQueryButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JCheckBox bugAssigneeCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JPanel byDetailsContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byDetailsLabel = new javax.swing.JLabel();
    final javax.swing.JPanel byDetailsPanel = new javax.swing.JPanel();
    final javax.swing.JPanel byLastChangeContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byLastChangeLabel = new javax.swing.JLabel();
    private javax.swing.JPanel byLastChangePanel;
    final javax.swing.JPanel byPeopleContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byPeopleLabel = new javax.swing.JLabel();
    private javax.swing.JPanel byPeoplePanel;
    final javax.swing.JPanel byTextContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byTextLabel = new javax.swing.JLabel();
    private javax.swing.JPanel byTextPanel;
    final javax.swing.JButton cancelChangesButton = new javax.swing.JButton();
    final javax.swing.JCheckBox ccCheckBox = new javax.swing.JCheckBox();
    private javax.swing.JLabel changedAndLabel;
    private javax.swing.JLabel changedBlaBlaLabel;
    final javax.swing.JTextField changedFromTextField = new javax.swing.JTextField();
    private javax.swing.JLabel changedHintLabel;
    private javax.swing.JLabel changedLabel;
    final javax.swing.JList changedList = new javax.swing.JList();
    final javax.swing.JTextField changedToTextField = new javax.swing.JTextField();
    private javax.swing.JLabel changedWhereLabel;
    public final org.netbeans.modules.bugtracking.commons.LinkButton cloneQueryButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final javax.swing.JComboBox commentComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel commentLabel = new javax.swing.JLabel();
    final javax.swing.JTextField commentTextField = new javax.swing.JTextField();
    final javax.swing.JCheckBox commenterCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JLabel componentLabel = new javax.swing.JLabel();
    final javax.swing.JList componentList = new javax.swing.JList();
    final javax.swing.JScrollPane componentScrollPane = new HackedScrollPane();
    private javax.swing.JPanel criteriaPanel;
    final javax.swing.JComboBox filterComboBox = new javax.swing.JComboBox();
    private javax.swing.JLabel filterLabel;
    final javax.swing.JButton gotoIssueButton = new javax.swing.JButton();
    final javax.swing.JPanel gotoPanel = new javax.swing.JPanel();
    final javax.swing.JTextField idTextField = new javax.swing.JTextField();
    final javax.swing.JLabel issueTypeLabel = new javax.swing.JLabel();
    final javax.swing.JList issueTypeList = new javax.swing.JList();
    final javax.swing.JScrollPane issueTypeScrollPane = new HackedScrollPane();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    final javax.swing.JButton keywordsButton = new javax.swing.JButton();
    final javax.swing.JComboBox keywordsComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel keywordsLabel = new javax.swing.JLabel();
    final javax.swing.JTextField keywordsTextField = new javax.swing.JTextField();
    final javax.swing.JLabel lastRefreshDateLabel = new javax.swing.JLabel();
    private javax.swing.JLabel lastRefreshLabel;
    public final org.netbeans.modules.bugtracking.commons.LinkButton modifyButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final javax.swing.JLabel nameLabel = new javax.swing.JLabel();
    final javax.swing.JTextField newValueTextField = new javax.swing.JTextField();
    private javax.swing.JLabel noContentLabel;
    private javax.swing.JPanel noContentPanel;
    final javax.swing.JComboBox peopleComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel peopleLabel = new javax.swing.JLabel();
    final javax.swing.JTextField peopleTextField = new javax.swing.JTextField();
    final javax.swing.JLabel priorityLabel = new javax.swing.JLabel();
    final javax.swing.JList priorityList = new javax.swing.JList();
    final javax.swing.JScrollPane priorityScrollPane = new HackedScrollPane();
    final javax.swing.JLabel productLabel = new javax.swing.JLabel();
    final javax.swing.JList productList = new javax.swing.JList();
    final javax.swing.JScrollPane productScrollPane = new javax.swing.JScrollPane();
    private javax.swing.JPanel queryHeaderPanel;
    final org.netbeans.modules.bugtracking.commons.LinkButton refreshButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final org.netbeans.modules.bugtracking.commons.LinkButton refreshConfigurationButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    public final org.netbeans.modules.bugtracking.commons.LinkButton removeButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final javax.swing.JCheckBox reporterCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JLabel resolutionLabel = new javax.swing.JLabel();
    final javax.swing.JList resolutionList = new javax.swing.JList();
    final javax.swing.JScrollPane resolutionScrollPane = new HackedScrollPane();
    final javax.swing.JButton saveChangesButton = new javax.swing.JButton();
    final javax.swing.JButton searchButton = new javax.swing.JButton();
    final javax.swing.JPanel searchPanel = new javax.swing.JPanel();
    final org.netbeans.modules.bugtracking.commons.LinkButton seenButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    private javax.swing.JLabel separatorLabel2;
    private javax.swing.JLabel separatorLabel3;
    final javax.swing.JLabel severityLabel = new javax.swing.JLabel();
    final javax.swing.JList severityList = new javax.swing.JList();
    final javax.swing.JScrollPane severityScrollPane = new HackedScrollPane();
    final javax.swing.JLabel statusLabel = new javax.swing.JLabel();
    final javax.swing.JList statusList = new javax.swing.JList();
    final javax.swing.JScrollPane statusScrollPane = new HackedScrollPane();
    final javax.swing.JComboBox summaryComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel summaryLabel = new javax.swing.JLabel();
    final javax.swing.JTextField summaryTextField = new javax.swing.JTextField();
    private javax.swing.JPanel tableFieldsPanel;
    private javax.swing.JPanel tableHeaderPanel;
    final javax.swing.JPanel tablePanel = new javax.swing.JPanel();
    final javax.swing.JLabel tableSummaryLabel = new javax.swing.JLabel();
    final javax.swing.JLabel tmLabel = new javax.swing.JLabel();
    final javax.swing.JList tmList = new javax.swing.JList();
    final javax.swing.JScrollPane tmScrollPane = new HackedScrollPane();
    final javax.swing.JPanel urlPanel = new javax.swing.JPanel();
    final javax.swing.JTextField urlTextField = new javax.swing.JTextField();
    final org.netbeans.modules.bugtracking.commons.LinkButton urlToggleButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final javax.swing.JLabel versionLabel = new javax.swing.JLabel();
    final javax.swing.JList versionList = new javax.swing.JList();
    final javax.swing.JScrollPane versionScrollPane = new HackedScrollPane();
    final org.netbeans.modules.bugtracking.commons.LinkButton webButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final javax.swing.JComboBox whiteboardComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel whiteboardLabel = new javax.swing.JLabel();
    final javax.swing.JTextField whiteboardTextField = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables

    /**
     * enables/disables all but the parameter fields
     * @param enable
     */
    void enableFields(final boolean enable) {
        
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                summaryLabel.setEnabled(enable);
                whiteboardLabel.setEnabled(enable);
                commentLabel.setEnabled(enable);
                keywordsLabel.setEnabled(enable);
                keywordsButton.setEnabled(enable);
                
                productLabel.setEnabled(enable);
                componentLabel.setEnabled(enable);
                versionLabel.setEnabled(enable);
                statusLabel.setEnabled(enable);
                severityLabel.setEnabled(enable);
                resolutionLabel.setEnabled(enable);
                priorityLabel.setEnabled(enable);
                tmLabel.setEnabled(enable);
                issueTypeLabel.setEnabled(enable);
                
                peopleLabel.setEnabled(enable);
                peopleTextField.setEnabled(enable);
                
                searchButton.setEnabled(enable);
                webButton.setEnabled(enable);
                urlToggleButton.setEnabled(enable);
                refreshConfigurationButton.setEnabled(enable);
                
                changedLabel.setEnabled(enable);
                changedAndLabel.setEnabled(enable);
                changedWhereLabel.setEnabled(enable);
                changedBlaBlaLabel.setEnabled(enable);
                changedHintLabel.setEnabled(enable);
            }
        });
    }

    void switchQueryFields(final boolean showAdvanced) {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                byDetails.setVisible(showAdvanced);
                byText.setVisible(showAdvanced);
                byLastChange.setVisible(showAdvanced);
                byPeople.setVisible(showAdvanced);
                
                urlPanel.setVisible(!showAdvanced);
                if (showAdvanced) {
                    urlToggleButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.urlToggleButton.textUrl"));
                } else {
                    urlToggleButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.urlToggleButton.textForm"));
                }
            }
        });
    }

    void showError(final String text) {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                noContentPanel.setVisible(true);
                tableSummaryLabel.setVisible(false);
                tableFieldsPanel.setVisible(false);
                if (text != null) {
                    noContentLabel.setForeground(BugzillaUtil.getErrorForegroundColor());
                    noContentLabel.setText(text);
                }
            }
        });
    }

    void showSearchingProgress(final boolean on, final String text) {
        UIUtils.runInAWT(new Runnable() {
            public void run() {
                noContentPanel.setVisible(on);
                tableSummaryLabel.setVisible(!on);
                tableFieldsPanel.setVisible(!on);
                if (on && text != null) {
                    noContentLabel.setForeground(defaultTextColor);
                    noContentLabel.setText(text);
                }
            }
        });
    }

    void showRetrievingProgress(final boolean on, final String text, final boolean searchPanelVisible) {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                noContentPanel.setVisible(on);
                noContentLabel.setForeground(Color.red);
                if (searchPanelVisible) {
                    searchPanel.setVisible(!on);
                }
                if (on && text != null) {
                    noContentLabel.setForeground(defaultTextColor);
                    noContentLabel.setText(text);
                }
            }
        });
    }

    void showNoContentPanel(boolean on) {
        showSearchingProgress(on, null);
    }

    void setModifyVisible(final boolean b) {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                searchPanel.setVisible(b);
                cancelChangesButton.setVisible(b);
                saveChangesButton.setVisible(b);
                
                webButton.setVisible(b);
                separatorLabel2.setVisible(b);
                
                tableFieldsPanel.setVisible(!b);
                searchButton.setVisible(!b);
                urlToggleButton.setVisible(!b);
                
                separatorLabel3.setVisible(!b);
            }
        });
    }

    void setSaved(final String name, final String lastRefresh) {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                searchPanel.setVisible(false);
                gotoPanel.setVisible(false);
                queryHeaderPanel.setVisible(true);
                tableHeaderPanel.setVisible(true);
                filterComboBox.setVisible(true); // XXX move to bugtracking IssueTable component
                filterLabel.setVisible(true);
                tablePanel.setVisible(true);
                nameLabel.setText(name);
                setLastRefresh(lastRefresh);
            }
        });
    }

    void setLastRefresh(final String lastRefresh) {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                lastRefreshDateLabel.setText(lastRefresh);
            }
        });
    }

    void setNBFieldsVisible(final boolean visible) {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                whiteboardLabel.setVisible(visible);
                whiteboardComboBox.setVisible(visible);
                whiteboardTextField.setVisible(visible);
                
                issueTypeLabel.setVisible(visible);
                issueTypeList.setVisible(visible);
                issueTypeScrollPane.setVisible(visible);
                severityLabel.setVisible(!visible);
                severityList.setVisible(!visible);
                severityScrollPane.setVisible(!visible);
            }
        });
    }

    public void focusLost(FocusEvent e) {
        // do nothing
    }

    class ExpandablePanel {
        private final JPanel panel;
        private final JLabel label;
        private final Icon ei;
        private final Icon ci;
        private boolean expaned = true;
        public ExpandablePanel(JLabel l, JPanel p, final Icon ei, final Icon ci) {
            this.panel = p;
            this.label = l;
            this.ci = ci;
            this.ei = ei;
            this.label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(panel.isVisible()) {
                        colapse();
                    } else {
                        expand();
                    }
                }
            });
        }
        public void expand() {
            expaned = true;
            panel.setVisible(true);
            label.setIcon(ei);
        }
        public void colapse() {
            expaned = false;
            panel.setVisible(false);
            label.setIcon(ci);
        }
        public void setVisible(boolean visible ) {
            label.setVisible(visible);
            panel.setVisible(visible && expaned);
        }
    }

    private static class FilterCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(value instanceof Filter) {
                value = ((Filter)value).getDisplayName();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    static class HackedScrollPane extends JScrollPane {
        @Override
        public Dimension getPreferredSize() {
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            Dimension dim = super.getPreferredSize();
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            return dim;
        }
    }

}
