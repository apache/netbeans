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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.awt.Component;
import org.netbeans.modules.apisupport.project.api.BasicWizardPanel;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * First panel from <em>Adding New Platform</em> wizard panels. Allows user to
 * choose platform directory.
 *
 * @author Martin Krauskopf
 */
final class PlatformChooserWizardPanel extends BasicWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    
    /** Representing visual component for this step. */
    private PlatformChooserVisualPanel visualPanel;
    
    public PlatformChooserWizardPanel(WizardDescriptor settings) {
        super(settings);
    }
    
    public @Override void storeSettings(WizardDescriptor settings) {
        visualPanel.storeData();
    }
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new PlatformChooserVisualPanel(getSettings());
            visualPanel.addPropertyChangeListener(this);
            visualPanel.setName(NbPlatformCustomizer.CHOOSER_STEP);
        }
        return visualPanel;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.apisupport.project.ui.platform.PlatformChooserWizardPanel");
    }
    
    public boolean isFinishPanel() {
        return NbPlatform.isLabelValid((String) getSettings().getProperty(NbPlatformCustomizer.PLAF_LABEL_PROPERTY));
    }
    
}
