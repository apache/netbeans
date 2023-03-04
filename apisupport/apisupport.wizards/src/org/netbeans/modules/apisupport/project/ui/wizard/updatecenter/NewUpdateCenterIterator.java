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

package org.netbeans.modules.apisupport.project.ui.wizard.updatecenter;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle.Messages;

/**
 * Wizard for creating new update catalog.
 *
 * @author Jiri Rechtacek
 */
@TemplateRegistration(
    folder=UIUtil.TEMPLATE_FOLDER,
    position=600,
    displayName="#template_updatecenter",
    iconBase="org/netbeans/modules/apisupport/project/ui/wizard/updatecenter/newUpdateCenter.png",
    description="newUpdateCenter.html",
    category=UIUtil.TEMPLATE_CATEGORY
)
@Messages("template_updatecenter=Update Center")
public final class NewUpdateCenterIterator extends BasicWizardIterator {

    private DataModel data;
    
    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.refreshCreatedModifiedFiles();
        cmf.run();
        return Collections.singleton(LayerHandle.forProject(data.getProject()).getLayerFile());
    }

    protected BasicWizardIterator.Panel[] createPanels (WizardDescriptor wiz) {
        data = new DataModel (wiz);
        return new BasicWizardIterator.Panel[] {
            new UpdateCenterRegistrationPanel (wiz, data)
        };
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
        
}
