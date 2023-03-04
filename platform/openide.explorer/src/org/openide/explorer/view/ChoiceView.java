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
package org.openide.explorer.view;

import org.openide.explorer.*;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;

import java.beans.*;

import java.io.*;

import javax.swing.*;


/** Explorer view based on a combo box.
 * <p>
 * This class is a <q>view</q>
 * to use it properly you need to add it into a component which implements
 * {@link Provider}. Good examples of that can be found
 * in {@link ExplorerUtils}. Then just use
 * {@link Provider#getExplorerManager} call to get the {@link ExplorerManager}
 * and control its state.
 * </p>
 * <p>
 * There can be multiple <q>views</q> under one container implementing {@link Provider}. Select from
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
 * @author Jaroslav Tulach
 */
public class ChoiceView extends JComboBox implements Externalizable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 2522310031223476067L;

    /** The local reference to the explorerManager. It is transient
    * because it will be reset in initializeManager() after deserialization.*/
    private transient ExplorerManager manager;

    /** Listens on ExplorerManager. */
    private transient PropertyIL iListener;

    /** model to use */
    private transient NodeListModel model;

    /** Value of property showExploredContext. */
    private boolean showExploredContext = true;

    // init .................................................................................

    /** Default constructor. */
    public ChoiceView() {
        super();
        initializeChoice();
    }

    /** Initialize view. */
    private void initializeChoice() {
        setRenderer(new NodeRenderer());

        setModel(model = createModel());

        iListener = new PropertyIL();
    }

    // XXX [PENDING] setting new model via setModel() is in fact ignored, see model
    // field -> which 'replaces' normal combo model thus the underlying one making 
    // useless.

    /*
    * Write view's state to output stream.
    */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(showExploredContext ? Boolean.TRUE : Boolean.FALSE);
    }

    /*
    * Reads view's state form output stream.
    */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        showExploredContext = ((Boolean) in.readObject()).booleanValue();
    }

    //
    // To override
    //

    /** Creates the model that this view should show.
    */
    protected NodeListModel createModel() {
        return new NodeListModel();
    }

    // main methods .........................................................................

    /** Set showing of explored contexts.
    * @param b <code>true</code> to show the explored context, <code>false</code> the root context
    */
    public void setShowExploredContext(boolean b) {
        showExploredContext = b;
        updateChoice();
    }

    /**
    * Get explored context toggle.
    * @return whether currently showing explored context (default <code>false</code>)
    */
    public boolean getShowExploredContext() {
        return showExploredContext;
    }

    // main methods .........................................................................

    /* Initializes view.
    */
    @Override
    public void addNotify() {
        manager = ExplorerManager.find(this);
        manager.addVetoableChangeListener(iListener);
        manager.addPropertyChangeListener(iListener);

        updateChoice();

        addActionListener(iListener);

        super.addNotify();
    }

    /* Deinitializes view.
    */
    @Override
    public void removeNotify() {
        super.removeNotify();

        removeActionListener(iListener);
        if (manager != null) {
            manager.removeVetoableChangeListener(iListener);
            manager.removePropertyChangeListener(iListener);
        }
    }

    private void updateSelection() {
        Node[] nodes = manager.getSelectedNodes();

        if (nodes.length > 0) {
            setSelectedItem(VisualizerNode.getVisualizer(null, nodes[0]));
        } else {
            setSelectedItem(showExploredContext ? manager.getExploredContext() : manager.getRootContext());
        }
    }

    private void updateChoice() {
        if (manager == null) {
            return;
        }
        if (showExploredContext) {
            model.setNode(manager.getExploredContext());
        } else {
            model.setNode(manager.getRootContext());
        }

        updateSelection();
    }

    // innerclasses .........................................................................

    /* The inner adaptor class for listening to the ExplorerManager's property and vetoable changes. */
    final class PropertyIL extends Object implements PropertyChangeListener, VetoableChangeListener,
        java.awt.event.ActionListener {
        public void vetoableChange(PropertyChangeEvent evt)
        throws PropertyVetoException {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] nodes = (Node[]) evt.getNewValue();

                if (nodes.length > 1) {
                    throw new PropertyVetoException("", evt); // we do not allow multiple selection // NOI18N
                }
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            ChoiceView.this.removeActionListener(this);

            try {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    Node[] selectedNodes = (Node[]) evt.getNewValue();
                    updateSelection();

                    return;
                }

                if (!showExploredContext && ExplorerManager.PROP_ROOT_CONTEXT.equals(evt.getPropertyName())) {
                    updateChoice();

                    return;
                }

                if (showExploredContext && ExplorerManager.PROP_EXPLORED_CONTEXT.equals(evt.getPropertyName())) {
                    updateChoice();

                    return;
                }
            } finally {
                ChoiceView.this.addActionListener(this);
            }
        }

        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            int s = getSelectedIndex();

            if ((s < 0) || (s >= model.getSize())) {
                return;
            }

            Node n = Visualizer.findNode(model.getElementAt(s));

            manager.removeVetoableChangeListener(this);
            manager.removePropertyChangeListener(this);

            try {
                manager.setSelectedNodes(new Node[] { n });
            } catch (PropertyVetoException ex) {
                updateChoice(); // no selection change allowed
            } finally {
                manager.addVetoableChangeListener(this);
                manager.addPropertyChangeListener(this);
            }
        }
    }
}
