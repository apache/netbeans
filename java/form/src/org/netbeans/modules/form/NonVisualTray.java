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
package org.netbeans.modules.form;

import java.awt.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import org.openide.nodes.*;
import org.openide.actions.*;
import org.openide.explorer.*;
import org.openide.explorer.view.*;

/**
 * A component that displays non visual beans.
 *
 * @author  Jan Stola
 */
public class NonVisualTray extends JPanel implements ExplorerManager.Provider {
    /** List view used to display beans. */
    private NonVisualView listView;
    /** Explorer manager for the list view. */
    private ExplorerManager manager;
    /** Explorer manager from FormDesigner to synchronize with. */
    private ExplorerManager syncManager;
    /** Listener doing the synchronization. */
    private PropertyChangeListener selectionListener;

    /**
     * Creates new <code>NonVisualTray</code>.
     * @param syncManager ExplorerManager to synchronize with
     * @param rootNode Node to be used as root (Other Components)
     */
    public NonVisualTray(ExplorerManager syncManager, Node rootNode) {
        this.manager = new ExplorerManager();
        this.syncManager = syncManager;
        manager.setRootContext(new NonVisualNode(rootNode, new NonVisualChildren(rootNode)));
        selectionListener = new Listener();
        manager.addPropertyChangeListener(selectionListener);
        syncManager.addPropertyChangeListener(selectionListener);
        listView = new NonVisualView();
        setLayout(new BorderLayout());
        add(listView, BorderLayout.CENTER);
    }

    void close() {
        syncManager.removePropertyChangeListener(selectionListener);
    }

    /**
     * Returns explorer manager for the list view.
     *
     * @return explorer manager for the list view.
     */
    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    void updateVisualSettings() {
        listView.updateVisualSettings();
    }
    
    /**
     * List view used in the non visual tray.
     */
    private static class NonVisualView extends ListView {
        
        /**
         * Creates new <code>NonVisualView</code>.
         */
        public NonVisualView() {
            list.setCellRenderer(new Renderer());
            list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            list.setVisibleRowCount(-1);
            list.setBorder(BorderFactory.createEmptyBorder(0,4,4,4));
            updateVisualSettings();
            setTraversalAllowed(false);
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        }
        
        @Override
        public Dimension getPreferredSize() {
            Dimension dim = super.getPreferredSize();
            return new Dimension(1, (int)dim.getHeight());
        }
        
        void updateVisualSettings() {
            list.setBackground(FormLoaderSettings.getInstance().getFormDesignerBackgroundColor());
        }

    }

    /**
     * Renderer for the list view of the non visual tray.
     */
    private static class Renderer implements ListCellRenderer {
        /** Button used as a renderer component. */
        private JButton button;
        /** Border for the selected rendered items. */
        private Border selectedBorder;
        /** Border for the unselected rendered items. */
        private Border unselectedBorder;
        
        /**
         * Creates new <code>Renderer</code>.
         */
        public Renderer() {
            button = new JButton();
            button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            unselectedBorder = BorderFactory.createEmptyBorder(4,4,4,4);
            Color selectionColor = FormLoaderSettings.getInstance().getSelectionBorderColor();
            selectedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(3,3,3,3),
                BorderFactory.createLineBorder(selectionColor));
            button.setOpaque(false);
            int fontSize = button.getFontMetrics(button.getFont()).getHeight();
            button.setPreferredSize(new Dimension(64+2*4, 50+fontSize));
        }
        
