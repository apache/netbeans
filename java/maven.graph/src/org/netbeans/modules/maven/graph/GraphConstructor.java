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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.modules.java.graph.GraphEdge;
import org.netbeans.modules.java.graph.GraphNode;
import org.netbeans.modules.java.graph.DependencyGraphScene;
import org.netbeans.modules.java.graph.GraphNodeImplementation;
import org.netbeans.modules.java.graph.GraphNodeVisitor;

/**
 *
 * @author mkleint
 */
public class GraphConstructor implements GraphNodeVisitor<MavenDependencyNode> {
    private final MavenProject proj;
    
    private MavenDependencyNode root;
    private final Stack<MavenDependencyNode> path;
//    private Stack<ArtifactGraphNode> graphPath;
    final Map<String, GraphNode<MavenDependencyNode>> cache;
    private final List<GraphEdge<MavenDependencyNode>> edges;

    public GraphConstructor(MavenProject proj) {
        this.proj = proj;
        path = new Stack<>();
//        graphPath = new Stack<ArtifactGraphNode>();
        cache = new HashMap<>();
        edges = new ArrayList<>();
    }

    @Override public boolean visit(MavenDependencyNode node) {
        if (root == null) {
            root = node;
        }
        GraphNode<MavenDependencyNode> grNode;
        boolean primary = false;
        grNode = cache.get(node.getDependencyConflictId());
        if (node.getState() == DependencyNode.INCLUDED) {
            if (grNode == null) {
                grNode = new GraphNode(node);
                cache.put(node.getDependencyConflictId(), grNode);
            } else {
                grNode.setImpl(node);
            }
            grNode.setPrimaryLevel(path.size());
            primary = true;
        } else {
            if (grNode == null) {
                grNode = new GraphNode(node);
                String conflictId = node.getState() == DependencyNode.OMITTED_FOR_CONFLICT ? node.getRelatedDependencyConflictId() : node.getDependencyConflictId();
                cache.put(conflictId, grNode);
            }
            grNode.addDuplicateOrConflict(node);
        }

        if (!path.empty()) {
            GraphNodeImplementation parent = path.peek();
            GraphEdge ed = new GraphEdge(parent, node);
            ed.setPrimaryPath(primary);
            edges.add(ed);
        }

        if (node != root && grNode.getImpl() != null) {
            grNode.setManagedState(obtainManagedState(grNode.getImpl()));
        }

        path.push(node);
//        graphPath.push(grNode);

        return true;
    }
    
    public void updateScene(final DependencyGraphScene scene) {
        assert SwingUtilities.isEventDispatchThread();
        //add all nodes and edges now
        GraphNode rootNode = cache.get(root.getDependencyConflictId());
        //root needs to go first..
        scene.addNode(rootNode);
        for (GraphNode nd : cache.values()) {
            if (nd != rootNode) {
                scene.addNode(nd);
            }
        }
        for (GraphEdge<MavenDependencyNode> ed : edges) {
            scene.addEdge(ed);
            GraphNode grNode = cache.get(ed.getTarget().getDependencyConflictId());
            if (grNode == null) { //FOR conflicting nodes..
                grNode = cache.get(ed.getTarget().getRelatedDependencyConflictId());
            }
            scene.setEdgeTarget(ed, grNode);
            GraphNode parentGrNode = cache.get(ed.getSource().getDependencyConflictId());
            scene.setEdgeSource(ed, parentGrNode);
        }
    }
    
    @Override public boolean endVisit(MavenDependencyNode node) {
        path.pop();
//        graphPath.pop();
        return true;
    }

    private int obtainManagedState(MavenDependencyNode dependencyNode) {        
        if (proj == null) {
            return GraphNode.UNMANAGED;
        }

        DependencyManagement dm = proj.getDependencyManagement();
        if (dm == null) {
            return GraphNode.UNMANAGED;
        }

        @SuppressWarnings("unchecked")
        List<Dependency> deps = dm.getDependencies();
        if (deps == null) {
            return GraphNode.UNMANAGED;
        }

        Artifact artifact = dependencyNode.getArtifact();
        String id = artifact.getArtifactId();
        String groupId = artifact.getGroupId();
        String version = artifact.getVersion();

        for (Dependency dep : deps) {
            if (id.equals(dep.getArtifactId()) && groupId.equals(dep.getGroupId())) {
                if (!version.equals(dep.getVersion())) {
                    return GraphNode.OVERRIDES_MANAGED;
                } else {
                    return GraphNode.MANAGED;
                }
            }
        }

        return GraphNode.UNMANAGED;
    }
}
