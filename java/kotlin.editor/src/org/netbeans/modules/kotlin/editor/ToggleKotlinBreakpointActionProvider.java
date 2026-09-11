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
package org.netbeans.modules.kotlin.editor;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.spi.debugger.*;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
@ActionsProvider.Registrations({
    @ActionsProvider.Registration(path="netbeans-JPDASession", actions={ "toggleBreakpoint" }, activateForMIMETypes={ "text/x-kotlin" }),
    @ActionsProvider.Registration(path="", actions={ "toggleBreakpoint" }, activateForMIMETypes={ "text/x-kotlin" })
})
public final class ToggleKotlinBreakpointActionProvider extends ActionsProvider {
    public ToggleKotlinBreakpointActionProvider() {
    }

    @Override
    public Set<String> getActions() {
        return Collections.singleton("toggleBreakpoint");
    }

    @Override
    public void doAction(Object o) {
        final DebuggerManager m = DebuggerManager.getDebuggerManager ();
        for (EditorContext ctx : m.lookup(null, EditorContext.class)) {
            String url = ctx.getCurrentURL();
            int line = ctx.getCurrentLineNumber();
            if (url != null && line >= 0) {
                if (!url.endsWith(".kt")) {
                    continue;
                }
                if (removeExistingBreakpoint(m, line, url)) {
                    return;
                }
                createNewBreakpoint(url, line, m);
                return;
            }
        }
    }

    private static final Pattern PACKAGE = Pattern.compile("package *([\\p{Alnum}+\\.$]+) *");
    private void createNewBreakpoint(String url, int line, final DebuggerManager m) {
        try {
            var b = LineBreakpoint.create(url, line);

            FileObject fo = URLMapper.findFileObject(URI.create(url).toURL());
            for (String code : fo.asLines()) {
                Matcher match = PACKAGE.matcher(code);
                if (match.matches()) {
                    String pkg = match.group(1);
                    int slash = url.lastIndexOf("/");
                    int dot = url.indexOf('.', slash + 1);
                    if (dot >= 0) {
                        String filter = pkg + "." + url.substring(slash + 1, dot) + "*";
                        b.setPreferredClassName(filter);
                    }
                    break;
                }
            }
            m.addBreakpoint(b);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private boolean removeExistingBreakpoint(final DebuggerManager m, int line, String url) {
        for (Breakpoint b : m.getBreakpoints()) {
            if (b instanceof LineBreakpoint lb) {
                if (lb.getLineNumber() == line && url.equals(lb.getURL())) {
                    m.removeBreakpoint(lb);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isEnabled(Object o) {
        final DebuggerManager m = DebuggerManager.getDebuggerManager ();
        for (EditorContext ctx : m.lookup(null, EditorContext.class)) {
            String url = ctx.getCurrentURL();
            int line = ctx.getCurrentLineNumber();
            if (url != null && line >= 0) {
                if (url.endsWith(".kt")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void addActionsProviderListener(ActionsProviderListener al) {
    }

    @Override
    public void removeActionsProviderListener(ActionsProviderListener al) {
    }

}
