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

public final class SetBreakpointByUrlRequest {
    private int lineNumber;
    private String url;
    private String urlRegex;
    private String scriptHash;
    private Integer columnNumber;
    private String condition;

    public SetBreakpointByUrlRequest() {
    }

    /**
     * Line number to set breakpoint at.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Line number to set breakpoint at.
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * URL of the resources to set breakpoint on.
     */
    public String getUrl() {
        return url;
    }

    /**
     * URL of the resources to set breakpoint on.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Regex pattern for the URLs of the resources to set breakpoints on. Either
     * {@link #url} or {@link #urlRegex} must be specified.
     */
    public String getUrlRegex() {
        return urlRegex;
    }

    /**
     * Regex pattern for the URLs of the resources to set breakpoints on. Either
     * {@link #url} or {@link #urlRegex} must be specified.
     */
    public void setUrlRegex(String urlRegex) {
        this.urlRegex = urlRegex;
    }

    /**
     * Script hash of the resources to set breakpoint on.
     */
    public String getScriptHash() {
        return scriptHash;
    }

    /**
     * Script hash of the resources to set breakpoint on.
     */
    public void setScriptHash(String scriptHash) {
        this.scriptHash = scriptHash;
    }

    /**
     * Offset in the line to set breakpoint at.
     */
    public Integer getColumnNumber() {
        return columnNumber;
    }

    /**
     * Offset in the line to set breakpoint at.
     */
    public void setColumnNumber(Integer columnNumber) {
        this.columnNumber = columnNumber;
    }

    /**
     * Expression to use as a breakpoint condition. When specified, debugger
     * will only stop on the breakpoint if this expression evaluates to true.
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Expression to use as a breakpoint condition. When specified, debugger
     * will only stop on the breakpoint if this expression evaluates to true.
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "BreakpointByUrlRequest{" + "lineNumber=" + lineNumber + ", url=" + url + ", urlRegex=" + urlRegex + ", scriptHash=" + scriptHash + ", columnNumber=" + columnNumber + ", condition=" + condition + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + this.lineNumber;
        hash = 11 * hash + Objects.hashCode(this.url);
        hash = 11 * hash + Objects.hashCode(this.urlRegex);
        hash = 11 * hash + Objects.hashCode(this.scriptHash);
        hash = 11 * hash + Objects.hashCode(this.columnNumber);
        hash = 11 * hash + Objects.hashCode(this.condition);
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
        final SetBreakpointByUrlRequest other = (SetBreakpointByUrlRequest) obj;
        if (this.lineNumber != other.lineNumber) {
            return false;
        }
        if (!Objects.equals(this.urlRegex, other.urlRegex)) {
            return false;
        }
        if (!Objects.equals(this.scriptHash, other.scriptHash)) {
            return false;
        }
        if (!Objects.equals(this.condition, other.condition)) {
            return false;
        }
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        return Objects.equals(this.columnNumber, other.columnNumber);
    }

    
}
