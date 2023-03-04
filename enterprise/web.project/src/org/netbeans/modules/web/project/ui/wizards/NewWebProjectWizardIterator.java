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

package org.netbeans.modules.web.project.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.api.progress.ProgressHandle;

import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectLocationWizardPanel;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectServerWizardPanel;
import org.netbeans.modules.web.api.webmodule.WebFrameworks;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.api.WebProjectCreateData;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.modules.javaee.project.api.ui.UserProjectSettings;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.web.project.Utils;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;

/**
 * Wizard to create a new Web project.
 * @author Jesse Glick, Radko Najman
 */
public class NewWebProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;

    /** Create a new wizard iterator. */
    public NewWebProjectWizardIterator() {}
        
    private String[] createSteps() {
	String[] steps;
	if (WebFrameworks.getFrameworks().size() > 0)
	    steps = new String[] {
		NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"), //NOI18N
                NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP1_ProjectServer"),
		NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP2_Frameworks") //NOI18N
	    };
	else
	    steps = new String[] {
		NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"), //NOI18N
                NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP1_ProjectServer")
	    };
	
        return steps;
    }
    
    public Set instantiate() throws IOException {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    boolean isIstantiating = false;

    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        isIstantiating = true;
        handle.start(4);
        handle.progress(NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NewWebProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        
        Set<FileObject> resultSet = new HashSet<FileObject>();

        File dirF = (File) wiz.getProperty(ProjectLocationWizardPanel.PROJECT_DIR);
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }

        String servInstID = (String) wiz.getProperty(ProjectServerWizardPanel.SERVER_INSTANCE_ID);
        
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(dirF);
        createData.setName((String) wiz.getProperty(ProjectLocationWizardPanel.NAME));
        createData.setServerInstanceID(servInstID);
        if (createData.getSourceStructure() == null) {
            createData.setSourceStructure(WebProjectUtilities.SRC_STRUCT_BLUEPRINTS);
        }
        createData.setJavaEEProfile((Profile) wiz.getProperty(ProjectServerWizardPanel.J2EE_LEVEL));
        createData.setContextPath((String) wiz.getProperty(ProjectServerWizardPanel.CONTEXT_PATH));
        createData.setJavaPlatformName((String) wiz.getProperty(ProjectServerWizardPanel.JAVA_PLATFORM));
        createData.setSourceLevel((String) wiz.getProperty(ProjectServerWizardPanel.SOURCE_LEVEL));
        
        createData.setLibrariesDefinition(
                SharabilityUtility.getLibraryLocation((String) wiz.getProperty(ProjectServerWizardPanel.WIZARD_SHARED_LIBRARIES)));
        createData.setWebXmlRequired(checkFrameworksForWebXml());
        createData.setCDIEnabled((Boolean)wiz.getProperty(ProjectServerWizardPanel.CDI));
        
        AntProjectHelper h = WebProjectUtilities.createProject(createData);
        handle.progress(2);
        
        FileObject dir = FileUtil.toFileObject(dirF);

        wiz.putProperty(ProjectLocationWizardPanel.NAME, null); // reset project name

        Project earProject = (Project) wiz.getProperty(ProjectServerWizardPanel.EAR_APPLICATION);
        
        WebProject createdWebProject = (WebProject) ProjectManager.getDefault().findProject(dir);
        if (earProject != null && createdWebProject != null) {
            Ear ear = Ear.getEar(earProject.getProjectDirectory());
            if (ear != null) {
                ear.addWebModule(createdWebProject.getAPIWebModule());
            }
        }

        //remember last used server
        UserProjectSettings.getDefault().setLastUsedServer(servInstID);
        SharableLibrariesUtils.setLastProjectSharable(createData.getLibrariesDefinition() != null);
	
        // save last project location
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);
        }

        resultSet.add(dir);

        WebModule apiWebModule = null;
        if (createdWebProject != null) {
            apiWebModule = createdWebProject.getAPIWebModule();
        }
        //add framework extensions
        List selectedExtenders = (List) wiz.getProperty(WizardProperties.EXTENDERS);
        if (selectedExtenders != null && apiWebModule != null) {
            handle.progress(NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NewWebProjectWizardIterator_WizardProgress_AddingFrameworks"), 3);
            for(int i = 0; i < selectedExtenders.size(); i++) {
                Set<FileObject> o = ((WebModuleExtender) selectedExtenders.get(i)).extend(apiWebModule);
                if (o != null) {
                    resultSet.addAll(o);
                }
            }
        }

        FileObject webRoot = h.getProjectDirectory().getFileObject("web");//NOI18N
        if (apiWebModule != null) {
            FileObject dd = apiWebModule.getDeploymentDescriptor();
            resultSet.addAll(WebProjectUtilities.ensureWelcomePage(webRoot, dd, createData.getJavaEEProfile()));
        }
        
        handle.progress(NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NewWebProjectWizardIterator_WizardProgress_PreparingToOpen"), 4);

        
        List <String> selectedFrameworkNames = (List<String>) wiz.getProperty(WizardProperties.FRAMEWORK_NAMES);
        int frameworkCount = (selectedFrameworkNames != null) ? selectedFrameworkNames.size() : 0;
        Object[] parameters = new Object [5 + frameworkCount];
        parameters[0] = Deployment.getDefault().getServerID( createData.getServerInstanceID());
        parameters[1] = createData.getServerInstanceID();
        parameters[2] = createData.getJavaEEVersion();
        parameters[3] = createData.getSourceLevel();
        parameters[4] = createData.getSourceStructure();
        if (selectedFrameworkNames != null) {
            for (int i = 0; i < selectedFrameworkNames.size(); i++) {
                parameters[5 + i] = selectedFrameworkNames.get(i);
            }
        }
        
        Utils.logUI(NbBundle.getBundle(NewWebProjectWizardIterator.class),
                "UI_WEB_PROJECT_CREATE", parameters); // NOI18N

        Object[] parameters2 = new Object[5];
        parameters2[0] = ""; // NOI18N
        try {
            if (servInstID != null) {
                parameters2[0] = Deployment.getDefault().getServerInstance(servInstID).getServerDisplayName();
            }
        }
        catch (InstanceRemovedException ire) {
            // ignore
        }
        parameters2[1] = createData.getJavaEEVersion();
        parameters2[2] = createData.getSourceLevel();
        parameters2[3] = createData.getSourceStructure();
        StringBuffer sb = new StringBuffer(50);
        if (selectedFrameworkNames != null) {
            for (int i = 0; i < selectedFrameworkNames.size(); i++) {
                sb.append(selectedFrameworkNames.get(i));
                if ((i + 1) < selectedFrameworkNames.size()) {
                    sb.append("|"); // NOI18N
                }
            }
        }
        parameters2[4] = sb;
        Utils.logUsage(NewWebProjectWizardIterator.class, "USG_PROJECT_CREATE_WEB", parameters2); // NOI18N
        
        // Returning set of FileObject of project diretory. 
        // Project will be open and set as main
        return resultSet;
    }
    
    private transient int index;
    private transient int panelsCount;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;

	if (WebFrameworks.getFrameworks().size() > 0)
	    //standard panels + configurable framework panel
	    panels = new WizardDescriptor.Panel[] {
                new ProjectLocationWizardPanel(J2eeModule.WAR, 
                        NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"),
                        NbBundle.getMessage(NewWebProjectWizardIterator.class, "TXT_NewWebApp"),
                        NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NPW1_DefaultProjectName")),
                new ProjectServerWizardPanel(J2eeModule.WAR, 
                        NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP1_ProjectServer"),
                        NbBundle.getMessage(NewWebProjectWizardIterator.class, "TXT_NewWebApp"),
                        true, false, true, false, false, true),
		new PanelSupportedFrameworks(this)
	    };
	else
	    //no framework available, don't show framework panel
	    panels = new WizardDescriptor.Panel[] {
                new ProjectLocationWizardPanel(J2eeModule.WAR, 
                        NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"),
                        NbBundle.getMessage(NewWebProjectWizardIterator.class, "TXT_NewWebApp"),
                        NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NPW1_DefaultProjectName")),
                new ProjectServerWizardPanel(J2eeModule.WAR, 
                        NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP1_ProjectServer"),
                        NbBundle.getMessage(NewWebProjectWizardIterator.class, "TXT_NewWebApp"),
                        true, false, true, false, false, true),
	    };
        panelsCount = panels.length;
        
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < steps.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        if (this.wiz != null) {
            this.wiz.putProperty(ProjectLocationWizardPanel.PROJECT_DIR,null);
            this.wiz.putProperty(ProjectLocationWizardPanel.NAME,null);
            this.wiz = null;
        }
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_WizardStepsCount"), Integer.toString(index + 1), Integer.toString(panels.length)); //NOI18N
    }
    
    public boolean hasNext() {
        return index < panelsCount - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        // To be able to trace #240974 a bit better, adding actual values
        if (!hasNext()) {
            StringBuilder sb = new StringBuilder();
            sb.append("panelsCount: ");
            sb.append(panelsCount);
            sb.append("\n panels size: ");
            sb.append(panels.length);
            throw new NoSuchElementException(sb.toString());
        }
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}

    private boolean checkFrameworksForWebXml() {
        boolean res = false;

        List<String> frameworkNames = (List<String>)wiz.getProperty(WizardProperties.FRAMEWORK_NAMES);
        if (frameworkNames != null) {
            for (String fName : frameworkNames) {
                for (WebFrameworkProvider wfp : WebFrameworks.getFrameworks()) {
                    if (wfp.getName().equals(fName)) {
                        res |= wfp.requiresWebXml();
                        break;
                    }
                }
            }
        }
        return res;
    }

}
