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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.generic.project.GenericProject;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 * Provides the "Files" category of the project customizer, toggling
 * whether the project view applies the IDE's global "ignored files"
 * filter and defining the project's own additional pattern.
 */
@Messages({
    "LBL_Files=Files",
    "MSG_InvalidPattern=The additional ignored files pattern is not a valid regular expression:",
})
public final class GpFilesCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CATEGORY_FILES = "category-files"; // NOI18N

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType=GenericProject.PROJECT_KEY,
            position=200
    )
    public static GpFilesCategoryProvider createFilesCategory() {
        return new GpFilesCategoryProvider();
    }

    private GpFilesCategoryProvider() {
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(CATEGORY_FILES, Bundle.LBL_Files(), null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        Project prj = context.lookup(Project.class);
        GenericProject gp = prj instanceof GenericProject g ? g : null;
        GpFilesPanel panel = new GpFilesPanel();
        panel.setUseGlobalFilter(gp != null && GenericProject.isGlobalFileFilterEnabled(gp));
        String pattern = gp != null ? GenericProject.getFilesFilterPattern(gp) : null;
        panel.setPatternText(pattern != null ? pattern : "");
        if (gp != null) {
            // invoked on OK, off the AWT thread, in ProjectManager write access
            category.setStoreListener(evt -> {
                String text = panel.getPatternText().strip();
                if (!text.isEmpty()) {
                    try {
                        Pattern.compile(text);
                    } catch (PatternSyntaxException ex) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                Bundle.MSG_InvalidPattern() + "\n" + ex.getMessage(),
                                NotifyDescriptor.ERROR_MESSAGE));
                        return;
                    }
                }
                GenericProject.setGlobalFileFilterEnabled(gp, panel.isUseGlobalFilter());
                GenericProject.setFilesFilterPattern(gp, text.isEmpty() ? null : text);
            });
        }
        return panel;
    }

}
