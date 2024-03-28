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

public final class SetBreakpointByUrlResponse {
    private String breakpointId;
    private List<Location> locations;

    public SetBreakpointByUrlResponse() {
    }

    /**
     * Id of the created breakpoint for further reference.
     */
    public String getBreakpointId() {
        return breakpointId;
    }

    /**
     * Id of the created breakpoint for further reference.
     */
    public void setBreakpointId(String breakpointId) {
        this.breakpointId = breakpointId;
    }

    /**
     * List of the locations this breakpoint resolved into upon addition.
     */
    public List<Location> getLocations() {
        return locations;
    }

    /**
     * List of the locations this breakpoint resolved into upon addition.
     */
    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    @Override
    public String toString() {
        return "BreakpointByUrlResponse{" + "breakpointId=" + breakpointId + ", locations=" + locations + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.breakpointId);
        hash = 59 * hash + Objects.hashCode(this.locations);
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
        final SetBreakpointByUrlResponse other = (SetBreakpointByUrlResponse) obj;
        if (!Objects.equals(this.breakpointId, other.breakpointId)) {
            return false;
        }
        return Objects.equals(this.locations, other.locations);
    }

}
