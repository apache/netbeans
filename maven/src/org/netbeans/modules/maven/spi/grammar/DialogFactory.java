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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.spi.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.dependencies.ExcludeDependencyPanel;
import org.netbeans.modules.maven.nodes.AddDependencyPanel;
import static org.netbeans.modules.maven.spi.grammar.Bundle.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle.Messages;

/**
 * A factory class that creates dialogs to add/edit stuff,
 * eg. dependency, primarily for use by the maven.grammar module
 * in code generators
 * @author mkleint
 */
public final class DialogFactory {

    private DialogFactory() {}

    /**
     * 
     * @param prj
     * @return null, if dialog was cancelled, or string array
     * [0] - groupId
     * [1] - artifactId
     * [2] - version
     * [3] - scope
     * [4] - type
     * [5] - classifier
     *
     *
     */
    public static String[] showDependencyDialog(Project prj, boolean showDepMan) {
        return AddDependencyPanel.show(prj, showDepMan, "compile");
    }

    @Messages("TIT_Exclude=Add Dependency Excludes")
    public static Map<Artifact, List<Artifact>> showDependencyExcludeDialog(Project prj) {
        NbMavenProject nbproj = prj.getLookup().lookup(NbMavenProject.class);
        final ExcludeDependencyPanel pnl = new ExcludeDependencyPanel(nbproj.getMavenProject());
        DialogDescriptor dd = new DialogDescriptor(pnl, TIT_Exclude());
        pnl.setStatusDisplayer(dd.createNotificationLineSupport());
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (ret == DialogDescriptor.OK_OPTION) {
            Map<Artifact, List<DependencyNode>> dependencyExcludes = pnl.getDependencyExcludes();
            Map<Artifact, List<Artifact>> toRet = new HashMap<Artifact, List<Artifact>>();
            for (Artifact exclude : dependencyExcludes.keySet()) {
                List<DependencyNode> directs = dependencyExcludes.get(exclude);
                List<Artifact> dirArts = new ArrayList<Artifact>();
                for (DependencyNode nd : directs) {
                    dirArts.add(nd.getArtifact());
                }
                if (dirArts.size() > 0) {
                    toRet.put(exclude, dirArts);
                }
            }
            return toRet;
        }
        return null;
    }
}
