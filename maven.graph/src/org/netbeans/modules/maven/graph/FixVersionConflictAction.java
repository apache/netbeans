/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
