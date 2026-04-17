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
package org.netbeans.lib.chrome_devtools_protocol.runtime;

import java.util.Objects;

/**
 * Stack entry for runtime errors and assertions.
 */
public final class CallFrame {
    private String functionName;
    private String scriptId;
    private String url;
    private int lineNumber;
    private int columnNumber;

    public CallFrame() {
    }

    /**
     * JavaScript function name.
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * JavaScript function name.
     */
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * JavaScript script id.
     */
    public String getScriptId() {
        return scriptId;
    }

    /**
     * JavaScript script id.
     */
    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    /**
     * JavaScript script name or url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * JavaScript script name or url.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * JavaScript script line number (0-based).
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * JavaScript script line number (0-based).
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * JavaScript script column number (0-based).
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * JavaScript script column number (0-based).
     */
    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    @Override
    public String toString() {
        return "CallFrame{" + "functionName=" + functionName + ", scriptId=" + scriptId + ", url=" + url + ", lineNumber=" + lineNumber + ", columnNumber=" + columnNumber + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.functionName);
        hash = 43 * hash + Objects.hashCode(this.scriptId);
        hash = 43 * hash + Objects.hashCode(this.url);
        hash = 43 * hash + this.lineNumber;
        hash = 43 * hash + this.columnNumber;
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
        final CallFrame other = (CallFrame) obj;
        if (this.lineNumber != other.lineNumber) {
            return false;
        }
        if (this.columnNumber != other.columnNumber) {
            return false;
        }
        if (!Objects.equals(this.functionName, other.functionName)) {
            return false;
        }
        if (!Objects.equals(this.scriptId, other.scriptId)) {
            return false;
        }
        return Objects.equals(this.url, other.url);
    }


}
