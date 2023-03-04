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

package org.netbeans.spi.debugger.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.debugger.ui.eval.CodeEvaluatorUI;
import org.netbeans.modules.debugger.ui.views.VariablesViewButtons;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Code evaluator UI. Use this to access and manage a component for code
 * evaluations and handle it's result.
 * 
 * @author Martin Entlicher
 * @since 2.49
 */
public final class CodeEvaluator {
    
    private static final CodeEvaluator INSTANCE = new CodeEvaluator();
    
    private CodeEvaluator() {
    }
    
    /**
     * Get the default instance of the code evaluator.
     * @return The code evaluator instance.
     */
    public static CodeEvaluator getDefault() {
        return INSTANCE;
    }
    
    /**
     * Open the evaluator UI.
     */
    public void open() {
        CodeEvaluatorUI.openEvaluator();
    }
    
    /**
     * Set an expression to the evaluator UI.
     * @param expression The expression to set to the evaluator UI.
     */
    public void setExpression(String expression) {
        CodeEvaluatorUI.getInstance().pasteExpression(expression);
    }
    
    /**
     * Request focus to the evaluator UI.
     */
    public void requestFocus() {
        CodeEvaluatorUI.getInstance().requestFocusInWindow();
    }
    
    /**
     * Service handling evaluations in the code evaluator.
     * Register an implementation of this class via {@link Registration} annotation.
     */
    public abstract static class EvaluatorService {
        
        /**
         * Property name fired in order to refresh the evaluate button state.
         */
        public static final String PROP_CAN_EVALUATE = "canEvaluate";           // NOI18N
        /**
         * Property name fired in order to refresh the list of expressions history.
         */
        public static final String PROP_EXPRESSIONS_HISTORY = "expressionsHistory"; // NOI18N
        
        private final PropertyChangeSupport pchs = new PropertyChangeSupport(this);
        
        /**
         * Perform setup of the editor pane of the code evaluator.
         * Typically perform binding for code completion.
         * @param editorPane
         * @param setUpCallback call this back when the setup is finished.
         *                      As the setup may require asynchronous calls,
         *                      this is necessary to notify the infrastructure
         *                      that the code editor is set up.
         */
        public abstract void setupContext(JEditorPane editorPane, Runnable setUpCallback);
        
        /**
         * Provide status whether the service is ready to evaluate code now.
         * Fire {@link #PROP_CAN_EVALUATE} when this status changes.
         * @return <code>true</code> when code evaluation is possible (e.g. the debugger is suspended),
         * <code>false</code> otherwise.
         */
        public abstract boolean canEvaluate();
        
        /**
         * Evaluate the given expression.
         * The evaluation typically should be performed asynchronously, and the
         * result is then set to {@link Result} or any other custom result handler.
         * @param expression The expression to evaluate.
         */
        public abstract void evaluate(String expression);
        
        /**
         * Get the historical evaluated expressions. Items provided by this list
         * are accessible from the code evaluator UI.
         * @return The list of evaluated expressions.
         */
        public abstract List<String> getExpressionsHistory();
        
        /**
         * Fire a property change event.
         * @param propertyName The property name, one of PROP_* constants.
         * @param oldValue An old value
         * @param newValue A new value
         */
        protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            pchs.firePropertyChange(propertyName, oldValue, newValue);
        }
        
        /**
         * Add a property change listener to be notified about property change events.
         * @param l the property change listener
         */
        public final void addPropertyChangeListener(PropertyChangeListener l) {
            pchs.addPropertyChangeListener(l);
        }
        
        /**
         * Remove a property change listener.
         * @param l  the property change listener
         */
        public final void removePropertyChangeListener(PropertyChangeListener l) {
            pchs.removePropertyChangeListener(l);
        }
        
        /**
         * Declarative registration of a EvaluatorService implementation.
         * By marking the implementation class with this annotation,
         * you automatically register that implementation for use by the debugger evaluator.
         * The class must be public and have a public constructor which takes
         * no arguments or takes {@link ContextProvider} as an argument.
         *
         * @author Martin Entlicher
         */
        @Retention(RetentionPolicy.SOURCE)
        @Target({ElementType.TYPE})
        public @interface Registration {
            /**
             * The path to register this implementation in.
             * Usually the session or engine ID.
             */
            String path();
            
