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
package org.netbeans.modules.web.webkit.debugging.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.netbeans.modules.web.webkit.debugging.APIFactory;
import org.netbeans.modules.web.webkit.debugging.LiveHTML;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.debugger.Script;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.openide.util.RequestProcessor;

/**
 * See Debugger section of WebKit Remote Debugging Protocol for more details.
 */
public final class Debugger {
    
    public static final String PROP_CURRENT_FRAME = "currentFrame";     // NOI18N
    public static final String PROP_BREAKPOINTS_ACTIVE = "breakpointsActive"; // NOI18N
    
    private static final String COMMAND_ENABLE = "Debugger.enable";             // NOI18N
    private static final String COMMAND_DISABLE = "Debugger.disable";           // NOI18N
    private static final String COMMAND_STEP_OVER = "Debugger.stepOver";        // NOI18N
    private static final String COMMAND_STEP_INTO = "Debugger.stepInto";        // NOI18N
    private static final String COMMAND_STEP_OUT = "Debugger.stepOut";          // NOI18N
    private static final String COMMAND_PAUSE = "Debugger.pause";               // NOI18N
    private static final String COMMAND_RESUME = "Debugger.resume";             // NOI18N
    private static final String COMMAND_SET_BRKP_BY_URL = "Debugger.setBreakpointByUrl";// NOI18N
    private static final String COMMAND_SET_BRKP_DOM = "DOMDebugger.setDOMBreakpoint";  // NOI18N
    private static final String COMMAND_SET_BRKP_XHR = "DOMDebugger.setXHRBreakpoint";  // NOI18N
    private static final String COMMAND_SET_BRKP_EVENT = "DOMDebugger.setEventListenerBreakpoint";  // NOI18N
    private static final String COMMAND_SET_BRKP_INSTR = "DOMDebugger.setInstrumentationBreakpoint";// NOI18N
    private static final String COMMAND_REMOVE_BRKP = "Debugger.removeBreakpoint";      // NOI18N
    private static final String COMMAND_REMOVE_BRKP_DOM = "DOMDebugger.removeDOMBreakpoint";    // NOI18N
    private static final String COMMAND_REMOVE_BRKP_XHR = "DOMDebugger.removeXHRBreakpoint";    // NOI18N
    private static final String COMMAND_REMOVE_BRKP_EVENT = "DOMDebugger.removeEventListenerBreakpoint";    // NOI18N
    private static final String COMMAND_REMOVE_BRKP_INSTR = "DOMDebugger.removeInstrumentationBreakpoint";  // NOI18N
    private static final String COMMAND_SET_BRKPS_ACTIVE = "Debugger.setBreakpointsActive";     // NOI18N
    
    private static final String RESPONSE_BRKP_RESOLVED = "Debugger.breakpointResolved";         // NOI18N
    private static final String RESPONSE_GLOB_OBJECT_CLEARED = "Debugger.globalObjectCleared";  // NOI18N
    private static final String RESPONSE_PAUSED = "Debugger.paused";            // NOI18N
    private static final String RESPONSE_RESUMED = "Debugger.resumed";          // NOI18N
    private static final String RESPONSE_SCRIPT_PARSED = "Debugger.scriptParsed";// NOI18N
    
    private static final Logger LOG = Logger.getLogger(Debugger.class.getName());

    private static boolean lastBreakpointsActive = true;
    
    private final TransportHelper transport;
    private boolean enabled = false;
    private boolean suspended = false;
    private final Callback callback;
    private boolean initDOMLister = true;
    private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private final List<ScriptsListener> scriptsListeners = new CopyOnWriteArrayList<ScriptsListener>();
    private final PropertyChangeSupport pchs = new PropertyChangeSupport(this);
    private final Map<String, Script> scripts = new HashMap<String, Script>();
    private final WebKitDebugging webkit;
    private List<CallFrame> currentCallStack = new ArrayList<CallFrame>();
    private CallFrame currentCallFrame = null;
    private boolean breakpointsActive;
    private final Object breakpointsActiveLock = new Object();
    private final Map<String, Breakpoint> breakpointsById = Collections.synchronizedMap(new HashMap<String, Breakpoint>());
    private boolean inLiveHTMLMode = false;
    private RequestProcessor.Task latestSnapshotTask;   
    private final Object ENABLED_LOCK = new Object();

