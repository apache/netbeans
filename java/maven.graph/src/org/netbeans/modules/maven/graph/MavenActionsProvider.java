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

import org.netbeans.modules.java.graph.GraphNode;
import org.netbeans.modules.java.graph.DependencyGraphScene;
import java.util.Set;
import javax.swing.Action;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.CommonArtifactActions;
import org.netbeans.modules.maven.api.NbMavenProject;
import static org.netbeans.modules.maven.graph.Bundle.ACT_Show_Graph;
import org.netbeans.modules.maven.indexer.api.ui.ArtifactViewer;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.openide.util.NbBundle;

/**
 * @author tomas
 */
public class MavenActionsProvider implements DependencyGraphScene.ActionsProvider<MavenDependencyNode> {

    private final Project project;
    private final POMModel model;
    private final DependencyGraphTopComponent ownerTC;
    
    public MavenActionsProvider(DependencyGraphTopComponent ownerTC, Project project, POMModel model) {
        this.project = project;
        this.model = model;
        this.ownerTC = ownerTC;
    }

    @Override
    public Action createExcludeDepAction(DependencyGraphScene scene, GraphNode<MavenDependencyNode> rootNode, GraphNode<MavenDependencyNode> node) {
        return model != null && node.getPrimaryLevel() > 1 ? new ExcludeDepAction(ownerTC, scene, rootNode, node, model, project) : null;
    }

    @Override
    public Action createFixVersionConflictAction(DependencyGraphScene scene, GraphNode<MavenDependencyNode> rootNode, GraphNode<MavenDependencyNode> node) {
        return model != null && isFixCandidate(node) ? new FixVersionConflictAction(ownerTC, scene, rootNode, node, model, project) : null;
    }

    @Override
    @NbBundle.Messages({
        "ACT_Show_Graph=Show Dependency Graph", 
    })
    public Action createShowGraphAction(GraphNode<MavenDependencyNode> node) {
        Action a = CommonArtifactActions.createViewArtifactDetails(node.getImpl().getArtifact(), project.getLookup().lookup(NbMavenProject.class).getMavenProject().getRemoteArtifactRepositories());
        a.putValue("PANEL_HINT", ArtifactViewer.HINT_GRAPH); //NOI18N
        a.putValue(Action.NAME, ACT_Show_Graph());
        return a;
    }

    static boolean isFixCandidate (GraphNode<MavenDependencyNode> node) {
        Set<MavenDependencyNode> conf = node.getDuplicatesOrConflicts();
        for (MavenDependencyNode dn : conf) {
            if (dn.getState() == DependencyNode.OMITTED_FOR_CONFLICT) {
                if (node.getImpl().compareVersions(dn) < 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
