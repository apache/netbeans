/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee.db;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.admin.CommandCreateJDBCConnectionPool;
import org.netbeans.modules.glassfish.tooling.admin.CommandCreateJDBCResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.glassfish.eecommon.api.DomainEditor;
import org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
import org.netbeans.modules.glassfish.spi.ResourceDesc;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Nitya Doraisamy
 */
public class ResourcesHelper {

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
                                GlassfishModule commonSupport = ((Hk2DeploymentManager) dm).getCommonServerSupport();
                                String gfdir = commonSupport.getInstanceProperties().get(GlassfishModule.DOMAINS_FOLDER_ATTR);
                                if (null != gfdir) {
                                    String domain = commonSupport.getInstanceProperties().get(GlassfishModule.DOMAIN_NAME_ATTR);
                                    if (commonSupport.getServerState() != ServerState.RUNNING) {
                                        // TODO : need to account for remote domain here?
                                        DomainEditor de = new DomainEditor(gfdir, domain, false);
                                        de.createSampleDatasource();
                                    } else {
                                        registerSampleResource(commonSupport);
                                    }
                                }
                            }
                        }
                    } else {
                        Logger.getLogger("glassfish-javaee").finer("Could not find project for J2eeModule");   // NOI18N
                    }
                } else {
                    Logger.getLogger("glassfish-javaee").finer("Could not find project root directory for J2eeModule");   // NOI18N
                }
            }
        });
    }

    static private J2eeModuleProvider getProvider(Project project) {
        J2eeModuleProvider provider = null;
        if (project != null) {
            org.openide.util.Lookup lookup = project.getLookup();
            provider = lookup.lookup(J2eeModuleProvider.class);
        }
        return provider;
    }

    static private void registerSampleResource(GlassfishModule commonSupport) {
        String sample_poolname = "SamplePool"; //NOI18N
        String sample_jdbc = "jdbc/sample"; //NOI18N
        String sample_classname = "org.apache.derby.jdbc.ClientDataSource"; //NOI18N
        String sample_restype = "javax.sql.DataSource"; //NOI18N
        Map<String, String> sample_props = new HashMap<String, String>();
        sample_props.put("DatabaseName", "sample");
        sample_props.put("User", "app");
        sample_props.put("Password", "app");
        sample_props.put("PortNumber", "1527");
        sample_props.put("serverName", "localhost");
        sample_props.put("URL", "jdbc\\:derby\\://localhost\\:1527/sample");
        Map<String, ResourceDesc> jdbcsMap = commonSupport.getResourcesMap(GlassfishModule.JDBC_RESOURCE);
        if (!jdbcsMap.containsKey(sample_jdbc)) {
            try {
                CommandCreateJDBCConnectionPool.createJDBCConnectionPool(commonSupport.getInstance(),
                        sample_poolname, sample_classname, sample_restype,
                        sample_props, 60000);
                CommandCreateJDBCResource.createJDBCResource(
                        commonSupport.getInstance(), sample_poolname,
                        sample_jdbc, null, null, 60000);
            } catch (GlassFishIdeException gfie) {
                Logger.getLogger("glassfish-javaee").log(
                        Level.SEVERE, gfie.getLocalizedMessage(), gfie);
            }
        }
    }

}

