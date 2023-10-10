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
package org.netbeans.modules.javaee.wildfly;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class WildflyDeploymentFactory implements DeploymentFactory {

    public static final String URI_PREFIX = "wildfly-deployer:"; // NOI18N

    private static final String DISCONNECTED_URI = URI_PREFIX + "http://localhost:8080&"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(WildflyDeploymentFactory.class.getName());

    /**
     * Mapping of a instance properties to a deployment factory.
     * <i>GuardedBy(WildflyDeploymentFactory.class)</i>
     */
    private final Map<InstanceProperties, DeploymentFactory> factoryCache =
            new WeakHashMap<InstanceProperties, DeploymentFactory>();

    /**
     * Mapping of a instance properties to a deployment manager.
     * <i>GuardedBy(WildflyDeploymentFactory.class)</i>
     */
    private final Map<InstanceProperties, WildflyDeploymentManager> managerCache =
            new WeakHashMap<InstanceProperties, WildflyDeploymentManager>();

    private final Map<InstanceProperties, WildflyClassLoader> classLoaderCache =
            new WeakHashMap<InstanceProperties, WildflyClassLoader>();

    private static WildflyDeploymentFactory instance;

    private WildflyDeploymentFactory() {
        super();
    }

    public static synchronized WildflyDeploymentFactory getInstance() {
        if (instance == null) {
            instance = new WildflyDeploymentFactory();
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);
        }
        return instance;
    }

    public synchronized WildflyClassLoader getWildFlyClassLoader(InstanceProperties ip) {
        WildflyClassLoader cl = classLoaderCache.get(ip);
        if (cl == null) {
            DeploymentFactory factory = factoryCache.get(ip);
            if (factory != null && factory.getClass().getClassLoader() instanceof WildflyClassLoader) {
                cl = (WildflyClassLoader) factory.getClass().getClassLoader();
            }
            if (cl == null) {
                cl = WildflyClassLoader.createWildFlyClassLoader(ip.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR));
            }
            classLoaderCache.put(ip, cl);
        }
        return cl;
    }

    @Override
    public boolean handlesURI(String uri) {
        if (uri != null && uri.startsWith(URI_PREFIX)) {
            return true;
        }

        return false;
    }

    @Override
    public DeploymentManager getDeploymentManager(String uri, String uname, String passwd) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException(NbBundle.getMessage(WildflyDeploymentFactory.class, "MSG_INVALID_URI", uri)); // NOI18N
        }

        synchronized (WildflyDeploymentFactory.class) {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            if (ip != null) {
                WildflyDeploymentManager dm = managerCache.get(ip);
                if (dm != null) {
                    return dm;
                }
            }

            try {
                DeploymentFactory df = getFactory(uri);
                if (df == null) {
                    throw new DeploymentManagerCreationException(NbBundle.getMessage(WildflyDeploymentFactory.class, "MSG_ERROR_CREATING_DM", uri)); // NOI18N
                }

                String jbURI = uri;
                try {
                    int index1 = uri.indexOf('#'); // NOI18N
                    int index2 = uri.indexOf('&'); // NOI18N
                    int index = Math.min(index1, index2);
                    jbURI = uri.substring(0, index); // NOI18N
                } catch (Exception e) {
                    LOGGER.log(Level.INFO, null, e);
                }

                // see #228619
                // The default host where the DM is connecting is based on
                // serverHost parameter if it is null it uses InetAddress.getLocalHost()
                // which is however based on hostname. If hostname is not mapped
                // to localhost (the interface where the JB is running) we get
                // an excpetion
                if (jbURI.endsWith("as7")) { // NOI18N
                    jbURI = jbURI + "&serverHost=" // NOI18N
                            + (ip != null ? ip.getProperty(WildflyPluginProperties.PROPERTY_HOST) : "localhost"); // NOI18N
                }
                WildflyDeploymentManager dm = new WildflyDeploymentManager(df, uri, jbURI, uname, passwd);
                if (ip != null) {
                    managerCache.put(ip, dm);
                }
                return dm;
            } catch (NoClassDefFoundError e) {
                DeploymentManagerCreationException dmce = new DeploymentManagerCreationException("Classpath is incomplete"); // NOI18N
                dmce.initCause(e);
                throw dmce;
            }
        }
    }

    @Override
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException(NbBundle.getMessage(WildflyDeploymentFactory.class, "MSG_INVALID_URI", uri)); // NOI18N
        }

        try {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            if (ip == null) {
                // null ip either means that the instance is not registered, or that this is the disconnected URL
                if (!DISCONNECTED_URI.equals(uri)) {
                    throw new DeploymentManagerCreationException("JBoss instance " + uri + " is not registered in the IDE."); // NOI18N
                }
            }

            if (ip != null) {
                String root = ip.getProperty(WildflyPluginProperties.PROPERTY_ROOT_DIR);
                if (root == null || !new File(root).isDirectory()) {
                    throw new DeploymentManagerCreationException("Non existent server root " + root); // NOI18N
                }
                String server = ip.getProperty(WildflyPluginProperties.PROPERTY_SERVER_DIR);
                if (server == null || !new File(server).isDirectory()) {
                    throw new DeploymentManagerCreationException("Non existent domain root " + server); // NOI18N
                }
            }

            return new WildflyDeploymentManager(null, uri, null, null, null);
        } catch (NoClassDefFoundError e) {
            DeploymentManagerCreationException dmce = new DeploymentManagerCreationException("Classpath is incomplete"); // NOI18N
            dmce.initCause(e);
            throw dmce;
        }
    }

    @Override
    public String getProductVersion() {
        return NbBundle.getMessage (WildflyDeploymentFactory.class, "LBL_JBossFactoryVersion");
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(WildflyDeploymentFactory.class, "WILDFLY_SERVER_NAME"); // NOI18N
    }

    private DeploymentFactory getFactory(String instanceURL) {
        DeploymentFactory jbossFactory = null;
        try {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(instanceURL);
            synchronized (WildflyDeploymentFactory.class) {
                if (ip != null) {
                    jbossFactory = (DeploymentFactory) factoryCache.get(ip);
                }
               if (jbossFactory == null) {
                    jbossFactory = this;
                    if (ip != null) {
                        factoryCache.put(ip, jbossFactory);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
        }

        return jbossFactory;
    }

}
