/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.websvc.core.dev.wizard;

import org.netbeans.api.project.ProjectUtils;
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
import org.netbeans.modules.websvc.core.WSStackUtils;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.websvc.api.support.SourceGroups;

public class LogicalHandlerWizard implements WizardDescriptor.InstantiatingIterator {
    public int currentPanel = 0;
    private WizardDescriptor.Panel [] wizardPanels;
    private WizardDescriptor.Panel<WizardDescriptor> firstPanel; //special case: use Java Chooser
    private WizardDescriptor wiz;
    private Project project;
    public static final String JAVAC_CLASSPATH = "javac.classpath"; //NOI18N
    
    public static LogicalHandlerWizard create() {
        return new LogicalHandlerWizard();
    }
    
    private static final String [] HANDLER_STEPS =
            new String [] {
        NbBundle.getMessage(LogicalHandlerWizard.class, "LBL_SpecifyLogicalHandlerInfo") //NOI18N
    };
    
    public void initialize(WizardDescriptor wizard) {
        
        wiz = wizard;
        project = Templates.getProject(wiz);
        SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);
        
        //create the Java Project chooser
//        firstPanel = JavaTemplates.createPackageChooser(project, sourceGroups, new BottomPanel());
        
        if (sourceGroups.length == 0) {
            SourceGroup[] genericSourceGroups = ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC);
            firstPanel = new FinishableProxyWizardPanel(Templates.createSimpleTargetChooser(project, genericSourceGroups, new BottomPanel()), sourceGroups, false);
        } else
            firstPanel = new FinishableProxyWizardPanel(JavaTemplates.createPackageChooser(project, sourceGroups, new BottomPanel(), true));
        
        JComponent c = (JComponent) firstPanel.getComponent();
        Utils.changeLabelInComponent(c, NbBundle.getMessage(LogicalHandlerWizard.class, "LBL_JavaTargetChooserPanelGUI_ClassName_Label"), //NOI18N
                NbBundle.getMessage(LogicalHandlerWizard.class, "LBL_LogicalHandler_Name") ); //NOI18N
        c.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, //NOI18N
                HANDLER_STEPS);
        c.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, //NOI18N
                Integer.valueOf(0));
        c.getAccessibleContext().setAccessibleDescription
                (HANDLER_STEPS[0]);
        wizardPanels = new WizardDescriptor.Panel[] {firstPanel};
    }
    
    public void uninitialize(WizardDescriptor wizard) {
    }
    
    public Set instantiate() throws IOException {
        //new WebServiceCreator(project, wiz).createLogicalHandler();
        HandlerCreator creator = CreatorProvider.getHandlerCreator(project, wiz);
        if (creator!=null) {
            creator.createLogicalHandler();

            // logging usage of wizard
            Object[] params = new Object[5];
            String creatorClassName = creator.getClass().getName();
            params[0] = creatorClassName.contains("jaxrpc") ? LogUtils.WS_STACK_JAXRPC : LogUtils.WS_STACK_JAXWS; //NOI18N
            params[1] = project.getClass().getName();
            J2eeModule j2eeModule = JaxWsUtils.getJ2eeModule(project);
            params[2] = j2eeModule == null ? "J2SE" : j2eeModule.getModuleVersion()+"("+JaxWsUtils.getModuleType(project)+")"; //NOI18N
            params[3] = "LOGICAL HANDLER"; //NOI18N
            LogUtils.logWsWizard(params);
        }
        return Collections.EMPTY_SET;
    }
    
    
    public WizardDescriptor.Panel current() {
        return wizardPanels[currentPanel];
    }
    
    public boolean hasNext() {
        return currentPanel < wizardPanels.length -1;
    }
    
    public boolean hasPrevious() {
        return currentPanel > 0;
    }
    
    public String name() {
        return NbBundle.getMessage(LogicalHandlerWizard.class, "LBL_Create_LogicalHandler_Title"); //NOI18N
    }
    
    public void nextPanel() {
        if(!hasNext()){
            throw new NoSuchElementException();
        }
        currentPanel++;
    }
    
    public void previousPanel() {
        if(!hasPrevious()){
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
                return MessageHandlerWizard.isValidInJavaProject(project, wiz);
            }
            /*
            if (projectType == ProjectInfo.JSE_PROJECT_TYPE && Util.isSourceLevel16orHigher(project))
                return true;
            
            if (projectType == ProjectInfo.JSE_PROJECT_TYPE && Util.getSourceLevel(project).equals("1.5")) { //NOI18N
                //test JAX-WS library
                if (!PlatformUtil.hasJAXWSLibrary(project)) {
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(BottomPanel.class, "LBL_LogicalHandlerWarning")); // NOI18N
                    return false;
                } else
                    return true;
            }
            */
            if (ProjectUtil.isJavaEE5orHigher(project) && (projectType == ProjectInfo.WEB_PROJECT_TYPE
                    || projectType == ProjectInfo.CAR_PROJECT_TYPE
                    || projectType == ProjectInfo.EJB_PROJECT_TYPE)) { //NOI18N
                return true;
            }
            
            //if platform is Tomcat, source level must be jdk 1.5 and jaxws library must be in classpath
            WSStackUtils wsStackUtils = new WSStackUtils(project);
            if(!ProjectUtil.isJavaEE5orHigher(project) && projectType == ProjectInfo.WEB_PROJECT_TYPE
                    && !wsStackUtils.isJsr109Supported() 
                    && !wsStackUtils.isJsr109OldSupported() ){
                if (!wsStackUtils.hasJAXWSLibrary()) { //must have jaxws library
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(BottomPanel.class, "LBL_LogicalHandlerWarning")); // NOI18N
                    return false;
                } else
                    return true;
            }
            
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(BottomPanel.class, "LBL_LogicalHandlerWarning")); // NOI18N
            
            return false;
        }
        
        public HelpCtx getHelp() {
            return new HelpCtx(LogicalHandlerWizard.class);
        }
    }
}
