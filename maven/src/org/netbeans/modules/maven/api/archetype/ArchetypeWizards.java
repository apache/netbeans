/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
        return new BasicWizardPanel(vg, archetype, isFinish, false);
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
        Archetype arch = new Archetype();
        arch.setGroupId(groupId);
        arch.setArtifactId(artifactId);
        arch.setVersion(version);
        arch.setRepository(repository);
        return new MavenWizardIterator(arch, title);
    }

}
