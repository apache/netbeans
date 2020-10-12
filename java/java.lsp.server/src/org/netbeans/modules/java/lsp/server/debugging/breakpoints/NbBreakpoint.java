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
package org.netbeans.modules.java.lsp.server.debugging.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.truffle.breakpoints.TruffleLineBreakpoint;

/**
 *
 * @author martin
 */
public final class NbBreakpoint {

    private static final Logger LOGGER = Logger.getLogger(NbBreakpoint.class.getName());

    private final String sourceURL;
    private final int line;
    private int hitCount;
    private String condition;
    private String logMessage;
    private final Map<Object, Object> properties = new HashMap<>();
    private Breakpoint breakpoint; // Either JPDA's LineBreakpoint, or TruffleLineBreakpoint

    public NbBreakpoint(String sourceURL, int line, int hitCount, String condition, String logMessage) {
        this.sourceURL = sourceURL;
        this.line = line;
        this.hitCount = hitCount;
        this.condition = condition;
        this.logMessage = logMessage;
    }

    public Breakpoint getNBBreakpoint() {
        return breakpoint;
    }

    /**
     * Interpreted as a source URL.
     */
    public String className() {
        return sourceURL;
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
            }
        }
    }

    public CompletableFuture<NbBreakpoint> install() {
        Breakpoint breakpoint;
        if (sourceURL.toLowerCase().endsWith(".java")) {
            LineBreakpoint b = LineBreakpoint.create(sourceURL, line);
            if (condition != null && !condition.isEmpty()) {
                b.setCondition(condition);
            }
            if (logMessage != null && !logMessage.isEmpty()) {
                b.setPrintText(logMessage);
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
            breakpoint = b;
        }
        if (hitCount > 0) {
            breakpoint.setHitCountFilter(hitCount, Breakpoint.HIT_COUNT_FILTERING_STYLE.GREATER);
        }
        breakpoint.addPropertyChangeListener(Breakpoint.PROP_VALIDITY, evt -> {
            updateValid(breakpoint);
        });
        updateValid(breakpoint);
        DebuggerManager d = DebuggerManager.getDebuggerManager();
        d.addBreakpoint(breakpoint);
        this.breakpoint = breakpoint;
        return CompletableFuture.completedFuture(this);
    }

    private void updateValid(Breakpoint breakpoint) {
        if (breakpoint.getValidity() == Breakpoint.VALIDITY.VALID) {
            putProperty("verified", true);
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
                // no print text
            }
        }
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
