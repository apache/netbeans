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

package org.netbeans.modules.javascript.cdtdebug.ui.vars.tooltip;

import java.util.concurrent.CancellationException;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;
import org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject;
import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;
import org.netbeans.modules.javascript.cdtdebug.ui.vars.models.VariablesModel;
import org.netbeans.modules.javascript.cdtdebug.vars.CDTEvaluator;
import org.netbeans.modules.javascript.cdtdebug.vars.EvaluationError;
import org.netbeans.modules.javascript.cdtdebug.vars.Variable;

import org.netbeans.modules.javascript2.debug.ui.tooltip.AbstractJSToolTipAnnotation;
import org.openide.util.Pair;

public class ToolTipAnnotation extends AbstractJSToolTipAnnotation {

    @Override
    protected void handleToolTipClose(DebuggerEngine engine, final ToolTipSupport tts) {
        CDTDebugger debugger = engine.lookupFirst(null, CDTDebugger.class);
        if (debugger == null) {
            return ;
        }
        handleToolTipClose(debugger, tts);
    }

    public static void handleToolTipClose(CDTDebugger debugger, final ToolTipSupport tts) {
        CDTDebugger.Listener listener = new CDTDebugger.Listener() {
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
        CDTDebugger debugger = engine.lookupFirst(null, CDTDebugger.class);
        if (debugger == null || !debugger.isSuspended()) {
            return null;
        }
        try {
            RemoteObject value = CDTEvaluator.evaluate(debugger, expression);
            if (value == null) {
                throw new CancellationException();
            }
            toolTipText = expression + " = " + CDTEvaluator.getStringValue(value);
            if (! VariablesModel.isLeaf2(value)) {
                var = new Variable(expression, value);
            }
        } catch (EvaluationError ex) {
            toolTipText = expression + " = >" + ex.getMessage () + "<";
        }
        return Pair.of(toolTipText, (Object) var);
    }

}