            /**
             * An optional position in which to register this service relative to others.
             * Lower-numbered services are returned in the lookup result first.
             * Services with no specified position are returned last.
             */
            int position() default Integer.MAX_VALUE;

        }
    }
    
    /**
     * Default implementation of expressions history persistence.
     * Can be used by {@link EvaluatorService#getExpressionsHistory()}.
     */
    public static final class DefaultExpressionsHistoryPersistence {
        
        private static final int NUM_HISTORY_ITEMS = 20;
        
        private final String engineName;
        private ArrayList<String> editItemsList;
        private Set<String> editItemsSet;
        
        private DefaultExpressionsHistoryPersistence(String engineName) {
            this.engineName = engineName;
            getExpressions();
        }
        
        /**
         * Create a new instance of default expressions history persistence.
         * @param engineName The name of the persistence storage. Usually the engine name to be unique.
         * @return A new instance of default expressions history persistence.
         */
        public static DefaultExpressionsHistoryPersistence create(String engineName) {
            return new DefaultExpressionsHistoryPersistence(engineName);
        }
        
        /**
         * Get the list of stored expressions.
         * @return the list of stored expressions.
         */
        public synchronized List<String> getExpressions() {
            if (editItemsList == null) {
                editItemsList = (ArrayList<String>)
                    Properties.getDefault().getProperties(engineName).
                    getCollection("EvaluatorItems", new ArrayList());           // NOI18N
                editItemsSet = new HashSet<String>(editItemsList);
            }
            return editItemsList;
        }
        
        /**
         * Add a new expression to the history.
         * @param expression a new expression to store.
         */
        public synchronized void addExpression(String expression) {
            expression = expression.trim();
            //getExpressions(); // To asure that we're initialized.
            if (editItemsSet.contains(expression)) {
                editItemsList.remove(expression);
                editItemsList.add(0, expression);
            } else {
                editItemsList.add(0, expression);
                editItemsSet.add(expression);
                if (editItemsList.size() > NUM_HISTORY_ITEMS) {
                    String removed = editItemsList.remove(editItemsList.size() - 1);
                    editItemsSet.remove(removed);
                }
            }
            storeEditItems(editItemsList);
        }
        
        private void storeEditItems(ArrayList<String> items) {
            Properties.getDefault().getProperties(engineName).
                    setCollection("EvaluatorItems", items);                     // NOI18N
        }
    }
    
    /**
     * A helper class managing the evaluator result. Use it when "resultsView"
     * is used to visualize the result of evaluation.
     * @param <R> The type of the result object
     * @param <H> The type of the result history item ({@link DefaultHistoryItem} can be used).
     */
    public static final class Result<R, H> {
        
        // Do not use WeakSet, because the value references to the key.
        private static final Map<Integer, List<WeakReference<Result>>> ENGINE_HASH_MAP = new HashMap<Integer, List<WeakReference<Result>>>();
        
        private final Preferences preferences = NbPreferences.forModule(ContextProvider.class).node(VariablesViewButtons.PREFERENCES_NAME);
        private final DebuggerEngine engine;

        private TopComponent resultView;
        private volatile String expression;
        private volatile R result;
        private final List<H> historyItems = new ArrayList<H>();
        private final List<H> historyItemsRO = Collections.unmodifiableList(historyItems);
        private H lastHistoryItem;
        private int maxHistoryItems = 100;
        private final Set<Listener<R>> listeners = new CopyOnWriteArraySet<Listener<R>>();
        
        private Result(DebuggerEngine engine) {
            this.engine = engine;
        }
        
        /**
         * Get an instance of Result for the given {@link DebuggerEngine}.
         * A new instance is created if there is not one available, an existing
         * instance is returned otherwise.
         * @param <R> The type of the result object
         * @param <H> The type of the result history item
         * @param engine The debugger engine
         * @return an instance of Result container.
         */
        public static <R, H> Result<R, H> get(DebuggerEngine engine) {
            int hc = engine.hashCode();
            synchronized (ENGINE_HASH_MAP) {
                List<WeakReference<Result>> elist = ENGINE_HASH_MAP.get(hc);
                if (elist == null) {
                    elist = new LinkedList<WeakReference<Result>>();
                    ENGINE_HASH_MAP.put(hc, elist);
                }
                //Result r = null;
                for (int i = 0; i < elist.size(); i++) {
                    WeakReference<Result> wr = elist.get(i);
                    Result r = wr.get();
                    if (r == null) {
                        elist.remove(wr);
                        i--;
                        continue;
                    }
                    if (engine == r.engine) {
                        return r;
                    }
                }
                Result<R, H> r = new Result(engine);
                elist.add(new WeakReference<Result>(r));
                return r;
            }
        }
        
        /**
         * Set the maximum number of result history items being held.
         * By default this is 100.
         * @param maxHistoryItems maximum number of history items
         */
        public void setMaxHistoryItems(int maxHistoryItems) {
            if (maxHistoryItems < 0) {
                throw new IllegalArgumentException(Integer.toString(maxHistoryItems));
            }
            this.maxHistoryItems = maxHistoryItems;
        }
        
        /**
         * Set the current expression, it's result and a history item, that is
         * added to the list of history items upon the next evaluation.
         * @param expression The evaluated expression
         * @param result The result of that expression
         * @param historyItem A history representation of the result
         */
        public void setAndOpen(final String expression, final R result, final H historyItem) {
            this.expression = expression;
            this.result = result;
            if (lastHistoryItem != null) {
                synchronized (historyItems) {
                    historyItems.add(0, lastHistoryItem);
                    while (historyItems.size() > maxHistoryItems) {
                        historyItems.remove(historyItems.size() - 1);
                    }
                }
            }
            lastHistoryItem = historyItem;
            if (result == null) {
                fireResultChange(result);
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
                        CodeEvaluatorUI.getInstance().requestActive();
                    }
                    fireResultChange(result);
                }
            });
        }
        
        /**
         * Get the current expression.
         * @return the expression set by {@link #setAndOpen(java.lang.String, java.lang.Object, java.lang.Object)}
         */
        public String getExpression() {
            return expression;
        }
        
        /**
         * Get the current result.
         * @return the result set by {@link #setAndOpen(java.lang.String, java.lang.Object, java.lang.Object)}
         */
        public R getResult() {
            return result;
        }
        
        /**
         * Get the history items.
         * @return the history items.
         */
        public List<H> getHistoryItems() {
            return historyItemsRO;
        }
        
        private synchronized TopComponent getResultViewInstance() {
            /** unique ID of <code>TopComponent</code> (singleton) */
            TopComponent instance = WindowManager.getDefault().findTopComponent("resultsView"); // NOI18N [TODO]
            // Can be null
            return instance;
        }

        private void fireResultChange(R result) {
            for (Listener<R> l : listeners) {
                l.resultChanged(result);
            }
        }
        
        /**
         * Add a result change listener.
         * @param l result change listener
         */
        public void addListener(Listener<R> l) {
            listeners.add(l);
        }
        
        /**
         * Remove a result change listener.
         * @param l result change listener
         */
        public void removeListener(Listener<R> l) {
            listeners.remove(l);
        }
        
        /**
         * Listener that is notified when result changes.
         * @param <R> the type of result object.
         */
        public interface Listener<R> {
            
            /**
             * Notify that the result object has changed.
             * @param o a new result object.
             */
            void resultChanged(R o);
        }
        
        /**
         * A default implementation of a history item. A utility class that handles
         * typical usage.
         */
        public static final class DefaultHistoryItem {
            
            private final String expression;
            private final String type;
            private final String value;
            private final String toStringValue;
            private final String tooltip;
            
            /**
             * Create a new history item.
             * @param expression The evaluated expression
             * @param type type of the result
             * @param value value of the result
             * @param toStringValue toString representation of the result
             */
            public DefaultHistoryItem(String expression, String type, String value, String toStringValue) {
                this.expression = expression;
                this.type = type;
                this.value = value;
                this.toStringValue = toStringValue;
                String text = expression.replace("&", "&amp;")
                                        .replace("<", "&lt;")
                                        .replace(">", "&gt;")
                                        .replace("\n", "<br/>")
                                        .replace("\r", "");
                this.tooltip = "<html>"+text+"</html>";
            }

            /**
             * Get the evaluated expression.
             * @return the expression
             */
            public String getExpression() {
                return expression;
            }

            /**
             * Get the type of the result.
             * @return the type of the result
             */
            public String getType() {
                return type;
            }

            /**
             * Get the value of the result.
             * @return the value of the result
             */
            public String getValue() {
                return value;
            }
            
            /**
             * Get toString representation of the result.
             * @return toString representation of the result
             */
            public String getToStringValue() {
                return toStringValue;
            }

            /**
             * Get tooltip displayed upon the history item.
             * @return the tooltip
             */
            public String getTooltip() {
                return tooltip;
            }
            
        }
    }
}
