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
package org.netbeans.modules.web.inspect.webkit.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.netbeans.modules.web.inspect.PageInspectorImpl;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.ui.FakeRootNode;
import org.netbeans.modules.web.inspect.webkit.Utilities;
import org.netbeans.modules.web.inspect.webkit.WebKitPageModel;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Document section of CSS Styles view.
 *
 * @author Jan Stola
 */
public class CSSStylesDocumentPanel extends JPanel implements ExplorerManager.Provider, Lookup.Provider {
    /** Request processor used by this class. */
    static final RequestProcessor RP = new RequestProcessor(CSSStylesDocumentPanel.class);
    /** Tree view showing the style sheet information. */
    private BeanTreeView treeView;
    /** Explorer manager provided by this panel. */
    private final ExplorerManager manager = new ExplorerManager();
    /** Lookup of this panel. */
    @org.netbeans.api.annotations.common.SuppressWarnings(value="SE_TRANSIENT_FIELD_NOT_RESTORED", justification="The instances are never serialized.") // NOI18N
    private final transient Lookup lookup = ExplorerUtils.createLookup(getExplorerManager(), getActionMap());
    /** Filter for the tree displayed in this panel. */
    private final transient Filter filter = new Filter();

    /**
     * Creates a new {@code CSSStylesDocumentPanel}.
     */
    CSSStylesDocumentPanel() {
        setLayout(new BorderLayout());
        initTreeView();
        initFilter();
        updateContent(null, true);
    }

    /**
     * Initializes the tree view.
     */
    private void initTreeView() {
        treeView = new BeanTreeView() {
            {
                MouseAdapter listener = createTreeMouseListener();
                tree.addMouseListener(listener);
                tree.addMouseMotionListener(listener);
                tree.setCellRenderer(createTreeCellRenderer(tree.getCellRenderer()));
            }

            @Override
            public void expandAll() {
                // The original expandAll() doesn't work for us as it doesn't
                // seem to wait for the calculation of sub-nodes.
                Node root = manager.getRootContext();
                expandAll(root);
                // The view attempts to scroll to the expanded node
                // and it does it with a delay. Hence, simple calls like
                // tree.scrollRowToVisible(0) have no effect (are overriden
                // later) => the dummy collapse and expansion attempts
                // to work around that and keep the root node visible.
                collapseNode(root);
                expandNode(root);
            }
            /**
             * Expands the whole sub-tree under the specified node.
             *
             * @param node root node of the sub-tree that should be expanded.
             */
            private void expandAll(Node node) {
                treeView.expandNode(node);
                for (Node subNode : node.getChildren().getNodes(true)) {
                    if (!subNode.isLeaf()) {
                        expandAll(subNode);
                    }
                }
            }
        };
        treeView.setAllowedDragActions(DnDConstants.ACTION_NONE);
        treeView.setAllowedDropActions(DnDConstants.ACTION_NONE);
        treeView.setRootVisible(false);
        add(treeView, BorderLayout.CENTER);
    }

