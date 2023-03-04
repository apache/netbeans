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

package org.netbeans.modules.profiler.ppoints.ui;

import org.netbeans.modules.profiler.ppoints.ProfilingPointFactory;
import org.netbeans.modules.profiler.ppoints.Utils;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.netbeans.modules.profiler.ppoints.ProfilingPointsManager;
import org.openide.util.Lookup;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "WizardPanel1UI_SelectProjectString=<Select Profiling Point project>",
    "WizardPanel1UI_PpTypeString=Profiling point &type:",
    "WizardPanel1UI_PpProjectString=Profiling point &project:",
    "WizardPanel1UI_DescriptionLabelText=Description:",
    "WizardPanel1UI_SupportedModesLabelText=Supported modes:",
    "WizardPanel1UI_MonitorModeString=Telemetry",
    "WizardPanel1UI_CpuModeString=Methods",
    "WizardPanel1UI_MemoryModeString=Objects",
    "WizardPanel1UI_PpListAccessName=List of available Profiling Points",
    "WizardPanel1UI_ProjectsListAccessName=List of open projects",
    "WizardPanel1UI_SelectProject=<Select Project>"
})
public class WizardPanel1UI extends ValidityAwarePanel implements HelpCtx.Provider {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class PPointTypeTableModel extends AbstractTableModel {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Integer.class;
            } else {
                return String.class;
            }
        } // TODO: enable once Scope is implemented
          //    public Class getColumnClass(int columnIndex) { return String.class; }

        public int getColumnCount() {
            return 2;
        } // TODO: enable once Scope is implemented
          //    public int getColumnCount() { return 1; }

        public int getRowCount() {
            return ppFactories.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return ppFactories[rowIndex];
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String HELP_CTX_KEY = "PPointsWizardPanel1UI.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
    private static final Icon MONITOR_ICON = Icons.getIcon(ProfilerIcons.MONITORING);
    private static final Icon CPU_ICON = Icons.getIcon(ProfilerIcons.CPU);
    private static final Icon MEMORY_ICON = Icons.getIcon(ProfilerIcons.MEMORY);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private AbstractTableModel ppointTypeTableModel;
    private Dimension initialMinSize;
    private ProjectSelector ppointProjectSelector;
    private ProfilerTable ppointTypeTable;
    private JLabel ppointDescriptionCaptionLabel;
    private JLabel ppointEffectiveCPULabel;
    private JLabel ppointEffectiveCaptionLabel;
    private JLabel ppointEffectiveMemoryLabel;
    private JLabel ppointEffectiveMonitorLabel;
    private JLabel ppointProjectLabel;
    private JLabel ppointTypeCaptionLabel;
    private JTextArea ppointDescriptionArea;
    private ProfilingPointFactory[] ppFactories = new ProfilingPointFactory[0];
    
    private boolean hasDefaultScope = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public WizardPanel1UI() {
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    public Dimension getMinSize() {
        return initialMinSize;
    }

    public void setSelectedIndex(int index) {
        if (index == -1) {
            ppointTypeTable.clearSelection();
        } else {
            ppointTypeTable.setRowSelectionInterval(index, index);
        }
    }

    public int getSelectedIndex() {
        return ppointTypeTable.getSelectedRow();
    }

    public void setSelectedProject(Lookup.Provider project) {
        ppointProjectSelector.setProject(project);
    }

    public Lookup.Provider getSelectedProject() {
        return ppointProjectSelector.getProject();
    }
    
    public boolean hasDefaultScope() {
        return hasDefaultScope;
    }

    public void init(final ProfilingPointFactory[] ppFactories) {
        this.ppFactories = ppFactories;
        initProjectsCombo();
        ppointTypeTableModel.fireTableDataChanged();
        ppointTypeTable.getColumnModel().getColumn(0)
                       .setMaxWidth(Math.max(ProfilingPointFactory.SCOPE_CODE_ICON.getIconWidth(),
                                             ProfilingPointFactory.SCOPE_GLOBAL_ICON.getIconWidth()) + 25); // TODO: enable once Scope is implemented

        refresh();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints;

        ppointTypeCaptionLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(ppointTypeCaptionLabel, Bundle.WizardPanel1UI_PpTypeString());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 5, 10);
        add(ppointTypeCaptionLabel, constraints);

        ppointTypeTableModel = new PPointTypeTableModel();
        ppointTypeTable = new ProfilerTable(ppointTypeTableModel, false, false, null);
        ppointTypeTable.getAccessibleContext().setAccessibleName(Bundle.WizardPanel1UI_ProjectsListAccessName());
        ppointTypeCaptionLabel.setLabelFor(ppointTypeTable);
        ppointTypeTable.setMainColumn(1);
        ppointTypeTable.setFitWidthColumn(1);
        ppointTypeTable.setTableHeader(null);
        ppointTypeTable.setRowSelectionAllowed(true);
        ppointTypeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ppointTypeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    refresh();
                }
            });
        ppointTypeTable.setDefaultRenderer(Integer.class, Utils.getScopeRenderer()); // TODO: enable once Scope is implemented
        ppointTypeTable.setDefaultRenderer(String.class, Utils.getPresenterRenderer());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 15, 12, 10);
        add(new ProfilerTableContainer(ppointTypeTable, true, null), constraints);

        ppointProjectLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(ppointProjectLabel, Bundle.WizardPanel1UI_PpProjectString());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 5, 10);
        add(ppointProjectLabel, constraints);

        ppointProjectSelector = new ProjectSelector(Bundle.WizardPanel1UI_SelectProject()) {
            protected void selectionChanged() { refresh(); }
        };
        ppointProjectLabel.getAccessibleContext().setAccessibleName(Bundle.WizardPanel1UI_ProjectsListAccessName());
        ppointProjectLabel.setLabelFor(ppointProjectSelector);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 15, 12, 10);
        add(ppointProjectSelector, constraints);

        ppointDescriptionCaptionLabel = new JLabel(Bundle.WizardPanel1UI_DescriptionLabelText());
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 5, 10);
        add(ppointDescriptionCaptionLabel, constraints);

        ppointDescriptionArea = new JTextArea();
        ppointDescriptionArea.setOpaque(false);
        ppointDescriptionArea.setWrapStyleWord(true);
        ppointDescriptionArea.setLineWrap(true);
        ppointDescriptionArea.setEnabled(false);
        ppointDescriptionArea.setFont(UIManager.getFont("Label.font")); //NOI18N
        ppointDescriptionArea.setDisabledTextColor(UIManager.getColor("Label.foreground")); //NOI18N

        int rows = ppointDescriptionArea.getRows();
        ppointDescriptionArea.setRows(4);

        final int height = ppointDescriptionArea.getPreferredSize().height;
        ppointDescriptionArea.setRows(rows);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 15, 12, 10);

        JScrollPane ppointDescriptionAreaScroll = new JScrollPane(ppointDescriptionArea,
                                                                  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                                  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, height);
            }

            public Dimension getMinimumSize() {
                return new Dimension(super.getMinimumSize().width, height);
            }
        };

        ppointDescriptionAreaScroll.setBorder(BorderFactory.createEmptyBorder());
        ppointDescriptionAreaScroll.setViewportBorder(BorderFactory.createEmptyBorder());
        ppointDescriptionAreaScroll.setOpaque(false);
        ppointDescriptionAreaScroll.getViewport().setOpaque(false);
        add(ppointDescriptionAreaScroll, constraints);

        int maxHeight = ppointDescriptionCaptionLabel.getPreferredSize().height;
        maxHeight = Math.max(maxHeight, MONITOR_ICON.getIconHeight());
        maxHeight = Math.max(maxHeight, CPU_ICON.getIconHeight());
        maxHeight = Math.max(maxHeight, MEMORY_ICON.getIconHeight());

        final int mheight = maxHeight;

        JPanel effectiveModesContainer = new JPanel(new GridBagLayout());

        ppointEffectiveCaptionLabel = new JLabel(Bundle.WizardPanel1UI_SupportedModesLabelText()) {
                public Dimension getPreferredSize() {
                    return new Dimension(super.getPreferredSize().width, mheight);
                }

                public Dimension getMinimumSize() {
                    return new Dimension(super.getMinimumSize().width, mheight);
                }
            };
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 10);
        effectiveModesContainer.add(ppointEffectiveCaptionLabel, constraints);

        ppointEffectiveMonitorLabel = new JLabel(Bundle.WizardPanel1UI_MonitorModeString(), MONITOR_ICON, SwingConstants.LEFT);
        ppointEffectiveMonitorLabel.setVisible(false); // TODO: remove once Monitor mode will support Profiling Points
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 10);
        effectiveModesContainer.add(ppointEffectiveMonitorLabel, constraints);

        ppointEffectiveCPULabel = new JLabel(Bundle.WizardPanel1UI_CpuModeString(), CPU_ICON, SwingConstants.LEFT);
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 10);
        effectiveModesContainer.add(ppointEffectiveCPULabel, constraints);

        ppointEffectiveMemoryLabel = new JLabel(Bundle.WizardPanel1UI_MemoryModeString(), MEMORY_ICON, SwingConstants.LEFT);
        constraints = new GridBagConstraints();
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 10);
        effectiveModesContainer.add(ppointEffectiveMemoryLabel, constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(effectiveModesContainer, constraints);

        initialMinSize = getMinimumSize();
    }

    private void initProjectsCombo() {
        ProfilingPointsManager manager = ProfilingPointsManager.getDefault();
        
        Lookup.Provider defaultScope = null;
        for (Lookup.Provider providedScope : manager.getProvidedScopes()) {
            if (providedScope != null && manager.isDefaultScope(providedScope)) {
                defaultScope = providedScope;
                break;
            }
        }

        hasDefaultScope = defaultScope != null;
        setSelectedProject(hasDefaultScope ? defaultScope : Utils.getCurrentProject());
    }

    private void refresh() {
        int selectedIndex = ppointTypeTable.getSelectedRow();

        if (selectedIndex != -1) {
            ProfilingPointFactory ppFactory = ppFactories[selectedIndex];
            ppointDescriptionArea.setText(ppFactory.getDescription());
            ppointEffectiveMonitorLabel.setVisible(ppFactory.supportsMonitor());
            ppointEffectiveCPULabel.setVisible(ppFactory.supportsCPU());
            ppointEffectiveMemoryLabel.setVisible(ppFactory.supportsMemory());
        } else {
            ppointDescriptionArea.setText(""); // NOI18N
            ppointEffectiveMonitorLabel.setVisible(false);
            ppointEffectiveCPULabel.setVisible(false);
            ppointEffectiveMemoryLabel.setVisible(false);
        }

        boolean ppointTypeSelected = selectedIndex != -1;
        boolean ppointProjectSelected = ppointProjectSelector.getProject() != null;
        boolean isValid = ppointTypeSelected && ppointProjectSelected;

        if (isValid) {
            if (!areSettingsValid()) {
                fireValidityChanged(true);
            }
        } else {
            if (areSettingsValid()) {
                fireValidityChanged(false);
            }
        }
    }
    
}
