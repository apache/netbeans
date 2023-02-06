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

package org.netbeans.modules.j2ee.persistence.wizard.entity;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.util.SourceLevelChecker;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class EntityWizardDescriptor implements WizardDescriptor.FinishablePanel, ChangeListener {
    
    private EntityWizardPanel p;
    private List changeListeners = new ArrayList();
    private WizardDescriptor wizardDescriptor;
    private Project project;
    
    @Override
    public void addChangeListener(javax.swing.event.ChangeListener l) {
        changeListeners.add(l);
    }
    
    @Override
    public java.awt.Component getComponent() {
        if (p == null) {
            p = new EntityWizardPanel(this);
            p.addPropertyChangeListener( (PropertyChangeEvent evt) -> {
                if (evt.getPropertyName().equals(EntityWizardPanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if (newvalue instanceof Boolean) {
                        stateChanged(null);
                    }
                }
            });
        }
        return p;
    }
    
    @Override
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(EntityWizardDescriptor.class);
    }
    
    @Override
    public boolean isValid() {
        // XXX add the following checks
        // p.getName = valid NmToken
        // p.getName not already in module
        if (wizardDescriptor == null) {
            return true;
        }
        
        Sources sources=ProjectUtils.getSources(project);
        SourceGroup groups[]=sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if(groups == null || groups.length == 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(EntityWizardDescriptor.class,"ERR_JavaSourceGroup")); //NOI18N
            return false;
        }
        
        
        if (SourceLevelChecker.isSourceLevel14orLower(project)) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_NeedProperSourceLevel")); // NOI18N
            return false;
        }
        if (p.getPrimaryKeyClassName().trim().isEmpty()) { //NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(EntityWizardDescriptor.class,"ERR_PrimaryKeyNotEmpty")); //NOI18N
            return false;
        }
        
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); //NOI18N
        return true;
    }
    
    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        if (project == null) {
            project = Templates.getProject(wizardDescriptor);
            p.setProject(project);
        }
        FileObject targetFolder = Templates.getTargetFolder(wizardDescriptor);
        p.setTargetFolder(targetFolder);
        
        try{
            if (ProviderUtil.isValidServerInstanceOrNone(project) && !isPersistenceUnitDefined(targetFolder)) {
                p.setPersistenceUnitButtonVisibility(true);
            } else {
                p.setPersistenceUnitButtonVisibility(false);
            }
        } catch (InvalidPersistenceXmlException | RuntimeException ex){ 
            p.setPersistenceUnitButtonVisibility(false);
        }
    }
    
    private boolean isPersistenceUnitDefined(FileObject targetFolder) throws InvalidPersistenceXmlException {
        return ProviderUtil.persistenceExists(project, targetFolder);
    }
    
    @Override
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        changeListeners.remove(l);
    }
    
    @Override
    public void storeSettings(Object settings) {
        
    }
    
    public String getPrimaryKeyClassName() {
        return p.getPrimaryKeyClassName();
    }
    
//    public PersistenceUnit getPersistenceUnit(){
//        return p.getPersistenceUnit();
//    }

    public boolean isCreatePU(){
        return p.isCreatePU();
    }

    @Override
    public boolean isFinishPanel() {
        return isValid();
    }
    
    

    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (changeListeners) {
            it = new HashSet(changeListeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }


}

