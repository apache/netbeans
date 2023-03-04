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

package org.netbeans.modules.refactoring.java.callhierarchy;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Pokorsky
 */
final class CallHierarchyModel {

    enum HierarchyType { CALLER, CALLEE };
    enum Scope { ALL, PROJECT, TESTS, BASE }
    
    public static final String PROP_ROOT = "root"; // NOI18N
    
    /**
     * Contents of the model has been changed -- e.g. after computeCalls.
     */
    public static final String PROP_CONTENTS = "contents"; // NOI18N
    
    private Call root;
    private HierarchyType type = HierarchyType.CALLER;
    private Set<Scope> scopes = EnumSet.of(Scope.ALL, Scope.TESTS);
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    private CallHierarchyModel(HierarchyType type, Set<Scope> scopes) {
        if (type != null) {
            this.type = type;
        }
        if (scopes != null) {
            this.scopes = scopes;
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }
    
    public Call getRoot() {
        return root;
    }

    public synchronized Set<Scope> getScopes() {
        return scopes;
    }

    public synchronized void setScopes(Set<Scope> scopes) {
        this.scopes = scopes;
    }

    public synchronized HierarchyType getType() {
        return type;
    }

    public void setType(HierarchyType type) {
        synchronized (this) {
            this.type = type;
        }
        replaceRoot();
    }
    
    public void computeCalls(Call c, final Runnable resultHandler) {
        Runnable handlerWrapper = new Runnable() {

            @Override
            public void run() {
                support.firePropertyChange(PROP_CONTENTS, null, null);
                resultHandler.run();
            }
            
        };
        if (type == HierarchyType.CALLER) {
            CallHierarchyTasks.findCallers(c, scopes.contains(Scope.TESTS), scopes.contains(Scope.ALL), handlerWrapper);
        } else {
            CallHierarchyTasks.findCallees(c, handlerWrapper);
        }
    }
    
    void replaceRoot() {
        if (root == null) {
            return;
        }
        CallHierarchyTasks.resolveRoot(root.selection, scopes.contains(Scope.BASE), type == HierarchyType.CALLER, new ReplaceRootTask(this));
    }
    
    void replaceRoot(Call root) {
        Call oroot = this.root;
        
        if (root != null && (oroot == null || !root.isIncomplete())) {
            root.model = this;
        } else {
            return;
        }
        this.root = root;
        support.firePropertyChange(PROP_ROOT, oroot, root);
    }
    
    static CallHierarchyModel create(Lookup context, Set<Scope> scopes, HierarchyType type) {
        boolean isCallerGraph = type == null || type == HierarchyType.CALLER;
        CallHierarchyModel m = new CallHierarchyModel(type, scopes);
        CallHierarchyTasks.resolveRoot(context, scopes.contains(Scope.BASE), isCallerGraph, m.new ReplaceRootTask(m));
        return m;
    }
    
    static CallHierarchyModel create(TreePathHandle selection, Set<Scope> scopes, HierarchyType type) {
        boolean isCallerGraph = type == null || type == HierarchyType.CALLER;
        CallHierarchyModel m = new CallHierarchyModel(type, scopes);
        CallHierarchyTasks.resolveRoot(selection, scopes.contains(Scope.BASE), isCallerGraph, m.new ReplaceRootTask(m));
        return m;
    }
    
    private class ReplaceRootTask implements Task<Call> {
        CallHierarchyModel model;

        public ReplaceRootTask(CallHierarchyModel model) {
            this.model = model;
        }
        
        @Override
        public void run(Call parameter) throws Exception {
            model.replaceRoot(parameter);
        }
    }
}
