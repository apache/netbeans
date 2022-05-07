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
package org.netbeans.modules.cnd.lsp.pkgconfig.manager;

import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.lookup.ServiceProvider;

/**
 * LSPPkgConfigManager is a simple PkgConfigManager (currently only local
 * environments are supported).
 * @author antonio
 */
@ServiceProvider(service = PkgConfigManager.class)
public class PkgConfigManagerImpl extends PkgConfigManager {

    @Override
    public PkgConfig getPkgConfig(ExecutionEnvironment env, MakeConfiguration conf) {
        if (! env.isLocal()) {
            // TODO: Implement a remote PkgConfigManager?
            return null;
        }
        return new PkgConfigImpl(env, conf);
    }
    
}
