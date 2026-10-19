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

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.generic.project.GenericProject;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 * Provides the "General" category of the project customizer, showing
 * the project name and location (both read-only).
 *
 * <p>Additional categories can be added to the same dialog by registering
 * further {@link ProjectCustomizer.CompositeCategoryProvider} instances
 * for {@link GenericProject#PROJECT_KEY}.
 */
@Messages({
    "LBL_General=General",
    "LBL_Name=Name:",
    "LBL_Location=Location:",
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

        JLabel nameLabel = new JLabel(Bundle.LBL_Name());
        JLabel locationLabel = new JLabel(Bundle.LBL_Location());
        JTextField nameField = new JTextField(info != null ? info.getName() : "");
        JTextField locationField = new JTextField(prj != null ? prj.getProjectDirectory().getPath() : "");
        nameField.setEditable(false);
        locationField.setEditable(false);

        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                    .addComponent(nameLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(locationLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(nameField, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(locationField, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameField))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(locationLabel)
                    .addComponent(locationField))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        return panel;
    }

}