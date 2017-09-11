/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
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
    private final DependencyGraphScene scene;
    private final MavenProject proj;
    
    private MavenDependencyNode root;
    private final Stack<MavenDependencyNode> path;
//    private Stack<ArtifactGraphNode> graphPath;
    final Map<String, GraphNode<MavenDependencyNode>> cache;
    private final List<GraphEdge<MavenDependencyNode>> edges;

    public GraphConstructor(DependencyGraphScene scene, MavenProject proj) {
        this.scene = scene;
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

    @Override public boolean endVisit(MavenDependencyNode node) {
        path.pop();
//        graphPath.pop();
        if (root == node) {
            //add all nodes and edges now
            GraphNode rootNode = cache.get(node.getDependencyConflictId());
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
