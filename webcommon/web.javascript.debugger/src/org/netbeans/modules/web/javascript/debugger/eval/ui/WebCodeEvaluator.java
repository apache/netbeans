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

package org.netbeans.modules.web.javascript.debugger.eval.ui;

import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JEditorPane;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;
import org.netbeans.modules.web.javascript.debugger.locals.VariablesModel;
import org.netbeans.modules.web.javascript.debugger.locals.VariablesModel.ScopedRemoteObject;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.CodeEvaluator;
import static org.netbeans.spi.debugger.ui.CodeEvaluator.EvaluatorService.PROP_EXPRESSIONS_HISTORY;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
@CodeEvaluator.EvaluatorService.Registration(path = "javascript-debuggerengine")
public class WebCodeEvaluator extends CodeEvaluator.EvaluatorService {
    
    private final Debugger debugger;
    private final CodeEvaluator.DefaultExpressionsHistoryPersistence historyPersistence;
    private String expression;
    private static RequestProcessor rp = new RequestProcessor("Debugger Evaluator", 1);  // NOI18N
    private final RequestProcessor.Task evalTask = rp.create(new EvaluateTask());
    private CodeEvaluator.Result<ScopedRemoteObject, CodeEvaluator.Result.DefaultHistoryItem> result;
    
    public WebCodeEvaluator(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, Debugger.class);
        historyPersistence = CodeEvaluator.DefaultExpressionsHistoryPersistence.create("javascript-debuggerengine");
        result = CodeEvaluator.Result.get(contextProvider.lookupFirst(null, DebuggerEngine.class));
    }

    @Override
    public void setupContext(JEditorPane editorPane, Runnable contextSetUp) {
        MiscEditorUtil.setupContext(editorPane, contextSetUp);
    }

    @Override
    public boolean canEvaluate() {
        return debugger.isEnabled() && debugger.isSuspended() &&
               debugger.getCurrentCallFrame() != null;
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
    
    private class EvaluateTask implements Runnable {
        @Override
        public void run() {
            String exp = getExpression();
            if (exp == null || "".equals(exp)) {
                //System.out.println("Can not evaluate '"+exp+"'");
                return ;
            }
            //System.out.println("evaluate: '"+exp+"'");
            //try {
                CallFrame frame = debugger.getCurrentCallFrame();
                if (frame != null) {
                    RemoteObject ro = frame.evaluate(exp);
                    ScopedRemoteObject var;
                    if (ro != null) {
                        var = new ScopedRemoteObject(ro, exp, VariablesModel.ViewScope.LOCAL);
                    } else {
                        var = null;
                    }
                    //addResultToHistory(exp, var);
                    //displayResult(var);
                    result.setAndOpen(exp, var, getHistoryItem(exp, var));
                    historyPersistence.addExpression(exp);
                    firePropertyChange(PROP_EXPRESSIONS_HISTORY, null, null);
                }
        }
        
        private CodeEvaluator.Result.DefaultHistoryItem getHistoryItem(final String expr, final ScopedRemoteObject result) {
            if (result != null) {
                RemoteObject var = result.getRemoteObject();
                RemoteObject.Type type = var.getType();
                String typeStr;
                if (type == RemoteObject.Type.OBJECT) {
                    String clazz = var.getClassName();
                    if (clazz == null) {
                        typeStr = type.getName();
                    } else {
                        typeStr = clazz;
                    }
                } else {
                    typeStr = type.getName();
                }
                String value = var.getValueAsString();
                if (value.isEmpty()) {
                    if (type == RemoteObject.Type.OBJECT ||
                        type == RemoteObject.Type.FUNCTION) {
                        
                        value = var.getDescription();
                    }
                }
                String toString = var.getValueAsString();
                return new CodeEvaluator.Result.DefaultHistoryItem(expr, typeStr, value, toString);
            } else {
                return null;
            }
        }
    }
}
