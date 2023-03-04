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

package org.netbeans.modules.websvc.core.client.wizard;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import java.awt.Component;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;


/**
 *
 * @author Peter Williams
 */
public class WebServiceClientWizardDescriptor implements WizardDescriptor.FinishablePanel, WizardDescriptor.AsynchronousValidatingPanel {

    private WizardDescriptor wizardDescriptor;
    private ClientInfo component = null;
    private String projectPath;
    private Project project;

    public WebServiceClientWizardDescriptor() {
    }

    public boolean isFinishPanel(){
        return true;
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
        public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    public Component getComponent() {
        if(component == null) {
            component = new ClientInfo(this);
        }

        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(WebServiceClientWizardDescriptor.class);
    }

    public boolean isValid() { 
        boolean projectDirValid=true;
        String illegalChar = null;
        
        if (projectPath.indexOf("%")>=0) {
            projectDirValid=false;
            illegalChar="%";
        } else if (projectPath.indexOf("&")>=0) {
            projectDirValid=false;
            illegalChar="&";
        } else if (projectPath.indexOf("?")>=0) {
            projectDirValid=false;
            illegalChar="?";
        }
        if (!projectDirValid) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(WebServiceClientWizardDescriptor.class,"MSG_InvalidProjectPath",projectPath,illegalChar));
            return false;
        }
        
        return component.valid(wizardDescriptor);   
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        project = Templates.getProject(wizardDescriptor);
        projectPath = project.getProjectDirectory().getPath();
        component.read(wizardDescriptor);

        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFileWizard to modify the title
        wizardDescriptor.putProperty("NewFileWizard_Title", 
        NbBundle.getMessage(WebServiceClientWizardDescriptor.class, "LBL_WebServiceClient"));// NOI18N        
    }

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        ((WizardDescriptor) d).putProperty("NewFileWizard_Title", null); // NOI18N
    }

    public void validate() throws org.openide.WizardValidationException {
        component.validatePanel();
    }

    public void prepareValidation() {
    }
    
}
