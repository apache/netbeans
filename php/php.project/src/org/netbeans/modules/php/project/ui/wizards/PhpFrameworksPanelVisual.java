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

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * List of frameworks is "copied" from web project.
 * @author Tomas Mysik
 */
public class PhpFrameworksPanelVisual extends JPanel implements HelpCtx.Provider, TableModelListener, ListSelectionListener, ChangeListener {
    private static final int STEP_INDEX = 2;
    private static final long serialVersionUID = 158602680330133653L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final FrameworksTableModel model;
    private final Map<PhpFrameworkProvider, PhpModuleExtender> extenders;

    private PhpModuleExtender actualExtender;

    public PhpFrameworksPanelVisual(PhpFrameworksPanel wizardPanel, Map<PhpFrameworkProvider, PhpModuleExtender> extenders) {
        assert extenders != null;
        this.extenders = extenders;

        // Provide a name in the title bar.
        setName(wizardPanel.getSteps()[STEP_INDEX]);
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, STEP_INDEX);
        // Step name (actually the whole list for reference).
        putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, wizardPanel.getSteps());

        initComponents();

        // frameworks
        model = new FrameworksTableModel();
        frameworksTable.setModel(model);
        // #214843
        frameworksTable.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none"); // NOI18N

        createFrameworksList();

        FrameworksTableCellRenderer renderer = new FrameworksTableCellRenderer(model);
        renderer.setBooleanRenderer(frameworksTable.getDefaultRenderer(Boolean.class));
        frameworksTable.setDefaultRenderer(PhpFrameworkProvider.class, renderer);
        frameworksTable.setDefaultRenderer(Boolean.class, renderer);
        initTableVisualProperties();

        changeDescriptionAndPanel();
    }

    public void addPhpFrameworksListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removePhpFrameworksListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public Map<PhpFrameworkProvider, PhpModuleExtender> getSelectedExtenders() {
        Map<PhpFrameworkProvider, PhpModuleExtender> selectedExtenders = new LinkedHashMap<>();
        for (int i = 0; i < model.getRowCount(); ++i) {
            FrameworkModelItem item = model.getItem(i);
            if (item.isSelected()) {
                PhpFrameworkProvider framework = item.getFramework();
                assert framework != null;
                PhpModuleExtender extender = extenders.get(framework);
                selectedExtenders.put(framework, extender);
            }
        }

        return selectedExtenders;
    }

    public PhpModuleExtender getSelectedVisibleExtender() {
        int selectedRow = frameworksTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        FrameworkModelItem item = model.getItem(selectedRow);
        assert item != null;
        if (item.isSelected()) {
            return extenders.get(item.getFramework());
        }
        return null;
    }

    public void markInvalidFrameworks(Set<PhpFrameworkProvider> invalidFrameworks) {
        for (int i = 0; i < model.getRowCount(); ++i) {
            FrameworkModelItem item = model.getItem(i);
            item.setValid(!invalidFrameworks.contains(item.getFramework()));
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        for (Component component : configPanel.getComponents()) {
            if (component instanceof HelpCtx.Provider) {
                HelpCtx helpCtx = ((HelpCtx.Provider) component).getHelpCtx();
                if (helpCtx != null) {
                    return helpCtx;
                }
            }
        }
        return null;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        changeDescriptionAndPanel();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        changeDescriptionAndPanel();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }

    private void fireChange() {
        changeSupport.fireChange();
    }

    private void createFrameworksList() {
        for (PhpFrameworkProvider provider : extenders.keySet()) {
            model.addItem(new FrameworkModelItem(provider));
        }
    }

    private void initTableVisualProperties() {
        frameworksTable.getModel().addTableModelListener(this);
        frameworksTable.getSelectionModel().addListSelectionListener(this);

        frameworksTable.setRowHeight(frameworksTable.getRowHeight() + 4);
        frameworksTable.setIntercellSpacing(new Dimension(0, 0));
        // set the color of the table's JViewport
        frameworksTable.getParent().setBackground(frameworksTable.getBackground());
        frameworksTable.getColumnModel().getColumn(0).setMaxWidth(30);
    }

    private void changeDescriptionAndPanel() {
        if (actualExtender != null) {
            actualExtender.removeChangeListener(this);
        }
        if (frameworksTable.getSelectedRow() == -1) {
            descriptionLabel.setText(" "); // NOI18N
            configPanel.removeAll();
            configPanel.repaint();
            configPanel.revalidate();
        } else {
            FrameworkModelItem item = model.getItem(frameworksTable.getSelectedRow());
            descriptionLabel.setText(item.getFramework().getDescription());
            descriptionLabel.setEnabled(item.isSelected());

            configPanel.removeAll();
            actualExtender = extenders.get(item.getFramework());
            actualExtender.addChangeListener(this);
            JComponent component = actualExtender.getComponent();
            if (component != null) {
                configPanel.add(component, BorderLayout.CENTER);
                enableComponents(component, item.isSelected());
            }
            configPanel.revalidate();
            configPanel.repaint();
        }
        fireChange();
    }

    private void enableComponents(Container root, boolean enabled) {
        root.setEnabled(enabled);
        for (Component child : root.getComponents()) {
            if (child instanceof Container) {
                enableComponents((Container) child, enabled);
            } else {
                child.setEnabled(enabled);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        frameworksScrollPane = new JScrollPane();
        frameworksTable = new JTable();
        descriptionLabel = new JLabel();
        separator = new JSeparator();
        configPanel = new JPanel();

        setFocusTraversalPolicy(null);

        frameworksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        frameworksTable.setShowHorizontalLines(false);
        frameworksTable.setShowVerticalLines(false);
        frameworksTable.setTableHeader(null);
        frameworksScrollPane.setViewportView(frameworksTable);
        frameworksTable.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpFrameworksPanelVisual.class, "PhpFrameworksPanelVisual.frameworksTable.AccessibleContext.accessibleName")); // NOI18N
        frameworksTable.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpFrameworksPanelVisual.class, "PhpFrameworksPanelVisual.frameworksTable.AccessibleContext.accessibleDescription")); // NOI18N

        descriptionLabel.setText("DUMMY"); // NOI18N

        configPanel.setLayout(new BorderLayout());

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(separator, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(descriptionLabel)
                .addContainerGap())
            .addComponent(configPanel, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
            .addComponent(frameworksScrollPane, GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(frameworksScrollPane, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(separator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(descriptionLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(configPanel, GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE))
        );

        frameworksScrollPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpFrameworksPanelVisual.class, "PhpFrameworksPanelVisual.frameworksScrollPane.AccessibleContext.accessibleName")); // NOI18N
        frameworksScrollPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpFrameworksPanelVisual.class, "PhpFrameworksPanelVisual.frameworksScrollPane.AccessibleContext.accessibleDescription")); // NOI18N
        descriptionLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpFrameworksPanelVisual.class, "PhpFrameworksPanelVisual.descriptionLabel.AccessibleContext.accessibleName")); // NOI18N
        configPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpFrameworksPanelVisual.class, "PhpFrameworksPanelVisual.configPanel.AccessibleContext.accessibleName")); // NOI18N
        configPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpFrameworksPanelVisual.class, "PhpFrameworksPanelVisual.configPanel.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpFrameworksPanelVisual.class, "PhpFrameworksPanelVisual.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpFrameworksPanelVisual.class, "PhpFrameworksPanelVisual.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel configPanel;
    private JLabel descriptionLabel;
    private JScrollPane frameworksScrollPane;
    private JTable frameworksTable;
    private JSeparator separator;
    // End of variables declaration//GEN-END:variables

    private static final class FrameworksTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 22495101047716943L;
        private static final Color ERROR_COLOR = UIManager.getColor("nb.errorForeground"); // NOI18N
        private static final Color NORMAL_COLOR = new JLabel().getForeground();

        private final FrameworksTableModel model;

        private TableCellRenderer booleanRenderer;

        private FrameworksTableCellRenderer(FrameworksTableModel model) {
            this.model = model;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof PhpFrameworkProvider) {
                FrameworkModelItem item = model.getItem(row);

                Component defaultRenderer = super.getTableCellRendererComponent(table, item.getFramework().getName(), isSelected, false, row, column);
                if (item.isValid()) {
                    defaultRenderer.setForeground(NORMAL_COLOR);
                } else {
                    defaultRenderer.setForeground(ERROR_COLOR);
                }
                return defaultRenderer;
            } else if (value instanceof Boolean && booleanRenderer != null) {
                return booleanRenderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        }

        public void setBooleanRenderer(TableCellRenderer booleanRenderer) {
            this.booleanRenderer = booleanRenderer;
        }
    }

    private static final class FrameworksTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 8082636013224696L;

        private final DefaultListModel<FrameworkModelItem> model;

        public FrameworksTableModel() {
            model = new DefaultListModel<>();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return model.size();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Boolean.class;
                case 1:
                    return PhpFrameworkProvider.class;
                default:
                    assert false : "Unknown column index: " + columnIndex;
                    break;
            }
            return super.getColumnClass(columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Object getValueAt(int row, int column) {
            FrameworkModelItem item = getItem(row);
            switch (column) {
                case 0:
                    return item.isSelected();
                case 1:
                    return item.getFramework();
                default:
                    assert false : "Unknown column index: " + column;
                    break;
            }
            return "";
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            FrameworkModelItem item = getItem(row);
            switch (column) {
                case 0:
                    item.setSelected((Boolean) value);
                    break;
                case 1:
                    // nothing needed
                    break;
                default:
                    assert false : "Unknown column index: " + column;
                    break;
            }
            fireTableCellUpdated(row, column);
        }

        FrameworkModelItem getItem(int index) {
            return model.get(index);
        }

        void addItem(FrameworkModelItem item) {
            model.addElement(item);
        }
    }

    private static final class FrameworkModelItem {
        private final PhpFrameworkProvider framework;

        private Boolean selected;
        private boolean valid = true;

        public FrameworkModelItem(PhpFrameworkProvider framework) {
            assert framework != null;

            this.framework = framework;
            setSelected(Boolean.FALSE);
        }

        public PhpFrameworkProvider getFramework() {
            return framework;
        }

        public Boolean isSelected() {
            return selected;
        }

        public void setSelected(Boolean selected) {
            this.selected = selected;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }
    }
}