        @Override
        public Component getListCellRendererComponent(JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Node node = Visualizer.findNode(value);
            ImageIcon icon = new ImageIcon(node.getIcon(java.beans.BeanInfo.ICON_COLOR_32x32));
            button.setIcon(icon);
            String text = node.getShortDescription();
            button.setText(text);
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
            button.setHorizontalAlignment(SwingConstants.CENTER);
            button.setBorder(isSelected ? selectedBorder : unselectedBorder);
            return button;
        }
        
    }
    
    /**
     * Listener that synchronizes the selected nodes of the form designer
     * and the non-visual tray.
     */
    private class Listener implements PropertyChangeListener {
        private boolean updating;
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())
                    || updating) {
                return;
            }
            if (evt.getSource() == manager) { // sync in->out
                Node[] newNodes = (Node[])evt.getNewValue();
                Node[] nodes = new Node[newNodes.length];
                for (int i=0; i<nodes.length; i++) {
                    nodes[i] = ((NonVisualNode)newNodes[i]).getOriginal();
                }
                try {
                    updating = true;
                    syncManager.setSelectedNodes(nodes);
                } catch (PropertyVetoException pvex) {
                } finally {
                    updating = false;
                }
            } else { // sync out->in
                Node[] nodes = (Node[])evt.getNewValue();
                java.util.List<Node> list = new ArrayList<Node>();
                Node node = ((NonVisualNode)manager.getRootContext()).getOriginal();
                for (int i=0; i<nodes.length; i++) {
                    if (node == nodes[i].getParentNode()) {
                        list.add(findFilterNode(nodes[i]));
                    }
                }
                try {
                    updating = true;
                    manager.setSelectedNodes(list.toArray(new Node[0]));
                } catch (PropertyVetoException pvex) {
                } finally {
                    updating = false;
                }
            }
        }

        /**
         * Finds a filter node (in the non-visual tray) that corresponds
         * to the passed node (RADComponentNode).
         */
        private Node findFilterNode(Node original) {
            Node root = manager.getRootContext();
            Node[] nodes = root.getChildren().getNodes(false);
            for (int i=0; i<nodes.length; i++) {
                NonVisualNode node = (NonVisualNode)nodes[i];
                if (node.getOriginal() == original) {
                    return node;
                }
            }
            return null;
        }
        
    }
    
    /**
     * Class that manages children of the <code>NonVisualNode</code>.
     */
    private class NonVisualChildren extends FilterNode.Children {
        
        /**
         * Creates new <code>NonVisualChildren</code>.
         *
         * @param original the original node.
         */
        public NonVisualChildren(Node original) {
            super(original);
        }
        
        /**
         * Creates a replacement for the original subnode.
         *
         * @return a replacement for the original subnode.
         */
        @Override
        protected Node copyNode(Node node) {
            return new NonVisualNode(node);
        }
        
    }

    /**
     * Nodes used in the non visual tray.
     */
    private static class NonVisualNode extends FilterNode {
        
        /**
         * Creates new <code>NonVisualNode</code>.
         *
         * @param original the original node.
         */
        public NonVisualNode(Node original) {
            super(original);
            disableDelegation(DELEGATE_GET_SHORT_DESCRIPTION
                | DELEGATE_GET_ACTIONS);
        }
        
        /**
         * Creates new <code>NonVisualNode</code>
         *
         * @param original the original node.
         * @param children management of the subnodes.
         */
        public NonVisualNode(Node original, Children children) {
            super(original, children);
        }
        
        /**
         * Returns short description of the node (used in tooltips by default).
         *
         * @return short description of the node.
         */
        @Override
        public String getShortDescription() {
            return getName();
        }
        
        /**
         * Returns the original node.
         *
         * @return the original node.
         */
        @Override
        protected Node getOriginal() {
            return super.getOriginal();
        }
        
        /**
         * Returns actions of the node.
         *
         * @param context determines whether context actions should be returned.
         * @return actions of the node.
         */
        @Override
        public Action[] getActions(boolean context) {
            java.util.List forbiddenActions = Arrays.asList(new Class[] {
                MoveUpAction.class,
                MoveDownAction.class
            });
            Action[] actions = getOriginal().getActions(context);
            java.util.List<Action> actionList = new ArrayList<Action>(Arrays.asList(actions));
            for (int i=0; i<actions.length; i++) {
                Action action = actions[i];
                if ((action != null) && (forbiddenActions.contains(action.getClass()))) {
                    actionList.remove(action);
                }
            }
            return actionList.toArray(new Action[0]);
        }
        
    }

}
