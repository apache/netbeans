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

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Node;

/**
 * Class that represents diff node info
 *
 * @author Ayub Khan
 */
public class NodeInfo {
    
    public NodeInfo(Node n, int pos, List<Node> ancestors1, List<Node> ancestors2) {
        if (! (n instanceof Document)) {
            assert ancestors1 != null && ! ancestors1.isEmpty() : "bad ancestors1";
            assert ancestors2 != null && ! ancestors2.isEmpty() : "bad ancestors1";
        }
        this.n = n;
        this.pos = pos;
        this.ancestors1 = ancestors1;
        this.ancestors2 = ancestors2;
    }
    
    public Node getNode() {
        return n;
    }
    
    /**
     * Only to update new version of same nodeid.
     */
    void setNode(Node node) {
        if (updated) {
            assert node.getId() == n.getId() : "expect id="+n.getId()+" got id="+node.getId();
        }
        updated = true;
        n = node;
    }
    
    /**
     * @returns position of removed in the original parent or
     *          position of the added in the final parent.
     */
    public int getPosition() {
        return pos;
    }
    
    public Node getParent() {
        if (ancestors1 != null && ancestors1.size() > 0) {
            return ancestors1.get(0);
        }
        return null;
    }
    
    /**
     * @returns document the node is captured in (a node in xdm tree can be in
     * multiple document roots).
     */
    public Document getDocument() {
        return (Document) ancestors1.get(ancestors1.size()-1);
    }
    
    /**
     * @returns original path from parent to root.
     */
    public List<Node> getOriginalAncestors() {
        return Collections.unmodifiableList(ancestors1);
    }
    
    /**
     * @returns new path to root from parent of added or removed node.
     * Note that this path need to be updated
     */
    public List<Node> getNewAncestors() {
        if (ancestors2 == null) {
            assert parent2 != null : "expect parent2 is set";
            ancestors2 = DiffFinder.getPathToRoot(parent2);
        }
        return Collections.unmodifiableList(ancestors2);
    }
    
    public void setNewAncestors(List<Node> ancestors2) {
        assert ancestors2 != null && ! ancestors2.isEmpty();
        this.ancestors2 = ancestors2;
        parent2 = ancestors2.get(0);
    }
    
    public void setNewParent(Node parent) {
        assert parent != null && parent.isInTree() : "new parent should be not null and inTree";
        ancestors2 = null;
        parent2 = parent;
    }
    
    public Node getNewParent() {
        if (parent2 == null && ! (getNode() instanceof Document)) {
            assert ancestors2 != null && ancestors2.size() > 0;
            return ancestors2.get(0);
        }
        return parent2;
    }
    
    public String toString() {
        int parentId = getParent() == null ? -1 : getParent().getId();
        return DiffFinder.getNodeType(n) + "." + pos + " ids[" + n.getId() + "," +
                parentId + "]";
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////////
    
    public static enum NodeType { ELEMENT, ATTRIBUTE, TEXT, WHITE_SPACE };
    
    ////////////////////////////////////////////////////////////////////////////////
    // Member variables
    ////////////////////////////////////////////////////////////////////////////////
    
    private Node n;
    
    private boolean updated = false;
    
    private final int pos;
    
    private final List<Node> ancestors1;
    
    private List<Node> ancestors2;  // new ancestors or would-have-been ancestors in case of delete
    
    private Node parent2;
}
