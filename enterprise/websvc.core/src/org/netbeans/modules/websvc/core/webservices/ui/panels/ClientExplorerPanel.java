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
package org.netbeans.modules.websvc.core.webservices.ui.panels;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.core.ProjectClientView;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.FilterNode;
import org.netbeans.modules.websvc.spi.support.DefaultClientSelectionPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Williams, Milan Kuchtiak
 */
public class ClientExplorerPanel extends DefaultClientSelectionPanel {

    private Project[] projects;
    private Children rootChildren;
    private Node explorerClientRoot;
    private List<Node> projectNodeList;

    public ClientExplorerPanel(FileObject srcFileObject) {
        super(srcFileObject);
        projects = OpenProjects.getDefault().getOpenProjects();
        rootChildren = new Children.Array();
        explorerClientRoot = new AbstractNode(rootChildren);
        projectNodeList = new ArrayList<Node>();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        getTreeView().expandAll();
    }

    @Override
    protected Node getRootContext() {
        for (int i=0;i<projects.length;i++) {
            Project srcFileProject = FileOwnerQuery.getOwner(getTargetFile());
            if (srcFileProject!=null && JaxWsUtils.isProjectReferenceable(projects[i], srcFileProject)) {
                LogicalViewProvider logicalProvider = (LogicalViewProvider)projects[i].getLookup().lookup(LogicalViewProvider.class);
                if (logicalProvider!=null) {
                    Node rootNode = logicalProvider.createLogicalView();
                    Node[] servicesNodes = ProjectClientView.createClientView(projects[i]);
                    if (servicesNodes!=null && servicesNodes.length>0) {
                        Children children = new Children.Array();
                        for(Node servicesNode:servicesNodes) {
                            Node[] nodes= servicesNode.getChildren().getNodes();
                            if (nodes!=null && nodes.length>0) {
                                Node[] filterNodes = new Node[nodes.length];
                                for (int j=0;j<nodes.length;j++) filterNodes[j] = new FilterNode(nodes[j]);
                                children.add(filterNodes);
                            }
                        }
                        if(children.getNodesCount()>0)
                            projectNodeList.add(new ProjectNode(children, rootNode));
                    }
                }
            }

        }
        if (projectNodeList.size() > 0) {
            Node[] projectNodes = new Node[projectNodeList.size()];
            projectNodeList.<Node>toArray(projectNodes);
            rootChildren.add(projectNodes);
        } else {
            Node noClients = new NoServicesNode();
            rootChildren.add(new Node[]{noClients});
        }
        return explorerClientRoot;
    }

    @Override
    protected boolean isClientNode(Node node) {
        return node.getLookup().lookup(WsdlOperation.class) != null;
    }

    private static class ProjectNode extends AbstractNode {
        private Node rootNode;

        ProjectNode(Children children, Node rootNode) {
            super(children);
            this.rootNode = rootNode;
            setName(rootNode.getDisplayName());
        }

        @Override
        public Image getIcon(int type) {
            return rootNode.getIcon(type);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return rootNode.getOpenedIcon(type);
        }
    }

    private static class NoServicesNode extends AbstractNode {

        NoServicesNode() {
            super(Children.LEAF);
            setIconBaseWithExtension("org/netbeans/modules/websvc/core/webservices/ui/resources/webservice.png"); //NOI18N
            setName("no_ws_clients"); //NOI18N
            setDisplayName(NbBundle.getMessage(ClientExplorerPanel.class, "LBL_no_ws_clients"));
        }
    }


}
