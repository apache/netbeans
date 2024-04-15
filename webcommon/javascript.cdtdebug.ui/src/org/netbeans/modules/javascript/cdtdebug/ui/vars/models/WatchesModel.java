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

package org.netbeans.modules.javascript.cdtdebug.ui.vars.models;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;
import org.netbeans.lib.chrome_devtools_protocol.debugger.EvaluateOnCallFrameRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.EvaluateOnCallFrameResponse;
import org.netbeans.lib.chrome_devtools_protocol.debugger.Scope;
import org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject;
import org.netbeans.modules.javascript.cdtdebug.CDTDebuggerEngineProvider;
import org.netbeans.modules.javascript.cdtdebug.vars.CDTEvaluator;
import org.netbeans.modules.javascript.cdtdebug.vars.Variable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.Constants;

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
import org.openide.util.Pair;

import static org.netbeans.modules.javascript2.debug.ui.models.ViewModelSupport.toHTML;
import static org.netbeans.spi.viewmodel.TreeModel.ROOT;

@DebuggerServiceRegistration(path=CDTDebuggerEngineProvider.ENGINE_NAME+"/WatchesView",
                             types={ TreeModelFilter.class, ExtendedNodeModel.class, TableModel.class })
public class WatchesModel extends VariablesModel implements TreeModelFilter {

    //@StaticResource(searchClasspath = true)
    public static final String ICON_WATCH =
            "org/netbeans/modules/debugger/resources/watchesView/watch_16.png"; // NOI18N

