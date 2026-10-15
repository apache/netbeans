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
package org.netbeans.modules.search.project;

import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.modules.search.project.spi.CompatibilityUtils;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.netbeans.spi.search.SubTreeSearchOptions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author jhavlin
 */
public class SearchInfoHelper {

    /**
     * Get search info for legacy or current definition in a lookup.
     */
    static org.netbeans.api.search.provider.SearchInfo getSearchInfoForLookup(
            Lookup lookup) {

        SearchInfoDefinition sid = lookup.lookup(SearchInfoDefinition.class);
        if (sid != null) {
            return SearchInfoUtils.createForDefinition(sid);
        } else {
            for (CompatibilityUtils cu :
                    Lookup.getDefault().lookupAll(CompatibilityUtils.class)) {
                SearchInfo si = cu.getSearchInfoForLookup(lookup);
                if (si != null) {
                    return si;
                }
            }
            return null;
        }
    }

    /**
     * Get search info for a node. If there is no explicit search info
     * definition, try to create default search info.
     */
    static SearchInfo getSearchInfoForNode(Node node) {

        SearchInfo searchInfo = SearchInfoUtils.findDefinedSearchInfo(node);
        if (searchInfo != null) {
            return searchInfo;
        }
        for (CompatibilityUtils cu :
                Lookup.getDefault().lookupAll(CompatibilityUtils.class)) {
            searchInfo = cu.getSearchInfoForNode(node);
            if (searchInfo != null) {
                return searchInfo;
            }
        }
        Project p = node.getLookup().lookup(Project.class);
        Project ancestorProject = findAncestorProjectNode(node.getParentNode());
        if (p != null && ancestorProject == null) { // project node
            return AbstractProjectSearchScope.createSingleProjectSearchInfo(p);
        }
        if (ancestorProject != null) {
            org.netbeans.api.search.provider.SearchInfo subTreeSearchInfo;
            subTreeSearchInfo = findSearchInfoForProjectSubTree(
                    ancestorProject, node);
            if (subTreeSearchInfo != null) {
                return subTreeSearchInfo;
            }
        }
        return SearchInfoUtils.getSearchInfoForNode(node);
    }

    /**
     * Check whether there is a project node among ancestors of a node.
     */
    private static Project findAncestorProjectNode(Node node) {
        if (node == null) {
            return null;
        }
        Project p = node.getLookup().lookup(Project.class);
        if (p != null) {
            return p;
        } else {
            return findAncestorProjectNode(node.getParentNode());
        }
    }

    /**
     * Create search info for a node that is under a project node, if the
     * project lookup contains SubTreeSearchOptions instance.
     *
     * @return SearchInfo for the node if ancestor project defines
     * SubTreeSearchOptions and a file object for the node can be found, null
     * otherwise.
     */
    private static org.netbeans.api.search.provider.SearchInfo findSearchInfoForProjectSubTree(
            Project ancestorProject, Node node) {
        SubTreeSearchOptions stso;
        stso = ancestorProject.getLookup().lookup(
                SubTreeSearchOptions.class);
        FileObject fileObject = node.getLookup().lookup(FileObject.class);
        if (fileObject == null) {
            DataObject dob = node.getLookup().lookup(DataObject.class);
            if (dob != null) {
                fileObject = dob.getPrimaryFile();
            }
        }
        if (stso != null && fileObject != null) {
            return SearchInfoUtils.createSearchInfoForRoots(
                    new FileObject[]{fileObject},
                    false, subTreeFilters(stso));
        }
        return null;
    }

    /**
     * Create an array of search filter definition for filters from a
     * SubTreeSearchOptions instance.
     */
    static SearchFilterDefinition[] subTreeFilters(SubTreeSearchOptions subTreeSearchOptions) {
        assert subTreeSearchOptions != null;
        List<SearchFilterDefinition> list = subTreeSearchOptions.getFilters();
        SearchFilterDefinition[] array = list.toArray(new SearchFilterDefinition[0]);
        return array;
    }
}
