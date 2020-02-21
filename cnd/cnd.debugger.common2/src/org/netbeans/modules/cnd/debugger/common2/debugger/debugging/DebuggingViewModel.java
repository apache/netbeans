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
