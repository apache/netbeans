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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.deployment.plugins.spi.config;

import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;

/**
 * Configuration useful for managing module data sources.
 * <p>
 * Implementation of this interface should be registered in the {@link ModuleConfiguration}
 * lookup.
 * 
 * @since 1.23
 * @author sherold
 */
public interface DatasourceConfiguration {
    
    /**
     * Returns the data sources defined in the module.
     * 
     * @return a set of data sources defined in the module.
     * 
     * @throws ConfigurationException reports problems in retrieving data source
     *         definitions.
     */
    Set<Datasource> getDatasources() throws ConfigurationException;
    
    /**
     * Returns true if data source creation is supported, false otherwise.
     * 
     * @return true if data source creation is supported, false otherwise.
     */
    boolean supportsCreateDatasource();
    
    /**
     * Creates the data source definition in the module.
     * 
     * @param jndiName data source JNDI name.
     * @param url database URL.
     * @param username database user.
     * @param password user's password.
     * @param driver fully qualified name of the database driver class.
     * 
     * @return created data source.
     * 
     * @throws UnsupportedOperationException if operation is not supported.
     * @throws ConfigurationException reports problems in creating data source
     *         definition.
     * @throws DatasourceAlreadyExistsException if a data source with the same
     *         JNDI name already exists.
     */
    Datasource createDatasource(String jndiName, String  url, String username, String password, String driver) 
    throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException;

    /**
     * Binds the data source reference name with the corresponding data source which is
     * identified the given JNDI name.
     * 
     * @param referenceName name used to identify the data source
     * @param jndiName JNDI name of the data source
     * @throws ConfigurationException if there is some problem with data source configuration
     * 
     * @since 1.25
     */
    void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException;

    /**
     * Binds the data source reference name with the corresponding data source which is
     * identified the given JNDI name. The reference is used within the scope of the EJB.
     * 
     * @param ejbName EJB name
     * @param ejbType EJB type - the possible values are 
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION,
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY and
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN
     * @param referenceName name used to identify the data source
     * @param jndiName JNDI name of the data source
     *
     * @throws ConfigurationException if there is some problem with data source configuration
     * 
     * @since 1.25
     */
    void bindDatasourceReferenceForEjb(String ejbName, String ejbType, 
            String referenceName, String jndiName) throws ConfigurationException;

    /**
     * Finds JNDI name of the data source which is mapped to the given reference name.
     * 
     * @param referenceName reference name
     * 
     * @return JNDI name of the data source if the mapping exists, null otherwise
     *
     * @throws ConfigurationException if there is some problem with data source configuration
     * 
     * @since 1.25
     */
    String findDatasourceJndiName(String referenceName) throws ConfigurationException;
    
    /**
     * Finds JNDI name of the data source which is mapped to the given reference name in the scope the EJB.
     * 
     * @param ejbName EJB name
     * @param referenceName reference name
     * @return JNDI name of the data source if the mapping exists, null otherwise
     *
     * @throws ConfigurationException if there is some problem with data source configuration
     * 
     * @since 1.25
     */
    String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException;
}
