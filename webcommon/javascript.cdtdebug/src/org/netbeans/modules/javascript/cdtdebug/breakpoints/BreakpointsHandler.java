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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.netbeans.lib.chrome_devtools_protocol.debugger.BreakpointResolved;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;
import org.netbeans.lib.chrome_devtools_protocol.debugger.Paused;
import org.netbeans.lib.chrome_devtools_protocol.debugger.RemoveBreakpointRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointByUrlRequest;
import org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointsActiveRequest;
import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;
import org.netbeans.modules.javascript.cdtdebug.ScriptsHandler;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointStatus;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator;
import org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator.Location;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

@NbBundle.Messages({
    "MSG_BRKP_Unresolved=Not resolved/inactive at current line.",
    "MSG_BRKP_Resolved=Successfully resolved at current line."
})
public class BreakpointsHandler implements CDTDebugger.Listener {

    private final RequestProcessor RP = new RequestProcessor(BreakpointsHandler.class.getName(), 1);

    private final CDTDebugger dbg;
    private final List<JSLineBreakpoint> trackedBreakpoints = new CopyOnWriteArrayList<>();
    private final Map<JSLineBreakpoint, String[]> submittedBreakpoints = new HashMap<>();
    private final Map<String, JSLineBreakpoint> breakpointsById = new HashMap<>();
    private final List<ActiveBreakpointListener> abListeners = new CopyOnWriteArrayList<>();
    private volatile JSLineBreakpoint activeBreakpoint;
    private final List<BreakpointsActiveListener> acListeners = new CopyOnWriteArrayList<>();
    private volatile boolean areBreakpointsActive = true;
    private final PropertyChangeListener bpChangeListener = (PropertyChangeEvent evt) -> {
        switch (evt.getPropertyName()) {
            case JSLineBreakpoint.PROP_ENABLED:
            case JSLineBreakpoint.PROP_CONDITION:
                JSLineBreakpoint source = (JSLineBreakpoint) evt.getSource();
                RP.submit(() -> {
                    unsubmitBreakpoint(source);
                    submitBreakpoint(source);
                });
        }
    };
    private final Consumer<BreakpointResolved> resolvedHandler = (br) -> {
        JSLineBreakpoint b;
        synchronized (submittedBreakpoints) {
            b = breakpointsById.get(br.getBreakpointId());
        }
        if (b != null) {
            JSBreakpointStatus.setValid(b, Bundle.MSG_BRKP_Resolved());
        }
    };

    private final Consumer<Paused> pausedHandler = (p) -> {
        for (String id : p.getHitBreakpoints()) {
            JSLineBreakpoint b;
            synchronized (submittedBreakpoints) {
                b = breakpointsById.get(id);
            }
            if (b != null) {
                setActiveBreakpoint(b);
            }
        }
    };

    @SuppressWarnings("LeakingThisInConstructor")
    public BreakpointsHandler(CDTDebugger dbg) {
        this.dbg = dbg;
        dbg.addListener(this);
        dbg.getConnection().getDebugger().onBreakpointResolved(resolvedHandler);
        dbg.getConnection().getDebugger().onPaused(pausedHandler);
    }


    public void add(JSLineBreakpoint lb) {
        trackedBreakpoints.add(lb);
        lb.addPropertyChangeListener(bpChangeListener);
        RP.submit(() -> submitBreakpoint(lb));
    }

    public void remove(JSLineBreakpoint lb) {
        trackedBreakpoints.remove(lb);
        lb.removePropertyChangeListener(bpChangeListener);
        JSBreakpointStatus.resetValidity(lb);
        RP.submit(() -> unsubmitBreakpoint(lb));
    }

    private void submitBreakpoint(JSLineBreakpoint lb) {
        try {
            synchronized (submittedBreakpoints) {
                JSBreakpointStatus.setInvalid(lb, Bundle.MSG_BRKP_Unresolved());
                String[] ids = createSetRequestArguments(lb)
                        .stream()
                        .map(req -> dbg.getConnection().getDebugger().setBreakpointByUrl(req))
                        .map(cs -> cs.toCompletableFuture())
                        .map(cs -> cs.join())
                        .map(sbbur -> sbbur.getBreakpointId())
                        .collect(Collectors.toList())
                        .toArray(String[]::new);
                submittedBreakpoints.put(lb, ids);
                for(String id: ids) {
                    breakpointsById.put(id, lb);
                }
            }
        } catch (RuntimeException ex) {
            JSBreakpointStatus.setInvalid(lb, "Failed to submit breakpoint");
            throw ex;
        }
        if(! trackedBreakpoints.contains(lb)) {
            unsubmitBreakpoint(lb);
        }
    }

