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

package org.netbeans.modules.javascript.v8debug.ui.eval;

import java.awt.Color;
import java.util.List;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.modules.javascript.v8debug.V8DebuggerEngineProvider;
import org.netbeans.modules.javascript.v8debug.ui.vars.models.VariablesModel;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.CodeEvaluator.Result.DefaultHistoryItem;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import static org.netbeans.spi.viewmodel.TreeModel.ROOT;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path=V8DebuggerEngineProvider.ENGINE_NAME+"/ResultsView",
                             types={ TreeModel.class, ExtendedNodeModel.class, TableModel.class })
public class EvaluationResultsModel extends VariablesModel {
    
    //@StaticResource(searchClasspath = true)
    private static final String ICON_HISTORY_NODE =
        "org/netbeans/modules/debugger/resources/evaluator/history_node_16.png"; // NOI18N

    //@StaticResource(searchClasspath = true)
    private static final String ICON_HISTORY_ITEM =
        "org/netbeans/modules/debugger/resources/evaluator/eval_history_item.png"; // NOI18N
    
    //@StaticResource(searchClasspath = true)
    private static final String ICON_EVAL_RESULT =
        "org/netbeans/modules/debugger/resources/evaluator/evaluator_result_16.png"; // NOI18N

    //@StaticResource(searchClasspath = true)
    private static final String ICON_WRONG_PASS =
        "org/netbeans/modules/debugger/resources/wrong_pass.png"; // NOI18N

    private static final RequestProcessor RP = new RequestProcessor(EvaluationResultsModel.class);
    private final org.netbeans.spi.debugger.ui.CodeEvaluator.Result result;
    private EvaluatorListener evalListener = new EvaluatorListener();
    
    public EvaluationResultsModel(ContextProvider contextProvider) {
        super(contextProvider);
        //CodeEvaluator.addResultListener(evalListener);
        result = org.netbeans.spi.debugger.ui.CodeEvaluator.Result.get(contextProvider.lookupFirst(null, DebuggerEngine.class));
        result.addListener(evalListener);
    }

    @Override
    public int getChildrenCount(Object parent) throws UnknownTypeException {
        if (parent instanceof HistoryNode) {
            return ((HistoryNode) parent).getItems().size();
        }
        if (parent == ROOT) {
            return 2;
        } else if (parent instanceof VarOrError) {
            VarOrError voe = (VarOrError) parent;
            if (voe.hasVar()) {
                return super.getChildrenCount(voe.getVar());
            } else {
                return 0;
            }
        } else {
            return super.getChildrenCount(parent);
        }
    }

