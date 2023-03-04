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

package org.netbeans.modules.gradle.newproject;

import org.netbeans.modules.gradle.spi.newproject.TemplateOperation;
import java.util.Set;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.WizardDescriptor;
import static org.netbeans.modules.gradle.newproject.Bundle.*;
import org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Laszlo Kishalmi
 */
@TemplateRegistration(folder="Project/Gradle", position=500, displayName="#template.multiProject", iconBase="org/netbeans/modules/gradle/resources/gradle.png", description="MultiProjectDescription.html")
@Messages("template.multiProject=Multi-Project Build")
public class GradleMultiWizardIterator extends BaseGradleWizardIterator {

    private static final long serialVersionUID = 1L;

    public static final String PROP_SUBPROJECTS = "subProjects";  //NOI18N

    @StaticResource
    private static final String TEMPLATE_ROOT = "org/netbeans/modules/gradle/newproject/multi-root.gradle.template";
    @StaticResource
    private static final String TEMPLATE_SETTINGS = "org/netbeans/modules/gradle/newproject/multi-settings.gradle.template";
    @StaticResource
    private static final String TEMPLATE_BUILD = "org/netbeans/modules/gradle/newproject/multi-build.gradle.template";
    @StaticResource
    private static final String TEMPLATE_PROPS = "org/netbeans/modules/gradle/newproject/multi-gradle.properties.template";

    @Override
    protected List<? extends WizardDescriptor.Panel<WizardDescriptor>> createPanels() {
        return Arrays.asList(
            createProjectAttributesPanel(null),
            new SubProjectPanel()
        );
    }

    @Override
    @Messages("LBL_MultiProject=Gradle Multi-Project Build")
    protected String getTitle() {
        return LBL_MultiProject();
    }

    @Override
    protected void collectOperations(TemplateOperation ops, Map<String, Object> params) {
        String name = (String) params.get(PROP_NAME);
        String packageBase = (String) params.get(PROP_PACKAGE_BASE);
        String sp = (String) params.get(PROP_SUBPROJECTS);
        Set<SubProject> subProjects = parseSubProjects(sp);

        params.put(PROP_SUBPROJECTS, subProjects);

        File loc = (File) params.get(CommonProjectActions.PROJECT_PARENT_FOLDER);

        File root = new File(loc, name);
        ops.createFolder(root);
        ops.copyFromFile(TEMPLATE_PROPS,
                new File(root, "gradle.properties"), params); //NOI18N
        ops.copyFromFile(TEMPLATE_SETTINGS,
                new File(root, "settings.gradle"), params); //NOI18N
        ops.copyFromFile(TEMPLATE_ROOT,
                new File(root, "build.gradle"), params); //NOI18N

        for (SubProject subProject : subProjects) {
            HashMap<String, Object> subParams = new HashMap<> (params);
            subParams.put("subProject", subProject); //NOI18N

            File projectDir =  new File(root, subProject.path);
            ops.createFolder(projectDir);
            ops.copyFromFile(TEMPLATE_BUILD,
                    new File(projectDir, "build.gradle"), subParams); //NOI18N
            File mainJava = new File(projectDir, "src/main/java"); //NOI18N
            if (!packageBase.isEmpty()) {
                ops.createPackage(mainJava, packageBase + '.' + subProject.name);
            }
        }

        ops.addProjectPreload(root);
        for (SubProject subProject : subProjects) {
            File projectDir =  new File(root, subProject.path);
            ops.addProjectPreload(projectDir);
        }

        Boolean initWrapper = (Boolean) params.get(PROP_INIT_WRAPPER);
        if (initWrapper == null || initWrapper) {
            ops.addWrapperInit(root);
        }

    }

    private static Set<SubProject> parseSubProjects(String text) {
        Set<SubProject> ret = new LinkedHashSet<>();
        if (text != null) {
            String[] lines = text.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    ret.add(new SubProject(line));
                }
            }
        }
        return ret;
    }

    public static class SubProject {
        final String path;
        final String name;
        final String description;

        public SubProject(String definition) {
            String[] parts = definition.split("\\s+", 2); //NOI18N
            path = parts[0].replace('\\', '/');
            int pathSep = path.lastIndexOf('/');
            name = pathSep > 0 ? path.substring(pathSep + 1) : path;
            description = parts.length == 2 ? parts[1] : name;
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SubProject other = (SubProject) obj;
            return Objects.equals(this.path, other.path);
        }

    }
}
