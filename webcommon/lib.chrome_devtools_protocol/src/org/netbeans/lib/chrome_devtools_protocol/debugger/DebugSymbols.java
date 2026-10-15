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

import java.net.URI;
import java.util.Objects;

/**
 * Debug symbols available for a wasm script.
 */
public final class DebugSymbols {
    private String type;
    private URI externalURL;

    public DebugSymbols() {
    }

    /**
     * Type of the debug symbols.<br />
     * Allowed Values: {@code None, SourceMap, EmbeddedDWARF, ExternalDWARF}
     */
    public String getType() {
        return type;
    }

    /**
     * Type of the debug symbols.<br />
     * Allowed Values: {@code None, SourceMap, EmbeddedDWARF, ExternalDWARF}
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * URL of the external symbol source.
     */
    public URI getExternalURL() {
        return externalURL;
    }

    /**
     * URL of the external symbol source.
     */
    public void setExternalURL(URI externalURL) {
        this.externalURL = externalURL;
    }

    @Override
    public String toString() {
        return "DebugSymbols{" + "type=" + type + ", externalURL=" + externalURL + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.type);
        hash = 83 * hash + Objects.hashCode(this.externalURL);
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
        final DebugSymbols other = (DebugSymbols) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return Objects.equals(this.externalURL, other.externalURL);
    }

}
