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
