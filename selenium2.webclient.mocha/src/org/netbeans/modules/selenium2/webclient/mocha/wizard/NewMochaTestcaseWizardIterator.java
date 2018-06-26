/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.selenium2.webclient.mocha.wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.selenium2.api.Selenium2Support;
import org.netbeans.modules.selenium2.spi.Selenium2SupportImpl;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Theofanis Oikonomou
 */
@TemplateRegistration(folder = "SeleniumTests",
        displayName = "#SeleniumMochaTestCase_displayName",
        content = "SeleneseMochaTest.js.template",
        description = "SeleneseMochaTestWebclient.html",
        position = 20,
        scriptEngine = "freemarker",
        category = "selenium-types")
@NbBundle.Messages({"SeleniumMochaTestCase_displayName=Selenium Mocha Test Case",
    "# {0} - project",
    "NO_SELENIUM_SUPPORT=No Selenium 2.0 support for project {0}"})
public class NewMochaTestcaseWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private transient WizardDescriptor.Panel panel;
    private transient WizardDescriptor wiz;

    @Override
    public Set instantiate() throws IOException {
        FileObject createdFile = null;
        FileObject targetFolder = Templates.getTargetFolder(wiz);
        Selenium2SupportImpl selenium2Support = Selenium2Support.findSelenium2Support(FileOwnerQuery.getOwner(targetFolder));
        if (selenium2Support == null) {
            return Collections.singleton(createdFile);
        }
        selenium2Support.configureProject(targetFolder);
        TestCreatorProvider.Context context = new TestCreatorProvider.Context(new FileObject[]{targetFolder});
        context.setSingleClass(true);
        context.setTargetFolder(targetFolder);
        context.setTestClassName(Templates.getTargetName(wiz));
        ArrayList<FileObject> createTests = Selenium2Support.createTests(context);
        if (!createTests.isEmpty()) {
            createdFile = createTests.get(0);
        }

        return Collections.singleton(createdFile);
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wiz = wizard;
        Project project = Templates.getProject(wiz);
        Selenium2SupportImpl selenium2Support = Selenium2Support.findSelenium2Support(project);
        Templates.setTargetName(wizard, "newSeleneseMochaTest"); // NOI18N
        if (selenium2Support != null){
            panel = selenium2Support.createTargetChooserPanel(wiz);
            panel.getComponent();
        } else {
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.NO_SELENIUM_SUPPORT(project.getProjectDirectory().toString()));
            panel = Templates.buildSimpleTargetChooser(project, new SourceGroup[0]).create();
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        this.wiz = null;
        panel = null;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panel;
    }

    @Override
    @NbBundle.Messages("Selenium2_Template_Wizard_Title=Selenium 2.0 Test Case name")
    public String name() {
        return Bundle.Selenium2_Template_Wizard_Title();
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public void nextPanel() {
        assert(false);
    }

    @Override
    public void previousPanel() {
        assert(false);
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
}
