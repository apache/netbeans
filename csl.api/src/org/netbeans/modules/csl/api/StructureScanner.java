/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
