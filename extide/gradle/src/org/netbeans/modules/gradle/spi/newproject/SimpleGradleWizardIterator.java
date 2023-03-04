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

package org.netbeans.modules.gradle.spi.newproject;

import static org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator.PROP_INIT_WRAPPER;
import static org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator.PROP_NAME;
import static org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator.PROP_PACKAGE_BASE;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.WizardDescriptor;

/**
 *
 * @author Laszlo Kishalmi
 */
public class SimpleGradleWizardIterator extends BaseGradleWizardIterator {

    final String title;
    final String buildTemplate;
    final Map<String, Object> templateParams;

    @StaticResource
    private static final String TEMPLATE_SETTINGS = "org/netbeans/modules/gradle/newproject/single-settings.gradle.template";
    @StaticResource
    private static final String TEMPLATE_BUILD = "org/netbeans/modules/gradle/newproject/single-build.gradle.template";
    @StaticResource
    private static final String TEMPLATE_PROPS = "org/netbeans/modules/gradle/newproject/single-gradle.properties.template";

    public static final String PROP_PROJECT_ROOT = "projectDir";
    public static final String PROP_MAIN_JAVA_DIR = "projectMainJavaDir";

    public static final String PROP_PLUGINS = "plugins"; //NOI18N
    public static final String PROP_DEPENDENCIES = "dependencies"; //NOI18N

    public static final String PROP_JAVA_VERSION = "javaVersion"; //NOI18N

    public SimpleGradleWizardIterator(String title, String buildTemplate, Map<String, Object> params) {
        this.title = title;
        this.buildTemplate = buildTemplate;
        this.templateParams = params;
    }

    public SimpleGradleWizardIterator(String title, Map<String, Object> params) {
        this(title, TEMPLATE_BUILD, params);
    }

    @Override
    protected List<? extends WizardDescriptor.Panel<WizardDescriptor>> createPanels() {
        return Collections.singletonList(createProjectAttributesPanel(null));
    }

    @Override
    protected final String getTitle() {
        return title;
    }

    @Override
    protected void collectOperations(final TemplateOperation ops, Map<String, Object> params) {
        params.putAll(templateParams);
        String name = (String) params.get(PROP_NAME);
        String packageBase = (String) params.get(PROP_PACKAGE_BASE);
        File loc = (File) params.get(CommonProjectActions.PROJECT_PARENT_FOLDER);

        File root = new File(loc, name);
        File mainJava = new File(root, "src/main/java");

        params.put(PROP_PROJECT_ROOT, root);
        params.put(PROP_MAIN_JAVA_DIR, mainJava);

        ops.createFolder(root);
        ops.copyFromFile(buildTemplate,
                new File(root, "build.gradle"), params); //NOI18N
        if (assumedRoot() == null) {
            ops.copyFromFile(TEMPLATE_SETTINGS,
                    new File(root, "settings.gradle"), params); //NOI18N
            ops.copyFromFile(TEMPLATE_PROPS,
                    new File(root, "gradle.properties"), params); //NOI18N
        }


        ops.createFolder(mainJava);
        ops.createPackage(mainJava, packageBase);
        ops.addProjectPreload(root);

        Boolean initWrapper = (Boolean) params.get(PROP_INIT_WRAPPER);
        if (initWrapper == null || initWrapper) {
            ops.addWrapperInit(root);
        }
    }

}
