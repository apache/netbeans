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
package org.netbeans.modules.web.javascript.debugger.annotation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.api.debugger.Watch;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.web.javascript.debugger.eval.Evaluator;
import org.netbeans.modules.web.javascript.debugger.locals.VariablesModel;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject.Type;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.AbstractExpandToolTipAction;
import org.netbeans.spi.debugger.ui.PinWatchUISupport;
import org.openide.util.RequestProcessor;

/**
 *
 * @author martin
 */
@DebuggerServiceRegistration(path = "javascript-debuggerengine", types = PinWatchUISupport.ValueProvider.class)
public class PinWatchValueProvider implements PinWatchUISupport.ValueProvider,
                                              Debugger.Listener {
    
    private static final RequestProcessor RP = new RequestProcessor(PinWatchValueProvider.class);
    private final Debugger dbg;
    private final Map<Watch, ValueListeners> valueListeners = new HashMap<>();
    
    public PinWatchValueProvider(ContextProvider lookupProvider) {
        dbg = lookupProvider.lookupFirst(null, Debugger.class);
        dbg.addListener(this);
    }

    @Override
    public String getId() {
        return "org.netbeans.modules.javascript2.debug.PIN_VALUE_PROVIDER";     // NOI18N
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
            return vl.valueString;
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
        } else {
            return vl.valueOnlyString;
        }
    }

    @Override
    public Action[] getHeadActions(Watch watch) {
        ValueListeners vl;
        synchronized (valueListeners) {
            vl = valueListeners.get(watch);
        }
        if (vl == null) {
            return null;
        }
        VariablesModel.ScopedRemoteObject sr = vl.value;
        if (sr == null || !vl.hasChildren) {
            return null;
        }
        return new Action[] { new ExpandAction(sr, watch.getExpression()) };
    }

    @Override
    public void setChangeListener(Watch watch, ValueChangeListener chl) {
        ValueListeners vl = new ValueListeners(chl);
        synchronized (valueListeners) {
            valueListeners.put(watch, vl);
        }
        updateValue(watch);
    }

    @Override
    public void unsetChangeListener(Watch watch) {
        synchronized (valueListeners) {
            valueListeners.remove(watch);
        }
    }
    
    void updateValue(final Watch watch) {
        RP.post(() -> {
            if (!dbg.isSuspended()) {
                return ;
            }
            CallFrame currentCallFrame = dbg.getCurrentCallFrame();
            if (currentCallFrame == null) {
                return ;
            }
            VariablesModel.ScopedRemoteObject sv = Evaluator.evaluateExpression(currentCallFrame, watch.getExpression(), true);
            if (sv != null) {
                ValueListeners vl;
                synchronized (valueListeners) {
                    vl = valueListeners.get(watch);
                }
                if (vl != null) {
                    RemoteObject var = sv.getRemoteObject();
                    String value = ToolTipAnnotation.getStringValue(var);
                    Type type = var.getType();
                    if (type == Type.OBJECT) {
                        vl.value = sv;
                        // TODO: add obj ID
                        vl.hasChildren = !var.getProperties().isEmpty();
                    }
                    if (type != Type.UNDEFINED) {
                        vl.valueString = value;
                        if (type != Type.OBJECT && type != Type.FUNCTION) {
                            vl.valueOnlyString = var.getValueAsString();
                        }
                    } else {
                        vl.valueString = var.getDescription();
                    }
                    vl.listener.valueChanged(watch);
                }
            }
        });
    }

    @Override
    public void paused(List<CallFrame> callStack, String reason) {
        synchronized (valueListeners) {
            for (Map.Entry<Watch, ValueListeners> wvl : valueListeners.entrySet()) {
                ValueListeners vl = wvl.getValue();
                vl.value = null;
                vl.hasChildren = false;
                vl.valueString = getEvaluatingText();
                vl.valueOnlyString = null;
                Watch w = wvl.getKey();
                vl.listener.valueChanged(w);
                updateValue(w);
            }
        }
    }

    @Override
    public void resumed() {
        synchronized (valueListeners) {
            for (Map.Entry<Watch, ValueListeners> wvl : valueListeners.entrySet()) {
                ValueListeners vl = wvl.getValue();
                vl.value = null;
                vl.hasChildren = false;
                vl.valueString = null;
                vl.valueOnlyString = null;
                vl.listener.valueChanged(wvl.getKey());
            }
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void enabled(boolean enabled) {
    }

    private static final class ValueListeners {

        volatile String valueString = null;
        volatile VariablesModel.ScopedRemoteObject value = null;
        volatile String valueOnlyString = null;
        volatile boolean hasChildren = false;

        ValueChangeListener listener;

        ValueListeners(ValueChangeListener listener) {
            this.listener = listener;
        }
    }

    private class ExpandAction extends AbstractExpandToolTipAction {

        private final VariablesModel.ScopedRemoteObject sr;
        private final String expression;

        ExpandAction(VariablesModel.ScopedRemoteObject sr, String expression) {
            this.sr = sr;
            this.expression = expression;
        }

        @Override
        protected void openTooltipView() {
            ToolTipSupport tts = openTooltipView(expression, sr);
            if (tts != null) {
                ToolTipAnnotation.handleToolTipClose(dbg, tts);
            }
        }

    }

}
