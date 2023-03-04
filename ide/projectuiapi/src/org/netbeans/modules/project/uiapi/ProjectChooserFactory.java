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

package org.netbeans.modules.project.uiapi;

import java.io.File;
import javax.swing.JFileChooser;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.WizardDescriptor;

/**
 * Factory to be implemented by the UI implementation
 * @author Petr Hrebejk
 */
public interface ProjectChooserFactory {

    public static final String WIZARD_KEY_PROJECT = "project"; // NOI18N

    public static final String WIZARD_KEY_TARGET_FOLDER = "targetFolder"; // NOI18N
    
    public static final String WIZARD_KEY_TARGET_NAME = "targetName"; // NOI18N
    
    public static final String WIZARD_KEY_TEMPLATE = "targetTemplate"; // NOI18N
    
    public File getProjectsFolder ();

    public void setProjectsFolder (File file);

    public JFileChooser createProjectChooser(); 
    
    public WizardDescriptor.Panel<WizardDescriptor> createSimpleTargetChooser(@NullAllowed Project project, @NonNull SourceGroup[] folders,
            WizardDescriptor.Panel<WizardDescriptor> bottomPanel, boolean freeFileExtension);
            
}
