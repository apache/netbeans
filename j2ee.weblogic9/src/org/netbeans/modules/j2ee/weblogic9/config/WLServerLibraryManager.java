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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryManager;
import org.netbeans.modules.j2ee.weblogic9.ProgressObjectSupport;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.CommandBasedDeployer;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class WLServerLibraryManager implements ServerLibraryManager {

    private static final Logger LOGGER = Logger.getLogger(WLServerLibraryManager.class.getName());

    private static final String JSF_SPEC_TITLE = "JavaServer Faces"; // NOI18N

    private static final Version JSF2_SPEC_VERSION = Version.fromJsr277OrDottedNotationWithFallback("2.0"); // NOI18N

    private static final String JAX_RS_SPEC_TITLE_START = "JAX-RS"; // NOI18N

    private static final Version JAX_RS_SPEC_VERSION = Version.fromJsr277OrDottedNotationWithFallback("1.1"); // NOI18N

    private static final Version JSF2_SUPPORTED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.1.1"); // NOI18N

    private static final Version JAX_RS_SUPPORTED_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.1.1"); // NOI18N

    private final WLDeploymentManager manager;

    private final WLServerLibrarySupport support;

    public WLServerLibraryManager(WLDeploymentManager manager) {
        this.manager = manager;
        String domainDir = manager.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);
        assert domainDir != null;
        String serverDir = manager.getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
        assert serverDir != null;
        this.support = new WLServerLibrarySupport(new File(serverDir), new File(domainDir));
    }

    @Override
    public void deployLibraries(Set<ServerLibraryDependency> libraries) throws ConfigurationException {
        Set<ServerLibraryDependency> notHandled = filterDeployed(libraries);

        Set<File> toDeploy = new HashSet<File>();
        Map<ServerLibrary, File> deployable = support.getDeployableFiles();
        for (Iterator<ServerLibraryDependency> it = notHandled.iterator(); it.hasNext(); ) {
            ServerLibraryDependency range = it.next();
            for (Map.Entry<ServerLibrary, File> entry : deployable.entrySet()) {
                if (range.versionMatches(entry.getKey())) {
                    it.remove();
                    toDeploy.add(entry.getValue());
                    break;
                }
            }
        }

        if (!notHandled.isEmpty()) {
            throw new MissingLibrariesException(NbBundle.getMessage(WLServerLibraryManager.class, "MSG_DidNotFindServerLibraries"), notHandled);
        }

        try {
            deployFiles(toDeploy);
        } finally {
            FileObject fo = WLPluginProperties.getDomainConfigFileObject(manager);
            if (fo != null) {
                fo.refresh();
            }
        }
    }

    // this handles only archives
    @Override
    public Set<ServerLibrary> getDeployableLibraries() {
        Map<ServerLibrary, File> deployable = support.getDeployableFiles();
        if (JSF2_SUPPORTED_SERVER_VERSION.isBelowOrEqual(manager.getServerVersion())) {
            // we are handling jsf 2 in dummy library here - it should not be offered
            // via this API method, but for legacy apps the missing/deploy machinery
            // has to be available
            for (Iterator<Map.Entry<ServerLibrary, File>> it = deployable.entrySet().iterator(); it.hasNext();) {
                Map.Entry<ServerLibrary, File> entry = it.next();
                ServerLibrary lib = entry.getKey();
                if (JSF_SPEC_TITLE.equals(lib.getSpecificationTitle())
                        && JSF2_SPEC_VERSION.isBelowOrEqual(lib.getSpecificationVersion())
                        // defensive check on size
                        && entry.getValue().length() < 10240) {
                    it.remove();
                    break;
                }
            }
        }
        if (JAX_RS_SUPPORTED_SERVER_VERSION.isBelowOrEqual(manager.getServerVersion())) {
            // we are handling jersey in dummy library here - it should not be offered
            // via this API method, but for legacy apps the missing/deploy machinery
            // has to be available
            // TODO perhaps we should filter out the jersey based on modules on classpath
            // rather than constant
            for (Iterator<Map.Entry<ServerLibrary, File>> it = deployable.entrySet().iterator(); it.hasNext();) {
                Map.Entry<ServerLibrary, File> entry = it.next();
                ServerLibrary lib = entry.getKey();
                if (lib.getSpecificationTitle() != null &&
                        lib.getSpecificationTitle().startsWith(JAX_RS_SPEC_TITLE_START)
                        && JAX_RS_SPEC_VERSION.isBelowOrEqual(lib.getSpecificationVersion())
                        // defensive check on size
                        && entry.getValue().length() < 10240) {
                    it.remove();
                    break;
                }
            }
        }
        return deployable.keySet();
    }

    @Override
    public Set<ServerLibrary> getDeployedLibraries() {
        Set<ServerLibrary> result = new HashSet<ServerLibrary>();
        for (WLServerLibrarySupport.WLServerLibrary lib : support.getDeployedLibraries()) {
            result.add(ServerLibraryFactory.createServerLibrary(lib));
        }
        return result;
    }

    @Override
    public Set<ServerLibraryDependency> getDeployableDependencies(Set<ServerLibraryDependency> libraries) {
        Set<ServerLibraryDependency> notHandled = filterDeployed(libraries);

        Set<ServerLibraryDependency> result = new HashSet<ServerLibraryDependency>();
        Map<ServerLibrary, File> deployable = support.getDeployableFiles();
        for (Iterator<ServerLibraryDependency> it = notHandled.iterator(); it.hasNext(); ) {
            ServerLibraryDependency range = it.next();
            for (Map.Entry<ServerLibrary, File> entry : deployable.entrySet()) {
                if (range.versionMatches(entry.getKey())) {
                    result.add(range);
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public Set<ServerLibraryDependency> getMissingDependencies(Set<ServerLibraryDependency> libraries) {
        Set<ServerLibraryDependency> notHandled = filterDeployed(libraries);

        Map<ServerLibrary, File> deployable = support.getDeployableFiles();
        for (Iterator<ServerLibraryDependency> it = notHandled.iterator(); it.hasNext(); ) {
            ServerLibraryDependency range = it.next();
            for (Map.Entry<ServerLibrary, File> entry : deployable.entrySet()) {
                if (range.versionMatches(entry.getKey())) {
                    it.remove();
                    break;
                }
            }
        }

        return notHandled;
    }

    private void deployFiles(Set<File> libraries) throws ConfigurationException {
        CommandBasedDeployer deployer = new CommandBasedDeployer(manager);
        ProgressObject po = deployer.deployLibraries(libraries, manager.getDeployTargets());
        if (!ProgressObjectSupport.waitFor(po) || po.getDeploymentStatus().isFailed()) {
            String msg = NbBundle.getMessage(WLDatasourceManager.class, "MSG_FailedToDeployLibrary", po.getDeploymentStatus().getMessage());
            throw new ConfigurationException(msg);
        }
    }

    private Set<ServerLibraryDependency> filterDeployed(Set<ServerLibraryDependency> libraries) {
        Set<ServerLibraryDependency> notHandled = new HashSet<ServerLibraryDependency>(libraries);
        Set<ServerLibrary> deployed = getDeployedLibraries();

        for (ServerLibraryDependency range : libraries) {
            for (ServerLibrary lib : deployed) {
                if (range.versionMatches(lib)) {
                    notHandled.remove(range);
                    break;
                }
            }
        }

        return notHandled;
    }
}
