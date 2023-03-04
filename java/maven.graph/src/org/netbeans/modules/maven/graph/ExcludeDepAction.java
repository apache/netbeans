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
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import org.apache.maven.artifact.Artifact;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.maven.graph.Bundle.ACT_ExcludeDep;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author tomas
 */
public class ExcludeDepAction extends MavenAction {

    @NbBundle.Messages({
        "ACT_ExcludeDep=Exclude",
        "TIP_ExcludeDep=Adds dependency exclusion of this artifact to relevant direct dependencies"
    })
    ExcludeDepAction(DependencyGraphTopComponent ownerTC, DependencyGraphScene scene, GraphNode<MavenDependencyNode> rootNode, GraphNode<MavenDependencyNode> node, POMModel model, Project project) {
        super(ownerTC, scene, rootNode, node, model, project);
        putValue(NAME, ACT_ExcludeDep());
        putValue(SHORT_DESCRIPTION, Bundle.TIP_ExcludeDep());
    }

    @Override public void actionPerformed(ActionEvent e) {
        FixVersionConflictPanel.ExclusionTargets et =
                new FixVersionConflictPanel.ExclusionTargets(node, findNewest(node, true));
        Set<Artifact> exclTargets = et.getAll();

        if (!model.startTransaction()) {
            return;
        }
        try {
            excludeDepFromModel(node.getImpl(), exclTargets);
        } finally {
            try {
                model.endTransaction();
            } catch (IllegalStateException ex) {
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(DependencyGraphScene.class, "ERR_UpdateModel", Exceptions.findLocalizedMessage(ex)), 
                        StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
                return;
            }
        }

        HashSet<MavenDependencyNode> conflictParents = new HashSet<MavenDependencyNode>();
        for (Artifact artif : exclTargets) {
            conflictParents.addAll(et.getConflictParents(artif));
        }
        updateGraphAfterExclusion(node, exclTargets, conflictParents);

        save();
    }

}
