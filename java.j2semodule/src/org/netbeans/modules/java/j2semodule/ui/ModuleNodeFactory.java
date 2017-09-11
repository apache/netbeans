/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
        final NodeFactory res = MultiModuleNodeFactory.Builder.create()
                .setSources(mp.getModuleRoots(),mp.getSourceRoots())
                .setTests(mp.getTestModuleRoots(), mp.getTestSourceRoots())
                .addLibrariesNodes(mp.getUpdateHelper(), mp.evaluator(), mp.getReferenceHelper())
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
