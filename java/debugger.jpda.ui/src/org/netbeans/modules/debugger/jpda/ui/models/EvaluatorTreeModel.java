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
package org.netbeans.modules.debugger.jpda.ui.models;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.ui.JPDACodeEvaluator;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.CodeEvaluator;
import org.netbeans.spi.debugger.ui.CodeEvaluator.Result.DefaultHistoryItem;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;
import org.netbeans.spi.viewmodel.CachedChildrenTreeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

@DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                             types={TreeModel.class, AsynchronousModelFilter.class, NodeActionsProviderFilter.class},
                             position=12000)
public class EvaluatorTreeModel extends CachedChildrenTreeModel implements NodeActionsProviderFilter {

    private final Action PASTE_TO_EVALUATOR = Models.createAction (
        NbBundle.getBundle (EvaluatorTreeModel.class).getString ("CTL_PasteExprFromHistoryToEvaluator"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                CodeEvaluator.getDefault().setExpression(((DefaultHistoryItem) nodes[0]).getExpression());
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );

    private final Collection<ModelListener> listeners = new HashSet<ModelListener>();

    private final CodeEvaluator.Result result;
    private final CodeEvaluator.Result.Listener evalListener;

    public EvaluatorTreeModel(ContextProvider contextProvider) {
        result = CodeEvaluator.Result.get(contextProvider.lookupFirst(null, DebuggerEngine.class));
        if (result != null) {
            RequestProcessor rp = contextProvider.lookupFirst(null, RequestProcessor.class);
            if (rp == null) {
                rp = new RequestProcessor(EvaluatorListener.class);
            }
            evalListener = new EvaluatorListener(rp);
            result.addListener(evalListener);
        } else {
            evalListener = null;
        }
    }

    @Override
    public Object getRoot() {
        return TreeModel.ROOT;
    }

    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (TreeModel.ROOT.equals(node)) {
            return false;
        } else if (node instanceof HistoryNode) {
            return false;
        } else if (node instanceof DefaultHistoryItem) {
            return true;
        }
        throw new UnknownTypeException(node.toString());
    }

    @Override
    protected Object[] computeChildren(Object node) throws UnknownTypeException {
        if (node instanceof HistoryNode) {
            List l = ((HistoryNode) node).getItems();
            for (Object o : l) {
                if (!(o instanceof DefaultHistoryItem)) {
                    return new Object[]{};
                }
            }
            return l.toArray();
        }
        if (node == TreeModel.ROOT && this.result != null) {
            Object result = this.result.getResult();
            List items = this.result.getHistoryItems();
            //ArrayList<JPDACodeEvaluator.History.Item> items = jpdaEval.getHistory();
            int count = 0;
            if (result != null) {
                count++;
            }
            if (items.size() > 0) {
                count++;
            }
            Object[] children = new Object[count];
            int index = 0;
            if (result != null) {
                children[index++] = result;
            }
            if (items.size() > 0) {
                children[index] = new HistoryNode(items);
            }
            return children;
        }
        throw new UnknownTypeException(node.toString());
    }

    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT && this.result != null) {
            Object result = this.result.getResult();
            List<DefaultHistoryItem> items = this.result.getHistoryItems();
            //ArrayList items = jpdaEval.getHistory();
            int count = 0;
            if (result != null) {
                count++;
            }
            if (items.size() > 0) {
                count++;
            }
            return count;
        }
        if (node instanceof HistoryNode) {
            return ((HistoryNode) node).getItems().size();
        }
        return Integer.MAX_VALUE;
    }

    public void addModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.add (l);
        }
    }

    public void removeModelListener (ModelListener l) {
        synchronized (listeners) {
            listeners.remove (l);
        }
    }

    public void fireNodeChanged (Object node) {
        try {
            recomputeChildren();
        } catch (UnknownTypeException ex) {
            return;
        }
        ModelListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ModelListener[0]);
        }
        ModelEvent ev = new ModelEvent.NodeChanged(this, node);
        for (int i = 0; i < ls.length; i++) {
            ls[i].modelChanged (ev);
        }
    }
    
    private void fireSelectionChanged(final Object result, final RequestProcessor rp) {
        final ModelListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ModelListener[0]);
        }
        // Unselect
        ModelEvent ev = new ModelEvent.SelectionChanged(this);
        for (int i = 0; i < ls.length; i++) {
            ls[i].modelChanged (ev);
        }
        // Select
        rp.post(new Runnable() {
            public void run() {
                ModelEvent ev = new ModelEvent.SelectionChanged(EvaluatorTreeModel.this, result);
                for (int i = 0; i < ls.length; i++) {
                    ls[i].modelChanged (ev);
                }
            }
        }, 500);
    }

    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof DefaultHistoryItem) {
            CodeEvaluator.getDefault().setExpression(((DefaultHistoryItem) node).getExpression());
        }
        original.performDefaultAction(node);
    }

    @Override
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof DefaultHistoryItem) {
            return new Action[] {PASTE_TO_EVALUATOR};
        }
        return original.getActions(node);
    }

    // **************************************************************************
    
    static class HistoryNode {
        
        private final List<DefaultHistoryItem> items;

        HistoryNode(List<DefaultHistoryItem> items) {
            this.items = items;
        }
        
        List<DefaultHistoryItem> getItems() {
            return items;
        }
    }

    private class EvaluatorListener implements CodeEvaluator.Result.Listener<Variable> {

        private final RequestProcessor rp;
        
        public EvaluatorListener(RequestProcessor rp) {
            this.rp = rp;
        }

        @Override
        public void resultChanged(Variable v) {
            fireNodeChanged(TreeModel.ROOT);
            fireSelectionChanged(v, rp);
        }
        
    }

}
