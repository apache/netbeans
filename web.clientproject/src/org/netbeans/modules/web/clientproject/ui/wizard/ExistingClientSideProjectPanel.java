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

import java.io.File;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;

/**
 * Controller for existing project.
 */
public class ExistingClientSideProjectPanel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor> {

    private volatile WizardDescriptor wizardDescriptor;
    // @GuardedBy("EDT") - not possible, wizard support calls store() method in EDT as well as in a background thread
    private volatile ExistingClientSideProject clientSideProject;


    @Override
    public ExistingClientSideProject getComponent() {
        // assert EventQueue.isDispatchThread(); - not possible, see comment above (@GuardedBy())
        if (clientSideProject == null) {
            clientSideProject = new ExistingClientSideProject();
        }
        return clientSideProject;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.clientproject.ui.wizard.ExistingClientSideProjectPanel"); // NOI18N
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        wizardDescriptor.putProperty(ClientSideProjectWizardIterator.Wizard.PROJECT_DIRECTORY, getNormalizedFile(getComponent().getProjectDirectory()));
        wizardDescriptor.putProperty(ClientSideProjectWizardIterator.Wizard.NAME, getComponent().getProjectName());
        wizardDescriptor.putProperty(ClientSideProjectWizardIterator.ExistingHtml5ProjectWizard.SITE_ROOT, getNormalizedFile(getComponent().getSiteRoot()));
        wizardDescriptor.putProperty(ClientSideProjectWizardIterator.ExistingHtml5ProjectWizard.SOURCE_ROOT, getNormalizedFile(getComponent().getSources()));
        wizardDescriptor.putProperty(ClientSideProjectWizardIterator.ExistingHtml5ProjectWizard.TEST_ROOT, getNormalizedFile(getComponent().getTestDir()));
    }

    private File getNormalizedFile(String path) {
        if (!StringUtilities.hasText(path)) {
            return null;
        }
        return FileUtil.normalizeFile(new File(path));
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
    public void addChangeListener(ChangeListener listener) {
        getComponent().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getComponent().removeChangeListener(listener);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

}
