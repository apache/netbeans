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
        d.putProperty("NewProjectWizard_Title", null); // NOI18N
    }

}
