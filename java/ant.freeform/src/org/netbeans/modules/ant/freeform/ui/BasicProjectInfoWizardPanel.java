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

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Component;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class BasicProjectInfoWizardPanel implements WizardDescriptor.Panel, ChangeListener {

    private BasicProjectInfoPanel component;
    private WizardDescriptor wizardDescriptor;
    private final ChangeSupport cs = new ChangeSupport(this);

    public BasicProjectInfoWizardPanel() {
        getComponent().setName(NbBundle.getMessage(BasicProjectInfoWizardPanel.class, "WizardPanel_NameAndLocation"));
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new BasicProjectInfoPanel("", "", "", "", this); // NOI18N
            component.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BasicProjectInfoWizardPanel.class, "ACSD_BasicProjectInfoWizardPanel")); // NOI18N
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx( BasicProjectInfoWizardPanel.class );
    }
    
    public boolean isValid() {
        getComponent();
        String[] error = component.getError();
        if (error != null) {
            wizardDescriptor.putProperty(error[1], error[0]);
            return false;
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
        return true;
    }
    
    public final void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    
    public final void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); // NOI18N
    }
    
    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        wizardDescriptor.putProperty(NewFreeformProjectSupport.PROP_ANT_SCRIPT, component.getAntScript());
        wizardDescriptor.putProperty(NewFreeformProjectSupport.PROP_PROJECT_NAME, component.getProjectName());
        wizardDescriptor.putProperty(NewFreeformProjectSupport.PROP_PROJECT_LOCATION, component.getProjectLocation());
        wizardDescriptor.putProperty(NewFreeformProjectSupport.PROP_PROJECT_FOLDER, component.getProjectFolder());
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); // NOI18N
    }
    
    public void stateChanged(ChangeEvent e) {
        cs.fireChange();
    }
    
}
