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
 *
* @author   Jan Jancura, Jaroslav Tulach, Ian Formanek
*/
public class PropertySheetView extends PropertySheet {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7568245745904766160L;

    /** helper flag for avoiding multiple initialization of the GUI */
    transient private boolean guiInitialized = false;

    /** The Listener that tracks changes in explorerManager */
    transient private PropertyIL managerListener;

    /** manager to use */
    transient private ExplorerManager explorerManager;

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
