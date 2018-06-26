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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.weblogic9;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLSharedState;
import org.netbeans.modules.weblogic.common.api.WebLogicConfiguration;
import org.openide.util.NbBundle;

/**
 * The main entry point to the plugin. Keeps the required static data for the
 * plugin and returns the DeploymentManagers required for deployment and
 * configuration. Does not directly perform any interaction with the server.
 *
 * @author Kirill Sorokin
 * @author Petr Hejl
 */
public class WLDeploymentFactory implements DeploymentFactory {

    public static final String SERVER_ID = "WebLogic9"; // NOI18N

    public static final String URI_PREFIX = "deployer:WebLogic:http://"; // NOI18N

    public static final int DEFAULT_PORT = 7001;

    public static final Version VERSION_10 = Version.fromJsr277NotationWithFallback("10"); // NOI18N

    public static final Version VERSION_11 = Version.fromJsr277NotationWithFallback("11"); // NOI18N

    public static final Version VERSION_1212 = Version.fromJsr277NotationWithFallback("12.1.2"); // NOI18N

    public static final Version VERSION_1221 = Version.fromJsr277NotationWithFallback("12.2.1"); // NOI18N

    private static final Logger LOGGER = Logger.getLogger(WLDeploymentFactory.class.getName());

    /**
     * The singleton instance of the factory
     * <p>
     * <i>GuardedBy(WLDeploymentFactory.class)</i>
     */
    private static WLDeploymentFactory instance;

    /*
     * We need to cache deployment manager. The server instance and server restart
     * logic depend on this.
     * <p>
     * <i>GuardedBy(WLDeploymentFactory.class)</i>
     */
    private static Map<InstanceProperties, WLDeploymentManager> managerCache =
            new WeakHashMap<InstanceProperties, WLDeploymentManager>();

    /*
     * We need to share the state across the instances of deployment managers.
     * <p>
     * <i>GuardedBy(WLDeploymentFactory.class)</i>
     */
    private static Map<InstanceProperties, WLSharedState> stateCache =
            new WeakHashMap<InstanceProperties, WLSharedState>();

    /*
     * We share and cache ClassLoaders to spare resources.
     * <p>
     * <i>GuardedBy(WLDeploymentFactory.class)</i>
     */
    private static Map<InstanceProperties, WLClassLoader> classLoaderCache =
            new WeakHashMap<InstanceProperties, WLClassLoader>();

    /**
     * The singleton factory method
     *
     * @return the singleton instance of the factory
     */
    public static synchronized WLDeploymentFactory getInstance() {
        if (instance == null) {
            instance = new WLDeploymentFactory();
            //DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);
        }
        return instance;
    }

    public static String getUrl(WebLogicConfiguration config) {
        File domain = config.getDomainHome();
        return getUrl(config.getHost(), config.getPort(),
                config.getServerHome().getAbsolutePath(), domain == null ? null : domain.getAbsolutePath());
    }

    public static String getUrl(String host, int port, String serverHome, String domainHome) {
        StringBuilder sb = new StringBuilder(WLDeploymentFactory.URI_PREFIX);
        sb.append(host).append(":").append(port).append(":").append(serverHome); // NOI18N
        if (domainHome != null) {
            sb.append(":").append(domainHome); // NOI18N;
        }
        return sb.toString();
    }

    @Override
    public boolean handlesURI(String uri) {
        if (uri != null && uri.startsWith(URI_PREFIX)) {
            return true;
        }

        return false;
    }

