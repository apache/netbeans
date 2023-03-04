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

package org.netbeans.modules.gradle.htmlui;

import org.netbeans.modules.gradle.spi.newproject.SimpleGradleWizardIterator;
import org.netbeans.modules.gradle.spi.newproject.TemplateOperation;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;
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
        String name = (String) params.get(PROP_NAME);
        File loc = (File) params.get(CommonProjectActions.PROJECT_PARENT_FOLDER);

        File rootDir = new File(loc, name);
        params.put(PROP_PROJECT_ROOT, rootDir);

        ops.createFolder(rootDir);

        FileObject folder = readKey(params, "template", FileObject.class); // NOI18N
        if (folder == null) {
            folder = ((TemplateWizard)this.getData()).getTemplate().getPrimaryFile();
        }
        GradleArchetype ga = new GradleArchetype(folder, rootDir, params);
        ga.copyTemplates(ops);

        Boolean initWrapper = (Boolean) params.get(PROP_INIT_WRAPPER);
        if (initWrapper == null || initWrapper) {
            ops.addWrapperInit(rootDir);
        }
    }

    private static <T> T readKey(Map<String, Object> map, String key, Class<T> type) {
        Object obj = map.get(key);
        return type.isInstance(obj) ? type.cast(obj) : null;
    }

    public static class DummyProject {
        public String getLicensePath() {
            return DEFAULT_LICENSE_TEMPLATE;
        }
    }

}
