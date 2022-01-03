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
package org.netbeans.modules.cnd.search;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public final class MatchingFileData {

    private final SearchParams params;
    private final String path;
    private final String fname;
    private List<Entry> entries = null;
    private Integer size = -1;

    public MatchingFileData(SearchParams params, String path) {
        this.params = params;
        this.path = path;
        fname = path.substring(1 + path.lastIndexOf('/'));
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fname;
    }

    public void setFileSize(Integer size) {
        this.size = size;
    }

    public Integer getSize() {
        return size;
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public SearchParams getSearchParams() {
        return params;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public boolean hasEntries() {
        return !(entries == null || entries.isEmpty());
    }

    public static class Entry {

        private final int line;
        private final String context;

        public Entry(int line, String context) {
            this.line = line;
            this.context = context;
        }

        public int getLineNumber() {
            return line;
        }

        public String getContext() {
            return context;
        }
    }
}
