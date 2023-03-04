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
package org.netbeans.api.search.provider.impl;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchListener;
import org.openide.filesystems.FileObject;

/**
 * Always empty search info.
 *
 * @author jhavlin
 */
public class EmptySearchInfo extends SearchInfo {

    @Override
    public boolean canSearch() {
        return false;
    }

    @Override
    public List<SearchRoot> getSearchRoots() {
        return Collections.emptyList();
    }

    @Override
    public Iterator<FileObject> createFilesToSearchIterator(
            SearchScopeOptions options, SearchListener listener,
            AtomicBoolean terminated) {

        return Collections.<FileObject>emptyList().iterator();
    }

    @Override
    public Iterator<URI> createUrisToSearchIterator(
            SearchScopeOptions options, SearchListener listener,
            AtomicBoolean terminated) {

        return Collections.<URI>emptyList().iterator();
    }
}
