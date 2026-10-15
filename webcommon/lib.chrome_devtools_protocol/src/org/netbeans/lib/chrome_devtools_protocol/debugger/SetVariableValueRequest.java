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

import java.util.Objects;
import org.netbeans.lib.chrome_devtools_protocol.runtime.CallArgument;

/**
 * Changes value of variable in a callframe. Object-based scopes are not
 * supported and must be mutated manually.
 */
public final class SetVariableValueRequest {
    private int scopeNumber;
    private String variableName;
    private CallArgument newValue;
    private String callFrameId;

    public SetVariableValueRequest() {
    }

    /**
     * 0-based number of scope as was listed in scope chain. Only 'local',
     * 'closure' and 'catch' scope types are allowed. Other scopes could be
     * manipulated manually.
     */
    public int getScopeNumber() {
        return scopeNumber;
    }

    /**
     * @see #getScopeNumber()
     */
    public void setScopeNumber(int scopeNumber) {
        this.scopeNumber = scopeNumber;
    }

    /**
     * Variable name.
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * @see #getVariableName()
     */
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    /**
     * New variable value.
     */
    public CallArgument getNewValue() {
        return newValue;
    }

    /**
     * @see #getNewValue()
     */
    public void setNewValue(CallArgument newValue) {
        this.newValue = newValue;
    }

    /**
     * Id of callframe that holds variable.
     */
    public String getCallFrameId() {
        return callFrameId;
    }

    /**
     * @see #getCallFrameId()
     */
    public void setCallFrameId(String callFrameId) {
        this.callFrameId = callFrameId;
    }

    @Override
    public String toString() {
        return "SetVariableValueRequest{" + "scopeNumber=" + scopeNumber + ", variableName=" + variableName + ", newValue=" + newValue + ", callFrameId=" + callFrameId + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + this.scopeNumber;
        hash = 43 * hash + Objects.hashCode(this.variableName);
        hash = 43 * hash + Objects.hashCode(this.newValue);
        hash = 43 * hash + Objects.hashCode(this.callFrameId);
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
        final SetVariableValueRequest other = (SetVariableValueRequest) obj;
        if (this.scopeNumber != other.scopeNumber) {
            return false;
        }
        if (!Objects.equals(this.variableName, other.variableName)) {
            return false;
        }
        if (!Objects.equals(this.callFrameId, other.callFrameId)) {
            return false;
        }
        return Objects.equals(this.newValue, other.newValue);
    }


}
