/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.v8debug.ui.eval;

import java.util.List;
import javax.swing.JEditorPane;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.lib.v8debug.vars.V8Value;
import org.netbeans.modules.javascript.v8debug.ui.EditorUtils;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.V8DebuggerEngineProvider;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript.v8debug.vars.EvaluationError;
import org.netbeans.modules.javascript.v8debug.vars.V8Evaluator;
import org.netbeans.modules.javascript.v8debug.vars.Variable;
import org.netbeans.modules.javascript2.debug.ui.EditorContextSetter;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.CodeEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
@CodeEvaluator.EvaluatorService.Registration(path = V8DebuggerEngineProvider.ENGINE_NAME)
public class V8CodeEvaluator extends CodeEvaluator.EvaluatorService {

    private final V8Debugger dbg;
    private final V8ContextProvider contextProvider = new V8ContextProvider();
    private final CodeEvaluator.DefaultExpressionsHistoryPersistence historyPersistence;
    private String expression;
    private static RequestProcessor rp = new RequestProcessor("Debugger Evaluator", 1);  // NOI18N
    private final RequestProcessor.Task evalTask = rp.create(new EvaluateTask());
    private CodeEvaluator.Result<VarOrError, CodeEvaluator.Result.DefaultHistoryItem> result;
    
    public V8CodeEvaluator(ContextProvider contextProvider) {
        dbg = contextProvider.lookupFirst(null, V8Debugger.class);
        historyPersistence = CodeEvaluator.DefaultExpressionsHistoryPersistence.create(V8DebuggerEngineProvider.ENGINE_NAME);
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
    
    private class V8ContextProvider implements EditorContextSetter.EditorContextProvider {

        @Override
        public Line.Part getContext() {
            CallFrame frame = dbg.getCurrentFrame();
            if (frame != null) {
                V8Script script = frame.getScript();
                if (script != null) {
                    FileObject file = dbg.getScriptsHandler().getFile(script);
                    V8Frame f = frame.getFrame();
                    int line = (int) f.getLine();
                    if (line < 0) {
                        line = 0;
                    }
                    int column = (int) f.getColumn();
                    if (column < 0) {
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
                //System.out.println("Can not evaluate '"+exp+"'");
                return ;
            }
            //System.out.println("evaluate: '"+exp+"'");
            CallFrame frame = dbg.getCurrentFrame();
            if (frame == null) {
                return ;
            }
            VarOrError voe;
            try {
                V8Value value = V8Evaluator.evaluate(dbg, expression);
                Variable var = new Variable(Variable.Kind.LOCAL, expression, value.getHandle(), value, false);
                voe = new VarOrError(var);
            } catch (EvaluationError ex) {
                voe = new VarOrError(ex);
            }
            result.setAndOpen(exp, voe, getHistoryItem(exp, voe));
            historyPersistence.addExpression(exp);
            firePropertyChange(PROP_EXPRESSIONS_HISTORY, null, null);
        }
        
        private CodeEvaluator.Result.DefaultHistoryItem getHistoryItem(final String expr, final VarOrError voe) {
            if (voe != null) {
                String err;
                if (voe.hasVar()) {
                    Variable var = voe.getVar();
                    try {
                        V8Value value = var.getValue();
                        String type = V8Evaluator.getStringType(value);
                        String stringValue = V8Evaluator.getStringValue(value);
                        return new CodeEvaluator.Result.DefaultHistoryItem(expr,
                                                                           type,
                                                                           stringValue,
                                                                           stringValue);
                    } catch (EvaluationError ee) {
                        err = ee.getLocalizedMessage();
                    }
                } else {
                    //err = voe.getError();
                    return null; // do not store failed expressions
                }
                return new CodeEvaluator.Result.DefaultHistoryItem(expr, "", err, err);
            } else {
                return null;
            }
        }
    }
    
}
