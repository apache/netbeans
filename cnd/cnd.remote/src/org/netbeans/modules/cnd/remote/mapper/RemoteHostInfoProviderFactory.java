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
package org.netbeans.modules.cnd.remote.mapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.spi.remote.HostInfoProviderFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.remote.HostInfoProviderFactory.class)
public class RemoteHostInfoProviderFactory implements HostInfoProviderFactory {

    public static class RemoteHostInfo extends HostInfoProvider {

        private final ExecutionEnvironment executionEnvironment;
        private Map<String, String> envCache = null;
        private Boolean isCshShell;

        @Override
        public boolean fileExists(String path) {
            try {
                return HostInfoUtils.fileExists(executionEnvironment, path);
            } catch (IOException | InterruptedException ex) {
                return false; // so it was before - see RemoteCommandSupport
            }
        }

        @Override
        public String getLibDir() {
            String tmpDir;
            try {
                tmpDir = HostInfoUtils.getHostInfo(executionEnvironment).getTempDir();
            } catch (Throwable ex) {
                tmpDir = "/var/tmp"; // NOI18N
            }
            String libDir = tmpDir + "/tools"; // NOI18N
            return libDir;
        }

        private RemoteHostInfo(ExecutionEnvironment executionEnvironment) {
            this.executionEnvironment = executionEnvironment;
        }

        @Override
        public synchronized PathMap getMapper() {
            return RemotePathMap.getPathMap(executionEnvironment);
        }

        @Override
        public synchronized Map<String, String> getEnv() {
            if (envCache == null) {
                envCache = new HashMap<>();
                ProcessUtils.ExitStatus rc = ProcessUtils.execute(executionEnvironment, "env"); // NOI18N
                if (rc.isOK()) {
                    String val = rc.getOutputString();
                    String[] lines = val.split("\n"); // NOI18N
                    for (int i = 0; i < lines.length; i++) {
                        int pos = lines[i].indexOf('=');
                        if (pos > 0) {
                            envCache.put(lines[i].substring(0, pos), lines[i].substring(pos + 1));
                        }
                    }
                }
            }
            return envCache;
        }
    }

    private final static Map<ExecutionEnvironment, RemoteHostInfo> env2hostinfo =
            new HashMap<>();

    public static synchronized RemoteHostInfo getHostInfo(ExecutionEnvironment execEnv) {
        RemoteHostInfo hi = env2hostinfo.get(execEnv);
        if (hi == null) {
            hi = new RemoteHostInfo(execEnv);
            env2hostinfo.put(execEnv, hi);
        }
        return hi;
    }

    public boolean canCreate(ExecutionEnvironment execEnv) {
        return execEnv.isRemote();
    }

    public HostInfoProvider create(ExecutionEnvironment execEnv) {
        return getHostInfo(execEnv);
    }
}
