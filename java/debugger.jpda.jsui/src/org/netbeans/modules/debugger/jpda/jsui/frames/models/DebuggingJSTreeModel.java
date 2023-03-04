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

package org.netbeans.modules.debugger.jpda.jsui.frames.models;

import org.netbeans.modules.debugger.jpda.js.frames.JSStackFrame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/JS/DebuggingView",
                             types={ TreeModelFilter.class })
public class DebuggingJSTreeModel implements TreeModelFilter {
    
    private static final String FILTER1 = "java.lang.invoke.LambdaForm";
    private static final String FILTER2 = "jdk.nashorn.";

    @Override
    public Object getRoot(TreeModel original) {
        return original.getRoot();
    }

    @Override
    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
        Object[] children = original.getChildren(parent, from, to);
        if (parent instanceof DebuggingView.DVThread) {
            Object[] jsChildren = createChildrenWithJSStack(children);
            if (jsChildren != null) {
                children = filterChildren(jsChildren);
            }
        }
        return children;
    }
    
    static Object[] createChildrenWithJSStack(Object[] children) {
        boolean isJSStack = false;
        for (int i = 0; i < children.length; i++) {
            Object ch = children[i];
            if (ch instanceof CallStackFrame) {
                CallStackFrame csf = (CallStackFrame) ch;
                if (csf.getClassName().startsWith(JSUtils.NASHORN_SCRIPT)) {
                    if (!isJSStack) {
                        Object[] children2 = new Object[children.length];
                        System.arraycopy(children, 0, children2, 0, children.length);
                        children = children2;
                    }
                    children[i] = JSStackFrame.get(csf);
                    isJSStack = true;
                }
            }
        }
        if (isJSStack) {
            return children;
        } else {
            return null;
        }
    }
    
    static Object[] filterChildren(Object[] children) {
        List<Object> newChildren = new ArrayList<>(children.length);
        newChildren.addAll(Arrays.asList(children));
        for (int i = 0; i < newChildren.size(); i++) {
            Object ch = newChildren.get(i);
            if (ch instanceof CallStackFrame) {
                String className = ((CallStackFrame) ch).getClassName();
                if (className.startsWith(FILTER1) ||
                    className.startsWith(FILTER2)) {
                    
                    newChildren.remove(i);
                    i--;
                }
            }
        }
        return newChildren.toArray();
    }

    @Override
    public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
        return original.getChildrenCount(node);
    }

    @Override
    public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
        if (node instanceof JSStackFrame) {
            return true;
        } else {
            return original.isLeaf(node);
        }
    }

    @Override
    public void addModelListener(ModelListener l) {
    }

    @Override
    public void removeModelListener(ModelListener l) {
    }
    
}
