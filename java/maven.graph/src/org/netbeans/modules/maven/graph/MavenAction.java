/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.maven.graph;

import org.netbeans.modules.java.graph.GraphEdge;
import org.netbeans.modules.java.graph.GraphNode;
import org.netbeans.modules.java.graph.DependencyGraphScene;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Profile;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.Exclusion;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.java.graph.GraphNodeImplementation;

/**
 *
 * @author tomas
 */
public abstract class MavenAction extends AbstractAction {
    
    private static final RequestProcessor RP = new RequestProcessor(DependencyGraphScene.class);
    
    protected final GraphNode<MavenDependencyNode> node;
    protected final POMModel model;
    protected final DependencyGraphScene<MavenDependencyNode> scene;
    protected final GraphNode<MavenDependencyNode> rootNode;
    protected final Artifact nodeArtif;
    private final Project nbProject;
    private final DependencyGraphTopComponent ownerTC;
    
    protected MavenAction(DependencyGraphTopComponent ownerTC, DependencyGraphScene scene, GraphNode<MavenDependencyNode> rootNode, GraphNode<MavenDependencyNode> node, POMModel model, Project nbProject) {
        this.node = node;
        this.nodeArtif = node.getImpl().getArtifact();
        this.model = model;
        this.scene = scene;
        this.rootNode = rootNode;
        this.nbProject = nbProject;
        this.ownerTC = ownerTC;
    }

    protected void save() {
        /** Saves fix changes to the pom file, posted to RequestProcessor */
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    ownerTC.saveChanges(model);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    //TODO error reporting on wrong model save
                }
            }
        });
    }
        
//    @Override public void run() {
//    }
    
    /** Note, must be called inside model transaction */
    protected void excludeDepFromModel(MavenDependencyNode node, Set<Artifact> exclTargets) {
        assert model.isIntransaction() : "Must be called inside transaction"; //NOI18N
        Artifact nodeArtif = node.getArtifact();
        for (Artifact eTarget : exclTargets) {
            Dependency dep = model.getProject().findDependencyById(eTarget.getGroupId(), eTarget.getArtifactId(), null);
            if (dep == null) {
                // now check the active profiles for the dependency..
                List<String> profileNames = new ArrayList<String>();
                NbMavenProject nbMavproject = nbProject.getLookup().lookup(NbMavenProject.class);
                for (Profile prof : nbMavproject.getMavenProject().getActiveProfiles()) {
                    profileNames.add(prof.getId());
                }
                for (String profileId : profileNames) {
                    org.netbeans.modules.maven.model.pom.Profile modProf = model.getProject().findProfileById(profileId);
                    if (modProf != null) {
                        dep = modProf.findDependencyById(eTarget.getGroupId(), eTarget.getArtifactId(), null);
                        if (dep != null) {
                            break;
                        }
                    }
                }
            }
            if (dep == null) {
                // must create dependency if not found locally, so that
                // there is a place where to add dep exclusion
                dep = model.getFactory().createDependency();
                dep.setGroupId(eTarget.getGroupId());
                dep.setArtifactId(eTarget.getArtifactId());
                dep.setType(eTarget.getType());
                dep.setVersion(eTarget.getVersion());
                model.getProject().addDependency(dep);
            }
            Exclusion ex = dep.findExclusionById(nodeArtif.getGroupId(), nodeArtif.getArtifactId());
            if (ex == null) {
                ex = model.getFactory().createExclusion();
                ex.setGroupId(nodeArtif.getGroupId());
                ex.setArtifactId(nodeArtif.getArtifactId());
                dep.addExclusion(ex);
            }
        }
    }

    protected void updateGraphAfterExclusion(GraphNode<MavenDependencyNode> node, Set<Artifact> exclTargets, Set<MavenDependencyNode> exclParents) {
        boolean shouldValidate = false;
        Set<MavenDependencyNode> toExclude = new HashSet<MavenDependencyNode>();
        MavenDependencyNode curDn;
        for (MavenDependencyNode dn : node.getDuplicatesOrConflicts()) {
            if (dn.getState() == DependencyNode.OMITTED_FOR_CONFLICT) {
                curDn = dn.getParent();
                while (curDn != null) {
                    if (exclTargets.contains(curDn.getArtifact())) {
                        toExclude.add(dn);
                        break;
                    }
                    curDn = curDn.getParent();
                }
            }
        }
        List<GraphEdge> edges2Exclude = new ArrayList<>();
        Collection<GraphEdge<MavenDependencyNode>> incoming = scene.findNodeEdges(node, false, true);
        GraphNode<MavenDependencyNode> sourceNode = null;
        boolean primaryExcluded = false;
        for (GraphEdge age : incoming) {
            sourceNode = scene.getEdgeSource(age);
            if (sourceNode != null) {
                for (MavenDependencyNode dn : exclParents) {
                    if (sourceNode.getImpl().equals(dn)) {
                        primaryExcluded = true;
                    }
                    if (sourceNode.represents(dn)) {
                        edges2Exclude.add(age);
                        break;
                    }
                }
            }
        }
        // note, must be called before node removing edges to work correctly
        for(MavenDependencyNode mdn: toExclude) {
            node.removeDuplicateOrConflict(mdn);
        }
        for (GraphEdge<MavenDependencyNode> age : edges2Exclude) {
            scene.removeEdge(age);
            age.getSource().removeChild(age.getTarget());
            shouldValidate = true;
        }
        incoming = scene.findNodeEdges(node, false, true);
        if (primaryExcluded) {
            ArtifactVersion newVersion = findNewest(node, true);
            node.getImpl().getArtifact().setVersion(newVersion.toString());
            for (GraphEdge age : incoming) {
                scene.notifyModelChanged(age);
            }
        }
        if (incoming.isEmpty()) {
            removeSubGraph(node);
            shouldValidate = true;
        } else {
            scene.notifyModelChanged(node);
        }
        if (shouldValidate) {
            scene.validate();
        }
    }

    private void removeSubGraph(GraphNode node) {
        if (!scene.isNode(node)) {
            // already visited and removed
            return;
        }
        Collection<GraphEdge> incoming = scene.findNodeEdges(node, false, true);
        if (!incoming.isEmpty()) {
            return;
        }
        Collection<GraphEdge> outgoing = scene.findNodeEdges(node, true, false);
        List<GraphNode> children = new ArrayList<>();
        // remove edges to children
        for (GraphEdge<MavenDependencyNode> age : outgoing) {
            MavenDependencyNode dn = age.getTarget();
            GraphNode<MavenDependencyNode>  childNode = scene.getGraphNodeRepresentant(dn);
            if (childNode == null) {
                continue;
            }
            children.add(childNode);
            scene.removeEdge(age);
            age.getSource().removeChild(dn);
            childNode.removeDuplicateOrConflict(dn);
        }
        // recurse to children
        for (GraphNode age : children) {
            removeSubGraph(age);
        }
        // remove itself finally
        scene.removeNode(node);
    }

    static ArtifactVersion findNewest(GraphNode<MavenDependencyNode> node, boolean all) {
        Set<MavenDependencyNode> conf = node.getDuplicatesOrConflicts();
        ArtifactVersion result = new DefaultArtifactVersion(node.getImpl().getArtifact().getVersion());
        ArtifactVersion curV = null;
        for (MavenDependencyNode dn : conf) {
            if (all || dn.getState() == DependencyNode.OMITTED_FOR_CONFLICT) {
                curV = new DefaultArtifactVersion(dn.getArtifact().getVersion());
                if (result.compareTo(curV) < 0) {
                    result = curV;
                }
            }
        }
        return result;
    }    
}
