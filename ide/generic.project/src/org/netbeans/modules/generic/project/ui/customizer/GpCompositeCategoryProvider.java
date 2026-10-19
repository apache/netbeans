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
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.generic.project.GenericProject;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
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
    "TT_Name=<html>"
            + "<p>The name of the project as shown in the project view.</p>"
            + "<p>Changing it does not rename the project folder. Enter the folder name to reset.</p></html>",
    "LBL_Location=Location:",
    "LBL_MarkerFile=Marker file in project directory",
    "TT_MarkerFile=<html>"
            + "<p>When unchecked, the <b>.nbgp</b> marker file is <b>removed</b> from the project directory.</p>"
            + "<p>The project becomes a <b>local</b> (private) project.</p></html>",
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

        JLabel nameLabel = new JLabel(Bundle.LBL_Name());
        JLabel locationLabel = new JLabel(Bundle.LBL_Location());
        JTextField nameField = new JTextField(info != null ? info.getDisplayName() : "");
        nameField.setToolTipText(Bundle.TT_Name());
        JTextField locationField = new JTextField(projectDir != null ? projectDir.getPath() : "");
        locationField.setEditable(false);
        JCheckBox markerBox = new JCheckBox(Bundle.LBL_MarkerFile());
        markerBox.setToolTipText(Bundle.TT_MarkerFile());
        markerBox.setSelected(projectDir != null && GenericProject.hasMarker(projectDir));

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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(markerBox))
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
                .addGap(18, 18, 18)
                .addComponent(markerBox)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        if (prj != null && projectDir != null) {
            // invoked on OK, off the AWT thread, in ProjectManager write access
            category.setStoreListener(evt -> {
                try {
                    GenericProject.setMarkerState(projectDir, markerBox.isSelected());
                    String name = nameField.getText().strip();
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