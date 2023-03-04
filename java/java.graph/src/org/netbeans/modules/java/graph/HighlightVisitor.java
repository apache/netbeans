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

package org.netbeans.modules.java.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 *
 * @author mkleint
 */
class HighlightVisitor implements GraphNodeVisitor {
    private final DependencyGraphScene scene;
    private final Stack<GraphNodeImplementation> path;
    private GraphNodeImplementation root;
    private int max = Integer.MAX_VALUE;

    private final Map<GraphNodeImplementation, Integer> node2Path;
    
    HighlightVisitor(DependencyGraphScene scene) {
        this.scene = scene;
        path = new Stack<>(); 
        node2Path = new HashMap<>();
    }

    public void setMaxDepth(int max) {
        this.max = max;
        node2Path.clear();
    }

    @Override public boolean visit(GraphNodeImplementation node) {
        if (root == null) {
            root = node;
        }
        if (scene.isIncluded(node) && acceptDepth(node)) {
            path.push(node);
            node2Path.put(node, path.size());
            
            GraphNode grNode = scene.getGraphNodeRepresentant(node);
            if (grNode == null) {
                return false;
            }
            NodeWidget nw = (NodeWidget) scene.findWidget(grNode);
            Collection<GraphEdge> edges = scene.findNodeEdges(grNode, true, true);
            nw.setReadable(false);
            if (path.size() > max) {
                nw.setPaintState(EdgeWidget.GRAYED);
                for (GraphEdge e : edges) {
                    EdgeWidget ew = (EdgeWidget) scene.findWidget(e);
                    ew.setState(EdgeWidget.GRAYED);
                }
            } else {
                nw.setPaintState(EdgeWidget.REGULAR);
                for (GraphEdge e : edges) {
                    EdgeWidget ew = (EdgeWidget) scene.findWidget(e);
                    ew.setState(EdgeWidget.REGULAR);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override public boolean endVisit(GraphNodeImplementation node) {
        if (path.peek() == node) {
            path.pop();
        }
        return true;
    }

    /**
     * accept only nodes with path shorter as the one already visited
     * @param node
     * @return 
     */
    private boolean acceptDepth(GraphNodeImplementation node) {
        Integer d = node2Path.get(node);
        if(d == null) {
            return true;
        }
        return path.size() < d;
    }

}