    private void unsubmitBreakpoint(JSLineBreakpoint lb) {
        synchronized (submittedBreakpoints) {
            String[] ids = submittedBreakpoints.remove(lb);
            if (ids != null) {
                Arrays.stream(ids)
                        .map(id -> {
                            breakpointsById.remove(id);
                            return id;
                        })
                        .map(RemoveBreakpointRequest::new)
                        .map(req -> dbg.getConnection().getDebugger().removeBreakpoint(req))
                        .map(cs -> cs.toCompletableFuture())
                        .forEach(cf -> cf.join());
            }
        }
    }

    private List<SetBreakpointByUrlRequest> createSetRequestArguments(JSLineBreakpoint b) {
        if(! b.isEnabled()) {
            return Collections.emptyList();
        }
        FileObject fo = b.getFileObject();
        ScriptsHandler scriptsHandler = dbg.getScriptsHandler();
        List<String> serverPath = new ArrayList<>();
        List<Integer> lineNumber = new ArrayList<>();
        List<Integer> columnNumber = new ArrayList<>();
        if (fo != null) {
            SourceMapsTranslator smt = scriptsHandler.getSourceMapsTranslator();
            if (smt != null) {
                Location loc = new Location(fo, b.getLineNumber() - 1, 0);
                Location cl = smt.getCompiledLocation(loc);
                if (cl != loc) {
                    serverPath.add(scriptsHandler.getServerPath(cl.getFile()));
                    lineNumber.add(cl.getLine());
                    columnNumber.add(cl.getColumn());
                }
            }
            serverPath.add(scriptsHandler.getServerPath(fo));
            lineNumber.add(b.getLineNumber() - 1);
            columnNumber.add(0);
        } else {    // Future BP
            URL url = b.getURL();
            if (scriptsHandler.containsRemoteFile(url)) {
                serverPath.add(scriptsHandler.getServerPath(url));
                lineNumber.add(b.getLineNumber() - 1);
                columnNumber.add(0);
            }
        }
        String condition = (b.isConditional()) ? b.getCondition() : null;
        List<SetBreakpointByUrlRequest> args = new ArrayList<>();
        for (int i = 0; i < serverPath.size(); i++) {
            SetBreakpointByUrlRequest newBreakPoint = new SetBreakpointByUrlRequest();
            try {
                newBreakPoint.setUrl(new URI("file", "", serverPath.get(i), null, null));
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
            newBreakPoint.setLineNumber(lineNumber.get(i));
            newBreakPoint.setColumnNumber(columnNumber.get(i));
            newBreakPoint.setCondition(condition);
            args.add(newBreakPoint);
        }
        return args;
    }

    @Override
    public void notifySuspended(boolean suspended) {
        if (!suspended) {
            setActiveBreakpoint(null);
        }
    }

    @Override
    public void notifyCurrentFrame(CallFrame cf) {
    }

    @Override
    public void notifyFinished() {
        synchronized (submittedBreakpoints) {
            submittedBreakpoints.clear();
            breakpointsById.clear();
        }
        for (JSLineBreakpoint lb : trackedBreakpoints) {
            lb.removePropertyChangeListener(bpChangeListener);
            JSBreakpointStatus.resetValidity(lb);
        }
        setActiveBreakpoint(null);
    }

    public JSLineBreakpoint getActiveBreakpoint() {
        return activeBreakpoint;
    }

    private void setActiveBreakpoint(JSLineBreakpoint activeBreakpoint) {
        this.activeBreakpoint = activeBreakpoint;
        JSBreakpointStatus.setActive(activeBreakpoint);
        for (ActiveBreakpointListener abl : abListeners) {
            abl.notifyActiveBreakpoint(activeBreakpoint);
        }
    }

    public void addActiveBreakpointListener(ActiveBreakpointListener abl) {
        abListeners.add(abl);
    }

    public void removeActiveBreakpointListener(ActiveBreakpointListener abl) {
        abListeners.remove(abl);
    }

    boolean areBreakpointsActive() {
        return areBreakpointsActive;
    }

    void setBreakpointsActive(final boolean active) {
        SetBreakpointsActiveRequest req = new SetBreakpointsActiveRequest();
        req.setActive(active);
        dbg.getConnection()
                .getDebugger()
                .setBreakpointsActive(req)
                .handle((sbar, thr) -> {
                    areBreakpointsActive = active;
                    for (BreakpointsActiveListener acl : acListeners) {
                        acl.breakpointsActivated(active);
                    }
                    return null;
                });
    }

    void addBreakpointsActiveListener(BreakpointsActiveListener abl) {
        acListeners.add(abl);
    }

    void removeBreakpointsActiveListener(BreakpointsActiveListener abl) {
        acListeners.remove(abl);
    }

    /** Fired when an active (hit) breakpoint changes. */
    public static interface ActiveBreakpointListener {

        void notifyActiveBreakpoint(JSLineBreakpoint activeBreakpoint);
    }

    /** Fired when breakpoints are activated/deactivated */
    static interface BreakpointsActiveListener {

        void breakpointsActivated(boolean activated);
    }
}
