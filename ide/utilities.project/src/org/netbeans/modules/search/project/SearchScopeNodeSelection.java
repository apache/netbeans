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

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;


/**
 * Defines search scope across selected nodes.
 *
 * @author  Marian Petras
 */
final class SearchScopeNodeSelection extends SearchScopeDefinition
                                     implements LookupListener {

    @StaticResource
    private static final String MULTI_SELECTION_ID =
            "org/netbeans/modules/search/project/resources/multi_selection.png"; //NOI18N
    private static final Icon MULTI_SELECTION_ICON;

    private final Lookup.Result<Node> lookupResult;
    private LookupListener lookupListener;

    static {
        MULTI_SELECTION_ICON = ImageUtilities.loadImageIcon(
                MULTI_SELECTION_ID, false);
    }

    public SearchScopeNodeSelection() {
        Lookup lookup = Utilities.actionsGlobalContext();
        lookupResult = lookup.lookupResult(Node.class);
        lookupListener = WeakListeners.create(LookupListener.class,
                this,
                lookupResult);
        lookupResult.addLookupListener(lookupListener);
    }

    @Override
    public String getTypeId() {
        return "node selection";                                        //NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(),
                                   "SearchScopeNameSelectedNodes");     //NOI18N
    }

    @Override
    public String getAdditionalInfo() {
        final Node[] nodes = getNodes();

        if ((nodes == null) || (nodes.length == 0)) {
            return null;
        }

        return (nodes.length == 1)
               ? nodes[0].getDisplayName()
               : NbBundle.getMessage(getClass(),
                                     "SearchScopeSelectionAddInfo",     //NOI18N
                                     nodes.length);
    }

    @Override
    public boolean isApplicable() {
        return checkIsApplicable(getNodes());
    }

    private Node[] getNodes() {
        Collection<? extends Node> lookupAll =
                Utilities.actionsGlobalContext().lookupAll(Node.class);
        Node[] nodes = new Node[lookupAll.size()];
        int i = 0;
        for (Node n : lookupAll) {
            nodes[i] = n;
            i++;
        }
        return nodes;
    }

    /**
     * Decides whether searching should be enabled with respect to a set
     * of selected nodes.
     * Searching is enabled if searching instructions
     * (<code>SearchInfo</code> object) are available for all selected nodes.
     *
     * @param  nodes  selected nodes
     * @return  <code>true</code> if searching the selected nodes should be
     *          enabled; <code>false</code> otherwise
     * @see  SearchInfo
     */
    private static boolean checkIsApplicable(Node[] nodes) {
        if (nodes == null || nodes.length == 0) {
            return false;
        }

        for (Node node : nodes) {
            if (!canSearch(node)) {
                return false;
            }
        }
        return true;
    }

    /**
     */
    private static boolean canSearch(Node node) {

        SearchInfo si = SearchInfoHelper.getSearchInfoForNode(node);
        return si != null && si.canSearch();
    }

    @Override
    public SearchInfo getSearchInfo() {
        return getSearchInfo(TopComponent.getRegistry().getActivatedNodes());
    }

    private SearchInfo getSearchInfo(Node[] nodes) {
        if ((nodes == null) || (nodes.length == 0)) {
            return SearchInfoUtils.createEmptySearchInfo();
        }
        
        nodes = normalizeNodes(nodes);
        if (nodes.length == 1) {
            SearchInfo searchInfo = getSearchInfo(nodes[0]);
            return (searchInfo != null)
                    ? searchInfo
                    : SearchInfoUtils.createEmptySearchInfo();
        }
        
        List<SearchInfo> searchInfos = new ArrayList<SearchInfo>(nodes.length);
        for (Node node : nodes) {
            SearchInfo searchInfo = getSearchInfo(node);
            if (searchInfo != null) {
                searchInfos.add(searchInfo);
            }
        }
        
        if (searchInfos.isEmpty()) {
            return SearchInfoUtils.createEmptySearchInfo();
        }
        int searchInfoCount = searchInfos.size();
        if (searchInfoCount == 1) {
            return searchInfos.get(0);
        } else {
            return SearchInfoUtils.createCompoundSearchInfo(
                        searchInfos.toArray(new SearchInfo[searchInfoCount]));
        }
    }

    /**
     */
    private static SearchInfo getSearchInfo(Node node) {
        return SearchInfoHelper.getSearchInfoForNode(node);
    }

    /**
     * Computes a subset of nodes (search origins) covering all specified nodes.
     * <p>
     * Search is performed on trees whose roots are the specified nodes.
     * If node A is a member of the tree determined by node B, then the A's tree
     * is a subtree of the B's tree. It means that it is redundant to extra
     * search the A's tree. This method computes a minimum set of nodes whose
     * trees cover all nodes' subtrees but does not contain any node not covered
     * by the original set of nodes.
     *
     * @param  nodes  roots of search trees
     * @return  subset of the original set of nodes
     *          (may be the same object as the parameter)
     */
    private static Node[] normalizeNodes(Node[] nodes) {
        
        /* No need to normalize: */
        if (nodes.length < 2) {
            return nodes;
        }
        
        /*
         * In the algorithm, we use two sets of nodes: "good nodes" and "bad
         * nodes". "Good nodes" are nodes not known to be covered by any 
         * search root. "Bad nodes" are nodes known to be covered by at least
         * one of the search roots.
         *
         * Since all the search roots are covered by themselves, they are all
         * put to "bad nodes" initially. To recognize whether a search root
         * is covered only by itself or whether it is covered by any other
         * search root, the former group of nodes has mapped value FALSE
         * and the later group of nodes has mapped value TRUE.
         *
         * Initially, all search roots have mapped value FALSE (not known to be
         * covered by any other search root) and as the procedure runs, some of
         * them may be remapped to value TRUE (known to be covered by at least
         * one other search root).
         *
         * The algorithm checks all search roots one by one. The ckeck starts
         * at a search root to be tested and continues up to its parents until
         * one of the following:
         *  a) the root of the whole tree of nodes is reached
         *     - i.e. the node being checked is not covered by any other
         *       search root
         *     - mark all the nodes in the path from the node being checked
         *       to the root as "good nodes", except the search root being
         *       checked
         *     - put the search root being checked into the resulting set
         *       of nodes
         *  b) a "good node" is reached
         *     - i.e. neither the good node nor any of the nodes on the path
         *       are covered by any other search root
         *     - mark all the nodes in the path from the node being checked
         *       to the root as "good nodes", except the search root being
         *       checked
         *     - put the search root being checked into the resulting set
         *       of nodes
         *  c) a "bad node" is reached (it may be either another search root
         *     or another "bad node")
         *     - i.e. we know that the reached node is covered by another search
         *       root or the reached node is another search root - in both cases
         *       the search root being checked is covered by another search root
         *     - mark all the nodes in the path from the node being checked
         *       to the root as "bad nodes"; the search root being checked
         *       will be remapped to value TRUE
         */
        
        Map<Node, Boolean> badNodes = new HashMap<Node, Boolean>(2 * nodes.length, 0.75f);
        Map<Node, Boolean> goodNodes = new HashMap<Node, Boolean>(2 * nodes.length, 0.75f);
        List<Node> path = new ArrayList<Node>(10);
        List<Node> result = new ArrayList<Node>(nodes.length);
        
        /* Put all search roots into "bad nodes": */
        for (Node node : nodes) {
            badNodes.put(node, Boolean.FALSE);
        }
        
        main_cycle:
        for (Node node : nodes) {
            path.clear();
            boolean isBad = false;
            for (Node n = node.getParentNode(); n != null;
                                                n = n.getParentNode()) {
                if (badNodes.containsKey(n)) {
                    isBad = true;
                    break;
                }
                if (goodNodes.containsKey(n)) {
                    break;
                }
                path.add(n);
            }
            if (isBad) {
                badNodes.put(node, Boolean.TRUE);
		for (Node n : path) {
                    badNodes.put(n, Boolean.TRUE);
		}
            } else {
                for (Node n : path) {
                    goodNodes.put(n, Boolean.TRUE);
                }
                result.add(node);
            }
        }
        return result.toArray(new Node[0]);
    }

    @Override
    public void clean() {
        if (lookupResult != null && lookupListener != null) {
            lookupResult.removeLookupListener(lookupListener);
        }
    }

    @Override
    public int getPriority() {
        return 400;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        notifyListeners();
    }

    @Override
    public Icon getIcon() {
        Node[] nodes = getNodes();
        if (nodes.length > 1) {
            return MULTI_SELECTION_ICON;
        } else if (nodes.length == 1 && nodes[0] != null) {
            Node n = nodes[0];
            Image image = n.getIcon(BeanInfo.ICON_COLOR_16x16);
            if (image != null) {
                return ImageUtilities.image2Icon(image);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
