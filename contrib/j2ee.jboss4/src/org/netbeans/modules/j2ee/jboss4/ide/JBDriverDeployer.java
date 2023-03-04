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

package org.netbeans.modules.j2ee.jboss4.ide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
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
import org.netbeans.modules.j2ee.jboss4.JB7Deployer;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.util.JBProperties;
import org.netbeans.modules.j2ee.jboss4.util.ProgressEventSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * JBoss JDBCDriverDeployer implementation.
 * 
 * @author sherold
 */
public class JBDriverDeployer implements JDBCDriverDeployer {
    
    private static final Logger LOGGER = Logger.getLogger(JBDriverDeployer.class.getName());
    
    private final JBDeploymentManager manager;
    
    /** Creates a new instance of JBDriverDeployer */
    public JBDriverDeployer(JBDeploymentManager manager) {
        this.manager = manager;
    }

    public boolean supportsDeployJDBCDrivers(Target target) {
        return true;
    }

    public ProgressObject deployJDBCDrivers(Target target, Set<Datasource> datasources) {        
        return new DriverDeploymentProgressObject(datasources);
    }
    
    private class DriverDeploymentProgressObject implements ProgressObject, Runnable {
        
        private final ProgressEventSupport eventSupport;
        private final Set<Datasource> datasources;
        
        
        public DriverDeploymentProgressObject(Set<Datasource> datasources) {
            eventSupport = new ProgressEventSupport(JBDriverDeployer.this);
            this.datasources = datasources;
            String msg = NbBundle.getMessage(JBDriverDeployer.class, "MSG_CheckingMissingDrivers");
            eventSupport.fireProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, msg));
            RequestProcessor.getDefault().post(this);
        }
    
        @Override
        public void run() {
            if (manager.isAs7()) {
                deployDriversAs7();
            } else {
                deployDrivers();
            }
        }

        private void deployDrivers() {
            List<FileObject> jdbcDriverURLs = jdbcDriversToDeploy(false);
            // deploy the driers if needed
            if (!jdbcDriverURLs.isEmpty()) {
                JBProperties properties = manager.getProperties();
                for (FileObject file : jdbcDriverURLs) {
                    File libsDir = properties.getLibsDir();
                    File toJar = new File(libsDir, file.getNameExt());
                    try {
                        BufferedInputStream is = new BufferedInputStream(file.getInputStream());
                        try {
                            String msg = NbBundle.getMessage(JBDriverDeployer.class, "MSG_DeployingJDBCDrivers", toJar.getPath());
                            eventSupport.fireProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, msg));
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
                        String msg = NbBundle.getMessage(JBDriverDeployer.class, "MSG_DeployingJDBCDriversFailed", toJar.getPath(), libsDir.getPath());
                        eventSupport.fireProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, msg));
                        return;
                    }
                }
                // set the restart flag
                manager.setNeedsRestart(true);
            }
            eventSupport.fireProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED, "")); // NOI18N
        }

        private void deployDriversAs7() {
            List<FileObject> jdbcDriverURLs = jdbcDriversToDeploy(true);
            // deploy the driers if needed
            if (!jdbcDriverURLs.isEmpty()) {
                JBProperties properties = manager.getProperties();
                File deployDir = properties.getDeployDir();
                for (FileObject file : jdbcDriverURLs) {
                    try {
                        String message = JB7Deployer.deployFile(FileUtil.toFile(file),
                                deployDir);
                        if (message != null) {
                            eventSupport.fireProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE,
                                    CommandType.DISTRIBUTE, StateType.FAILED, message));
                            return;
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.INFO, null, e);
                        String msg = NbBundle.getMessage(JBDriverDeployer.class, "MSG_DeployingJDBCDriversFailed", new File(deployDir, file.getNameExt()).getPath(), deployDir.getPath());
                        eventSupport.fireProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE,
                                CommandType.DISTRIBUTE, StateType.FAILED, msg));
                        return;
                    } catch (InterruptedException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                        String msg = NbBundle.getMessage(JBDriverDeployer.class, "MSG_DeployingJDBCDriversInterrupted");
                        eventSupport.fireProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE,
                                CommandType.DISTRIBUTE, StateType.FAILED, msg));
                        return;
                    }
                }
                // set the restart flag
                manager.setNeedsRestart(true);
            }
            eventSupport.fireProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED, "")); // NOI18N
        }

        /** Returns a list of jdbc drivers that need to be deployed. */
        private List<FileObject> jdbcDriversToDeploy(boolean as7) {
            List<FileObject> jdbcDriverFiles = new ArrayList<FileObject>();
            Collection<File> driverCP = getJDBCDriverClasspath(as7);
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
        private Collection<File> getJDBCDriverClasspath(boolean as7) {
            JBProperties properties = manager.getProperties();
            if (!as7) {
                return Arrays.asList(properties.getLibsDir().listFiles());
            }
            else {
                return Arrays.asList(properties.getDeployDir().listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".jar"); // NOI18N
                    }
                }));
            }
        }
        
        public DeploymentStatus getDeploymentStatus() {
            return eventSupport.getDeploymentStatus();
        }

        public TargetModuleID[] getResultTargetModuleIDs() {
            return null;
        }

        public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
            return null;
        }

        public boolean isCancelSupported() {
            return false;
        }

        public void cancel() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("Cancel is not supported"); // NOI18N
        }

        public boolean isStopSupported() {
            return false;
        }

        public void stop() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("Stop is not supported"); // NOI18N
        }

        public void addProgressListener(ProgressListener progressListener) {
            eventSupport.addProgressListener(progressListener);
        }

        public void removeProgressListener(ProgressListener progressListener) {
            eventSupport.removeProgressListener(progressListener);
        }
        
    }
}
