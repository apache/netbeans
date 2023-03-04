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

package org.netbeans.modules.maven.dependencies;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;

/**
 *
 * @author mkleint
 */
public class DependencyExcludeNodeVisitor implements DependencyNodeVisitor {
    private DependencyNode root;
    private Set<DependencyNode> directs;
    private Stack<DependencyNode> path;
    private Set<Stack<DependencyNode>> allPaths;
    private String key;

    public DependencyExcludeNodeVisitor(String groupId, String artifactId, String type) {
        assert groupId != null;
        assert artifactId != null;
        key = groupId + ":" + artifactId + ":" + type;
    }

    public Set<DependencyNode> getDirectDependencies() {
        return directs;
    }

    public Set<Stack<DependencyNode>> getAllPaths() {
        return allPaths;
    }

    @Override
    public boolean visit(DependencyNode node) {
        if (root == null) {
            root = node;
            directs = new HashSet<DependencyNode>();
            path = new Stack<DependencyNode>();
            allPaths = new HashSet<Stack<DependencyNode>>();
            return true;
        }
        path.push(node);
        Artifact artifact = node.getArtifact();
        if (key.equals(artifact.getDependencyConflictId())) {
            if (!path.isEmpty()) {
                directs.add(path.firstElement());
                Stack<DependencyNode> copy = new Stack<DependencyNode>();
                copy.addAll(path);
                allPaths.add(copy);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean endVisit(DependencyNode node) {
        if (root == node) {
            root = null;
            path = null;
            return true;
        }
        path.pop();
        return true;
    }

}
