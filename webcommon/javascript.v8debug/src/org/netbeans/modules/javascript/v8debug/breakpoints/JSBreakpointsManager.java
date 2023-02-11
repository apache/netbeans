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

package org.netbeans.modules.javascript.v8debug.breakpoints;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.modules.javascript.v8debug.ScriptsHandler;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class JSBreakpointsManager extends DebuggerManagerAdapter {
    
    private final Set<V8Debugger> activeDebuggers = new CopyOnWriteArraySet<>();
    private ThreadLocal<Boolean> breakpointsAddedDuringInit = new ThreadLocal<Boolean>();

    @Override
    public void engineAdded(DebuggerEngine engine) {
        V8Debugger dbg = engine.lookupFirst(null, V8Debugger.class);
        if (dbg != null) {
            activeDebuggers.add(dbg);
            submitExistingBreakpoints(dbg);
        }
    }

    @Override
    public void engineRemoved(DebuggerEngine engine) {
        V8Debugger dbg = engine.lookupFirst(null, V8Debugger.class);
        if (dbg != null) {
            activeDebuggers.remove(dbg);
        }
    }
    
    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        if (!(breakpoint instanceof JSLineBreakpoint)) {
            return ;
        }
        if (breakpointsAddedDuringInit.get() != null) {
            breakpointsAddedDuringInit.set(true);
        }
        JSLineBreakpoint lb = (JSLineBreakpoint) breakpoint;
        for (V8Debugger dbg : activeDebuggers) {
            ScriptsHandler scriptsHandler = dbg.getScriptsHandler();
            FileObject fo = lb.getFileObject();
            if (fo == null && scriptsHandler.containsRemoteFile(lb.getURL()) ||
                scriptsHandler.containsLocalFile(fo)) {

                submit(lb, dbg);
            }
        }
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (!(breakpoint instanceof JSLineBreakpoint)) {
            return ;
        }
        JSLineBreakpoint lb = (JSLineBreakpoint) breakpoint;
        for (V8Debugger dbg : activeDebuggers) {
            if (dbg.getScriptsHandler().containsLocalFile(lb.getFileObject())) {
                dbg.getBreakpointsHandler().remove(lb);
            }
        }
    }
    
    private void submitExistingBreakpoints(V8Debugger dbg) {
        breakpointsAddedDuringInit.set(false);
        Boolean added = breakpointsAddedDuringInit.get();
        breakpointsAddedDuringInit.remove();
        if (added) {
            return ;
        }
        ScriptsHandler scriptsHandler = dbg.getScriptsHandler();
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint b : breakpoints) {
            if (!(b instanceof JSLineBreakpoint)) {
                continue;
            }
            JSLineBreakpoint lb = (JSLineBreakpoint) b;
            FileObject fo = lb.getFileObject();
            if (fo == null && scriptsHandler.containsRemoteFile(lb.getURL()) ||
                scriptsHandler.containsLocalFile(fo)) {

                submit(lb, dbg);
            }
        }
    }

    private void submit(JSLineBreakpoint b, V8Debugger dbg) {
        dbg.getBreakpointsHandler().add(b);
    }
    
}
