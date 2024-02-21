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
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import static org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess.BASIC_CLASS_NAME;
import org.netbeans.modules.debugger.jpda.truffle.actions.StepActionProvider;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.options.TruffleOptions;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVFrame;
import org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThread;
import org.netbeans.modules.debugger.jpda.util.WeakCacheMap;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.debugger.ui.DebuggingView.DVFrame;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

@DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                             types={ TreeModelFilter.class }, position=25000)
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
                mls = listeners.toArray(new ModelListener[0]);
            }
            ModelEvent event = new ModelEvent.TreeChanged(TreeModel.ROOT);
            for (ModelListener ml : mls) {
                ml.modelChanged(event);
            }
        };
        TruffleOptions.onLanguageDeveloperModeChange(propListenerHolder);
        DebuggingTruffleActionsProvider.onShowAllHostFramesChange(propListenerHolder);
    }

    @Override
    public Object getRoot(TreeModel original) {
        return original.getRoot();
    }

    @Override
    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
        Object[] children = original.getChildren(parent, from, to);
        if (parent instanceof DebuggingView.DVThread && children.length > 0 &&
                !DebuggingTruffleActionsProvider.isShowAllHostFrames((DebuggingView.DVThread) parent)) {

            JPDAThread thread = ((WeakCacheMap.KeyedValue<JPDAThread>) parent).getKey();
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentGuestPCInfo(thread);
            boolean inGuest = isInGuest(children[0]);
            boolean haveTopHostFrames = false;
            if (currentPCInfo == null) {
                currentPCInfo = inGuest ? TruffleAccess.getCurrentSuspendHereInfo(thread) : TruffleAccess.getSuspendHere(thread);
                haveTopHostFrames = true;
            }
            if (currentPCInfo != null) {
                boolean showInternalFrames = TruffleOptions.isLanguageDeveloperMode();
                TruffleStackFrame[] stackFrames = currentPCInfo.getStack().getStackFrames(showInternalFrames);
                if (inGuest && !currentPCInfo.getStack().hasJavaFrames()) {
                    children = filterAndAppend(children, stackFrames, currentPCInfo.getTopFrame());
                } else {
                    children = mergeFrames(children, stackFrames, currentPCInfo.getTopFrame(), haveTopHostFrames);
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

    private static boolean isInGuest(Object child) {
        if (child instanceof CallStackFrame) {
            CallStackFrame csf = (CallStackFrame) child;
            return BASIC_CLASS_NAME.equals(csf.getClassName());
        } else {
            return false;
        }
    }

    private static Object[] filterAndAppend(Object[] children, TruffleStackFrame[] stackFrames,
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

    private static TruffleStackFrame[] join(TruffleStackFrame[] stackFrames,
                                            TruffleStackFrame topFrame) {
        TruffleStackFrame[] joined = new TruffleStackFrame[stackFrames.length + 1];
        joined[0] = topFrame;
        System.arraycopy(stackFrames, 0, joined, 1, stackFrames.length);
        return joined;
    }

    static Object[] mergeFrames(Object[] children, TruffleStackFrame[] stackFrames,
                                TruffleStackFrame topFrame, boolean haveTopHostFrames) {
        List<Object> newChildren = new ArrayList<>(children.length);
        stackFrames = join(stackFrames, topFrame);
        int chi = 0;
        if (haveTopHostFrames) {
            boolean step2Java = false;
            for (; chi < children.length; chi++) {
                Object ch = children[chi];
                if (ch instanceof CallStackFrame) {
                    CallStackFrame csf = (CallStackFrame) ch;
                    String className = csf.getClassName();
                    if (StepActionProvider.STEP2JAVA_CLASS.equals(className) &&
                            StepActionProvider.STEP2JAVA_METHOD.equals(csf.getMethodName())) {
                        step2Java = true;
                        break;
                    }
                    int lastDot = className.lastIndexOf('.');
                    String packageName = lastDot > 0 ? className.substring(0, lastDot) : "";
                    if ("org.graalvm.polyglot".equals(packageName)) {
                        // There's no step to Java call and we hit the SDK code.
                        break;
                    }
                }
                newChildren.add(ch);
            }
            if (!step2Java) {
                // No, we do not have top host frames:
                newChildren.clear();
            }
        }
        for (TruffleStackFrame tframe : stackFrames) {
            if (tframe.isHost() && chi < children.length) {
                for (; chi < children.length; chi++) {
                    Object ch = children[chi];
                    if (ch instanceof CallStackFrame) {
                        CallStackFrame csf = (CallStackFrame) ch;
                        String className = csf.getClassName();
                        if (className.startsWith(FILTER3)) {
                            continue;
                        }
                        if (equalFrames(csf, tframe)) {
                            newChildren.add(csf);
                            chi++;
                            break;
                        }
                    } else {
                        newChildren.add(ch);
                    }
                }
            } else {
                newChildren.add(tframe);
            }
        }
        for (; chi < children.length; chi++) {
            newChildren.add(children[chi]);
        }
        return newChildren.toArray();
    }

    static List<DVFrame> filterAndAppend(JPDADVThread thread, List<DVFrame> children,
                                         TruffleStackFrame[] stackFrames,
                                         TruffleStackFrame topFrame) {
        List<DVFrame> newChildren = new ArrayList<>(children.size());
        for (DVFrame ch : children) {
            if (ch instanceof JPDADVFrame) {
                String className = ((JPDADVFrame) ch).getCallStackFrame().getClassName();
                if (PREDICATE1.test(className) ||
                    className.startsWith(FILTER2) ||
                    className.startsWith(FILTER3)) {
                    
                        continue;
                    }
            }
            newChildren.add(ch);
        }
        int i = 0;
        newChildren.add(i++, new TruffleDVFrame(thread, topFrame));
        for (TruffleStackFrame tsf : stackFrames) {
            newChildren.add(i++, new TruffleDVFrame(thread, tsf));
        }
        return Collections.unmodifiableList(newChildren);
    }

    static List<DVFrame> mergeFrames(JPDADVThread thread, List<DVFrame> children,
                                     TruffleStackFrame[] stackFrames,
                                     TruffleStackFrame topFrame, boolean haveTopHostFrames) {
        List<DVFrame> newChildren = new ArrayList<>(children.size());
        stackFrames = join(stackFrames, topFrame);
        int chi = 0;
        if (haveTopHostFrames) {
            for (; chi < children.size(); chi++) {
                DVFrame ch = children.get(chi);
                CallStackFrame csf = ((JPDADVFrame) ch).getCallStackFrame();
                if (StepActionProvider.STEP2JAVA_CLASS.equals(csf.getClassName()) &&
                        StepActionProvider.STEP2JAVA_METHOD.equals(csf.getMethodName())) {
                    break;
                }
                newChildren.add(ch);
            }
        }
        for (TruffleStackFrame tframe : stackFrames) {
            if (tframe.isHost()) {
                for (; chi < children.size(); chi++) {
                    DVFrame ch = children.get(chi);
                    CallStackFrame csf = ((JPDADVFrame) ch).getCallStackFrame();
                    String className = csf.getClassName();
                    if (className.startsWith(FILTER3)) {
                        continue;
                    }
                    if (equalFrames(csf, tframe)) {
                        newChildren.add(ch);
                        chi++;
                        break;
                    }
                }
            } else {
                newChildren.add(new TruffleDVFrame(thread, tframe));
            }
        }
        for (; chi < children.size(); chi++) {
            newChildren.add(children.get(chi));
        }
        return Collections.unmodifiableList(newChildren);
    }

    private static boolean equalFrames(CallStackFrame csf, TruffleStackFrame tframe) {
        if (!csf.getClassName().equals(tframe.getHostClassName())) {
            return false;
        }
        if (!csf.getMethodName().equals(tframe.getHostMethodName())) {
            return false;
        }
        int linej = csf.getLineNumber(null);
        SourcePosition sourcePosition = tframe.getSourcePosition();
        if (sourcePosition == null) {
            return false;
        }
        int linet = sourcePosition.getStartLine();
        return (linej == linet || linej == 0);
    }
    
}
