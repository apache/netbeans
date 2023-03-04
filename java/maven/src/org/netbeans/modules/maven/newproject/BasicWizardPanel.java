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

package org.netbeans.modules.maven.newproject;

import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.api.archetype.Archetype;
import static org.netbeans.modules.maven.newproject.Bundle.*;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

public class BasicWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    
    private WizardDescriptor wizardDescriptor;
    private BasicPanelVisual component;

    private final boolean isFinish;
    private final boolean additional;
    private final ValidationGroup validationGroup;
    private final Archetype arch;
    private final Map<String, String> defaultProps;
    
    public BasicWizardPanel(ValidationGroup vg, @NullAllowed
            Archetype arch, boolean isFinish, boolean additional, Map<String,String> defaultArgs
    ) {
        this.isFinish = isFinish;
        this.additional = additional;
        this.validationGroup = vg;
        this.arch = arch;
        this.defaultProps = defaultArgs;
    }

    ValidationGroup getValidationGroup() {
        return validationGroup;
    }
    
    @Messages("LBL_CreateProjectStep2=Name and Location")
    public @Override BasicPanelVisual getComponent() {
        if (component == null) {
            component = new BasicPanelVisual(this, arch);
            component.setName(LBL_CreateProjectStep2());
        }
        return component;
    }

    boolean areAdditional() {
        return additional;
    }

    public @Override HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.maven.newproject.BasicWizardPanel");
    }
    
    
    private final ChangeSupport cs = new ChangeSupport(this);
    public @Override void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    public @Override void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    protected final void fireChangeEvent() {
        cs.fireChange();
    }
    
    public @Override void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        getComponent().read(wizardDescriptor, defaultProps);
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
//        Object substitute = getComponent().getClientProperty ("NewProjectWizard_Title"); // NOI18N
//        if (substitute != null) {
//            wizardDescriptor.putProperty ("NewProjectWizard_Title", "XXX"); // NOI18N
//        }        
    }
    
    public @Override void storeSettings(WizardDescriptor settings) {
        getComponent().store(settings);
    }
    
    public @Override boolean isFinishPanel() {
        return isFinish;
    }
    
    public @Override boolean isValid() {
        return validationGroup.performValidation() == null;
    }
    
}
