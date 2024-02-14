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
            return defaults.toArray(new SearchFilterDefinition[0]);
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
