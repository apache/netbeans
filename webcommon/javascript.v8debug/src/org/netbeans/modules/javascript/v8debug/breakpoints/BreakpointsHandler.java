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

package org.netbeans.modules.javascript.v8debug.breakpoints;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.lib.v8debug.V8Arguments;
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
    private final V8Debugger.CommandResponseCallback breakpointsCommandsCallback = new BreakpointsCommandsCallback();
    private final Map<V8Arguments, Pair<JSLineBreakpoint, Location>> submittingBreakpoints = new HashMap<>();
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
    public boolean add(JSLineBreakpoint lb) {
        Pair<SetBreakpoint.Arguments, Location> srargsLoc = createSetRequestArguments(lb);
        LOG.log(Level.FINE, "Adding {0}, args = {1}", new Object[]{lb, srargsLoc.first()});
        if (srargsLoc == null) {
            return false;
        }
        synchronized (submittingBreakpoints) {
            submittingBreakpoints.put(srargsLoc.first(), Pair.of(lb, srargsLoc.second()));
        }
        JSBreakpointStatus.setInvalid(lb, Bundle.MSG_BRKP_Unresolved());
        V8Request request = dbg.sendCommandRequest(V8Command.Setbreakpoint, srargsLoc.first(), breakpointsCommandsCallback);
        LOG.log(Level.FINE, "  request = {0}", request);
        if (request == null) {
            // Failed
            synchronized (submittingBreakpoints) {
                submittingBreakpoints.remove(srargsLoc.first());
            }
            return false;
        } else {
            return true;
        }
    }
    
    public boolean remove(JSLineBreakpoint lb) {
        SubmittedBreakpoint sb;
        synchronized (submittedBreakpoints) {
            sb = submittedBreakpoints.get(lb);
            if (sb == null) {
                removeAfterSubmit.add(lb);
                return false;
            }
        }
        sb.notifyDestroyed();
        return requestRemove(lb, sb.getId());
    }
    
    private boolean requestRemove(JSLineBreakpoint lb, long id) {
        ClearBreakpoint.Arguments cbargs = new ClearBreakpoint.Arguments(id);
        V8Request request = dbg.sendCommandRequest(V8Command.Clearbreakpoint, cbargs);
        LOG.log(Level.FINE, "Removing {0}, request = {1}", new Object[]{lb, request});
        return request != null;
    }
    
    @CheckForNull
    private Pair<SetBreakpoint.Arguments, Location> createSetRequestArguments(JSLineBreakpoint b) {
        FileObject fo = b.getFileObject();
        ScriptsHandler scriptsHandler = dbg.getScriptsHandler();
        String serverPath = null;
        long lineNumber = (long) b.getLineNumber() - 1;
        Long columnNumber = null;
        Location bpLocation = null;
        if (fo != null) {
            SourceMapsTranslator smt = dbg.getScriptsHandler().getSourceMapsTranslator();
            if (smt != null) {
                Location loc = new Location(fo, (int) lineNumber, 0);
                Location cl = smt.getCompiledLocation(loc);
                if (cl != loc) {
                    fo = cl.getFile();
                    lineNumber = cl.getLine();
                    columnNumber = (long) cl.getColumn();
                    if (lineNumber == 0) {
                        columnNumber += dbg.getScriptsHandler().getScriptFirstLineColumnShift(fo);
                    }
                    bpLocation = cl;
                }
            }
            serverPath = scriptsHandler.getServerPath(fo);
        } else {    // Future BP
            URL url = b.getURL();
            if (scriptsHandler.containsRemoteFile(url)) {
                serverPath = scriptsHandler.getServerPath(url);
            }
        }
        if (serverPath == null) {
            return null;
        }
        String condition = (b.isConditional()) ? b.getCondition() : null;
        Long groupId = null; // TODO ?
        SetBreakpoint.Arguments args = new SetBreakpoint.Arguments(
                V8Breakpoint.Type.scriptName, serverPath,
                lineNumber, columnNumber, b.isEnabled(),
                condition, null, groupId);
        return Pair.of(args, bpLocation);
    }
    
    static ChangeBreakpoint.Arguments createChangeRequestArguments(SubmittedBreakpoint sb) {
        JSLineBreakpoint b = sb.getBreakpoint();
        String condition = (b.isConditional()) ? b.getCondition() : null;
        return new ChangeBreakpoint.Arguments(sb.getId(), b.isEnabled(), condition, null);
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
        synchronized (submittingBreakpoints) {
            submittingBreakpoints.clear();
        }
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
            sb.updatePosition(line, column);
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

        @NbBundle.Messages({
            "MSG_BRKP_Resolved=Successfully resolved at current line."
        })
        @Override
        public void notifyResponse(V8Request request, V8Response response) {
            Pair<JSLineBreakpoint, Location> lbLoc;
            synchronized (submittingBreakpoints) {
                lbLoc = submittingBreakpoints.remove(request.getArguments());
            }
            if (lbLoc == null) {
                LOG.log(Level.INFO, "Did not find a submitting breakpoint for response {0}, request was {1}",
                        new Object[]{response, request});
                return ;
            }
            JSLineBreakpoint lb = lbLoc.first();
            if (response != null) {
                SetBreakpoint.ResponseBody sbrb = (SetBreakpoint.ResponseBody) response.getBody();
                long id = sbrb.getBreakpoint();
                Location bpLoc = lbLoc.second();
                SubmittedBreakpoint sb = new SubmittedBreakpoint(lb, id, bpLoc, sbrb.getActualLocations(), dbg);
                boolean removed;
                synchronized (submittedBreakpoints) {
                    submittedBreakpoints.put(lb, sb);
                    breakpointsById.put(id, sb);
                    removed = removeAfterSubmit.remove(lb);
                }
                if (removed) {
                    requestRemove(lb, id);
                    sb.notifyDestroyed();
                } else {
                    JSBreakpointStatus.setValid(lb, Bundle.MSG_BRKP_Resolved());
                }
            } else {
                JSBreakpointStatus.setInvalid(lb, response.getErrorMessage());
            }
        }
        
    }
    
}
