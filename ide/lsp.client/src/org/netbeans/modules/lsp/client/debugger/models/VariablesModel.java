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

package org.netbeans.modules.lsp.client.debugger.models;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.lsp.client.debugger.DAPDebugger;
import org.netbeans.modules.lsp.client.debugger.DAPFrame;
import org.netbeans.modules.lsp.client.debugger.DAPVariable;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

import org.netbeans.spi.viewmodel.ColumnModel;

/**
 */
@DebuggerServiceRegistration(path="DAPDebuggerSession/LocalsView", types={TreeModel.class, NodeModel.class, TableModel.class})
public final class VariablesModel extends CurrentFrameTracker implements TreeModel, NodeModel, TableModel {

    private static final String         LOCAL =
        "org/netbeans/modules/debugger/resources/localsView/LocalVariable";

    @NbBundle.Messages("CTL_VariablesModel_noVars=No variables to display.") //better mesage?
    private static final Object[] NO_VARS = new Object[]{Bundle.CTL_VariablesModel_noVars()};

    private final DAPDebugger       debugger;
    private final List<ModelListener>   listeners = new CopyOnWriteArrayList<>();


    public VariablesModel (ContextProvider contextProvider) {
        super(contextProvider);
        debugger = contextProvider.lookupFirst(null, DAPDebugger.class);
    }


    // TreeModel implementation ................................................

    /**
     * Returns the root node of the tree or null, if the tree is empty.
     *
     * @return the root node of the tree or null
     */
    @Override
    public Object getRoot () {
        return ROOT;
    }

    /**
     * Returns children for given parent on given indexes.
     *
     * @param   parent a parent of returned nodes
     * @param   from a start index
     * @param   to a end index
     *
     * @throws  NoInformationException if the set of children can not be
     *          resolved
     * @throws  ComputingException if the children resolving process
     *          is time consuming, and will be performed off-line
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  children for given parent on given indexes
     */
    @Override
    public Object[] getChildren (Object parent, int from, int to) throws UnknownTypeException {
        DAPVariable parentVar;
        if (parent == ROOT) {
            parentVar = null;
        } else if (parent instanceof DAPVariable) {
            parentVar = (DAPVariable) parent;
        } else {
            throw new UnknownTypeException (parent);
        }
        DAPFrame frame = getCurrentFrame();
        if (frame != null) {
            if (parentVar == null) {
                try {
                    return debugger.getFrameVariables(frame).get().toArray();
                } catch (Throwable t) {
                    return new Object[] {t.getLocalizedMessage()};
                }
            } else {
                return parentVar.getChildren(from, to);
            }
        } else {
            return NO_VARS;
        }
    }

    /**
     * Returns true if node is leaf.
     *
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     * @return  true if node is leaf
     */
    @Override
    public boolean isLeaf (Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        }
        if (node instanceof String) {
            return true;
        }
        if (node instanceof DAPVariable) {
            return ((DAPVariable) node).getTotalChildren() == 0;
        }
        throw new UnknownTypeException (node);
    }

    /**
     * Returns number of children for given node.
     *
     * @param   node the parent node
     * @throws  NoInformationException if the set of children can not be
     *          resolved
     * @throws  ComputingException if the children resolving process
     *          is time consuming, and will be performed off-line
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     * @since 1.1
     */
    @Override
    public int getChildrenCount (Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return Integer.MAX_VALUE;
        } else if (node instanceof DAPVariable) {
            return ((DAPVariable) node).getTotalChildren();
        }
        throw new UnknownTypeException (node);
    }

    /**
     * Registers given listener.
     *
     * @param l the listener to add
     */
    @Override
    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    /**
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    @Override
    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }


    // NodeModel implementation ................................................

    /**
     * Returns display name for given node.
     *
     * @throws  ComputingException if the display name resolving process
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve display name for given node type
     * @return  display name for given node
     */
    @Override
    public String getDisplayName (Object node) throws UnknownTypeException {
        if (node instanceof String) {
            return (String) node;
        }
        if (node instanceof DAPVariable) {
            return ((DAPVariable) node).getName();
        }
        throw new UnknownTypeException (node);
    }

    /**
     * Returns icon for given node.
     *
     * @throws  ComputingException if the icon resolving process
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve icon for given node type
     * @return  icon for given node
     */
    @Override
    public String getIconBase (Object node) throws UnknownTypeException {
        if (node instanceof DAPVariable) {
            return LOCAL;
        }
        if (node instanceof String) {
            return null;
        }
        throw new UnknownTypeException (node);
    }

    /**
     * Returns tooltip for given node.
     *
     * @throws  ComputingException if the tooltip resolving process
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve tooltip for given node type
     * @return  tooltip for given node
     */
    @Override
    public String getShortDescription (Object node) throws UnknownTypeException {
        if (node instanceof String)
            return null;
        throw new UnknownTypeException (node);
    }


    // TableModel implementation ...............................................

    /**
     * Returns value to be displayed in column <code>columnID</code>
     * and row identified by <code>node</code>. Column ID is defined in by
     * {@link ColumnModel#getID}, and rows are defined by values returned from
     * {@link org.netbeans.spi.viewmodel.TreeModel#getChildren}.
     *
     * @param node a object returned from
     *         {@link org.netbeans.spi.viewmodel.TreeModel#getChildren} for this row
     * @param columnID a id of column defined by {@link ColumnModel#getID}
     * @throws ComputingException if the value is not known yet and will
     *         be computed later
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     *
     * @return value of variable representing given position in tree table.
     */
    @Override
    public Object getValueAt (Object node, String columnID) throws UnknownTypeException {
        if (columnID.equals ("LocalsValue")) {
            if (node instanceof DAPVariable) {
                return ((DAPVariable) node).getValue();
            }
        }
        if (columnID.equals ("LocalsType")) {
            if (node instanceof DAPVariable) {
                return ((DAPVariable) node).getType();
            }
        }
        if (node instanceof String) {
            return "";
        }
        throw new UnknownTypeException (node);
    }

    /**
     * Returns true if value displayed in column <code>columnID</code>
     * and row <code>node</code> is read only. Column ID is defined in by
     * {@link ColumnModel#getID}, and rows are defined by values returned from
     * {@link TreeModel#getChildren}.
     *
     * @param node a object returned from {@link TreeModel#getChildren} for this row
     * @param columnID a id of column defined by {@link ColumnModel#getID}
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     *
     * @return true if variable on given position is read only
     */
    @Override
    public boolean isReadOnly (Object node, String columnID) throws UnknownTypeException {
        if ( (node instanceof String) &&
             (columnID.equals ("LocalsValue"))
        ) return true;
        throw new UnknownTypeException (node);
    }

    /**
     * Changes a value displayed in column <code>columnID</code>
     * and row <code>node</code>. Column ID is defined in by
     * {@link ColumnModel#getID}, and rows are defined by values returned from
     * {@link TreeModel#getChildren}.
     *
     * @param node a object returned from {@link TreeModel#getChildren} for this row
     * @param columnID a id of column defined by {@link ColumnModel#getID}
     * @param value a new value of variable on given position
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     */
    @Override
    public void setValueAt (Object node, String columnID, Object value) throws UnknownTypeException {
        throw new UnknownTypeException (node);
    }


    // other mothods ...........................................................

    void fireChanges () {
        ModelEvent.TreeChanged event = new ModelEvent.TreeChanged(this);
        for (ModelListener l : listeners) {
            l.modelChanged(event);
        }
    }

    @Override
    protected void frameChanged() {
        fireChanges();
    }
}
