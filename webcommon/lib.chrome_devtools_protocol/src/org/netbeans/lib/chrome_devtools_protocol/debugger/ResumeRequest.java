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

public final class ResumeRequest {
    private Boolean terminateOnResume;

    public ResumeRequest() {
    }

    /**
     * Set to true to terminate execution upon resuming execution. In contrast
     * to Runtime.terminateExecution, this will allows to execute further
     * JavaScript (i.e. via evaluation) until execution of the paused code is
     * actually resumed, at which point termination is triggered. If execution
     * is currently not paused, this parameter has no effect.
     */
    public Boolean getTerminateOnResume() {
        return terminateOnResume;
    }

    /**
     * Set to true to terminate execution upon resuming execution. In contrast
     * to Runtime.terminateExecution, this will allows to execute further
     * JavaScript (i.e. via evaluation) until execution of the paused code is
     * actually resumed, at which point termination is triggered. If execution
     * is currently not paused, this parameter has no effect.
     */
    public void setTerminateOnResume(Boolean terminateOnResume) {
        this.terminateOnResume = terminateOnResume;
    }

    @Override
    public String toString() {
        return "ResumeRequest{" + "terminateOnResume=" + terminateOnResume + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.terminateOnResume);
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
        final ResumeRequest other = (ResumeRequest) obj;
        return Objects.equals(this.terminateOnResume, other.terminateOnResume);
    }

    
}
