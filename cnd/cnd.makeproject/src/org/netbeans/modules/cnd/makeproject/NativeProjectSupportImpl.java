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
package org.netbeans.modules.cnd.makeproject;

import java.io.IOException;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport.NativeExitStatus;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.spi.project.NativeFileSearchProvider;
import org.netbeans.modules.cnd.spi.project.NativeProjectExecutionProvider;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 */
@ServiceProviders({
@ServiceProvider(service=NativeProjectExecutionProvider.class, path=NativeProjectExecutionProvider.PATH, position=100),
@ServiceProvider(service=NativeFileSearchProvider.class, path=NativeFileSearchProvider.PATH, position=100)
})
public class NativeProjectSupportImpl implements NativeProjectExecutionProvider, NativeFileSearchProvider {

    @Override
    public NativeExitStatus execute(NativeProject project, String executable, String[] env, String... args) throws IOException {
        if (project instanceof NativeProjectProvider) {
            return ((NativeProjectProvider)project).execute(executable, env, args);
        }
        return null;
    }

    @Override
    public String getPlatformName(NativeProject project) {
        if (project instanceof NativeProjectProvider) {
            return ((NativeProjectProvider) project).getPlatformName();
        }
        return null;
    }

    @Override
    public NativeFileSearch getNativeFileSearch(NativeProject project) {
        if (project instanceof NativeProjectProvider) {
            return ((NativeProjectProvider) project).getNativeFileSearch();
        }
        return null;
    }

}
