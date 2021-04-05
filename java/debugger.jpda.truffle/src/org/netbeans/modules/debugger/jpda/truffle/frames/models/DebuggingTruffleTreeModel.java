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

package org.netbeans.modules.debugger.jpda.truffle.frames.models;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.options.TruffleOptions;
import org.netbeans.modules.debugger.jpda.util.WeakCacheMap;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

@DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/DebuggingView",
                             types={ TreeModelFilter.class })
public class DebuggingTruffleTreeModel implements TreeModelFilter {
    
    private static final Predicate<String> PREDICATE1 = Pattern.compile("^((com|org)\\.\\p{Alpha}*\\.truffle|(com|org)(\\.graalvm|\\.truffleruby))\\..*$").asPredicate();
    private static final String FILTER1 = "com.[A-z]*.truffle.";                     // NOI18N
    private static final String FILTER2 = "com.oracle.graal.";                  // NOI18N
    private static final String FILTER3 = "org.netbeans.modules.debugger.jpda.backend.";    // NOI18N
    
    private final JPDADebugger debugger;
    private final List<ModelListener> listeners = new ArrayList<>();
    private final PropertyChangeListener propListenerHolder;    // Not to have the listener collected
    
    public DebuggingTruffleTreeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        propListenerHolder = propEvent -> {
            ModelListener[] mls;
            synchronized (listeners) {
                mls = listeners.toArray(new ModelListener[listeners.size()]);
            }
            ModelEvent event = new ModelEvent.TreeChanged(TreeModel.ROOT);
            for (ModelListener ml : mls) {
                ml.modelChanged(event);
            }
        };
        TruffleOptions.onLanguageDeveloperModeChange(propListenerHolder);
    }

    @Override
    public Object getRoot(TreeModel original) {
        return original.getRoot();
    }

    @Override
    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
        Object[] children = original.getChildren(parent, from, to);
        if (parent instanceof DebuggingView.DVThread && children.length > 0) {
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(((WeakCacheMap.KeyedValue<JPDAThread>) parent).getKey());
            if (currentPCInfo != null) {
                boolean showInternalFrames = TruffleOptions.isLanguageDeveloperMode();
                TruffleStackFrame[] stackFrames = currentPCInfo.getStack().getStackFrames(showInternalFrames);
                children = filterAndAppend(children, stackFrames, currentPCInfo.getTopFrame());
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
        if (node instanceof TruffleStackFrame) {
            return true;
        } else {
            return original.isLeaf(node);
        }
    }

    @Override
    public void addModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private Object[] filterAndAppend(Object[] children, TruffleStackFrame[] stackFrames,
                                     TruffleStackFrame topFrame) {
        List<Object> newChildren = new ArrayList<>(children.length);
        //newChildren.addAll(Arrays.asList(children));
        for (Object ch : children) {
            if (ch instanceof CallStackFrame) {
                String className = ((CallStackFrame) ch).getClassName();
                if (PREDICATE1.test(className) ||
                    className.startsWith(FILTER2) ||
                    className.startsWith(FILTER3)) {
                    
                    continue;
                }
            }
            newChildren.add(ch);
        }
        int i = 0;
        newChildren.add(i++, topFrame);
        for (TruffleStackFrame tsf : stackFrames) {
            newChildren.add(i++, tsf);
        }
        return newChildren.toArray();
    }
    
}
