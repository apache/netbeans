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
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.ui.JPDACodeEvaluator;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.CodeEvaluator;
import org.netbeans.spi.debugger.ui.CodeEvaluator.Result.DefaultHistoryItem;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

//@DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
//                             types=TreeModelFilter.class,
//                             position=350)
public class EvaluatorTreeModelFilter implements TreeModelFilter {

    private Collection<ModelListener> listeners = new HashSet<ModelListener>();

    private final JPDACodeEvaluator jpdaEval;
    private final CodeEvaluator.Result<Variable, DefaultHistoryItem> result;
    private CodeEvaluator.Result.Listener evalListener = new EvaluatorListener();

    public EvaluatorTreeModelFilter(ContextProvider contextProvider) {
        jpdaEval = (JPDACodeEvaluator) contextProvider.lookupFirst(null, CodeEvaluator.EvaluatorService.class);
        assert jpdaEval != null;
        //jpdaEval.addResultListener(evalListener);
        result = CodeEvaluator.Result.get(contextProvider.lookupFirst(null, DebuggerEngine.class));
        result.addListener(evalListener);
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
//        try {
//            recomputeChildren();
//        } catch (UnknownTypeException ex) {
//            return;
//        }
        ModelListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ModelListener[0]);
        }
        ModelEvent ev = new ModelEvent.NodeChanged(this, node);
        for (int i = 0; i < ls.length; i++) {
            ls[i].modelChanged (ev);
        }
    }

    private void fireSelectionChanged(final Variable result) {
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
        jpdaEval.getRequestProcessor().post(new Runnable() {
            public void run() {
                ModelEvent ev = new ModelEvent.SelectionChanged(EvaluatorTreeModelFilter.this, result);
                for (int i = 0; i < ls.length; i++) {
                    ls[i].modelChanged (ev);
                }
            }
        }, 500);
    }

    public Object getRoot(TreeModel original) {
        return TreeModel.ROOT;
    }

    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
//        if (parent instanceof EvaluatorTreeModel.SpecialNode) {
//            return ((EvaluatorTreeModel.SpecialNode) parent).getChildren(from, to);
//        }
        if (parent == TreeModel.ROOT) {
            Variable result = this.result.getResult();
            List<DefaultHistoryItem> items = this.result.getHistoryItems();
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
//                children[index] = new EvaluatorTreeModel.HistoryNode(items);
            }
            return children;
        }
        return original.getChildren(parent, from, to);
    }

    public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
        if (TreeModel.ROOT.equals(node)) {
            Variable result = this.result.getResult();
            List<DefaultHistoryItem> items = this.result.getHistoryItems();
            int count = 0;
            if (result != null) {
                count++;
            }
            if (items.size() > 0) {
                count++;
            }
            return count;
        }
//        if (node instanceof EvaluatorTreeModel.SpecialNode) {
//            return ((EvaluatorTreeModel.SpecialNode)node).getChildrenCount();
//        }
        return original.getChildrenCount(node);
    }

    public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
        if (TreeModel.ROOT.equals(node)) {
            return false;
//        } else if (node instanceof EvaluatorTreeModel.SpecialNode) {
//            return ((EvaluatorTreeModel.SpecialNode)node).isLeaf();
        }
        return original.isLeaf(node);
    }

    private class EvaluatorListener implements CodeEvaluator.Result.Listener<Variable> {

        @Override
        public void resultChanged(Variable v) {
            fireNodeChanged(TreeModel.ROOT);
            fireSelectionChanged(v);
        }

    }

}
