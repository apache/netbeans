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

package org.netbeans.modules.junit.ui.wizards;

import java.awt.Component;
import org.netbeans.modules.junit.api.JUnitUtils;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.java.testrunner.GuiUtils;
import org.netbeans.modules.junit.api.JUnitSettings;
import org.netbeans.modules.junit.api.JUnitTestUtil;
import org.netbeans.modules.junit.plugin.JUnitPlugin;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin.CreateTestParam;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 */
@TemplateRegistration(folder = "UnitTests", position = 300, scriptEngine = "freemarker",
        displayName = "org.netbeans.modules.junit.ui.Bundle#Templates/UnitTests/SimpleJUnitTest.java",
        iconBase = "org/netbeans/modules/junit/ui/resources/JUnitLogo.png",
        description = "/org/netbeans/modules/junit/ui/resources/SimpleJUnitTest.html", category = "junit")
public class SimpleTestCaseWizardIterator implements TemplateWizard.InstantiatingIterator<WizardDescriptor> {

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wizard;    
    /** */
    private Project lastSelectedProject = null;
    /** panel for choosing name and target location of the test class */
    private WizardDescriptor.Panel<WizardDescriptor> classChooserPanel;
    
    public SimpleTestCaseWizardIterator() {
    }

    @Override
    public String name() {
        return ""; // NOI18N
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    @Override public final void addChangeListener(ChangeListener l) { }

    @Override public final void removeChangeListener(ChangeListener l) { }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        index = 0;
        panels = createPanels(wizard);
        FileObject targetFolder = Templates.getTargetFolder(wizard);
//	DataFolder targetFolder = null;
//	try {
//	    targetFolder = wizard.getTargetFolder();
//	} catch (IOException ex) {
//	    Exceptions.printStackTrace(ex);
//	}
	
        loadSettings(wizard);
        // Make sure list of steps is accurate.
        String[] beforeSteps = null;
        Object prop = wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
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
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }

//        String [] panelNames =  new String [] {
//          NbBundle.getMessage(EmptyTestCaseWizardIterator.class,"LBL_panel_chooseFileType"),
//          NbBundle.getMessage(EmptyTestCaseWizardIterator.class,"LBL_panel_ChooseClass")};
//
//        ((javax.swing.JComponent)getClassChooserPanel().getComponent()).putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, panelNames); 
//        ((javax.swing.JComponent)getClassChooserPanel().getComponent()).putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(0));
        if(targetFolder == null) {
            // No test folders yet? Let's try one more time.
            targetFolder = Templates.getTargetFolder(wizard);
//            try {
//                targetFolder = wizard.getTargetFolder();
//            } catch (IOException ex1) {
//                Exceptions.printStackTrace(ex1);
//            }
        }
        WizardDescriptor.Panel<WizardDescriptor> chooserPanel = getClassChooserPanel();
        if(chooserPanel instanceof SimpleTestStepLocation) {
            if(targetFolder != null) {// No test folders yet. IOExceptions already loggged.
                ((SimpleTestStepLocation) chooserPanel).selectLocation(targetFolder);
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        if ((classChooserPanel != null)
                && !(classChooserPanel instanceof StepProblemMessage)) {
            
            assert classChooserPanel instanceof SimpleTestStepLocation;
            
            ((SimpleTestStepLocation) classChooserPanel).cleanUp();
        }
        classChooserPanel = null;
        this.wizard = null;
    }

    @Override
    public Set<DataObject> instantiate() throws IOException {
        saveSettings(wizard);
        
        /* collect and build necessary data: */
        FileObject classToTest = (FileObject)
                wizard.getProperty(SimpleTestCaseWizard.PROP_CLASS_TO_TEST);
        FileObject testRootFolder = (FileObject)
                wizard.getProperty(SimpleTestCaseWizard.PROP_TEST_ROOT_FOLDER);
        Map<CreateTestParam, Object> params
                = JUnitTestUtil.getSettingsMap(false);
                
        /* create test class(es) for the selected source class: */
        JUnitPlugin plugin = JUnitTestUtil.getPluginForProject(
                                                Templates.getProject(wizard));
        
        if (!JUnitTestUtil.createTestActionCalled(
                                            plugin,
                                            new FileObject[] {classToTest})) {
            return null;
        }

        /*
         * The JUnitPlugin instance must be initialized _before_ field
         * JUnitPluginTrampoline.DEFAULT gets accessed.
         * See issue #74744.
         */
        final FileObject[] testFileObjects
                = JUnitTestUtil.createTests(
                     plugin,
                     new FileObject[] {classToTest},
                     testRootFolder,
                     params);
        
        //XXX: What if the selected class is not testable?
        //     It should not be skipped!
        
        if (testFileObjects == null) {
            throw new IOException();
        }
        
        final Set<DataObject> dataObjects
               = new HashSet<DataObject>((int) (testFileObjects.length * 1.5f));
        for (FileObject testFile : testFileObjects) {
            try {
                dataObjects.add(DataObject.find(testFile));
            } catch (DataObjectNotFoundException ex) {
                //XXX - does nothing special - just continues
            }
        }
        
        if (dataObjects.isEmpty()) {
            throw new IOException();
        }
        return dataObjects;
    }
    
    private WizardDescriptor.Panel[] createPanels(final WizardDescriptor wizardDescriptor) {
        return new WizardDescriptor.Panel[]{getClassChooserPanel()};
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

    /**
     * Returns a panel for choosing name and target location of the test
     * class. If the panel already exists, returns the existing panel,
     * otherwise creates a new panel.
     *
     * @return  existing panel or a newly created panel if it did not exist
     */
    private WizardDescriptor.Panel<WizardDescriptor> getClassChooserPanel() {
        final Project project = Templates.getProject(wizard);
        if (classChooserPanel == null || project != lastSelectedProject) {
            SourceGroupModifier.createSourceGroup(project, JavaProjectConstants.SOURCES_TYPE_JAVA, JavaProjectConstants.SOURCES_HINT_TEST);
            final JUnitUtils utils = new JUnitUtils(project);
            if (utils.getSourcesToTestsMap(true).isEmpty()) {
                classChooserPanel = new StepProblemMessage(
                        project,
                        NbBundle.getMessage(EmptyTestCaseWizardIterator.class,
                                            "MSG_NoTestSourceGroup"));  //NOI18N
            } else {
                if (classChooserPanel == null) {
                    classChooserPanel = new SimpleTestStepLocation();
                }
                ((SimpleTestStepLocation) classChooserPanel).setUp(utils);
            }
        }
        lastSelectedProject = project;
        return classChooserPanel;
    }
    
    private void loadSettings(WizardDescriptor wizard) {
        JUnitSettings settings = JUnitSettings.getDefault();
        
        wizard.putProperty(GuiUtils.CHK_PUBLIC, settings.isMembersPublic());
        wizard.putProperty(GuiUtils.CHK_PROTECTED, settings.isMembersProtected());
        wizard.putProperty(GuiUtils.CHK_PACKAGE, settings.isMembersPackage());
        wizard.putProperty(GuiUtils.CHK_SETUP, settings.isGenerateSetUp());
        wizard.putProperty(GuiUtils.CHK_TEARDOWN, settings.isGenerateTearDown());
        wizard.putProperty(GuiUtils.CHK_BEFORE_CLASS, settings.isGenerateClassSetUp());
        wizard.putProperty(GuiUtils.CHK_AFTER_CLASS, settings.isGenerateClassTearDown());
        wizard.putProperty(GuiUtils.CHK_METHOD_BODIES, settings.isBodyContent());
        wizard.putProperty(GuiUtils.CHK_JAVADOC, settings.isJavaDoc());
        wizard.putProperty(GuiUtils.CHK_HINTS, settings.isBodyComments());
        wizard.putProperty("NewFileWizard_Title", NbBundle.getMessage(SimpleTestStepLocation.class, "LBL_simpleTestWizard_stepName"));
    }

    private void saveSettings(WizardDescriptor wizard) {
        JUnitSettings settings = JUnitSettings.getDefault();
        
        settings.setMembersPublic(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_PUBLIC)));
        settings.setMembersProtected(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_PROTECTED)));
        settings.setMembersPackage(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_PACKAGE)));
        settings.setGenerateSetUp(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_SETUP)));
        settings.setGenerateTearDown(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_TEARDOWN)));
        settings.setGenerateClassSetUp(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_BEFORE_CLASS)));
        settings.setGenerateClassTearDown(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_AFTER_CLASS)));
        settings.setBodyContent(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_METHOD_BODIES)));
        settings.setJavaDoc(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_JAVADOC)));
        settings.setBodyComments(Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_HINTS)));
    }

}
