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

public final class SetBreakpointRequest {
    private Location location;
    private String condition;

    public SetBreakpointRequest() {
    }

    /**
     * Location to set breakpoint in.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Location to set breakpoint in.
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Expression to use as a breakpoint condition. When specified, debugger
     * will only stop on the breakpoint if this expression evaluates to
     * {@code true}.
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Expression to use as a breakpoint condition. When specified, debugger
     * will only stop on the breakpoint if this expression evaluates to
     * {@code true}.
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "SetBreakpointRequest{" + "location=" + location + ", condition=" + condition + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.location);
        hash = 97 * hash + Objects.hashCode(this.condition);
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
        final SetBreakpointRequest other = (SetBreakpointRequest) obj;
        if (!Objects.equals(this.condition, other.condition)) {
            return false;
        }
        return Objects.equals(this.location, other.location);
    }

    
}
