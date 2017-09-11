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
        SearchFilterDefinition[] array = list.toArray(
                new SearchFilterDefinition[list.size()]);
        return array;
    }
}
