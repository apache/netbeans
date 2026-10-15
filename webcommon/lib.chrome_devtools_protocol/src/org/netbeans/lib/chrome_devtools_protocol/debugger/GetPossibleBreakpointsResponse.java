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

public final class GetPossibleBreakpointsResponse {
    private List<BreakLocation> locations;

    public GetPossibleBreakpointsResponse() {
    }

    /**
     * List of the possible breakpoint locations.
     */
    public List<BreakLocation> getLocations() {
        return locations;
    }

    /**
     * List of the possible breakpoint locations.
     */
    public void setLocations(List<BreakLocation> locations) {
        this.locations = locations;
    }

    @Override
    public String toString() {
        return "GetPossibleBreakpointsResponse{" + "locations=" + locations + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.locations);
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
        final GetPossibleBreakpointsResponse other = (GetPossibleBreakpointsResponse) obj;
        return Objects.equals(this.locations, other.locations);
    }

}