    @Override
    public DeploymentManager getDeploymentManager(String uri, String username,
            String password) throws DeploymentManagerCreationException {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "getDeploymentManager, uri: {0} username: {1} password: {2}",
                    new Object[] {uri, username, password});
        }

        InstanceProperties props = InstanceProperties.getInstanceProperties(uri);
        if (props == null) {
            throw new DeploymentManagerCreationException("Could not create deployment manager for " + uri);
        }

        synchronized (WLDeploymentFactory.class) {
            WLDeploymentManager dm = managerCache.get(props);
            if (dm != null) {
                return dm;
            }

            WLSharedState shared = getSharedState(props);

            String[] parts = uri.split(":"); // NOI18N
            String host = parts[3].substring(2);
            String port = parts[4] != null ? parts[4].trim() : parts[4];

            dm = new WLDeploymentManager(uri, host, port, false, shared);
            if (dm.getCommonConfiguration() != null) {
                managerCache.put(props, dm);
            }
            return dm;
        }
    }

    @Override
    public DeploymentManager getDisconnectedDeploymentManager(String uri)
            throws DeploymentManagerCreationException {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "getDisconnectedDeploymentManager, uri: {0}", uri);
        }

        // TODO should we decorate it somehow ?
        WLDeploymentManager dm = (WLDeploymentManager) getDeploymentManager(uri, null, null);
        if (dm.getCommonConfiguration() == null) {
            LOGGER.log(Level.INFO, "Invalid WebLogic instance: {0}", uri);
            return null;
        }
        return dm;
    }

    @Override
    public String getProductVersion() {
        return NbBundle.getMessage(WLDeploymentFactory.class, "TXT_productVersion");
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(WLDeploymentFactory.class, "TXT_displayName");
    }

    WLClassLoader getClassLoader(WLDeploymentManager manager) {
        synchronized (WLDeploymentFactory.class) {
            InstanceProperties props = manager.getInstanceProperties();
            WLClassLoader classLoader = classLoaderCache.get(props);
            if (classLoader != null) {
                return classLoader;
            }

            // two instances may have the same classpath because of the same
            // server root directory
            for (Map.Entry<InstanceProperties, WLClassLoader> entry : classLoaderCache.entrySet()) {
                // FIXME base the check on classpath - it would be more safe
                // more expensive as well
                String serverRootCached = entry.getKey().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
                String serverRootFresh = props.getProperty(WLPluginProperties.SERVER_ROOT_ATTR);

                if ((serverRootCached == null) ? (serverRootFresh == null) : serverRootCached.equals(serverRootFresh)) {
                    classLoader = entry.getValue();
                    break;
                }
            }

            if (classLoader == null) {
                classLoader = createClassLoader(manager);
            }

            classLoaderCache.put(props, classLoader);
            return classLoader;
        }
    }

    private static synchronized WLSharedState getSharedState(InstanceProperties props) {
        WLSharedState mutableState = stateCache.get(props);
        if (mutableState == null) {
            mutableState = new WLSharedState();
            stateCache.put(props, mutableState);
        }
        mutableState.configure(props);
        return mutableState;
    }

    private WLClassLoader createClassLoader(WLDeploymentManager manager) {
        LOGGER.log(Level.FINE, "Creating classloader for {0}", manager.getUri());
        try {
            File[] classpath = WLPluginProperties.getClassPath(manager);
            URL[] urls = new URL[classpath.length];
            for (int i = 0; i < classpath.length; i++) {
                urls[i] = classpath[i].toURI().toURL();
            }
            WLClassLoader classLoader = new WLClassLoader(urls, WLDeploymentManager.class.getClassLoader());
            LOGGER.log(Level.FINE, "Classloader for {0} created successfully", manager.getUri());
            return classLoader;
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, null, e);
        }
        return new WLClassLoader(new URL[] {}, WLDeploymentManager.class.getClassLoader());
    }

    private static class WLClassLoader extends URLClassLoader {

        public WLClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            Class<?> clazz = super.findClass(name);
            if (LOGGER.isLoggable(Level.FINEST)) {
                String filename = name.replace('.', '/'); // NOI18N
                int index = filename.indexOf('$'); // NOI18N
                if (index > 0) {
                    filename = filename.substring(0, index);
                }
                filename = filename + ".class"; // NOI18N

                URL url = this.getResource(filename);
                LOGGER.log(Level.FINEST, "WebLogic classloader asked for {0}", name);
                if (url != null) {
                    LOGGER.log(Level.FINEST, "WebLogic classloader found {0} at {1}",new Object[]{name, url});
                }
            }
            return clazz;
        }

        @Override
        protected PermissionCollection getPermissions(CodeSource codeSource) {
            Permissions p = new Permissions();
            p.add(new AllPermission());
            return p;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            // get rid of annoying warnings
            if (name.indexOf("jndi.properties") != -1 || name.indexOf("i18n_user.properties") != -1) { // NOI18N
                return Collections.enumeration(Collections.<URL>emptyList());
            }

            return super.getResources(name);
        }
    }

}
