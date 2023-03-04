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

package org.netbeans.modules.weblogic.common.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.weblogic.common.ProxyUtils;
import org.netbeans.modules.weblogic.common.spi.WebLogicTrustHandler;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public final class WebLogicRemote {

    private final WebLogicConfiguration config;

    WebLogicRemote(WebLogicConfiguration config) {
        this.config = config;
    }

    public <T> T executeAction(@NonNull Callable<T> action, @NullAllowed Callable<String> nonProxy) throws Exception {
        synchronized (this) {
            String originalNonProxyHosts = System.getProperty(ProxyUtils.HTTP_NON_PROXY_HOSTS);
            String nonProxyHosts = ProxyUtils.getNonProxyHosts(nonProxy);
            if (nonProxyHosts != null) {
                System.setProperty(ProxyUtils.HTTP_NON_PROXY_HOSTS, nonProxyHosts);
            }
            if (config.isSecured()) {
                WebLogicTrustHandler handler = Lookup.getDefault().lookup(WebLogicTrustHandler.class);
                if (handler != null) {
                    for (Map.Entry<String, String> e : handler.getTrustProperties(config).entrySet()) {
                        System.setProperty(e.getKey(), e.getValue());
                    }
                }
            }

            try {
                return action.call();
            } finally {
                // this is not really safe considering other threads, but it is the best we can do
                if (originalNonProxyHosts == null) {
                    System.clearProperty(ProxyUtils.HTTP_NON_PROXY_HOSTS);
                } else {
                    System.setProperty(ProxyUtils.HTTP_NON_PROXY_HOSTS, originalNonProxyHosts);
                }
            }
        }
    }

    public <T> T executeAction(@NonNull final JmxAction<T> action, @NullAllowed Callable<String> nonProxy) throws Exception {
        return executeAction(new Callable<T>() {

            @Override
            public T call() throws Exception {
                JMXServiceURL url = new JMXServiceURL(config.isSecured() ? "t3s" : "t3", // NOI18N
                        config.getHost(), config.getPort(), "/jndi/weblogic.management.mbeanservers.domainruntime"); // NOI18N

                String username = config.getUsername();
                String password = config.getPassword();

                Map<String, Object> env = new HashMap<String, Object>();
                env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES,
                        "weblogic.management.remote"); // NOI18N
                env.put(javax.naming.Context.SECURITY_PRINCIPAL, username);
                env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);
                env.put("jmx.remote.credentials", // NOI18N
                        new String[]{username, password});
                env.put("jmx.remote.protocol.provider.class.loader", //NOI18N
                        config.getLayout().getClassLoader());

                JMXConnector jmxConnector = JMXConnectorFactory.newJMXConnector(url, env);
                jmxConnector.connect();
                try {
                    return action.execute(jmxConnector.getMBeanServerConnection());
                } finally {
                    jmxConnector.close();
                }
            }
        }, nonProxy);
    }

    public static interface JmxAction<T> {

        T execute(MBeanServerConnection connection) throws Exception;

    }
}
