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
            if (deployed.keySet().contains(jndiName)) { // conflicting ds found
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
