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
package org.netbeans.modules.cnd.cncppunit.editor.filecreation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.cncppunit.codegeneration.CppUnitCodeGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibrariesConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.LinkerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.simpleunit.spi.wizard.AbstractUnitTestIterator;
import org.netbeans.modules.cnd.simpleunit.utils.MakefileUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.CreateFromTemplateHandler;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/**
 *
 */
public class TestCppUnitIterator extends AbstractUnitTestIterator {
    private WizardDescriptor.Panel<WizardDescriptor> targetChooserDescriptorPanel;

    @Override
    public void initialize(TemplateWizard wiz) {
        super.initialize(wiz);
        Project project = Templates.getProject(wiz);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        targetChooserDescriptorPanel = new NewTestCppUnitPanel(project, groups, null,
                (String) wiz.getProperty(CND_UNITTEST_DEFAULT_NAME));
        wiz.putProperty(CND_UNITTEST_KIND, CND_UNITTEST_KIND_CPPUNIT);
    }

    @Override
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        UIGesturesSupport.submit("USG_CND_UNIT_TESTS_CPPUNIT"); //NOI18N

        Set<DataObject> dataObjects = new HashSet<DataObject>();

        if(getTestName() == null) {
            return dataObjects;
        }
        Project project = Templates.getProject(wiz);

        DataFolder targetFolder = wiz.getTargetFolder();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CreateFromTemplateHandler.FREE_FILE_EXTENSION, true);

        List<CsmFunction> fs = new ArrayList<CsmFunction>();
        Object listObj = wiz.getProperty(CND_UNITTEST_FUNCTIONS);
        if(listObj instanceof List<?>) {
            List<?> list = (List<?>) listObj;
            for (Object obj : list) {
                if(obj instanceof CsmFunction) {
                    fs.add((CsmFunction)obj);
                }
            }
        }

        FileObject rootFolder = getRootFolder();
        FSPath rootFolderFilePath = FSPath.toFSPath(rootFolder);        
        params.putAll(CppUnitCodeGenerator.generateTemplateParamsForFunctions(
                getTestClassName(),
                rootFolderFilePath,
                fs));

        String headerName = getTestClassHeaderFileName(); //NOI18N
        StringBuilder guardName = new StringBuilder();
        for (int i = 0; i < headerName.length(); i++) {
            char c = headerName.charAt(i);
            guardName.append(Character.isJavaIdentifierPart(c) ? Character.toUpperCase(c) : '_');
        }
        params.put("guardName", guardName.toString()); // NOI18N
        params.put("className", getTestClassName()); // NOI18N
        params.put("headerNameAndExt", headerName); // NOI18N

        Folder folder = null;
        Folder testsRoot = getTestsRootFolder(project);
        if(testsRoot == null) {
            testsRoot = createTestsRootFolder(project);
            MakefileUtils.createTestTargets(project);
        }
        if(testsRoot != null) {
            Folder newFolder = testsRoot.addNewFolder(true, Folder.Kind.TEST);
            newFolder.setDisplayName(getTestName());
            folder = newFolder;
        }

        if(folder == null) {
            return dataObjects;
        }

        setCppUnitOptions(project, folder);

        DataObject formDataObject;
        DataObject dataObject;

        formDataObject = NewTestCppUnitPanel.getTemplateDataObject("cppunittestclassfile.h"); // NOI18N
        dataObject = formDataObject.createFromTemplate(targetFolder, getTestClassHeaderFileName(), params);
        addItemToLogicalFolder(project, folder, dataObject);
        //dataObjects.add(dataObject);

        formDataObject = NewTestCppUnitPanel.getTemplateDataObject("cppunittestclassfile.cpp"); // NOI18N
        dataObject = formDataObject.createFromTemplate(targetFolder, getTestClassSourceFileName(), params);
        addItemToLogicalFolder(project, folder, dataObject);
        dataObjects.add(dataObject);

        formDataObject = NewTestCppUnitPanel.getTemplateDataObject("cppunittestrunnerfile.cpp"); // NOI18N
        dataObject = formDataObject.createFromTemplate(targetFolder, getTestRunnerFileName(), params);
        addItemToLogicalFolder(project, folder, dataObject);
        //dataObjects.add(dataObject);

        return dataObjects;
    }

    private String getTestClassName() {
        return ((NewTestCppUnitPanelGUI)targetChooserDescriptorPanel.getComponent()).getClassName();
    }

    private String getTestClassSourceFileName() {
        return ((NewTestCppUnitPanelGUI)targetChooserDescriptorPanel.getComponent()).getSourceFileName();
    }

    private String getTestClassHeaderFileName() {
        return ((NewTestCppUnitPanelGUI)targetChooserDescriptorPanel.getComponent()).getHeaderFileName();
    }

    private String getTestRunnerFileName() {
        return ((NewTestCppUnitPanelGUI)targetChooserDescriptorPanel.getComponent()).getRunnerFileName();
    }

    private String getTestName() {
        return ((NewTestCppUnitPanelGUI)targetChooserDescriptorPanel.getComponent()).getTestName();
    }

    private FileObject getRootFolder() {
        return ((NewTestCppUnitPanelGUI)targetChooserDescriptorPanel.getComponent()).getTargetGroup().getRootFolder();
    }

    private void setCppUnitOptions(Project project, Folder testFolder) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor projectDescriptor = cdp.getConfigurationDescriptor();
        for (Configuration cfg : projectDescriptor.getConfs().getConfigurations()) {
            FolderConfiguration folderConfiguration = testFolder.getFolderConfiguration(cfg);
            LinkerConfiguration linkerConfiguration = folderConfiguration.getLinkerConfiguration();
            LibrariesConfiguration librariesConfiguration = linkerConfiguration.getLibrariesConfiguration();
            librariesConfiguration.add(new LibraryItem.OptionItem("`cppunit-config --libs`")); // NOI18N
            linkerConfiguration.getOutput().setValue("${TESTDIR}/" + testFolder.getPath()); // NOI18N
            CCompilerConfiguration cCompilerConfiguration = folderConfiguration.getCCompilerConfiguration();
            CCCompilerConfiguration ccCompilerConfiguration = folderConfiguration.getCCCompilerConfiguration();
            cCompilerConfiguration.getCommandLineConfiguration().setValue("`cppunit-config --cflags`"); // NOI18N;
            ccCompilerConfiguration.getCommandLineConfiguration().setValue("`cppunit-config --cflags`"); // NOI18N;
        }
    }

    @Override
    protected Panel<WizardDescriptor>[] createPanels() {
        @SuppressWarnings("unchecked")
        Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[]{targetChooserDescriptorPanel};
        return panels;
    }

}
