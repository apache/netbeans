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
package org.netbeans.modules.debugger.jpda.projectsui;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.Action;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.AbstractExpandToolTipAction;

/**
 *
 * @author martin
 */
@DebuggerServiceRegistration(path = "netbeans-JPDASession/PinWatchHeadActions", types = Action.class)
public class PinWatchExpandAction extends AbstractExpandToolTipAction {

    private Reference<JPDADebugger> debuggerRef;
    private String expression;
    private Reference<ObjectVariable> varRef;

    public PinWatchExpandAction() {
    }

    @Override
    public void putValue(String key, Object value) {
        switch (key) {
            case "debugger":
                synchronized (this) {
                    if (debuggerRef == null || debuggerRef.get() != value) {
                        debuggerRef = new WeakReference<>((JPDADebugger) value);
                    }
                }
                break;
            case "expression":
                synchronized (this) {
                    expression = (String) value;
                }
                break;
            case "variable":
                synchronized (this) {
                    if (varRef == null || varRef.get() != value) {
                        varRef = new WeakReference<>((ObjectVariable) value);
//                        expanded = false;
                    }
                }
                break;
            case "disposeState":
                synchronized (this) {
                    debuggerRef = null;
                    expression = null;
                    varRef = null;
                }
            default:
                super.putValue(key, value);
        }
    }

    @Override
    protected void openTooltipView() {
        JPDADebugger debugger = null;
        String exp;
        ObjectVariable var = null;
        synchronized (this) {
            if (debuggerRef != null) {
                debugger = debuggerRef.get();
            }
            exp = expression;
            if (varRef != null) {
                var = varRef.get();
            }
        }
        if (debugger != null && exp != null && var != null) {
            ToolTipSupport tts = openTooltipView(expression, var);
            if (tts != null) {
                DebuggerStateChangeListener.attach(debugger, tts);
            }
        }
    }

}
