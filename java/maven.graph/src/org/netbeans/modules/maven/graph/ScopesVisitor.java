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

package org.netbeans.modules.maven.graph;

import java.util.List;
import java.util.Stack;
import org.netbeans.modules.java.graph.DependencyGraphScene;
import org.netbeans.modules.java.graph.GraphNodeImplementation;
import org.netbeans.modules.java.graph.GraphNodeVisitor;

/**
 *
 * @author mkleint
 */
public class ScopesVisitor implements GraphNodeVisitor<MavenDependencyNode> {
    private final DependencyGraphScene scene;
    private GraphNodeImplementation root;
    private final Stack<GraphNodeImplementation> path;
    private final List<String> scopes;

    public ScopesVisitor(DependencyGraphScene scene, List<String> scopes) {
        this.scene = scene;
        path = new Stack<>();
        this.scopes = scopes;
    }

    @Override public boolean visit(MavenDependencyNode node) {
        if (root == null) {
            root = node;
        }
        if (scene.isIncluded(node)) {
            node.hightlightScopes(scopes);
            path.push(node);
            return true;
        } else {
            return false;
        }
    }

    @Override public boolean endVisit(MavenDependencyNode node) {
        if (scene.isIncluded(node)) {
            path.pop();
        }
        return true;
    }
}
