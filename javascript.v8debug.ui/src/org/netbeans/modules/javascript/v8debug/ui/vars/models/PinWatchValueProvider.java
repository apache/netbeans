/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.v8debug.ui.vars.models;

import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.api.debugger.Watch;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.commands.Evaluate;
import org.netbeans.lib.v8debug.vars.V8Value;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.V8DebuggerEngineProvider;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript.v8debug.ui.vars.tooltip.ToolTipAnnotation;
import org.netbeans.modules.javascript.v8debug.vars.V8Evaluator;
import org.netbeans.modules.javascript.v8debug.vars.Variable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.AbstractExpandToolTipAction;
import org.netbeans.spi.debugger.ui.PinWatchUISupport;
import org.openide.util.RequestProcessor;

/**
 *
 * @author martin
 */
@DebuggerServiceRegistration(path = V8DebuggerEngineProvider.ENGINE_NAME, types = PinWatchUISupport.ValueProvider.class)
public class PinWatchValueProvider implements PinWatchUISupport.ValueProvider,
                                              V8Debugger.Listener {

    private static final RequestProcessor RP = new RequestProcessor(PinWatchValueProvider.class);
    private final V8Debugger dbg;
    private final Map<Watch, ValueListeners> valueListeners = new HashMap<>();

    public PinWatchValueProvider(ContextProvider lookupProvider) {
        this.dbg = lookupProvider.lookupFirst(null, V8Debugger.class);
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
            if (vl.value != null && !VariablesModel.isReadOnly(vl.value)) {
                return vl.valueOnlyString;
            } else {
                return null;
            }
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
        V8Value value = vl.value;
        if (value == null || !VariablesModel.hasChildren(value)) {
            return null;
        }
        Variable var = new Variable(Variable.Kind.LOCAL, watch.getExpression(), value.getHandle(), value, false);
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
        RP.post(() -> {
            if (!dbg.isSuspended()) {
                return ;
            }
            WatchesModel.WatchCB wcb = new WatchesModel.WatchCB();
            V8Request request = dbg.sendCommandRequest(V8Command.Evaluate,
                                                       new Evaluate.Arguments(watch.getExpression()), wcb);
            if (request != null) {
                V8Value value = wcb.getValue();
                if (value != null) {
                    ValueListeners vl;
                    synchronized (valueListeners) {
                        vl = valueListeners.get(watch);
                    }
                    if (vl != null) {
                        vl.value = value;
                        String strValue = V8Evaluator.getStringValue(value);
                        //String type = V8Evaluator.getStringType(value);
                        vl.valueString = strValue;
                        vl.valueOnlyString = strValue;
                        vl.listener.valueChanged(watch);
                    }
                }
            }
        });
    }

    private static final class ValueListeners {

        volatile String valueString = null;
        volatile V8Value value = null;
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
