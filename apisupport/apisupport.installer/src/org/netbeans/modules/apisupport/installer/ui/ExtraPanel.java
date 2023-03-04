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

package org.netbeans.modules.apisupport.installer.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Exceptions;


@ProjectCustomizer.CompositeCategoryProvider.Registration(
    projectType="org-netbeans-modules-apisupport-project-suite",
    position=1000
)
public class ExtraPanel implements ProjectCustomizer.CompositeCategoryProvider {

    public @Override ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(
                "Installer",
                NbBundle.getMessage(ExtraPanel.class, "LBL_InstallerPanel"),
                null,
                (ProjectCustomizer.Category[])null);
    }

    public @Override JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        SuiteInstallerProjectProperties installerProjectProperties = new SuiteInstallerProjectProperties(context.lookup(Project.class));
        // use OkListener to create new configuration first
        //category.setOkButtonListener(new OkButtonListener(installerProjectProperties, context.lookup(Project.class)));
        category.setStoreListener(new SavePropsListener(installerProjectProperties));
        return new InstallerPanel(installerProjectProperties);
    }

    private static class SavePropsListener implements ActionListener {

        private SuiteInstallerProjectProperties installerProps;

        public SavePropsListener(SuiteInstallerProjectProperties props) {
            installerProps = props;
        }

        public @Override void actionPerformed(ActionEvent e) {
            try {
                installerProps.store();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }   
        }
}}
