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

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Panel just asking for basic info.
 */
public class NewClientSideProjectPanel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor> {

    private final String projectNameTemplate;

    private volatile WizardDescriptor wizardDescriptor;
    // @GuardedBy("EDT") - not possible, wizard support calls store() method in EDT as well as in a background thread
    private volatile NewClientSideProject clientSideProject;


    public NewClientSideProjectPanel(String projectNameTemplate) {
        assert projectNameTemplate != null;
        this.projectNameTemplate = projectNameTemplate;
    }

    @Override
    public NewClientSideProject getComponent() {
        if (clientSideProject == null) {
            clientSideProject = new NewClientSideProject(projectNameTemplate);
        }
        return clientSideProject;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.clientproject.ui.wizard.NewClientSideProjectPanel"); // NOI18N
    }

    @Override
    public boolean isValid() {
        // error
        String error = getComponent().getErrorMessage();
        if (error != null && !error.isEmpty()) {
            setErrorMessage(error);
            return false;
        }
        // everything ok
        setErrorMessage(""); // NOI18N
        return true;
    }

    private void setErrorMessage(String message) {
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }

    @Override
    public final void addChangeListener(ChangeListener listener) {
        getComponent().addChangeListener(listener);
    }

    @Override
    public final void removeChangeListener(ChangeListener listener) {
        getComponent().removeChangeListener(listener);
    }

    @Override
    public void readSettings(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
    }

    @Override
    public void storeSettings(WizardDescriptor wizardDescriptor) {
        wizardDescriptor.putProperty(ClientSideProjectWizardIterator.Wizard.PROJECT_DIRECTORY, getComponent().getProjectDirectory());
        wizardDescriptor.putProperty(ClientSideProjectWizardIterator.Wizard.NAME, getComponent().getProjectName());
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

}
