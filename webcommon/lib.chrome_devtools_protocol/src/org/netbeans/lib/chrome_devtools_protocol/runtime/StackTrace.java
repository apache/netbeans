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
package org.netbeans.lib.chrome_devtools_protocol.runtime;

import java.util.List;
import java.util.Objects;

/**
 * Call frames for assertions or error messages.
 */
public final class StackTrace {
    private String description;
    private List<CallFrame> callFrames;
    private StackTrace parent;
    private StackTraceId parentId;

    public StackTrace() {
    }

    /**
     * String label of this stack trace. For async traces this may be a name of
     * the function that initiated the async call.
     */
    public String getDescription() {
        return description;
    }

    /**
     * String label of this stack trace. For async traces this may be a name of
     * the function that initiated the async call.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * JavaScript function name.
     */
    public List<CallFrame> getCallFrames() {
        return callFrames;
    }

    /**
     * JavaScript function name.
     */
    public void setCallFrames(List<CallFrame> callFrames) {
        this.callFrames = callFrames;
    }

    /**
     * Asynchronous JavaScript stack trace that preceded this stack, if available.
     */
    public StackTrace getParent() {
        return parent;
    }

    /**
     * Asynchronous JavaScript stack trace that preceded this stack, if available.
     */
    public void setParent(StackTrace parent) {
        this.parent = parent;
    }

    /**
     * Asynchronous JavaScript stack trace that preceded this stack, if available.
     * <p><strong>Experimental</strong></p>
     */
    public StackTraceId getParentId() {
        return parentId;
    }

    /**
     * Asynchronous JavaScript stack trace that preceded this stack, if available.
     * <p><strong>Experimental</strong></p>
     */
    public void setParentId(StackTraceId parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return "StackTrace{" + "description=" + description + ", callFrames=" + callFrames + ", parent=" + parent + ", parentId=" + parentId + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.description);
        hash = 67 * hash + Objects.hashCode(this.callFrames);
        hash = 67 * hash + Objects.hashCode(this.parent);
        hash = 67 * hash + Objects.hashCode(this.parentId);
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
        final StackTrace other = (StackTrace) obj;
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.callFrames, other.callFrames)) {
            return false;
        }
        if (!Objects.equals(this.parent, other.parent)) {
            return false;
        }
        return Objects.equals(this.parentId, other.parentId);
    }


}
