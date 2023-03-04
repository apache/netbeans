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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.cpplite.debugger.CPPFrame;
import org.netbeans.modules.cpplite.debugger.CPPLiteDebugger;
import org.netbeans.modules.cpplite.debugger.CPPLiteDebugger.StateListener;
import org.netbeans.modules.cpplite.debugger.CPPThread;
import org.netbeans.modules.nativeimage.api.debug.EvaluateException;
import org.netbeans.modules.nativeimage.api.debug.NIVariable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.openide.util.WeakListeners;

@DebuggerServiceRegistration(path="CPPLiteSession/WatchesView", types={TreeModelFilter.class, NodeModelFilter.class, TableModel.class})
public class WatchesModel implements TreeModelFilter, NodeModelFilter, TableModel, StateListener {

    private static final String WATCH =
    "org/netbeans/modules/debugger/resources/watchesView/Watch";

    private final CPPLiteDebugger       debugger;
    private final Map<Watch, EvalWatch> evalWatches = new HashMap<>();
    private final List<ModelListener>   listeners = new CopyOnWriteArrayList<>();
    private volatile CPPFrame           currentFrame;

    public WatchesModel (ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, CPPLiteDebugger.class);
        debugger.addStateListener(WeakListeners.create(StateListener.class, this, debugger));
        currentFrame = debugger.getCurrentFrame();
    }

    // TreeModelFilter implementation ................................................

    @Override
    public Object getRoot(TreeModel original) {
        return original.getRoot();
    }

    @Override
    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
        Object[] watches = original.getChildren(parent, from, to);
        synchronized (evalWatches) {
            for (int i = 0; i < watches.length; i++) {
                Object watchObj = watches[i];
                if (watchObj instanceof Watch) {
                    Watch w = (Watch) watchObj;
                    EvalWatch ew = evalWatches.get(w);
                    if (ew == null) {
                        ew = new EvalWatch(w);
                        evalWatches.put(w, ew);
                    }
                }
            }
        }
        return watches;
    }

    @Override
    public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
        EvalWatch ew;
        synchronized (evalWatches) {
            ew = evalWatches.get(node);
        }
        if (ew != null) {
            switch (ew.getStatus()) {
                case READY:
                    NIVariable result = ew.getResult();
                    return result.getNumChildren();
            }
        }
        return original.getChildrenCount(node);
    }

    @Override
    public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
        EvalWatch ew;
        synchronized (evalWatches) {
            ew = evalWatches.get(node);
        }
        if (ew != null) {
            switch (ew.getStatus()) {
                case READY:
                    NIVariable result = ew.getResult();
                    return result.getNumChildren() == 0;
            }
        }
        return true;
    }

    // NodeModelFilter implementation ................................................

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
    public String getDisplayName (NodeModel model, Object node) throws UnknownTypeException {
        return model.getDisplayName(node);
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
    public String getIconBase (NodeModel model, Object node) throws UnknownTypeException {
        return model.getIconBase(node);
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
    public String getShortDescription (NodeModel model, Object node) throws UnknownTypeException {
        EvalWatch ew;
        synchronized (evalWatches) {
            ew = evalWatches.get(node);
        }
        if (ew != null) {
            ew.startEvaluate();
            switch (ew.getStatus()) {
                case READY:
                    NIVariable result = ew.getResult();
                    return ew.getExpression() + " = " + result.getValue();
                case FAILED:
                    EvaluateException exc = ew.getException();
                    return exc.getLocalizedMessage();
            }
        }
        return model.getShortDescription(node);
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
        boolean showValue = columnID == Constants.WATCH_VALUE_COLUMN_ID;
        if (showValue || columnID == Constants.WATCH_TYPE_COLUMN_ID) {
            EvalWatch ew;
            synchronized (evalWatches) {
                ew = evalWatches.get(node);
            }
            if (ew != null) {
                ew.startEvaluate();
                switch (ew.getStatus()) {
                    case READY:
                        NIVariable result = ew.getResult();
                        if (showValue) {
                            return result.getValue();
                        } else {
                            return result.getType();
                        }
                    case FAILED:
                        if (showValue) {
                            EvaluateException exc = ew.getException();
                            return exc.getLocalizedMessage();
                        }
                }
                return "";
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
        if (columnID == Constants.WATCH_VALUE_COLUMN_ID ||
            columnID == Constants.WATCH_TYPE_COLUMN_ID) {
            if (node instanceof Watch) {
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


    // other mothods ...........................................................

    void fireChanges() {
        ModelEvent.TreeChanged event = new ModelEvent.TreeChanged(this);
        for (ModelListener l : listeners) {
            l.modelChanged(event);
        }
    }

    void fireChanged(Object node) {
        ModelEvent.NodeChanged event = new ModelEvent.NodeChanged(this, node);
        for (ModelListener l : listeners) {
            l.modelChanged(event);
        }
    }

    @Override
    public void currentThread(CPPThread thread) {
    }

    @Override
    public void currentFrame(CPPFrame frame) {
        synchronized (evalWatches) {
            evalWatches.clear();
        }
        currentFrame = frame;
        fireChanges();
    }

    @Override
    public void suspended(boolean suspended) {
    }

    @Override
    public void finished() {
    }

    enum EvalStatus {
        NEW,
        EVALUATING,
        READY,
        FAILED
    }

    private final class EvalWatch implements PropertyChangeListener {

        private final Watch watch;
        private volatile AtomicReference<EvalStatus> status = new AtomicReference<>(EvalStatus.NEW);
        private volatile String expression;
        private volatile NIVariable result;
        private volatile EvaluateException exception;

        private EvalWatch(Watch watch) {
            this.watch = watch;
            watch.addPropertyChangeListener(this);
        }

        EvalStatus getStatus() {
            return status.get();
        }

        void startEvaluate() {
            CPPFrame frame = currentFrame;
            if (frame == null || !watch.isEnabled()) {
                return;
            }
            if (status.compareAndSet(EvalStatus.NEW, EvalStatus.EVALUATING)) {
                result = null;
                exception = null;
                String expression = watch.getExpression();
                this.expression = expression;
                frame.evaluateAsync(expression).thenAccept(
                                   (NIVariable variable) -> {
                                       result = variable;
                                       status.set(EvalStatus.READY);
                                       fireChanged(watch);
                                   }).exceptionally(
                                   exc -> {
                                       exception = (EvaluateException) exc;
                                       status.set(EvalStatus.FAILED);
                                       fireChanged(watch);
                                       return null;
                                   });
            }
        }

        String getExpression() {
            return expression;
        }

        NIVariable getResult() {
            return result;
        }

        EvaluateException getException() {
            return exception;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (status.getAndSet(EvalStatus.NEW) != EvalStatus.NEW) {
                startEvaluate();
            }
        }

    }
}
