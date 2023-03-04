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
package org.netbeans.modules.maven.repository;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.apache.lucene.search.BooleanQuery;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import static org.netbeans.modules.maven.repository.Bundle.*;
import org.netbeans.modules.maven.repository.M2RepositoryBrowser.QueryRequest;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author  mkleint
 */
public class FindResultsNode extends AbstractNode {

    private static final @StaticResource String FIND_IN_REPO = "org/netbeans/modules/maven/repository/FindInRepo.png";
    private static final @StaticResource String ARTIFACT_BADGE = "org/netbeans/modules/maven/repository/ArtifactBadge.png";
    private static final @StaticResource String EMPTY_ICON = "org/netbeans/modules/maven/repository/empty.png";
    private static final @StaticResource String WAIT_ICON = "org/netbeans/modules/maven/repository/wait.gif";

    
    private static final RequestProcessor queryRP = new RequestProcessor(FindResultsNode.class.getName(), 10);
    private final QueryRequest queryRequest;
    private String toAppend;
    private final HtmlDisplayNameChanger changer = new HtmlDisplayNameChanger();

    FindResultsNode(QueryRequest request) {
        super(Children.LEAF, Lookups.singleton(request));
        changer.setNode(this);
        setChildren(Children.create(new FindResultsChildren(request.fields, request.infos, changer), true));
        setDisplayName(request.fields.get(0).getValue());
        setIconBaseWithExtension(FIND_IN_REPO);
        queryRequest = request;
    }

    @Override public boolean canDestroy() {
        return true;
    }
    
    private void changeHDM(String toAppend) {
        this.toAppend = toAppend;
        fireDisplayNameChange(null, null);
    }

    @Override public void destroy() throws IOException {
        M2RepositoryBrowser.remove(queryRequest);
    }

    @Override public Action[] getActions(boolean context) {
        return new Action[] {SystemAction.get(DeleteAction.class)};
    }

    @Override
    public String getHtmlDisplayName() {
        StringBuilder base = new StringBuilder().append(getDisplayName());
        if (toAppend != null) {
            base.append(" <font color='!controlShadow'>[");
            base.append(toAppend);
            base.append("]</font>");
        }
        return base.toString();
    }
    
    private static class HtmlDisplayNameChanger {
        private FindResultsNode node;
        void changeHtmlDisplayName(String toAppend) {
            assert node != null;
            node.changeHDM(toAppend);
        }
        
        void setNode(FindResultsNode node) {
            this.node = node;
        }
    }
    
    

    // XXX clumsy, use a real key instead (NBGroupInfo?) and replace no results/too general nodes with status line notifications
    private static class FindResultsChildren extends ChildFactory.Detachable<Node> {

        private List<Node> nodes;
        private final List<QueryField> fields;
        private final List<RepositoryInfo> infos;
        private final HtmlDisplayNameChanger changer;

        @Messages("MSG_Narrow={0} of {1} results shown. Narrow your search.")
        FindResultsChildren(List<QueryField> fields, List<RepositoryInfo> infos, HtmlDisplayNameChanger changer) {
            this.fields = fields;
            this.infos = infos;
            this.changer = changer;
        }

        @Override protected Node createNodeForKey(Node key) {
            return key;
        }

