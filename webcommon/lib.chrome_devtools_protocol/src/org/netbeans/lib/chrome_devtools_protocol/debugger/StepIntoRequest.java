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

/**
 * Steps into the function call.
 */
public final class StepIntoRequest {
    private Boolean breakOnAsyncCall;
    private List<LocationRange> skipList;

    public StepIntoRequest() {
    }

    /**
     * Debugger will pause on the execution of the first async task which was
     * scheduled before next pause.
     * <p><strong>Experimental</strong></p>
     */
    public Boolean getBreakOnAsyncCall() {
        return breakOnAsyncCall;
    }

    /**
     * @see #getBreakOnAsyncCall()
     */
    public void setBreakOnAsyncCall(Boolean breakOnAsyncCall) {
        this.breakOnAsyncCall = breakOnAsyncCall;
    }

    /**
     * The skipList specifies location ranges that should be skipped on step
     * into.
     * <p><strong>Experimental</strong></p>
     */
    public List<LocationRange> getSkipList() {
        return skipList;
    }

    /**
     * @see #getSkipList()
     */
    public void setSkipList(List<LocationRange> skipList) {
        this.skipList = skipList;
    }

    @Override
    public String toString() {
        return "StepIntoRequest{" + "breakOnAsyncCall=" + breakOnAsyncCall + ", skipList=" + skipList + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.breakOnAsyncCall);
        hash = 17 * hash + Objects.hashCode(this.skipList);
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
        final StepIntoRequest other = (StepIntoRequest) obj;
        if (!Objects.equals(this.breakOnAsyncCall, other.breakOnAsyncCall)) {
            return false;
        }
        return Objects.equals(this.skipList, other.skipList);
    }


}
