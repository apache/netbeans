/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package org.jetbrains.kotlin.projectsextensions.j2se.lookup;

import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.projectsextensions.KotlinPrivilegedTemplates;
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper;
import org.jetbrains.kotlin.projectsextensions.j2se.J2SEProjectOpenedHook;
import org.jetbrains.kotlin.projectsextensions.j2se.classpath.J2SEExtendedClassPathProvider;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Alexander.Baratynski
 */
public class J2SEProjectLookupProviderExtension implements LookupProvider {

    @Override
    public Lookup createAdditionalLookup(Lookup lkp) {
        final Project j2seProject = lkp.lookup(Project.class);

        ProjectUtils.checkKtHome();
        
        return Lookups.fixed(
                new KotlinPrivilegedTemplates(),
                new J2SEExtendedClassPathProvider(j2seProject), 
                new J2SEProjectOpenedHook(j2seProject)
        );
    }

}