    Debugger(TransportHelper transport, WebKitDebugging webkit) {
        this.transport = transport;
        this.webkit = webkit;
        this.callback = new Callback();
        this.transport.addListener(callback);
    }
    
    public boolean enable() {
        synchronized (ENABLED_LOCK) {
            if (enabled) {
                return true;
            }
            breakpointsActive = true; // By default, breakpoints are active initially.
            Response resp = transport.sendBlockingCommand(new Command(COMMAND_ENABLE));
            if (resp != null && resp.getResponse() != null && resp.getResponse().get("error") != null) {
                LOG.info("Enable failed: "+resp.getResponse()); // NOI18N
                return false;
            }

            // always enable Page and Network; at the moment only Live HTML is using them
            // but I expect that soon it will be used somewhere else as well
            webkit.getPage().enable();
            webkit.getNetwork().enable();
            webkit.getConsole().enable();
            webkit.getDOM().enable();
            webkit.getCSS().enable();

            enabled = true;
        }
        
        setBreakpointsActive(lastBreakpointsActive);
        for (Listener l : listeners ) {
            l.enabled(true);
        }
        
        return true;
    }

    public void enableDebuggerInLiveHTMLMode() {
        inLiveHTMLMode = true;
        
        enable();
        
        latestSnapshotTask = transport.getRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                recordDocumentChange(System.currentTimeMillis(), null, false, false);
            }
        });
        
        initDOMLister = true;
        addEventBreakpoint("DOMContentLoaded");
        addEventBreakpoint("load");
    }

    public boolean isInLiveHTMLMode() {
        return inLiveHTMLMode;
    }
    
    public void disable() {
        synchronized (ENABLED_LOCK) {
            if (!enabled) {
                return;
            }
            webkit.getPage().disable();
            webkit.getNetwork().disable();
            webkit.getConsole().disable();
            webkit.getCSS().disable();
            webkit.getDOM().disable();
            transport.sendCommand(new Command(COMMAND_DISABLE));
            enabled = false;
            suspended = false;
            currentCallStack.clear();
            currentCallFrame = null;
            initDOMLister = true;
        }
        for (Listener l : listeners ) {
            l.enabled(false);
        }
    }

    public void stepOver() {
        doCommand(COMMAND_STEP_OVER);
    }
    
    public void stepInto() {
        doCommand(COMMAND_STEP_INTO);
    }
    
    public void stepOut() {
        doCommand(COMMAND_STEP_OUT);
    }
    
    public void resume() {
        doCommand(COMMAND_RESUME);
    }
    
    public void pause() {
        doCommand(COMMAND_PAUSE);
    }
    
    private void doCommand(String name) {
        transport.sendCommand(new Command(name));
    }
    
    public boolean isSuspended() {
        return suspended;
    }
    
    public boolean isEnabled() {
        synchronized (ENABLED_LOCK) {
            return enabled;
        }
    }
    
    /**
     * Add a listener for debugger state changes.
     * @param l a state change listener
     */
    public void addListener(Listener l) {
        listeners.add(l);
    }
    
    /**
     * Remove a listener for debugger state changes.
     * @param l a state change listener
     */
    public void removeListener(Listener l) {
        listeners.remove(l);
    }
    
    /**
     * Add a listener for debugger state changes.
     * @param l a state change listener
     */
    public void addScriptsListener(ScriptsListener l) {
        scriptsListeners.add(l);
    }
    
    /**
     * Remove a listener for debugger state changes.
     * @param l a state change listener
     */
    public void removeScriptsListener(ScriptsListener l) {
        scriptsListeners.remove(l);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pchs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pchs.removePropertyChangeListener(l);
    }
    
    private void notifyResumed() {
        suspended = false;
        setCurrentCallFrame(null);
        for (Listener l : listeners ) {
            l.resumed();
        }
    }
    
    public URL getConnectionURL() {
        return transport.getConnectionURL();
    }
    
    private List<CallFrame> createCallStack(JSONArray callFrames) {
        List<CallFrame> callStack = new ArrayList<CallFrame>();
        for (Object cf : callFrames) {
            callStack.add(APIFactory.createCallFrame((JSONObject)cf, webkit, transport));
        }
        return callStack;
    }
    
    private void notifyPaused(JSONArray callFrames, String reason, JSONObject data) {
        suspended = true;
        List<CallFrame> callStack = createCallStack(callFrames);
        setCurrentCallStack(callStack);
        if (!callStack.isEmpty()) {
            setCurrentCallFrame(callStack.get(0));
        } else {
            setCurrentCallFrame(null);
        }
        for (Listener l : listeners ) {
            l.paused(callStack, reason);
        }
    }
    
    private void notifyReset() {
        if (!breakpointsActive) {
            // Repeat that the breakpoints are not active
            JSONObject params = new JSONObject();
            params.put("active", false);
            transport.sendCommand(new Command(COMMAND_SET_BRKPS_ACTIVE, params));
        }
        for (Listener l : listeners ) {
            l.reset();
        }
    }

    private JSONArray normalizeStackTrace(JSONArray callStack) {
        JSONArray res = new JSONArray();
        for (Object o : callStack) {
            JSONObject cf = (JSONObject)o;
            JSONObject ncf = new JSONObject();
            ncf.put("lineNumber", ((JSONObject)cf.get("location")).get("lineNumber"));
            ncf.put("columnNumber", ((JSONObject)cf.get("location")).get("columnNumber"));
            ncf.put("function", cf.get("functionName"));
            Script sc = getScript((String)((JSONObject)(cf.get("location"))).get("scriptId"));
            if (sc == null) {
                continue;
            }
            ncf.put("script", sc.getURL());
            res.add(ncf);
        }
        return res;
    }

    private void addScript(JSONObject data) {
        Script script = APIFactory.createScript(data, webkit);
        synchronized (this) {
            scripts.put(script.getID(), script);
        }
        for (ScriptsListener sl : scriptsListeners) {
            sl.scriptParsed(script);
        }
    }
    
    public synchronized Script getScript(String scriptID) {
        return scripts.get(scriptID);
    }

    public synchronized List<CallFrame> getCurrentCallStack() {
        assert isSuspended();
        return currentCallStack;
    }
    
    private synchronized void setCurrentCallStack(List<CallFrame> callstack) {
        this.currentCallStack = callstack;
    }
    
    public synchronized CallFrame getCurrentCallFrame() {
        return currentCallFrame;
    }
    
    /**
     * Set the current call frame.
     * @param frame the actual call frame
     * @throws IllegalArgumentException when the frame is not on the current call stack.
     */
    public void setCurrentCallFrame(CallFrame frame) {
        CallFrame lastFrame;
        synchronized (this) {
            if (frame != null) {
                assert isSuspended();
                if (!currentCallStack.contains(frame)) {
                    throw new IllegalArgumentException("Unknown frame: "+frame);
                }
            }
            lastFrame = this.currentCallFrame;
            this.currentCallFrame = frame;
        }
        pchs.firePropertyChange(PROP_CURRENT_FRAME, lastFrame, frame);
    }
    
    /* not tested yet
    public void restartFrame(CallFrame frame) {
        JSONObject params = new JSONObject();
        params.put("callFrameId", frame);
        Response resp = transport.sendBlockingCommand(new Command("Debugger.restartFrame", params));
        if (resp != null) {
            notifyPaused((JSONArray)resp.getResponse().get("callFrames"), "", null);
        }
    }*/
    
    @SuppressWarnings("unchecked")    
    public Breakpoint addLineBreakpoint(String url, int lineNumber, Integer columnNumber, String condition) throws BreakpointException {
        if (inLiveHTMLMode) {
            // ignore line breakpoints when in Live HTML mode
            return null;
        }
        JSONObject params = new JSONObject();
        params.put("lineNumber", lineNumber);
        params.put("urlRegex", createURLRegex(url));
        if (columnNumber != null) {
            params.put("columnNumber", columnNumber);
        }
        if (condition != null) {
            params.put("condition", condition);
        }
        Response resp = transport.sendBlockingCommand(new Command(COMMAND_SET_BRKP_BY_URL, params));
        if (resp != null) {
            if (resp.getException() != null) {
                // transport is broken
                String message;
                JSONObject response = resp.getResponse();
                Object error = null;
                if (response != null) {
                    error = response.get("error");
                }
                if (error instanceof String) {
                    message = parseError((String) error);
                } else {
                    message = resp.getException().getLocalizedMessage();
                }
                throw new BreakpointException(message);
            }
            JSONObject result = (JSONObject) resp.getResponse().get("result");
            if (result != null) {
                Breakpoint b = APIFactory.createBreakpoint(result, webkit);
                breakpointsById.put(b.getBreakpointID(), b);
                return b;
            } else {
                // What can we do when we have no results?
                LOG.log(Level.WARNING, "No result in setBreakpointByUrl response: {0}", resp);
            }
        }
        return null;
    }

    private String createURLRegex(String url) {
        String baseURL;
        try {
            URL u = new URL(url);
            baseURL = u.getProtocol()+":"+
                ((u.getAuthority() != null && u.getAuthority().length() > 0) ?
                    "//"+u.getAuthority() : "")+
                ((u.getPath() != null) ?
                    u.getPath() : "");
        } catch (MalformedURLException ex) {
            baseURL = url;
            int i = url.indexOf('?');
            if (i > 0) {
                baseURL = baseURL.substring(0, i);
            }
            i = url.indexOf('#');
            if (i > 0) {
                baseURL = baseURL.substring(0, i);
            }
        }
        // Escape regex special characters:
        baseURL = baseURL.replace("\\", "\\\\");
        baseURL = baseURL.replace(".", "\\.");
        baseURL = baseURL.replace("*", "\\*");
        baseURL = baseURL.replace("+", "\\+");
        baseURL = baseURL.replace("(", "\\(");
        baseURL = baseURL.replace(")", "\\)");
        baseURL = baseURL.replace("{", "\\{");
        baseURL = baseURL.replace("}", "\\}");
        baseURL = baseURL.replace("|", "\\|");
        //System.err.println("Escaped baseURL = '"+baseURL+"'");
        return "^"+baseURL+".*";
    }

    @SuppressWarnings("unchecked")    
    public void removeLineBreakpoint(Breakpoint b) {
        JSONObject params = new JSONObject();
        String id = b.getBreakpointID();
        params.put("breakpointId", id);
        transport.sendBlockingCommand(new Command(COMMAND_REMOVE_BRKP, params));
        breakpointsById.remove(id);
    }
    
    public Breakpoint addDOMBreakpoint(Node node, String type) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId());
        params.put("type", type);
        Response resp = transport.sendBlockingCommand(new Command(COMMAND_SET_BRKP_DOM, params));
        if (resp != null) {
            if (resp.getException() != null) {
                // transport is broken
                return null;
            }
            JSONObject result = (JSONObject) resp.getResponse().get("result");
            if (result != null) {
                Breakpoint b = APIFactory.createBreakpoint(result, webkit);
                return b;
            } else {
                // What can we do when we have no results?
                LOG.log(Level.WARNING, "No result in setDOMBreakpoint response: {0}", resp);
            }
        }
        return null;
    }
    
    public void removeDOMBreakpoint(Node node, String type) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId());
        params.put("type", type);
        transport.sendBlockingCommand(new Command(COMMAND_REMOVE_BRKP_DOM, params));
    }
    
    public Breakpoint addXHRBreakpoint(String urlSubstring) {
        JSONObject params = new JSONObject();
        params.put("url", urlSubstring);
        Response resp = transport.sendBlockingCommand(new Command(COMMAND_SET_BRKP_XHR, params));
        if (resp != null) {
            if (resp.getException() != null) {
                // transport is broken
                return null;
            }
            JSONObject result = (JSONObject) resp.getResponse().get("result");
            if (result != null) {
                Breakpoint b = APIFactory.createBreakpoint(result, webkit);
                return b;
            } else {
                // What can we do when we have no results?
                LOG.log(Level.WARNING, "No result in setXHRBreakpoint response: {0}", resp);
            }
        }
        return null;
    }
    
    public void removeXHRBreakpoint(String urlSubstring) {
        JSONObject params = new JSONObject();
        params.put("url", urlSubstring);
        transport.sendBlockingCommand(new Command(COMMAND_REMOVE_BRKP_XHR, params));
    }
    
    public static final String DOM_BREAKPOINT_SUBTREE = "subtree-modified";
    public static final String DOM_BREAKPOINT_ATTRIBUTE = "attribute-modified";
    public static final String DOM_BREAKPOINT_NODE = "node-removed";

    public Breakpoint addEventBreakpoint(String event) {
        JSONObject params = new JSONObject();
        params.put("eventName", event);
        Response resp = transport.sendBlockingCommand(new Command(COMMAND_SET_BRKP_EVENT, params));
        if (resp != null) {
            if (resp.getException() != null) {
                // transport is broken
                return null;
            }
            JSONObject result = (JSONObject) resp.getResponse().get("result");
            if (result != null) {
                Breakpoint b = APIFactory.createBreakpoint(result, webkit);
                return b;
            } else {
                // What can we do when we have no results?
                LOG.log(Level.WARNING, "No result in setEventListenerBreakpoint response: {0}", resp);
            }
        }
        return null;
    }
    
    public void removeEventBreakpoint(String event) {
        JSONObject params = new JSONObject();
        params.put("eventName", event);
        transport.sendBlockingCommand(new Command(COMMAND_REMOVE_BRKP_EVENT, params));
    }
    
    public Breakpoint addInstrumentationBreakpoint(String event) {
        JSONObject params = new JSONObject();
        params.put("eventName", event);
        Response resp = transport.sendBlockingCommand(new Command(COMMAND_SET_BRKP_INSTR, params));
        if (resp != null) {
            if (resp.getException() != null) {
                // transport is broken
                return null;
            }
            JSONObject result = (JSONObject) resp.getResponse().get("result");
            if (result != null) {
                Breakpoint b = APIFactory.createBreakpoint(result, webkit);
                return b;
            } else {
                // What can we do when we have no results?
                LOG.log(Level.WARNING, "No result in setEventListenerBreakpoint response: {0}", resp);
            }
        }
        return null;
    }
    
    public void removeInstrumentationBreakpoint(String event) {
        JSONObject params = new JSONObject();
        params.put("eventName", event);
        transport.sendBlockingCommand(new Command(COMMAND_REMOVE_BRKP_INSTR, params));
    }
    
    public boolean areBreakpointsActive() {
        return breakpointsActive;
    }
    
    public void setBreakpointsActive(boolean active) {
        boolean oldActive;
        synchronized (breakpointsActiveLock) {
            oldActive = breakpointsActive;
            if (oldActive != active) {
                JSONObject params = new JSONObject();
                params.put("active", active);
                transport.sendBlockingCommand(new Command(COMMAND_SET_BRKPS_ACTIVE, params));
                breakpointsActive = active;
                lastBreakpointsActive = active;
            }
        }
        if (oldActive != active) {
            pchs.firePropertyChange(PROP_BREAKPOINTS_ACTIVE, oldActive, active);
        }
    }
    
    private void recordDocumentChange(long timeStamp, JSONArray callStack, boolean attachDOMListeners, boolean realChange) {
        assert inLiveHTMLMode;
        
        Node n = webkit.getDOM().getDocument();
        if (attachDOMListeners) {
            addDOMBreakpoint(n, Debugger.DOM_BREAKPOINT_SUBTREE);
            removeEventBreakpoint("DOMContentLoaded");
            removeEventBreakpoint("load");
        }
        String content = webkit.getDOM().getOuterHTML(n);
        JSONArray callStack2 = callStack != null ? normalizeStackTrace(callStack) : null;
        if (realChange) {
            LiveHTML.getDefault().storeDocumentVersionBeforeChange(transport.getConnectionURL(), 
                    timeStamp, content, callStack2 != null ? callStack2.toJSONString() : null);
            resume();
            latestSnapshotTask.schedule(345);
        } else {
            LiveHTML.getDefault().storeDocumentVersionAfterChange(transport.getConnectionURL(), 
                    timeStamp, content);
        }
    }
    
    // Parse error response like:
    // {"message":"{\"code\":-32000,\"message\":\"Breakpoint at specified location already exists.\"}"}
    private static String parseError(String error) {
        try {
            Object eo = new org.json.simple.parser.JSONParser().parse(error);
            if (eo instanceof JSONObject) {
                error = (String) ((JSONObject) eo).get("message");
                eo = new org.json.simple.parser.JSONParser().parse(error);
                if (eo instanceof JSONObject) {
                    error = (String) ((JSONObject) eo).get("message");
                }
            }
        } catch (ParseException ex) {
        }
        return error;
    }
    
    private class Callback implements ResponseCallback {

        @Override
        public void handleResponse(Response response) {
            String method = response.getMethod();
            if (RESPONSE_RESUMED.equals(method)) {
                notifyResumed();
                webkit.getRuntime().releaseNetBeansObjectGroup();
            } else if (RESPONSE_PAUSED.equals(method)) {
                JSONObject params = response.getParams();

                if (inLiveHTMLMode) {
                    final long timestamp = System.currentTimeMillis();
                    final JSONObject data = (JSONObject)params.get("data");

                    boolean internalSuspend = false;
                    boolean attachDOMListeners = false;
                    if ("DOM".equals(params.get("reason"))) {
                        internalSuspend = true;
                    }
                    if ("EventListener".equals(params.get("reason"))) {
                        if (data != null && ("listener:DOMContentLoaded".equals(data.get("eventName")) ||
                                "listener:load".equals(data.get("eventName")))) {
                            internalSuspend = true;
                            if (initDOMLister) {
                                attachDOMListeners = true;
                            }
                            initDOMLister = false;
                        }
                    }
                    if (internalSuspend) {
                        final JSONArray callStack = (JSONArray)params.get("callFrames");
                        final boolean finalAttachDOMListeners = attachDOMListeners;
                        transport.getRequestProcessor().post(new Runnable() {
                            @Override
                            public void run() {
                                recordDocumentChange(timestamp, callStack, finalAttachDOMListeners, true);
                            }
                        });
                    }
                } else {
                    JSONArray frames = (JSONArray)params.get("callFrames");
                    //TODO: workaround for mobile safari
                    if (frames == null) {
                        frames = (JSONArray) ((JSONObject) params.get("details")).get("callFrames");
                    }
                    notifyPaused(frames, (String)params.get("reason"), (JSONObject)params.get("data"));
                }
            } else if (RESPONSE_GLOB_OBJECT_CLEARED.equals(method)) {
                notifyReset();
            } else if (RESPONSE_SCRIPT_PARSED.equals(method)) {
                addScript(response.getParams());
            } else if (RESPONSE_BRKP_RESOLVED.equals(method)) {
                JSONObject params = response.getParams();
                String id = (String)params.get("breakpointId");
                Breakpoint bp = breakpointsById.get(id);
                if (bp != null) {
                    APIFactory.breakpointResolved(bp, (JSONObject) params.get("location"));
                }
            }
        }
        
    }

    /**
     * Debugger state listener.
     */
    public interface Listener extends EventListener {
        
        /**
         * Execution was suspended.
         * @param callStack current call stack
         * @param reason what triggered this suspense
         */
        void paused(List<CallFrame> callStack, String reason);
        
        /**
         * Execution was resumed.
         */
        void resumed();
        
        /**
         * Object state was reset due to page reload.
         */
        void reset();
        
        /**
         * Debugger was enabled or disabled.
         * @param enabled <code>true</code> when the debugger was enabled,
         *                <code>false</code> when disabled.
         */
        void enabled(boolean enabled);
    }
    
    /**
     * Notifies when a new script is parsed.
     */
    public interface ScriptsListener extends EventListener {
        
        void scriptParsed(Script script);
        
    }
    
}
