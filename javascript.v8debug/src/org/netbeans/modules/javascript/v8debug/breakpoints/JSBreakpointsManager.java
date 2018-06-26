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

    private void submit(JSLineBreakpoint b, V8Debugger dbg) {
        dbg.getBreakpointsHandler().add(b);
    }
    
}
