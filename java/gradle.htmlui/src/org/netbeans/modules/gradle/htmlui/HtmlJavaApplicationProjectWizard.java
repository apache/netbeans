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

package org.netbeans.modules.gradle.htmlui;

import org.netbeans.modules.gradle.spi.newproject.SimpleGradleWizardIterator;
import org.netbeans.modules.gradle.spi.newproject.TemplateOperation;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@Messages("template.htmlUIProject=Java Frontend Application")
public class HtmlJavaApplicationProjectWizard extends SimpleGradleWizardIterator {
    private static final String DEFAULT_LICENSE_TEMPLATE = "/Templates/Licenses/license-default.txt"; //NOI18N
    
    @Messages("LBL_FrontendApplicationProject=Java Frontend Application with Gradle")
    public HtmlJavaApplicationProjectWizard() {
        super(Bundle.LBL_FrontendApplicationProject(), initParams());
    }

    private static Map<String, Object> initParams() {
        Map<String, Object> params = new HashMap<>();
        return params;
    }
    
    @Override
    protected List<? extends WizardDescriptor.Panel<WizardDescriptor>> createPanels() {
        return Collections.singletonList(createProjectAttributesPanel(null));
    }

    @Override
    protected void collectOperations(TemplateOperation ops, Map<String, Object> params) {
        super.collectOperations(ops, params);
        String packageBase = (String) params.get(PROP_PACKAGE_BASE);
        String mainClassName = (String) params.get("mainClassName");
        
        File projectDir = (File) params.get("projectDir");

        File mainJava = (File) params.get(PROP_MAIN_JAVA_DIR);
        File packageDir = new File(mainJava, packageBase.replace('.', '/'));
        Map<String, Object> mainParams = new HashMap<>(params);
        mainParams.put("project", new DummyProject());
        mainParams.put("package", packageBase); //NOI18N
        mainParams.put("name", mainClassName); //NOI18N

        FileObject folder = ((TemplateWizard)this.getData()).getTemplate().getPrimaryFile();
        ops.addConfigureProject(projectDir, new CopyTree(folder, projectDir, mainParams));
    }

    private static class CopyTree implements TemplateOperation.ProjectConfigurator {
        private final FileObject templateFolder;
        private final File projectDir;
        private final Map<String, Object> params;

        private CopyTree(FileObject templateFolder, File projectDir, Map<String, Object> params) {
            this.projectDir = projectDir;
            this.params = params;
            this.templateFolder = templateFolder;
        }

        @Override
        public void configure(Project project) {
            FileObject projectFo = FileUtil.toFileObject(projectDir);

            GradleArchetype ga = new GradleArchetype(templateFolder, projectFo, params);
            try {
                ga.copyTemplates();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                NbGradleProject.fireGradleProjectReload(project);
            }
        }
    }

    public static class DummyProject {
        public String getLicensePath() {
            return DEFAULT_LICENSE_TEMPLATE;
        }
    }

}
