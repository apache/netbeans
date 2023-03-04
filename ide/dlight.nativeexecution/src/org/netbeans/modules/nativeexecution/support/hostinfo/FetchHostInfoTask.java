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
package org.netbeans.modules.nativeexecution.support.hostinfo;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.support.Computable;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.Lookup;

public final class FetchHostInfoTask implements Computable<ExecutionEnvironment, HostInfo> {

    private static final java.util.logging.Logger log = Logger.getInstance();

    @Override
    public final HostInfo compute(ExecutionEnvironment execEnv) throws InterruptedException {
        final Collection<? extends HostInfoProvider> providers = Lookup.getDefault().lookupAll(HostInfoProvider.class);
        HostInfo result = null;

        for (HostInfoProvider provider : providers) {
            try {
                result = provider.getHostInfo(execEnv);
            } catch (IOException ex) {
                log.log(Level.INFO, "Exception while receiving hostinfo for " + execEnv.getDisplayName(), ex); //NOI18N
            }
            if (result != null) {
                break;
            }
        }

        return result;
    }
}
