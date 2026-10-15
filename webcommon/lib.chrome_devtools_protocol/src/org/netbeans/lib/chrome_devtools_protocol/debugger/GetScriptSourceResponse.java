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

public final class GetScriptSourceResponse {
    private String scriptSource;
    private String bytecode;

    public GetScriptSourceResponse() {
    }

    /**
     * Script source (empty in case of Wasm bytecode).
     */
    public String getScriptSource() {
        return scriptSource;
    }

    /**
     * Script source (empty in case of Wasm bytecode).
     */
    public void setScriptSource(String scriptSource) {
        this.scriptSource = scriptSource;
    }

    /**
     * Wasm bytecode. (Encoded as a base64 string when passed over JSON)
     */
    public String getBytecode() {
        return bytecode;
    }

    /**
     * Wasm bytecode. (Encoded as a base64 string when passed over JSON)
     */
    public void setBytecode(String bytecode) {
        this.bytecode = bytecode;
    }

    @Override
    public String toString() {
        return "GetScriptSourceResponse{" + "scriptSource=" + scriptSource + ", bytecode=" + bytecode + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.scriptSource);
        hash = 83 * hash + Objects.hashCode(this.bytecode);
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
        final GetScriptSourceResponse other = (GetScriptSourceResponse) obj;
        if (!Objects.equals(this.scriptSource, other.scriptSource)) {
            return false;
        }
        return Objects.equals(this.bytecode, other.bytecode);
    }

}
