/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                    manager.setSelectedNodes(list.toArray(new Node[list.size()]));
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
            return actionList.toArray(new Action[actionList.size()]);
        }
        
    }

}
