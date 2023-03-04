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

package org.netbeans.modules.websvc.core.dev.wizard;

import org.netbeans.api.project.SourceGroup;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * FinishableProxyWizardPanel.java - used decorator pattern to enable to finish 
 * the original wizard panel, that is not finishable
 * 
 *
 * @author mkuchtiak
 */
public class FinishableProxyWizardPanel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor> {
    
    private WizardDescriptor.Panel<WizardDescriptor> original;
    private SourceGroup[] sourceGroups;
    private boolean checkSourceGroups;
    private WizardDescriptor settings;
    /** Creates a new instance of ProxyWizardPanel */

    public FinishableProxyWizardPanel(WizardDescriptor.Panel<WizardDescriptor> original) {
        this.original = original;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("EI_EXPOSE_REP2")
    public FinishableProxyWizardPanel(WizardDescriptor.Panel<WizardDescriptor> original, SourceGroup[] sourceGroups, boolean checkSourceGroups) {
        this.original=original;
        this.sourceGroups = sourceGroups;
        this.checkSourceGroups = checkSourceGroups;
    }

    public void addChangeListener(javax.swing.event.ChangeListener l) {
        original.addChangeListener(l);
    }

    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        original.removeChangeListener(l);
    }

    public void storeSettings(WizardDescriptor settings) {
        original.storeSettings(settings);
    }

    public void readSettings(WizardDescriptor settings) {
        this.settings = settings;
        original.readSettings(settings);
    }

    public boolean isValid() {
        String warningMessage = null;
        if (sourceGroups != null && sourceGroups.length == 0) {
            if (checkSourceGroups) {
                settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                    NbBundle.getMessage(FinishableProxyWizardPanel.class, "ERR_NoSources")); // NOI18N
                return false;
            } else {
                warningMessage = NbBundle.getMessage(FinishableProxyWizardPanel.class, "MSG_NoSources");
            }
        }
        boolean valid = original.isValid();
        if (valid) {
            if (warningMessage != null) {
                settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, warningMessage);
            }
        }
        return valid;
    }

    public boolean isFinishPanel() {
        return true;
    }

    public java.awt.Component getComponent() {
        return original.getComponent();
    }

    public org.openide.util.HelpCtx getHelp() {
        return original.getHelp();
    }
    
}
