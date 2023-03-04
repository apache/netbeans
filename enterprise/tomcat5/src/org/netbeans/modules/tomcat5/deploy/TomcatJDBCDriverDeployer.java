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

package org.netbeans.modules.tomcat5.deploy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
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
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.j2ee.common.ClasspathUtil;
import org.netbeans.modules.j2ee.common.DatasourceHelper;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.tomcat5.progress.ProgressEventSupport;
import org.netbeans.modules.tomcat5.progress.Status;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Tomcat JDBCDriverDeployer implementation.
 *
 * @author sherold
 */
public class TomcatJDBCDriverDeployer implements JDBCDriverDeployer {

    private static final Logger LOG = Logger.getLogger(TomcatJDBCDriverDeployer.class.getName());

    private final TomcatManager manager;

    /** Creates a new instance of TomcatJDBCDriverDeployer */
    public TomcatJDBCDriverDeployer(TomcatManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean supportsDeployJDBCDrivers(Target target) {
        return manager.getTomcatProperties().getDriverDeployment();
    }

    @Override
    public ProgressObject deployJDBCDrivers(Target target, Set<Datasource> datasources) {
        return new DriverDeploymentProgressObject(datasources);
    }

    private class DriverDeploymentProgressObject implements ProgressObject, Runnable {

        private final ProgressEventSupport eventSupport;
        private final Set<Datasource> datasources;


        public DriverDeploymentProgressObject(Set<Datasource> datasources) {
            eventSupport = new ProgressEventSupport(TomcatJDBCDriverDeployer.this);
            this.datasources = datasources;
            String msg = NbBundle.getMessage(TomcatJDBCDriverDeployer.class, "MSG_CheckingMissingDrivers");
            eventSupport.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE, CommandType.DISTRIBUTE, msg, StateType.RUNNING));
            RequestProcessor.getDefault().post(this);
        }

        @Override
        public void run() {
            List<FileObject> jdbcDriverFiles = jdbcDriversToDeploy();
            // deploy the driers if needed
            if (!jdbcDriverFiles.isEmpty()) {
                TomcatProperties tp = manager.getTomcatProperties();
                for (FileObject file : jdbcDriverFiles) {
                    File libsDir = tp.getLibsDir();
                    File toJar = new File(libsDir, file.getNameExt());
                    try (BufferedInputStream is = new BufferedInputStream(file.getInputStream())) {
                        String msg = NbBundle.getMessage(TomcatJDBCDriverDeployer.class, "MSG_DeployingJDBCDrivers", toJar.getPath());
                        eventSupport.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE, CommandType.DISTRIBUTE, msg, StateType.RUNNING));
                        try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(toJar))) {
                            FileUtil.copy(is, os);
                        }
                    } catch (IOException e) {
                        LOG.log(Level.INFO, null, e);
                        String msg = NbBundle.getMessage(TomcatJDBCDriverDeployer.class, "MSG_DeployingJDBCDriversFailed", toJar.getPath(), libsDir.getPath());
                        eventSupport.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE, CommandType.DISTRIBUTE, msg, StateType.FAILED));
                        return;
                    }
                }
                // set the restart flag
                manager.setNeedsRestart(true);
            }
            eventSupport.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE, CommandType.DISTRIBUTE, "", StateType.COMPLETED)); // NOI18N
        }

        /** Returns a list of jdbc drivers that need to be deployed. */
        private List<FileObject> jdbcDriversToDeploy() {
            List<FileObject> jdbcDriverFiles = new ArrayList<>();
            Collection<File> driverCP = getJDBCDriverClasspath();
            for (Datasource datasource : datasources) {
                String className = datasource.getDriverClassName();
                boolean exists = false;
                try {
                    exists = ClasspathUtil.containsClass(driverCP, className);
                } catch (IOException e) {
                    LOG.log(Level.INFO, null, e);
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
            TomcatProperties tp = manager.getTomcatProperties();
            return Arrays.asList(tp.getLibsDir().listFiles());
        }

        @Override
        public DeploymentStatus getDeploymentStatus() {
            return eventSupport.getDeploymentStatus();
        }

        @Override
        public TargetModuleID[] getResultTargetModuleIDs() {
            return null;
        }

        @Override
        public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
            return null;
        }

        @Override
        public boolean isCancelSupported() {
            return false;
        }

        @Override
        public void cancel() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("Cancel is not supported"); // NOI18N
        }

        @Override
        public boolean isStopSupported() {
            return false;
        }

        @Override
        public void stop() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("Stop is not supported"); // NOI18N
        }

        @Override
        public void addProgressListener(ProgressListener progressListener) {
            eventSupport.addProgressListener(progressListener);
        }

        @Override
        public void removeProgressListener(ProgressListener progressListener) {
            eventSupport.removeProgressListener(progressListener);
        }

    }
}
