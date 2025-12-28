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

package org.netbeans.modules.projectimport.eclipse.core.wizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.modules.projectimport.eclipse.core.EclipseProject;
import org.netbeans.modules.projectimport.eclipse.core.ProjectImporterException;
import org.netbeans.modules.projectimport.eclipse.core.Workspace;
import org.netbeans.modules.projectimport.eclipse.core.WorkspaceFactory;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;

/**
 * Represent "Project to import" step(panel) in the Eclipse importer wizard.
 *
 * @author mkrauskopf
 */
public final class ProjectSelectionPanel extends JPanel {
    
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(ProjectSelectionPanel.class.getName());

    private ProjectWizardPanel wizard;
    
    private class ProjectCheckboxEditorAndRenderer extends AbstractCellEditor
            implements TableCellEditor, TableCellRenderer {

        private JCheckBox checkbox;
        
        public Object getCellEditorValue() {
            return Boolean.valueOf(checkbox.isSelected());
        }

        private JCheckBox createComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            EclipseProject project = projects[row];
            JCheckBox chb = new JCheckBox();
            chb.setSelected(selectedProjects.contains(project) ||
                    requiredProjects.contains(project));
            chb.setToolTipText(null);
            if (project.isImportSupported() && !requiredProjects.contains(project)) {
                chb.setEnabled(true);
            } else {
                // required and non-java project are disabled
                chb.setEnabled(false);
                if (!project.isImportSupported()) {
                    chb.setToolTipText(ProjectImporterWizard.getMessage(
                            "MSG_NonJavaProject", project.getName())); // NOI18N
                }
            }
            if (isSelected) {
                chb.setOpaque(true);
                chb.setForeground(table.getSelectionForeground());
                chb.setBackground(table.getSelectionBackground());
            } else {
                chb.setOpaque(false);
                chb.setForeground(table.getForeground());
                chb.setBackground(table.getBackground());
            }
            return chb;
        }
        
