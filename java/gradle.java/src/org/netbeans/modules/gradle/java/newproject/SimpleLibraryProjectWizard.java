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

package org.netbeans.modules.gradle.java.newproject;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator;
import org.netbeans.modules.gradle.spi.newproject.TemplateOperation;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Laszlo Kishalmi
 */
@TemplateRegistration(folder="Project/Gradle", position=110, displayName="#template.simpleLibProject", iconBase="org/netbeans/modules/gradle/java/resources/javaseProjectIcon.png", description="SimpleLibraryDescription.html")
@NbBundle.Messages("template.simpleLibProject=Java Class Library")
public class SimpleLibraryProjectWizard extends BaseGradleWizardIterator {
    public SimpleLibraryProjectWizard() {
    }

    @Override
    protected List<? extends WizardDescriptor.Panel<WizardDescriptor>> createPanels() {
        return Collections.singletonList(createProjectAttributesPanel(null));
    }

    @NbBundle.Messages("LBL_SimpleLibraryProject=Java Class Library with Gradle")
    @Override
    protected String getTitle() {
        return Bundle.LBL_SimpleLibraryProject();
    }

    @Override
    protected void collectOperations(TemplateOperation ops, Map<String, Object> params) {
        SimpleApplicationProjectWizard.collectOperationsForType(params, ops, "java-library", "lib");
    }
}

