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
package org.netbeans.modules.maven.jaxws.actions;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.jaxws.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.project.api.WebService;
import org.netbeans.modules.websvc.project.api.WebServiceData;
import org.netbeans.modules.websvc.spi.support.DefaultClientSelectionPanel;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Peter Williams, Milan Kuchtiak
 */
public class ClientExplorerPanel extends DefaultClientSelectionPanel {

    private Project[] sourceProjects;
    private Children rootChildren;
    private Node explorerClientRoot;
    private List<Node> projectNodeList;

    public ClientExplorerPanel(FileObject targetSource) {
        super(targetSource);
        sourceProjects = OpenProjects.getDefault().getOpenProjects();
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
        for (int i = 0; i < sourceProjects.length; i++) {
            Project targetProject = FileOwnerQuery.getOwner(getTargetFile());
            if (targetProject != null
                    && WSUtils.isProjectReferenceable(sourceProjects[i], targetProject)) {
                LogicalViewProvider logicalProvider =
                        (LogicalViewProvider) sourceProjects[i].getLookup().lookup(LogicalViewProvider.class);
                if (logicalProvider != null) {
                    Node rootNode = logicalProvider.createLogicalView();
                    Node[] servicesNodes = getClientNodes(sourceProjects[i]);
                    if (servicesNodes.length > 0) {
                        Children children = new Children.Array();
                        children.add(servicesNodes);
                        if (children.getNodesCount() > 0) {
                            projectNodeList.add(new ProjectNode(children, rootNode));
                        }
                    }
                }
            }

        }
        Node[] projectNodes = new Node[projectNodeList.size()];
        projectNodeList.<Node>toArray(projectNodes);
        rootChildren.add(projectNodes);
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

    private Node[] getClientNodes(Project project) {
        WebServiceData wsData = WebServiceData.getWebServiceData(project);
        if (wsData != null) {
            List<Node> nodes = new ArrayList<Node>();
            for (WebService ws : wsData.getServiceConsumers()) {
                nodes.add(ws.createNode());
            }
            return nodes.toArray(new Node[0]);
        }
        return new Node[]{};
    }

}
