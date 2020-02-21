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

package org.netbeans.modules.cnd.debugger.common2.debugger.api;

/**
 * enum like ID for debugger engines
 */
public final class EngineType {

    private final boolean inheritProject;
    private final String debuggerID;
    private final String displayName;
    /*package*/ EngineType(boolean inheritProject, String debuggerID, String displayName) {
        this.inheritProject = inheritProject;
        this.debuggerID = debuggerID;
        this.displayName = displayName;
    }

    public boolean isInherited() {
        return inheritProject;
    }

    public String getDebuggerID() {
        return debuggerID;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return getDebuggerID();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EngineType other = (EngineType) obj;
        if (this.inheritProject != other.inheritProject) {
            return false;
        }
        if (!this.debuggerID.equals(other.debuggerID)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.inheritProject ? 1 : 0);
        hash = 37 * hash + this.debuggerID.hashCode();
        return hash;
    }

}
