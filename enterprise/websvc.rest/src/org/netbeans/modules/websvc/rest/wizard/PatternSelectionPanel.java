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

package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import org.netbeans.modules.websvc.rest.wizard.PatternResourcesSetupPanel.Pattern;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * @author nam
 */
final class PatternSelectionPanel extends AbstractPanel {
    private PatternSelectionPanelVisual component;
    private Pattern selectedPattern;
    
    /** Create the wizard panel descriptor. */
    public PatternSelectionPanel(String name, WizardDescriptor wizardDescriptor) {
        super(name, wizardDescriptor);
    }
    
    public boolean isFinishPanel() {
        return false;
    }

    public Component getComponent() {
        if (component == null) {
            component = new PatternSelectionPanelVisual(panelName);
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return null;
    }
    
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }

    @Override
    public void storeSettings(Object settings) {
        super.storeSettings(settings);
        selectedPattern = (Pattern) ((WizardDescriptor)settings).getProperty(WizardProperties.PATTERN_SELECTION);
    }

    public Pattern getSelectedPattern() {
        return selectedPattern;
    }
}
