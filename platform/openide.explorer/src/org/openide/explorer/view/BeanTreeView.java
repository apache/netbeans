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
package org.openide.explorer.view;

import org.openide.explorer.*;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import java.awt.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.*;


/** Displays {@link Node} hierarchy as a tree of all nodes.
 *
 * <p>
 * This class is a <em>view</em>
 * to use it properly you need to add it into a component which implements
 * {@link Provider}. Good examples of that can be found 
 * in {@link ExplorerUtils}. Then just use 
 * {@link Provider#getExplorerManager} call to get the {@link ExplorerManager}
 * and control its state.
 * </p>
 * <p>
 * There can be multiple <em>views</em> under one container implementing {@link Provider}. Select from
 * range of predefined ones or write your own:
 * </p>
 * <ul>
 *      <li>{@link org.openide.explorer.view.BeanTreeView} - shows a tree of nodes</li>
 *      <li>{@link org.openide.explorer.view.ContextTreeView} - shows a tree of nodes without leaf nodes</li>
 *      <li>{@link org.openide.explorer.view.ListView} - shows a list of nodes</li>
 *      <li>{@link org.openide.explorer.view.IconView} - shows a rows of nodes with bigger icons</li>
 *      <li>{@link org.openide.explorer.view.ChoiceView} - creates a combo box based on the explored nodes</li>
 *      <li>{@link org.openide.explorer.view.TreeTableView} - shows tree of nodes together with a set of their {@link Property}</li>
 *      <li>{@link org.openide.explorer.view.MenuView} - can create a {@link JMenu} structure based on structure of {@link Node}s</li>
 * </ul>
 * <p>
 * All of these views use {@link ExplorerManager#find} to walk up the AWT hierarchy and locate the
 * {@link ExplorerManager} to use as a controler. They attach as listeners to
 * it and also call its setter methods to update the shared state based on the
 * user action. Not all views make sence together, but for example
 * {@link org.openide.explorer.view.ContextTreeView} and {@link org.openide.explorer.view.ListView} were designed to complement
 * themselves and behaves like windows explorer. The {@link org.openide.explorer.propertysheet.PropertySheetView}
 * for example should be able to work with any other view.
 * </p>
*/
public class BeanTreeView extends TreeView {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3841322840231536380L;

    /** Constructor.
    */
    public BeanTreeView() {
        // we should have no border, window system will provide borders
        setBorder(BorderFactory.createEmptyBorder());
    }

    /** Create a new model.
    * The default implementation creates a {@link NodeTreeModel}.
    * @return the model
    */
    protected NodeTreeModel createModel() {
        return new NodeTreeModel();
    }

    /** Can select any nodes.
    */
    protected boolean selectionAccept(Node[] nodes) {
        return true;
    }

    /* Synchronizes selected nodes from the manager of this Explorer.
    */
    protected void showSelection(TreePath[] treePaths) {
        tree.getSelectionModel().setSelectionPaths(treePaths);

        if (treePaths.length == 1) {
            showPathWithoutExpansion(treePaths[0]);
        }
    }

    /* Called whenever the value of the selection changes.
    * @param nodes nodes
    * @param em explorer manager
    */
    @Override
    protected void selectionChanged(Node[] nodes, ExplorerManager em)
    throws PropertyVetoException {
        if (nodes.length > 0) {
            Node context = nodes[0].getParentNode();

            for (int i = 1; i < nodes.length; i++) {
                if (context != nodes[i].getParentNode()) {
                    em.setSelectedNodes(nodes);

                    return;
                }
            }

            // May not set explored context above the root context:
            if (em.getRootContext().getParentNode() == context) {
                em.setExploredContextAndSelection(em.getRootContext(), nodes);
            } else {
                em.setExploredContextAndSelection(context, nodes);
            }
        } else {
            em.setSelectedNodes(nodes);
        }
    }

    /** Expand the given path and makes it visible.
    * @param path the path
    */
    protected void showPath(TreePath path) {
        tree.expandPath(path);
        showPathWithoutExpansion(path);
    }

    /** Make a path visible.
    * @param path the path
    */
    private void showPathWithoutExpansion(TreePath path) {
        Rectangle rect = tree.getPathBounds(path);
        if (rect != null) { //PENDING
            TreeUI tmp = tree.getUI();
            int correction = 0;
            if (tmp instanceof BasicTreeUI) {
                correction = ((BasicTreeUI) tmp).getLeftChildIndent();
                correction += ((BasicTreeUI) tmp).getRightChildIndent();
            }
            rect.x = Math.max(0, rect.x - correction);
            if (rect.y >= 0) { //#197514 - do not scroll to negative y values
                tree.scrollRectToVisible(rect);
            }
        }
    }

    /** Delegate the setEnable method to Jtree
     *  @param enabled whether to enable the tree
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.tree.setEnabled(enabled);
    }

    /** Is the tree enabled
     *  @return boolean
     */
    @Override
    public boolean isEnabled() {
        if (this.tree == null) {
            // E.g. in JDK 1.5 w/ GTK L&F, may be called from TreeView's
            // super (JScrollPane) constructor, so tree is uninitialized
            return true;
        }

        return this.tree.isEnabled();
    }
}
