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

package org.netbeans.modules.payara.jakartaee.db;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.admin.CommandCreateJDBCConnectionPool;
import org.netbeans.modules.payara.tooling.admin.CommandCreateJDBCResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.payara.eecommon.api.DomainEditor;
import org.netbeans.modules.payara.jakartaee.Hk2DeploymentManager;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.spi.PayaraModule.ServerState;
import org.netbeans.modules.payara.spi.ResourceDesc;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Nitya Doraisamy
 */
public class ResourcesHelper {

    private static final Logger LOG = Logger.getLogger("payara-jakartaee");

    private static RequestProcessor RP = new RequestProcessor("Sample Datasource work");

    public static void addSampleDatasource(final J2eeModule module , final DeploymentManager dmParam) {
        RP.post(new Runnable() {

            @Override
            public void run() {
                File f = module.getResourceDirectory();
                if(null != f && f.exists()){
                    f = f.getParentFile();
                }
                if (null != f) {
                    Project p = FileOwnerQuery.getOwner(Utilities.toURI(f));
                    if (null != p) {
                        J2eeModuleProvider jmp = getProvider(p);
                        if (null != jmp) {
                            DeploymentManager dm = dmParam;
                            if (dm instanceof Hk2DeploymentManager) {
                                PayaraModule commonSupport = ((Hk2DeploymentManager) dm).getCommonServerSupport();
                                String gfdir = commonSupport.getInstanceProperties().get(PayaraModule.DOMAINS_FOLDER_ATTR);
                                if (null != gfdir) {
                                    String domain = commonSupport.getInstanceProperties().get(PayaraModule.DOMAIN_NAME_ATTR);
                                    if (commonSupport.getServerState() != ServerState.RUNNING) {
                                        // TODO : need to account for remote domain here?
                                        DomainEditor de = new DomainEditor(gfdir, domain);
                                        de.createSampleDatasource();
                                    } else {
                                        registerSampleResource(commonSupport);
                                    }
                                }
                            }
                        }
                    } else {
                        LOG.finer("Could not find project for J2eeModule");   // NOI18N
                    }
                } else {
                    LOG.finer("Could not find project root directory for J2eeModule");   // NOI18N
                }
            }
        });
    }

    private static J2eeModuleProvider getProvider(Project project) {
        J2eeModuleProvider provider = null;
        if (project != null) {
            org.openide.util.Lookup lookup = project.getLookup();
            provider = lookup.lookup(J2eeModuleProvider.class);
        }
        return provider;
    }

    private static void registerSampleResource(PayaraModule commonSupport) {
        String sample_poolname = "H2Pool"; //NOI18N
        String sample_jdbc = "jdbc/__default"; //NOI18N
        String sample_classname = "org.h2.jdbcx.JdbcDataSource"; //NOI18N
        String sample_restype = "javax.sql.DataSource"; //NOI18N
        Map<String, String> sample_props = new HashMap<>();
        sample_props.put("URL", "jdbc:h2:${com.sun.aas.instanceRoot}/lib/databases/embedded_default;AUTO_SERVER=TRUE");
        Map<String, ResourceDesc> jdbcsMap = commonSupport.getResourcesMap(PayaraModule.JDBC_RESOURCE);
        if (!jdbcsMap.containsKey(sample_jdbc)) {
            try {
                CommandCreateJDBCConnectionPool.createJDBCConnectionPool(commonSupport.getInstance(),
                        sample_poolname, sample_classname, sample_restype,
                        sample_props, 60000);
                CommandCreateJDBCResource.createJDBCResource(
                        commonSupport.getInstance(), sample_poolname,
                        sample_jdbc, null, null, 60000);
            } catch (PayaraIdeException ex) {
                LOG.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }

}

