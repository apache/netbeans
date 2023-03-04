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
package org.netbeans.modules.websvc.core.dev.wizard;

import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.ProjectInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.core.CreatorProvider;
import org.netbeans.modules.websvc.core.HandlerCreator;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.support.SourceGroups;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.core.WSStackUtils;
import org.openide.filesystems.FileObject;

public class MessageHandlerWizard implements WizardDescriptor.InstantiatingIterator {

    public int currentPanel = 0;
    private WizardDescriptor.Panel[] wizardPanels;
    private WizardDescriptor.Panel<WizardDescriptor> firstPanel; //special case: use Java Chooser
    private WizardDescriptor wiz;
    private Project project;
    public static final String JAVAC_CLASSPATH = "javac.classpath"; //NOI18N

    public static MessageHandlerWizard create() {
        return new MessageHandlerWizard();
    }
    private static final String[] HANDLER_STEPS = new String[]{NbBundle.getMessage(MessageHandlerWizard.class, "LBL_SpecifyHandlerInfo")};

    public void initialize(WizardDescriptor wizard) {

        wiz = wizard;
        project = Templates.getProject(wiz);
        SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);

        //create the Java Project chooser
        if (sourceGroups.length == 0) {
            SourceGroup[] genericSourceGroups = ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC);
            firstPanel = new FinishableProxyWizardPanel(Templates.createSimpleTargetChooser(project, genericSourceGroups, new BottomPanel()), sourceGroups, false);
        } else {
            firstPanel = new FinishableProxyWizardPanel(JavaTemplates.createPackageChooser(project, sourceGroups, new BottomPanel(), true));
        }
        JComponent c = (JComponent) firstPanel.getComponent();
        Utils.changeLabelInComponent(c, NbBundle.getMessage(MessageHandlerWizard.class, "LBL_JavaTargetChooserPanelGUI_ClassName_Label"), NbBundle.getMessage(MessageHandlerWizard.class, "LBL_Handler_Name")); //NOI18N
        c.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, HANDLER_STEPS);
        c.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, 0);
        c.getAccessibleContext().setAccessibleDescription(HANDLER_STEPS[0]);
        wizardPanels = new WizardDescriptor.Panel[]{firstPanel};
    }

    public void uninitialize(WizardDescriptor wizard) {
    }

    public Set instantiate() throws IOException {
        //new WebServiceCreator(project, wiz).createMessageHandler();
        HandlerCreator creator = CreatorProvider.getHandlerCreator(project, wiz);
        if (creator != null) {
            creator.createMessageHandler();

            // logging usage of wizard
            Object[] params = new Object[5];
            String creatorClassName = creator.getClass().getName();
            params[0] = creatorClassName.contains("jaxrpc") ? LogUtils.WS_STACK_JAXRPC : LogUtils.WS_STACK_JAXWS; //NOI18N
            params[1] = project.getClass().getName();
            J2eeModule j2eeModule = JaxWsUtils.getJ2eeModule(project);
            params[2] = j2eeModule == null ? "J2SE" : j2eeModule.getModuleVersion()+"("+JaxWsUtils.getModuleType(project)+")"; //NOI18N
            params[3] = "MESSAGE HANDLER"; //NOI18N
            LogUtils.logWsWizard(params);
        }
        return Collections.EMPTY_SET;
    }

    public WizardDescriptor.Panel current() {
        return wizardPanels[currentPanel];
    }

    public boolean hasNext() {
        return currentPanel < wizardPanels.length - 1;
    }

    public boolean hasPrevious() {
        return currentPanel > 0;
    }

    public String name() {
        return NbBundle.getMessage(MessageHandlerWizard.class, "LBL_Create_MessageHandler_Title"); //NOI18N
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        currentPanel++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        currentPanel--;
    }

    public void addChangeListener(javax.swing.event.ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    protected int getCurrentPanelIndex() {
        return currentPanel;
    }

    /** Dummy implementation of WizardDescriptor.Panel required in order to provide Help Button
     */
    private class BottomPanel implements WizardDescriptor.Panel<WizardDescriptor> {

        public void storeSettings(WizardDescriptor settings) {
        }

        public void readSettings(WizardDescriptor settings) {
        }

        public java.awt.Component getComponent() {
            return new javax.swing.JPanel();
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public boolean isValid() {
            ProjectInfo creator = new ProjectInfo(project);
            int projectType = creator.getProjectType();

            //test for conditions in JSE
            if (projectType == ProjectInfo.JSE_PROJECT_TYPE) {
                return isValidInJavaProject(project, wiz);
            }

            //if platform is Tomcat, source level must be jdk 1.5 and jaxws library must be in classpath
            WSStackUtils wsStackUtils = new WSStackUtils(project);
            if (!ProjectUtil.isJavaEE5orHigher(project) && projectType == ProjectInfo.WEB_PROJECT_TYPE && !wsStackUtils.isJsr109Supported() && !wsStackUtils.isJsr109OldSupported()) {
                if (!wsStackUtils.hasJAXWSLibrary()) {
                    //must have jaxws library
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(BottomPanel.class, "LBL_LogicalHandlerWarning")); // NOI18N
                    return false;
                } else {
                    return true;
                }
            }
            // else check the JAXRPC support installation
            if (!ProjectUtil.isJavaEE5orHigher(project) && ((WebServicesSupport.getWebServicesSupport(project.getProjectDirectory()) == null) && (WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory()) == null))) {
                // check if jaxrpc plugin installed
                wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(MessageHandlerWizard.class, "ERR_NoJaxrpcPluginFoundHandler")); // NOI18N
                return false;
            }
            return true;
        }

        public HelpCtx getHelp() {
            return new HelpCtx(MessageHandlerWizard.class);
        }
    }

    public static boolean isValidInJavaProject(Project javaProject, WizardDescriptor wiz) {

        SourceGroup[] sgs = ProjectUtils.getSources(javaProject).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath;
        FileObject handlerFO = null;
        if (sgs.length > 0) {
            classPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.BOOT);
            if (classPath != null) {
                handlerFO = classPath.findResource("javax/xml/ws/handler/Handler.class");  //NOI18N
            }
            if(handlerFO == null){
                classPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.COMPILE);
                if(classPath != null){
                    handlerFO = classPath.findResource("javax/xml/ws/handler/Handler.class");  //NOI18N
                }
            }
        }

        if (handlerFO == null) {
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(MessageHandlerWizard.class, "ERR_HandlerNeedProperLibraries")); // NOI18N
            return false;
        }
        return true;
    }
}
