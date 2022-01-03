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

package org.netbeans.modules.cnd.simpleunit.editor.filecreation;

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
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LinkerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.simpleunit.codegeneration.CodeGenerator;
import org.netbeans.modules.cnd.simpleunit.spi.wizard.AbstractUnitTestIterator;
import org.netbeans.modules.cnd.simpleunit.utils.MakefileUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
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
 */
public class TestSimpleIterator extends AbstractUnitTestIterator {
    private WizardDescriptor.Panel<WizardDescriptor> targetChooserDescriptorPanel;

    private static final String C_HEADER_MIME_TYPE = "text/x-c/text/x-h"; // NOI18N

    @Override
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        UIGesturesSupport.submit("USG_CND_UNIT_TESTS_SIMPLE_" + wiz.getTemplate().getPrimaryFile().getExt().toUpperCase()); //NOI18N

        Project project = Templates.getProject(wiz);

        Set<DataObject> dataObjects = new HashSet<DataObject>();

        if(getTestFileName() == null || getTestName() == null) {
            return dataObjects;
        }

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
        params.putAll(CodeGenerator.generateTemplateParamsForFunctions(
                getTestFileName().replaceFirst("[.].*", ""), // NOI18N
                rootFolderFilePath,
                fs,
                ("cpp".equals(wiz.getTemplate().getPrimaryFile().getExt())?CodeGenerator.Language.CPP:CodeGenerator.Language.C))); // NOI18N

        DataObject formDataObject = NewTestSimplePanel.getTemplateDataObject(
                "simpletest." + wiz.getTemplate().getPrimaryFile().getExt()); // NOI18N
        
        DataObject dataObject = formDataObject.createFromTemplate(targetFolder, getTestFileName(), params);

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
        
        setOptions(project, folder);

        addItemToLogicalFolder(project, folder, dataObject);

        dataObjects.add(dataObject);
        return dataObjects;
    }

    protected WizardDescriptor.Panel<WizardDescriptor>[] createPanels() {
        if (targetChooserDescriptorPanel == null) {
            TemplateWizard wiz = getWizard();
            DataObject dobj = wiz.getTemplate();
            FileObject fobj = dobj.getPrimaryFile();
            String mimeType = fobj.getMIMEType();
            MIMEExtensions extensions = MIMEExtensions.get(mimeType);
            if (extensions != null) {
                Project project = Templates.getProject(getWizard());
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                if (MIMENames.HEADER_MIME_TYPE.equals(extensions.getMIMEType())) {
                    // this is the only place where we want to differ c headers from cpp headers (creation of new one)
                    if (dobj.getPrimaryFile().getAttribute(C_HEADER_MIME_TYPE) != null) {
                        MIMEExtensions cHeaderExtensions = MIMEExtensions.get(C_HEADER_MIME_TYPE);
                        if ((cHeaderExtensions == null) || !C_HEADER_MIME_TYPE.equals(cHeaderExtensions.getMIMEType())) {
                            System.err.println("not found extensions for C Headers"); // NOI18N
                        } else {
                            extensions = cHeaderExtensions;
                        }
                    }
                }
                String defaultExt = null; // let the chooser panel decide default extension
                if (mimeType.equals(MIMENames.SHELL_MIME_TYPE)) {
                    // for shell scripts set default extension explicitly
                    defaultExt = fobj.getExt();
                } else if (mimeType.equals(MIMENames.HEADER_MIME_TYPE) && fobj.getExt().length() == 0) {
                    // for standard header without extension
                    defaultExt = fobj.getExt();
                }

                targetChooserDescriptorPanel = new NewTestSimplePanel(project, groups, null, extensions, defaultExt,
                    (String) wiz.getProperty(CND_UNITTEST_DEFAULT_NAME));
            } else {
                targetChooserDescriptorPanel = getWizard().targetChooser();
            }
        }
        @SuppressWarnings("unchecked")
        Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[]{targetChooserDescriptorPanel};
        return panels;
    }

    private String getTestFileName() {
        return ((NewTestSimplePanelGUI)targetChooserDescriptorPanel.getComponent()).getTestFileName();
    }

    private String getTestName() {
        return ((NewTestSimplePanelGUI)targetChooserDescriptorPanel.getComponent()).getTestName();
    }

    private FileObject getRootFolder() {
        return ((NewTestSimplePanelGUI)targetChooserDescriptorPanel.getComponent()).getTargetGroup().getRootFolder();
    }

    private void setOptions(Project project, Folder testFolder) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor projectDescriptor = cdp.getConfigurationDescriptor();
        for (Configuration cfg : projectDescriptor.getConfs().getConfigurations()) {
            FolderConfiguration folderConfiguration = testFolder.getFolderConfiguration(cfg);
            LinkerConfiguration linkerConfiguration = folderConfiguration.getLinkerConfiguration();
            linkerConfiguration.getOutput().setValue("${TESTDIR}/" + testFolder.getPath()); // NOI18N
            CCompilerConfiguration cCompilerConfiguration = folderConfiguration.getCCompilerConfiguration();
            CCCompilerConfiguration ccCompilerConfiguration = folderConfiguration.getCCCompilerConfiguration();
            cCompilerConfiguration.getIncludeDirectories().add("."); // NOI18N
            ccCompilerConfiguration.getIncludeDirectories().add("."); // NOI18N
        }

    }
}
