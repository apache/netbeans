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

public final class SearchInContentRequest {
    private String scriptId;
    private String query;
    private Boolean caseSensitive;
    private Boolean isRegex;

    public SearchInContentRequest() {
    }

    /**
     * Id of the script to search in.
     */
    public String getScriptId() {
        return scriptId;
    }

    /**
     * Id of the script to search in.
     */
    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    /**
     * String to search for.
     */
    public String getQuery() {
        return query;
    }

    /**
     * String to search for.
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * If true, search is case sensitive.
     */
    public Boolean getCaseSensitive() {
        return caseSensitive;
    }

    /**
     * If true, search is case sensitive.
     */
    public void setCaseSensitive(Boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * If true, treats string parameter as regex.
     */
    public Boolean getIsRegex() {
        return isRegex;
    }

    /**
     * If true, treats string parameter as regex.
     */
    public void setIsRegex(Boolean isRegex) {
        this.isRegex = isRegex;
    }

    @Override
    public String toString() {
        return "SearchInContentRequest{" + "scriptId=" + scriptId + ", query=" + query + ", caseSensitive=" + caseSensitive + ", isRegex=" + isRegex + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.scriptId);
        hash = 89 * hash + Objects.hashCode(this.query);
        hash = 89 * hash + Objects.hashCode(this.caseSensitive);
        hash = 89 * hash + Objects.hashCode(this.isRegex);
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
        final SearchInContentRequest other = (SearchInContentRequest) obj;
        if (!Objects.equals(this.scriptId, other.scriptId)) {
            return false;
        }
        if (!Objects.equals(this.query, other.query)) {
            return false;
        }
        if (!Objects.equals(this.caseSensitive, other.caseSensitive)) {
            return false;
        }
        return Objects.equals(this.isRegex, other.isRegex);
    }


}
