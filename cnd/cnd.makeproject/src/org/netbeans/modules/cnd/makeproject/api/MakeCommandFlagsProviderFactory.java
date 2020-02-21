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
package org.netbeans.modules.cnd.makeproject.api;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.openide.util.Lookup;

/**
 *
 */
public interface MakeCommandFlagsProviderFactory {
    boolean canHandle(String commandID, Lookup context, MakeConfiguration conf);
    MakeCommandFlagsProvider createProvider();
    
    //@ServiceProvider(service=MakeCommandFlagsProviderFactory.class, position=1000)
    public static final MakeCommandFlagsProviderFactory DEFAULT = new MakeCommandFlagsProviderFactory() {

        @Override
        public boolean canHandle(String commandID, Lookup context, MakeConfiguration conf) {
            return true;
        }

        @Override
        public MakeCommandFlagsProvider createProvider() {
            return (String commandID, Lookup context, Project project, MakeConfiguration conf, String flag, boolean defaultFlagValue) -> {
                if (MakeCommandFlagsProvider.PRE_BUILD_FIRST.equals(flag)) {//NOI18N
                    return conf.getPreBuildConfiguration().getPreBuildFirst().getValue();
                } else if (MakeCommandFlagsProvider.BUILD_FIRST.equals(flag)) {//NOI18N
                    RunProfile profile = conf.getProfile();
                    if (profile != null) {
                        return profile.getBuildFirst();
                    }
                }
                return defaultFlagValue;
            };
        }
    };
}
