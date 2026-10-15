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

package org.netbeans.modules.javascript.cdtdebug.breakpoints;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;
import org.netbeans.modules.javascript.cdtdebug.ScriptsHandler;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class JSBreakpointsManager extends DebuggerManagerAdapter {

    private final Set<CDTDebugger> activeDebuggers = new CopyOnWriteArraySet<>();
    private final ThreadLocal<Boolean> breakpointsAddedDuringInit = new ThreadLocal<>();

    @Override
    public void engineAdded(DebuggerEngine engine) {
        CDTDebugger dbg = engine.lookupFirst(null, CDTDebugger.class);
        if (dbg != null) {
            activeDebuggers.add(dbg);
            submitExistingBreakpoints(dbg);
        }
    }

    @Override
    public void engineRemoved(DebuggerEngine engine) {
        CDTDebugger dbg = engine.lookupFirst(null, CDTDebugger.class);
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
        for (CDTDebugger dbg : activeDebuggers) {
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
        for (CDTDebugger dbg : activeDebuggers) {
            if (dbg.getScriptsHandler().containsLocalFile(lb.getFileObject())) {
                dbg.getBreakpointsHandler().remove(lb);
            }
        }
    }

    private void submitExistingBreakpoints(CDTDebugger dbg) {
        breakpointsAddedDuringInit.set(false);
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        Boolean added = breakpointsAddedDuringInit.get();
        breakpointsAddedDuringInit.remove();
        if (added) {
            return ;
        }
        ScriptsHandler scriptsHandler = dbg.getScriptsHandler();
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

    private void submit(JSLineBreakpoint b, CDTDebugger dbg) {
        dbg.getBreakpointsHandler().add(b);
    }

}
