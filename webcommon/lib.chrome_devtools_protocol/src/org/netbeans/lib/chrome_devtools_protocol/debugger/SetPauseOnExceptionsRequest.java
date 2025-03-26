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

public final class SetPauseOnExceptionsRequest {
    private String state;

    public SetPauseOnExceptionsRequest() {
    }

    /**
     * Pause on exceptions mode.<br />
     * Allowed Values: {@code none, uncaught, all}
     */
    public String getState() {
        return state;
    }

    /**
     * Pause on exceptions mode.<br />
     * Allowed Values: {@code none, uncaught, all}
     */
    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "SetPauseOnExceptionsRequest{" + "state=" + state + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.state);
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
        final SetPauseOnExceptionsRequest other = (SetPauseOnExceptionsRequest) obj;
        return Objects.equals(this.state, other.state);
    }
}
