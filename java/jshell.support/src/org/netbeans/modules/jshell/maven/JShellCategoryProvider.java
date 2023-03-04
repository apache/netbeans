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
package org.netbeans.modules.jshell.maven;

import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@CompositeCategoryProvider.Registration(
    projectType = "org-netbeans-modules-maven",
    // after maven run == 300
    position = 310
)
@NbBundle.Messages({
    "CAT_RunJShell=Java Shell"
})
public class JShellCategoryProvider implements CompositeCategoryProvider {
    public static final String CATEGORY = "JSHELL";
    public static final String CATEGORY_FULL = ModelHandle2.PANEL_RUN + "/" + CATEGORY;
    
    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        // suppress panel for JEE projects ... generally anything other than JAR packaging
        // type:
        Project p = context.lookup(Project.class);
        if (p == null) {
            return null;
        }
        NbMavenProject mvnProject = p.getLookup().lookup(NbMavenProject.class);
        if (mvnProject == null || 
            !NbMavenProject.TYPE_JAR.equals(mvnProject.getPackagingType())) {
            return null;
        }
        return ProjectCustomizer.Category.create(
                CATEGORY,
                Bundle.CAT_RunJShell(),
                null
        );
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        Project prj = context.lookup(Project.class);
        ModelHandle2 handle = context.lookup(ModelHandle2.class);
        if (prj == null || handle == null) {
            return null;
        }
        MavenRunOptions opts = new MavenRunOptions(prj, category, handle);
        return opts;
    }
}
