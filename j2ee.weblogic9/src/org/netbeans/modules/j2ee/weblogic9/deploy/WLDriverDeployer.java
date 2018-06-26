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
