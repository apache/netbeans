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

package org.netbeans.installer.wizard.components.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.RegistryType;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.dependencies.Conflict;
import org.netbeans.installer.product.dependencies.Requirement;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.utils.helper.swing.NbiTree;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Kirill Sorokin
 */
public class ComponentsSelectionPanel extends ErrorMessagePanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ComponentsSelectionPanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        setProperty(DESCRIPTION_INSTALL_PROPERTY,
                DEFAULT_DESCRIPTION_INSTALL);
        setProperty(DESCRIPTION_UNINSTALL_PROPERTY,
                DEFAULT_DESCRIPTION_UNINSTALL);
        setProperty(FEATURE_DESCRIPTION_TITLE_PROPERTY,
                DEFAULT_FEATURE_DESCRIPTION_TITLE);
        
        setProperty(COMPONENT_DESCRIPTION_TEXT_PROPERTY,
                DEFAULT_COMPONENT_DESCRIPTION_TEXT);
        setProperty(COMPONENT_DESCRIPTION_CONTENT_TYPE_PROPERTY,
                DEFAULT_COMPONENT_DESCRIPTION_CONTENT_TYPE);
        setProperty(SIZES_LABEL_TEXT_PROPERTY,
                DEFAULT_SIZES_LABEL_TEXT);
        setProperty(SIZES_LABEL_TEXT_NO_DOWNLOAD_PROPERTY,
                DEFAULT_SIZES_LABEL_TEXT_NO_DOWNLOAD);
        
        setProperty(DEFAULT_INSTALLATION_SIZE_PROPERTY,
                DEFAULT_INSTALLATION_SIZE);
        setProperty(DEFAULT_DOWNLOAD_SIZE_PROPERTY,
                DEFAULT_DOWNLOAD_SIZE);
        
        setProperty(ERROR_NO_CHANGES_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES);
        setProperty(ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES_INSTALL_ONLY);
        setProperty(ERROR_NO_CHANGES_UNINSTALL_ONLY_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES_UNINSTALL_ONLY);
        setProperty(ERROR_REQUIREMENT_INSTALL_PROPERTY,
                DEFAULT_ERROR_REQUIREMENT_INSTALL);
        setProperty(ERROR_CONFLICT_INSTALL_PROPERTY,
                DEFAULT_ERROR_CONFLICT_INSTALL);
        setProperty(ERROR_REQUIREMENT_UNINSTALL_PROPERTY,
                DEFAULT_ERROR_REQUIREMENT_UNINSTALL);
        setProperty(ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD_PROPERTY,
                DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD);
        setProperty(ERROR_NO_ENOUGH_SPACE_TO_EXTRACT_PROPERTY,
                DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_EXTRACT);
        setProperty(ERROR_CANNOT_CHECK_SPACE_PROPERTY,
                DEFAULT_ERROR_CANNOT_CHECK_SPACE);
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new ComponentsSelectionPanelUi(this);
        }
        
        return wizardUi;
    }
    
    @Override
    public boolean canExecuteForward() {
        return canExecute();
    }
    
    @Override
    public boolean canExecuteBackward() {
        return canExecute();
    }
    
    @Override
    public void initialize() {
        if (!isThereAnythingVisibleToInstall()) {
            setProperty(
                    DESCRIPTION_PROPERTY,
                    getProperty(DESCRIPTION_UNINSTALL_PROPERTY));
        }
        if (!isThereAnythingVisibleToUninstall()) {
            setProperty(
                    DESCRIPTION_PROPERTY,
                    getProperty(DESCRIPTION_INSTALL_PROPERTY));
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private boolean canExecute() {
        return !(Boolean.getBoolean(Registry.FORCE_INSTALL_PROPERTY) ||
                Boolean.getBoolean(Registry.FORCE_UNINSTALL_PROPERTY));
    }
    
    private boolean isThereAnythingVisibleToInstall() {
        final Registry registry = Registry.getInstance();
        
        final List<Product> toInstall = new LinkedList<Product>();
        toInstall.addAll(registry.getProducts(Status.NOT_INSTALLED));
        toInstall.addAll(registry.getProducts(Status.TO_BE_INSTALLED));
        
        for (Product product: toInstall) {
            if (product.isVisible()) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isThereAnythingVisibleToUninstall() {
        final Registry registry = Registry.getInstance();
        
        final List<Product> toUninstall = new LinkedList<Product>();
        toUninstall.addAll(registry.getProducts(Status.INSTALLED));
        toUninstall.addAll(registry.getProducts(Status.TO_BE_UNINSTALLED));
        
        for (Product product: toUninstall) {
            if (product.isVisible()) {
                return true;
            }
        }
        
        return false;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class ComponentsSelectionPanelUi extends ErrorMessagePanelUi {
        private ComponentsSelectionPanel panel;
        
        public ComponentsSelectionPanelUi(final ComponentsSelectionPanel panel) {
            super(panel);
            
            this.panel = panel;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new ComponentsSelectionPanelSwingUi(panel, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class ComponentsSelectionPanelSwingUi extends ErrorMessagePanelSwingUi {
        /////////////////////////////////////////////////////////////////////////////
        // Instance
        private ComponentsSelectionPanel panel;
        
        private NbiTree       componentsTree;
        private NbiScrollPane componentsScrollPane;
        
        private NbiTextPane   descriptionPane;
        private NbiScrollPane descriptionScrollPane;
        
        private NbiLabel      sizesLabel;
        
        public ComponentsSelectionPanelSwingUi(
                final ComponentsSelectionPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.panel = component;
            
            initComponents();
        }
        
        @Override
        protected void initialize() {
            descriptionPane.setContentType(
                    panel.getProperty(COMPONENT_DESCRIPTION_CONTENT_TYPE_PROPERTY));
            
            if (!panel.isThereAnythingVisibleToInstall()) {
                sizesLabel.setVisible(false);
            }
            
            updateDescription();
            updateSizes();
            
            super.initialize();
        }
        
        @Override
        protected String validateInput() {
            final Registry registry = Registry.getInstance();
            
            final List<Product> toInstall   =
                    registry.getProducts(Status.TO_BE_INSTALLED);
            final List<Product> toUninstall =
                    registry.getProducts(Status.TO_BE_UNINSTALLED);
            
            if ((toInstall.size() == 0) && (toUninstall.size() == 0)) {
                if (!panel.isThereAnythingVisibleToInstall()) {
                    return panel.getProperty(
                            ERROR_NO_CHANGES_UNINSTALL_ONLY_PROPERTY);
                }
                
                if (!panel.isThereAnythingVisibleToUninstall()) {
                    return panel.getProperty(
                            ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY);
                }
                
                return panel.getProperty(ERROR_NO_CHANGES_PROPERTY);
            }
            
            for (Product product: toInstall) {
                for (Dependency requirement: product.getDependencies(
                        Requirement.class)) {
                    final List<Product> requirees =
                            registry.getProducts(requirement);
                    
                    boolean satisfied = false;
                    
                    for (Product requiree: requirees) {
                        if ((requiree.getStatus() == Status.TO_BE_INSTALLED) ||
                                (requiree.getStatus() == Status.INSTALLED)) {
                            satisfied = true;
                            break;
                        }
                    }
                    
                    if (!satisfied) {
                        return StringUtils.format(
                                panel.getProperty(ERROR_REQUIREMENT_INSTALL_PROPERTY),
                                product.getDisplayName(),
                                requirees.get(0).getDisplayName());
                    }
                }
                
                for (Dependency conflict: product.getDependencies(
                        Conflict.class)) {
                    final List<Product> conflictees = registry.getProducts(conflict);
                    
                    boolean satisfied = true;
                    Product unsatisfiedConflict = null;
                    
                    for (Product conflictee: conflictees) {
                        if ((conflictee.getStatus() == Status.TO_BE_INSTALLED) ||
                                (conflictee.getStatus() == Status.INSTALLED)) {
                            satisfied = false;
                            unsatisfiedConflict = conflictee;
                            break;
                        }
                    }
                    
                    if (!satisfied) {
                        return StringUtils.format(
                                panel.getProperty(ERROR_CONFLICT_INSTALL_PROPERTY),
                                product.getDisplayName(),
                                unsatisfiedConflict.getDisplayName());
                    }
                }
            }
            
            for (Product product: toUninstall) {
                for (Product dependent: registry.getProducts()) {
                    if ((dependent.getStatus() == Status.TO_BE_UNINSTALLED) ||
                            (dependent.getStatus() == Status.NOT_INSTALLED)) {
                        continue;
                    }
                    
                    for (Dependency requirement: dependent.getDependencies(Requirement.class)) {
                        final List<Product> requirees = registry.getProducts(requirement);
                        
                        if (requirees.contains(product)) {
                            boolean satisfied = false;
                            for (Product requiree: requirees) {
                                if (requiree.getStatus() == Status.INSTALLED) {
                                    satisfied = true;
                                    break;
                                }
                            }
                            
                            if (!satisfied) {
                                return StringUtils.format(
                                        panel.getProperty(ERROR_REQUIREMENT_UNINSTALL_PROPERTY),
                                        product.getDisplayName(),
                                        dependent.getDisplayName());
                            }
                        }
                    }
                }
            }
            
            // devise the error message template which should be used - "not enough
            // space to download..." is at least one of the components comes from
            // a remote registry, "not enough space to extract..." - otherwise
            String template = panel.getProperty(
                    ERROR_NO_ENOUGH_SPACE_TO_EXTRACT_PROPERTY);
            for (Product product: toInstall) {
                if (product.getRegistryType() == RegistryType.REMOTE) {
                    template = panel.getProperty(
                            ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD_PROPERTY);
                    break;
                }
            }
            
            // check whether the space available in the local directory (which will
            // be used for downloading data) is enough to keep all the installation
            // data and configuration logic (plus some margin for safety)
            try {
                if (!Boolean.getBoolean(SystemUtils.NO_SPACE_CHECK_PROPERTY)) {
                    
                    final File localDirectory = Installer.getInstance().getLocalDirectory();
                    final long availableSize = SystemUtils.getFreeSpace(localDirectory);

                    long requiredSize = 0;
                    for (Product product : toInstall) {
                        requiredSize += product.getDownloadSize();
                    }
                    requiredSize += REQUIRED_SPACE_ADDITION;

                    if (availableSize < requiredSize) {
                        return StringUtils.format(
                                template,
                                localDirectory,
                                StringUtils.formatSize(requiredSize - availableSize));
                    }
                }
            } catch (NativeException e) {
                ErrorManager.notifyError(
                        panel.getProperty(ERROR_CANNOT_CHECK_SPACE_PROPERTY),
                        e);
            }
            
            return null;
        }
        
        @Override
        public JComponent getDefaultFocusOwner() {
            return componentsTree;
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // componentsTree ///////////////////////////////////////////////////////
            componentsTree = new NbiTree();
            componentsTree.setModel(
                    new ComponentsTreeModel());
            componentsTree.setCellRenderer(
                    new ComponentsTreeCell());
            componentsTree.setEditable(
                    true);
            componentsTree.setCellEditor(
                    new ComponentsTreeCell());
            componentsTree.setShowsRootHandles(
                    true);
            componentsTree.setRootVisible(
                    false);
            componentsTree.setBorder(
                    new EmptyBorder(5, 5, 5, 5));
            componentsTree.setRowHeight(
                    new JCheckBox().getPreferredSize().height);
            componentsTree.getSelectionModel().setSelectionMode(
                    TreeSelectionModel.SINGLE_TREE_SELECTION);
            componentsTree.getSelectionModel().addTreeSelectionListener(
                    new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent event) {
                    updateDescription();
                }
            });
            componentsTree.getModel().addTreeModelListener(
                    new TreeModelListener() {
                public void treeNodesChanged(TreeModelEvent event) {
                    updateSizes();
                    updateErrorMessage();
                }
                public void treeNodesInserted(TreeModelEvent event) {
                    // does nothing
                }
                public void treeNodesRemoved(TreeModelEvent event) {
                    // does nothing
                }
                public void treeStructureChanged(TreeModelEvent event) {
                    // does nothing
                }
            });
            componentsTree.getActionMap().put(
                    "checkbox.update",
                    new AbstractAction("checkbox.update") {
                public void actionPerformed(ActionEvent event) {
                    final TreePath path = componentsTree.getSelectionPath();
                    
                    if (path != null) {
                        componentsTree.getModel().valueForPathChanged(path, null);
                    }
                }
            });
            componentsTree.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false),
                    "checkbox.update");
            
            // componentsScrollPane /////////////////////////////////////////////////
            componentsScrollPane = new NbiScrollPane(componentsTree);
            
            // descriptionPane //////////////////////////////////////////////////////
            descriptionPane = new NbiTextPane();
            descriptionPane.setBorder(
                    new EmptyBorder(5, 5, 5, 5));
            
            // descriptionScrollPane ////////////////////////////////////////////////
            descriptionScrollPane = new NbiScrollPane(descriptionPane);
            descriptionScrollPane.setVerticalScrollBarPolicy(
                    NbiScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            descriptionScrollPane.setBorder(
                    new TitledBorder(panel.getProperty(FEATURE_DESCRIPTION_TITLE_PROPERTY)));
            
            // sizesLabel ///////////////////////////////////////////////////////////
            sizesLabel = new NbiLabel();
            //sizesLabel.setFocusable(true);
            
            // this /////////////////////////////////////////////////////////////////
            add(componentsScrollPane, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    0.7, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 0),         // padding
                    0, 0));                           // padx, pady - ???
            add(descriptionScrollPane, new GridBagConstraints(
                    1, 0,                             // x, y
                    1, 1,                             // width, height
                    0.3, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 6, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(sizesLabel, new GridBagConstraints(
                    0, 1,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(6, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            
            // run through all nodes and expand those that have the expand flag set
            // to true
            for (RegistryNode node: Registry.getInstance().getNodes()) {
                if (node.getExpand()) {
                    componentsTree.expandPath(node.getTreePath());
                }
            }
            
            // l&f-specific tweaks
            if (UIManager.getLookAndFeel().getID().equals("GTK")) {
                descriptionPane.setOpaque(true);
            }
        }
        
        private void updateDescription() {
            final TreePath path = componentsTree.getSelectionPath();
            
            if (path != null) {
                final RegistryNode node = (RegistryNode) path.getLastPathComponent();
                descriptionPane.setText(node.getDescription());
            } else {
                descriptionPane.clearText();
            }
            
            descriptionPane.setCaretPosition(0);
        }
        
        private void updateSizes() {
            final Registry registry = Registry.getInstance();
            
            long installationSize = 0;
            long downloadSize     = 0;
            for (Product product: registry.getProductsToInstall()) {
                installationSize += product.getRequiredDiskSpace();
                downloadSize += product.getDownloadSize();
            }
            
            String template = panel.getProperty(SIZES_LABEL_TEXT_NO_DOWNLOAD_PROPERTY);
            for (RegistryNode remoteNode: registry.getNodes(RegistryType.REMOTE)) {
                if (remoteNode.isVisible()) {
                    template = panel.getProperty(
                            SIZES_LABEL_TEXT_PROPERTY);
                }
            }
            
            if (installationSize == 0) {
                sizesLabel.setText(StringUtils.format(
                        template,
                        panel.getProperty(DEFAULT_INSTALLATION_SIZE_PROPERTY),
                        panel.getProperty(DEFAULT_DOWNLOAD_SIZE_PROPERTY)));
            } else {
                sizesLabel.setText(StringUtils.format(
                        template,
                        StringUtils.formatSize(installationSize),
                        StringUtils.formatSize(downloadSize)));
            }
        }
    }
    
    public static class ComponentsTreeModel implements TreeModel {
        private List<TreeModelListener> listeners =
                new LinkedList<TreeModelListener>();
        
        public Object getRoot() {
            return Registry.getInstance().getRegistryRoot();
        }
        
        public Object getChild(final Object node, final int index) {
            return ((RegistryNode) node).getVisibleChildren().get(index);
        }
        
        public int getChildCount(final Object node) {
            return ((RegistryNode) node).getVisibleChildren().size();
        }
        
        public boolean isLeaf(final Object node) {
            return ((RegistryNode) node).getVisibleChildren().size() == 0;
        }
        
        public void valueForPathChanged(final TreePath path, final Object value) {
            if (path.getLastPathComponent() instanceof Product) {
                final Product product = (Product) path.getLastPathComponent();
                
                switch (product.getStatus()) {
                case NOT_INSTALLED:
                    product.setStatus(Status.TO_BE_INSTALLED);
                    updateRequirements(product);
                    break;
                case TO_BE_INSTALLED:
                    product.setStatus(Status.NOT_INSTALLED);
                    break;
                case INSTALLED:
                    product.setStatus(Status.TO_BE_UNINSTALLED);
                    break;
                case TO_BE_UNINSTALLED:
                    product.setStatus(Status.INSTALLED);
                    break;
                }
            }
            
            final TreeModelListener[] clone;
            synchronized (listeners) {
                clone = listeners.toArray(new TreeModelListener[0]);
            }
            
            final TreeModelEvent event = new TreeModelEvent(this, path);
            for (TreeModelListener listener: clone) {
                listener.treeNodesChanged(event);
            }
        }
        
        public int getIndexOfChild(final Object node, final Object child) {
            return ((RegistryNode) node).getVisibleChildren().indexOf(child);
        }
        
        public void addTreeModelListener(final TreeModelListener listener) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
        
        public void removeTreeModelListener(final TreeModelListener listener) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void updateRequirements(final Product product) {
            // check whether the requirements are satisfied and if there
            // is an unsatisfied requirement - pick an appropriate
            // product and select it
            for (Dependency requirement: product.getDependencies(
                    Requirement.class)) {
                final List<Product> requirees =
                        Registry.getInstance().getProducts(requirement);
                
                boolean requireesNeedUpdate = true;
                Product correctRequiree = null;
                for (Product requiree: requirees) {
                    final Version version = requiree.getVersion();
                    
                    if ((requiree.getStatus() == Status.INSTALLED) ||
                            (requiree.getStatus() == Status.TO_BE_INSTALLED)) {
                        requireesNeedUpdate = false;
                        break;
                    }
                    if ((correctRequiree == null) ||
                            correctRequiree.getVersion().olderThan(version)) {
                        correctRequiree = requiree;
                    }
                }
                
                if (requireesNeedUpdate && !requirees.isEmpty()) {
                    valueForPathChanged(correctRequiree.getTreePath(), null);
                }
            }
        }
    }
    
    public static class ComponentsTreeCell implements TreeCellRenderer, TreeCellEditor {
        private List<CellEditorListener> listeners =
                new LinkedList<CellEditorListener>();
        
        private NbiPanel panel;
        private NbiCheckBox checkBox;
        private NbiLabel iconLabel;
        private NbiLabel titleLabel;
        private NbiLabel statusLabel;
        
        private Color foreground;
        private Color background;
        
        private Color selectionForeground;
        private Color selectionBackground;
        
        public ComponentsTreeCell() {
            foreground = UIManager.getColor("Tree.textForeground");
            background = UIManager.getColor("Tree.textBackground");
            
            selectionForeground = UIManager.getColor("Tree.selectionForeground");
            selectionBackground = UIManager.getColor("Tree.selectionBackground");
            
            initComponents();
        }
        
        public Component getTreeCellRendererComponent(
                final JTree tree,
                final Object value,
                final boolean selected,
                final boolean expanded,
                final boolean leaf,
                final int row,
                final boolean focus) {
            return getComponent(tree, value, selected, expanded, leaf, row, focus);
        }
        
        public Component getTreeCellEditorComponent(
                final JTree tree,
                final Object value,
                final boolean selected,
                final boolean expanded,
                final boolean leaf,
                final int row) {
            return getComponent(tree, value, selected, expanded, leaf, row, true);
        }
        
        public Object getCellEditorValue() {
            return null;
        }
        
        public boolean isCellEditable(final EventObject event) {
            return true;
        }
        
        public boolean shouldSelectCell(final EventObject event) {
            return true;
        }
        
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }
        
        public void cancelCellEditing() {
            fireEditingCanceled();
        }
        
        public void addCellEditorListener(final CellEditorListener listener) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
        
        public void removeCellEditorListener(final CellEditorListener listener) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // panel ////////////////////////////////////////////////////////////////
            panel = new NbiPanel();
            panel.setLayout(new GridBagLayout());
            panel.setOpaque(false);
            
            // checkBox /////////////////////////////////////////////////////////////
            checkBox = new NbiCheckBox();
            checkBox.setOpaque(false);
            checkBox.setFocusable(false);
            checkBox.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent event) {
                    stopCellEditing();
                }
            });
            
            // iconLabel ////////////////////////////////////////////////////////////
            iconLabel = new NbiLabel();
            iconLabel.setFocusable(false);
            iconLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(final MouseEvent event) {
                    cancelCellEditing();
                }
                
                public void mousePressed(final MouseEvent event) {
                    cancelCellEditing();
                }
                
                public void mouseReleased(final MouseEvent event) {
                    cancelCellEditing();
                }
            });
            
            // titleLabel ///////////////////////////////////////////////////////////
            titleLabel = new NbiLabel();
            titleLabel.setFocusable(false);
            titleLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(final MouseEvent event) {
                    cancelCellEditing();
                }
                
                public void mousePressed(final MouseEvent event) {
                    cancelCellEditing();
                }
                
                public void mouseReleased(final MouseEvent event) {
                    cancelCellEditing();
                }
            });
            
            // statusLabel //////////////////////////////////////////////////////////
            statusLabel = new NbiLabel();
            statusLabel.setForeground(Color.GRAY);
            statusLabel.setFocusable(false);
            statusLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(final MouseEvent event) {
                    cancelCellEditing();
                }
                
                public void mousePressed(final MouseEvent event) {
                    cancelCellEditing();
                }
                
                public void mouseReleased(final MouseEvent event) {
                    cancelCellEditing();
                }
            });
            
            // this /////////////////////////////////////////////////////////////////
            panel.add(checkBox, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???);
            panel.add(iconLabel, new GridBagConstraints(
                    1, 0,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???);
            panel.add(titleLabel, new GridBagConstraints(
                    2, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???);
            panel.add(statusLabel, new GridBagConstraints(
                    3, 0,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???);
        }
        
        private void fireEditingStopped() {
            final CellEditorListener[] clone;
            synchronized (listeners) {
                clone = listeners.toArray(new CellEditorListener[0]);
            }
            
            final ChangeEvent event = new ChangeEvent(this);
            for (CellEditorListener listener: clone) {
                listener.editingStopped(event);
            }
        }
        
        private void fireEditingCanceled() {
            final CellEditorListener[] clone;
            synchronized (listeners) {
                clone = listeners.toArray(new CellEditorListener[0]);
            }
            
            final ChangeEvent event = new ChangeEvent(this);
            for (CellEditorListener listener: clone) {
                listener.editingCanceled(event);
            }
        }
        
        private JComponent getComponent(
                final JTree tree,
                final Object value,
                final boolean selected,
                final boolean expanded,
                final boolean leaf,
                final int row,
                final boolean focus) {
            if (selected) {
                titleLabel.setOpaque(true);
                
                titleLabel.setForeground(selectionForeground);
                titleLabel.setBackground(selectionBackground);
            } else {
                titleLabel.setOpaque(false);
                
                titleLabel.setForeground(foreground);
                titleLabel.setBackground(background);
            }
            
            if (value instanceof Product) {
                final Product product = (Product) value;
                
                final String title =
                        " " + product.getDisplayName() + " ";
                final String status =
                        " [" + product.getStatus().getDisplayName() + "]";
                final String tooltip = title + status;
                
                iconLabel.setIcon(product.getIcon());
                iconLabel.setToolTipText(tooltip);
                
                titleLabel.setText(title);
                titleLabel.setToolTipText(tooltip);
                
                statusLabel.setText(status);
                statusLabel.setToolTipText(tooltip);
                
                checkBox.setVisible(true);
                checkBox.setToolTipText(tooltip);
                
                if ((product.getStatus() == Status.INSTALLED) ||
                        (product.getStatus() == Status.TO_BE_INSTALLED)) {
                    checkBox.setSelected(true);
                } else {
                    checkBox.setSelected(false);
                }
            } else if (value instanceof Group) {
                final Group group = (Group) value;
                
                final String title =
                        " " + group.getDisplayName() + " ";
                final String status =
                        "";
                final String tooltip = title + status;
                
                iconLabel.setIcon(group.getIcon());
                iconLabel.setToolTipText(tooltip);
                
                titleLabel.setText(title);
                titleLabel.setToolTipText(tooltip);
                
                statusLabel.setText(status);
                statusLabel.setToolTipText(tooltip);
                
                checkBox.setVisible(false);
            }
            
            // l&f-specific tweaks
            if (UIManager.getLookAndFeel().getID().equals("GTK")) {
                panel.setOpaque(false);
            }
            
            return panel;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DESCRIPTION_INSTALL_PROPERTY =
            "description.install"; // NOI18N
    public static final String DESCRIPTION_UNINSTALL_PROPERTY =
            "description.uninstall"; // NOI18N
    public static final String FEATURE_DESCRIPTION_TITLE_PROPERTY =
            "feature.description.title";
    
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.description.both"); // NOI18N
    public static final String DEFAULT_DESCRIPTION_INSTALL =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.description.install"); // NOI18N
    public static final String DEFAULT_DESCRIPTION_UNINSTALL =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.description.uninstall"); // NOI18N
    public static final String DEFAULT_FEATURE_DESCRIPTION_TITLE =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.feature.description.title");// NOI18N
    public static final String COMPONENT_DESCRIPTION_TEXT_PROPERTY =
            "component.description.text"; // NOI18N
    public static final String COMPONENT_DESCRIPTION_CONTENT_TYPE_PROPERTY =
            "component.description.content.type"; // NOI18N
    public static final String SIZES_LABEL_TEXT_PROPERTY =
            "sizes.label.text"; // NOI18N
    public static final String SIZES_LABEL_TEXT_NO_DOWNLOAD_PROPERTY =
            "sizes.label.text.no.download"; // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE_PROPERTY =
            "default.installation.size"; // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE_PROPERTY =
            "default.download.size"; // NOI18N
    
    public static final String DEFAULT_COMPONENT_DESCRIPTION_TEXT =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.component.description.text"); // NOI18N
    public static final String DEFAULT_COMPONENT_DESCRIPTION_CONTENT_TYPE =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.component.description.content.type"); // NOI18N
    public static final String DEFAULT_SIZES_LABEL_TEXT =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.sizes.label.text"); // NOI18N
    public static final String DEFAULT_SIZES_LABEL_TEXT_NO_DOWNLOAD =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.sizes.label.text.no.download"); // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.default.installation.size"); // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.default.download.size"); // NOI18N
    
    public static final String ERROR_NO_CHANGES_PROPERTY =
            "error.no.changes.both"; // NOI18N
    public static final String ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY =
            "error.no.changes.install"; // NOI18N
    public static final String ERROR_NO_CHANGES_UNINSTALL_ONLY_PROPERTY =
            "error.no.changes.uninstall"; // NOI18N
    public static final String ERROR_REQUIREMENT_INSTALL_PROPERTY =
            "error.requirement.install"; // NOI18N
    public static final String ERROR_CONFLICT_INSTALL_PROPERTY =
            "error.conflict.install"; // NOI18N
    public static final String ERROR_REQUIREMENT_UNINSTALL_PROPERTY =
            "error.requirement.uninstall"; // NOI18N
    public static final String ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD_PROPERTY =
            "error.not.enough.space.to.download"; // NOI18N
    public static final String ERROR_NO_ENOUGH_SPACE_TO_EXTRACT_PROPERTY =
            "error.not.enough.space.to.extract"; // NOI18N
    public static final String ERROR_CANNOT_CHECK_SPACE_PROPERTY =
            "error.cannot.check.space"; // NOI18N
    
    public static final String DEFAULT_ERROR_NO_CHANGES =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.no.changes.both"); // NOI18N
    public static final String DEFAULT_ERROR_NO_CHANGES_INSTALL_ONLY =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.no.changes.install"); // NOI18N
    public static final String DEFAULT_ERROR_NO_CHANGES_UNINSTALL_ONLY =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.no.changes.uninstall"); // NOI18N
    public static final String DEFAULT_ERROR_REQUIREMENT_INSTALL =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.requirement.install"); // NOI18N
    public static final String DEFAULT_ERROR_CONFLICT_INSTALL =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.conflict.install"); // NOI18N
    public static final String DEFAULT_ERROR_REQUIREMENT_UNINSTALL =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.requirement.uninstall"); // NOI18N
    public static final String DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.not.enough.space.to.download"); // NOI18N
    public static final String DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_EXTRACT =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.not.enough.space.to.extract"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_CHECK_SPACE =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.cannot.check.space"); // NOI18N
    
    public static final long REQUIRED_SPACE_ADDITION =
            10L * 1024L * 1024L; // 10MB
}
