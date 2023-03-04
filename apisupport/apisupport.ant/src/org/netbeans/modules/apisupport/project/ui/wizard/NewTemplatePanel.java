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

import org.netbeans.modules.apisupport.project.api.BasicWizardPanel;
import org.openide.WizardDescriptor;

abstract class NewTemplatePanel extends BasicWizardPanel {

    private final NewModuleProjectData data;

    NewTemplatePanel(final NewModuleProjectData data) {
        super(data.getSettings());
        this.data = data;
    }

    abstract void reloadData();
    abstract void storeData();

    public NewModuleProjectData getData() {
        return data;
    }

    public @Override void readSettings(WizardDescriptor settings) {
        reloadData();
    }

    public @Override void storeSettings(WizardDescriptor settings) {
        storeData();
    }

    protected String getWizardTypeString() {
        String helpId = null;
        switch (data.getWizardType()) {
            case SUITE:
                helpId = "suite"; // NOI18N
                break;
            case APPLICATION:
                helpId = "application"; // NOI18N
                break;
            case MODULE:
            case SUITE_COMPONENT:
                helpId = "module"; // NOI18N
                break;
            case LIBRARY_MODULE:
                helpId = "library"; // NOI18N
                break;
            default:
                assert false : "Unknown wizard type = " + data.getWizardType();
        }
        return helpId;
    }

}
