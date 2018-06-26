/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
