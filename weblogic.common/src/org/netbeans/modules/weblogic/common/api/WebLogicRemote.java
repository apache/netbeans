/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
