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
 * Search match for resource.
 */
public final class SearchMatch {
    private int lineNumber;
    private String lineContent;

    public SearchMatch() {
    }

    /**
     * Line number in resource content.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Line number in resource content.
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Line with match content.
     */
    public String getLineContent() {
        return lineContent;
    }

    /**
     * Line with match content.
     */
    public void setLineContent(String lineContent) {
        this.lineContent = lineContent;
    }

    @Override
    public String toString() {
        return "SearchMatch{" + "lineNumber=" + lineNumber + ", lineContent=" + lineContent + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + this.lineNumber;
        hash = 17 * hash + Objects.hashCode(this.lineContent);
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
        final SearchMatch other = (SearchMatch) obj;
        if (this.lineNumber != other.lineNumber) {
            return false;
        }
        return Objects.equals(this.lineContent, other.lineContent);
    }
}
