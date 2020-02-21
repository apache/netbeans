/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.common2.debugger.debugging;

import java.util.Collection;
import java.util.HashSet;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.Thread;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 */
public abstract class DebuggingViewModel implements TreeModel, ModelListener/*, AsynchronousModelFilter*/ {
    private final NativeDebugger debugger;

    protected DebuggingViewModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, NativeDebugger.class);
    }

    @Override
    public Object getRoot() {
        return ROOT;
    }

    @Override
    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        if (parent.equals(ROOT)) {
            return debugger.getThreadsWithStacks();
        }
        if (parent instanceof Thread) {
            return ((Thread) parent).getStack();
        }
        
        return new Object[]{};
    }

    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        return getChildrenCount(node) == 0;
    }

    @Override
    public int getChildrenCount(Object node) throws UnknownTypeException {     
        Object[] ch = getChildren(node, 0, 0);
        return ( ch == null ? 0 : ch.length );
    }
    
    private final Collection<ModelListener> listeners = new HashSet<ModelListener>();

    @Override
    public void addModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
        debugger.registerDebuggingViewModel(this);
    }

    @Override
    public void removeModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
        debugger.registerDebuggingViewModel(null);
    }

    @Override
    public void modelChanged(ModelEvent event) {
        for (ModelListener modelListener : listeners) {
            modelListener.modelChanged(event);
        }
    }
}
