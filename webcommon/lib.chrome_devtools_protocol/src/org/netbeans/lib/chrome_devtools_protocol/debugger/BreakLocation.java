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

public final class BreakLocation {

    private String scriptId;
    private int lineNumber;
    private int columnNumber;
    private String type;

    /**
     * Script identifier as reported in the Debugger.scriptParsed.
     */
    public String getScriptId() {
        return scriptId;
    }

    /**
     * Script identifier as reported in the Debugger.scriptParsed.
     */
    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    /**
     * Line number in the script (0-based).
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Line number in the script (0-based).
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Column number in the script (0-based).
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * Column number in the script (0-based).
     */
    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    /**
     * Allowed Values: {@code debuggerStatement}, {@code call}, {@code return}
     */
    public String getType() {
        return type;
    }

    /**
     * Allowed Values: {@code debuggerStatement}, {@code call}, {@code return}
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "BreakLocation{" + "scriptId=" + scriptId + ", lineNumber=" + lineNumber + ", columnNumber=" + columnNumber + ", type=" + type + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.scriptId);
        hash = 23 * hash + this.lineNumber;
        hash = 23 * hash + this.columnNumber;
        hash = 23 * hash + Objects.hashCode(this.type);
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
        final BreakLocation other = (BreakLocation) obj;
        if (this.lineNumber != other.lineNumber) {
            return false;
        }
        if (this.columnNumber != other.columnNumber) {
            return false;
        }
        if (!Objects.equals(this.scriptId, other.scriptId)) {
            return false;
        }
        return Objects.equals(this.type, other.type);
    }

    
}
