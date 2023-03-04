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

package org.netbeans.modules.javascript.v8debug.ui.vars.models;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.commands.Evaluate;
import org.netbeans.lib.v8debug.vars.V8Object;
import org.netbeans.lib.v8debug.vars.V8Value;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.V8DebuggerEngineProvider;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript.v8debug.vars.V8Evaluator;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_TO_STRING_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_TYPE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_VALUE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_TO_STRING_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_TYPE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_VALUE_COLUMN_ID;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path=V8DebuggerEngineProvider.ENGINE_NAME+"/WatchesView",
                             types={ TreeModelFilter.class, ExtendedNodeModel.class, TableModel.class })
public class WatchesModel extends VariablesModel implements TreeModelFilter {
    
    //@StaticResource(searchClasspath = true)
    public static final String ICON_WATCH =
            "org/netbeans/modules/debugger/resources/watchesView/watch_16.png"; // NOI18N
    
    private final RequestProcessor RP = new RequestProcessor(WatchesModel.class);
    private final Map<Watch, Pair<V8Value, String>> evaluatedWatches = new HashMap<>();
    private WatchesListener wlistener;

    public WatchesModel(final ContextProvider contextProvider) {
        super(contextProvider);
        this.wlistener = new WatchesListener(this);
    }
    
    @Override
    public Object getRoot(TreeModel original) {
        return ROOT;
    }

