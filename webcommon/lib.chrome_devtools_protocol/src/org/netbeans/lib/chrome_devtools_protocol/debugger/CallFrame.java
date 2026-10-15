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

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;
import org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject;

/**
 * JavaScript call frame. Array of call frames form the call stack.
 */
public final class CallFrame {
    private String callFrameId;
    private String functionName;
    private Location functionLocation;
    private Location location;
    private String url;
    private List<Scope> scopeChain;
    @SerializedName("this")
    private RemoteObject thisObject;
    private RemoteObject returnValue;
    private Boolean canBeRestarted;

    public CallFrame() {
    }

    /**
     * Call frame identifier. This identifier is only valid while the virtual
     * machine is paused.
     */
    public String getCallFrameId() {
        return callFrameId;
    }

    /**
     * Call frame identifier. This identifier is only valid while the virtual
     * machine is paused.
     */
    public void setCallFrameId(String callFrameId) {
        this.callFrameId = callFrameId;
    }

    /**
     * Name of the JavaScript function called on this call frame.
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Name of the JavaScript function called on this call frame.
     */
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * Location in the source code.
     */
    public Location getFunctionLocation() {
        return functionLocation;
    }

    /**
     * Location in the source code.
     */
    public void setFunctionLocation(Location functionLocation) {
        this.functionLocation = functionLocation;
    }

    /**
     * Location in the source code.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Location in the source code.
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * JavaScript script name or url. Deprecated in favor of using the
     * location.scriptId to resolve the URL via a previously sent
     * Debugger.scriptParsed event.
     */
    @Deprecated
    public String getUrl() {
        return url;
    }

    /**
     * JavaScript script name or url. Deprecated in favor of using the
     * location.scriptId to resolve the URL via a previously sent
     * Debugger.scriptParsed event.
     */
    @Deprecated
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Scope chain for this call frame.
     */
    public List<Scope> getScopeChain() {
        return scopeChain;
    }

    /**
     * Scope chain for this call frame.
     */
    public void setScopeChain(List<Scope> scopeChain) {
        this.scopeChain = scopeChain;
    }

    /**
     * {@code this} object for this call frame.
     */
    public RemoteObject getThisObject() {
        return thisObject;
    }

    /**
     * {@code this} object for this call frame.
     */
    public void setThisObject(RemoteObject thisObject) {
        this.thisObject = thisObject;
    }

    /**
     * The value being returned, if the function is at return point.
     */
    public RemoteObject getReturnValue() {
        return returnValue;
    }

    /**
     * The value being returned, if the function is at return point.
     */
    public void setReturnValue(RemoteObject returnValue) {
        this.returnValue = returnValue;
    }

    /**
     * Valid only while the VM is paused and indicates whether this frame can be
     * restarted or not. Note that a true value here does not guarantee that
     * Debugger#restartFrame with this CallFrameId will be successful, but it is
     * very likely.
     *
     * <p><strong>Experimental</strong></p>
     */
    public Boolean getCanBeRestarted() {
        return canBeRestarted;
    }

    /**
     * Valid only while the VM is paused and indicates whether this frame can be
     * restarted or not. Note that a true value here does not guarantee that
     * Debugger#restartFrame with this CallFrameId will be successful, but it is
     * very likely.
     *
     * <p><strong>Experimental</strong></p>
     */
    public void setCanBeRestarted(Boolean canBeRestarted) {
        this.canBeRestarted = canBeRestarted;
    }

    @Override
    public String toString() {
        return "CallFrame{" + "callFrameId=" + callFrameId + ", functionName=" + functionName + ", functionLocation=" + functionLocation + ", location=" + location + ", url=" + url + ", scopeChain=" + scopeChain + ", thisObject=" + thisObject + ", returnValue=" + returnValue + ", canBeRestarted=" + canBeRestarted + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.callFrameId);
        hash = 53 * hash + Objects.hashCode(this.functionName);
        hash = 53 * hash + Objects.hashCode(this.functionLocation);
        hash = 53 * hash + Objects.hashCode(this.location);
        hash = 53 * hash + Objects.hashCode(this.url);
        hash = 53 * hash + Objects.hashCode(this.scopeChain);
        hash = 53 * hash + Objects.hashCode(this.thisObject);
        hash = 53 * hash + Objects.hashCode(this.returnValue);
        hash = 53 * hash + Objects.hashCode(this.canBeRestarted);
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
        final CallFrame other = (CallFrame) obj;
        if (!Objects.equals(this.callFrameId, other.callFrameId)) {
            return false;
        }
        if (!Objects.equals(this.functionName, other.functionName)) {
            return false;
        }
        if (!Objects.equals(this.functionLocation, other.functionLocation)) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        if (!Objects.equals(this.scopeChain, other.scopeChain)) {
            return false;
        }
        if (!Objects.equals(this.thisObject, other.thisObject)) {
            return false;
        }
        if (!Objects.equals(this.returnValue, other.returnValue)) {
            return false;
        }
        return Objects.equals(this.canBeRestarted, other.canBeRestarted);
    }

    
}
