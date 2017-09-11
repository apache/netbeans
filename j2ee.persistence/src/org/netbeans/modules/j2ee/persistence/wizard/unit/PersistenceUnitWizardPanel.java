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
