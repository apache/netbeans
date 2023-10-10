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

package org.netbeans.api.maven.archetype;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 * Utilities for creating New Project wizards based on maven-archetype-plugin.
 *
 * @author Tomas Stupka
 * @since 1.0
 */
public class ArchetypeWizards {
    
    private ArchetypeWizards() { }

    /**
     * Run a single archetype.
     * 
     * @param projDir the new project directory (must be normalized first!) (note: parent dir is actually passed to plugin, i.e. assumes that project name matches this basedir)
     * @param vi metadata for new project
     * @param arch the archetype to process
     * @param additionalProperties any additional archetype properties, or null
     * @param updateLastUsedProjectDir true to update last-used project directory for next wizard run
     * @since 1.0
     * @throws java.io.IOException
     */
    public static void createFromArchetype(File projDir, ProjectInfo vi, Archetype arch, @NullAllowed Map<String,String> additionalProperties, boolean updateLastUsedProjectDir) throws IOException {
        org.netbeans.modules.maven.api.archetype.ArchetypeWizards.createFromArchetype(
                projDir, 
                convertProjectInfo(vi), 
                convertArchetype(arch), 
                additionalProperties, 
                updateLastUsedProjectDir);
    }

    /**
     * Opens newly created Maven projects.
     * 
     * @param dirF the top-level dir to check in
     * @param mainProjectDir the dir containing the main project, or null
     * @return set of project directories
     * @since 1.0
     * @throws java.io.IOException
     */
    public static Set<FileObject> openProjects(File dirF, File mainProjectDir) throws IOException {
        return org.netbeans.modules.maven.api.archetype.ArchetypeWizards.openProjects(dirF, mainProjectDir);
    }

    /**
     * Wizard iterator using a predetermined archetype.
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @param repository
     * @param title
     * @return a wizard iterators
     * @since 1.0
     * @see TemplateRegistration
     * @see <a href="@org-netbeans-modules-maven@/org/netbeans/modules/maven/api/archetype/ArchetypeWizards.html#TEMPLATE_FOLDER">#TEMPLATE_FOLDER</a>
     */
    public static WizardDescriptor.InstantiatingIterator<?> definedArchetype(String groupId, String artifactId, String version, @NullAllowed String repository, String title) {
        return org.netbeans.modules.maven.api.archetype.ArchetypeWizards.definedArchetype(groupId, artifactId, version, repository, title);
    }

    // possiblly expose the new method with Map<String, String> defaultProps here
    // and an API for others to consume
    // alas, with all the versioning stuff...

    private static org.netbeans.modules.maven.api.archetype.ProjectInfo convertProjectInfo(ProjectInfo pi) {
        return new org.netbeans.modules.maven.api.archetype.ProjectInfo(pi.getGroupId(), pi.getArtifactId(), pi.getVersion(), pi.getPackageName());
    }
    
    private static org.netbeans.modules.maven.api.archetype.Archetype convertArchetype(Archetype a) {
        org.netbeans.modules.maven.api.archetype.Archetype archetype = new org.netbeans.modules.maven.api.archetype.Archetype();
        archetype.setGroupId(a.getGroupId());
        archetype.setArtifactId(a.getArtifactId());
        archetype.setVersion(a.getVersion());
        archetype.setName(a.getName());
        archetype.setDescription(a.getDescription());
        archetype.setRepository(a.getRepository());
        return archetype;
    }
}
