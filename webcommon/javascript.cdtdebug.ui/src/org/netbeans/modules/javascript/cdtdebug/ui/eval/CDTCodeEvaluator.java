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

package org.netbeans.modules.javascript.cdtdebug.ui.eval;

import java.util.List;
import javax.swing.JEditorPane;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;
import org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject;

import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;
import org.netbeans.modules.javascript.cdtdebug.CDTDebuggerEngineProvider;
import org.netbeans.modules.javascript.cdtdebug.ScriptsHandler;
import org.netbeans.modules.javascript.cdtdebug.ui.EditorUtils;
import org.netbeans.modules.javascript.cdtdebug.vars.CDTEvaluator;
import org.netbeans.modules.javascript.cdtdebug.vars.EvaluationError;
import org.netbeans.modules.javascript.cdtdebug.vars.Variable;

import org.netbeans.modules.javascript2.debug.ui.EditorContextSetter;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.CodeEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.util.RequestProcessor;


@CodeEvaluator.EvaluatorService.Registration(path = CDTDebuggerEngineProvider.ENGINE_NAME)
public class CDTCodeEvaluator extends CodeEvaluator.EvaluatorService {
    private static RequestProcessor rp = new RequestProcessor("Debugger Evaluator", 1);  // NOI18N

    private final CDTDebugger dbg;
    private final CDTContextProvider contextProvider = new CDTContextProvider();
    private final CodeEvaluator.DefaultExpressionsHistoryPersistence historyPersistence;
    private final CodeEvaluator.Result<VarOrError, CodeEvaluator.Result.DefaultHistoryItem> result;
    private final RequestProcessor.Task evalTask = rp.create(new EvaluateTask());
    private String expression;

    public CDTCodeEvaluator(ContextProvider contextProvider) {
        dbg = contextProvider.lookupFirst(null, CDTDebugger.class);
        historyPersistence = CodeEvaluator.DefaultExpressionsHistoryPersistence.create(CDTDebuggerEngineProvider.ENGINE_NAME);
        result = CodeEvaluator.Result.get(contextProvider.lookupFirst(null, DebuggerEngine.class));
    }

    @Override
    public void setupContext(JEditorPane editorPane, Runnable setUpCallback) {
        EditorContextSetter.setContext(editorPane, contextProvider, setUpCallback);
    }

    @Override
    public boolean canEvaluate() {
        return dbg.isSuspended();
    }

    @Override
    public void evaluate(String expression) {
        this.expression = expression;
        evalTask.schedule(0);
    }

    private String getExpression() {
        return expression;
    }

    @Override
    public List<String> getExpressionsHistory() {
        return historyPersistence.getExpressions();
    }

    private class CDTContextProvider implements EditorContextSetter.EditorContextProvider {

        @Override
        public Line.Part getContext() {
            CallFrame frame = dbg.getCurrentFrame();
            ScriptsHandler sh = dbg.getScriptsHandler();
            if (frame != null) {
                FileObject file = sh.getFile(frame.getLocation().getScriptId());
                if (file != null) {
                    int line = (int) frame.getLocation().getLineNumber();
                    if (line < 0) {
                        line = 0;
                    }
                    Integer column = frame.getLocation().getColumnNumber();
                    if (column == null || column < 0) {
                        column = 0;
                    }
                    Line l = EditorUtils.getLine(dbg, file, line, column);
                    if (l != null) {
                        return l.createPart(column, 0);
                    }
                }
            }
            return null;
        }

    }

    private class EvaluateTask implements Runnable {
        @Override
        public void run() {
            String exp = getExpression();
            if (exp == null || "".equals(exp)) {
                return ;
            }
            CallFrame frame = dbg.getCurrentFrame();
            if (frame == null) {
                return ;
            }
            VarOrError voe;
            try {
                RemoteObject value = CDTEvaluator.evaluate(dbg, expression);
                Variable var = new Variable("<EvaluationResult>", value);
                voe = new VarOrError(var);
            } catch (EvaluationError ex) {
                voe = new VarOrError(ex.getMessage());
            }
            result.setAndOpen(exp, voe, getHistoryItem(exp, voe));
            historyPersistence.addExpression(exp);
            firePropertyChange(PROP_EXPRESSIONS_HISTORY, null, null);
        }

        private CodeEvaluator.Result.DefaultHistoryItem getHistoryItem(final String expr, final VarOrError voe) {
            if (voe != null) {
                if (voe.hasVar()) {
                    Variable var = voe.getVar();
                    RemoteObject value = var.getValue();
                    String type = CDTEvaluator.getStringType(value);
                    String stringValue = CDTEvaluator.getStringValue(value);
                    return new CodeEvaluator.Result.DefaultHistoryItem(expr,
                            type,
                            stringValue,
                            stringValue);
                } else {
                    return null; // do not store failed expressions
                }
            } else {
                return null;
            }
        }
    }

}
