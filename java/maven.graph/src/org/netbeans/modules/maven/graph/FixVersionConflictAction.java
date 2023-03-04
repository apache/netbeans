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

import org.netbeans.modules.java.graph.GraphEdge;
import org.netbeans.modules.java.graph.GraphNode;
import org.netbeans.modules.java.graph.DependencyGraphScene;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.logging.Logger;
import static javax.swing.Action.NAME;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.ModelUtils;
import static org.netbeans.modules.maven.graph.Bundle.TIT_FixConflict;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author tomas
 */
public class FixVersionConflictAction extends MavenAction {

    FixVersionConflictAction(DependencyGraphTopComponent ownerTC, DependencyGraphScene scene, GraphNode<MavenDependencyNode> rootNode, GraphNode<MavenDependencyNode> node, POMModel model, Project project) {
        super(ownerTC, scene, rootNode, node, model, project);
        putValue(NAME, TIT_FixConflict());
    }

    @NbBundle.Messages("TIT_FixConflict=Fix Version Conflict...")
    @Override public void actionPerformed(ActionEvent e) {
        FixVersionConflictPanel fixPanel = new FixVersionConflictPanel(scene, node);
        DialogDescriptor dd = new DialogDescriptor(fixPanel, TIT_FixConflict());
        //pnl.setStatusDisplayer(dd.createNotificationLineSupport());
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (ret == DialogDescriptor.OK_OPTION) {
            FixVersionConflictPanel.FixDescription res = fixPanel.getResult();
            fixDependency(res);
            updateGraph(res);
            save();
        }
    }
    
    private void fixDependency(FixVersionConflictPanel.FixDescription fixContent) {
        if (!model.startTransaction()) {
            return;
        }
        if (model.getProject() == null) {
            Logger.getLogger(FixVersionConflictAction.class.getName()).warning("#238748 we got a graph, but the model turned invalid for some reason. if you have steps to reproduce, please reopen the issue.");
            return; //#238748 not clear under which circumstances we get a graph but invalid model
        }
        try {
            if (fixContent.isSet && fixContent.version2Set != null) {
                Dependency dep = ModelUtils.checkModelDependency(model, nodeArtif.getGroupId(), nodeArtif.getArtifactId(), true);
                dep.setVersion(fixContent.version2Set.toString());
            }
            if (fixContent.isExclude) {
                excludeDepFromModel(node.getImpl(), fixContent.exclusionTargets);
            }
        } finally {
            model.endTransaction();
        }
    }

    private void updateGraph(FixVersionConflictPanel.FixDescription fixContent) {
        if (fixContent.isSet) {
            node.getImpl().getArtifact().setVersion(fixContent.version2Set.toString());
            Collection<GraphEdge<MavenDependencyNode>> incoming = scene.findNodeEdges(node, false, true);
            for (GraphEdge age : incoming) {
                scene.notifyModelChanged(age);
            }
            scene.notifyModelChanged(node);
            // add edge representing direct dependency if not exist yet
            if (scene.findEdgesBetween(rootNode, node).isEmpty()) {
                GraphEdge ed = scene.addEdge(rootNode.getImpl(), node.getImpl());
                ed.setPrimaryPath(true);
                scene.setEdgeTarget(ed, node);
                scene.setEdgeSource(ed, rootNode);
                node.setPrimaryLevel(1);
                node.setParent(rootNode.getImpl());                
                rootNode.getImpl().addChild(node.getImpl());
                scene.validate();
            }
        }
        if (fixContent.isExclude) {
            updateGraphAfterExclusion(node, fixContent.exclusionTargets, fixContent.conflictParents);
        }
    }
}
