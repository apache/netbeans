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

package org.netbeans.modules.j2ee.weblogic9;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.optional.NonProxyHostsHelper;
import org.netbeans.modules.weblogic.common.api.WebLogicConfiguration;
import org.netbeans.modules.weblogic.common.spi.WebLogicTrustHandler;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public final class WLConnectionSupport {

    private final WLDeploymentManager deploymentManager;

    // full weblogic code is setting this, causing CNFE on DWP
    private static final String PORTABLE_OBJECT_PROPERTY = "javax.rmi.CORBA.PortableRemoteObjectClass"; // NOI18N

    // we should hide this constructor
    public WLConnectionSupport(WLDeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    public <T> T executeAction(Callable<T> action) throws Exception {
        synchronized (this) {
            ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();

            String portable = System.getProperty(PORTABLE_OBJECT_PROPERTY);

            String originalNonProxyHosts = System.getProperty(NonProxyHostsHelper.HTTP_NON_PROXY_HOSTS);
            String configuredNonProxyHosts = NonProxyHostsHelper.getNonProxyHosts();

            boolean nonProxyHostsChanged = false;
            if (!configuredNonProxyHosts.isEmpty()) {
                String nonProxyHosts;
                if (originalNonProxyHosts != null) {
                    nonProxyHosts = NonProxyHostsHelper.compactNonProxyHosts(
                            originalNonProxyHosts + "," + configuredNonProxyHosts); // NOI18N
                } else {
                    nonProxyHosts = configuredNonProxyHosts;
                }
                if (!nonProxyHosts.equals(originalNonProxyHosts)) {
                    nonProxyHostsChanged = true;
                    System.setProperty(NonProxyHostsHelper.HTTP_NON_PROXY_HOSTS, nonProxyHosts);
                }
            }

            if (deploymentManager.getCommonConfiguration().isSecured()) {
                WebLogicTrustHandler handler = Lookup.getDefault().lookup(WebLogicTrustHandler.class);
                if (handler != null) {
                    for (Map.Entry<String, String> e : handler.getTrustProperties(deploymentManager.getCommonConfiguration()).entrySet()) {
                        System.setProperty(e.getKey(), e.getValue());
                    }
                }
            }

            Thread.currentThread().setContextClassLoader(
                    WLDeploymentFactory.getInstance().getClassLoader(deploymentManager));
            try {
                return action.call();
            } finally {
                Thread.currentThread().setContextClassLoader(originalLoader);

                // this is not really safe considering other threads, but it is the best we can do
                if (nonProxyHostsChanged) {
                    if (originalNonProxyHosts == null) {
                        System.clearProperty(NonProxyHostsHelper.HTTP_NON_PROXY_HOSTS);
                    } else {
                        System.setProperty(NonProxyHostsHelper.HTTP_NON_PROXY_HOSTS, originalNonProxyHosts);
                    }
                }

                // this is not really safe considering other threads, but it is the best we can do
                if (portable == null) {
                    System.clearProperty(PORTABLE_OBJECT_PROPERTY);
                } else {
                    System.setProperty(PORTABLE_OBJECT_PROPERTY, portable);
                }
            }
        }
    }

    public <T> T executeAction(final JMXAction<T> action) throws Exception {
        InstanceProperties instanceProperties = deploymentManager.getInstanceProperties();
        String host = instanceProperties.getProperty(WLPluginProperties.HOST_ATTR);
        String port = instanceProperties.getProperty(WLPluginProperties.PORT_ATTR);
        if ((host == null || host.trim().length() == 0
                && (port == null || port.trim().length() == 0))) {
            
            if (!deploymentManager.isRemote()) {            
                // getDomainConfiguration instantiate DocumentBuilderFactory
                // if we would add it inside call such factory could be loaded
                // from weblogic classes causing troubles, see #189483
                WebLogicConfiguration config = deploymentManager.getCommonConfiguration();
                host = config.getHost();
                port = Integer.toString(config.getPort());
            }
            if ((host == null || host.trim().length() == 0
                    && (port == null || port.trim().length() == 0))) {
                String[] parts = deploymentManager.getUri().split(":"); // NOI18N
                host = parts[3].substring(2);
                port = parts[4] != null ? parts[4].trim() : parts[4];
            }
        }
        
        final String resolvedHost = host.trim();
        final String resolvedPort = port.trim();

        return executeAction(new Callable<T>() {

            @Override
            public T call() throws Exception {
                boolean secured = deploymentManager.getCommonConfiguration().isSecured();
                JMXServiceURL url = new JMXServiceURL(secured ? "t3s" : "t3", resolvedHost, // NOI18N
                        Integer.parseInt(resolvedPort), action.getPath());
                
                String username = deploymentManager.getInstanceProperties().getProperty(
                        InstanceProperties.USERNAME_ATTR);
                String password = deploymentManager.getInstanceProperties().getProperty(
                        InstanceProperties.PASSWORD_ATTR);

                Map<String, Object> env = new HashMap<String, Object>();
                env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES,
                             "weblogic.management.remote"); // NOI18N
                env.put(javax.naming.Context.SECURITY_PRINCIPAL, username);
                env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);
                env.put("jmx.remote.credentials", // NOI18N
                        new String[] {username, password});

                JMXConnector jmxConnector = JMXConnectorFactory.newJMXConnector(url, env);
                jmxConnector.connect();
                try {
                    return action.call(jmxConnector.getMBeanServerConnection());
                } finally {
                    jmxConnector.close();
                }
            }
        });
    }

    public static interface JMXAction<T> {

        T call(MBeanServerConnection connection) throws Exception;

        String getPath();

    }
    
    public abstract static class JMXRuntimeAction<T> implements JMXAction<T> {

        public abstract T call(MBeanServerConnection connection, ObjectName service) throws Exception;

        @Override
        public final T call(MBeanServerConnection connection) throws Exception {
            ObjectName service = new ObjectName("com.bea:Name=DomainRuntimeService," // NOI18N
                    + "Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean"); // NOI18N
            return call(connection, service);
        }

        @Override
        public final String getPath() {
            return "/jndi/weblogic.management.mbeanservers.domainruntime"; // NOI18N
        }
    }
    
    public abstract static class JMXEditAction<T> implements JMXAction<T> {

        public abstract T call(MBeanServerConnection connection, ObjectName service) throws Exception;

        @Override
        public final T call(MBeanServerConnection connection) throws Exception {
            ObjectName service = new ObjectName("com.bea:Name=EditService," // NOI18N
                    + "Type=weblogic.management.mbeanservers.edit.EditServiceMBean"); // NOI18N
            return call(connection, service);
        }

        @Override
        public final String getPath() {
            return "/jndi/weblogic.management.mbeanservers.edit"; // NOI18N
        }
    }
}
