/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.spi.search.impl;

import java.util.List;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.netbeans.spi.search.SearchInfoDefinitionFactory;
import org.netbeans.spi.search.SubTreeSearchOptions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 * Utility methods for getting or creating SearchInfoDefinition object for
 * nodes.
 *
 * @author jhavlin
 */
public final class SearchInfoDefinitionUtils {

    private SearchInfoDefinitionUtils() {
        // hide constructor
    }

    /**
     * Try to find a explicitly defined SearchInfoDefinition for a node.
     *
     * @return SearchInfoDefinition from node's lookup if any, otherwise try to
     * find SubTreeSearchOptions in the node or its ancestor. If
     * SubTreeSearchOptions instance is found, create an appropriate
     * SearchInfoDefinition for it. If it is not found, return null.
     */
    public static SearchInfoDefinition findSearchInfoDefinition(Node node) {
        return getSearchInfoDefinition(node, false);
    }

    /**
     * Gets or creates a SearchInfoDefinition for a node. If no explicit search
     * info is defined, a default one is created.
     */
    public static SearchInfoDefinition getSearchInfoDefinition(Node node) {
        return getSearchInfoDefinition(node, true);
    }

    /**
     * Get a SearchInfoDefinition for a node.
     *
     * @param node Node for which the definition should be created.
     * @param createDefault If true, default definition will be created even if
     * no explicit settings are defined. If false, null will be returned in such
     * case.
     */
    private static SearchInfoDefinition getSearchInfoDefinition(
            Node node, boolean createDefault) {
        /*
         * 1st try - is the SearchInfo object in the node's lookup?
         */
        SearchInfoDefinition info = node.getLookup().lookup(
                SearchInfoDefinition.class);
        if (info != null) {
            return info;
        }
        /*
         * 2nd try - does the node represent a DataObject.Container?
         */
        FileObject fileObject = node.getLookup().lookup(FileObject.class);
        if (fileObject == null) {
            DataObject dataObject = node.getLookup().lookup(DataObject.class);
            if (dataObject != null) {
                fileObject = dataObject.getPrimaryFile();
            }
        }
        if (fileObject == null) {
            return null;
        } else {
            SubTreeSearchOptions subTreeSearchOptions =
                    findSubTreeSearchOptions(node);
            if (subTreeSearchOptions == null && !createDefault) {
                return null;
            } else {
                return SearchInfoDefinitionFactory.createSearchInfo(
                        fileObject, getFiltersForNode(subTreeSearchOptions));
            }
        }
    }

    /**
     * Find appropriate SearchFilters for searching under a node. Default
     * filters are returned if no SubTreeSearchOptions is defined.
     */
    private static SearchFilterDefinition[] getFiltersForNode(
            SubTreeSearchOptions subTreeSearchOptions) {

        if (subTreeSearchOptions != null) {
            List<SearchFilterDefinition> filterList =
                    subTreeSearchOptions.getFilters();
            SearchFilterDefinition[] filterArray =
                    new SearchFilterDefinition[filterList.size()];
            return filterList.toArray(filterArray);
        } else {
            List<SearchFilterDefinition> defaults =
                    SearchInfoDefinitionFactory.DEFAULT_FILTER_DEFS;
            return defaults.toArray(
                    new SearchFilterDefinition[defaults.size()]);
        }
    }

    /**
     * Try to find SubTreeSearchOptions instance in lookups of a node and its
     * parent nodes.
     *
     * @param node Node where the search starts.
     * @return SubTreeSearchOptions instance of the nearest ancestor node that
     * contains one in its lookup, or null if no such ancestor was found.
     */
    private static SubTreeSearchOptions findSubTreeSearchOptions(Node node) {
        for (Node n = node; n != null; n = n.getParentNode()) {
            SubTreeSearchOptions subTreeSearchOptions =
                    n.getLookup().lookup(SubTreeSearchOptions.class);
            if (subTreeSearchOptions
                    != null) {
                return subTreeSearchOptions;
            }
        }
        return null;
    }
}
