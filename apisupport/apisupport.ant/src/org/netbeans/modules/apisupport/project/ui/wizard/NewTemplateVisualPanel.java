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

package org.netbeans.modules.apisupport.project.ui.wizard;

import org.netbeans.modules.apisupport.project.api.BasicVisualPanel;
import static org.netbeans.modules.apisupport.project.ui.wizard.Bundle.*;

abstract class NewTemplateVisualPanel extends BasicVisualPanel {

    private final NewModuleProjectData data;

    NewTemplateVisualPanel(final NewModuleProjectData data) {
        super(data.getSettings());
        this.data = data;
        String title;
        switch (data.getWizardType()) {
            case SUITE:
                title = template_suite();
                break;
            case APPLICATION:
                title = template_application();
                break;
            case MODULE:
            case SUITE_COMPONENT:
                title = template_module();
                break;
            case LIBRARY_MODULE:
                title = template_library_module();
                break;
            default:
                assert false : "Unknown wizard type = " + data.getWizardType();
                title = "";
        }
        data.getSettings().putProperty("NewProjectWizard_Title", // NOI18N
                title);
    }

    protected NewModuleProjectData getData() {
        return data;
    }

    protected boolean isSuiteWizard() {
        return NewNbModuleWizardIterator.isSuiteWizard(getData().getWizardType());
    }

    protected boolean isSuiteComponentWizard() {
        return NewNbModuleWizardIterator.isSuiteComponentWizard(getData().getWizardType());
    }

    protected boolean isLibraryWizard() {
        return NewNbModuleWizardIterator.isLibraryWizard(getData().getWizardType());
    }

}
