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


import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.CodeEvaluator;
import org.netbeans.spi.debugger.ui.CodeEvaluator.Result.DefaultHistoryItem;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

@DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                             types=ExtendedNodeModelFilter.class,
                             position=280)
public class EvaluatorNodeModelFilter implements ExtendedNodeModelFilter {

    private static final String ICON_HISTORY_NODE =
        "org/netbeans/modules/debugger/resources/evaluator/history_node_16.png"; // NOI18N

    private static final String ICON_HISTORY_ITEM =
        "org/netbeans/modules/debugger/resources/evaluator/eval_history_item.png"; // NOI18N
    
    private static final String ICON_EVAL_RESULT =
        "org/netbeans/modules/debugger/resources/evaluator/evaluator_result_16.png"; // NOI18N

    private CodeEvaluator.Result<Variable, DefaultHistoryItem> result;
    //private final Collection<ModelListener> listeners = new HashSet<ModelListener>();
    
    public EvaluatorNodeModelFilter(ContextProvider contextProvider) {
        result = CodeEvaluator.Result.get(contextProvider.lookupFirst(null, DebuggerEngine.class));
    }

    public void addModelListener(ModelListener l) {
//        synchronized (listeners) {
//            listeners.add (l);
//        }
    }

    public void removeModelListener (ModelListener l) {
//        synchronized (listeners) {
//            listeners.remove (l);
//        }
    }

    /*
    private void fireNodeChanged (Object node) {
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        ModelEvent event;
//        event = new ModelEvent.NodeChanged(this, node,
//                ModelEvent.NodeChanged.DISPLAY_NAME_MASK |
//                ModelEvent.NodeChanged.ICON_MASK |
//                ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK |
//                ModelEvent.NodeChanged.CHILDREN_MASK |
//                ModelEvent.NodeChanged.EXPANSION_MASK);
        event = new ModelEvent.NodeChanged(this, node, ModelEvent.NodeChanged.CHILDREN_MASK);
        for (ModelListener ml : ls) {
            ml.modelChanged (event);
        }
    }
    */

    public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException {
        return null;
    }

    public void setName(ExtendedNodeModel original, Object node, String name) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getIconBaseWithExtension(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        if (node == result.getResult()) {
            return ICON_EVAL_RESULT;
        }
        if (node instanceof DefaultHistoryItem) {
            return ICON_HISTORY_ITEM;
        }
        if (node instanceof EvaluatorTreeModel.HistoryNode) {
            return ICON_HISTORY_NODE;
        }
        return original.getIconBaseWithExtension(node);
    }

    public String getIconBase(NodeModel original, Object node) throws UnknownTypeException {
        throw new IllegalStateException("getIconBaseWithExtension() to be called instead.");
        /*
        if (node instanceof EvaluatorTreeModel.SpecialNode) {
            return ((EvaluatorTreeModel.SpecialNode)node).getIconBase();
        }
        if (node == result.getResult()) {
            return "org/netbeans/modules/debugger/resources/evaluator/evaluator_result_16.png"; // NOI18N
        }
        return original.getIconBase(node);*/
    }

    public String getDisplayName(NodeModel original, Object node) throws UnknownTypeException {
        if (node == result.getResult()) {
            String str = result.getExpression();
            if (str != null) {
                return str;
            }
        }
        if (node instanceof DefaultHistoryItem) {
            return ((DefaultHistoryItem) node).getExpression();
        }
        if (node instanceof EvaluatorTreeModel.HistoryNode) {
            return NbBundle.getMessage(EvaluatorNodeModelFilter.class, "MSG_EvaluatorHistoryFilterNode"); // NOI18N
        }
        return original.getDisplayName(node);
    }

    public String getShortDescription(NodeModel original, Object node) throws UnknownTypeException {
        if (node == result.getResult()) {
            String str = result.getExpression();
            if (str != null) {
                str = str.replace ("&", "&amp;")
                         .replace ("<", "&lt;")
                         .replace (">", "&gt;")
                         .replace ("\n", "<br/>")
                         .replace ("\r", "");
                return "<html>"+str+"</html>";
            }
        }
        if (node instanceof DefaultHistoryItem) {
            return ((DefaultHistoryItem) node).getTooltip();
        }
        if (node instanceof EvaluatorTreeModel.HistoryNode) {
            return NbBundle.getMessage(EvaluatorNodeModelFilter.class, "CTL_EvaluatorHistoryNode"); // NOI18N
        }
        return original.getShortDescription(node);
    }

}
