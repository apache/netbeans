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

public final class GetPossibleBreakpointsRequest {
    private Location start;
    private Location end;
    private Boolean restrictToFunction;

    public GetPossibleBreakpointsRequest() {
    }

    /**
     * Start of range to search possible breakpoint locations in.
     */
    public Location getStart() {
        return start;
    }

    /**
     * Start of range to search possible breakpoint locations in.
     */
    public void setStart(Location start) {
        this.start = start;
    }

    /**
     * End of range to search possible breakpoint locations in (excluding). When
     * not specified, end of scripts is used as end of range.
     */
    public Location getEnd() {
        return end;
    }

    /**
     * End of range to search possible breakpoint locations in (excluding). When
     * not specified, end of scripts is used as end of range.
     */
    public void setEnd(Location end) {
        this.end = end;
    }

    /**
     * Only consider locations which are in the same (non-nested) function as
     * start.
     */
    public Boolean getRestrictToFunction() {
        return restrictToFunction;
    }

    /**
     * Only consider locations which are in the same (non-nested) function as
     * start.
     */
    public void setRestrictToFunction(Boolean restrictToFunction) {
        this.restrictToFunction = restrictToFunction;
    }

    @Override
    public String toString() {
        return "GetPossibleBreakpointsRequest{" + "start=" + start + ", end=" + end + ", restrictToFunction=" + restrictToFunction + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.start);
        hash = 67 * hash + Objects.hashCode(this.end);
        hash = 67 * hash + Objects.hashCode(this.restrictToFunction);
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
        final GetPossibleBreakpointsRequest other = (GetPossibleBreakpointsRequest) obj;
        if (!Objects.equals(this.start, other.start)) {
            return false;
        }
        if (!Objects.equals(this.end, other.end)) {
            return false;
        }
        return Objects.equals(this.restrictToFunction, other.restrictToFunction);
    }

    
}
