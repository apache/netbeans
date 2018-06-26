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
    
    public static abstract class JMXRuntimeAction<T> implements JMXAction<T> {

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
    
    public static abstract class JMXEditAction<T> implements JMXAction<T> {

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
