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
package org.netbeans.modules.cordova.wizard;

import org.netbeans.modules.cordova.project.CordovaNotFound;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cordova.options.MobilePlatformsOptionsPanelController;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class CordovaSetupPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    private WizardDescriptor myDescriptor;
    private JComponent panel;


    public CordovaSetupPanel(WizardDescriptor descriptor) {
        myDescriptor = descriptor;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public JComponent getComponent() {
        if (panel == null) {
            panel = new CordovaNotFound();
            panel.setName(NbBundle.getMessage(CordovaSetupPanel.class, "LBL_MobilePlatformsSetup"));//NOI18N
        }
        return panel;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.cordova.wizard.CordovaSetupPanel"); // NOI18N
    }

    @Override
    public boolean isValid() {
        setErrorMessage("Install Cordova and restart NetBeans");
        return false;
    }

    @Override
    public void readSettings(WizardDescriptor descriptor) {
        this.myDescriptor=descriptor;
        descriptor.putProperty("NewProjectWizard_Title", NbBundle.getMessage(
                SamplePanel.class, "TTL_SamplePanel"));         // NOI18N
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
    }

    @Override
    public void storeSettings(WizardDescriptor descriptor) {
    }

    private void setErrorMessage(String message) {
        myDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }
}