    @Override
    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        if (parent instanceof HistoryNode) {
            List l = ((HistoryNode) parent).getItems();
            for (Object o : l) {
                if (!(o instanceof org.netbeans.spi.debugger.ui.CodeEvaluator.Result.DefaultHistoryItem)) {
                    return new Object[]{};
                }
            }
            return l.toArray();
        }
        if (parent == ROOT) {
            Object result = this.result.getResult();
            List items = this.result.getHistoryItems();
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
        } else if (parent instanceof VarOrError) {
            VarOrError voe = (VarOrError) parent;
            if (voe.hasVar()) {
                return super.getChildren(voe.getVar(), from, to);
            } else {
                return new Object[]{};
            }
        } else {
            return super.getChildren(parent, from, to);
        }
    }
    
    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (TreeModel.ROOT.equals(node)) {
            return false;
        } else if (node instanceof HistoryNode) {
            return false;
        } else if (node instanceof DefaultHistoryItem) {
            return true;
        } else if (node instanceof VarOrError) {
            VarOrError voe = (VarOrError) node;
            if (voe.hasVar()) {
                return super.isLeaf(voe.getVar());
            } else {
                return true;
            }
        }
        return super.isLeaf(node);
    }

    @NbBundle.Messages("CTL_EvaluatorHistoryNode=History")
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof DefaultHistoryItem) {
            return ((DefaultHistoryItem) node).getExpression();
        }
        if (node instanceof EvaluationResultsModel.HistoryNode) {
            return Bundle.CTL_EvaluatorHistoryNode();
        } else if (node instanceof VarOrError) {
            return result.getExpression();
        }
        return super.getDisplayName(node);
    }

    @NbBundle.Messages("CTL_EvaluatorHistoryNodeDescr=Evaluation History")
    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
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
        if (node instanceof EvaluationResultsModel.HistoryNode) {
            return Bundle.CTL_EvaluatorHistoryNodeDescr();
        } else if (node instanceof VarOrError) {
            return result.getExpression();
        }
        return super.getShortDescription(node);
    }
    
    @Override
    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node == result.getResult()) {
            return ICON_EVAL_RESULT;
        }
        if (node instanceof DefaultHistoryItem) {
            return ICON_HISTORY_ITEM;
        }
        if (node instanceof EvaluationResultsModel.HistoryNode) {
            return ICON_HISTORY_NODE;
        } else if (node instanceof VarOrError) {
            VarOrError voe = (VarOrError) node;
            if (voe.hasVar()) {
                return super.getIconBaseWithExtension(voe.getVar());
            } else {
                return ICON_WRONG_PASS;
            }
        }
        return super.getIconBaseWithExtension(node);
    }

    @Override
    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
        if (node instanceof DefaultHistoryItem) {
            DefaultHistoryItem item = (DefaultHistoryItem) node;
            if (Constants.LOCALS_TO_STRING_COLUMN_ID.equals(columnID)) {
                return item.getToStringValue();
            } else if (Constants.LOCALS_TYPE_COLUMN_ID.equals(columnID)) {
                return item.getType();
            } else if (Constants.LOCALS_VALUE_COLUMN_ID.equals(columnID)) {
                return item.getValue();
            }
            return ""; // NOI18N
        }
        if (node instanceof EvaluationResultsModel.HistoryNode) {
            return "";
        } else if (node instanceof VarOrError) {
            VarOrError voe = (VarOrError) node;
            if (voe.hasVar()) {
                return super.getValueAt(voe.getVar(), columnID);
            } else {
                if (Constants.LOCALS_TYPE_COLUMN_ID.equals(columnID)) {
                    return "";
                } else {
                    return toHTML(voe.getError(), true, false, Color.red);
                }
            }
        }
        return super.getValueAt(node, columnID);
    }

    @Override
    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
        if (node instanceof DefaultHistoryItem || node instanceof EvaluationResultsModel.HistoryNode) {
            return true;
        } else if (node instanceof VarOrError) {
            return true;
        }
        return super.isReadOnly(node, columnID);
    }

    @Override
    public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
        if (node instanceof DefaultHistoryItem || node instanceof EvaluationResultsModel.HistoryNode) {
            return ;
        } else if (node instanceof VarOrError) {
            return ;
        } else {
            super.setValueAt(node, columnID, value);
        }
    }
    
    public void fireNodeChanged (Object node) {
        fireChangeEvent(new ModelEvent.NodeChanged(this, node));
    }
    
    private void fireSelectionChanged(final Object result) {
        // Unselect
        fireChangeEvent(new ModelEvent.SelectionChanged(this));
        // Select
        RP.post(new Runnable() {
            public void run() {
                fireChangeEvent(new ModelEvent.SelectionChanged(EvaluationResultsModel.this, result));
            }
        }, 500);
    }

    private static class HistoryNode {
        
        private final List<DefaultHistoryItem> items;

        HistoryNode(List<DefaultHistoryItem> items) {
            this.items = items;
        }
        
        List<DefaultHistoryItem> getItems() {
            return items;
        }
    }

    private class EvaluatorListener implements org.netbeans.spi.debugger.ui.CodeEvaluator.Result.Listener<VarOrError> {

        @Override
        public void resultChanged(VarOrError voe) {
            fireNodeChanged(TreeModel.ROOT);
            fireSelectionChanged(voe);
        }

    }

    
}
