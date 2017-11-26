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
package org.netbeans.modules.hibernate.wizards;

import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.hibernate.wizards.support.SelectedTables;
import org.netbeans.modules.hibernate.wizards.support.Table;
import org.netbeans.modules.hibernate.wizards.support.TableClosure;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author gowri
 */
public class HibernateRevengDbTablesWizardDescriptor implements WizardDescriptor.FinishablePanel, ChangeListener {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private HibernateRevengDatabaseTablesPanel panel;
    private boolean componentInitialized;
    private WizardDescriptor wizardDescriptor;
    private Project project;
    private String title;

    public HibernateRevengDbTablesWizardDescriptor(Project project, String title) {
        this.project = project;
        this.title = title;
    }

    public HibernateRevengDatabaseTablesPanel getComponent() {
        if (panel == null) {
            panel = new HibernateRevengDatabaseTablesPanel(project);
            panel.addChangeListener(this);
        }
        return panel;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(HibernateRevengDbTablesWizardDescriptor.class);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public boolean isValid() {
        if (getComponent().getConfigurationFile() != null) {
            try {
                DataObject cfgDataObject = DataObject.find(getComponent().getConfigurationFile());
                HibernateCfgDataObject hco = (HibernateCfgDataObject) cfgDataObject;
                HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);
                boolean value = env.canLoadDBDriver(hco.getHibernateConfiguration());
                if (!value) {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(HibernateRevengDbTablesWizardDescriptor.class, "ERR_Include_DBJars")); // NOI18N
                    return false;
                }
                value = env.canDirectlyConnectToDB(hco.getHibernateConfiguration());
                if (!value) {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(HibernateRevengDbTablesWizardDescriptor.class, "ERR_No_DB_Connection", //NOI18N
                            getComponent().getConfigurationFile().getNameExt()));
                    return false;
                }
            } catch (Exception e) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(HibernateRevengDbTablesWizardDescriptor.class, "ERR_Include_DBJars")); // NOI18N
                return false;
            }
        }

        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); //NOI18N
        return true;
    }



    public void stateChanged(ChangeEvent event) {
        changeSupport.fireChange();
    }

    private void setErrorMessage(String errorMessage) {
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMessage); // NOI18N

    }

    public boolean isFinishPanel() {
        return isValid();
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        wizardDescriptor.putProperty("NewFileWizard_Title", title);


        if (!componentInitialized) {
            componentInitialized = true;
            project = Templates.getProject(wizardDescriptor);
            FileObject targetFolder = Templates.getTargetFolder(wizardDescriptor);
            getComponent().initialize(project);
        }        
    }

    public void storeSettings(Object settings) {
        WizardDescriptor wiz = (WizardDescriptor) settings;
        wizardDescriptor = (WizardDescriptor) settings;
        panel = (HibernateRevengDatabaseTablesPanel) getComponent();
        if (WizardDescriptor.PREVIOUS_OPTION.equals(wizardDescriptor.getValue())) {
            return;
        }
        // prevent NPE from IZ#164960
        TableClosure tc = this.getTableClosure();
        if (tc != null) {
            getComponent().update(tc);
        }
    }

    public TableClosure getTableClosure() {
        return getComponent().getTableClosure();
    }

    public FileObject getConfigurationFile() {
        return getComponent().getConfigurationFile();
    }

    public String getSchemaName() {
        return getComponent().getSchemaName();
    }

    public String getCatalogName() {
        return getComponent().getCatalogName();
    }

    public Set<Table> getSelectedTables() {
        return getComponent().getTableClosure().getSelectedTables();

    }
}
