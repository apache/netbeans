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

/**
 * Fired when breakpoint is resolved to an actual script and location.
 */
public final class BreakpointResolved {
    private String breakpointId;
    private Location location;

    public BreakpointResolved() {
    }

    /**
     * Breakpoint unique identifier.
     */
    public String getBreakpointId() {
        return breakpointId;
    }

    /**
     * Breakpoint unique identifier.
     */
    public void setBreakpointId(String breakpointId) {
        this.breakpointId = breakpointId;
    }

    /**
     * Actual breakpoint location.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Actual breakpoint location.
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "BreakpointResolved{" + "breakpointId=" + breakpointId + ", location=" + location + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.breakpointId);
        hash = 79 * hash + Objects.hashCode(this.location);
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
        final BreakpointResolved other = (BreakpointResolved) obj;
        if (!Objects.equals(this.breakpointId, other.breakpointId)) {
            return false;
        }
        return Objects.equals(this.location, other.location);
    }


}
