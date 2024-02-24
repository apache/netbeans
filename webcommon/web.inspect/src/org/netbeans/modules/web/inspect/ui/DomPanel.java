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
package org.netbeans.modules.web.inspect.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.WebBrowsers;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.webkit.ui.CustomToolbar;
import org.openide.explorer.ExplorerManager;
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
 * Panel that displays DOM tree of the inspected page.
 *
 * @author Jan Stola
 */
public class DomPanel extends JPanel implements ExplorerManager.Provider {
    /** Request processor used for interaction with {@code PageModel}. */
    private static final RequestProcessor RP = new RequestProcessor(DomPanel.class.getName(), 5);
    /** Explorer manager provided by this panel. */
    private final ExplorerManager manager = new ExplorerManager();
    /** Tree view that displays the DOM tree. */
    private BeanTreeView treeView;
    /** Label used when no DOM tree is available. */
    private JLabel noDomLabel;
    /** Label showing URL of the page. */
    private JLabel urlLabel; 
    /** Header. */
    private JPanel headerPanel;
    /** Select mode button. */
    private AbstractButton selectModeButton;
    /** Page model used by this panel. */
    private final PageModel pageModel;
    /** Determines whether we are just updating view from the model. */
    private boolean updatingView = false;

    /**
     * Creates a new {@code DomPanel}.
     * 
     * @param pageModel page model for the panel (can be {@code null}).
     */
    public DomPanel(PageModel pageModel) {
        this.pageModel = pageModel;
        setLayout(new BorderLayout());
        initTreeView();
        initNoDOMLabel();
        initHeader();
        add(noDomLabel);
        add(headerPanel, BorderLayout.PAGE_START);
        if (pageModel != null) {
            pageModel.addPropertyChangeListener(createModelListener());
            manager.addPropertyChangeListener(createSelectedNodesListener());
            update();
        }
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
        };
        treeView.setAllowedDragActions(DnDConstants.ACTION_NONE);
        treeView.setAllowedDropActions(DnDConstants.ACTION_NONE);
        treeView.setRootVisible(false);
    }

    /**
     * Creates a mouse listener for the DOM tree.
     * 
     * @return mouse listener for the DOM tree.
     */
    private MouseAdapter createTreeMouseListener() {
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

            // The last node we were hovering over.
            private Object lastHover = null;
            
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
                    if  (path != null) {
                        hover = path.getLastPathComponent();
                    }
                }
                if (hover != lastHover) {
                    lastHover = hover;
                    final List<? extends Node> highlight;
                    if (hover != null) {
                        Node node = Visualizer.findNode(hover);
                        highlight = Arrays.asList(node);
                    } else {
                        highlight = Collections.EMPTY_LIST;
                    }
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            pageModel.setHighlightedNodes(highlight);
                        }
                    });
                }
            }
            
        };
    }

    /**
     * Highlighted (visualizer) nodes.
     * This collection is for rendering purposes only.
     */
    private final List highlightedTreeNodes = new ArrayList();

    /**
     * Updates the set of highlighted nodes.
     */
    final void updateHighlight() {
        synchronized (highlightedTreeNodes) {
            highlightedTreeNodes.clear();
            for (Node node : pageModel.getHighlightedNodes()) {
                TreeNode visualizer = Visualizer.findVisualizer(node);
                highlightedTreeNodes.add(visualizer);
            }
        }
        treeView.repaint();
    }

    /**
     * Determines whether the given (visualizer) node is highlighted.
     * 
     * @param treeNode (visualizer) node to check.
     * @return {@code true} when the specified node should be highlighted,
     * returns {@code false} otherwise.
     */
    boolean isHighlighted(Object treeNode) {
        synchronized (highlightedTreeNodes) {
            return highlightedTreeNodes.contains(treeNode);
        }
    } 

    /**
     * Creates a cell renderer for the DOM tree.
     * 
     * @param delegate delegating/original tree renderer.
     * @return call renderer for the DOM tree.
     */
    private TreeCellRenderer createTreeCellRenderer(final TreeCellRenderer delegate) {
        Color origColor = UIManager.getColor("Tree.selectionBackground"); // NOI18N
        Color color = origColor.brighter().brighter();
        if (color.equals(Color.WHITE)) { // Issue 217127
            color = origColor.darker();
        }
        // Color used for hovering highlight
        final Color hoverColor = color;
        final boolean nimbus = "Nimbus".equals(UIManager.getLookAndFeel().getID()); // NOI18N
        final JPanel nimbusPanel = nimbus ? new JPanel(new BorderLayout()) : null;
        return new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JComponent component;
                if (!selected && isHighlighted(value)) {
                    component = (JComponent)delegate.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, hasFocus);
                    if (nimbus) {
                        nimbusPanel.removeAll();
                        nimbusPanel.add(component);
                        component = nimbusPanel;
                    }
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
     * Initializes the "no DOM available" label.
     */
    private void initNoDOMLabel() {
        noDomLabel = new JLabel();
        noDomLabel.setText(NbBundle.getMessage(DomPanel.class, "DomPanel.noDomLabel.text")); // NOI18N
        noDomLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noDomLabel.setVerticalAlignment(SwingConstants.CENTER);
        noDomLabel.setEnabled(false);
        noDomLabel.setBackground(UIManager.getColor("Tree.background"));
        noDomLabel.setOpaque(true);
    }

    /**
     * Initializes the header panel.
     */
    private void initHeader() {
        selectModeButton = createSelectModeButton();
        initURLLabel();

        CustomToolbar toolbar = new CustomToolbar();
        toolbar.addButton(selectModeButton);

        headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.add(toolbar);
        headerPanel.add(urlLabel);
        headerPanel.setVisible(false);
    }
    
    /**
     * Initializes the label showing the URL.
     */
    private void initURLLabel() {
        urlLabel = new JLabel() {
            @Override
            public Dimension getMinimumSize() {
                // Allow horizontal shrinking of Browser DOM view below pref./min. size
                Dimension dim = super.getMinimumSize();
                return new Dimension(0, dim.height);
            }
        };
        urlLabel.setOpaque(false);
        if (pageModel != null) {
            Lookup lookup = pageModel.getPageContext();
            Image image = lookup.lookup(Image.class);
            Icon icon;
            if (image == null) {
                icon = lookup.lookup(Icon.class);
            } else {
                icon = new ImageIcon(image);
            }
            if (icon == null) {
                BrowserFamilyId id = lookup.lookup(BrowserFamilyId.class);
                if (id != null) {
                    for (WebBrowser browser : WebBrowsers.getInstance().getAll(true, true, true, false)) {
                        if (browser.hasNetBeansIntegration() && (id == browser.getBrowserFamily())) {
                            image = browser.getIconImage(true);
                            if (image != null) {
                                icon = new ImageIcon(image);
                                break;
                            }
                        }
                    }
                }
            }
            if (icon != null) {
                urlLabel.setIcon(icon);
            }
        }
    }

    /**
     * Creates a button that controls the select mode in the browser.
     * 
     * @return button that controls the select mode in the browser.
     */
    private AbstractButton createSelectModeButton() {
        AbstractButton button = new JToggleButton();
        button.setFocusPainted(false);
        button.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/web/inspect/resources/selectionMode.png", true)); // NOI18N
        button.setToolTipText(NbBundle.getMessage(DomPanel.class, "DomPanel.inspectMode")); // NOI18N
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractButton button = (AbstractButton)e.getSource();
                final boolean selectMode = button.isSelected();
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        pageModel.setSelectionMode(selectMode);
                    }
                });
            }
        });
        return button;
    }

    /**
     * Updates the state of {@code selectModeButton}.
     */
    void updateSelectionMode() {
        if (EventQueue.isDispatchThread()) {
            boolean selectMode = pageModel.isSelectionMode();
            selectModeButton.setSelected(selectMode);
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateSelectionMode();
                }
            });
        }
    }

    /**
     * Updates the content of the panel. It fetches the current data
     * from the model and updates the view accordingly.
     */
    private void update() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                final Node node = pageModel.getDocumentNode();
                final String url = pageModel.getDocumentURL();
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        update(node, url);
                        updateSelection();
                        updateHighlight();
                    }
                });
            }
        });
    }

    /**
     * Updates the content of the panel.
     * 
     * @param documentNode node that should be shown in the panel (can be {@code null}).
     * @param url URL that corresponds to the given document node.
     */
    private void update(Node documentNode, String url) {
        assert EventQueue.isDispatchThread();

        updatingView  = true;
        try {
            if (documentNode == null) {
                Node root = new AbstractNode(Children.LEAF);
                headerPanel.setVisible(false);
                replace(treeView, noDomLabel);
                manager.setRootContext(root);
            } else {
                manager.setRootContext(documentNode);
                urlLabel.setText(url);
                headerPanel.setVisible(true);
                replace(noDomLabel, treeView);
                expandNodes();
            }
        } finally {
            updatingView = false;
        }
    }

    /**
     * Expands {@code HTML} and {@code BODY} nodes.
     */
    private void expandNodes() {
        Node root = manager.getRootContext();
        treeView.expandNode(root);
        for (Node node : root.getChildren().getNodes()) {
            String nodeName = node.getName();
            if (nodeName != null && nodeName.trim().equalsIgnoreCase("html")) { // NOI18N
                treeView.expandNode(node);
                for (Node subNode : node.getChildren().getNodes()) {
                    nodeName = subNode.getName();
                    if (nodeName != null && nodeName.trim().equalsIgnoreCase("body")) { // NOI18N
                        treeView.expandNode(subNode);
                    }
                }
            }
        }
    }

    /**
     * Helper method that replaces one component by another (if it is necessary).
     * 
     * @param componentToHide component that should be hidden.
     * @param componentToShow component that should be shown.
     */
    private void replace(Component componentToHide, Component componentToShow) {
        if (componentToHide.getParent() != null) {
            remove(componentToHide);
            add(componentToShow);
            revalidate();
            repaint();
        }
    }

    /**
     * Creates {@code PageModel} listener.
     * 
     * @return {@code PageModel} listener.
     */
    private PropertyChangeListener createModelListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if (PageModel.PROP_SELECTED_NODES.equals(propName)) {
                    updateSelection();
                } else if (PageModel.PROP_HIGHLIGHTED_NODES.equals(propName)) {
                    updateHighlight();
                } else if (PageModel.PROP_DOCUMENT.equals(propName)) {
                    update();
                } else if (PageModel.PROP_SELECTION_MODE.equals(propName)) {
                    updateSelectionMode();
                }
            }
        };
    }

    /**
     * Updates the set of selected nodes.
     */
    private void updateSelection() {
        if (EventQueue.isDispatchThread()) {
            List<? extends Node> nodes = pageModel.getSelectedNodes();
            Node[] selection = nodes.toArray(new Node[0]);
            updatingView = true;
            try {
                manager.setSelectedNodes(selection);
            } catch (PropertyVetoException pvex) {
                Logger.getLogger(DomPanel.class.getName()).log(Level.INFO, null, pvex);
            } finally {
                updatingView = false;
            }
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateSelection();
                }
            });
        }
    }

    /**
     * Creates a listener for selected nodes.
     * 
     * @return listener for selected nodes.
     */
    private PropertyChangeListener createSelectedNodesListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if (ExplorerManager.PROP_SELECTED_NODES.equals(propName)) {
                    if (updatingView) {
                        // This change was triggered by update from the model
                        // => no need to synchronize back into the model.
                        return;
                    }
                    final Node[] nodes = manager.getSelectedNodes();
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            pageModel.setSelectedNodes(Arrays.asList(nodes));
                        }
                    });
                }
            }
        };
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    @Override
    public boolean requestFocusInWindow() {
        return treeView.requestFocusInWindow();
    }

}
