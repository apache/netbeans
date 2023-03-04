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
package org.netbeans.modules.j2ee.deployment.plugins.api;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.bridge.BridgingServerInstanceProvider;
import org.netbeans.modules.j2ee.deployment.impl.bridge.ServerInstanceProviderLookup;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.util.Parameters;

/**
 * The utility class providing bridge to API classes of the common server.
 *
 * @author Petr Hejl
 * @since 1.88
 */
public final class CommonServerBridge {

    private CommonServerBridge() {
        super();
    }

    private static final Logger LOGGER = Logger.getLogger(CommonServerBridge.class.getName());

    /**
     * Returns the {@link ServerInstance} corresponding to the server instance
     * identified by the given url.
     *
     * @param instanceUrl the server instance url
     * @return the {@link ServerInstance} corresponding to the server instance
     *             identified by the given url
     */
    @NonNull
    public static ServerInstance getCommonInstance(@NonNull String instanceUrl) {
        Parameters.notNull("instanceUrl", instanceUrl);

        org.netbeans.modules.j2ee.deployment.impl.ServerInstance instance =
                ServerRegistry.getInstance().getServerInstance(instanceUrl);
        ServerInstance bridgingInstance = null;
        Collection<? extends ServerInstanceProvider> providers =
                ServerInstanceProviderLookup.getInstance().lookupAll(ServerInstanceProvider.class);
        for (ServerInstanceProvider provider : providers) {
            if (provider instanceof BridgingServerInstanceProvider) {
                bridgingInstance = ((BridgingServerInstanceProvider) provider).getBridge(instance);
                if (bridgingInstance != null) {
                    break;
                }
            }
        }
        if (bridgingInstance == null) {
            LOGGER.log(Level.INFO, "No bridging instance for {0}", instance);
            throw new IllegalStateException("Instance registered without UI. No common instance available.");
        }
        return bridgingInstance;
    }
}