    @Override
    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        throw new IllegalStateException("TreeModelFilter.getChildren() should be called instead!");
    }

    @Override
    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
        if (parent == ROOT) {
            evaluateWatches(dbg.getCurrentFrame());
            return original.getChildren(parent, from, to);
        } else if (parent instanceof Watch) {
            V8Value value;
            Pair<V8Value, String> ew;
            synchronized (evaluatedWatches) {
                ew = evaluatedWatches.get((Watch) parent);
            }
            if (ew != null) {
                value = ew.first();
            } else {
                value = null;
            }
            if (value instanceof V8Object) {
                return getObjectChildren((V8Object) value);
            } else {
                return EMPTY_CHILDREN;
            }
        } else {
            return super.getChildren(parent, from, to);
        }
    }
    
    @Override
    public int getChildrenCount(Object node) throws UnknownTypeException {
        throw new IllegalStateException("TreeModelFilter.getChildrenCount() should be called instead!");
    }

    @Override
    public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        throw new IllegalStateException("TreeModelFilter.isLeaf() should be called instead!");
    }

    @Override
    public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        } else if (node instanceof Watch) {
            V8Value value;
            Pair<V8Value, String> ew;
            synchronized (evaluatedWatches) {
                ew = evaluatedWatches.get((Watch) node);
            }
            if (ew != null) {
                value = ew.first();
            } else {
                value = null;
            }
            return !hasChildren(value);
        } else {
            return super.isLeaf(node);
        }
    }
    
    @Override
    public String getShortDescription(Object node)
            throws UnknownTypeException {
        if (node instanceof Watch) {
            return "";
        } else {
            return super.getShortDescription(node);
        }
    }

    // NodeModel implementation ................................................

    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof Watch) {
            return ((Watch) node).getExpression();
        } else {
            return super.getDisplayName(node);
        }
    }

    @Override
    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node == ROOT || node instanceof Watch) {
            return ICON_WATCH;
        } else {
            return super.getIconBaseWithExtension(node);
        }
    }
    
    // TableModel implementation ...............................................

    @Override
    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
        if (node instanceof Watch) {
            Watch watch = (Watch) node;
            Pair<V8Value, String> ew;
            synchronized (evaluatedWatches) {
                ew = evaluatedWatches.get(watch);
            }
            if (ew != null) {
                if (ew.second() != null) {
                    if (WATCH_VALUE_COLUMN_ID.equals(columnID) ||
                        WATCH_TO_STRING_COLUMN_ID.equals(columnID)) {
                        
                        return toHTML(ew.second(), true, false, Color.red);
                        
                    } else if (WATCH_TYPE_COLUMN_ID.equals(columnID)) {
                        return "";
                    }
                } else {
                    V8Value value = ew.first();
                    if (value != null) {
                        if (WATCH_VALUE_COLUMN_ID.equals(columnID) ||
                            WATCH_TO_STRING_COLUMN_ID.equals(columnID)) {
                            
                            return toHTML(V8Evaluator.getStringValue(value));
                            
                        } else if (WATCH_TYPE_COLUMN_ID.equals(columnID)) {
                            
                            return toHTML(V8Evaluator.getStringType(value));
                        }
                    }
                }
            }
            if (WATCH_VALUE_COLUMN_ID.equals(columnID) ||
                WATCH_TO_STRING_COLUMN_ID.equals(columnID)) {
                return (watch.isEnabled()) ? "N/A" : "";
            } else if(WATCH_TYPE_COLUMN_ID.equals(columnID)) {
                return "";
            }
        } else {
            if (WATCH_VALUE_COLUMN_ID.equals(columnID)) {
                return super.getValueAt(node, LOCALS_VALUE_COLUMN_ID);
            } else if(WATCH_TYPE_COLUMN_ID.equals(columnID)) {
                return super.getValueAt(node, LOCALS_TYPE_COLUMN_ID);
            } else if (WATCH_TO_STRING_COLUMN_ID.equals(columnID) ||
                    LOCALS_TO_STRING_COLUMN_ID.equals(columnID)) {
                return super.getValueAt(node, LOCALS_TO_STRING_COLUMN_ID);
            }
        }
        throw new UnknownTypeException(node);
    }
    
    @Override
    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
        // TODO
        return true;
    }
    
    @Override
    public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
        // TODO
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void notifyFinished() {
        super.notifyFinished();
        if (wlistener != null) {
            wlistener.unregister();
            wlistener = null;
        }
        synchronized (evaluatedWatches) {
            evaluatedWatches.clear();
        }
    }
    
    private void evaluateWatches(CallFrame frame) {
        if (frame == null) {
            synchronized (evaluatedWatches) {
                evaluatedWatches.clear();
            }
            return;
        }
        Watch[] watches = DebuggerManager.getDebuggerManager().getWatches();
        Map<Watch, Pair<V8Value, String>> watchesMap = null;
        int n = watches.length;
        if (n > 0) {
            WatchCB[] cbs = new WatchCB[n];
            for (int i = 0; i < n; i++) {
                if (!watches[i].isEnabled()) {
                    continue;
                }
                cbs[i] = new WatchCB();
                V8Request request = dbg.sendCommandRequest(V8Command.Evaluate,
                                                           new Evaluate.Arguments(watches[i].getExpression()), cbs[i]);
                if (request == null) {
                    cbs[i] = null;
                    break;
                }
            }
            watchesMap = new HashMap<>();
            for (int i = 0; i < n; i++) {
                WatchCB cb = cbs[i];
                if (cb != null) {
                    watchesMap.put(watches[i], Pair.of(cb.getValue(), cb.getError()));
                }
            }
        }
        synchronized (evaluatedWatches) {
            evaluatedWatches.clear();
            if (watchesMap != null) {
                evaluatedWatches.putAll(watchesMap);
            }
        }
    }
    
    private void reevaluateWatch(final Watch watch) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                Pair<V8Value, String> result = null;
                if (watch.isEnabled()) {
                    WatchCB wcb = new WatchCB();
                    V8Request request = dbg.sendCommandRequest(V8Command.Evaluate,
                                                               new Evaluate.Arguments(watch.getExpression()), wcb);
                    if (request != null) {
                        result = Pair.of(wcb.getValue(), wcb.getError());
                    }
                }
                synchronized (evaluatedWatches) {
                    if (result == null) {
                        evaluatedWatches.remove(watch);
                    } else {
                        evaluatedWatches.put(watch, result);
                    }
                }
                fireChangeEvent(new ModelEvent.NodeChanged(this, watch));
            }
        });
    }
    
    static final class WatchCB implements V8Debugger.CommandResponseCallback {
        
        private boolean responded;
        private V8Value value;
        private String error;

        @Override
        public synchronized void notifyResponse(V8Request request, V8Response response) {
            if (response != null) {
                if (response.isSuccess()) {
                    this.error = null;
                    Evaluate.ResponseBody erb = (Evaluate.ResponseBody) response.getBody();
                    this.value = erb.getValue();
                } else {
                    this.error = response.getErrorMessage();
                    this.value = null;
                }
            }
            responded = true;
            notifyAll();
        }
        
        public synchronized V8Value getValue() {
            if (!responded) {
                try {
                    wait();
                } catch (InterruptedException ex) {}
            }
            return value;
        }
        
        public synchronized String getError() {
            if (!responded) {
                try {
                    wait();
                } catch (InterruptedException ex) {}
            }
            return error;
        }
        
    }
    
    private static class WatchesListener extends DebuggerManagerAdapter implements PropertyChangeListener {

        private WatchesModel model;

        public WatchesListener(WatchesModel watchesModel) {
            this.model = watchesModel;
            DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_WATCHES, this);
            Watch[] watches = DebuggerManager.getDebuggerManager().getWatches();
            for (Watch watch : watches) {
                watch.addPropertyChangeListener(this);
            }
        }

        @Override
        public void watchAdded(Watch watch) {
            watch.addPropertyChangeListener(this);
            model.refresh();
        }

        @Override
        public void watchRemoved(Watch watch) {
            watch.removePropertyChangeListener(this);
            model.refresh();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!(evt.getSource() instanceof Watch)) {
                return;
            }
            Watch w = (Watch) evt.getSource();
            model.reevaluateWatch(w);
            //model.fireChangeEvent(new ModelEvent.NodeChanged(this, w));
        }

        private void unregister() {
            DebuggerManager.getDebuggerManager().removeDebuggerListener(DebuggerManager.PROP_WATCHES, this);
            Watch[] watches = DebuggerManager.getDebuggerManager().getWatches();
            for (Watch watch : watches) {
                watch.removePropertyChangeListener(this);
            }
        }
    }
    
}
