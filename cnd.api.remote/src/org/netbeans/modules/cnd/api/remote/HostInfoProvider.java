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
package org.netbeans.modules.cnd.api.remote;

import java.util.Collection;
import java.util.Map;
//import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.spi.remote.HostInfoProviderFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Lookup;

/**
 * Interface for a remote host information utility provider which can/will be implemented in another module.
 *
 */
public abstract class HostInfoProvider {

    /** Returns path mapper for the given host */
    public static PathMap getMapper(ExecutionEnvironment execEnv) {
        return getDefault(execEnv).getMapper();
    }

    /** Returns path mapper for the given host */
    protected abstract PathMap getMapper();

    /** Returns system environment for the given host */
    public static Map<String, String> getEnv(ExecutionEnvironment execEnv) {
        return getDefault(execEnv).getEnv();
    }

    /** Returns system environment for the given host */
    protected abstract Map<String, String> getEnv();

    /** Validates file existence */
    public static boolean fileExists(ExecutionEnvironment execEnv, String path) {
        return getDefault(execEnv).fileExists(path);
    }

    /** Validates file existence */
    protected abstract boolean fileExists(String path);

    /** Returns dir where libraries are located */
    public static String getLibDir(ExecutionEnvironment execEnv) {
        return getDefault(execEnv).getLibDir();
    }

    /** Returns dir where libraries are located */
    protected abstract String getLibDir();


    private static synchronized HostInfoProvider getDefault(ExecutionEnvironment execEnv) {
        final Collection<? extends HostInfoProviderFactory> factories =
                Lookup.getDefault().lookupAll(HostInfoProviderFactory.class);
        for (HostInfoProviderFactory factory : factories) {
            if (factory.canCreate(execEnv)) {
                final HostInfoProvider provider = factory.create(execEnv);
                assert provider != null;
                return provider;
            }
        }
        throw new IllegalStateException("No host info provider exists for " + execEnv); //NOI18N
    }
}
