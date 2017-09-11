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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Prevents the models from contacting debuggee when an action is pending.
 * @author martin
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types={ TreeModelFilter.class, TableModelFilter.class },
                                 position=10),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/WatchesView",
                                 types={ TreeModelFilter.class, TableModelFilter.class },
                                 position=10)
})
public class PendingActionsFilter implements TreeModelFilter, TableModelFilter {

    private JPDADebuggerImpl debugger;
    
    public PendingActionsFilter(ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
    }

    private boolean isPending() {
        JPDAThreadImpl ct = (JPDAThreadImpl) debugger.getCurrentThread();
        if (ct != null) {
            return ct.getPendingAction() != null;
        } else {
            return false;
        }
    }

    @Override
    public Object getRoot(TreeModel original) {
        return original.getRoot();
    }

    @Override
    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
        if (isPending()) {
            return new Object[] {};
        } else {
            return original.getChildren(parent, from, to);
        }
    }

    @Override
    public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
        return original.getChildrenCount(node);
    }

    @Override
    public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
        return original.isLeaf(node);
    }

    @Override
    public void addModelListener(ModelListener l) {
        
    }

    @Override
    public void removeModelListener(ModelListener l) {
        
    }

    @Override
    public Object getValueAt(TableModel original, Object node, String columnID) throws UnknownTypeException {
        JPDAThreadImpl ct = (JPDAThreadImpl) debugger.getCurrentThread();
        if (ct != null) {
            Object pa = ct.getPendingAction();
            if (pa != null) {
                return ct.getPendingString(pa);
            }
        }
        return original.getValueAt(node, columnID);
    }

    @Override
    public boolean isReadOnly(TableModel original, Object node, String columnID) throws UnknownTypeException {
        return original.isReadOnly(node, columnID);
    }

    @Override
    public void setValueAt(TableModel original, Object node, String columnID, Object value) throws UnknownTypeException {
        original.setValueAt(node, columnID, value);
    }

}
