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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9.config;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration;

/**
 *
 * @author Petr Hejl
 */
public class WLDeploymentConfiguration implements DatasourceConfiguration, MessageDestinationConfiguration {

    private final WLDatasourceSupport datasourceSupport;
    
    private final WLMessageDestinationSupport messageSupport;

    public WLDeploymentConfiguration(J2eeModule module, Version version) {
        this.datasourceSupport = new WLDatasourceSupport(module.getResourceDirectory());
        this.messageSupport = new WLMessageDestinationSupport(module.getResourceDirectory(), version);
    }

    @Override
    public void bindMdbToMessageDestination(String mdbName, String name, Type type) throws ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName, String destName, Type type) throws ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType, String referenceName, String connectionFactoryName, String destName, Type type) throws ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String findMessageDestinationName(String mdbName) throws ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageDestination createMessageDestination(String name, Type type) throws UnsupportedOperationException, ConfigurationException {
        // api does not provide module and jndi name so we use the same
        return messageSupport.createMessageDestination(name, name, type);
    }

    @Override
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        return new HashSet<MessageDestination>(messageSupport.getMessageDestinations());
    }

    @Override
    public boolean supportsCreateMessageDestination() {
        return false;
    }

    @Override
    public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException {
        throw new UnsupportedOperationException("bindDatasourceReference");
    }

    @Override
    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType, String referenceName, String jndiName) throws ConfigurationException {
        throw new UnsupportedOperationException("bindDatasourceReferenceForEjb");
    }

    @Override
    public Datasource createDatasource(String jndiName, String url, String username, String password, String driver) throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException {
        return datasourceSupport.createDatasource(jndiName, url, username, password, driver);
    }

    @Override
    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        throw new UnsupportedOperationException("findDatasourceJndiName");
    }

    @Override
    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {
        throw new UnsupportedOperationException("findDatasourceJndiNameForEjb");
    }

    @Override
    public Set<Datasource> getDatasources() throws ConfigurationException {
        return new HashSet<Datasource>(datasourceSupport.getDatasources());
    }

    @Override
    public boolean supportsCreateDatasource() {
        return true;
    }

}
