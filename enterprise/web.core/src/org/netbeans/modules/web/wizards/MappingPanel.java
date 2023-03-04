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
package org.netbeans.modules.web.wizards;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * Wizard panel that collects deployment data for Servlets and Filters
 * @author Ana von Klopp 
 */
class MappingPanel extends JPanel implements ActionListener,
        TableModelListener, ListSelectionListener {

    private static final String ADD = "add";
    private static final String EDIT = "edit";
    private static final String REMOVE = "remove";
    private static final String UP = "up";
    private static final String DOWN = "down";

    // UI Variables
    private JLabel jLtableheader;
    private MappingTable table;
    private JButton jBnew,  jBedit,  jBdelete,  jBdown,  jBup;
    private JScrollPane scrollP;
    private ServletData deployData;
    private BaseWizardPanel parent;
    private boolean edited = false;
    private static final long serialVersionUID = 6540270797782597645L;
    private TemplateWizard wizard;

    public MappingPanel(ServletData deployData, BaseWizardPanel parent, TemplateWizard wizard) {
        this.deployData = deployData;
        this.parent = parent;
        this.wizard = wizard;
        initComponents();
}

    private void initComponents() {
        // Layout description
        setLayout(new java.awt.GridBagLayout());

        // Entity covers entire row
        GridBagConstraints fullRowC = new GridBagConstraints();
        fullRowC.gridx = 0;
        fullRowC.gridy = 0;
        fullRowC.gridwidth = 2;
        fullRowC.anchor = GridBagConstraints.WEST;
        fullRowC.fill = GridBagConstraints.HORIZONTAL;
        fullRowC.insets = new Insets(4, 0, 4, 0);

        // Button
        GridBagConstraints bC = new GridBagConstraints();
        bC.gridx = 1;
        bC.gridy = 1;
        bC.weightx = 0.05;
        bC.fill = GridBagConstraints.HORIZONTAL;
        bC.insets = new Insets(4, 20, 4, 0);

        // Table panel
        GridBagConstraints tablePanelC = new GridBagConstraints();
        tablePanelC.gridx = 0;
        tablePanelC.gridy = 1;
        tablePanelC.gridheight = 6;
        tablePanelC.fill = GridBagConstraints.BOTH;
        tablePanelC.weightx = 0.95;
        tablePanelC.weighty = 1.0;
        tablePanelC.anchor = GridBagConstraints.WEST;
        tablePanelC.insets = new Insets(4, 0, 4, 0);

        // Filler panel
        GridBagConstraints fillerC = new GridBagConstraints();
        fillerC.gridx = 1;
        fillerC.gridy = GridBagConstraints.RELATIVE;
        fillerC.fill = GridBagConstraints.BOTH;
        fillerC.weighty = 1.0;
        fillerC.insets = new Insets(4, 0, 4, 0);

        // 2. Table header
        jLtableheader = new JLabel(NbBundle.getMessage(MappingPanel.class, "LBL_filter_mappings"));
        jLtableheader.setDisplayedMnemonic(NbBundle.getMessage(MappingPanel.class, "LBL_filter_mappings_mnemonic").charAt(0));
        jLtableheader.setLabelFor(table);
        this.add(jLtableheader, fullRowC);

        // 3. Table row
        table = new MappingTable(deployData.getName(),
                deployData.getFilterMappings());
        jLtableheader.setLabelFor(table);
        table.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MappingPanel.class, "ACSD_filter_mappings")); // NOI18N
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MappingPanel.class, "ACSD_filter_mappings_desc")); // NOI18N

        table.getModel().addTableModelListener(this);
        table.getSelectionModel().addListSelectionListener(this);
        scrollP = new JScrollPane(table);
        table.setPreferredScrollableViewportSize(new Dimension(300, 200));
        this.add(scrollP, tablePanelC);

        jBnew = new JButton();
        jBnew.setText(NbBundle.getMessage(MappingPanel.class,
                "LBL_newdots")); //NOI18N
        jBnew.setMnemonic(NbBundle.getMessage(MappingPanel.class, "LBL_new_mnemonic").charAt(0));
        jBnew.addActionListener(this);
        jBnew.setActionCommand(ADD);
        jBnew.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MappingPanel.class, "ACSD_filter_mappings_new"));
        this.add(jBnew, bC);

        bC.gridy++;
        jBedit = new JButton();
        jBedit.setText(NbBundle.getMessage(MappingPanel.class,
                "LBL_edit")); //NOI18N
        jBedit.setMnemonic(NbBundle.getMessage(MappingPanel.class, "LBL_edit_mnemonic").charAt(0));
        jBedit.addActionListener(this);
        jBedit.setActionCommand(EDIT);
        jBedit.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MappingPanel.class, "ACSD_filter_mappings_edit"));
        this.add(jBedit, bC);

        bC.gridy++;
        jBdelete = new JButton();
        jBdelete.setText(NbBundle.getMessage(MappingPanel.class,
                "LBL_delete")); //NOI18N
        jBdelete.setMnemonic(NbBundle.getMessage(MappingPanel.class, "LBL_delete_mnemonic").charAt(0));
        jBdelete.addActionListener(this);
        jBdelete.setActionCommand(REMOVE);
        jBdelete.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MappingPanel.class, "ACSD_filter_mappings_delete"));
        this.add(jBdelete, bC);

        bC.gridy++;
        jBup = new JButton();
        jBup.setText(NbBundle.getMessage(MappingPanel.class,
                "LBL_move_up")); //NOI18N
        jBup.setMnemonic(NbBundle.getMessage(MappingPanel.class, "LBL_move_up_mnemonic").charAt(0));
        jBup.addActionListener(this);
        jBup.setActionCommand(UP);
        jBup.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MappingPanel.class, "ACSD_filter_mappings_up"));
        this.add(jBup, bC);

        bC.gridy++;
        jBdown = new JButton();
        jBdown.setText(NbBundle.getMessage(MappingPanel.class,
                "LBL_move_down")); //NOI18N
        jBdown.setMnemonic(NbBundle.getMessage(MappingPanel.class, "LBL_move_down_mnemonic").charAt(0));
        jBdown.addActionListener(this);
        jBdown.setActionCommand(DOWN);
        jBdown.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MappingPanel.class, "ACSD_filter_mappings_down"));
        this.add(jBdown, bC);

        bC.gridy++;
        bC.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        this.add(filler, bC);

        this.add(new javax.swing.JPanel(), fillerC);
    }

    void setData() {
        // Check if the name has changed - if it has, then we
        // change all the entries for this filter.
        table.setFilterName(deployData.getName());

        if (!edited) {
            if (!deployData.makeEntry() && !Utilities.isJavaEE6Plus(wizard)) {
                this.setEnabled(false);
                return;
            }

            if (table.getRowCount() > 0)
                table.setRowSelectionInterval(0, 0);
            edited = true;
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() instanceof JButton) {
            if (evt.getActionCommand() == ADD) {
                FilterMappingData fmd = new FilterMappingData(deployData.getName());
                MappingEditor editor = new MappingEditor(fmd, deployData.getServletNames());
                editor.showEditor();
                if (editor.isOK()) {
                    table.addRow(0, fmd);
                }
            } else if (evt.getActionCommand() == EDIT) {
                int index = table.getSelectedRow();
                FilterMappingData fmd, fmd2;
                fmd = table.getRow(index);
                fmd2 = (FilterMappingData) (fmd.clone());
                MappingEditor editor = new MappingEditor(fmd2, deployData.getServletNames());
                editor.showEditor();
                if (editor.isOK()) {
                    table.setRow(index, fmd2);
                }
            } else if (evt.getActionCommand() == REMOVE) {
                int index = table.getSelectedRow();
                table.removeRow(index);
                table.clearSelection();
            } else if (evt.getActionCommand() == UP) {
                int index = table.getSelectedRow();
                table.moveRowUp(index);
                table.setRowSelectionInterval(index - 1, index - 1);

            } else if (evt.getActionCommand() == DOWN) {
                int index = table.getSelectedRow();
                table.moveRowDown(index);
                table.setRowSelectionInterval(index + 1, index + 1);
            }
        }
        edited = true;
        deployData.setFilterMappings(table.getFilterMappings());
        scrollP.revalidate();
        parent.fireChangeEvent();
    }

    public void tableChanged(TableModelEvent e) {
        edited = true;
        deployData.setFilterMappings(table.getFilterMappings());
        parent.fireChangeEvent();
    }

    public void valueChanged(ListSelectionEvent e) {
        //Ignore extra messages.
        if (e.getValueIsAdjusting()) {
            return;
        }
        this.setEnabled(deployData.makeEntry() || Utilities.isJavaEE6Plus(wizard));
    }

    @Override
    public void setEnabled(boolean enable) {
        jLtableheader.setEnabled(enable);
        jBnew.setEnabled(enable);

        if (!enable) {
            jBedit.setEnabled(false);
            jBdelete.setEnabled(false);
            jBup.setEnabled(false);
            jBdown.setEnabled(false);
            return;
        }

        ListSelectionModel lsm = table.getSelectionModel();
        if (lsm.isSelectionEmpty()) {
            // disable the relevant buttons
            jBdelete.setEnabled(false);
            jBedit.setEnabled(false);
            jBdown.setEnabled(false);
            jBup.setEnabled(false);
        } else {
            // We only allow single selections
            int selectedRow = lsm.getMinSelectionIndex();
            String str = (String) (table.getValueAt(selectedRow, 0));
            boolean canEdit = str.equals(deployData.getName());
            jBdelete.setEnabled(canEdit);
            jBedit.setEnabled(canEdit);
            int numRows = table.getRowCount();
            if (selectedRow > 0) {
                jBup.setEnabled(true);
            } else {
                jBup.setEnabled(false);
            }
            if (selectedRow < numRows - 1) {
                jBdown.setEnabled(true);
            } else {
                jBdown.setEnabled(false);
            }
        }
    }
}
