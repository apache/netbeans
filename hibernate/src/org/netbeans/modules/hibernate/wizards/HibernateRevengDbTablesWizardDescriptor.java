/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
