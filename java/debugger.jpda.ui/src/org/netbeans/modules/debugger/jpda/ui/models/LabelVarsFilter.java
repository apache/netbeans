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
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;

import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 * Labels on variables
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types={NodeActionsProviderFilter.class, NodeModelFilter.class},
                                 position=200),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                                 types={NodeActionsProviderFilter.class, NodeModelFilter.class},
                                 position=200),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/WatchesView",
                                 types={NodeActionsProviderFilter.class, NodeModelFilter.class},
                                 position=200)
})
public class LabelVarsFilter implements NodeActionsProviderFilter, ExtendedNodeModelFilter {

    private final JPDADebuggerImpl debugger;
    private final List<ModelListener> listeners = new ArrayList<ModelListener>();
    
    /** Creates a new instance of LabelVarsFilter */
    public LabelVarsFilter(ContextProvider contextProvider) {
        debugger = (JPDADebuggerImpl) contextProvider.lookupFirst(null, JPDADebugger.class);
        
    }
    
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        original.performDefaultAction(node);
    }

    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        Action [] actions = original.getActions (node);
        if (node instanceof ObjectVariable) {
            int index;
            for (index = 0; index < actions.length; index++) {
                if (actions[index] == null)
                    break;
            }
            Action[] newActions = new Action[actions.length + 1];
            System.arraycopy(actions, 0, newActions, 0, index);
            newActions[index] = MARK_OBJECT_ACTION;
            if (index < actions.length) {
                System.arraycopy(actions, index, newActions, index + 1, actions.length - index);
            }
            actions = newActions;
        }
        return actions;
    }

    @NbBundle.Messages({"CTL_MarkObject_Label=Mark Object...",
                        "CTL_MarkObject_DLG_Title=Mark Object",
                        "CTL_MarkObject_DLG_Label=&Label:"})
    private final Action MARK_OBJECT_ACTION = Models.createAction (
        Bundle.CTL_MarkObject_Label(),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                if ((node == null) || (!(node instanceof ObjectVariable))) {
                    return false;
                }
                ObjectVariable var = (ObjectVariable) node;
                return var.getUniqueID() != 0L;
            }
            public void perform (Object[] nodes) {
                ObjectVariable var = (ObjectVariable) nodes[0];
                if (var.getUniqueID() == 0L) return ;
                String title = Bundle.CTL_MarkObject_DLG_Title();
                String label = Bundle.CTL_MarkObject_DLG_Label();
                NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(label, title);
                Object ret = DialogDisplayer.getDefault().notify(nd);
                if (nd.OK_OPTION == ret) {
                    label = nd.getInputText().trim();
                    if (label.length() == 0) {
                        label = null;
                    }
                    debugger.markObject(var, label);
                    fireNodeChange(null); // Refresh all nodes, not just (var);
                }
            }

        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );

    public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canRename(node);
    }

    public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canCopy(node);
    }

    public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canCut(node);
    }

    public Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return original.clipboardCopy(node);
    }

    public Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return original.clipboardCut(node);
    }

    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException {
        return original.getPasteTypes(node, t);
    }

    public void setName(ExtendedNodeModel original, Object node, String name) throws UnknownTypeException {
        original.setName(node, name);
    }

    public String getIconBaseWithExtension(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.getIconBaseWithExtension(node);
    }

    public String getDisplayName(NodeModel original, Object node) throws UnknownTypeException {
        String displayName = original.getDisplayName(node);
        if (node instanceof ObjectVariable) {
            String label = debugger.getLabel((ObjectVariable) node);
            if (label != null) {
                return displayName + " ["+label+"]"; // NOI18N
            }
        }
        return displayName;
    }

    public String getIconBase(NodeModel original, Object node) throws UnknownTypeException {
        return original.getIconBase(node);
    }

    public String getShortDescription(NodeModel original, Object node) throws UnknownTypeException {
        String sd = original.getShortDescription(node);
        if (node instanceof ObjectVariable) {
            String label = debugger.getLabel((ObjectVariable) node);
            if (label != null) {
                return "["+label+"] "+sd; // NOI18N
            }
        }
        return sd;
    }

    public void addModelListener(ModelListener l) {
        synchronized (listeners) {
            if (!listeners.contains(l)) {
                listeners.add(l);
            }
        }
    }

    public void removeModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
        
    private void fireNodeChange(ObjectVariable var) {
        ModelEvent event = new ModelEvent.NodeChanged(this, var, ModelEvent.NodeChanged.DISPLAY_NAME_MASK |
                                                                 ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK);
        ModelListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ModelListener[0]);
        }
        for (ModelListener l : ls) {
            l.modelChanged(event);
        }
    }

}
