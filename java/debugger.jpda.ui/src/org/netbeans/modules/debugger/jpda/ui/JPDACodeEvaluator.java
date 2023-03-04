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

package org.netbeans.modules.debugger.jpda.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.CodeEvaluator;
import org.netbeans.spi.debugger.ui.CodeEvaluator.Result.DefaultHistoryItem;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
@CodeEvaluator.EvaluatorService.Registration(path = "netbeans-JPDASession")
public class JPDACodeEvaluator extends CodeEvaluator.EvaluatorService {
    
    private static final Logger LOG = Logger.getLogger(JPDACodeEvaluator.class.getName());
    
    private final JPDADebugger debugger;
    private final DebuggerChangeListener debuggerListener;
    private final CodeEvaluator.DefaultExpressionsHistoryPersistence historyPersistence;
    //private final History history;
    //private HistoryRecord lastEvaluationRecord = null;
    private final RequestProcessor rp;
    private final RequestProcessor.Task evalTask;
    private String expression;
    private CodeEvaluator.Result<Variable, DefaultHistoryItem> result;
    //private Variable result;
    
    public JPDACodeEvaluator(ContextProvider contextProvider) {
        this.debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
        rp = contextProvider.lookupFirst(null, RequestProcessor.class);
        evalTask = rp.create(new EvaluateTask());
        historyPersistence = CodeEvaluator.DefaultExpressionsHistoryPersistence.create("debugger.jpda");
        //history = new History();
        debuggerListener = new DebuggerChangeListener();
        debugger.addPropertyChangeListener(JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, debuggerListener);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_CLASSES_FIXED, debuggerListener);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, debuggerListener);
        result = CodeEvaluator.Result.get(contextProvider.lookupFirst(null, DebuggerEngine.class));
    }

    @Override
    public void setupContext(JEditorPane editorPane, Runnable contextSetUp) {
        WatchPanel.setupContext(editorPane, contextSetUp);
    }

    @Override
    public boolean canEvaluate() {
        return debugger.getCurrentThread() != null &&
               debugger.getState() == JPDADebugger.STATE_STOPPED;
    }

    @Override
    public List<String> getExpressionsHistory() {
        return historyPersistence.getExpressions();
    }

    @Override
    public void evaluate(String expression) {
        this.expression = expression;
        evalTask.schedule(0);
    }
    
    public String getExpression() {
        return expression;
    }
    
    public RequestProcessor getRequestProcessor() {
        return rp;
    }
    
    /*
    private void addResultToHistory(final String expr, Variable result) {
        System.err.println("addResultToHistory("+expr+", "+result+")");
        if (lastEvaluationRecord != null) {
            history.addItem(lastEvaluationRecord.expr, lastEvaluationRecord.type,
                    lastEvaluationRecord.value, lastEvaluationRecord.toString);
        }
        if (result != null) { // 'result' can be null if debugger finishes
            String type = result.getType();
            String value = result.getValue();
            String toString = ""; // NOI18N
            if (result instanceof ObjectVariable) {
                try {
                    toString = ((ObjectVariable) result).getToStringValue ();
                } catch (InvalidExpressionException ex) {
                }
            } else {
                toString = value;
            }
            lastEvaluationRecord = new HistoryRecord(expr, type, value, toString);
        }

        historyPersistence.addExpression(expr);
        firePropertyChange(PROP_EXPRESSIONS_HISTORY, null, null);
    }
    */
    
    private DefaultHistoryItem getHistoryItem(final String expr, final Variable result) {
        if (result != null) {
            String type = result.getType();
            String value = result.getValue();
            String toString = ""; // NOI18N
            if (result instanceof ObjectVariable) {
                try {
                    toString = ((ObjectVariable) result).getToStringValue ();
                } catch (InvalidExpressionException ex) {
                }
            } else {
                toString = value;
            }
            return new DefaultHistoryItem(expr, type, value, toString);
        } else {
            return null;
        }
    }

    //public ArrayList<History.Item> getHistory() {
    //    return history.getItems();
    //}

    //private synchronized TopComponent getResultViewInstance() {
        /** unique ID of <code>TopComponent</code> (singleton) */
    //    TopComponent instance = WindowManager.getDefault().findTopComponent("resultsView"); // NOI18N [TODO]
        // Can be null
    //    return instance;
    //}

    private class EvaluateTask implements Runnable {
        public void run() {
            String exp = getExpression();
            if (exp == null || "".equals(exp)) {
                //System.out.println("Can not evaluate '"+exp+"'");
                return ;
            }
            //System.out.println("evaluate: '"+exp+"'");
            try {
                Variable var = debugger.evaluate(exp);
                //addResultToHistory(exp, var);
                result.setAndOpen(exp, var, getHistoryItem(exp, var));
                historyPersistence.addExpression(exp);
                firePropertyChange(PROP_EXPRESSIONS_HISTORY, null, null);
                //displayResult(var);
            } catch (InvalidExpressionException ieex) {
                String message = ieex.getLocalizedMessage();
                Throwable t = ieex.getTargetException();
                if (t != null && ieex.hasApplicationTarget()) {
                    java.io.StringWriter s = new java.io.StringWriter();
                    java.io.PrintWriter p = new java.io.PrintWriter(s);
                    t.printStackTrace(p);
                    p.close();
                    message += " \n" + s.toString();
                }
                if (message == null || message.trim().isEmpty()) {
                    // Some exceptions do not have a message!
                    // Like ClassNotPreparedException
                    message = t.toString();
                    LOG.log(Level.INFO, message, t);
                }
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(message));
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        //evalDialog.requestFocus();
                        CodeEvaluator.getDefault().requestFocus();
                    }
                });
            }
        }
        
        // TODO: Decide whether the CodeEvaluator will handle the result object
        // ? whether evaluate() returns Object
        // And whether addResultListener(DebuggerEngine engine) will be on CodeEvaluator.
        // Or whether create some helper class, that handles the evaluated result
        // for the given engine and assures that result view opens, etc.
        /*
        private void displayResult(Variable var) {
            this.result = var;
            if (var == null) {
                fireResultChange();
                return ;
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    boolean isMinimized = false;
                    if (preferences.getBoolean("show_evaluator_result", true)) {
                        TopComponent view = WindowManager.getDefault().findTopComponent("localsView"); // NOI18N [TODO]
                        view.open();
                        isMinimized = WindowManager.getDefault().isTopComponentMinimized(view);
                        view.requestActive();
                    } else {
                        if (resultView == null) {
                            resultView = getResultViewInstance();
                        }
                        if (result != null && resultView != null) {
                            resultView.open();
                            isMinimized = WindowManager.getDefault().isTopComponentMinimized(resultView);
                            resultView.requestActive();
                        }
                    }
                    if (!isMinimized) {
                        getInstance().requestActive();
                    }
                    fireResultChange();
                }
            });
        }
        */
    }
    
    private class DebuggerChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME.equals(propertyName) ||
                JPDADebugger.PROP_CLASSES_FIXED.equals(propertyName)) {
                
                firePropertyChange(PROP_CAN_EVALUATE, null, null);
            } else if (JPDADebugger.PROP_STATE.equals(propertyName)) {
                firePropertyChange(PROP_CAN_EVALUATE, null, null);
            }
        }
        
    }
    
    // History ..................................................................
    /*
    public static class History {

        private static final int MAX_ITEMS = 100;

        private ArrayList<Item> historyItems = new ArrayList<Item>();

        private void addItem(String expr, String type, String value, String toString) {
            Item item = new Item(expr, type, value, toString);
            historyItems.add(0, item);
            if (historyItems.size() > MAX_ITEMS) {
                historyItems.remove(MAX_ITEMS);
            }
        }

        public ArrayList<Item> getItems() {
            return historyItems;
        }

        public void clear() {
            historyItems.clear();
        }

        public class Item {
            public String expr;
            public String type;
            public String value;
            public String toString;
            public String tooltip;
            public String exprFormatted;

            Item(String expr, String type, String value, String toString) {
                this.expr = expr;
                this.type = type;
                this.value = value;
                this.toString = toString;
                StringBuffer buf = new StringBuffer();
                buf.append("<html>");
                String text = expr.replaceAll ("&", "&amp;");
                text = text.replaceAll ("<", "&lt;");
                text = text.replaceAll (">", "&gt;");
                text = text.replaceAll ("\n", "<br/>");
                text = text.replaceAll ("\r", "");
                buf.append(text);
                buf.append("</html>");
                this.tooltip = buf.toString();
            }

            @Override
            public String toString() {
                return expr;
            }
        }

    }

    private static class HistoryRecord {
        String expr;
        String type;
        String value;
        String toString;

        HistoryRecord(String expr, String type, String value, String toString) {
            this.expr = expr;
            this.type = type;
            this.value = value;
            this.toString = toString;
        }
    }
    */
}
