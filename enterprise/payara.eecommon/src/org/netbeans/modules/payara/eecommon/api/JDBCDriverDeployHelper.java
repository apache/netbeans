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

package org.netbeans.modules.payara.eecommon.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
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
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

public class JDBCDriverDeployHelper {

    static public ProgressObject getProgressObject(File driverLoc, List listOfURLS) {
        return new JDBCDriversProgressObject(driverLoc, listOfURLS);
    }

    /**
     * Add JDBC drivers URLs into given List of URL objects.
     *
     * @param drivers Target ULR list where to add drivers.
     * @param jdbcDrivers JDBC drivers to be searched for URLs.
     */
    static private void addDriversURLs(List<URL> drivers, JDBCDriver[] jdbcDrivers) {
        for (JDBCDriver jdbcDriver : jdbcDrivers) {
            URL[] allUrls = jdbcDriver.getURLs();
            for (int i = 0; i < allUrls.length; i++) {
                URL driverUrl = allUrls[i];
                String strUrl = driverUrl.toString();
                if (strUrl.contains("nbinst:/")) { // NOI18N
                    FileObject fo = URLMapper.findFileObject(driverUrl);
                    if (fo != null) {
                        URL localURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                        if (localURL != null) {
                            drivers.add(localURL);
                        }
                    }
                } else {
                    drivers.add(driverUrl);
                }
            }
        } //JDBCDriver
    }

    /**
     * Returns a list of jdbc drivers that need to be deployed.
     *
     * @param driverLocs  JDBC drivers locations (server library directories).
     * @param datasources Server data sources from server resources files.
     * @return List of JDBC drivers URLs to be deployed.
     */
    static public List<URL> getMissingDrivers(File[] driverLocs, Set<Datasource> datasources) {
        List<URL> drivers = new ArrayList<URL>();
        for (Datasource datasource : datasources) {
            String className = datasource.getDriverClassName();
            if (null != className) {
                boolean exists = false;
                for (int j = 0; j < driverLocs.length; j++) {
                    File driverLoc = driverLocs[j];
                    if (driverLoc != null && driverLoc.exists()) {
                        Collection<File> driversLocation = Arrays.asList(driverLoc.listFiles(new Utils.JarFileFilter()));
                        try {
                            exists = ClasspathUtil.containsClass(driversLocation, className);
                        } catch (IOException e) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        }
                        if (exists) {
                            break;
                        }
                    } else {
                        Logger.getLogger("payara-eecommon").finer("Invalid directory for driver deployment");
                    }
                }
                if (!exists) {
                    List<DatabaseConnection> databaseConnections = DatasourceHelper.findDatabaseConnections(datasource);
                    JDBCDriver[] jdbcDrivers;

                    // Use matching configured database connections to find library for resource driver class.
                    if (databaseConnections != null && databaseConnections.size() > 0) {
                        for (DatabaseConnection databaseConnection : databaseConnections) {
                            JDBCDriver connDriver = databaseConnection.getJDBCDriver();
                            if (connDriver != null) {
                                jdbcDrivers = new JDBCDriver[]{connDriver};
                            } else {
                                // old fashioned way - fallback
                                String driverClass = databaseConnection.getDriverClass();
                                jdbcDrivers = JDBCDriverManager.getDefault().getDrivers(driverClass);
                            }
                            addDriversURLs(drivers, jdbcDrivers);
                        }
                    // Will try to find library for resource driver class if there is no database connection
                    // configured for data source. This is fallback option.
                    } else {
                        jdbcDrivers = JDBCDriverManager.getDefault().getDrivers(datasource.getDriverClassName());
                        addDriversURLs(drivers, jdbcDrivers);
                    }
                } //If
            }
        }
        return drivers;
    }

    static private class JDBCDriversProgressObject implements ProgressObject, Runnable {

        private final ProgressEventSupport eventSupport;
        private final File driverLoc;
        private List jdbcDriverURLs;

        public JDBCDriversProgressObject(File driverLoc, List jdbcDriverURLs) {
            eventSupport = new ProgressEventSupport(this); //JDBCDriverDeployHelper.this);
            String msg = NbBundle.getMessage(JDBCDriverDeployHelper.class, "MSG_CheckMissingDrivers");
            eventSupport.fireHandleProgressEvent(null, ProgressEventSupport.createStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, msg, StateType.RUNNING));
            this.driverLoc = driverLoc;
            this.jdbcDriverURLs = jdbcDriverURLs;
        }

        public void run() {
            String msg;
            if (jdbcDriverURLs.size() > 0) {
                boolean success = true;
                for (int i = 0; i < jdbcDriverURLs.size(); i++) {
                    URL jarUrl = (URL) jdbcDriverURLs.get(i);
                    File libsDir = driverLoc;
                    try {
                        File toJar = new File(libsDir, new File(jarUrl.toURI()).getName());
                        try {
                            BufferedInputStream is = new BufferedInputStream(jarUrl.openStream());
                            try {
                                msg = NbBundle.getMessage(JDBCDriverDeployHelper.class, "MSG_DeployDriver", toJar.getPath());
                                eventSupport.fireHandleProgressEvent(null, ProgressEventSupport.createStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, msg, StateType.RUNNING));
                                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(toJar));
                                try {
                                    FileUtil.copy(is, os);
                                } finally {
                                    if (null != os)
                                        try {
                                            os.close();
                                        } catch (IOException ioe) {

                                        }
                                }
                            } finally {
                                if (null != is)
                                    try {
                                        is.close();
                                    } catch (IOException ioe) {
                                        
                                    }
                            }
                        } catch (IOException e) {
                            Logger.getLogger(this.getClass().getName()).log(Level.FINER,"",e);
                            msg = NbBundle.getMessage(JDBCDriverDeployHelper.class, "ERR_DeployDriver", toJar.getPath(), libsDir.getPath());
                            eventSupport.fireHandleProgressEvent(null, ProgressEventSupport.createStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, msg, StateType.FAILED));
                            success = false;
                            continue;
                        }
                    } catch (URISyntaxException | RuntimeException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.FINER,"",ex);
                        msg = NbBundle.getMessage(JDBCDriverDeployHelper.class, "ERR_DeployDriver", jarUrl, libsDir.getPath());
                        eventSupport.fireHandleProgressEvent(null, ProgressEventSupport.createStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, msg, StateType.FAILED));
                        success = false;
                        continue;
                    }
                } //for
                msg = NbBundle.getMessage(JDBCDriverDeployHelper.class, "MSG_DeployDriverComplete");
                if (!success) {
                    msg = NbBundle.getMessage(JDBCDriverDeployHelper.class, "ERR_DeployDriverFailed");
                }
                eventSupport.fireHandleProgressEvent(null, ProgressEventSupport.createStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, msg, success ? StateType.COMPLETED : StateType.FAILED)); // NOI18N
            } else {
                eventSupport.fireHandleProgressEvent(null, ProgressEventSupport.createStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, "", StateType.COMPLETED)); // NOI18N
            }
        }

        public DeploymentStatus getDeploymentStatus() {
            return eventSupport.getDeploymentStatus();
        }

        public TargetModuleID[] getResultTargetModuleIDs() {
            return new TargetModuleID[0];
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
