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
import org.netbeans.lib.chrome_devtools_protocol.runtime.ExceptionDetails;
import org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace;
import org.netbeans.lib.chrome_devtools_protocol.runtime.StackTraceId;

public final class SetScriptSourceResponse {
    private List<CallFrame> callFrames;
    private Boolean stackChanged;
    private StackTrace asyncStackTrace;
    private StackTraceId asyncStackTraceId;
    private ExceptionDetails exceptionDetails;

    public SetScriptSourceResponse() {
    }

    /**
     * New stack trace in case editing has happened while VM was stopped.
     */
    public List<CallFrame> getCallFrames() {
        return callFrames;
    }

    /**
     * New stack trace in case editing has happened while VM was stopped.
     */
    public void setCallFrames(List<CallFrame> callFrames) {
        this.callFrames = callFrames;
    }

    /**
     * Whether current call stack was modified after applying the changes.
     */
    public Boolean getStackChanged() {
        return stackChanged;
    }

    /**
     * Whether current call stack was modified after applying the changes.
     */
    public void setStackChanged(Boolean stackChanged) {
        this.stackChanged = stackChanged;
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
     * <p><strong>Experimental</strong></p>
     */
    public StackTraceId getAsyncStackTraceId() {
        return asyncStackTraceId;
    }

    /**
     * Async stack trace, if any.
     * <p><strong>Experimental</strong></p>
     */
    public void setAsyncStackTraceId(StackTraceId asyncStackTraceId) {
        this.asyncStackTraceId = asyncStackTraceId;
    }

    /**
     * Exception details if any.
     */
    public ExceptionDetails getExceptionDetails() {
        return exceptionDetails;
    }

    /**
     * Exception details if any.
     */
    public void setExceptionDetails(ExceptionDetails exceptionDetails) {
        this.exceptionDetails = exceptionDetails;
    }

    @Override
    public String toString() {
        return "SetScriptSourceResponse{" + "callFrames=" + callFrames + ", stackChanged=" + stackChanged + ", asyncStackTrace=" + asyncStackTrace + ", asyncStackTraceId=" + asyncStackTraceId + ", exceptionDetails=" + exceptionDetails + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.callFrames);
        hash = 53 * hash + Objects.hashCode(this.stackChanged);
        hash = 53 * hash + Objects.hashCode(this.asyncStackTrace);
        hash = 53 * hash + Objects.hashCode(this.asyncStackTraceId);
        hash = 53 * hash + Objects.hashCode(this.exceptionDetails);
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
        final SetScriptSourceResponse other = (SetScriptSourceResponse) obj;
        if (!Objects.equals(this.callFrames, other.callFrames)) {
            return false;
        }
        if (!Objects.equals(this.stackChanged, other.stackChanged)) {
            return false;
        }
        if (!Objects.equals(this.asyncStackTrace, other.asyncStackTrace)) {
            return false;
        }
        if (!Objects.equals(this.asyncStackTraceId, other.asyncStackTraceId)) {
            return false;
        }
        return Objects.equals(this.exceptionDetails, other.exceptionDetails);
    }

    
}
