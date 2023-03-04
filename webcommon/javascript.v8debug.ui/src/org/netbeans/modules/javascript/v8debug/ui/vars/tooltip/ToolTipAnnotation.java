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

package org.netbeans.modules.javascript.v8debug.ui.vars.tooltip;

import java.util.concurrent.CancellationException;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.vars.V8Object;
import org.netbeans.lib.v8debug.vars.V8Value;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript.v8debug.vars.EvaluationError;
import org.netbeans.modules.javascript.v8debug.vars.V8Evaluator;
import org.netbeans.modules.javascript.v8debug.vars.Variable;
import org.netbeans.modules.javascript.v8debug.ui.vars.models.VariablesModel;
import org.netbeans.modules.javascript2.debug.ui.tooltip.AbstractJSToolTipAnnotation;
import org.openide.util.Pair;

/**
 *
 * @author Martin Entlicher
 */
public class ToolTipAnnotation extends AbstractJSToolTipAnnotation {

    @Override
    protected void handleToolTipClose(DebuggerEngine engine, final ToolTipSupport tts) {
        V8Debugger debugger = engine.lookupFirst(null, V8Debugger.class);
        if (debugger == null) {
            return ;
        }
        handleToolTipClose(debugger, tts);
    }
    
    public static void handleToolTipClose(V8Debugger debugger, final ToolTipSupport tts) {
        V8Debugger.Listener listener = new V8Debugger.Listener() {
            @Override
            public void notifySuspended(boolean suspended) {
                if (!suspended) {
                    doClose();
                }
            }

            @Override
            public void notifyCurrentFrame(CallFrame cf) {
                doClose();
            }

            @Override
            public void notifyFinished() {
                doClose();
            }
            
            private void doClose() {
                SwingUtilities.invokeLater(() ->
                    tts.setToolTipVisible(false)
                );
            }
        };
        debugger.addListener(listener);
        tts.addPropertyChangeListener(pl -> {
            if (ToolTipSupport.PROP_STATUS.equals(pl.getPropertyName()) &&
                    !tts.isToolTipVisible()) {
                debugger.removeListener(listener);
            }
        });
    }
    
    @Override
    protected Pair<String, Object> evaluate(String expression, DebuggerEngine engine) throws CancellationException {
        String toolTipText;
        Variable var = null;
        V8Debugger debugger = engine.lookupFirst(null, V8Debugger.class);
        if (debugger == null || !debugger.isSuspended()) {
            return null;
        }
        try {
            V8Value value = V8Evaluator.evaluate(debugger, expression);
            if (value == null) {
                throw new CancellationException();
            }
            toolTipText = expression + " = " + V8Evaluator.getStringValue(value);
            if (VariablesModel.hasChildren(value)) {
                var = new Variable(Variable.Kind.LOCAL, expression, value.getHandle(), value, false);
            }
        } catch (EvaluationError ex) {
            toolTipText = expression + " = >" + ex.getMessage () + "<";
        }
        return Pair.of(toolTipText, (Object) var);
    }
    
}
