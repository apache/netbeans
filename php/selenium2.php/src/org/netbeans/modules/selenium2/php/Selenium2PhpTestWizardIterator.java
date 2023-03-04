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
package org.netbeans.modules.selenium2.php;

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
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Theofanis Oikonomou
 */
@TemplateRegistration(folder = "SeleniumTests",
        displayName = "#SeleniumTestCase_displayName",
        content = "SeleneseTest.php.template",
        description = "SeleneseTestPHP.html",
        position = 15,
        scriptEngine = "freemarker",
        category = "PHP")
@Messages({"SeleniumTestCase_displayName=Selenium Test Case",
    "# {0} - project",
    "NO_SELENIUM_SUPPORT=No Selenium 2.0 support for project {0}"})
public class Selenium2PhpTestWizardIterator implements WizardDescriptor.InstantiatingIterator {

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
