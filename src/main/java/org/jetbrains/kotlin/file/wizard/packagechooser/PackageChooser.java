/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.file.wizard.packagechooser;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.WizardDescriptor;

/**
 *
 * @author Alexander.Baratynski
 */
public class PackageChooser {
    
    public static String pack = null;
    
    public static TargetChooserPanel createPackageChooser(Project project, SourceGroup[] folders, 
        WizardDescriptor.Panel<WizardDescriptor> bottomPanel, String type) {
        if (folders.length == 0) {
            throw new IllegalArgumentException("No folders selected");
        }
        return new TargetChooserPanel(project, folders, bottomPanel, Type.FILE, false, type);
    }
    
}
