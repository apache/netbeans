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
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.SearchRoot;

/**
 *
 */
public final class SearchParams {

    private final String fileNamePattern;
    private final SearchPattern searchPattern;
    private final List<SearchRoot> roots;

    public SearchParams(List<SearchRoot> roots, String fileNamePattern, SearchPattern searchPattern) {
        this.roots = roots;
        this.fileNamePattern = fileNamePattern;
        this.searchPattern = searchPattern;
    }

    public List<SearchRoot> getSearchRoots() {
        return Collections.unmodifiableList(roots);
    }

    public SearchPattern getSearchPattern() {
        return searchPattern;
    }

    public String getFileNamePattern() {
        return fileNamePattern;
    }
}
