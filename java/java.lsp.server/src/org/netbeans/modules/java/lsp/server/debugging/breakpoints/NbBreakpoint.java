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
package org.netbeans.modules.java.lsp.server.debugging.breakpoints;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.debug.BreakpointEventArguments;
import org.eclipse.lsp4j.debug.Source;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.truffle.breakpoints.TruffleLineBreakpoint;
import org.netbeans.modules.java.lsp.server.debugging.DebugAdapterContext;

/**
 *
 * @author martin
 */
public final class NbBreakpoint {

    private static final Logger LOGGER = Logger.getLogger(NbBreakpoint.class.getName());

    private final DebugAdapterContext context;
    private final Source source;
    private final String sourceURL;
    private int line;
    private int hitCount;
    private String condition;
    private String logMessage;
    private final Map<Object, Object> properties = new HashMap<>();
    private Breakpoint breakpoint; // Either JPDA's LineBreakpoint, or TruffleLineBreakpoint

    public NbBreakpoint(Source source, String sourceURL, int line, int hitCount, String condition, String logMessage, DebugAdapterContext context) {
        this.source = source;
        Integer ref = source.getSourceReference();
        if (ref != null && ref != 0) {
            URI uri = context.getSourceUri(ref);
            if (uri != null) {
                try {
                    sourceURL = uri.toURL().toString();
                } catch (MalformedURLException ex) {}
            }
        }
        this.sourceURL = sourceURL;
        this.line = line;
        this.hitCount = hitCount;
        this.condition = condition;
        this.logMessage = logMessage;
        this.context = context;
    }

    public Breakpoint getNBBreakpoint() {
        return breakpoint;
    }

    public int getLineNumber() {
        return line;
    }

    public int getHitCount() {
        return hitCount;
    }

    public void setHitCount(int hitCount) {
        if (this.hitCount != hitCount) {
            this.hitCount = hitCount;
            if (hitCount > 0) {
                breakpoint.setHitCountFilter(hitCount, Breakpoint.HIT_COUNT_FILTERING_STYLE.GREATER);
            } else {
                breakpoint.setHitCountFilter(0, null);
            }
        }
    }

    public CompletableFuture<NbBreakpoint> install() {
        Breakpoint breakpoint;
        String sourceURLLower = sourceURL.toLowerCase();
        boolean isJava = sourceURLLower.endsWith(".java");      // NOI18N
        boolean isGroovy = sourceURLLower.endsWith(".groovy");  // NOI18N
        if (isJava || isGroovy) {
            LineBreakpoint b = LineBreakpoint.create(sourceURL, line);
            if (condition != null && !condition.isEmpty()) {
                b.setCondition(condition);
            }
            if (logMessage != null && !logMessage.isEmpty()) {
                String message = lsp2NBLogMessage(logMessage);
                b.setPrintText(message);
                b.setSuspend(JPDABreakpoint.SUSPEND_NONE);
            }
            breakpoint = b;
        } else {
            URL url;
            try {
                url = new URL(sourceURL);
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.CONFIG, "source URL = "+sourceURL, ex);
                return null;
            }
            TruffleLineBreakpoint b = new TruffleLineBreakpoint(url, line);
            if (condition != null && !condition.isEmpty()) {
                b.setCondition(condition);
            }
            if (logMessage != null && !logMessage.isEmpty()) {
                String message = lsp2NBLogMessage(logMessage);
                b.setPrintText(message);
                b.setSuspend(false);
            }
            breakpoint = b;
        }
        if (hitCount > 0) {
            breakpoint.setHitCountFilter(hitCount, Breakpoint.HIT_COUNT_FILTERING_STYLE.GREATER);
        }
        breakpoint.addPropertyChangeListener(Breakpoint.PROP_VALIDITY, evt -> {
            updateValid(breakpoint, true);
        });
        DebuggerManager d = DebuggerManager.getDebuggerManager();
        d.addBreakpoint(breakpoint);
        updateValid(breakpoint, false);
        this.breakpoint = breakpoint;
        return CompletableFuture.completedFuture(this);
    }

    private static final String lsp2NBLogMessage(String message) {
        return message.replaceAll("\\{([^\\}]+)\\}", "{=$1}");      // NOI18N
    }

    private void updateValid(Breakpoint breakpoint, boolean sendNotify) {
        String message = breakpoint.getValidityMessage();
        boolean verified = breakpoint.getValidity() == Breakpoint.VALIDITY.VALID;
        putProperty("message", message);
        putProperty("verified", verified);
        if (verified) {
            // Update the line number
            if (breakpoint instanceof LineBreakpoint) {
                this.line = ((LineBreakpoint) breakpoint).getLineNumber();
            } else {
                this.line = ((TruffleLineBreakpoint) breakpoint).getLineNumber();
            }
        }
        if (sendNotify) {
            BreakpointEventArguments bea = new BreakpointEventArguments();
            bea.setBreakpoint(convertDebuggerBreakpointToClient());
            bea.setReason("changed");
            context.getClient().breakpoint(bea);
        }
    }

    public void putProperty(Object key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(Object key) {
        return properties.get(key);
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        if (!Objects.equals(this.condition, condition)) {
            this.condition = condition;
            if (breakpoint instanceof LineBreakpoint) {
                ((LineBreakpoint) breakpoint).setCondition(condition);
            } else {
                ((TruffleLineBreakpoint) breakpoint).setCondition(condition);
            }
        }
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        if (!Objects.equals(this.logMessage, logMessage)) {
            this.logMessage = logMessage;
            if (breakpoint instanceof LineBreakpoint) {
                ((LineBreakpoint) breakpoint).setPrintText(logMessage);
            } else {
                ((TruffleLineBreakpoint) breakpoint).setPrintText(logMessage);
            }
        }
    }

    org.eclipse.lsp4j.debug.Breakpoint convertDebuggerBreakpointToClient() {
        int id = (int) getProperty("id");
        boolean verified = getProperty("verified") != null && (boolean) getProperty("verified");
        int lineNumber = context.getClientLine(getLineNumber());
        org.eclipse.lsp4j.debug.Breakpoint bp = new org.eclipse.lsp4j.debug.Breakpoint();
        bp.setId(id);
        bp.setVerified(verified);
        bp.setLine(lineNumber);
        bp.setSource(source);
        String message = (String) getProperty("message");
        if (message != null) {
            bp.setMessage(message);
        }
        return bp;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.sourceURL);
        hash = 59 * hash + this.line;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NbBreakpoint other = (NbBreakpoint) obj;
        if (this.line != other.line) {
            return false;
        }
        if (!Objects.equals(this.sourceURL, other.sourceURL)) {
            return false;
        }
        return true;
    }

    public void close() throws Exception {
        if (breakpoint != null) {
            DebuggerManager d = DebuggerManager.getDebuggerManager();
            d.removeBreakpoint(breakpoint);
        }
    }
    
}
