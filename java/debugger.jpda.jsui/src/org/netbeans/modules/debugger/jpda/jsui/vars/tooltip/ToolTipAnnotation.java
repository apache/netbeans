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

package org.netbeans.modules.debugger.jpda.jsui.vars.tooltip;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CancellationException;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.modules.debugger.jpda.js.vars.DebuggerSupport;
import org.netbeans.modules.debugger.jpda.js.vars.JSVariable;
import org.netbeans.modules.javascript2.debug.ui.tooltip.AbstractJSToolTipAnnotation;
import org.openide.util.Pair;

/**
 *
 * @author Martin
 */
public final class ToolTipAnnotation extends AbstractJSToolTipAnnotation {
    
    @Override
    protected void handleToolTipClose(DebuggerEngine engine, ToolTipSupport tts) {
        JPDADebugger d = engine.lookupFirst(null, JPDADebugger.class);
        if (d == null) {
            return ;
        }
        PropertyChangeListener l = (PropertyChangeEvent evt) -> {
            int state = ((Integer) evt.getNewValue());
            if (JPDADebugger.STATE_DISCONNECTED == state ||
                    JPDADebugger.STATE_RUNNING == state) {
                SwingUtilities.invokeLater(() ->
                        tts.setToolTipVisible(false)
                );
            }
        };
        d.addPropertyChangeListener(JPDADebugger.PROP_STATE, l);
        tts.addPropertyChangeListener(pl -> {
            if (ToolTipSupport.PROP_STATUS.equals(pl.getPropertyName()) &&
                    !tts.isToolTipVisible()) {
                d.removePropertyChangeListener(JPDADebugger.PROP_STATE, l);
            }
        });
    }
    
    @Override
    protected Pair<String, Object> evaluate(String expression, DebuggerEngine engine) throws CancellationException {
        Session session = engine.lookupFirst(null, Session.class);
        if (engine != session.getEngineForLanguage(JSUtils.JS_STRATUM)) {
            return null;
        }
        JPDADebugger d = engine.lookupFirst(null, JPDADebugger.class);
        if (d == null) {
            return null;
        }
        CallStackFrame frame = d.getCurrentCallStackFrame();
        if (frame == null) {
            return null;
        }
        String toolTipText;
        JSVariable jsresult = null;
        try {
            Variable result = DebuggerSupport.evaluate(d, frame, expression);
            if (result == null) {
                throw new CancellationException();
            }
            if (result instanceof ObjectVariable) {
                jsresult = JSVariable.createIfScriptObject(d, (ObjectVariable) result, expression);
            }
            if (jsresult != null) {
                toolTipText = expression + " = " + jsresult.getValue();
            } else {
                toolTipText = expression + " = " + DebuggerSupport.getVarValue(d, result);
            }
        } catch (InvalidExpressionException ex) {
            toolTipText = expression + " = >" + ex.getMessage () + "<";
        }
        return Pair.of(toolTipText, (Object) jsresult);
    }
    
}
