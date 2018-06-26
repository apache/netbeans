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
package org.netbeans.modules.javaee.wildfly.config;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration;
import org.netbeans.modules.javaee.wildfly.config.ds.DatasourceSupport;
import org.netbeans.modules.javaee.wildfly.config.mdb.MessageDestinationSupport;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils;
import org.openide.loaders.DataObject;

/**
 * Base for JBoss DeploymentConfiguration implementations.
 *
 * @author Pavel Buzek, Libor Kotouc
 */
public abstract class WildflyDeploymentConfiguration
        implements DatasourceConfiguration, MessageDestinationConfiguration, EjbResourceConfiguration {

    // TODO move to a more appropriate class as soon as E-mail resource API is introduced
    protected static final String MAIL_SERVICE_JNDI_NAME_JB4 = "java:Mail"; // NOI18N

    //JSR-88 deployable object - initialized when instance is constructed
    protected final J2eeModule j2eeModule;

    //cached data object for the server-specific configuration file (initialized by the subclasses)
    protected DataObject deploymentDescriptorDO;

    protected final WildflyPluginUtils.Version version;

    //the directory with resources - supplied by the configuration support in the construction time
    private File resourceDir;

    //support for data sources
    private DatasourceSupport dsSupport;

    //support for message destination resources
    private MessageDestinationSupport destSupport;

    protected boolean isWildFly;

    /**
     * Creates a new instance of JBDeploymentConfiguration
     */
    public WildflyDeploymentConfiguration(J2eeModule j2eeModule, WildflyPluginUtils.Version version, boolean isWildFly) {
        this.j2eeModule = j2eeModule;
        this.version = version;
        this.resourceDir = j2eeModule.getResourceDirectory();
        this.isWildFly = isWildFly;
    }

// -------------------------------------- ModuleConfiguration  -----------------------------------------
    public J2eeModule getJ2eeModule() {
        return j2eeModule;
    }

    public boolean isWildfly() {
        return isWildFly;
    }

    @Override
    public boolean supportsCreateDatasource() {
        return false;
    }

    @Override
    public boolean supportsCreateMessageDestination() {
        return isWildfly();
    }

// -------------------------------------- DatasourceConfiguration  -----------------------------------------
    private DatasourceSupport getDatasourceSupport() {
        if (dsSupport == null) {
            dsSupport = new DatasourceSupport(resourceDir);
        }
        return dsSupport;
    }

    @Override
    public Set<Datasource> getDatasources() throws ConfigurationException {
        return getDatasourceSupport().getDatasources();
    }

    @Override
    public Datasource createDatasource(String jndiName, String url,
            String username, String password, String driver)
            throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException {

        return getDatasourceSupport().createDatasource(jndiName, url, username, password, driver);
    }

    @Override
    public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException {
    }

    @Override
    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String jndiName) throws ConfigurationException {
    }

    @Override
    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        return null;
    }

    @Override
    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {
        return null;
    }

// -------------------------------------- MessageDestinationConfiguration  -----------------------------------------
    private MessageDestinationSupport getMessageDestinationsSupport() throws IOException {
        if (destSupport == null) {
            String configFile = "module-destinations";
            if (this.j2eeModule != null && this.j2eeModule.getArchive() != null) {
                configFile = this.j2eeModule.getArchive().getName();
            }
            if (this.version.compareTo(WildflyPluginUtils.WILDFLY_10_0_0) < 0) {
                destSupport = new org.netbeans.modules.javaee.wildfly.config.mdb.MessageDestinationSupportImpl(resourceDir, configFile);
            } else {
                destSupport = new org.netbeans.modules.javaee.wildfly.config.mdb.wf10.MessageDestinationSupportImpl(resourceDir, configFile);
            }
        }
        return destSupport;
    }

    @Override
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        try {
            return getMessageDestinationsSupport().getMessageDestinations();
        } catch (IOException ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        }
    }

    @Override
    public MessageDestination createMessageDestination(String name, MessageDestination.Type type)
            throws UnsupportedOperationException, ConfigurationException {
        try {
            return getMessageDestinationsSupport().createMessageDestination(name, type);
        } catch (IOException ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        }
    }

    @Override
    public void bindMdbToMessageDestination(String mdbName, String name,
            MessageDestination.Type type) throws ConfigurationException {
    }

    @Override
    public String findMessageDestinationName(String mdbName) throws ConfigurationException {
        return null;
    }

    @Override
    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type type) throws ConfigurationException {
    }

    @Override
    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type type) throws ConfigurationException {
    }

// -------------------------------------- EjbResourceConfiguration  -----------------------------------------
    @Override
    public String findJndiNameForEjb(String ejbName) throws ConfigurationException {
        return null;
    }

    @Override
    public void bindEjbReference(String referenceName, String jndiName) throws ConfigurationException {
    }

    @Override
    public void bindEjbReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String jndiName) throws ConfigurationException {
    }

}
