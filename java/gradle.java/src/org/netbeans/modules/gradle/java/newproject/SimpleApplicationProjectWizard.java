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

package org.netbeans.modules.gradle.java.newproject;

import org.netbeans.modules.gradle.spi.newproject.TemplateOperation;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator;
import static org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator.PROP_INIT_WRAPPER;
import static org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator.PROP_NAME;
import static org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator.PROP_PACKAGE_BASE;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Laszlo Kishalmi
 */
@TemplateRegistration(folder="Project/Gradle", position=100, displayName="#template.simpleAppProject", iconBase="org/netbeans/modules/gradle/java/resources/javaseProjectIcon.png", description="SimpleApplicationDescription.html")
@Messages("template.simpleAppProject=Java Application")
public class SimpleApplicationProjectWizard extends BaseGradleWizardIterator {
    public SimpleApplicationProjectWizard() {
    }

    @Override
    protected List<? extends WizardDescriptor.Panel<WizardDescriptor>> createPanels() {
        return Collections.singletonList(createProjectAttributesPanel(null));
    }

    @Messages("LBL_SimpleApplicationProject=Java Application with Gradle")
    @Override
    protected String getTitle() {
        return Bundle.LBL_SimpleApplicationProject();
    }

    @Override
    protected void collectOperations(TemplateOperation ops, Map<String, Object> params) {
        collectOperationsForType(params, ops, "java-application", "app");
    }

    static void collectOperationsForType(Map<String, Object> params, TemplateOperation ops, String type, String subFolder) {
        final String name = (String) params.get(PROP_NAME);
        final String packageBase = (String) params.get(PROP_PACKAGE_BASE);
        final File loc = (File) params.get(CommonProjectActions.PROJECT_PARENT_FOLDER);
        final File root = new File(loc, name);

        ops.createGradleInit(root, type).basePackage(packageBase).projectName(name).dsl("groovy").add(); // NOI18N

        Boolean initWrapper = (Boolean) params.get(PROP_INIT_WRAPPER);
        if (initWrapper == null || initWrapper) {
            // @TODO allow configuration of wrapper version
            ops.addWrapperInit(root, "latest"); // NOI18N
        } else {
            // @TODO delete wrapper added by init?
        }

        ops.addProjectPreload(root);
        ops.addProjectPreload(new File(root, subFolder));

    }

}
