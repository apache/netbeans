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

package org.netbeans.modules.gradle.customizer;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import static org.netbeans.modules.gradle.customizer.Bundle.*;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import org.netbeans.api.project.ProjectUtils;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(
        service = {CustomizerProvider.class, CustomizerProvider2.class},
        projectType = NbGradleProject.GRADLE_PROJECT_TYPE
)
public class GradleCustomizerProvider implements CustomizerProvider2 {

    public static final HelpCtx HELP_CTX = new HelpCtx("gradle_settings");

    private final Project project;

    public GradleCustomizerProvider(Project project) {
        this.project = project;
    }

    @NbBundle.Messages({
        "# {0} - project display name",
        "TIT_Project_Properties=Project Properties - {0}",
        "TXT_Unloadable=Project is unloadable, the project information is unreliable. Show Project Properties dialog?",
        "TIT_Unloadable=Project Unloadable"
    })
    @Override
    public void showCustomizer(final String preselectedCategory, final String preselectedSubCategory) {
        if (NbGradleProject.get(project).isUnloadable()) {
            NotifyDescriptor.Confirmation nd = new NotifyDescriptor.Confirmation(TXT_Unloadable(), TIT_Unloadable());
            nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.NO_OPTION) {
                return;
            }
        }
//        try {
        Mutex.EVENT.readAccess(new Runnable() {

            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                Lookup context = Lookups.singleton(project);
                Dialog dialog = ProjectCustomizer.createCustomizerDialog("Projects/" + NbGradleProject.GRADLE_PROJECT_TYPE + "/Customizer", //NOI18N
                        context,
                        preselectedCategory,
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                //noop
                            }
                        }, null, HELP_CTX);
                dialog.setTitle(TIT_Project_Properties(ProjectUtils.getInformation(project).getDisplayName()));
                dialog.setModal(true);
                dialog.setVisible(true);
            }
        });
//        } catch (Exception ex) {
//            Logger.getLogger(GradleCustomizerProvider.class.getName()).log(Level.SEVERE, "Cannot show project customizer", ex);
//        }
    }

    @Override
    public void showCustomizer() {
        showCustomizer(null, null);
    }

    @NbBundle.Messages("category.ProjectInfo=Project Information")
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType=NbGradleProject.GRADLE_PROJECT_TYPE,
            category = "info",
            categoryLabel = "#category.ProjectInfo",
            position=200)
    public static ProjectCustomizer.CompositeCategoryProvider projectInfoProvider() {
        return new ProjectCustomizer.CompositeCategoryProvider() {
            @Override
            public ProjectCustomizer.Category createCategory(Lookup context) {
                return null;
            }

            @Override
            public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
                Project project = context.lookup(Project.class);
                return new ProjectInfoPanel(project);
            }
        };
    }

    @NbBundle.Messages("category.BuildActions=Build Actions")
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType=NbGradleProject.GRADLE_PROJECT_TYPE,
            category = "build/actions",
            categoryLabel = "#category.BuildActions",
            position=310)
    public static ProjectCustomizer.CompositeCategoryProvider buildActionCustomizerProvider() {
        return new ProjectCustomizer.CompositeCategoryProvider() {
            @Override
            public ProjectCustomizer.Category createCategory(Lookup context) {
                return null;
            }

            @Override
            public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
                Project project = context.lookup(Project.class);
                BuildActionsCustomizer customizer = new BuildActionsCustomizer(project);
                category.setStoreListener(customizer.getSaveListener());
                return customizer;
            }
        };
    }
}
