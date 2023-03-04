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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.api.search.provider.impl.AbstractCompoundIterator;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras
 */
public final class SubnodesSearchInfoDefinition extends SearchInfoDefinition {

    /**
     * Children - subnodes.
     */
    private final Children children;

    public SubnodesSearchInfoDefinition(final Children children) {
        this.children = children;
    }

    /**
     */
    @Override
    public boolean canSearch() {
        return true;
    }

    /**
     */
    @Override
    public Iterator<FileObject> filesToSearch(SearchScopeOptions options,
            SearchListener listener, AtomicBoolean terminated) {
        final Node[] nodes = children.getNodes(true);
        if (nodes.length == 0) {
            return SimpleSearchInfoDefinition.EMPTY_SEARCH_INFO.filesToSearch(
                    options, listener, terminated);
        }

        List<SearchInfo> searchInfoElements =
                new ArrayList<>(nodes.length);
        for (Node child : nodes) {
            SearchInfo subInfo = 
                    SearchInfoUtils.getSearchInfoForNode(child);
            if (subInfo != null && subInfo.canSearch()) {
                searchInfoElements.add(subInfo);
            }
        }

        final int size = searchInfoElements.size();
        switch (size) {
            case 0:
                return Collections.<FileObject>emptyList().iterator();
            case 1:
                return searchInfoElements.get(0).getFilesToSearch(
                        options, listener, terminated).iterator();
            default:
                return new AbstractCompoundIterator<SearchInfo, FileObject>(
                        searchInfoElements.toArray(
                        new SearchInfo[size]),
                        options, listener, terminated) {
                    @Override
                    protected Iterator<FileObject> getIteratorFor(
                            SearchInfo element, SearchScopeOptions options,
                            SearchListener listener, AtomicBoolean terminated) {
                        return element.getFilesToSearch(
                                options, listener, terminated).iterator();
                    }
                };
        }
    }

    @Override
    public List<SearchRoot> getSearchRoots() {
        final Node[] nodes = children.getNodes(true);
        if (nodes.length == 0) {
            return Collections.emptyList();
        }
        List<SearchRoot> allRoots = new LinkedList<>();
        for (Node subNode : nodes) {
            SearchInfoDefinition subInfo =
                    SearchInfoDefinitionUtils.getSearchInfoDefinition(subNode);
            if (subInfo != null && subInfo.canSearch()) {
                allRoots.addAll(subInfo.getSearchRoots());
            }
        }
        return allRoots;
    }
}
