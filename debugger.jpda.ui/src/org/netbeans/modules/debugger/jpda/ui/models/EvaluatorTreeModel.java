/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
