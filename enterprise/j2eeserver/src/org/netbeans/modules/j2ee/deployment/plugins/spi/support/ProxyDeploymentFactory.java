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

package org.netbeans.modules.j2ee.deployment.plugins.spi.support;

import java.util.Map;
import java.util.regex.Pattern;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.openide.util.Parameters;

/**
 * Provides a proxying implementation of {@link DeploymentFactory}. Handles
 * optional <code>urlPattern</code> attribute as precheck for url the plugin
 * can handle. Designed to be used via XML layer filesystem.
 * <p>
 * The <code>urlPattern</code> attribute is expected to be regexp string.
 * If it is present the server instance uri must match the regexp otherwise
 * {@link #handlesURI(java.lang.String)} will automatically return false
 * (before querying the delegate).
 *
 * @author Petr Hejl
 * @since 1.65
 */
public final class ProxyDeploymentFactory implements DeploymentFactory {

    private final Map attributes;

    private final Pattern urlPattern;

    private DeploymentFactory delegate;

    private ProxyDeploymentFactory(Map attributes) {
        this.attributes = attributes;

        String pattern = (String) attributes.get("urlPattern"); // NOI18N
        if (pattern != null) {
            urlPattern = Pattern.compile(pattern);
        } else {
            urlPattern = null;
        }
    }

    public static ProxyDeploymentFactory create(Map map) {
        return new ProxyDeploymentFactory(map);
    }

    @Override
    public boolean handlesURI(String string) {
        if (string == null) {
            return false;
        }
        if (urlPattern != null && !urlPattern.matcher(string).matches()) {
            return false;
        }
        return getDelegate().handlesURI(string);
    }

    @Override
    public String getProductVersion() {
        return getDelegate().getProductVersion();
    }

    @Override
    public String getDisplayName() {
        return getDelegate().getDisplayName();
    }

    @Override
    public DeploymentManager getDisconnectedDeploymentManager(String string) throws DeploymentManagerCreationException {
        return getDelegate().getDisconnectedDeploymentManager(string);
    }

    @Override
    public DeploymentManager getDeploymentManager(String string, String string1, String string2) throws DeploymentManagerCreationException {
        return getDelegate().getDeploymentManager(string, string1, string2);
    }

    private DeploymentFactory getDelegate() {
        synchronized (this) {
            if (delegate != null) {
                return delegate;
            }
        }

        DeploymentFactory factory = (DeploymentFactory) attributes.get("delegate"); // NOI18N
        Parameters.notNull("delegate", factory); // NOI18N

        synchronized (this) {
            if (delegate == null) {
                delegate = factory;
            }
            return delegate;
        }
    }
}
