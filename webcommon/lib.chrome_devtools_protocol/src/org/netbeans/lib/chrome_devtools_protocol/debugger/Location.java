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

/**
 * Location in the source code.
 */
public final class Location {
    private String scriptId;
    private int lineNumber;
    private Integer columnNumber;

    public Location() {
    }

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
    public Integer getColumnNumber() {
        return columnNumber;
    }

    /**
     * Column number in the script (0-based).
     */
    public void setColumnNumber(Integer columnNumber) {
        this.columnNumber = columnNumber;
    }

    @Override
    public String toString() {
        return "Location{" + "scriptId=" + scriptId + ", lineNumber=" + lineNumber + ", columnNumber=" + columnNumber + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.scriptId);
        hash = 71 * hash + this.lineNumber;
        hash = 71 * hash + this.columnNumber;
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
        final Location other = (Location) obj;
        if (this.lineNumber != other.lineNumber) {
            return false;
        }
        if (this.columnNumber != other.columnNumber) {
            return false;
        }
        return Objects.equals(this.scriptId, other.scriptId);
    }

}
