/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
