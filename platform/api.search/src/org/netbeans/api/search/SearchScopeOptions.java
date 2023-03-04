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
package org.netbeans.api.search;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.ui.ScopeOptionsController;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.openide.util.Parameters;

/**
 * This class holds user options and custom filters.
 *
 * It is used to specify which files should be iterated by {@link SearchInfo}.
 *
 * <div class="nonnormative">It can be initialized with values specified in UI
 * component {@link ScopeOptionsController} </div>
 *
 * @author jhavlin
 */
public class SearchScopeOptions {

    static final SearchScopeOptions DEFAULT = new DefaultSearchOptions();
    private String pattern = "";                                        //NOI18N
    private boolean regexp = false;
    private boolean searchInArchives = false;
    private boolean searchInGenerated = false;
    private List<SearchFilterDefinition> filters = new LinkedList<>();

    /*
     * Use static methods to create intances. This also makes this class final
     * for outside world.
     */
    private SearchScopeOptions() {
    }

    /**
     * Add a file object filter.
     */
    public void addFilter(@NonNull SearchFilterDefinition filter) {
        Parameters.notNull("filter", filter);                           //NOI18N
        if (!filters.contains(filter)) {
            filters.add(filter);
        }
    }

    /**
     * Get list of custom filters.
     */
    public @NonNull List<SearchFilterDefinition> getFilters() {
        return filters;
    }

    /**
     * @return true if and only if searching in archives is enabled.
     */
    public boolean isSearchInArchives() {
        return searchInArchives;
    }

    /**
     * Set searching in archives.
     */
    public void setSearchInArchives(boolean searchInArchives) {
        this.searchInArchives = searchInArchives;
    }

    /**
     * @return true if and only if searching in generated sources in enabled.
     */
    public boolean isSearchInGenerated() {
        return searchInGenerated;
    }

    /**
     * Set searching in generated sources.
     */
    public void setSearchInGenerated(boolean searchInGenerated) {
        this.searchInGenerated = searchInGenerated;
    }

    /**
     * @return File name pattern. If not specified, returns empty string, never
     * null.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Set file name pattern.
     */
    public void setPattern(@NullAllowed String pattern) {
        this.pattern = pattern == null ? "" : pattern;                  //NOI18N
    }

    /**
     * @return True if and only if the pattern should be handled as regular
     * expression.
     */
    public boolean isRegexp() {
        return regexp;
    }

    /**
     * Set whether the pattern should be handled as regular expression.
     */
    public void setRegexp(boolean regexp) {
        this.regexp = regexp;
    }

    /**
     * Immutable object holding default search options.
     */
    static class DefaultSearchOptions extends SearchScopeOptions {

        private static final List<SearchFilterDefinition> LIST =
                Collections.emptyList();

        private DefaultSearchOptions() {
        }

        @Override
        public void addFilter(SearchFilterDefinition filter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<SearchFilterDefinition> getFilters() {
            return LIST;
        }
    }

    /**
     * Create instance initialized with default options.
     */
    public @NonNull static SearchScopeOptions create() {
        return new SearchScopeOptions();
    }

    /**
     * @param pattern File name pattern.
     * @param regexp Regular expression mode.
     *
     * @return Instance initialized with passed file-name related options.
     */
    public static @NonNull SearchScopeOptions create(
            @NullAllowed String pattern, boolean regexp) {
        SearchScopeOptions so = SearchScopeOptions.create();
        so.setPattern(pattern);
        so.setRegexp(regexp);
        return so;
    }

    /**
     * @param pattern File name pattern.
     * @param regexp File name pattern specified as regular expression.
     * @param searchInArchives Enable searching in archives.
     * @param searchInGenerated Enable searching in generated sources.
     * @param filters List of file object filters. Can be null.
     *
     * @return Instance initialized with passed options;
     */
    public static @NonNull SearchScopeOptions create(
            @NullAllowed String pattern, boolean regexp,
            boolean searchInArchives, boolean searchInGenerated,
            @NullAllowed List<SearchFilterDefinition> filters) {

        SearchScopeOptions so = SearchScopeOptions.create(pattern, regexp);
        so.setSearchInArchives(searchInArchives);
        so.setSearchInGenerated(searchInGenerated);
        if (filters != null) {
            for (SearchFilterDefinition fof : filters) {
                so.addFilter(fof);
            }
        }
        return so;
    }
}
