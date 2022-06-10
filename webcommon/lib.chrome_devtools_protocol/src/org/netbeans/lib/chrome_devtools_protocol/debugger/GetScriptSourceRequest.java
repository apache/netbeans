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

public final class GetScriptSourceRequest {
    private String scriptId;

    public GetScriptSourceRequest() {
    }

    /**
     * Id of the script to get source for.
     */
    public String getScriptId() {
        return scriptId;
    }

    /**
     * Id of the script to get source for.
     */
    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    @Override
    public String toString() {
        return "GetScriptSourceRequest{" + "scriptId=" + scriptId + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.scriptId);
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
        final GetScriptSourceRequest other = (GetScriptSourceRequest) obj;
        return Objects.equals(this.scriptId, other.scriptId);
    }

}
