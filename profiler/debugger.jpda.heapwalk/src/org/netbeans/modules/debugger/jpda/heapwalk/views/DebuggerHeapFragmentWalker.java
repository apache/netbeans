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

package org.netbeans.modules.debugger.jpda.heapwalk.views;

import org.netbeans.lib.profiler.heap.Heap;

import javax.swing.JPanel;

import org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker;
import org.netbeans.modules.profiler.heapwalk.HeapWalker;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author martin
 */
public class DebuggerHeapFragmentWalker extends HeapFragmentWalker {
    
    /** Creates a new instance of DebuggerHeapFragmentWalker */
    public DebuggerHeapFragmentWalker(Heap heap) {
        super(heap, new HeapWalker(heap));
    }

    @Override
    public JPanel getPanel() {
        // Not supported
        return null;
    }

    @Override
    public void switchToClassesView() {
        openComponent("classes", true);
    }

    @Override
    public void switchToInstancesView() {
        openComponent("dbgInstances", true);
    }
    
    static TopComponent openComponent (String viewName, boolean activate) {
        TopComponent view = WindowManager.getDefault().findTopComponent(viewName);
        if (view == null) {
            throw new IllegalArgumentException(viewName);
        }
        view.open();
        if (activate) {
            view.requestActive();
        }
        ((InstancesView) view).assureSubViewsVisible();
        return view;
    }
    
}
