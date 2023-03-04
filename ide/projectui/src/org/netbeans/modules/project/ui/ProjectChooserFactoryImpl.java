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

package org.netbeans.modules.project.ui;

import java.io.File;
import javax.swing.JFileChooser;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.netbeans.api.project.SourceGroup;
import org.openide.WizardDescriptor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory to be implemented bu the ui implementation
 * @author Petr Hrebejk
 */
@ServiceProvider(service=ProjectChooserFactory.class)
public class ProjectChooserFactoryImpl implements ProjectChooserFactory {

    public ProjectChooserFactoryImpl() {}
    
    public @Override JFileChooser createProjectChooser() {
        return ProjectChooserAccessory.createProjectChooser( false );
    }

    public @Override WizardDescriptor.Panel<WizardDescriptor> createSimpleTargetChooser(@NullAllowed Project project, @NonNull SourceGroup[] folders,
            WizardDescriptor.Panel<WizardDescriptor> bottomPanel, boolean freeFileExtension) {
        return new SimpleTargetChooserPanel(project, folders, bottomPanel, false, freeFileExtension);
    }

    public @Override File getProjectsFolder() {
        return OpenProjectListSettings.getInstance().getProjectsFolder(true);
    }

    public @Override void setProjectsFolder(File file) {
        OpenProjectListSettings.getInstance().setProjectsFolder (file);
    }

}
