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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.lib.v8debug.V8Breakpoint;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.commands.ChangeBreakpoint;
import org.netbeans.lib.v8debug.commands.ClearBreakpoint;
import org.netbeans.lib.v8debug.commands.Flags;
import org.netbeans.lib.v8debug.commands.SetBreakpoint;
import org.netbeans.lib.v8debug.events.BreakEventBody;
import org.netbeans.modules.javascript.v8debug.ScriptsHandler;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointStatus;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator;
import org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator.Location;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.WeakSet;

/**
 *
 * @author Martin Entlicher
 */
public class BreakpointsHandler implements V8Debugger.Listener {
    
    private static final Logger LOG = Logger.getLogger(BreakpointsHandler.class.getName());
    
    private final V8Debugger dbg;
    private final Map<JSLineBreakpoint, SubmittedBreakpoint> submittedBreakpoints = new HashMap<>();
    private final Map<Long, SubmittedBreakpoint> breakpointsById = new HashMap<>();
    private final Set<JSLineBreakpoint> removeAfterSubmit = new WeakSet<>();
    private final List<ActiveBreakpointListener> abListeners = new CopyOnWriteArrayList<ActiveBreakpointListener>();
    private volatile JSLineBreakpoint activeBreakpoint;
    private final List<BreakpointsActiveListener> acListeners = new CopyOnWriteArrayList<>();
    private volatile boolean areBreakpointsActive = true;
    
    public BreakpointsHandler(V8Debugger dbg) { // TODO pass in initially submitted breakpoints in the debuggee
        this.dbg = dbg;
        dbg.addListener(this);
    }
    
    @NbBundle.Messages({
        "MSG_BRKP_Unresolved=Not resolved/inactive at current line."
    })
    public void add(JSLineBreakpoint lb) {
        List<Pair<SetBreakpoint.Arguments, Location>> srargsLoc = createSetRequestArguments(lb);
        LOG.log(Level.FINE, "Adding {0}, args = {1}", new Object[]{lb, srargsLoc});
        if (srargsLoc == null) {
            return;
        }
        JSBreakpointStatus.setInvalid(lb, Bundle.MSG_BRKP_Unresolved());
        BreakpointsCommandsCallback breakpointsCommandsCallback = new BreakpointsCommandsCallback(
                lb,
                srargsLoc
        );

        for(Pair<SetBreakpoint.Arguments, Location> arg: srargsLoc) {
            V8Request request = dbg.sendCommandRequest(V8Command.Setbreakpoint, arg.first(), breakpointsCommandsCallback);
            LOG.log(Level.FINE, "  request = {0}", request);
        }
    }

    public void remove(JSLineBreakpoint lb) {
        SubmittedBreakpoint sb;
        synchronized (submittedBreakpoints) {
            sb = submittedBreakpoints.get(lb);
            if (sb == null) {
                removeAfterSubmit.add(lb);
            }
        }
        sb.notifyDestroyed();
        requestRemove(lb, sb.getIds());
    }

    private void requestRemove(JSLineBreakpoint lb, List<Long> ids) {
        for (Long id : ids) {
            ClearBreakpoint.Arguments cbargs = new ClearBreakpoint.Arguments(id);
            V8Request request = dbg.sendCommandRequest(V8Command.Clearbreakpoint, cbargs);
            LOG.log(Level.FINE, "Removing {0}, request = {1}", new Object[]{lb, request});
        }
    }

    @CheckForNull
    private List<Pair<SetBreakpoint.Arguments, Location>> createSetRequestArguments(JSLineBreakpoint b) {
        FileObject fo = b.getFileObject();
        ScriptsHandler scriptsHandler = dbg.getScriptsHandler();
        List<String> serverPath = new ArrayList<>();
        List<Long> lineNumber = new ArrayList<>();
        List<Long> columnNumber = new ArrayList<>();
        List<Location> bpLocation = new ArrayList<>();
        if (fo != null) {
            serverPath.add(scriptsHandler.getServerPath(fo));
            lineNumber.add(((long) (b.getLineNumber() - 1)));
            columnNumber.add(0L);
            bpLocation.add(null);
            SourceMapsTranslator smt = dbg.getScriptsHandler().getSourceMapsTranslator();
            if (smt != null) {
                Location loc = new Location(fo, b.getLineNumber() - 1, 0);
                Location cl = smt.getCompiledLocation(loc);
                if (cl != loc) {
                    fo = cl.getFile();
                    long relocatedLineNumber = cl.getLine();
                    long relocatedColumnNumber = (long) cl.getColumn();
                    if (relocatedLineNumber == 0) {
                        relocatedColumnNumber += dbg.getScriptsHandler().getScriptFirstLineColumnShift(fo);
                    }
                    serverPath.add(scriptsHandler.getServerPath(fo));
                    lineNumber.add(relocatedLineNumber);
                    columnNumber.add(relocatedColumnNumber);
                    bpLocation.add(cl);
                }
            }
        } else {    // Future BP
            URL url = b.getURL();
            if (scriptsHandler.containsRemoteFile(url)) {
                serverPath.add(scriptsHandler.getServerPath(url));
                lineNumber.add(((long) (b.getLineNumber() - 1)));
                columnNumber.add(0L);
                bpLocation.add(null);
            }
        }
        Long groupId = null; // TODO ?
        String condition = (b.isConditional()) ? b.getCondition() : null;
        List<Pair<SetBreakpoint.Arguments, Location>> args = new ArrayList<>();
        for (int i = 0; i < serverPath.size(); i++) {
            SetBreakpoint.Arguments newBreakPoint = new SetBreakpoint.Arguments(
                    V8Breakpoint.Type.scriptName, serverPath.get(i),
                    lineNumber.get(i), columnNumber.get(i), b.isEnabled(),
                    condition, null, groupId);
            args.add(Pair.of(newBreakPoint, bpLocation.get(i)));
        }
        return args;
    }
    
