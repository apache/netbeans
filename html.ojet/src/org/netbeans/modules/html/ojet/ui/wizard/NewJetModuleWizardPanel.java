/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.ojet.ui.wizard;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public final class NewJetModuleWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    static final String FILE_NAME = "FILE_NAME"; // NOI18N
    static final String PROJECT = "PROJECT"; // NOI18N
    static final String JS_FOLDER = "JS_FOLDER"; // NOI18N
    static final String HTML_FOLDER = "HTML_FOLDER"; // NOI18N

    private volatile NewJetModuleWizardPanelUi panel = null;
    // @GuardedBy("EDT")
    private WizardDescriptor wizard;


    @Override
    public Component getComponent() {
        return getPanel();
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        assert EventQueue.isDispatchThread();
        this.wizard = settings;
        getPanel().setFileName((String) settings.getProperty(FILE_NAME));
        getPanel().setProject((Project) settings.getProperty(PROJECT));
        getPanel().setJsFolder((String) settings.getProperty(JS_FOLDER));
        getPanel().setHtmlFolder((String) settings.getProperty(HTML_FOLDER));
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        settings.putProperty(FILE_NAME, getPanel().getFileName());
        settings.putProperty(JS_FOLDER, getPanel().getJsFolder());
        settings.putProperty(HTML_FOLDER, getPanel().getHtmlFolder());
    }

    @NbBundle.Messages({
        "NewJetModuleWizardPanel.error.project=New module can be created only for project.",
        "NewJetModuleWizardPanel.error.name=File name must be set.",
        "NewJetModuleWizardPanel.error.jsFolder=JS folder must be set.",
        "NewJetModuleWizardPanel.error.jsFile=JS file already exists.",
        "NewJetModuleWizardPanel.error.htmlFolder=HTML folder must be set.",
        "NewJetModuleWizardPanel.error.htmlFile=HTML file already exists.",
    })
    @Override
    public boolean isValid() {
        assert EventQueue.isDispatchThread();
        if (!getPanel().hasProject()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.NewJetModuleWizardPanel_error_project());
            return false;
        }
        String name = getPanel().getFileName();
        if (name == null
                || name.trim().isEmpty()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.NewJetModuleWizardPanel_error_name());
            return false;
        }
        String jsFolder = getPanel().getJsFolder();
        if (jsFolder == null
                || jsFolder.trim().isEmpty()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.NewJetModuleWizardPanel_error_jsFolder());
            return false;
        }
        String createdJsFile = getPanel().getCreatedJsFile();
        if (new File(createdJsFile).exists()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.NewJetModuleWizardPanel_error_jsFile());
            return false;
        }
        String htmlFolder = getPanel().getHtmlFolder();
        if (htmlFolder == null
                || htmlFolder.trim().isEmpty()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.NewJetModuleWizardPanel_error_htmlFolder());
            return false;
        }
        String createdHtmlFile = getPanel().getCreatedHtmlFile();
        if (new File(createdHtmlFile).exists()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.NewJetModuleWizardPanel_error_htmlFile());
            return false;
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); // NOI18N
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getPanel().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getPanel().removeChangeListener(listener);
    }

    private NewJetModuleWizardPanelUi getPanel() {
        if (panel == null) {
            panel = new NewJetModuleWizardPanelUi();
        }
        return panel;
    }

}
