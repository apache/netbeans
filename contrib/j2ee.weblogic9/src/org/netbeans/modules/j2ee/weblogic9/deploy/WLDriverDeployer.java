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

package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.j2ee.common.ClasspathUtil;
import org.netbeans.modules.j2ee.common.DatasourceHelper;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class WLDriverDeployer implements JDBCDriverDeployer {

    private static final Logger LOGGER = Logger.getLogger(WLDriverDeployer.class.getName());

    private static final RequestProcessor DRIVER_DEPLOYMENT_RP = new RequestProcessor("Weblogic Driver Deployment", 1); // NOI18N

    private final WLDeploymentManager manager;

    private final FileFilter serverClasspathFilter;
    
    private static final FileFilter DEFAULT_CLASSPATH_FILTER = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory() || pathname.getName().endsWith(".jar"); // NOI18N
        }
    };

    public WLDriverDeployer(WLDeploymentManager manager) {
        this.manager = manager;
        serverClasspathFilter = DEFAULT_CLASSPATH_FILTER;
    }

    @Override
    public boolean supportsDeployJDBCDrivers(Target target) {
        return !manager.isRemote();
    }

    @Override
    public ProgressObject deployJDBCDrivers(Target target, final Set<Datasource> datasources) {
        final WLProgressObject progress = new WLProgressObject(new TargetModuleID[0]);

        progress.fireProgressEvent(null, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                NbBundle.getMessage(WLDriverDeployer.class, "MSG_CheckingMissingDrivers")));

        DRIVER_DEPLOYMENT_RP.submit(new Runnable() {

            @Override
            public void run() {
                List<FileObject> jdbcDriverURLs = jdbcDriversToDeploy(datasources);
                // deploy the driers if needed
                if (!jdbcDriverURLs.isEmpty()) {
                    File libsDir = WLPluginProperties.getDomainLibDirectory(manager);
                    if (libsDir == null) {
                        LOGGER.log(Level.FINE, "No domain lib, using server lib for {0}", manager.getUri());
                        libsDir = WLPluginProperties.getServerLibDirectory(manager, false);
                    }
                    if (libsDir != null) {
                        for (FileObject file : jdbcDriverURLs) {
                            File toJar = new File(libsDir, file.getNameExt());
                            try {
                                BufferedInputStream is = new BufferedInputStream(file.getInputStream());
                                try {
                                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                                            NbBundle.getMessage(WLDriverDeployer.class, "MSG_DeployingJDBCDrivers", toJar.getPath())));
                                    BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(toJar));
                                    try {
                                        FileUtil.copy(is, os);
                                    } finally {
                                        os.close();
                                    }
                                } finally {
                                    is.close();
                                }
                            } catch (IOException e) {
                                LOGGER.log(Level.INFO, null, e);
                                progress.fireProgressEvent(null, new WLDeploymentStatus(
                                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                        NbBundle.getMessage(WLDriverDeployer.class, "MSG_DeployingJDBCDriversFailed", toJar.getPath(), libsDir.getPath())));
                                return;
                            }
                        }
                    }
                    LOGGER.log(Level.FINE, "Restart flag configured");
                    manager.setRestartNeeded(true);
                }
                progress.fireProgressEvent(null, new WLDeploymentStatus(
                        ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED,
                        NbBundle.getMessage(WLDriverDeployer.class, "MSG_JDBCDriversDeployed")));
            }
        });

        return progress;
    }

    /** Returns a list of jdbc drivers that need to be deployed. */
    private List<FileObject> jdbcDriversToDeploy(Set<Datasource> datasources) {
        List<FileObject> jdbcDriverFiles = new ArrayList<FileObject>();
        Collection<File> driverCP = getJDBCDriverClasspath();
        for (Datasource datasource : datasources) {
            String className = datasource.getDriverClassName();
            boolean exists = false;
            try {
                exists = ClasspathUtil.containsClass(driverCP, className);
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
            if (!exists) {
                for (DatabaseConnection databaseConnection : DatasourceHelper.findDatabaseConnections(datasource)) {
                    JDBCDriver[] jdbcDrivers;
                    JDBCDriver connDriver = databaseConnection.getJDBCDriver();
                    if (connDriver != null) {
                        jdbcDrivers = new JDBCDriver[] {connDriver};
                    } else {
                        // old fashioned way - fallback
                        String driverClass = databaseConnection.getDriverClass();
                        jdbcDrivers = JDBCDriverManager.getDefault().getDrivers(driverClass);
                    }
                    for (JDBCDriver jdbcDriver : jdbcDrivers) {
                        for (URL url : jdbcDriver.getURLs()) {
                            FileObject file = URLMapper.findFileObject(url);
                            if (file != null) {
                                jdbcDriverFiles.add(file);
                            }
                        }
                    }
                }
            }
        }
        return jdbcDriverFiles;
    }

    /** Returns a classpath where the JDBC drivers could be placed */
    private Collection<File> getJDBCDriverClasspath() {
        // FIXME server/lib as well
        List<File> cp = new ArrayList<File>();
        File domainLib = WLPluginProperties.getDomainLibDirectory(manager);
        if (domainLib != null) {
            File[] files = domainLib.listFiles(DEFAULT_CLASSPATH_FILTER);
            if (files != null) {
                cp.addAll(Arrays.asList(files));
            }
        }
        File serverLib = WLPluginProperties.getServerLibDirectory(manager, false);
        if (serverLib != null) {
            File[] files = serverLib.listFiles(serverClasspathFilter);
            if (files != null) {
                cp.addAll(Arrays.asList(files));
            }
        }
        return cp;
    }
}
