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
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.*;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.testng.api.TestNGSupport;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

@TemplateRegistration(folder = "UnitTests", position = 1200,
        content = "../resources/TestNGSuite.xml.template",
        scriptEngine = "freemarker",
        displayName = "#TestNGSuite_displayName",
        description = "/org/netbeans/modules/testng/ui/resources/newTestSuite.html",
        iconBase = "org/netbeans/modules/testng/ui/resources/testng.gif",
        category="junit")
@NbBundle.Messages("TestNGSuite_displayName=TestNG Test Suite")
public final class NewTestSuiteWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public NewTestSuiteWizardIterator() {
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
        }
        return new WizardDescriptor.Panel[]{
                        JavaTemplates.createPackageChooser(project, groups)
                    };
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

    public Set<DataObject> instantiate() throws IOException {
        FileObject targetFolder = Templates.getTargetFolder(wiz);
        TestNGSupport.findTestNGSupport(FileOwnerQuery.getOwner(targetFolder)).configureProject(targetFolder);
        String targetName = Templates.getTargetName(wiz);

        DataFolder df = DataFolder.findFolder(targetFolder);
        FileObject template = Templates.getTemplate(wiz);

        DataObject dTemplate = DataObject.find(template);
        String pkgName = getSelectedPackageName(targetFolder);
        String suiteName = pkgName + " suite";
        String projectName = ProjectUtils.getInformation(FileOwnerQuery.getOwner(targetFolder)).getName();
        if (pkgName == null || pkgName.trim().length() < 1) {
            pkgName = ".*"; //NOI18N
            suiteName = "All tests for " + projectName;
        }
        
        Map<String, String> props = new HashMap<String, String>();
        props.put("suiteName", projectName);
        props.put("testName", suiteName);
        props.put("pkg", pkgName);

        DataObject dobj = dTemplate.createFromTemplate(df, targetName, props);

        return Collections.singleton(dobj);
    }   
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels(wiz);
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
