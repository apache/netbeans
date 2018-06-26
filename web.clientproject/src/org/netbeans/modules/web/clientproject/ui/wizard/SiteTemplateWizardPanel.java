/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
