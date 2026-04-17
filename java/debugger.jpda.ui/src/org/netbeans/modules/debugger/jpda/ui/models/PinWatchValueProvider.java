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
package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.io.InvalidObjectException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.ui.models.WatchesModel.JPDAWatchEvaluating;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.PinWatchUISupport;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path = "netbeans-JPDASession", types = PinWatchUISupport.ValueProvider.class)
public final class PinWatchValueProvider implements PinWatchUISupport.ValueProvider,
                                                    PropertyChangeListener {
    
    private final Map<Watch, ValueListeners> valueListeners = new HashMap<>();
    private final ContextProvider lookupProvider;
    private final JPDADebuggerImpl debugger;
    private final Action headAction;
    private final WatchRefreshModelImpl refrModel = new WatchRefreshModelImpl();
    
    public PinWatchValueProvider(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = (JPDADebuggerImpl) lookupProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
        headAction = lookupProvider.lookupFirst("PinWatchHeadActions", Action.class);
    }

    @Override
    public String getId() {
        return "org.netbeans.modules.debugger.jpda.PIN_VALUE_PROVIDER";         // NOI18N
    }

    @Override
    public String getValue(Watch watch) {
        ValueListeners vl;
        synchronized (valueListeners) {
            vl = valueListeners.get(watch);
        }
        if (vl == null) {
            return null;
        } else {
            return vl.value;
        }
    }

    @Override
    public void setChangeListener(Watch watch, ValueChangeListener chl) {
        ValueListeners vl = new ValueListeners(chl);
        synchronized (valueListeners) {
            valueListeners.put(watch, vl);
        }
        JPDAWatchEvaluating watchEv = new JPDAWatchEvaluating(refrModel, watch, debugger);
        vl.watchEv = watchEv;
        updateValueFrom(watchEv);
    }

    @Override
    public void unsetChangeListener(Watch watch) {
        synchronized (valueListeners) {
            valueListeners.remove(watch);
        }
    }

    @Override
    public String getEditableValue(Watch watch) {
        ValueListeners vl;
        synchronized (valueListeners) {
            vl = valueListeners.get(watch);
        }
        if (vl == null) {
            return null;
        }
        String valueOnly = vl.valueOnly;
        if (valueOnly == null) {
            return null;
        }
        if (!VariablesTableModel.isReadOnlyVar(vl.watchEv, debugger)) {
            return valueOnly;
        } else {
            return null;
        }
    }

    @Override
    public boolean setValue(final Watch watch, final String value) {
        final ValueListeners vl;
        synchronized (valueListeners) {
            vl = valueListeners.get(watch);
        }
        if (vl == null) {
            return false;
        }
        final String lastValue = vl.value;
        final String lastValueOnly = vl.valueOnly;
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    vl.watchEv.setValue(value);
                    vl.watchEv.setEvaluated(null);
                    //vl.watchEv = new JPDAWatchEvaluating(refrModel, watch, debugger);
                    updateValueFrom(vl.watchEv);
                } catch (InvalidExpressionException ex) {
                    NotifyDescriptor msg = new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(msg);
                    vl.value = lastValue;
                    vl.valueOnly = lastValueOnly;
                    vl.listener.valueChanged(watch);
                }
            }
        });
        vl.value = getEvaluatingText();
        vl.valueOnly = null;
        return true;
    }

    @Override
    public Action[] getHeadActions(Watch watch) {
        if (headAction == null) {
            return null;
        }
        Pair<ObjectVariable, ValueListeners> varVl = getObjectVariable(watch);
        if (varVl == null) {
            return null;
        }
        ObjectVariable expandableVar = varVl.first();
        return new Action[] { new ExpandAction(headAction, watch.getExpression(), expandableVar) };
    }

    @Override
    public Action[] getTailActions(Watch watch) {
        Pair<ObjectVariable, ValueListeners> varVl = getObjectVariable(watch);
        if (varVl == null) {
            return null;
        }
        ObjectVariable var = varVl.first();
        if (!ValuePropertyEditor.hasPropertyEditorFor(var)) {
            return null;
        }
        final Object mirror = var.createMirrorObject();
        if (mirror == null) {
            return null;
        }
        ValuePropertyEditor ped = new ValuePropertyEditor(lookupProvider);
        ped.setValueWithMirror(var, mirror);
        return new Action[] { null, getPropertyEditorAction(ped, var, varVl.second(), watch.getExpression()) };
    }

    private Pair<ObjectVariable, ValueListeners> getObjectVariable(Watch watch) {
        final ValueListeners vl;
        synchronized (valueListeners) {
            vl = valueListeners.get(watch);
        }
        if (vl == null) {
            return null;
        }
        JPDAWatch ew = vl.watchEv.getEvaluatedWatch();
        if (ew == null) {
            return null;
        }
        ObjectVariable ov = null;
        if (ew instanceof ObjectVariable) {
            try {
                Object jdiValue = ew.getClass().getMethod("getJDIValue").invoke(ew);
                if (jdiValue != null) {
                    ov = (ObjectVariable) ew;
                }
            } catch (Exception ex) {}
        }
        if (ov == null) {
            return null;
        } else {
            return Pair.of(ov, vl);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (debugger.getCurrentCallStackFrame() != null) {
            List<ValueListeners> vls;
            synchronized (valueListeners) {
                vls = new ArrayList(valueListeners.values());
            }
            for (ValueListeners vl : vls) {
                vl.watchEv.setEvaluated(null);
                vl.value = getEvaluatingText();
                vl.valueOnly = null;
                vl.listener.valueChanged(vl.watchEv.getWatch());
                refrModel.fireTableValueChangedChanged(vl.watchEv, null);
            }
        } else {
            synchronized (valueListeners) {
                for (Map.Entry<Watch, ValueListeners> wvl : valueListeners.entrySet()) {
                    wvl.getValue().value = null;
                    wvl.getValue().valueOnly = null;
                    wvl.getValue().listener.valueChanged(wvl.getKey());
                }
            }
        }
    }

    private static final RequestProcessor RP = new RequestProcessor(PinWatchValueProvider.class);

    private void updateValueFrom(final JPDAWatchEvaluating watchEv) {
        final ValueListeners vl;
        final Watch watch = watchEv.getWatch();
        synchronized (valueListeners) {
            vl = valueListeners.get(watch);
        }
        if (vl != null) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    watchEv.getValue();
                    JPDAWatch ew = watchEv.getEvaluatedWatch();
                    if (ew == null) {
                        return ;
                    }
                    String type = ew.getType();
                    int ti = type.lastIndexOf('.');
                    if (ti > 0) {
                        type = type.substring(ti + 1);
                    }
                    String value;
                    if (ew instanceof ObjectVariable) {
                        try {
                            value = ((ObjectVariable) ew).getToStringValue();
                        } catch (InvalidExpressionException ex) {
                            value = ew.getValue();
                        }
                    } else {
                        value = ew.getValue();
                    }
                    vl.value = (type.isEmpty() ? "" : "(" + type + ") ") + value;
                    vl.valueOnly = value;
                    vl.listener.valueChanged(watch);
                }
            });
        }
    }

    private final class WatchRefreshModelImpl implements JPDAWatchRefreshModel {

        @Override
        public boolean isLeaf(Object node) throws UnknownTypeException {
            return true;
        }

        @Override
        public void fireTableValueChangedChanged(Object node, String propertyName) {
            JPDAWatchEvaluating watchEv = (JPDAWatchEvaluating) node;
            if (watchEv.isCurrent()) {
                watchEv.setEvaluated(null);
            }
            updateValueFrom(watchEv);
        }

        @Override
        public void fireChildrenChanged(Object node) {
        }

    }

    private static final class ValueListeners {

        volatile String value = null;//PinWatchUISupport.ValueProvider.VALUE_EVALUATING;
        volatile String valueOnly = null;

        ValueChangeListener listener;
        JPDAWatchEvaluating watchEv;

        ValueListeners(ValueChangeListener listener) {
            this.listener = listener;
        }
    }

    private final class ExpandAction implements Action {

        private final Action delegate;
        private final String expression;
        private final ObjectVariable var;

        ExpandAction(Action delegate, String expression, ObjectVariable var) {
            this.delegate = delegate;
            this.expression = expression;
            this.var = var;
        }

        @Override
        public Object getValue(String key) {
            return delegate.getValue(key);
        }

        @Override
        public void putValue(String key, Object value) {
            delegate.putValue(key, value);
        }

        @Override
        public void setEnabled(boolean b) {
            delegate.setEnabled(b);
        }

        @Override
        public boolean isEnabled() {
            return delegate.isEnabled();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            delegate.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            delegate.removePropertyChangeListener(listener);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            delegate.putValue("debugger", debugger);                            // NOI18N
            delegate.putValue("expression", expression);                        // NOI18N
            delegate.putValue("variable", var);                                 // NOI18N
            delegate.actionPerformed(e);
        }

    }

    @NbBundle.Messages({"# {0} - the watched expression", "PropEditDisplayName=Value of {0}"})
    private Action getPropertyEditorAction(final PropertyEditor pe,
                                           final ObjectVariable var,
                                           final ValueListeners vl,
                                           final String expression) {
        Property property = new PropertySupport.ReadWrite(expression, null,
                                                          Bundle.PropEditDisplayName(expression),
                                                          Bundle.PropEditDisplayName(expression)) {
            @Override
            public Object getValue() throws IllegalAccessException, InvocationTargetException {
                return var;
            }
            @Override
            public void setValue(final Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            vl.watchEv.setFromMirrorObject(val);
                            vl.watchEv.setEvaluated(null);
                            updateValueFrom(vl.watchEv);
                        } catch (InvalidObjectException ex) {
                            NotifyDescriptor msg = new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notifyLater(msg);
                        }
                    }
                });
                vl.value = getEvaluatingText();
                vl.valueOnly = null;

            }
            @Override
            public PropertyEditor getPropertyEditor() {
                return pe;
            }
        };
        PropertyPanel pp = new PropertyPanel(property);
        return pp.getActionMap().get("invokeCustomEditor");                     // NOI18N
    }

}
