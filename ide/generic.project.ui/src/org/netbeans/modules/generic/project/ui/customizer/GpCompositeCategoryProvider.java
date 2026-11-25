/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.generic.project.ui.customizer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.generic.project.GenericProject;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 * Provides the "General" category of the project customizer, showing
 * the project name and location and the marker file option, using
 * the form-based {@link GpGeneralPanel}.
 *
 * <p>Additional categories can be added to the same dialog by registering
 * further {@link ProjectCustomizer.CompositeCategoryProvider} instances
 * for {@link GenericProject#PROJECT_KEY}.
 */
@Messages({
    "LBL_General=General",
})
public final class GpCompositeCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CATEGORY_GENERAL = "category-general"; // NOI18N

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType=GenericProject.PROJECT_KEY,
            position=100
    )
    public static GpCompositeCategoryProvider createGeneralCategory() {
        return new GpCompositeCategoryProvider();
    }

    private GpCompositeCategoryProvider() {
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(CATEGORY_GENERAL, Bundle.LBL_General(), null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        Project prj = context.lookup(Project.class);
        ProjectInformation info = prj != null ? prj.getLookup().lookup(ProjectInformation.class) : null;
        FileObject projectDir = prj != null ? prj.getProjectDirectory() : null;

        GpGeneralPanel panel = new GpGeneralPanel();
        panel.setNameText(info != null ? info.getDisplayName() : "");
        panel.setLocationText(projectDir != null ? projectDir.getPath() : "");
        panel.setMarkerEnabled(projectDir != null && GenericProject.hasMarker(projectDir));
        if (prj != null && projectDir != null) {
            // invoked on OK, off the AWT thread, in ProjectManager write access
            category.setStoreListener(evt -> {
                try {
                    GenericProject.setMarkerState(projectDir, panel.isMarkerEnabled());
                    String name = panel.getNameText().strip();
                    if (!name.isEmpty()) {
                        GenericProject.setDisplayNameOverride((GenericProject) prj, name);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(GpCompositeCategoryProvider.class.getName())
                        .log(Level.WARNING, "Cannot update project settings", ex);
                }
            });
        }
        return panel;
    }

}
