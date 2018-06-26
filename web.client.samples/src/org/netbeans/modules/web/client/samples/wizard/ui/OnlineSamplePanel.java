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

package org.netbeans.modules.web.client.samples.wizard.ui;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.client.samples.wizard.WizardConstants;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Janicek
 */
public class OnlineSamplePanel implements WizardDescriptor.AsynchronousValidatingPanel<WizardDescriptor> {

    private WizardDescriptor descriptor;
    private OnlineSampleVisualPanel myPanel;


    public OnlineSamplePanel(WizardDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public OnlineSampleVisualPanel getComponent() {
        if (myPanel == null) {
            myPanel = new OnlineSampleVisualPanel(descriptor);
        }
        return myPanel;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getComponent().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getComponent().removeChangeListener(listener);
    }

    @Override
    public HelpCtx getHelp() {
        return null;
        //return new HelpCtx("html5.samples"); // NOI18N
    }

    @Override
    public boolean isValid() {
        String error = getComponent().getErrorMessage();
        if (error != null && !error.isEmpty()) {
            setErrorMessage(error);
            return false;
        }
        setErrorMessage(""); // NOI18N
        return true;
    }

    private void setErrorMessage(String message) {
        descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }

    @Override
    public void readSettings(WizardDescriptor descriptor) {
        this.descriptor = descriptor;
        descriptor.putProperty("NewProjectWizard_Title", NbBundle.getMessage(
                OnlineSamplePanel.class, "TTL_SamplePanel"));         // NOI18N
    }

    @Override
    public void storeSettings(WizardDescriptor descriptor) {
        descriptor.putProperty(WizardConstants.SAMPLE_PROJECT_URL, getComponent().getProjectURL());
        descriptor.putProperty(WizardConstants.SAMPLE_PROJECT_NAME, getComponent().getProjectName());
        descriptor.putProperty(WizardConstants.SAMPLE_PROJECT_DIR, getComponent().getProjectDirectory());
    }

    @Override
    public void prepareValidation() {
    }

    @Override
    public void validate() throws WizardValidationException {
        final String error = getComponent().prepareTemplate();
        if (error != null) {
            throw new WizardValidationException(getComponent(), "ERROR_PREPARE", error); // NOI18N
        }
    }
}
