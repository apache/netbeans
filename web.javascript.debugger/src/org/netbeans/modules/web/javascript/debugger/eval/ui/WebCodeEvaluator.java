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
                RemoteObject var = ((ScopedRemoteObject) result).getRemoteObject();
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
