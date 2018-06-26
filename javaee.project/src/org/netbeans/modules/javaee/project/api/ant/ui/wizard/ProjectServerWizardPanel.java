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

package org.netbeans.modules.javaee.project.api.ant.ui.wizard;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * Panel just asking for basic info.
 */
public final class ProjectServerWizardPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
    
    public static final String SERVER_INSTANCE_ID = "serverInstanceID"; //NOI18N
    public static final String J2EE_LEVEL = "j2eeLevel"; //NOI18N
    public static final String CONTEXT_PATH = "contextPath"; //NOI18N
    public static final String EAR_APPLICATION = "earApplication"; //NOI18N
    public static final String JAVA_PLATFORM = "setJavaPlatform"; // NOI18N
    public static final String SOURCE_LEVEL = "setSourceLevel"; // NOI18N
    public static final String WIZARD_SHARED_LIBRARIES = "sharedLibraries"; // NOI18N
    public static final String MAIN_CLASS = "mainClass"; // NOI18N
    public static final String WAR_NAME = "warName"; // NOI18N
    public static final String JAR_NAME = "jarName"; // NOI18N
    public static final String CAR_NAME = "carName"; // NOI18N
    public static final String CREATE_WAR = "createWAR"; // NOI18N
    public static final String CREATE_JAR = "createJAR"; // NOI18N
    public static final String CREATE_CAR = "createCAR"; // NOI18N
    public static final String CDI = "cdi"; // NOI18N
    
    WizardDescriptor wizardDescriptor;
    private ProjectServerPanel component;
    
    private boolean finishable;
    private boolean showAddToEar;
    private boolean mainAppClientClass;
    private boolean showContextPath;
    private boolean createProjects;
    private boolean importScenario;
    private Object j2eeModuleType;
    private String name;
    private String title;
    
    /** Create the wizard panel descriptor. */
    public ProjectServerWizardPanel(Object j2eeModuleType, String name, String title, 
            boolean showAddToEar, boolean mainAppClientClass, 
            boolean showContextPath, boolean createProjects, boolean importScenario, boolean finishable) {
        this.finishable = finishable;
        this.showAddToEar = showAddToEar;
        this.mainAppClientClass = mainAppClientClass;
        this.createProjects = createProjects;
        this.showContextPath = showContextPath;
        this.j2eeModuleType = j2eeModuleType;
        this.name = name;
        this.title = title;
        this.importScenario = importScenario;
    }
    
    public boolean isFinishPanel() {
        return finishable;
    }

    public Component getComponent() {
        if (component == null) {
            component = new ProjectServerPanel(j2eeModuleType, name, title, this, showAddToEar, mainAppClientClass, showContextPath, createProjects, importScenario);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        String helpID = ProjectImportLocationPanel.generateHelpID(ProjectServerWizardPanel.class, j2eeModuleType);
        if (importScenario) {
            helpID += "_IMPORT"; // NOI18N
        }
        return new HelpCtx(helpID);
    }
    
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    protected void fireChangeEvent() {
        changeSupport.fireChange();
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read (wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        ((WizardDescriptor) d).putProperty("NewProjectWizard_Title", null); // NOI18N
    }

}
