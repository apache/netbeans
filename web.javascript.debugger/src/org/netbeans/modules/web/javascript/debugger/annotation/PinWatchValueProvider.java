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