    /**
     * Initializes the filter section of this panel.
     */
    private void initFilter() {
        JPanel panel = new JPanel();
        Color background = treeView.getViewport().getView().getBackground();
        panel.setBackground(background);

        // "Find" label
        JLabel label = new JLabel(ImageUtilities.loadImageIcon(
                "org/netbeans/modules/web/inspect/resources/find.png", true)); // NOI18N
        label.setVerticalAlignment(SwingConstants.CENTER);

        // Pattern text field
        final JTextField field = new JTextField();
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                filter.setPattern(field.getText());
            }
        });

        // Clear pattern button
        JButton button = new JButton(ImageUtilities.loadImageIcon(
                "org/netbeans/modules/web/inspect/resources/cancel.png", true)); // NOI18N
        button.setBackground(background);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0,0,0,0));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                field.setText(""); // NOI18N
            }
        });

        // Layout
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGap(2)
            .addComponent(label)
            .addComponent(field)
            .addComponent(button)
            .addGap(2));
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(label)
                .addComponent(field)
                .addComponent(button)));
        add(panel, BorderLayout.PAGE_START);
    }

    /**
     * Updates the content of this panel.
     *
     * @param webKit WebKit debugging.
     * @param keepSelection if {@code true} then an attempt to keep the current
     * selection is made, otherwise the selection is cleared.
     */
    final void updateContent(final WebKitPageModel pageModel, final boolean keepSelection) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                final Node root;
                if (pageModel == null) {
                    // Using dummy node as the root to release the old root
                    root = new AbstractNode(Children.LEAF);
                } else {
                    filter.removePropertyChangeListeners();
                    DocumentNode documentNode = new DocumentNode(pageModel, filter);
                    root = new FakeRootNode<DocumentNode>(documentNode,
                            new Action[] { new RefreshAction() });
                }
                final Node[] oldSelection = manager.getSelectedNodes();
                manager.setRootContext(root);
                treeView.expandAll();
                if (keepSelection) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            List<Node> selection = new ArrayList<Node>(oldSelection.length);
                            for (Node oldSelected : oldSelection) {
                                Rule rule = oldSelected.getLookup().lookup(Rule.class);
                                if (rule != null) {
                                    Node newSelected = Utilities.findRule(root, rule);
                                    if (newSelected != null) {
                                        selection.add(newSelected);
                                    }
                                }
                            }
                            try {
                                manager.setSelectedNodes(selection.toArray(new Node[0]));
                            } catch (PropertyVetoException pvex) {
                                Logger.getLogger(CSSStylesDocumentPanel.class.getName()).log(Level.FINEST, null, pvex);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public final ExplorerManager getExplorerManager() {
        return manager;
    }


    // The last node we were hovering over.
    Object lastHover = null;

    /**
     * Creates a mouse listener for the tree view.
     *
     * @return mouse listener for the tree view.
     */
    public MouseAdapter createTreeMouseListener() {
        return new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                processEvent(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                processEvent(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                processEvent(null);
                // Make sure that lastHover != <any potential value>
                // i.e., make sure that change in hover is triggered when
                // mouse returns into this component
                lastHover = new Object();
            }

            /**
             * Processes the specified mouse event.
             *
             * @param e mouse event to process.
             */
            private void processEvent(MouseEvent e) {
                Object hover = null;
                if (e != null) {
                    JTree tree = (JTree)e.getSource();
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        hover = path.getLastPathComponent();
                    }
                }
                if (hover != lastHover) {
                    lastHover = hover;
                    final String selector;
                    if (hover != null) {
                        Node node = Visualizer.findNode(hover);
                        Rule rule = node.getLookup().lookup(Rule.class);
                        if (rule != null) {
                            selector = rule.getSelector();
                        } else {
                            selector = null;
                        }
                    } else {
                        selector = null;
                    }
                    treeView.repaint();
                    final PageModel pageModel = currentPageModel();
                    if (pageModel != null) {
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                pageModel.setHighlightedSelector(selector);
                            }
                        });
                    }
                }
            }
        };
    }

    /**
     * Returns the page model that corresponds to the content of the tree view.
     *
     * @return page model that corresponds to the content of the tree view.
     */
    private PageModel currentPageModel() {
        Node node = manager.getRootContext();
        if (node instanceof FakeRootNode) {
            node = ((FakeRootNode)node).getRealRoot();
        }
        return node.getLookup().lookup(PageModel.class);
    }

    /**
     * Creates a cell renderer for the tree view.
     *
     * @param delegate delegating/original tree renderer.
     * @return call renderer for the tree view.
     */
    private TreeCellRenderer createTreeCellRenderer(final TreeCellRenderer delegate) {
        Color origColor = UIManager.getColor("Tree.selectionBackground"); // NOI18N
        Color color = origColor.brighter().brighter();
        if (color.equals(Color.WHITE)) { // Issue 217127
            color = origColor.darker();
        }
        // Color used for hovering highlight
        final Color hoverColor = color;
        return new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel component;
                if (!selected && (value == lastHover)) {
                    component = (JLabel)delegate.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, hasFocus);
                    component.setBackground(hoverColor);
                    component.setOpaque(true);
                } else {
                    component = (JLabel)delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                }
                return component;
            }
        };
    }

    /**
     * Action that refreshes the content of the document section of CSS Styles view.
     */
    private class RefreshAction extends AbstractAction {

        private RefreshAction() {
            String name = NbBundle.getMessage(RefreshAction.class,
                    "CSSStylesDocumentPanel.RefreshAction.displayName"); // NOI18N
            putValue(Action.NAME, name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            PageModel pageModel = PageInspectorImpl.getDefault().getPage();
            updateContent(pageModel instanceof WebKitPageModel ? (WebKitPageModel)pageModel : null, false);
        }

    }

}
