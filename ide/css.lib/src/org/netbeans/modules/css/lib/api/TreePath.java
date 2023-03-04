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
package org.netbeans.modules.css.lib.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a path between two {@link Node}s.
 * 
 * Can be encoded to {@link String} by {@link NodeUtil#encodeToString(org.netbeans.modules.css.lib.api.TreePath)}
 *
 * @since 1.42
 * @author mfukala@netbeans.org
 */
public class TreePath {

    private Node first,  last;
    
    /**
     * Path from root of the parse tree.
     * Same as {@link TreePath#TreePath(org.netbeans.modules.css.lib.api.Node, org.netbeans.modules.css.lib.api.Node)} with null first argument.
     * @param last 
     */
    public TreePath(Node last) {
        this(null, last);
    }
    
    /** @param first may be null; in such case a path from the root is created */
    public TreePath(Node first, Node last) {
        this.first = first;
        this.last = last;
    }

    public Node first() {
        return first;
    }
    
    public Node last() {
        return last;
    }
     
    /** returns a list of nodes from the first node to the last node including the boundaries. */
    public List<Node> path() {
        List<Node> path = new  ArrayList<>();
        Node node = last;
        while (node != null) {
            path.add(node);
            if(node == first) {
                break;
            }
            node = node.parent();
        }
        return path;
    }

    @Override
    public String toString() {
        return getNodePath();
    }
    
    private String getNodePath() {
        return NodeUtil.encodeToString(this);
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof TreePath)) {
            return false;
        }
        TreePath path = (TreePath)o;
        return getNodePath().equals(path.getNodePath());
    }
    
    @Override
    public int hashCode() {
        return getNodePath().hashCode();
    }
    
    
}
