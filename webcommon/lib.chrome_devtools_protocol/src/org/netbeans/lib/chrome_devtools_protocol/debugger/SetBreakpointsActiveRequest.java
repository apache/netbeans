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

public final class SetBreakpointsActiveRequest {
    private boolean active;

    public SetBreakpointsActiveRequest() {
    }

    /**
     * New value for breakpoints active state.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * New value for breakpoints active state.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "SetBreakpointsActiveRequest{" + "active=" + active + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.active ? 1 : 0);
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
        final SetBreakpointsActiveRequest other = (SetBreakpointsActiveRequest) obj;
        return this.active == other.active;
    }

}
