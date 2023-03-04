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
package org.netbeans.modules.search;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.search.MatchingObject.Def;
import org.netbeans.modules.search.matcher.AbstractMatcher;
import org.netbeans.modules.search.ui.UiUtils;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.netbeans.spi.search.provider.SearchComposition;
import org.netbeans.spi.search.provider.SearchProvider;
import org.netbeans.spi.search.provider.SearchProvider.Presenter;
import org.netbeans.spi.search.provider.SearchResultsDisplayer;
import org.openide.filesystems.FileObject;

/**
 *
 * @author jhavlin
 */
public class BasicComposition extends SearchComposition<MatchingObject.Def> {

    private SearchInfo searchInfo;
    private AbstractMatcher matcher;
    private SearchResultsDisplayer<MatchingObject.Def> displayer = null;
    private BasicSearchCriteria basicSearchCriteria;
    private Presenter presenter;
    private String scopeDisplayName;
    AtomicBoolean terminated = new AtomicBoolean(false);

    public BasicComposition(SearchInfo searchInfo, AbstractMatcher matcher,
            BasicSearchCriteria basicSearchCriteria, String scopeDisplayName) {

        this.searchInfo = searchInfo;
        this.matcher = matcher;
        this.basicSearchCriteria = basicSearchCriteria;
        this.scopeDisplayName = scopeDisplayName;
        this.presenter = BasicSearchProvider.createBasicPresenter(
                basicSearchCriteria.isSearchAndReplace(),
                basicSearchCriteria.getSearchPattern(),
                basicSearchCriteria.getReplaceExpr(),
                basicSearchCriteria.isPreserveCase(),
                basicSearchCriteria.getSearcherOptions(),
                basicSearchCriteria.isUseIgnoreList(), "last",          //NOI18N
                new LastScopeDefinition(searchInfo, scopeDisplayName));
    }

    @Override
    public void start(SearchListener listener) {

        Iterable<FileObject> iterable = searchInfo.getFilesToSearch(
                basicSearchCriteria.getSearcherOptions(),
                listener, terminated);

        for (FileObject fo : iterable) {

            Def result = matcher.check(fo, listener);
            if (result != null) {
                getSearchResultsDisplayer().addMatchingObject(result);
            }
            if (terminated.get()) {
                break;
            }
        }
    }

    @Override
    public void terminate() {
        terminated.set(true);
        matcher.terminate();
    }

    @Override
    public boolean isTerminated() {
        return terminated.get();
    }

    @Override
    public synchronized SearchResultsDisplayer<Def> getSearchResultsDisplayer() {
        if (displayer == null) {
            displayer = new ResultDisplayer(basicSearchCriteria,
                    this);
        }
        return displayer;
    }

    public SearchResultsDisplayer<Def> getDisplayer() {
        return displayer;
    }

    public List<FileObject> getRootFiles() {
        List<FileObject> list = new LinkedList<>();
        List<SearchRoot> searchRoots = searchInfo.getSearchRoots();
        if (searchRoots == null) {
            return Collections.emptyList();
        }
        for (SearchRoot sr : searchRoots) {
            list.add(sr.getFileObject());
        }
        return list;
    }

    public SearchProvider.Presenter getSearchProviderPresenter() {
        return presenter;
    }

    public BasicSearchCriteria getBasicSearchCriteria() {
        return basicSearchCriteria;
    }

    /**
     * Search scope conserving last used search info.
     */
    private static class LastScopeDefinition extends SearchScopeDefinition {

        private static final String PREFIX =
                UiUtils.getText("LBL_ScopeLastName");                   //NOI18N
        private SearchInfo searchInfo;
        private String lastScopeDisplayName;

        public LastScopeDefinition(SearchInfo lastSearchInfo,
                String lastScopeDisplayName) {
            this.searchInfo = lastSearchInfo;
            this.lastScopeDisplayName = normalizeTitle(lastScopeDisplayName);
        }

        @Override
        public String getTypeId() {
            return "last";                                              //NOI18N
        }

        @Override
        public String getDisplayName() {
            return PREFIX + (lastScopeDisplayName == null
                    ? "" : ": " + lastScopeDisplayName);                //NOI18N
        }

        @Override
        public boolean isApplicable() {
            return true;
        }

        @Override
        public SearchInfo getSearchInfo() {
            return searchInfo;
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public void clean() {
        }

        private String normalizeTitle(String title) {
            if (title == null || title.equals(PREFIX)) {
                return null;
            } else if (title.startsWith(PREFIX + ": ")) {               //NOI18N
                return title.substring(PREFIX.length() + 2);
            } else {
                return title;
            }
        }
    }

    public SearchInfo getSearchInfo() {
        return searchInfo;
    }

    public AbstractMatcher getMatcher() {
        return matcher;
    }

    public String getScopeDisplayName() {
        return scopeDisplayName;
    }
}
