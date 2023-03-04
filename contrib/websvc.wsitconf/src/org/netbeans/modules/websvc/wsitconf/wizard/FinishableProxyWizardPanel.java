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

package org.netbeans.modules.websvc.wsitconf.wizard;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * FinishableProxyWizardPanel.java - used decorator pattern to enable to finish 
 * the original wizard panel, that is not finishable
 * 
 *
 * @author mkuchtiak
 */
public class FinishableProxyWizardPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
    
    private boolean enableFinish = false;        
    private WizardDescriptor.Panel original;
    private WizardDescriptor wizard;
    
    /** Creates a new instance of ProxyWizardPanel */
    public FinishableProxyWizardPanel(WizardDescriptor.Panel original, boolean enableFinish) {
        this.original=original;
        this.enableFinish = enableFinish;
    }

    public void addChangeListener(javax.swing.event.ChangeListener l) {
        original.addChangeListener(l);
    }

    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        original.removeChangeListener(l);
    }

    public void storeSettings(Object settings) {
        original.storeSettings(settings);
    }

    public void readSettings(Object settings) {
        wizard = (WizardDescriptor)settings;
        original.readSettings(settings);
    }

    public boolean isValid() {
        setErrorMessage();
        return original.isValid() && enableFinish;
    }

    public boolean isFinishPanel() {
        setErrorMessage();
        return enableFinish;
    }

    public java.awt.Component getComponent() {
        setErrorMessage();
        return original.getComponent();
    }

    public org.openide.util.HelpCtx getHelp() {
        return original.getHelp();
    }
    
    private void setErrorMessage() {
        if (!enableFinish) {
            if (wizard != null) {
                wizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(FinishableProxyWizardPanel.class, "ERR_NotSupportedInbJavaEE4")); // NOI18N
            }
        }
    }
}
