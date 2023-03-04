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

import java.util.Stack;

/**
 *
 * @author mkleint
 */
class SearchVisitor implements GraphNodeVisitor {
    private final DependencyGraphScene scene;
    private final Stack<GraphNodeImplementation> path;
    private GraphNodeImplementation root;
    private String searchTerm;

    SearchVisitor(DependencyGraphScene scene) {
        this.scene = scene;
        path = new Stack<>();
    }

    public void setSearchString(String search) {
        searchTerm = search;
    }

    @Override public boolean visit(GraphNodeImplementation node) {
        if (root == null) {
            root = node;
        }
        if (scene.isIncluded(node)) {
            GraphNode grNode = scene.getGraphNodeRepresentant(node);
            if (grNode == null) {
                return false;
            }
            NodeWidget aw = (NodeWidget) scene.findWidget(grNode);
            aw.highlightText(searchTerm);
            path.push(node);
            return true;
        } else {
            return false;
        }
    }

    @Override public boolean endVisit(GraphNodeImplementation node) {
        if (scene.isIncluded(node)) {
            path.pop();
        }
        return true;
    }

}
