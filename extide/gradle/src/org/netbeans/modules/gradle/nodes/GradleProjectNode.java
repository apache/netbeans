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

package org.netbeans.modules.gradle.nodes;

import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import static org.netbeans.modules.gradle.nodes.Bundle.*;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleProjectNode extends AbstractNode {

    private final NbGradleProjectImpl project;
    private final ProjectInformation info;

    public GradleProjectNode(Lookup lookup, NbGradleProjectImpl proj) {
        super(NodeFactorySupport.createCompositeChildren(proj, "Projects/" + NbGradleProject.GRADLE_PROJECT_TYPE + "/Nodes"), lookup); //NOI18N
        this.project = proj;
        info = ProjectUtils.getInformation(project);
        info.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                String prop = event.getPropertyName();
                if (null != prop) {
                    switch (prop) {
                        case ProjectInformation.PROP_NAME:
                            fireNameChange(null, null);
                            break;
                        case ProjectInformation.PROP_DISPLAY_NAME:
                            fireDisplayNameChange(null, getDisplayName());
                            break;
                        case ProjectInformation.PROP_ICON:
                            fireIconChange();
                            fireOpenedIconChange();
                            break;
                    }
                }
            }
        });
        ProjectProblemsProvider problems = proj.getLookup().lookup(ProjectProblemsProvider.class);
        if (problems != null) {
            problems.addPropertyChangeListener((PropertyChangeEvent evt) -> {
                if (ProjectProblemsProvider.PROP_PROBLEMS.equals(evt.getPropertyName())) {
                    SwingUtilities.invokeLater(() -> {
                        fireNameChange(null, getName());
                        fireDisplayNameChange(null, getDisplayName());
                        fireShortDescriptionChange(null, getShortDescription());
                    });

                }
            });
        }

    }

    public @Override
    String getName() {
        return info.getName();
    }

    @Override
    public String getDisplayName() {
        return info.getDisplayName();
    }

    @Override
    public Image getIcon(int param) {
        return ImageUtilities.icon2Image(info.getIcon());
    }

    @Override
    public Image getOpenedIcon(int param) {
        return ImageUtilities.icon2Image(info.getIcon());
    }

    @Override
    public Action[] getActions(boolean param) {
        return CommonProjectActions.forType(NbGradleProject.GRADLE_PROJECT_TYPE);
    }

    @NbBundle.Messages({
        "TXT_FailedProjectLoadingDesc=This project could not be loaded by the NetBeans Connector. "
        + "That usually means something is wrong with your gradle files, or dependencies are missing. ",
        "LBL_DefaultDescription=A Gradle Project",
        "DESC_Project1=Location:",
        "DESC_Project2=GroupId:",
        "DESC_Project3=ArtifactId:",
        "DESC_Project4=Version:",
        "DESC_Project6=Description:",
        "DESC_Project7=Problems:"
    })
    @Override
    public String getShortDescription() {
        StringBuilder buf = new StringBuilder();
        String desc;
        GradleBaseProject gp = project.getGradleProject().getBaseProject();
        //TODO escape the short description
        desc = gp.getDescription();
        if (desc == null) {
            desc = LBL_DefaultDescription();
        }
        buf.append("<html><i>").append(DESC_Project1()).append("</i><b> ").append(FileUtil.getFileDisplayName(project.getProjectDirectory())).append("</b><br><i>"); //NOI18N
        buf.append(DESC_Project2()).append("</i><b> ").append(gp.getGroup()).append("</b><br><i>");//NOI18N
        buf.append(DESC_Project3()).append("</i><b> ").append(gp.getName()).append("</b><br><i>");//NOI18N
        buf.append(DESC_Project4()).append("</i><b> ").append(gp.getVersion()).append("</b><br><i>");//NOI18N
        Collection<? extends ProjectProblemsProvider.ProjectProblem> problems = project.getLookup().lookup(ProjectProblemsProvider.class).getProblems();
        if (!problems.isEmpty()) {
            buf.append("<br><b>").append(DESC_Project7()).append("</b><br><ul>");//NOI18N
            for (ProjectProblemsProvider.ProjectProblem elem : problems) {
                buf.append("<li>").append(elem.getDisplayName()).append("</li>");//NOI18N
            }
            buf.append("</ul>");//NOI18N
        }
        // it seems that with ending </html> tag the icon descriptions are not added.
//        buf.append("</html>");//NOI18N
        return buf.toString();
    }

}
