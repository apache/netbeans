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

package org.netbeans.modules.maven.api.archetype;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.newproject.ArchetypeWizardUtils;
import org.netbeans.modules.maven.newproject.BasicWizardPanel;
import org.netbeans.modules.maven.newproject.MavenWizardIterator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationGroupProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 * Utilities for creating New Project wizards based on maven-archetype-plugin.
 */
public class ArchetypeWizards {

    private ArchetypeWizards() {}

    /**
     * Customary location of Maven project templates.
     * @see TemplateRegistration#folder
     */
    public static final String TEMPLATE_FOLDER = "Project/Maven2";

    /**
     * Run a single archetype.
     * @param projDir the new project directory (must be normalized first!) (note: parent dir is actually passed to plugin, i.e. assumes that project name matches this basedir)
     * @param vi metadata for new project
     * @param arch the archetype to process
     * @param additionalProperties any additional archetype properties, or null
     * @param updateLastUsedProjectDir true to update last-used project directory for next wizard run
     */
    public static void createFromArchetype(File projDir, ProjectInfo vi, Archetype arch, @NullAllowed Map<String,String> additionalProperties, boolean updateLastUsedProjectDir) throws IOException {
        ArchetypeWizardUtils.createFromArchetype(projDir, vi, arch, additionalProperties, updateLastUsedProjectDir);
    }

    /**
     * Opens newly created Maven projects.
     * @param dirF the top-level dir to check in
     * @param mainProjectDir the dir containing the main project, or null
     * @return set of project directories
     */
    public static Set<FileObject> openProjects(File dirF, File mainProjectDir) throws IOException {
        return ArchetypeWizardUtils.openProjects(dirF, mainProjectDir);
    }

    /**
     * Log new project usage.
     */
    public static void logUsage(String groupId, String artifactId, String version) {
        ArchetypeWizardUtils.logUsage(groupId, artifactId, version);
    }

    public static ModelOperation<POMModel> addDependencyOperation(ProjectInfo info, String type) {
        return new ArchetypeWizardUtils.AddDependencyOperation(info, type);
    }

    public static WizardDescriptor.Panel<WizardDescriptor> basicWizardPanel(ValidationGroup vg, boolean isFinish, @NullAllowed Archetype archetype) {
        return new BasicWizardPanel(vg, archetype, isFinish, false, null);
    }

    static WizardDescriptor.Panel<WizardDescriptor> basicWizardPanel(Object p, String type) {
        return basicWizardPanel(ValidationGroup.create(), false, null);
    }

    /**
     * Wizard iterator using a predetermined archetype.
     * @since 2.28
     * @see TemplateRegistration
     * @see #TEMPLATE_FOLDER
     * @deprecated use the variant with template title name
     */
    @Deprecated
    public static WizardDescriptor.InstantiatingIterator<?> definedArchetype(String groupId, String artifactId, String version, @NullAllowed String repository) {
        return definedArchetype(groupId, artifactId, version, repository, null);
    }
    
    /**
     * Wizard iterator using a predetermined archetype.
     * @since 2.63
     * @see TemplateRegistration
     * @see #TEMPLATE_FOLDER
     */
    public static WizardDescriptor.InstantiatingIterator<?> definedArchetype(String groupId, String artifactId, String version, @NullAllowed String repository, String title) {
        return definedArchetype(groupId, artifactId, version, repository, title, null);
    }
    /** 
     * @since 2.138
     */
    public static WizardDescriptor.InstantiatingIterator<?> definedArchetype(
        String groupId, String artifactId, String version, @NullAllowed String repository, String title,
        Map<String,String> defaultProps
    ) {
        Archetype arch = new Archetype();
        arch.setGroupId(groupId);
        arch.setArtifactId(artifactId);
        arch.setVersion(version);
        arch.setRepository(repository);
        return new MavenWizardIterator(arch, title, defaultProps);
    }

}
