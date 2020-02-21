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

package org.netbeans.modules.cnd.remote.ui.wizard;

import org.netbeans.modules.cnd.spi.remote.setup.HostSetupProvider;
import org.netbeans.modules.cnd.spi.remote.setup.HostSetupWorker;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=HostSetupProvider.class, position=100)
public class RemoteHostSetupProvider implements HostSetupProvider {

    @Override
    public HostSetupWorker createHostSetupWorker(ToolsCacheManager toolsCacheManager) {
        return new RemoteHostSetupWorker(toolsCacheManager);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "PROVIDER_Name");
    }

    @Override
    public String getID() {
        return "cnd-remote"; //NOI18N
    }

    @Override
    public boolean isApplicable() {
        return true;
    }

    @Override
    public boolean canCheckSetup(ExecutionEnvironment execEnv) {
        return false; //TODO: implement for "ssh://"
    }

    @Override
    public boolean isSetUp(ExecutionEnvironment execEnv) {
        return true; //TODO: implement for "ssh://"
    }

    @Override
    public boolean setUp(ExecutionEnvironment execEnv) {
        return true; //TODO: implement for "ssh://"
    }
}
