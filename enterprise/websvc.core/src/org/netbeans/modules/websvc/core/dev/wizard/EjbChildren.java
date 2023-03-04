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

package org.netbeans.modules.websvc.core.dev.wizard;

import javax.swing.Action;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbNodesFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;


/**
 * Provide a set of children representing the ejb nodes.
 * @author ChrisWebster
 */
public class EjbChildren extends Children.Array {
    private Node projectNode;
    
    /** Creates a new instance of EjbChildren */
    public EjbChildren(Node projectNode) {
        this.projectNode = projectNode;
    }

    protected void addNotify() {
        super.addNotify();
        Node ejbsNode = projectNode.getChildren().findChild(EjbNodesFactory.CONTAINER_NODE_NAME);
        // could add code here to only show ejb's which can be referenced
        Node[] ejbNodes = ejbsNode.getChildren().getNodes(true);
        Node[] filteredNodes = new Node[ejbNodes.length];
        for (int i =0; i < ejbNodes.length; i++) {
            filteredNodes[i] = new FilterNode(ejbNodes[i], Children.LEAF) {
                public Action[] getActions(boolean context) {
                    return new Action[0];
                }
            };
        }
        add(filteredNodes);
    }
    
}
