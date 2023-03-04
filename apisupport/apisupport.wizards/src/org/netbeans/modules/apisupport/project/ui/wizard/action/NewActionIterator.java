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

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import java.io.IOException;
import java.util.Set;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle.Messages;

/**
 * Wizard for creating new Actions.
 *
 * @author Martin Krauskopf
 */
@TemplateRegistration(
    folder=UIUtil.TEMPLATE_FOLDER,
    id=UIUtil.TEMPLATE_ACTION_ID,
    displayName="#template_action",
    iconBase="org/netbeans/modules/apisupport/project/ui/wizard/action/newAction.png",
    position=100,
    description="newAction.html",
    category=UIUtil.TEMPLATE_CATEGORY
)
@Messages("template_action=Action")
public final class NewActionIterator extends BasicWizardIterator {

    private DataModel data;

    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }
    
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new ActionTypePanel(wiz, data),
            new GUIRegistrationPanel(wiz, data),
            new NameIconLocationPanel(wiz, data)
        };
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
}