        @Override 
        protected boolean createKeys(List<Node> toPopulate) {
            if (nodes != null) {
                toPopulate.addAll(nodes);
            } else {
        queryRP.post(new Runnable() {

            @Override
            public void run() {
                try {
                    Result<NBVersionInfo> result = RepositoryQueries.findResult(fields, infos);
                    update(result, result.isPartial());
                    if (result.isPartial()) {
                        result.waitForSkipped();
                        update(result, false);
                    }
                    if (result.getReturnedResultCount() != result.getTotalResultCount()) {
                        changer.changeHtmlDisplayName(MSG_Narrow(result.getReturnedResultCount(), result.getTotalResultCount()));
                    }
                } catch (BooleanQuery.TooManyClauses exc) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            nodes = Collections.singletonList(getTooGeneralNode());
                        }
                    });
                } catch (final OutOfMemoryError oome) {
                    // running into OOME may still happen in Lucene despite the fact that
                    // we are trying hard to prevent it in NexusRepositoryIndexerImpl
                    // (see #190265)
                    // in the bad circumstances theoretically any thread may encounter OOME
                    // but most probably this thread will be it
                    // trying to indicate the condition to the user here
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            nodes = Collections.singletonList(getTooGeneralNode());
                        }
                    });
                }
            }
        });
    }
            return true; // XXX queryRequest.isFinished() unsuitable here
        }

    void update(Result<NBVersionInfo> result, final boolean partial) {
        final Map<String, List<NBVersionInfo>> map = new HashMap<String, List<NBVersionInfo>>();

        if (result.getResults() != null) {
            for (NBVersionInfo nbvi : result.getResults()) {
                String key = nbvi.getGroupId() + " : " + nbvi.getArtifactId(); //NOI18n
                List<NBVersionInfo> get = map.get(key);
                if (get == null) {
                    get = new ArrayList<NBVersionInfo>();
                    map.put(key, get);
                }
                get.add(nbvi);
            }
        }

        final List<String> keyList = new ArrayList<String>(map.keySet());
        Collections.sort(keyList);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateResultNodes(keyList, map, partial);
            }
        });
    }

    private void updateResultNodes(List<String> keyList, Map<String, List<NBVersionInfo>> map, boolean partial) {

            if (keyList.size() > 0) { // some results available
                
                Map<String, Node> currentNodes = new HashMap<String, Node>();
                if (nodes != null) {
                for (Node nd : nodes) {
                    currentNodes.put(nd.getName(), nd);
                }
                }
                List<Node> newNodes = new ArrayList<Node>(keyList.size());

                for (String key : keyList) {
                    Node nd;
                    nd = currentNodes.get(key);
                    if (null != nd) {
                        ((ArtifactNode)nd).setVersionInfos(map.get(key));
                    } else {
                        nd = new ArtifactNode(key, map.get(key));
                    }
                    newNodes.add(nd);
                }
                if (partial) { // still searching, no results yet
                    newNodes.add(getPartialNode());
                }
                
                nodes = newNodes;
                refresh(false);
            } else if (partial) { // still searching, no results yet
                nodes = Collections.singletonList(getPartialNode());
            } else { // finished searching with no results
                nodes = Collections.singletonList(getNoResultsNode());
            }
        }

    }

    private static class ArtifactNode extends AbstractNode {

        private List<NBVersionInfo> versionInfos;
        private ArtifactNodeChildren myChildren;

        public ArtifactNode(String name, List<NBVersionInfo> list) {
            super(new ArtifactNodeChildren(list));
            myChildren = (ArtifactNodeChildren)getChildren();
            this.versionInfos=list;
            setName(name);
            setDisplayName(name);
        }

        @Override
        public Image getIcon(int arg0) {
            Image badge = ImageUtilities.loadImage(ARTIFACT_BADGE, true);

            return badge;
        }

        @Override
        public Image getOpenedIcon(int arg0) {
            return getIcon(arg0);
        }

        public @Override Action[] getActions(boolean context) {
            return new Action[0];
        }

        public List<NBVersionInfo> getVersionInfos() {
            return new ArrayList<NBVersionInfo>(versionInfos);
        }

        public void setVersionInfos(List<NBVersionInfo> infos) {
            versionInfos = infos;
            myChildren.setNewKeys(infos);
        }

        static class ArtifactNodeChildren extends Children.Keys<NBVersionInfoTuple> {

            private List<NBVersionInfoTuple> keys;


            public ArtifactNodeChildren(List<NBVersionInfo> keys) {
                this.keys = processKeys(keys);
            }

            @Override
            protected Node[] createNodes(NBVersionInfoTuple info) {                
                return new Node[]{new VersionNode(info.repo, info.info, info.info.isJavadocExists(),
                            info.info.isSourcesExists(), true)
                        };
            }

            @Override
            protected void addNotify() {
                setKeys(keys);
            }

            protected void setNewKeys(List<NBVersionInfo> keys) {
                this.keys = processKeys(keys);
                setKeys(this.keys);
            }

            private List<NBVersionInfoTuple> processKeys(List<NBVersionInfo> keys) {
                List<NBVersionInfoTuple> toRet = new ArrayList<NBVersionInfoTuple>();
                HashMap<RepositoryInfo, Set<NBVersionInfo>> map = new HashMap<RepositoryInfo, Set<NBVersionInfo>>();
                for (NBVersionInfo k : keys) {
                    RepositoryInfo rinf = RepositoryPreferences.getInstance().getRepositoryInfoById(k.getRepoId());
                    NBVersionInfoTuple t = new NBVersionInfoTuple(k, rinf);
                    toRet.add(t);
                    Set<NBVersionInfo> set = map.get(rinf);
                    if (set == null) {
                        set = new HashSet<NBVersionInfo>();
                        map.put(rinf, set);
                    }
                    set.add(k);
                }
                Iterator<NBVersionInfoTuple> it = toRet.iterator();
                //this stuff is likely slow for large amount of data..
                LBL: while (it.hasNext()) {
                    NBVersionInfoTuple one = it.next();
                    if (one.repo.isLocal()) { //for local ones right now only..
                        for (java.util.Map.Entry<RepositoryInfo, Set<NBVersionInfo>> ent : map.entrySet()) {
                            if (ent.getKey().equals(one.repo)) {
                                continue;
                            }
                            for (NBVersionInfo ver : ent.getValue()) {
                                if (one.info.compareToWithoutRepoId(ver) == 0) {
                                    //do some kind of merging?
                                    it.remove();
                                    continue LBL;
                                }
                            }
                        }
                    }
                }
                return toRet;
            }
        }
        static class NBVersionInfoTuple {
            final NBVersionInfo info;
            final RepositoryInfo repo;

            public NBVersionInfoTuple(NBVersionInfo info, RepositoryInfo repo) {
                this.info = info;
                this.repo = repo;
            }
            
        }
    }

    private static Node noResultsNode, tooGeneralNode, partialNode;

    @Messages("LBL_Node_Empty=No matching items")
    private static Node getNoResultsNode() {
        if (noResultsNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage(WAIT_ICON);
                    }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Empty"); //NOI18N

            nd.setDisplayName(LBL_Node_Empty()); //NOI18N

            noResultsNode = nd;
        }

        return new FilterNode (noResultsNode, Children.LEAF);
    }
    
    @Messages("LBL_Node_Partial=Partial result, waiting for indexing to finish.")
    private static Node getPartialNode() {
        if (partialNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage(EMPTY_ICON);
                }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("partial"); //NOI18N

            nd.setDisplayName(LBL_Node_Partial()); //NOI18N

            partialNode = nd;
        }

        return new FilterNode (partialNode, Children.LEAF);
    }    

    @Messages("LBL_Node_TooGeneral=Too general query")
    private static Node getTooGeneralNode() {
        if (tooGeneralNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage(EMPTY_ICON);
                    }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Too General"); //NOI18N

            nd.setDisplayName(LBL_Node_TooGeneral()); //NOI18N

            tooGeneralNode = nd;
        }

        return new FilterNode (tooGeneralNode, Children.LEAF);
    }

    
}
