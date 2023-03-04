/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.spi.search.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.api.search.provider.impl.DefinitionUtils;
import org.netbeans.api.search.provider.impl.FlatSearchIterator;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.openide.filesystems.FileObject;

/**
 * Search info for non-recursive searching in a single directory.
 *
 * @author jhavlin
 */
public class FlatSearchInfoDefinition extends SearchInfoDefinition {

    private FileObject rootFile;
    private SearchFilterDefinition[] filters;

    public FlatSearchInfoDefinition(FileObject rootFile,
            SearchFilterDefinition[] filters) {
        this.rootFile = rootFile;
        this.filters = filters;
    }

    @Override
    public boolean canSearch() {
        return true;
    }

    @Override
    public Iterator<FileObject> filesToSearch(SearchScopeOptions options,
            SearchListener listener, AtomicBoolean terminated) {

        return new FlatSearchIterator(rootFile, options,
                filters != null ? Arrays.asList(filters)
                : null, listener, terminated);
    }

    @Override
    public List<SearchRoot> getSearchRoots() {

        SearchRoot searchRoot = new SearchRoot(rootFile,
                DefinitionUtils.createSearchFilterList(filters));
        return Collections.singletonList(searchRoot);
    }
}
