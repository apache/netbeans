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
package org.netbeans.modules.java.j2semodule.ui;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.project.ui.LibrariesNode;
import org.netbeans.modules.java.api.common.project.ui.MultiModuleNodeFactory;
import org.netbeans.modules.java.api.common.project.ui.ProjectUISupport;
import org.netbeans.modules.java.j2semodule.J2SEModularProject;
import org.netbeans.modules.java.j2semodule.ui.customizer.CustomizerLibraries;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class ModuleNodeFactory implements NodeFactory {

    public ModuleNodeFactory() {
    }

    @Override
    public NodeList<?> createNodes(@NonNull final Project project) {
        Parameters.notNull("project", project);
        final J2SEModularProject modularProject = project.getLookup().lookup(J2SEModularProject.class);
        if (modularProject == null) {
            throw new IllegalStateException(
                    String.format("Not a J2SEModularProject: %s (%s)",          //NOI18N
                    project,
                    FileUtil.getFileDisplayName(project.getProjectDirectory())));
        }
        return getDelegate(modularProject).createNodes(project);
    }

    @NonNull
    private NodeFactory getDelegate(@NonNull final J2SEModularProject mp) {
        final NodeFactory res = MultiModuleNodeFactory.Builder.create(mp.getUpdateHelper(), mp.evaluator(), mp.getReferenceHelper())
                .setSources(mp.getModuleRoots(),mp.getSourceRoots())
                .setTests(mp.getTestModuleRoots(), mp.getTestSourceRoots())
                .addLibrariesNodes()
                .addLibrariesNodeActions(
                    LibrariesNode.createAddProjectAction(mp, mp.getSourceRoots()),
                    LibrariesNode.createAddLibraryAction(mp.getReferenceHelper(), mp.getSourceRoots(), null),
                    LibrariesNode.createAddFolderAction(mp.getAntProjectHelper(), mp.getSourceRoots()),
                    null,
                    ProjectUISupport.createPreselectPropertiesAction(mp, "Libraries", CustomizerLibraries.COMPILE) // NOI18N
                )
                .addTestLibrariesNodeActions(
//                                    LibrariesNode.createAddProjectAction(project, project.getTestSourceRoots()),
//                                    LibrariesNode.createAddLibraryAction(project.getReferenceHelper(), project.getTestSourceRoots(), null),
//                                    LibrariesNode.createAddFolderAction(project.getAntProjectHelper(), project.getTestSourceRoots()),
//                                    null,
                    ProjectUISupport.createPreselectPropertiesAction(mp, "Libraries", CustomizerLibraries.COMPILE_TESTS)    //NOI18N
                )
                .build();
        return res;
    }
}
