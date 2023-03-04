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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.support.hostinfo.HostInfoProvider;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.nativeexecution.support.hostinfo.HostInfoProvider.class, position = 10)
public final class SlowHostInfoProvider implements HostInfoProvider {

    private static final java.util.logging.Logger log = Logger.getInstance();

    @Override
    public HostInfo getHostInfo(final ExecutionEnvironment execEnv) throws IOException, InterruptedException {
        boolean enabled = Boolean.getBoolean("dlight.nativeexecution.SlowHostInfoProviderEnabled"); // NOI18N

        if (!enabled) {
            return null;
        }

        final Collection<? extends HostInfoProvider> providers = Lookup.getDefault().lookupAll(HostInfoProvider.class);
        HostInfo result = null;
        int providerIdx = 0;

        for (HostInfoProvider provider : providers) {
            if (provider == this) {
                continue;
            }

            providerIdx++;

            try {
                for (int i = 0; i < 3; i++) {
                    try {
                        log.log(Level.INFO, "Trying hard to get some information about the host... Not an easy task... [provider {0}/ delay {1}]", new Object[]{providerIdx, i}); // NOI18N
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        log.log(Level.SEVERE, "InterruptedException", ex); // NOI18N
                    }
                }
                result = provider.getHostInfo(execEnv);
            } catch (IOException ex) {
                String msg = "Exception while recieving hostinfo for " + execEnv.toString(); // NOI18N
                log.log(Level.SEVERE, msg, ex);
            }
            if (result != null) {
                break;
            }
        }

        return result;
    }
}
