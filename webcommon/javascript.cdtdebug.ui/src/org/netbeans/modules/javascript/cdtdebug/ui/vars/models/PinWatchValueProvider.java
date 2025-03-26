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

import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.api.debugger.Watch;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;
import org.netbeans.lib.chrome_devtools_protocol.debugger.EvaluateOnCallFrameRequest;
import org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject;
import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;
import org.netbeans.modules.javascript.cdtdebug.CDTDebuggerEngineProvider;
import org.netbeans.modules.javascript.cdtdebug.ui.vars.tooltip.ToolTipAnnotation;
import org.netbeans.modules.javascript.cdtdebug.vars.CDTEvaluator;
import org.netbeans.modules.javascript.cdtdebug.vars.Variable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.AbstractExpandToolTipAction;
import org.netbeans.spi.debugger.ui.PinWatchUISupport;


@DebuggerServiceRegistration(path = CDTDebuggerEngineProvider.ENGINE_NAME, types = PinWatchUISupport.ValueProvider.class)
public class PinWatchValueProvider implements PinWatchUISupport.ValueProvider,
                                              CDTDebugger.Listener {

    private final CDTDebugger dbg;
    private final Map<Watch, ValueListeners> valueListeners = new HashMap<>();

    public PinWatchValueProvider(ContextProvider lookupProvider) {
        this.dbg = lookupProvider.lookupFirst(null, CDTDebugger.class);
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
//            if (vl.value != null && !VariablesModel.isReadOnly(vl.value)) {
//                return vl.valueOnlyString;
//            } else {
                return null;
//            }
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
        RemoteObject value = vl.value;
        if (value == null || VariablesModel.isLeaf2(value)) {
            return null;
        }
        Variable var = new Variable(watch.getExpression(), value);
        return new Action[] { new ExpandAction(var) };
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

    @Override
    public void notifySuspended(boolean suspended) {
    }

    @Override
    public void notifyCurrentFrame(CallFrame cf) {
        if (cf != null) {
            synchronized (valueListeners) {
                for (Map.Entry<Watch, ValueListeners> wvl : valueListeners.entrySet()) {
                    ValueListeners vl = wvl.getValue();
                    vl.value = null;
                    vl.valueString = getEvaluatingText();
                    vl.valueOnlyString = null;
                    Watch w = wvl.getKey();
                    vl.listener.valueChanged(w);
                    updateValue(w);
                }
            }
        } else {
            synchronized (valueListeners) {
                for (Map.Entry<Watch, ValueListeners> wvl : valueListeners.entrySet()) {
                    ValueListeners vl = wvl.getValue();
                    vl.value = null;
                    vl.valueString = null;
                    vl.valueOnlyString = null;
                    vl.listener.valueChanged(wvl.getKey());
                }
            }
        }
    }

    @Override
    public void notifyFinished() {
    }

    void updateValue(final Watch watch) {
        if (!dbg.isSuspended()) {
            return;
        }
        dbg.getConnection()
                .getDebugger()
                .evaluateOnCallFrame(new EvaluateOnCallFrameRequest(dbg.getCurrentFrame().getCallFrameId(), watch.getExpression()))
                .handle((eocfr, thr) -> {
                    ValueListeners vl;
                    synchronized (valueListeners) {
                        vl = valueListeners.get(watch);
                    }
                    if (vl != null && eocfr != null && eocfr.getResult() != null) {
                        vl.value = eocfr.getResult();
                        String strValue = CDTEvaluator.getStringValue(vl.value);
                        vl.valueString = strValue;
                        vl.valueOnlyString = strValue;
                        vl.listener.valueChanged(watch);
                    }
                    return null;
                });
    }

    private static final class ValueListeners {

        volatile String valueString = null;
        volatile RemoteObject value = null;
        volatile String valueOnlyString = null;

        ValueChangeListener listener;

        ValueListeners(ValueChangeListener listener) {
            this.listener = listener;
        }
    }

    private class ExpandAction extends AbstractExpandToolTipAction {

        private final Variable var;

        ExpandAction(Variable var) {
            this.var = var;
        }

        @Override
        protected void openTooltipView() {
            ToolTipSupport tts = openTooltipView(var.getName(), var);
            if (tts != null) {
                ToolTipAnnotation.handleToolTipClose(dbg, tts);
            }
        }

    }

}
