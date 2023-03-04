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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.mdb.MessageNode;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbNodesFactory;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;

/**
 * Provide a set of children representing the ejb nodes.
 * @author ChrisWebster
 */
public class EjbChildren extends Children.Array implements NodeListener{
    private final Node projectNode;
    private final Node ejbsNode;

    /** Creates a new instance of EjbChildren */
    public EjbChildren(Node projectNode) {
        this.projectNode = projectNode;
        this.ejbsNode = projectNode.getChildren().findChild(EjbNodesFactory.CONTAINER_NODE_NAME);
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        if (ejbsNode != null) {
            ejbsNode.addNodeListener(this);
            addChildrens(ejbsNode.getChildren().getNodes(true));
                }
            }

    @Override
    protected void removeNotify() {
        super.removeNotify();
        if (ejbsNode != null){
            ejbsNode.removeNodeListener(this);
        }
    }

    private void addChildrens(Node[] nodesToAdd){
        if (nodesToAdd != null){
            Project project = projectNode.getLookup().lookup(Project.class);
            List<Node> filteredNodes = new ArrayList<Node>();
            for (Node node : nodesToAdd) {
                // #75721: MDB should not appear in Call EJB dialog
                if (node instanceof MessageNode)
                    continue;

                FileObject fo = node.getLookup().lookup(FileObject.class);
                Project foProject = fo == null ? null : FileOwnerQuery.getOwner(fo);
                if((foProject != null) && (project != foProject))
                    continue;

                filteredNodes.add(new FilterNode(node, Children.LEAF) {
                    @Override
                    public Action[] getActions(boolean context) {
                        return new Action[0];
                    }
                });
            }
            Node[] filteredNodesArray = new Node[filteredNodes.size()];
            add(filteredNodes.toArray(filteredNodesArray));
        }
    }

    public void childrenAdded(NodeMemberEvent ev) {
        addChildrens(ev.getDelta());
    }

    public void childrenRemoved(NodeMemberEvent ev) {
        remove(ev.getDelta());
    }

    public void childrenReordered(NodeReorderEvent ev) {
        //nothing to do
    }

    public void nodeDestroyed(NodeEvent ev) {
        remove(new Node[]{ev.getNode()});
    }

    public void propertyChange(PropertyChangeEvent evt) {
        //nothing to do
    }
    
}
