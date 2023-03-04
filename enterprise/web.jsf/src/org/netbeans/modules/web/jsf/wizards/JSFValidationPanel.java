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

package org.netbeans.modules.web.jsf.wizards;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.common.ServerUtil;
import org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * A panel which extends {@code DelegatingWizardDescriptorPanel} and is used for
 * further validations of JavaServer Faces New File wizards.
 *
 * @author Martin Fousek
 */
public class JSFValidationPanel extends DelegatingWizardDescriptorPanel {

    public JSFValidationPanel(WizardDescriptor.Panel delegate) {
        super(delegate);
    }

    @Override
    public boolean isValid() {
        Project project = getProject();
        WizardDescriptor wizardDescriptor = getWizardDescriptor();

        if (super.isValid()) {
            // check that this project has a valid target server
            if (!ServerUtil.isValidServerInstance(project)) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                        NbBundle.getMessage(TemplatePanel.class, "WARN_MissingTargetServer"));
            }
            return true;
        }
        return false;
    }
}
