/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.debugger.jpda.jsui.frames.models;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.netbeans.modules.debugger.jpda.js.frames.JSStackFrame;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.awt.Actions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/Java/DebuggingView",
                             types={ TreeModelFilter.class,
                                     NodeActionsProviderFilter.class })
public class DebuggingJSFramesInJavaModelFilter implements TreeModelFilter, NodeActionsProviderFilter {
    
    static final Preferences preferences = NbPreferences.forModule(DebuggingJSFramesInJavaModelFilter.class);
    static final String PREF_DISPLAY_JS_STACKS = "displayJSStacks";     // NOI18N
    
    private final Set<DebuggingView.DVThread> threadsWithJSStacks = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    // By default, filter frames to display just JS frames, where appropriate
    private volatile boolean displayJSStacks = preferences.getBoolean(PREF_DISPLAY_JS_STACKS, true);
    private final DisplayJSStacksAction displayJSStacksAction = new DisplayJSStacksAction();
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<>();
    
    @Override
    public Object getRoot(TreeModel original) {
        return original.getRoot();
    }

    @Override
    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
        Object[] children = original.getChildren(parent, from, to);
        if (parent instanceof DebuggingView.DVThread) {
            Object[] jsChildren = DebuggingJSTreeModel.createChildrenWithJSStack(children);
            if (jsChildren != null) {
                threadsWithJSStacks.add((DebuggingView.DVThread) parent);
                if (displayJSStacks) {
                    children = DebuggingJSTreeModel.filterChildren(jsChildren);
                }
            }
        }
        return children;
    }

    @Override
    public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
        if (node instanceof JSStackFrame) {
            return true;
        } else {
            boolean leaf = original.isLeaf(node);
            if (leaf && (node instanceof DebuggingView.DVThread)) {
                threadsWithJSStacks.remove((DebuggingView.DVThread) node);
            }
            return leaf;
        }
    }

    @Override
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }
    
    private void fireModelListeners() {
        ModelEvent event = new ModelEvent.TreeChanged(this);
        for (ModelListener l : listeners) {
            l.modelChanged(event);
        }
    }

    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        original.performDefaultAction(node);
    }

    @Override
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        Action[] actions = original.getActions(node);
        if (node instanceof DebuggingView.DVThread &&
            threadsWithJSStacks.contains((DebuggingView.DVThread) node)) {
            
            Action[] newActions = new Action[actions.length + 2];
            System.arraycopy(actions, 0, newActions, 0, actions.length);
            newActions[actions.length] = null;
            newActions[actions.length + 1] = displayJSStacksAction;
            actions = newActions;
        }
        return actions;
    }
    
    private class DisplayJSStacksAction extends AbstractAction implements Presenter.Popup {
        
        private final JCheckBoxMenuItem cbb;
        
        public DisplayJSStacksAction() {
            cbb = new JCheckBoxMenuItem(Bundle.LBL_DisplayAllJavaFrames(), !displayJSStacks);
            Actions.connect(cbb, this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            displayJSStacks = !displayJSStacks;
            preferences.putBoolean(PREF_DISPLAY_JS_STACKS, displayJSStacks);
            fireModelListeners();
        }

        @NbBundle.Messages("LBL_DisplayAllJavaFrames=Display all Java frames")
        @Override
        public JMenuItem getPopupPresenter() {
            cbb.setSelected(!displayJSStacks);
            return cbb;
        }
        
    }
    
}