    private final Map<Watch, Pair<RemoteObject, String>> evaluatedWatches = new HashMap<>();
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
            RemoteObject value;
            Pair<RemoteObject, String> ew;
            synchronized (evaluatedWatches) {
                ew = evaluatedWatches.get((Watch) parent);
            }
            if (ew != null) {
                value = ew.first();
            } else {
                value = null;
            }
            if (value instanceof RemoteObject) {
                return getObjectChildren(null, "", (RemoteObject) value);
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
    public boolean isLeaf(Object node) {
        throw new IllegalStateException("TreeModelFilter.isLeaf() should be called instead!");
    }

    @Override
    public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        } else if (node instanceof Watch) {
            RemoteObject value;
            Pair<RemoteObject, String> ew;
            synchronized (evaluatedWatches) {
                ew = evaluatedWatches.get((Watch) node);
            }
            if (ew != null) {
                value = ew.first();
            } else {
                value = null;
            }
            return VariablesModel.isLeaf2(value);
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
            Pair<RemoteObject, String> ew;
            synchronized (evaluatedWatches) {
                ew = evaluatedWatches.get(watch);
            }
            if (ew != null) {
                if (ew.second() != null) {
                    if (WATCH_VALUE_COLUMN_ID.equals(columnID) ||
                        WATCH_TO_STRING_COLUMN_ID.equals(columnID)) {

                        return ew.second();

                    } else if (WATCH_TYPE_COLUMN_ID.equals(columnID)) {
                        return "";
                    }
                } else {
                    RemoteObject value = ew.first();
                    if (value != null) {
                        if (WATCH_VALUE_COLUMN_ID.equals(columnID) ||
                            WATCH_TO_STRING_COLUMN_ID.equals(columnID)) {

                            return CDTEvaluator.getStringValue(value);

                        } else if (WATCH_TYPE_COLUMN_ID.equals(columnID)) {

                            return CDTEvaluator.getStringType(value);
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
    public boolean hasHTMLValueAt(Object node, String columnID) throws UnknownTypeException {
        if (node instanceof Watch) {
            Watch watch = (Watch) node;
            Pair<RemoteObject, String> ew;
            synchronized (evaluatedWatches) {
                ew = evaluatedWatches.get(watch);
            }
            if (ew != null) {
                if (ew.second() != null) {
                    if (WATCH_VALUE_COLUMN_ID.equals(columnID) ||
                        WATCH_TO_STRING_COLUMN_ID.equals(columnID)) {

                        return true;

                    } else if (WATCH_TYPE_COLUMN_ID.equals(columnID)) {
                        return false;
                    }
                } else {
                    RemoteObject value = ew.first();
                    if (value != null) {
                        if (WATCH_VALUE_COLUMN_ID.equals(columnID) ||
                            WATCH_TO_STRING_COLUMN_ID.equals(columnID)) {

                            return true;

                        } else if (WATCH_TYPE_COLUMN_ID.equals(columnID)) {

                            return true;
                        }
                    }
                }
            }
            if (WATCH_VALUE_COLUMN_ID.equals(columnID) ||
                WATCH_TO_STRING_COLUMN_ID.equals(columnID)) {
                return false;
            } else if(WATCH_TYPE_COLUMN_ID.equals(columnID)) {
                return false;
            }
        } else {
            if (WATCH_VALUE_COLUMN_ID.equals(columnID)) {
                return super.hasHTMLValueAt(node, LOCALS_VALUE_COLUMN_ID);
            } else if(WATCH_TYPE_COLUMN_ID.equals(columnID)) {
                return super.hasHTMLValueAt(node, LOCALS_TYPE_COLUMN_ID);
            } else if (WATCH_TO_STRING_COLUMN_ID.equals(columnID) ||
                    LOCALS_TO_STRING_COLUMN_ID.equals(columnID)) {
                return super.hasHTMLValueAt(node, LOCALS_TO_STRING_COLUMN_ID);
            }
        }
        throw new UnknownTypeException(node);
    }

    @Override
    public String getHTMLValueAt(Object node, String columnID) throws UnknownTypeException {
        if (node instanceof Watch) {
            Watch watch = (Watch) node;
            Pair<RemoteObject, String> ew;
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
                    RemoteObject value = ew.first();
                    if (value != null) {
                        if (WATCH_VALUE_COLUMN_ID.equals(columnID) ||
                            WATCH_TO_STRING_COLUMN_ID.equals(columnID)) {

                            return toHTML(CDTEvaluator.getStringValue(value));

                        } else if (WATCH_TYPE_COLUMN_ID.equals(columnID)) {

                            return toHTML(CDTEvaluator.getStringType(value));
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
                return super.getHTMLValueAt(node, LOCALS_VALUE_COLUMN_ID);
            } else if(WATCH_TYPE_COLUMN_ID.equals(columnID)) {
                return super.getHTMLValueAt(node, LOCALS_TYPE_COLUMN_ID);
            } else if (WATCH_TO_STRING_COLUMN_ID.equals(columnID) ||
                    LOCALS_TO_STRING_COLUMN_ID.equals(columnID)) {
                return super.getHTMLValueAt(node, LOCALS_TO_STRING_COLUMN_ID);
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
        Map<Watch, CompletableFuture<Pair<RemoteObject, String>>> watchesMapFutures = new HashMap<>();
        Map<Watch, Pair<RemoteObject, String>> watchesMap = null;
        int n = watches.length;
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                if (!watches[i].isEnabled()) {
                    continue;
                }
                EvaluateOnCallFrameRequest req = new EvaluateOnCallFrameRequest();
                req.setCallFrameId(dbg.getCurrentFrame().getCallFrameId());
                req.setExpression(watches[0].getExpression());

                watchesMapFutures.put(
                        watches[i],
                        dbg.getConnection()
                                .getDebugger()
                                .evaluateOnCallFrame(req)
                                .handle((eocfr, thr) -> {
                                    String exceptionText = convertExceptionData(eocfr, thr);
                                    return Pair.of(
                                            eocfr != null ? eocfr.getResult() : null,
                                            exceptionText);
                                })
                                .toCompletableFuture()
                );
            }

            watchesMap = watchesMapFutures
                    .entrySet()
                    .stream()
                    .map(e -> {
                        try {
                            return new SimpleEntry<>(e.getKey(), e.getValue().get());
                        } catch (InterruptedException | ExecutionException ex) {
                            return new SimpleEntry<>(e.getKey(), Pair.of((RemoteObject) null, ex.getMessage()));
                        }
                    })
                    .collect(
                            Collectors.toMap(e -> e.getKey(), e -> e.getValue())
                    );
        }
        synchronized (evaluatedWatches) {
            evaluatedWatches.clear();
            if(watchesMap != null) {
                evaluatedWatches.putAll(watchesMap);
            }
        }
    }

    private void reevaluateWatch(final Watch watch) {
        if (watch.isEnabled()) {
            EvaluateOnCallFrameRequest req = new EvaluateOnCallFrameRequest();
            req.setCallFrameId(dbg.getCurrentFrame().getCallFrameId());
            req.setExpression(watch.getExpression());
            dbg.getConnection()
                    .getDebugger()
                    .evaluateOnCallFrame(req)
                    .handle((eocfr, thr) -> {
                        synchronized (evaluatedWatches) {
                            String exceptionText = convertExceptionData(eocfr, thr);
                            evaluatedWatches.put(watch, Pair.of(
                                    eocfr != null ? eocfr.getResult() : null,
                                    exceptionText
                            ));
                        }
                        fireChangeEvent(new ModelEvent.NodeChanged(this, watch));
                        return null;
                    });
        } else {
            evaluatedWatches.remove(watch);
        }
    }

    private String convertExceptionData(EvaluateOnCallFrameResponse eocfr, Throwable thr) {
        if(eocfr != null && eocfr.getExceptionDetails() != null) {
            return eocfr.getExceptionDetails().getText()
                    + "\n\n"
                    + eocfr.getExceptionDetails().getException().getDescription();
        } else if (thr != null) {
            return thr.getMessage();
        } else {
            return null;
        }
    }

    private static class WatchesListener extends DebuggerManagerAdapter implements PropertyChangeListener {

        private final WatchesModel model;

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
            model.fireChangeEvent(new ModelEvent.NodeChanged(this, w));
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
