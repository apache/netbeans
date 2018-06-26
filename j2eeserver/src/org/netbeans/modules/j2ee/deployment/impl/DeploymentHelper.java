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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.deployment.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;

/**
 * Helper class containing helper methods to deploy various Java EE stuff.
 *
 * @author Petr Hejl
 */
public final class DeploymentHelper {

    private static final Logger LOGGER = Logger.getLogger(DeploymentHelper.class.getName());

    public DeploymentHelper() {
        super();
    }

    public static void deployMessageDestinations(J2eeModuleProvider jmp) throws ConfigurationException {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (jmp.getServerInstanceID ());
        if (si != null) {
            si.deployMessageDestinations(jmp.getConfigSupport().getMessageDestinations());
        } else {
            LOGGER.log(Level.WARNING,
                    "The message destinations cannot be deployed because the server instance cannot be found."); // NOI18N
        }
    }

    public static void deployJdbcDrivers(J2eeModuleProvider jmp, ProgressUI progress)
            throws ConfigurationException, TimeoutException {

        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (jmp.getServerInstanceID ());
        if (si != null) {
            Set<Datasource> moduleDatasources = jmp.getConfigSupport().getDatasources();
            if(moduleDatasources != null && moduleDatasources.size() > 0) {
                JDBCDriverDeployer jdbcDriverDeployer = si.getJDBCDriverDeployer();
                if(jdbcDriverDeployer != null) {
                    // Currently it is not possible to select target to which modules will
                    // be deployed. Lets use the first one.
                    ServerTarget targets[] = si.getTargets();
                    if (targets.length > 0) {
                        Target target = targets[0].getTarget();
                        if (jdbcDriverDeployer.supportsDeployJDBCDrivers(target)) {
                            ProgressObject po = jdbcDriverDeployer.deployJDBCDrivers(target, moduleDatasources);
                            ProgressObjectUtil.trackProgressObject(progress, po, Long.MAX_VALUE);
                        }
                    }
                }
            }
        } else {
            LOGGER.log(Level.WARNING,
                    "The JDBC drivers cannot be deployed because the server instance cannot be found."); // NOI18N
        }
    }

    public static void deployDatasources(J2eeModuleProvider jmp) throws ConfigurationException, DatasourceAlreadyExistsException {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (jmp.getServerInstanceID ());
        if (si != null) {
            Set<Datasource> moduleDS = jmp.getConfigSupport().getDatasources();
            si.deployDatasources(moduleDS);
        } else {
            LOGGER.log(Level.WARNING,
                    "The data sources cannot be deployed because the server instance cannot be found.");
        }
    }

    public static void deployServerLibraries(J2eeModuleProvider jmp) throws ConfigurationException {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (jmp.getServerInstanceID ());
        if (si != null) {
            Set<ServerLibraryDependency> libraries = jmp.getConfigSupport().getLibraries();
            if (jmp instanceof J2eeApplicationProvider) {
                libraries = new HashSet<ServerLibraryDependency>(libraries);
                J2eeApplicationProvider app = (J2eeApplicationProvider) jmp;
                for (J2eeModuleProvider p : app.getChildModuleProviders()) {
                    libraries.addAll(p.getConfigSupport().getLibraries());
                }
            }
            si.deployLibraries(libraries);
        } else {
            LOGGER.log(Level.WARNING,
                    "The libraries cannot be deployed because the server instance cannot be found.");
        }
    }
}
