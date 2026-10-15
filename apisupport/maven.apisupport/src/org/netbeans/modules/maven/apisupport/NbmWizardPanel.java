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

package org.netbeans.modules.maven.apisupport;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.maven.apisupport.Bundle.*;

/**
 * Panel just asking for nb platform related information.
 * @author mkleint
 */
public class NbmWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    
    private WizardDescriptor wizardDescriptor;
    private NbmWizardPanelVisual component;

    private final Archetype archetype;
    private final ValidationGroup validationGroup;
    private final ValidationGroup justEnabledStateValidationGroup;
    


    public NbmWizardPanel(ValidationGroup enabledVG, ValidationGroup errorMsgVG, Archetype arch) {
        validationGroup = errorMsgVG;
        archetype = arch;
        justEnabledStateValidationGroup = enabledVG;
    }

    ValidationGroup getValidationGroup() {
        return validationGroup;
    }
    
    ValidationGroup getEnabledStateValidationGroup() {
        return justEnabledStateValidationGroup;
    }
    
    @Override
    @Messages("LBL_CreateProjectStepNbm=Module Options")
    public Component getComponent() {
        if (component == null) {
            component = new NbmWizardPanelVisual(this);
            component.setName(LBL_CreateProjectStepNbm());
        }
        return component;
    }

    public Archetype getArchetype() {
        return archetype;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.maven.apisupport.NbmWizardPanel");
    }
    
    public @Override void addChangeListener(ChangeListener l) {}
    public @Override void removeChangeListener(ChangeListener l) {}
    
    public @Override void readSettings(WizardDescriptor wiz) {
        wizardDescriptor = wiz;
        component.read(wizardDescriptor);
    }
    
    public @Override void storeSettings(WizardDescriptor wiz) {
        component.store(wiz);
    }
    
    @Override
    public boolean isFinishPanel() {
        return true;
    }
    
    @Override
    public boolean isValid() {
        getComponent();
        return validationGroup.performValidation() == null;
    }
    
}
