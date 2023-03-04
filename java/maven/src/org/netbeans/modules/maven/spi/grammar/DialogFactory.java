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
            for (Map.Entry<Artifact, List<DependencyNode>> entry : dependencyExcludes.entrySet()) {
                Artifact exclude = entry.getKey();
                List<DependencyNode> directs = entry.getValue();
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
