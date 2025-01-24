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
package org.netbeans.modules.profiler.v2.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import org.netbeans.lib.profiler.ui.swing.FilteringToolbar;
import org.netbeans.lib.profiler.ui.swing.ProfilerPopup;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer;
import org.netbeans.lib.profiler.ui.swing.renderer.CheckBoxRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.LabelRenderer;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ProjectsSelector_selectProjects=Select projects:",
    "ProjectsSelector_filterProjects=Filter projects",
    "ProjectsSelector_columnSelected=Selected",
    "ProjectsSelector_columnProject=Project",
    "ProjectsSelector_columnSelectedToolTip=Selected for profiling",
    "ProjectsSelector_columnProjectToolTip=Project name"
})
public abstract class ProjectsSelector {
    
    private final Collection<Lookup.Provider> selected;
    
    public ProjectsSelector(Collection<Lookup.Provider> selected) {
        this.selected = new HashSet(selected);
    }
    
    
    public void show(Component invoker) {
        UI ui = new UI(selected);
        ui.show(invoker);
    }
    
    
    protected abstract void selectionChanged(Collection<Lookup.Provider> selected);
    
    
    private class UI {
        
        private JPanel panel;
        
        UI(Collection<Lookup.Provider> selected) {
            populatePopup();
        }
        
        void show(Component invoker) {
            int resizeMode = ProfilerPopup.RESIZE_BOTTOM | ProfilerPopup.RESIZE_RIGHT;
            ProfilerPopup.createRelative(invoker, panel, SwingConstants.SOUTH_WEST, resizeMode).show();
        }
        
        private void populatePopup() {
            JPanel content = new JPanel(new BorderLayout());
            
            JLabel hint = new JLabel(Bundle.ProjectsSelector_selectProjects(), JLabel.LEADING);
            hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
            content.add(hint, BorderLayout.NORTH);

            final SelectedProjectsModel projectsModel = new SelectedProjectsModel();
            final ProfilerTable projectsTable = new ProfilerTable(projectsModel, true, false, null);
            projectsTable.setColumnToolTips(new String[] {
                Bundle.ProjectsSelector_columnSelectedToolTip(),
                Bundle.ProjectsSelector_columnProjectToolTip() });
            projectsTable.setMainColumn(1);
            projectsTable.setFitWidthColumn(1);
            projectsTable.setDefaultSortOrder(1, SortOrder.ASCENDING);
            projectsTable.setSortColumn(1);
            projectsTable.setFixedColumnSelection(0); // #268298 - make sure SPACE always hits the Boolean column
            projectsTable.setColumnRenderer(0, new CheckBoxRenderer());
            LabelRenderer projectRenderer = new ProjectRenderer();
            projectsTable.setColumnRenderer(1, projectRenderer);
            int w = new JLabel(projectsTable.getColumnName(0)).getPreferredSize().width;
            projectsTable.setDefaultColumnWidth(0, w + 15);
            int h = projectsTable.getRowHeight() * 8;
            h += projectsTable.getTableHeader().getPreferredSize().height;
            projectRenderer.setText("A longest expected project name A longest expected project name"); // NOI18N
            Dimension prefSize = new Dimension(w + projectRenderer.getPreferredSize().width, h);
            projectsTable.setPreferredScrollableViewportSize(prefSize);
            ProfilerTableContainer tableContainer = new ProfilerTableContainer(projectsTable, true, null);
            JPanel tableContent = new JPanel(new BorderLayout());
            tableContent.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
            tableContent.add(tableContainer, BorderLayout.CENTER);
            content.add(tableContent, BorderLayout.CENTER);

            JToolBar controls = new FilteringToolbar(Bundle.ProjectsSelector_filterProjects()) {
                protected void filterChanged() {
                    if (isAll()) projectsTable.setRowFilter(null);
                    else projectsTable.setRowFilter(new RowFilter() {
                        public boolean include(RowFilter.Entry entry) {
                            return passes(entry.getStringValue(1));
                        }
                    });
                }
            };

            content.add(controls, BorderLayout.SOUTH);

            panel = content;
        }
        
        private class SelectedProjectsModel extends AbstractTableModel {
            
            Lookup.Provider[] projects = ProjectUtilities.getOpenedProjects(); 
            
            SelectedProjectsModel() {
                ProjectUtilities.getOpenedProjects();
            }
        
            public String getColumnName(int columnIndex) {
                if (columnIndex == 0) {
                    return Bundle.ProjectsSelector_columnSelected();
                } else if (columnIndex == 1) {
                    return Bundle.ProjectsSelector_columnProject();
                }
                return null;
            }

            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                } else if (columnIndex == 1) {
                    return Lookup.Provider.class;
                }
                return null;
            }

            public int getRowCount() {
                return projects.length;
            }

            public int getColumnCount() {
                return 2;
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                if (columnIndex == 0) {
                    return selected.contains(projects[rowIndex]);
                } else if (columnIndex == 1) {
                    return projects[rowIndex];
                }
                return null;
            }

            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                if (Boolean.TRUE.equals(aValue)) {
                    if (selected.add(projects[rowIndex])) selectionChanged(selected);
                } else if (selected.size() > 1) {
                    if (selected.remove(projects[rowIndex])) selectionChanged(selected);
                }
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 0;
            }

        }
        
    }
    
    private static class ProjectRenderer extends LabelRenderer {
        
        private final Font font;
        private final Lookup.Provider main;
        

        public ProjectRenderer() {
            font = getFont();
            main = ProjectUtilities.getMainProject();
        }

        public void setValue(Object value, int row) {
            if (value == null) {
                setText(""); // NOI18N
                setIcon(null);
            } else {
                Lookup.Provider project = (Lookup.Provider)value;
                setText(ProjectUtilities.getDisplayName(project));
                Icon icon = ProjectUtilities.getIcon(project);
                setIcon(isEnabled() ? icon : ImageUtilities.createDisabledIcon(icon));
                setFont(Objects.equals(main, value) ? font.deriveFont(Font.BOLD) : font);
            }
        }
    }
    
}
