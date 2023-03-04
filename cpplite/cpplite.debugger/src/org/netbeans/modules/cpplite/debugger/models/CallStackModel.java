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

package org.netbeans.modules.cpplite.debugger.models;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Action;

import org.netbeans.modules.cpplite.debugger.CPPLiteDebugger;
import org.netbeans.modules.cpplite.debugger.CPPFrame;
import org.netbeans.modules.cpplite.debugger.CPPLiteDebugger.StateListener;
import org.netbeans.modules.cpplite.debugger.CPPThread;
import org.netbeans.modules.cpplite.debugger.Utils;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.debugger.ui.Constants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

@DebuggerServiceRegistration(path="CPPLiteSession/CallStackView", types={TreeModel.class, NodeModel.class, NodeActionsProvider.class, TableModel.class})
public class CallStackModel implements TreeModel, NodeModel, NodeActionsProvider, TableModel,
                                       StateListener {

    private static final String CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/NonCurrentFrame";
    private static final String CURRENT_CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/CurrentFrame";

    @NbBundle.Messages("CTL_CallStackModel_noStack=No Stack Information")
    private static final Object[] NO_STACK = new Object[]{Bundle.CTL_CallStackModel_noStack()};

    private final CPPLiteDebugger debugger;
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<>();

    public CallStackModel (ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, CPPLiteDebugger.class);
        debugger.addStateListener(WeakListeners.create(StateListener.class, this, debugger));
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
    public Object[] getChildren (Object parent, int from, int to)
        throws UnknownTypeException {
        if (parent == ROOT) {
            CPPThread currentThread = debugger.getCurrentThread();
            if (currentThread == null) {
                return NO_STACK;
            }
            return currentThread.getStack();
        }
        throw new UnknownTypeException (parent);
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
        if (node == ROOT)
            return false;
        if (node instanceof CPPFrame) {
            return true;
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
            CPPThread currentThread = debugger.getCurrentThread();
            if (currentThread == null) {
                return 0;
            }
            CPPFrame[] stack = currentThread.getStack();
            if (stack == null) {
                return 1;
            } else {
                return stack.length;
            }
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
        if (node instanceof CPPFrame) {
            CPPFrame frame = (CPPFrame) node;
            return frame.getName();
        }
        if (node == ROOT) {
            return ROOT;
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
        if (node instanceof CPPFrame) {
            CPPFrame frame = (CPPFrame) node;
            return frame == debugger.getCurrentFrame() ? CURRENT_CALL_STACK : CALL_STACK;
        }
        if (node == ROOT) {
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
        if (node instanceof CPPFrame) {
            CPPFrame frame = (CPPFrame) node;
            return frame.getDescription();
        }
        throw new UnknownTypeException (node);
    }

    // NodeActionsProvider implementation ......................................

    /**
     * Performs default action for given node.
     *
     * @throws  UnknownTypeException if this NodeActionsProvider implementation
     *          is not able to resolve actions for given node type
     * @return  display name for given node
     */
    @Override
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node instanceof CPPFrame) {
            Line line = ((CPPFrame) node).location();
            if (line != null) {
                Utils.showLine(new Line[] {line});
            }
            ((CPPFrame) node).makeCurrent();
            return;
        }
        throw new UnknownTypeException (node);
    }

    /**
     * Returns set of actions for given node.
     *
     * @throws  UnknownTypeException if this NodeActionsProvider implementation
     *          is not able to resolve actions for given node type
     * @return  display name for given node
     */
    @Override
    public Action[] getActions (Object node) throws UnknownTypeException {
        return new Action [] {};
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
    public Object getValueAt (Object node, String columnID) throws
    UnknownTypeException {
        if (columnID == Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID) {
            if (node instanceof CPPFrame) {
                CPPFrame frame = (CPPFrame) node;
                URI sourceURI = frame.getSourceURI();
                if (sourceURI == null) {
                    return "";
                }
                String sourceName;
                try {
                    FileObject file = URLMapper.findFileObject(sourceURI.toURL());
                    sourceName = file.getPath();
                } catch (MalformedURLException ex) {
                    sourceName = sourceURI.toString();
                }
                int line = frame.getLine();
                if (line > 0) {
                    return sourceName + ':' + line;
                } else {
                    return sourceName + ":?";
                }
            }
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
        if (columnID == Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID) {
            if (node instanceof CPPFrame) {
                return true;
            }
        }
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

    private void fireChanges() {
        ModelEvent.TreeChanged event = new ModelEvent.TreeChanged(this);
        for (ModelListener l : listeners) {
            l.modelChanged(event);
        }
    }

    @Override
    public void suspended(boolean suspended) {
        fireChanges();
    }

    @Override
    public void finished() {
    }

    @Override
    public void currentThread(CPPThread thread) {
        fireChanges();
    }

    @Override
    public void currentFrame(CPPFrame frame) {
        fireChanges();
    }

}
