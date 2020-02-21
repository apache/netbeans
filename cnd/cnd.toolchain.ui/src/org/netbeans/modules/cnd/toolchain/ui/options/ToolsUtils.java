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

package org.netbeans.modules.cnd.toolchain.ui.options;

import java.io.IOException;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;

/**
 *
 */
public class ToolsUtils {
    private ToolsUtils() {
    }

    //TODO: we should be ensured already....check
    public static void ensureHostSetup(ExecutionEnvironment env) {
        if (env != null) {
            ServerList.get(env); // this will ensure the remote host is setup
        }
    }
    public static boolean isDevHostValid(ExecutionEnvironment env) {
        ServerRecord record = ServerList.get(env);
        return record != null && record.isOnline();
    }

    public static String getDefaultDirectory(ExecutionEnvironment env) {
        String seed;
        if (env.isLocal()) {
            seed = System.getProperty("user.home"); // NOI18N
        } else if (!(HostInfoUtils.isHostInfoAvailable(env) && ConnectionManager.getInstance().isConnectedTo(env))) {
            seed = null;
        } else {
            try {
                seed = HostInfoUtils.getHostInfo(env).getUserDir();
            } catch (IOException ex) {
                seed = null;
            } catch (ConnectionManager.CancellationException ex) {
                seed = null;
            }
        }
        return seed;
    }
}
