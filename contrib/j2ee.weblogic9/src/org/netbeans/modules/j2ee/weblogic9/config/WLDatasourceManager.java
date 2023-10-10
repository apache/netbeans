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

package org.netbeans.modules.j2ee.weblogic9.config;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.weblogic9.ProgressObjectSupport;
import org.netbeans.modules.j2ee.weblogic9.WLConnectionSupport;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.CommandBasedDeployer;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class WLDatasourceManager implements DatasourceManager {

    // since 12.2.1 we want to add default datasource for Java EE 7
    private static final Version JAVA_EE7_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.2.1"); // NOI18N

    private static final String JAVA_EE_7_DEFAULT_DATASOURCE = "java:comp/DefaultDataSource"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(WLDatasourceManager.class.getName());

    private final WLDeploymentManager manager;

    public WLDatasourceManager(WLDeploymentManager manager) {
        this.manager = manager;
    }

    // TODO we should start required JDBC datasources on server just for case
    // it previously failed (ie. db was not accessible at that time)
    @Override
    public void deployDatasources(Set<Datasource> datasources) throws ConfigurationException, DatasourceAlreadyExistsException {
        Set<Datasource> deployedDatasources = getDatasources();
        // for faster searching
        Map<String, Datasource> deployed = createMap(deployedDatasources);

        // will contain all ds which do not conflict with existing ones
        Map<String, WLDatasource> toDeploy = new HashMap<String, WLDatasource>();
        
        // resolve all conflicts
        LinkedList<Datasource> conflictDS = new LinkedList<Datasource>();
        for (Datasource datasource : datasources) {
            if (!(datasource instanceof WLDatasource)) {
                LOGGER.log(Level.INFO, "Unable to deploy {0}", datasource);
                continue;
            }

            WLDatasource wlDatasource = (WLDatasource) datasource;
            String jndiName = wlDatasource.getJndiName();
            if (deployed.containsKey(jndiName)) { // conflicting ds found
                Datasource deployedDatasource = deployed.get(jndiName);
                
                // jndi name is same, but DS differs
                if (!deployed.get(jndiName).equals(wlDatasource)) {
                    // they differ, but both are app modules - ok to redeploy
                    if (!((WLDatasource)deployedDatasource).isSystem() && !wlDatasource.isSystem()) {
                        toDeploy.put(jndiName, wlDatasource);
                    } else {
                        conflictDS.add(deployed.get(jndiName));
                    }
                } else {
                    // TODO try to start it
                }
            } else if (jndiName != null) {
                toDeploy.put(jndiName, wlDatasource);
            } else {
                LOGGER.log(Level.INFO, "JNDI name was null for {0}", datasource);
            }
        }
        
        if (!conflictDS.isEmpty()) {
            throw new DatasourceAlreadyExistsException(conflictDS);
        }

        CommandBasedDeployer deployer = new CommandBasedDeployer(manager);
        ProgressObject po = deployer.deployDatasource(toDeploy.values(), manager.getDeployTargets());
        if (!ProgressObjectSupport.waitFor(po) || po.getDeploymentStatus().isFailed()) {
            String msg = NbBundle.getMessage(WLDatasourceManager.class, "MSG_FailedToDeployDatasource", po.getDeploymentStatus().getMessage());
            throw new ConfigurationException(msg);
        }
    }

    @Override
    public Set<Datasource> getDatasources() throws ConfigurationException {
        HashSet<Datasource> ret;
        if (manager.isRemote()) {
            try {
                ret = new HashSet<Datasource>(manager.getConnectionSupport().executeAction(new WLConnectionSupport.JMXRuntimeAction<Set<WLDatasource>>(){

                    @Override
                    public Set<WLDatasource> call(MBeanServerConnection connection, ObjectName service) throws Exception {
                        return WLDatasourceSupport.getSystemDatasources(connection, service);
                    }
                }));
            } catch (Exception ex) {
                if (ex instanceof ConfigurationException) {
                    throw (ConfigurationException) ex;
                }
                throw new ConfigurationException("Datasource fetch failed", ex);
            }
        } else {
            // FIXME use methods from WLPluginproperties
            String domainDir = manager.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);
            File domainPath = FileUtil.normalizeFile(new File(domainDir));
            FileObject domain = FileUtil.toFileObject(domainPath);
            FileObject domainConfig = null;
            if (domain != null) {
                domainConfig = domain.getFileObject("config/config.xml"); // NOI18N
            }

            ret = new HashSet<Datasource>(
                    WLDatasourceSupport.getDatasources(domainPath, domainConfig, true));
            addDefaultDataSource(ret);
        }
        return ret;
    }

    private Map<String, Datasource> createMap(Set<Datasource> datasources) {
        if (datasources.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Datasource> map = new HashMap<String, Datasource>();
        for (Datasource datasource : datasources) {
            map.put(datasource.getJndiName(), datasource);
        }
        return map;
    }

    private void addDefaultDataSource(Set<Datasource> datasources) {
        for (Datasource ds : datasources) {
            if (JAVA_EE_7_DEFAULT_DATASOURCE.equals(ds.getJndiName())) {
                return;
            }
        }
        Version version = manager.getServerVersion();
        if (version != null && version.isAboveOrEqual(JAVA_EE7_SERVER_VERSION)) {
            File file = WLPluginProperties.getServerRoot(manager, false);
            File derby = new File(file, "common" + File.separatorChar + "derby"); // NOI18N
            // unfortunately the derby and thus default DS is not present in all
            // installations
            if (derby.isDirectory()) {
                // XXX it looks like we need localhost even for remote instances as
                // url may be used inside of the generated code ?
                datasources.add(new WLDatasource(JAVA_EE_7_DEFAULT_DATASOURCE,
                        "jdbc:derby://localhost:1527/DefaultDataSource", JAVA_EE_7_DEFAULT_DATASOURCE, // NOI18N
                        "app", "app", "org.apache.derby.jdbc.ClientDriver", null, true)); // NOI18N
            }
        }
    }
}