        public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
            checkbox = createComponent(table, value, isSelected, isSelected, row, column);
            checkbox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    fireEditingStopped();
                }
            });
            return checkbox;
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            checkbox = createComponent(table, value, isSelected, isSelected, row, column);
            return checkbox;
        }
        
    }
    
    /** Renderer for projects */
    private class ProjectNameRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel) {
                JLabel label = (JLabel)c;
                label.setOpaque(isSelected);
                EclipseProject project = projects[row];
                if (project.isImportSupported()) {
                    label.setIcon(project.getProjectTypeFactory().getProjectTypeIcon());
                } else {
                    label.setIcon(null);
                }
            }
            return c;
        }
        
    }
    
    /** All projects in a workspace. */
    private EclipseProject[] projects;
    
    /**
     * Projects selected by user. So it counts the projects which were selected
     * by user and then became required (so became disabled). But project which
     * weren't checked but are required are not members of this set.
     * This all servers for remembering checked project when working with
     * project dependencies.
     */
    private Set<EclipseProject> selectedProjects;
    
    /**
     * All projects we need to import (involving projects which selected
     * projects depend on.
     */
    private Set<EclipseProject> requiredProjects;
    
    private class ProjectTableModel extends AbstractTableModel {
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            EclipseProject project = projects[rowIndex];
            if (columnIndex == 0) {
                return Boolean.valueOf(selectedProjects.contains(project) ||
                    requiredProjects.contains(project));
            } else {
                if (project.isImportSupported()) {
                    return project.getName() + " ("+project.getProjectTypeFactory().getProjectTypeName()+")"; // NOI18N
                } else {
                    return project.getName() + " (unknown project type)"; // NOI18N
                }
            }
        }
        
        public int getRowCount() {
            return projects != null ? projects.length : 0;
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        @Override
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == 0 && projects[rowIndex].isImportSupported() &&
                    !requiredProjects.contains(projects[rowIndex]));
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            EclipseProject project = projects[rowIndex];
            assert projects != null;
            if (((Boolean) aValue).booleanValue()) {
                selectedProjects.add(project);
            } else {
                selectedProjects.remove(project);
            }
            solveDependencies();
            fireColumnDataChanged();
            updateValidity();
            wizard.fireProjectListChanged();
        }
        
        private void fireColumnDataChanged() {
            for (int i=0; i<getRowCount(); i++) {
                fireTableCellUpdated(i, 0);
            }
        }
    
    }
    
    /** Updates panel validity. */
    public void updateValidity() {
        if (selectedProjects == null || selectedProjects.isEmpty()) {
            // user has to select at least one project
            wizard.setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_ProjectIsNotChosed")); // NOI18N
            return;
        }
        boolean exists = false;
        if (jRadioInsideEclipse.isSelected()) {
            for (EclipseProject prj : allProjects()) {
                if (new File(prj.getDirectory(), "nbproject").exists()) { // NOI18N
                    exists = true;
                    break;
                }
            }
        } else {
            if (destination.getText().length() == 0) {
                wizard.setErrorMessage(ProjectImporterWizard.getMessage(
                        "MSG_DestinationIsEmpty")); // NOI18N
                return;
            }
            File f = new File(destination.getText());
            if (f.exists()) {
                for (EclipseProject prj : allProjects()) {
                    if (new File(f, prj.getDirectory().getName()).exists()) {
                        exists = true;
                        break;
                    }
                }
            }
        }
        if (exists) {
            wizard.setErrorMessage(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "MSG_AlreadyImportedProjects"), true);
        } else {
            wizard.setErrorMessage(null);
        }
    }
    
    public boolean isSeparateFolder() {
        return jRadioSeparate.isSelected();
    }
    
    /** Returns both selected and required projects */
    private Collection<EclipseProject> allProjects() {
        Collection<EclipseProject> all = new HashSet<EclipseProject>(selectedProjects);
        all.addAll(requiredProjects);
        return all;
    }
    
    /**
     * Solves project dependencies. Fills up <code>requiredProjects</code> as
     * needed.
     */
    private void solveDependencies() {
        requiredProjects.clear();
        requiredProjects.addAll(getFlattenedRequiredProjects(selectedProjects));
    }
    
    public static Set<EclipseProject> getFlattenedRequiredProjects(Set<EclipseProject> selectedProjects) {
        EclipseProject currentRoot;
        Stack<EclipseProject> solved = new Stack<EclipseProject>();
        Set<EclipseProject> requiredProjects = new HashSet<EclipseProject>();
        if (selectedProjects == null || selectedProjects.isEmpty()) {
            return requiredProjects;
        }
        for (EclipseProject selProject : selectedProjects) {
            assert selProject != null;
            solved.push(selProject);
            currentRoot = selProject;
            fillUpRequiredProjects(selProject, solved, requiredProjects);
            EclipseProject poped = solved.pop();
            assert poped.equals(currentRoot);
            assert solved.isEmpty();
            currentRoot = null;
        }
        return requiredProjects;
    }
    
    private static void fillUpRequiredProjects(EclipseProject project, Stack<EclipseProject> solved, Set<EclipseProject> requiredProjects) {
        for (EclipseProject child : project.getProjects()) {
            assert child != null;
            if (solved.contains(child)) {
                recursionDetected(child, solved);
                return;
            }
            requiredProjects.add(child);
            solved.push(child);
            fillUpRequiredProjects(child, solved, requiredProjects);
            EclipseProject popped = solved.pop();
            assert popped.equals(child);
        }
    }
    
    private static void recursionDetected(EclipseProject start, Stack<EclipseProject> solved) {
        int where = solved.search(start);
        assert where != -1 : "Cannot find start of the cycle."; // NOI18N
        EclipseProject rootOfCycle = solved.get(solved.size() - where);
        StringBuffer cycle = new StringBuffer();
        for (EclipseProject p : solved) {
            cycle.append(p.getName()).append(" --> "); // NOI18N
        }
        cycle.append(rootOfCycle.getName()).append(" --> ..."); // NOI18N
        logger.warning("Cycle dependencies was detected. Detected cycle: " + cycle); // NOI18N
        NotifyDescriptor d = new DialogDescriptor.Message(
                ProjectImporterWizard.getMessage("MSG_CycleDependencies", cycle.toString()), // NOI18N
                NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }
    
    /** Creates new form ProjectSelectionPanel */
    ProjectSelectionPanel(ProjectWizardPanel wizard) {
        this.wizard = wizard;
        initComponents();
        init();
        destination.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateValidity(); }
            public void removeUpdate(DocumentEvent e) { updateValidity(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        updateValidity();
        jRadioInsideEclipse.setSelected(true);
        enableLocation(false);
        TableColumn column = projectTable.getColumnModel().getColumn(0);
        column.setMaxWidth(25);
        column.setMinWidth(25);
    }
    
    private void init() {
        projectTable.setModel(new ProjectTableModel());
        projectTable.setTableHeader(null);
        projectTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectTable.getColumnModel().getColumn(0).setCellRenderer(new ProjectCheckboxEditorAndRenderer());
        projectTable.getColumnModel().getColumn(1).setCellRenderer(new ProjectNameRenderer());
        projectTable.setDefaultEditor(Boolean.class, new ProjectCheckboxEditorAndRenderer());
        projectTableSP.getViewport().setBackground(projectTable.getBackground());
        destination.setText(ProjectChooser.getProjectsFolder().getPath()); // NOI18N
    }
    
    /** Loads project from workspace in the given <code>workspaceDir</code>. */
    void loadProjects(File workspaceDir) {
        WorkspaceFactory.getInstance().resetCache();
        Workspace workspace = null;
        try {
            workspace = WorkspaceFactory.getInstance().load(workspaceDir);
        } catch (ProjectImporterException e) {
            wizard.setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_WorkspaceIsInvalid", workspaceDir)); // NOI18N
            logger.log(Level.FINE, "ProjectImporterException catched", e); // NOI18N
            return;
        }
        Set<EclipseProject> wsPrjs = new TreeSet<EclipseProject>(workspace.getProjects());
        projects = wsPrjs.toArray(new EclipseProject[0]);
        selectedProjects = new HashSet<EclipseProject>();
        requiredProjects = new HashSet<EclipseProject>();
        if (projects.length == 0) {
            wizard.setErrorMessage(ProjectImporterWizard.getMessage(
                    "MSG_WorkspaceIsEmpty", workspaceDir)); // NOI18N
        } else {
            updateValidity();
        }
    }
    
    /** Returns projects selected by selection panel and ordered so that required 
     *  projects are created first.
     */
    List<EclipseProject> getProjects() {
        return getFlattenedProjects(selectedProjects);
    }
    
    /** Returns projects ordered so that required projects are listed first.
     */
    public static List<EclipseProject> getFlattenedProjects(Set<EclipseProject> selectedProjects) {
        List<EclipseProject> list = new ArrayList<EclipseProject>();
        addProjects(selectedProjects, list);
        Iterator<EclipseProject> it = list.iterator();
        while (it.hasNext()) {
            EclipseProject eclipseProject = it.next();
            if (!eclipseProject.isImportSupported()) {
                it.remove();
            }
        }
        return list;
    }
    
    private static void addProjects(Set<EclipseProject> projects, List<EclipseProject> list) {
        for (EclipseProject p : projects) {
            if (list.contains(p)) {
                continue;
            }
            Set<EclipseProject> requiredProjs = p.getProjects();
            if (requiredProjs.size() == 0) {
                list.add(p);
            } else {
                addProjects(requiredProjs, list);
                list.add(p);
            }
        }
    }
    
    /**
     * Returns number of projects which will be imported (including both
     * required and selected projects)
     */
    int getNumberOfImportedProject() {
        return allProjects().size();
    }
    
    /**
     * Returns destination directory where new NetBeans projects will be stored.
     * Will return null if NetBeans projects should be created into the same folder
     * as Eclipse projects.
     */
    String getDestination() {
        if (isSeparateFolder()) {
            return destination.getText();
        } else {
            return null;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        projectListLabel = new javax.swing.JLabel();
        projectTableSP = new javax.swing.JScrollPane();
        projectTable = new javax.swing.JTable();
        prjLocationLBL = new javax.swing.JLabel();
        destination = new javax.swing.JTextField();
        chooseDestButton = new javax.swing.JButton();
        jRadioInsideEclipse = new javax.swing.JRadioButton();
        jRadioSeparate = new javax.swing.JRadioButton();
        prjLocationLBL1 = new javax.swing.JLabel();

        projectListLabel.setLabelFor(projectTable);
        org.openide.awt.Mnemonics.setLocalizedText(projectListLabel, org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "LBL_ProjectsToImport")); // NOI18N
        projectListLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        projectTable.setOpaque(false);
        projectTable.setShowHorizontalLines(false);
        projectTable.setShowVerticalLines(false);
        projectTableSP.setViewportView(projectTable);
        projectTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "ACSD_ProjectSelectionPanel_NA")); // NOI18N

        prjLocationLBL.setLabelFor(jRadioInsideEclipse);
        org.openide.awt.Mnemonics.setLocalizedText(prjLocationLBL, org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "LBL_LocationOfNBProjects")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chooseDestButton, org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "CTL_BrowseButton_B")); // NOI18N
        chooseDestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseDestButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioInsideEclipse);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioInsideEclipse, org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "RADIO_LOCATION_ECLIPSE")); // NOI18N
        jRadioInsideEclipse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioInsideEclipseActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioSeparate);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioSeparate, org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "RADIO_LOCATION_SEPARATE")); // NOI18N
        jRadioSeparate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioSeparateActionPerformed(evt);
            }
        });

        prjLocationLBL1.setLabelFor(destination);
        org.openide.awt.Mnemonics.setLocalizedText(prjLocationLBL1, org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "LBL_LocationOfNBProjects2")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(prjLocationLBL)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(prjLocationLBL1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(destination, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chooseDestButton))
            .addComponent(projectTableSP, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioInsideEclipse, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioSeparate, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(projectListLabel)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(projectListLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(projectTableSP, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(prjLocationLBL)
                .addGap(0, 0, 0)
                .addComponent(jRadioInsideEclipse)
                .addGap(0, 0, 0)
                .addComponent(jRadioSeparate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prjLocationLBL1)
                    .addComponent(chooseDestButton)
                    .addComponent(destination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        projectListLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "ACSD_ProjectSelectionPanel_NA")); // NOI18N
        projectTableSP.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "ACSD_ProjectSelectionPanel_NA")); // NOI18N
        projectTableSP.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "ACSD_ProjectSelectionPanel_NA")); // NOI18N
        prjLocationLBL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "ACSD_ProjectSelectionPanel_NA")); // NOI18N
        destination.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "ACSD_ProjectSelectionPanel_NA")); // NOI18N
        chooseDestButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "ACSD_ProjectSelectionPanel_NA")); // NOI18N
        jRadioInsideEclipse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "ACSD_ProjectSelectionPanel_NA")); // NOI18N
        jRadioSeparate.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "ACSD_ProjectSelectionPanel_NA")); // NOI18N
        prjLocationLBL1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "ACSD_ProjectSelectionPanel_NA")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "ACSD_ProjectSelectionPanel_NA")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectSelectionPanel.class, "ACSD_ProjectSelectionPanel_NA")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void chooseDestButtonActionPerformed(java.awt.event.ActionEvent evt) {
        File dest = new FileChooserBuilder(ProjectSelectionPanel.class).setDirectoriesOnly(true).showOpenDialog();
        if (dest != null) {
            destination.setText (dest.getAbsolutePath());
        }
    }                                                     

private void jRadioInsideEclipseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioInsideEclipseActionPerformed
    enableLocation(false);
}//GEN-LAST:event_jRadioInsideEclipseActionPerformed

private void jRadioSeparateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioSeparateActionPerformed
    enableLocation(true);
}//GEN-LAST:event_jRadioSeparateActionPerformed

    private void enableLocation(boolean enable) {
        prjLocationLBL1.setEnabled(enable);
        destination.setEnabled(enable);
        chooseDestButton.setEnabled(enable);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton chooseDestButton;
    private javax.swing.JTextField destination;
    private javax.swing.JRadioButton jRadioInsideEclipse;
    private javax.swing.JRadioButton jRadioSeparate;
    private javax.swing.JLabel prjLocationLBL;
    private javax.swing.JLabel prjLocationLBL1;
    private javax.swing.JLabel projectListLabel;
    private javax.swing.JTable projectTable;
    private javax.swing.JScrollPane projectTableSP;
    // End of variables declaration//GEN-END:variables
}
