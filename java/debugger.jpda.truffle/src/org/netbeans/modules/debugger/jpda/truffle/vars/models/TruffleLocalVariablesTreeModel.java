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

package org.netbeans.modules.debugger.jpda.truffle.vars.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleScope;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;

@DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/LocalsView",  types = TreeModelFilter.class)
public class TruffleLocalVariablesTreeModel extends TruffleVariablesTreeModel {

    private final WeakSet<CurrentPCInfo> cpisListening = new WeakSet<CurrentPCInfo>();
    private final CurrentInfoPropertyChangeListener cpiChL = new CurrentInfoPropertyChangeListener();
    
    public TruffleLocalVariablesTreeModel(ContextProvider lookupProvider) {
        super(lookupProvider);
    }
    
    @Override
    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
        if (parent == original.getRoot()) {
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(getDebugger().getCurrentThread());
            TruffleStackFrame selectedStackFrame;
            if (currentPCInfo != null && (selectedStackFrame = currentPCInfo.getSelectedStackFrame()) != null) {
                synchronized (cpisListening) {
                    if (!cpisListening.contains(currentPCInfo)) {
                        currentPCInfo.addPropertyChangeListener(
                                WeakListeners.propertyChange(cpiChL, currentPCInfo));
                        cpisListening.add(currentPCInfo);
                    }
                }
                TruffleScope[] scopes = selectedStackFrame.getScopes();
                if (scopes.length == 0) {
                    return new Object[] {};
                }
                TruffleVariable[] innerMostVars = scopes[0].getVariables();
                if (scopes.length == 1) {
                    return innerMostVars;
                }
                Object[] varsAndScopes = new Object[innerMostVars.length + scopes.length - 1];
                System.arraycopy(innerMostVars, 0, varsAndScopes, 0, innerMostVars.length);
                System.arraycopy(scopes, 1, varsAndScopes, innerMostVars.length, scopes.length - 1);
                return varsAndScopes;
                /*
                TruffleVariable[] vars = selectedStackFrame.getVars();
                ObjectVariable thisObj = selectedStackFrame.getThis();
                if (false && thisObj != null) {
                    TruffleVariable tThis = TruffleVariableImpl.get(thisObj);
                    if (tThis != null) {
                        Object[] children = new Object[vars.length + 1];
                        children[0] = tThis;
                        System.arraycopy(vars, 0, children, 1, vars.length);
                        return children;
                    }
                }
                return vars;
                */
            }
        } else if (parent instanceof TruffleScope) {
            TruffleScope scope = (TruffleScope) parent;
            return scope.getVariables();
        } else if (parent instanceof TruffleVariable) {
            return ((TruffleVariable) parent).getChildren();
        }
        return original.getChildren(parent, from, to);
    }
    
    private void fireVarsChanged() {
        ModelEvent evt = new ModelEvent.TreeChanged(this);
        for (ModelListener l : listeners) {
            l.modelChanged(evt);
        }
    }

    private class CurrentInfoPropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            fireVarsChanged();
        }

    }

}
