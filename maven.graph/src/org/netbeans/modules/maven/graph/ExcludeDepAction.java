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
