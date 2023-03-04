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

package org.netbeans.installer.wizard.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.helper.DetailedStatus;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.helper.swing.NbiDialog;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.NbiTreeTableColumnCellRenderer;
import org.netbeans.installer.utils.helper.swing.NbiTreeTable;
import org.netbeans.installer.utils.helper.swing.NbiTreeTableModel;

public class InstallationDetailsDialog extends NbiDialog {
    private NbiTreeTable   detailsTreeTable;
    private NbiScrollPane detailsScrollPane;
    private static final String TITLE_INSTALLATION_DETAILS_KEY =
            "IDD.installation.details.label";
    private static final String TITLE_COMPONENTS_KEY =
            "IDD.component.label";
    private static final String TITLE_STATUS_KEY =
            "IDD.status.label";
    
    public InstallationDetailsDialog() {
        super();
        
        initComponents();
        initialize();
    }
    
    private void initialize() {
        setTitle(ResourceUtils.getString(
                InstallationDetailsDialog.class,
                TITLE_INSTALLATION_DETAILS_KEY));
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        
        detailsTreeTable = new NbiTreeTable(new InstallationDetailsTreeTableModel());
        detailsTreeTable.setShowVerticalLines(false);
        detailsTreeTable.setOpaque(false);
        detailsTreeTable.setTableHeader(null);
        detailsTreeTable.setRowHeight(detailsTreeTable.getRowHeight() + 4);
        detailsTreeTable.setIntercellSpacing(new Dimension(0, 0));
        detailsTreeTable.setTreeColumnCellRenderer(new InstallationDetailsTreeColumnCellRenderer(detailsTreeTable));
        detailsTreeTable.getColumnModel().getColumn(1).setMaxWidth(200);
        detailsTreeTable.getColumnModel().getColumn(1).setMinWidth(200);
        detailsTreeTable.getColumnModel().getColumn(1).setCellRenderer(new InstallationStatusCellRenderer());
        detailsTreeTable.setRowSelectionAllowed(false);
        detailsTreeTable.setColumnSelectionAllowed(false);
        detailsTreeTable.setCellSelectionEnabled(false);
        
        detailsScrollPane = new NbiScrollPane(detailsTreeTable);
        
        add(detailsScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 11, 11), 0, 0));
    }
    
    private static class InstallationDetailsTreeModel implements TreeModel {
        private List<Product> components = new ArrayList<Product>();
        private Map<Product, List<String>> propertiesMap = new HashMap<Product, List<String>>();
        
        private Object root = new Object();
        
        public InstallationDetailsTreeModel() {
            final Registry registry = Registry.getInstance();
            
            components.addAll(registry.getProducts(DetailedStatus.INSTALLED_SUCCESSFULLY));
            components.addAll(registry.getProducts(DetailedStatus.INSTALLED_WITH_WARNINGS));
            components.addAll(registry.getProducts(DetailedStatus.FAILED_TO_INSTALL));
            
            components.addAll(registry.getProducts(DetailedStatus.UNINSTALLED_SUCCESSFULLY));
            components.addAll(registry.getProducts(DetailedStatus.UNINSTALLED_WITH_WARNINGS));
            components.addAll(registry.getProducts(DetailedStatus.FAILED_TO_UNINSTALL));
        }
        
        public Object getRoot() {
            return root;
        }
        
        public Object getChild(Object parent, int index) {
            if (parent.equals(root)) {
                return components.get(index);
            } else {
                if (parent instanceof Product) {
                    initComponentProperties((Product) parent);
                    return propertiesMap.get(parent).get(index);
                } else {
                    return null;
                }
            }
        }
        
        public int getChildCount(Object parent) {
            if (parent.equals(root)) {
                return components.size();
            }
            
            if (parent instanceof Product) {
                initComponentProperties((Product) parent);
                return propertiesMap.get(parent).size();
            } else {
                return 0;
            }
        }
        
        private void initComponentProperties(Product component) {
            List<String> properties = propertiesMap.get(component);
            if (properties == null) {
                properties = new ArrayList<String>();
                
                switch (component.getDetailedStatus()) {
                    case INSTALLED_WITH_WARNINGS:
                        for (Throwable warning: component.getInstallationWarnings()) {
                            properties.add("<html><b>Warning:</b> " + warning.getMessage());
                        }
                    case INSTALLED_SUCCESSFULLY:
                        properties.add("Installation location: " + component.getInstallationLocation());
                        break;
                    case FAILED_TO_INSTALL:
                        properties.add("<html><b>Error:</b> " + component.getInstallationError().getMessage());
                        break;
                    case UNINSTALLED_WITH_WARNINGS:
                        for (Throwable warning: component.getUninstallationWarnings()) {
                            properties.add("<html><b>Warning:</b> " + warning.getMessage());
                        }
                    case UNINSTALLED_SUCCESSFULLY:
                        break;
                    case FAILED_TO_UNINSTALL:
                        properties.add("<html><b>Error:</b> " + component.getUninstallationError().getMessage());
                        break;
                    default:
                        break;
                }
                
                propertiesMap.put(component, properties);
            }
        }
        
        public boolean isLeaf(Object node) {
            return !((node.equals(root)) || (node instanceof Product));
        }
        
        public void valueForPathChanged(TreePath path, Object newValue) {
            // do nothing we are read-only
        }
        
        public int getIndexOfChild(Object parent, Object child) {
            LogManager.log(ErrorLevel.DEBUG,"getIndexOfChild");
            if (parent.equals(root)) {
                return components.indexOf(child);
            } else {
                String string = (String) child;
                if (string.startsWith("Installation Location: ")) {
                    return 0;
                }
                if (string.startsWith("Disk space:")) {
                    return 1;
                }
                return -1;
            }
        }
        
        public void addTreeModelListener(TreeModelListener listener) {
            // do nothing we are read-only
        }
        
        public void removeTreeModelListener(TreeModelListener listener) {
            // do nothing we are read-only
        }
    }
    
    private static class InstallationDetailsTreeTableModel extends NbiTreeTableModel {
        public InstallationDetailsTreeTableModel() {
            super(new InstallationDetailsTreeModel());
        }
        
        public int getTreeColumnIndex() {
            return 0;
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return ResourceUtils.getString(
                            InstallationDetailsDialog.class,
                            TITLE_COMPONENTS_KEY);
                case 1:
                    return ResourceUtils.getString(
                            InstallationDetailsDialog.class,
                            TITLE_STATUS_KEY);
                default:
                    return null;
            }
        }
        
        public Class<?> getColumnClass(int column) {
            return Object.class;
        }
        
        public boolean isCellEditable(int row, int column) {
            return false;
        }
        
        public Object getValueAt(int row, int column) {
            Object node = getTree().getPathForRow(row).getLastPathComponent();
            
            switch (column) {
                case 0:
                    return node;
                case 1:
                    if (node instanceof Product) {
                        return ((Product) node).getDetailedStatus();
                    } else {
                        return null;
                    }
                default:
                    return null;
            }
        }
        
        public void setValueAt(Object value, int row, int column) {
            // do nothing, we're read-only
        }
    }
    
    public static class InstallationDetailsTreeColumnCellRenderer extends NbiTreeTableColumnCellRenderer {
        private static final EmptyBorder EMPTY_BORDER = new EmptyBorder(0, 0, 0, 0);
        private static final EmptyBorder PADDED_BORDER = new EmptyBorder(0, 0, 0, 5);
        
        public InstallationDetailsTreeColumnCellRenderer(final NbiTreeTable treeTable) {
            super(treeTable);
        }
        
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {
            setOpaque(false);
            setForeground(treeTable.getForeground());
            setBackground(treeTable.getBackground());
            
            if (value instanceof Product) {
                Product component = (Product) value;
                
                setIcon(component.getIcon());
                setText(component.getDisplayName());
                
                setBorder(EMPTY_BORDER);
            } else {
                setIcon(null);
                setText((value != null) ? value.toString() : "");
                
                setBorder(PADDED_BORDER);
            }
            
            return this;
        }
    }
    
    public static class InstallationStatusCellRenderer extends JLabel
            implements TableCellRenderer {
        public InstallationStatusCellRenderer() {
            setBorder(new EmptyBorder(0, 5, 0, 5));
        }
        
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            setOpaque(false);
            setBackground(table.getBackground());
            setForeground(table.getForeground());
            setText((value instanceof DetailedStatus) ? value.toString() : "");
            
            return this;
        }
    }
}
