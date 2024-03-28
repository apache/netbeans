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
package org.netbeans.lib.chrome_devtools_protocol.debugger;

import java.util.List;
import java.util.Objects;
import org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace;
import org.netbeans.lib.chrome_devtools_protocol.runtime.StackTraceId;

/**
 * Fired when the virtual machine stopped on breakpoint or exception or any
 * other stop criteria.
 */
public final class Paused {
    private List<CallFrame> callFrames;
    private String reason;
    private Object data;
    private List<String> hitBreakpoints;
    private StackTrace asyncStackTrace;
    private StackTraceId asyncStackTraceId;

    public Paused() {
    }

    /**
     * Call stack the virtual machine stopped on.
     */
    public List<CallFrame> getCallFrames() {
        return callFrames;
    }

    /**
     * Call stack the virtual machine stopped on.
     */
    public void setCallFrames(List<CallFrame> callFrames) {
        this.callFrames = callFrames;
    }

    /**
     * Pause reason.<br />
     * Allowed Values: {code ambiguous, assert, CSPViolation, debugCommand, DOM,
     * EventListener, exception, instrumentation, OOM, other, promiseRejection,
     * XHR
     */
    public String getReason() {
        return reason;
    }

    /**
     * Pause reason.<br />
     * Allowed Values: {code ambiguous, assert, CSPViolation, debugCommand, DOM,
     * EventListener, exception, instrumentation, OOM, other, promiseRejection,
     * XHR
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Object containing break-specific auxiliary properties.
     */
    public Object getData() {
        return data;
    }

    /**
     * Object containing break-specific auxiliary properties.
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * Hit breakpoints IDs
     */
    public List<String> getHitBreakpoints() {
        return hitBreakpoints;
    }

    /**
     * Hit breakpoints IDs
     */
    public void setHitBreakpoints(List<String> hitBreakpoints) {
        this.hitBreakpoints = hitBreakpoints;
    }

    /**
     * Async stack trace, if any.
     */
    public StackTrace getAsyncStackTrace() {
        return asyncStackTrace;
    }

    /**
     * Async stack trace, if any.
     */
    public void setAsyncStackTrace(StackTrace asyncStackTrace) {
        this.asyncStackTrace = asyncStackTrace;
    }

    /**
     * Async stack trace, if any.
     */
    public StackTraceId getAsyncStackTraceId() {
        return asyncStackTraceId;
    }

    /**
     * Async stack trace, if any.
     */
    public void setAsyncStackTraceId(StackTraceId asyncStackTraceId) {
        this.asyncStackTraceId = asyncStackTraceId;
    }

    @Override
    public String toString() {
        return "Paused{" + "callFrames=" + callFrames + ", reason=" + reason + ", data=" + data + ", hitBreakpoints=" + hitBreakpoints + ", asyncStackTrace=" + asyncStackTrace + ", asyncStackTraceId=" + asyncStackTraceId + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.callFrames);
        hash = 17 * hash + Objects.hashCode(this.reason);
        hash = 17 * hash + Objects.hashCode(this.data);
        hash = 17 * hash + Objects.hashCode(this.hitBreakpoints);
        hash = 17 * hash + Objects.hashCode(this.asyncStackTrace);
        hash = 17 * hash + Objects.hashCode(this.asyncStackTraceId);
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
        final Paused other = (Paused) obj;
        if (!Objects.equals(this.reason, other.reason)) {
            return false;
        }
        if (!Objects.equals(this.callFrames, other.callFrames)) {
            return false;
        }
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        if (!Objects.equals(this.hitBreakpoints, other.hitBreakpoints)) {
            return false;
        }
        if (!Objects.equals(this.asyncStackTrace, other.asyncStackTrace)) {
            return false;
        }
        return Objects.equals(this.asyncStackTraceId, other.asyncStackTraceId);
    }

}
