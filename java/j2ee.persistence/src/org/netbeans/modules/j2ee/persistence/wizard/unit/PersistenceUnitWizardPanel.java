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

import java.util.logging.Logger;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.openide.util.Parameters;

/**
 *
 * @author Pavel Buzek
 */
public abstract class PersistenceUnitWizardPanel extends JPanel {

    protected final Project project;
    private static final Logger LOG = Logger.getLogger(PersistenceUnitWizardPanel.class.getName());
    
    public static final String IS_VALID = "PersistenceUnitWizardPanel_isValid"; //NOI18N

    protected PersistenceUnitWizardPanel(Project project) {
        Parameters.notNull("project", project); //NOI18N
        this.project = project;
    }

    /**
     * Table generation strategy.
     */
    public enum TableGeneration {
        CREATE, DROP_CREATE, NONE
    }
    
    public abstract String getPersistenceUnitName();
    
    public abstract String getTableGeneration();
    
    public abstract boolean isValidPanel();
    
    /** Either data source jdbc name or connection name */
    public abstract void setPreselectedDB(String db);

    /**
     * Checks whether the name of the persistence unit is unique among current
     * project's persistence units.
     * @return true if the name is unique, false otherwise.
     * @throws InvalidPersistenceXmlException if the project has an invalid 
     *  persistence.xml file.
     */
    public final boolean isNameUnique() throws InvalidPersistenceXmlException {
        if (!ProviderUtil.persistenceExists(project)) {
            return true;
        }
        PUDataObject pudo = ProviderUtil.getPUDataObject(project);
        Persistence persistence = pudo.getPersistence();
        return isUnique(getPersistenceUnitName(), persistence.getPersistenceUnit());
    }
    
    /**
     * @return true if the given <code>candidate</code> represents a unique
     * name within the names of the given <code>punits</code>, false otherwise.
     */ 
    private boolean isUnique(String candidate, PersistenceUnit[] punits){
        for (PersistenceUnit punit : punits){
            if (candidate.equals(punit.getName())){
                return false;
            }
        }
        return true;
    }
    
    /**
     * @return the selected provider.
     */
    public abstract Provider getSelectedProvider();
    
    /**
     * Sets an error message to the panel.
     * @param msg the message to set.
     */
    public abstract void setErrorMessage(String msg);
    
}
