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
package org.netbeans.modules.web.clientproject.ui.wizard;

import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;

public class SiteTemplateWizardPanel implements WizardDescriptor.ExtendedAsynchronousValidatingPanel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor> {

    static final Logger LOGGER = Logger.getLogger(SiteTemplateWizardPanel.class.getName());

    // @GuardedBy("EDT") - not possible, wizard support calls store() method in EDT as well as in a background thread
    private volatile SiteTemplateWizard siteTemplateWizard;
    private volatile WizardDescriptor wizardDescriptor;


    @Override
    public SiteTemplateWizard getComponent() {
        if (siteTemplateWizard == null) {
            // #245956
            siteTemplateWizard = Mutex.EVENT.readAccess(new Mutex.Action<SiteTemplateWizard>() {
                @Override
                public SiteTemplateWizard run() {
                    return new SiteTemplateWizard();
                }
            });
        }
        return siteTemplateWizard;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.clientproject.ui.wizard.SiteTemplateWizard"); // NOI18N
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        SiteTemplateImplementation template = (SiteTemplateImplementation) wizardDescriptor.getProperty(ClientSideProjectWizardIterator.NewHtml5ProjectWizard.SITE_TEMPLATE);
        if (template != null) {
            getComponent().preSelectSiteTemplate(template);
        }

    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        wizardDescriptor.putProperty(ClientSideProjectWizardIterator.NewHtml5ProjectWizard.SITE_TEMPLATE, getComponent().getSiteTemplate());
    }

    @Override
    public void prepareValidation() {
        getComponent().lockPanel();
    }

    @Override
    public void validate() throws WizardValidationException {
        String error = getComponent().prepareTemplate();
        if (error != null) {
            throw new WizardValidationException(getComponent(), "ERROR_PREPARE", error); // NOI18N
        }
    }

    @Override
    public void finishValidation() {
        getComponent().unlockPanel();
    }

    @Override
    public boolean isValid() {
        // error
        String error = getComponent().getErrorMessage();
        if (error != null && !error.isEmpty()) {
            setErrorMessage(error);
            return false;
        }
        // warning
        String warning = getComponent().getWarningMessage();
        if (warning != null && !warning.isEmpty()) {
            setErrorMessage(warning);
            return true;
        }
        // everything ok
        setErrorMessage(""); // NOI18N
        return true;
    }

    private void setErrorMessage(String message) {
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        getComponent().addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        getComponent().removeChangeListener(l);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

}
