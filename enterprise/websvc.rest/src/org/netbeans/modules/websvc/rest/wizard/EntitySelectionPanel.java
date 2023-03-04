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

package org.netbeans.modules.websvc.rest.wizard;


import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.wizard.PersistenceClientEntitySelectionVisual;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper.PersistenceUnit;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * @author Pavel Buzek
 */
public final class EntitySelectionPanel extends AbstractPanel {
    private PersistenceClientEntitySelectionVisual component;
    
    /** Create the wizard panel descriptor. */
    public EntitySelectionPanel(String panelName, WizardDescriptor wizardDescriptor) {
        super(panelName, wizardDescriptor);
    }
    
    @Override
    public HelpCtx getHelp() {
        return null;
    }
    
    @Override
    public boolean isFinishPanel() {
        return false;
    }
    
    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        getComponent().read(wizardDescriptor);
    }
    
    @Override
    public void storeSettings(Object settings) {
        getComponent().store(wizardDescriptor);
        Project project = Templates.getProject(wizardDescriptor);
        if ( component.getCreatePersistenceUnit() && 
                getPersistenceUnit(project)==null)
        {
            wizardDescriptor.putProperty(WizardProperties.CREATE_PERSISTENCE_UNIT, 
                    Boolean.TRUE);
        }
        else {
            wizardDescriptor.putProperty(
                    WizardProperties.CREATE_PERSISTENCE_UNIT, null);
        }
    }
    
    @Override
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        getComponent().valid(wizardDescriptor);
        fireChangeEvent(e);
    }
    
    @Override
    public boolean isValid() {
        Project project = Templates.getProject(wizardDescriptor);
        RestSupport support = project.getLookup().lookup(RestSupport.class);
        if(support == null) {
            setInfoMessage("MSG_EntitySelectionPanel_NotWebProject");
            return false;
        } else {
            /*if(!support.hasSwdpLibrary()) {
                setErrorMessage("MSG_EntitySelectionPanel_NoSWDP");
                return false;
            }*/
            getPersistenceUnit(project);
            /*
             * Fix for BZ#206812: no need in this message because PU is forced to create
             * if ( == null) {
                setInfoMessage("MSG_EntitySelectionPanel_NoPersistenceUnit");
                return false;
            }*/
        }
        return component.valid(wizardDescriptor);
    }

    public PersistenceClientEntitySelectionVisual getComponent() {
        if (component == null) {
            component = new PersistenceClientEntitySelectionVisual(panelName, 
                    wizardDescriptor, true );//new EntitySelectionPanelVisual(panelName);
            component.addChangeListener(this);
        }
        return component;
    }
    
    private PersistenceUnit getPersistenceUnit(Project project) {
        return Util.getPersistenceUnit(wizardDescriptor, project);
    }
}