    static List<ChangeBreakpoint.Arguments> createChangeRequestArguments(SubmittedBreakpoint sb) {
        JSLineBreakpoint b = sb.getBreakpoint();
        String condition = (b.isConditional()) ? b.getCondition() : null;
        return sb.getIds()
                .stream()
                .map(id -> new ChangeBreakpoint.Arguments(id, b.isEnabled(), condition, null))
                .collect(Collectors.toList());
    }
    
    public void event(BreakEventBody beb) {
        long[] ids = beb.getBreakpoints();
        if (ids == null) {
            return ;
        }
        for (long id : ids) {
            SubmittedBreakpoint sb;
            synchronized (submittedBreakpoints) {
                sb = breakpointsById.get(id);
            }
            if (sb == null) {
                continue;
            }
            JSLineBreakpoint b = sb.getBreakpoint();
            setActiveBreakpoint(b);
        }
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
        Collection<SubmittedBreakpoint> sbs;
        synchronized (submittedBreakpoints) {
            sbs = new ArrayList<>(submittedBreakpoints.values());
            submittedBreakpoints.clear();
            breakpointsById.clear();
        }
        for (SubmittedBreakpoint sb : sbs) {
            sb.notifyDestroyed();
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
        Flags.Arguments args = new Flags.Arguments(Flags.FLAG_BREAK_POINTS_ACTIVE, active);
        V8Request fRequest = dbg.sendCommandRequest(V8Command.Flags, args, new V8Debugger.CommandResponseCallback() {
            @Override
            public void notifyResponse(V8Request request, V8Response response) {
                if (response != null && response.isSuccess()) {
                    areBreakpointsActive = active;
                    for (BreakpointsActiveListener acl : acListeners) {
                        acl.breakpointsActivated(active);
                    }
                }
            }
        });
    }
    
    void addBreakpointsActiveListener(BreakpointsActiveListener abl) {
        acListeners.add(abl);
    }

    void removeBreakpointsActiveListener(BreakpointsActiveListener abl) {
        acListeners.remove(abl);
    }
    
    public void positionChanged(long bpId, long line, long column) {
        SubmittedBreakpoint sb;
        synchronized (submittedBreakpoints) {
            sb = breakpointsById.get(bpId);
        }
        if (sb != null) {
            sb.updatePosition(bpId, line, column);
        }
    }

    /** Fired when an active (hit) breakpoint changes. */
    public static interface ActiveBreakpointListener {
        
        void notifyActiveBreakpoint(JSLineBreakpoint activeBreakpoint);
    }
    
    /** Fired when breakpoints are activated/deactivated */
    static interface BreakpointsActiveListener {

        void breakpointsActivated(boolean activated);
    }
    
    private final class BreakpointsCommandsCallback implements V8Debugger.CommandResponseCallback {
        private final JSLineBreakpoint lb;
        private final IdentityHashMap<SetBreakpoint.Arguments, V8Response> responses = new IdentityHashMap<>();
        private final IdentityHashMap<SetBreakpoint.Arguments, Location> requests = new IdentityHashMap<>();

        public BreakpointsCommandsCallback(JSLineBreakpoint lb, List<Pair<SetBreakpoint.Arguments, Location>> requestlist) {
            for(Pair<SetBreakpoint.Arguments, Location> request: requestlist) {
                requests.put(request.first(), request.second());
            }
            this.lb = lb;
        }

        @NbBundle.Messages({
            "MSG_BRKP_Resolved=Successfully resolved at current line."
        })
        @Override
        public void notifyResponse(V8Request request, V8Response response) {
            responses.put((SetBreakpoint.Arguments) request.getArguments(), response);
            if (responses.size() == requests.size()) {
                postProcess();
            }
        }

        private void postProcess() {
            boolean failed = false;
            String errorMessage = null;
            for (SetBreakpoint.Arguments arg : requests.keySet()) {
                V8Response res = responses.get(arg);
                if (res.getBody() == null) {
                    failed = true;
                    errorMessage = res.getErrorMessage();
                    break;
                }
            }
            if (failed) {
                JSBreakpointStatus.setInvalid(lb, errorMessage);
            } else {
                List<Long> ids = new ArrayList<>();
                List<Location> locations = new ArrayList<>();
                List<V8Breakpoint.ActualLocation[]> actualLocations = new ArrayList<>();
                for (SetBreakpoint.Arguments arg : requests.keySet()) {
                    V8Response res = responses.get(arg);
                    SetBreakpoint.ResponseBody sbrb = (SetBreakpoint.ResponseBody) res.getBody();
                    ids.add(sbrb.getBreakpoint());
                    locations.add(requests.get(arg));
                    actualLocations.add(sbrb.getActualLocations());
                }
                SubmittedBreakpoint sb = new SubmittedBreakpoint(lb, ids, locations, actualLocations, dbg);
                boolean removed;
                synchronized (submittedBreakpoints) {
                    submittedBreakpoints.put(lb, sb);
                    for(Long id: ids) {
                        breakpointsById.put(id, sb);
                    }
                    removed = removeAfterSubmit.remove(lb);
                }
                if (removed) {
                    requestRemove(lb, ids);
                    sb.notifyDestroyed();
                } else {
                    JSBreakpointStatus.setValid(lb, Bundle.MSG_BRKP_Resolved());
                }
            }
        }
    }
    
}
