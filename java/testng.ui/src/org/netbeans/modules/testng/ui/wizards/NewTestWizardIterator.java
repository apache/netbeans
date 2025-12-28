/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.testng.ui.wizards;

import java.awt.Component;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.*;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin;
import org.netbeans.modules.java.testrunner.CommonSettings;
import org.netbeans.modules.java.testrunner.CommonTestUtil;
import org.netbeans.modules.java.testrunner.GuiUtils;
import org.netbeans.modules.testng.api.TestNGSupport;
import org.netbeans.modules.testng.api.TestNGUtils;
import org.netbeans.modules.testng.spi.TestNGSupportImplementation;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new TestNG file.
 */
@TemplateRegistration(folder = "UnitTests", position = 1100,
        content = "../resources/EmptyTestNGTest.java.template",
        scriptEngine = "freemarker",
        displayName = "#EmptyTestNGTest_displayName",
        iconBase = "org/netbeans/modules/testng/ui/resources/testng.gif",
        description = "/org/netbeans/modules/testng/ui/resources/newTest.html",
        category="junit")
@NbBundle.Messages("EmptyTestNGTest_displayName=TestNG Test Case")
public final class NewTestWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;

    public NewTestWizardIterator() {
    }

    private WizardDescriptor.Panel[] createPanels(final WizardDescriptor wizardDescriptor) {
        // Ask for Java folders
        Project project = Templates.getProject(wizardDescriptor);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = getTestRoots(sources);
        if (groups.length == 0) {
            if (SourceGroupModifier.createSourceGroup(project, JavaProjectConstants.SOURCES_TYPE_JAVA, JavaProjectConstants.SOURCES_HINT_TEST) != null) {
                groups = getTestRoots(sources);
            }
        }
        if (groups.length == 0) {
            groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            return new WizardDescriptor.Panel[]{
                        Templates.buildSimpleTargetChooser(project, groups).create()
                    };
        } else {
            return new WizardDescriptor.Panel[]{
                        JavaTemplates.createPackageChooser(project, groups, new EmptyTestStepLocation())
                    };
        }
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[(before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }

    public Set<FileObject> instantiate() throws IOException {
        saveSettings(wiz);        
        
        /* collect and build necessary data: */
        String name = Templates.getTargetName(wiz);
        FileObject targetFolder = Templates.getTargetFolder(wiz);
        if(targetFolder == null) {
            Logger.getLogger(NewTestWizardIterator.class.getName()).log(Level.INFO, "Target folder was null while creating new TestNG file");
            return null;
        }
        Project project = FileOwnerQuery.getOwner(targetFolder);
        if (project == null) {
            Logger.getLogger(NewTestWizardIterator.class.getName()).log(Level.INFO, "No project found for target folder: {0}", targetFolder);
            return null;
        }
        TestNGSupportImplementation testNGSupport = TestNGSupport.findTestNGSupport(project);
        if (testNGSupport == null) {
            Logger.getLogger(NewTestWizardIterator.class.getName()).log(Level.INFO, "No TestNGSupportImplementation found for target folder: {0}", targetFolder);
            return null;
        }
        testNGSupport.configureProject(targetFolder);
        
        Map<CommonPlugin.CreateTestParam, Object> params
                = CommonTestUtil.getSettingsMap(false);
        params.put(CommonPlugin.CreateTestParam.CLASS_NAME,
                   Templates.getTargetName(wiz));
                
        if (!TestNGUtils.createTestActionCalled(new FileObject[] {targetFolder})) {
            return null;
        }

        /*
         * The TestNGPlugin instance must be initialized _before_ field
         * TestNGPluginTrampoline.DEFAULT gets accessed.
         * See issue #74744.
         */
        final FileObject[] testFileObjects
                = TestNGUtils.createTests(
                     null,
                     targetFolder,
                     params);
        
        if (testFileObjects == null) {
            throw new IOException();
        }
        
        FileObject createdFile = testFileObjects[0];
        return Collections.singleton(createdFile);
    }

    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels(wiz);
        loadSettings(wiz);
        // Make sure list of steps is accurate.
        String[] beforeSteps = null;
        Object prop = wiz.getProperty("WizardPanel_contentData"); // NOI18N
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", Integer.valueOf(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }   
    
    private void loadSettings(WizardDescriptor wizard) {
        CommonSettings settings = TestNGUtils.getTestNGSettings();        
        wizard.putProperty(GuiUtils.CHK_SETUP, settings.isGenerateSetUp());
        wizard.putProperty(GuiUtils.CHK_TEARDOWN, settings.isGenerateTearDown());
        wizard.putProperty(GuiUtils.CHK_BEFORE_CLASS, settings.isGenerateClassSetUp());
        wizard.putProperty(GuiUtils.CHK_AFTER_CLASS, settings.isGenerateClassTearDown());
        wizard.putProperty(GuiUtils.CHK_HINTS, settings.isBodyComments());
    }

    private void saveSettings(WizardDescriptor wizard) {
        CommonSettings settings = TestNGUtils.getTestNGSettings();
        settings.setGenerateSetUp(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_SETUP)));
        settings.setGenerateTearDown(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_TEARDOWN)));
        settings.setGenerateClassSetUp(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_BEFORE_CLASS)));
        settings.setGenerateClassTearDown(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_AFTER_CLASS)));
        settings.setBodyComments(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_HINTS)));
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wiz = null;
        panels = null;
    }

    public String name() {
        return ""; // NOI18N
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }

    private static String getSelectedPackageName(FileObject targetFolder) {
        Project project = FileOwnerQuery.getOwner(targetFolder);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = FileUtil.getRelativePath(groups[i].getRootFolder(), targetFolder);
        }
        if (packageName != null) {
            packageName = packageName.replace("/", "."); // NOI18N
        }
        return packageName;
    }
    
    private SourceGroup[] getTestRoots(Sources srcs) {
        SourceGroup[] groups = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assert groups != null : "Cannot return null from Sources.getSourceGroups: " + srcs;

        //XXX - have to filter out regular source roots, there should
        //be better way to do this... (Hint: use UnitTestForSourceQuery)
        //${test - Ant based projects
        //2TestSourceRoot - Maven projects
        List<SourceGroup> result = new ArrayList<SourceGroup>(2);
        for (SourceGroup sg : groups) {
            if (sg.getName().startsWith("${test") || "2TestSourceRoot".equals(sg.getName())) { //NOI18N
                result.add(sg);
            }            
        }
        return result.toArray(new SourceGroup[0]);
    }
}
