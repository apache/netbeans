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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.MultiTargetChooserPanel;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class SessionEJBWizardDescriptor implements WizardDescriptor.FinishablePanel, ChangeListener {
    
    private SessionEJBWizardPanel wizardPanel;
    private final EJBNameOptions ejbNames;
    private final Project project;
    private final TimerOptions timerOptions;
    //TODO: RETOUCHE
//    private boolean isWaitingForScan = false;
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private WizardDescriptor wizardDescriptor;

    public SessionEJBWizardDescriptor(Project project, TimerOptions timerOptions) {
        this.ejbNames = new EJBNameOptions();
        this.timerOptions = timerOptions;
        this.project = project;
    }
    
    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }
    
    public java.awt.Component getComponent() {
        if (wizardPanel == null) {
            wizardPanel = new SessionEJBWizardPanel(project, this, timerOptions);
            // add listener to events which could cause valid status to change
        }
        return wizardPanel;
    }
    
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(SessionEJBWizardDescriptor.class);
    }
    
    public boolean isValid() {
        // XXX add the following checks
        // p.getName = valid NmToken
        // p.getName not already in module
        if (wizardDescriptor == null) {
            return true;
        }
        boolean isLocal = wizardPanel.isLocal();
        boolean isRemote = wizardPanel.isRemote();
        if (!isLocal && !isRemote && !J2eeProjectCapabilities.forProject(project).isEjb31LiteSupported()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(SessionEJBWizardDescriptor.class,"ERR_RemoteOrLocal_MustBeSelected")); //NOI18N
            return false;
        }

        if (isRemote && wizardPanel.getRemoteInterfaceProject() == null){
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                    SessionEJBWizardPanel.isMaven(project) ? NbBundle.getMessage(SessionEJBWizardDescriptor.class,"ERR_NoRemoteInterfaceProjectMaven") :
                        NbBundle.getMessage(SessionEJBWizardDescriptor.class,"ERR_NoRemoteInterfaceProject")); //NOI18N
            return false;
        }

        FileObject targetFolder = (FileObject) wizardDescriptor.getProperty(MultiTargetChooserPanel.TARGET_FOLDER);
        if (targetFolder != null) {
            String targetName = (String) wizardDescriptor.getProperty(MultiTargetChooserPanel.TARGET_NAME);
            List<String> proposedNames = new ArrayList<String>();
            proposedNames.add(ejbNames.getSessionEjbClassPrefix() + targetName + ejbNames.getSessionEjbClassSuffix());
            if (isLocal) {
                proposedNames.add(ejbNames.getSessionLocalPrefix() + targetName + ejbNames.getSessionLocalSuffix());
                proposedNames.add(ejbNames.getSessionLocalHomePrefix() + targetName + ejbNames.getSessionLocalHomeSuffix());
            } 
            if (isRemote) {
                proposedNames.add(ejbNames.getSessionRemotePrefix() + targetName + ejbNames.getSessionRemoteSuffix());
                proposedNames.add(ejbNames.getSessionRemoteHomePrefix() + targetName + ejbNames.getSessionRemoteHomeSuffix());
            }
            for (String name : proposedNames) {
                if (targetFolder.getFileObject(name + ".java") != null) { // NOI18N
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                            NbBundle.getMessage(SessionEJBWizardDescriptor.class,"ERR_FileAlreadyExists", name + ".java")); //NOI18N
                    return false;
                }
            }

        }
        // #183916 - avoid cyclic dependencies
        if (isRemote && hasCyclicDependency(wizardPanel.getRemoteInterfaceProject())) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(SessionEJBWizardDescriptor.class, "ERR_CyclicDependency"));
            return false;
        }
        
        // check Schedule section if valid
        String timerOptionsError = wizardPanel.getTimerOptionsError();
        if (timerOptionsError != null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, timerOptionsError);
            return false;
        }

        //TODO: RETOUCHE waitScanFinished
//        if (JavaMetamodel.getManager().isScanInProgress()) {
//            if (!isWaitingForScan) {
//                isWaitingForScan = true;
//                RequestProcessor.getDefault().post(new Runnable() {
//                    public void run() {
//                        JavaMetamodel.getManager().waitScanFinished();
//                        isWaitingForScan = false;
//                        fireChangeEvent();
//                    }
//                });
//            }
//            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(SessionEJBWizardPanel.class,"scanning-in-progress")); //NOI18N
//            return false;
//        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); //NOI18N
        return true;
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }
    
    public void storeSettings(Object settings) {
        
    }
    
    public boolean hasRemote() {
        return wizardPanel.isRemote();
    }
    
    public boolean hasLocal() {
        return wizardPanel.isLocal();
    }

    public Project getRemoteInterfaceProject() {
        if (hasRemote() && !wizardPanel.getRemoteInterfaceProject().equals(project)) {
            return wizardPanel.getRemoteInterfaceProject();
        }
        return null;
    }

    public String getSessionType() {
        return wizardPanel.getSessionType();
    }

    public TimerOptions getTimerOptions() {
        return wizardPanel.getTimerOptions();
    }

    public boolean exposeTimerMethod() {
        return wizardPanel.exposeTimerMethod();
    }

    public boolean nonPersistentTimer() {
        return wizardPanel.nonPersistentTimer();
    }
    
    public boolean isFinishPanel() {
        return isValid();
    }
    
    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    public void stateChanged(ChangeEvent changeEvent) {
        fireChangeEvent();
    }

    private boolean hasCyclicDependency(Project projectToCheck) {
        if (projectToCheck == null) {
            return false;
        }
        //mkleint: see subprojectprovider for official contract, maybe classpath should be checked instead? see #210465
        SubprojectProvider subprojectProvider = projectToCheck.getLookup().lookup(SubprojectProvider.class);
        if (subprojectProvider != null) {
            Set<? extends Project> subprojects = subprojectProvider.getSubprojects();
            if (subprojects.contains(project)) {
                return true;
            }
            for (Project subproject : subprojects) {
                if (hasCyclicDependency(subproject)) {
                    return true;
                }
            }
        }
        return false;
    }
}
