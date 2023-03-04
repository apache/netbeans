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

package org.netbeans.modules.csl.api;

import java.awt.Component;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.fold.FoldTypeProvider;

/**
 * Given a parse tree, scan its structure and produce a flat list of
 * structure items suitable for display in a navigator / outline / structure
 * view
 *
 * @todo Make this a CancellableTask
 * 
 * @author Tor Norbye
 */
public interface StructureScanner {
    /**
     * Compute a list of structure items from the parse tree. The provided
     * {@link HtmlFormatter} can be used to format the HTML strings required for
     * StructureItems.
     */
    @NonNull List<? extends StructureItem> scan(@NonNull ParserResult info);
    
    /**
     * Compute a list of foldable regions, named "codeblocks", "comments", "imports", "initial-comment", ...
     * The returned Map must be keyed by {@link FoldType#code}. For backward compatibility, the following
     * tokens are temporarily supported although no FoldType is registered explicitly.
     * <ul>
     * <li>codeblocks
     * <li>comments
     * <li>initial-comment
     * <li>imports
     * <li>tags
     * <li>inner-classes
     * <li>othercodeblocks
     * </ul>
     * This additional support will cease to exist after NB-8.0. Language owners are required to register
     * their {@link FoldTypeProvider} and define their own folding.
     */
    @NonNull Map<String,List<OffsetRange>> folds(@NonNull ParserResult info);

    /**
     * Return configuration information for this language. Typically this information
     * is only requested for the outermost language in embedded scenarios.
     * You can return null if you have no special preferences.  This can be used
     * to provide a custom filter for languages that support it, or for example
     * change the default sorting and filtering options that are provided by GSF.
     */
    @CheckForNull Configuration getConfiguration();

    /**
     * The Configuration class of the StructureScanner provides information about reasonable
     * defaults for this language; whether the navigator should contain the standard filters,
     * no filters or a custom filter, whether the items in the list naturally should be
     * sorted alphabetically (the default) or be left in their natural order, etc.
     */
    public static final class Configuration {
        private final boolean sortable;
        private final boolean filterable;
        private Component customFilter;
        private int expandDepth = -1;

        /**
         * Create a new Configuration for this structure scanner
         *
         * @param sortable If true, the structure is naturally alphabetically sortable
         * @param filterable If true, show the default filters for this language
         * @param customFilter If non null, a custom filter to be shown instead of the default
         */
        public Configuration(boolean sortable, boolean filterable) {
            this.sortable = sortable;
            this.filterable = filterable;
        }

        public Configuration(boolean sortable, boolean filterable, int expandDepth) {
            this(sortable, filterable);
            this.expandDepth = expandDepth;
        }

        /**
         * Return true (the default) iff the structure is naturally sortable.
         * The default sorting mode in the navigator is alphabetical. If you return
         * false, the natural order will be used instead.
         *
         * @return true iff the structure is naturally sortable
         */
        public boolean isSortable() {
            return sortable;
        }

        /**
         * Return true iff the structure should have the default filters shown
         *
         * @return the filterable
         */
        public boolean isFilterable() {
            return filterable;
        }

        /**
         * If non null, return a filter component to be shown instead of the default one
         * 
         * @return the customFilter
         */
        public Component getCustomFilter() {
            return customFilter;
        }

        /**
         * Return the default number of levels to automatically expand of the navigator
         * structure when a file is opened. 0 means don't expand. -1 means expand
         * everything (the default).
         *
         * @return The number of levels to expand on open, or -1 for all levels.
         */
        public int getExpandDepth() {
            return expandDepth;
        }

        /**
         * Set the default number of levels to automatically expand of the navigator
         * structure when a file is opened. 0 means don't expand. -1 means expand
         * everything (the default).
         *
         * @param depth The number of levels to expand on open, or -1 for all levels.
         */
        public void setExpandDepth(int depth) {
            this.expandDepth = depth;
        }

        /**
         * Set a filter component to be shown instead of the default one.
         *
         * @param customFilter the component containing the new filter
         */
        public void setCustomFilter(Component customFilter) {
            this.customFilter = customFilter;
        }
    }
}
