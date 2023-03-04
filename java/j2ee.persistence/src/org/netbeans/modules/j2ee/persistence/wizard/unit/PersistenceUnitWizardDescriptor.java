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

package org.netbeans.modules.j2ee.persistence.wizard.unit;

import java.beans.PropertyChangeEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class PersistenceUnitWizardDescriptor implements WizardDescriptor.FinishablePanel, ChangeListener {
    
    private PersistenceUnitWizardPanelDS datasourcePanel;
    private PersistenceUnitWizardPanelJdbc jdbcPanel;
    private PersistenceUnitWizardPanel panel;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private WizardDescriptor wizardDescriptor;
    private Project project;
    private boolean isContainerManaged;
    private static String ERROR_MSG_KEY = WizardDescriptor.PROP_ERROR_MESSAGE;
    
    public PersistenceUnitWizardDescriptor(Project project) {
        this.project = project;
        this.isContainerManaged = Util.isContainerManaged(project);
    }
    
    @Override
    public void addChangeListener(javax.swing.event.ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    @Override
    public java.awt.Component getComponent() {
        if (panel == null) {
            if (isContainerManaged) {
                datasourcePanel = new PersistenceUnitWizardPanelDS(project, this, true);
                panel = datasourcePanel;
            } else {
                jdbcPanel= new PersistenceUnitWizardPanelJdbc(project, this, true);
                panel = jdbcPanel;
            }
            panel.addPropertyChangeListener( (PropertyChangeEvent evt) -> {
                if (evt.getPropertyName().equals(PersistenceUnitWizardPanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if (newvalue instanceof Boolean) {
                        stateChanged(null);
                    }
                }
            });
        }
        return panel;
    }
    
    @Override
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(PersistenceUnitWizardDescriptor.class);
    }
    
    @Override
    public boolean isValid() {
        if (wizardDescriptor == null) {
            return true;
        }
        if (!ProviderUtil.isValidServerInstanceOrNone(project)){
            wizardDescriptor.putProperty(ERROR_MSG_KEY,
                    NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_MissingServer")); //NOI18N
            return false;
        }
        if (panel != null && !panel.isValidPanel()) {
            try {
                if (!panel.isNameUnique()){
                    wizardDescriptor.putProperty(ERROR_MSG_KEY,
                            NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_PersistenceUnitNameNotUnique")); //NOI18N
                }
            } catch (InvalidPersistenceXmlException ipx){
                    wizardDescriptor.putProperty(ERROR_MSG_KEY,
                            NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_InvalidPersistenceXml", ipx.getPath())); //NOI18N
                
            }
            return false;
        }
        wizardDescriptor.putProperty(ERROR_MSG_KEY, " "); //NOI18N
        return true;
    }
    
    
    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        project = Templates.getProject(wizardDescriptor);
    }
    
    @Override
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    @Override
    public void storeSettings(Object settings) {
    }
    
    @Override
    public boolean isFinishPanel() {
        return isValid();
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }
    
    public String getPersistenceUnitName() {
        return panel.getPersistenceUnitName();
    }
    
    public DatabaseConnection getPersistenceConnection() {
        return jdbcPanel == null ? null : jdbcPanel.getPersistenceConnection();
    }
    
    public String getDatasource() {
        return datasourcePanel == null ? null : datasourcePanel.getDatasource();
    }

    public String getDBResourceSelection() {
        return getPersistenceConnection()!=null ? getPersistenceConnection().getName() : getDatasource();
    }
    
    public boolean isContainerManaged() {
        return isContainerManaged;
    }
    
    public boolean isJTA() {
        return datasourcePanel == null ? false : datasourcePanel.isJTA();
    }
    
    boolean isNonDefaultProviderEnabled() {
        return datasourcePanel == null ? false : datasourcePanel.isNonDefaultProviderEnabled();
    }
    
    public String getNonDefaultProvider() {
        return datasourcePanel == null ? null : datasourcePanel.getNonDefaultProvider();
    }
    
    public String getTableGeneration() {
        return panel.getTableGeneration();
    }
    
    public Provider getSelectedProvider(){
        return panel.getSelectedProvider();
    }
}
