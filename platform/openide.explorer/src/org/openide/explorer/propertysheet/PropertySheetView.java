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
package org.openide.explorer.propertysheet;

import java.awt.Dimension;
import org.openide.explorer.*;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;

import java.beans.*;

import javax.swing.*;


/** An Explorer view displaying a {@link PropertySheet} - e.g. table with
 * properties for currently selected {@link Node}
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
 *
* @author   Jan Jancura, Jaroslav Tulach, Ian Formanek
*/
public class PropertySheetView extends PropertySheet {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7568245745904766160L;

    /** helper flag for avoiding multiple initialization of the GUI */
    private transient boolean guiInitialized = false;

    /** The Listener that tracks changes in explorerManager */
    private transient PropertyIL managerListener;

    /** manager to use */
    private transient ExplorerManager explorerManager;

    public PropertySheetView() {
        setPreferredSize(new Dimension (200, 300));
    }

    /** Initializes the GUI of the view */
    private void initializeGUI() {
        guiInitialized = true;

        // (TDB) extra border deleted
        // setBorder (new javax.swing.border.EtchedBorder());
        managerListener = new PropertyIL();
    }

    /* Initializes the sheet.
    */
    public void addNotify() {
        super.addNotify();

        explorerManager = ExplorerManager.find(this);

        if (!guiInitialized) {
            initializeGUI();
        }

        // add propertyChange listeners to the explorerManager
        explorerManager.addPropertyChangeListener(managerListener);
        setNodes(explorerManager.getSelectedNodes());
    }

    /* Deinitializes the sheet.
    */
    public void removeNotify() {
        super.removeNotify();

        if (explorerManager != null) { //[PENDING] patch for bug in JDK1.3 Window
            explorerManager.removePropertyChangeListener(managerListener);
            explorerManager = null;
            setNodes(new Node[0]);
        }
    }

    // INNER CLASSES ***************************************************************************

    /**
    * The inner adaptor class for listening to the ExplorerManager's property and
    * vetoable changes.
    */
    class PropertyIL implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                setNodes((Node[]) evt.getNewValue());
            }
        }
    }
}
