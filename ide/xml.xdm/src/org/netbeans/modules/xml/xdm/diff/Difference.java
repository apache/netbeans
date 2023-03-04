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

package org.netbeans.modules.xml.xdm.diff;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Node;

/**
 * This class represents diff between 2 elements of 2 XML documents
 *
 * @author Ayub Khan
 */
public abstract class Difference {
    
    /** Creates a new instance of DiffEvent */
    public Difference(NodeInfo.NodeType nodeType,
            List<Node> ancestors1, List<Node> ancestors2,
            Node n1, Node n2, int n1Pos, int n2Pos) {
        this.nodeType = nodeType;
        if (! (n1 instanceof Document)) {
            assert ancestors1 != null && ! ancestors1.isEmpty() : "diff of non-root should have ancestors";
        }
        this.oldNodeInfo = new NodeInfo( n1, n1Pos, ancestors1, ancestors2);
        this.newNodeInfo = new NodeInfo( n2, n2Pos, new ArrayList(ancestors1), new ArrayList(ancestors2));
        if (newNodeInfo.getNode() != null && newNodeInfo.getNewAncestors().size() > 0) {
            assert newNodeInfo.getNewAncestors().get(0).getId() != newNodeInfo.getNode().getId();
        }
    }
    
    public NodeInfo.NodeType getNodeType() {
        return nodeType;
    }
    
    /**
     * @returns info on removed node.
     */
    public NodeInfo getOldNodeInfo() {
        return oldNodeInfo;
    }
    
    /**
     * @return info on added node.
     */
    public NodeInfo getNewNodeInfo() {
        return newNodeInfo;
    }
    
    /**
     * @return new path from parent to root.
     */
    public abstract List<Node> getNewAncestors();
    
    public abstract void setNewParent(Node n);
    
    public abstract Node getNewParent();
    
    ////////////////////////////////////////////////////////////////////////////////
    // Member variables
    ////////////////////////////////////////////////////////////////////////////////
    
    private NodeInfo.NodeType nodeType;
    
    private NodeInfo oldNodeInfo;
    
    private NodeInfo newNodeInfo;
    
}
