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

/**
 * Makes page not interrupt on any pauses (breakpoint, exception, dom exception
 * etc).
 */
public final class SetSkipAllPausesRequest {
    private boolean skip;

    public SetSkipAllPausesRequest() {
    }

    /**
     * New value for skip pauses state.
     */
    public boolean isSkip() {
        return skip;
    }

    /**
     * New value for skip pauses state.
     */
    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    @Override
    public String toString() {
        return "SetSkipAllPauses{" + "skip=" + skip + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (this.skip ? 1 : 0);
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
        final SetSkipAllPausesRequest other = (SetSkipAllPausesRequest) obj;
        return this.skip == other.skip;
    }

    
}
