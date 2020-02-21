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
package org.netbeans.modules.cnd.search.util;

import java.io.IOException;
import java.util.List;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;

public class SearchScopeValidator {

    private HostInfo.OSFamily localhostOSFamily;

    public SearchScopeValidator() {
        ExecutionEnvironment local = ExecutionEnvironmentFactory.getLocal();
        if (HostInfoUtils.isHostInfoAvailable(local)) {
            try {
                localhostOSFamily = HostInfoUtils.getHostInfo(local).getOSFamily();
            } catch (Exception ign) {
                // localhost -- not reachable
            }
        }
    }

    public boolean isSearchAllowed(final SearchInfo searchInfo) {
        try {
            if (searchInfo == null) {
                return false;
            }

            List<SearchRoot> searchRoots = searchInfo.getSearchRoots();

            for (SearchRoot searchRoot : searchRoots) {
                ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(searchRoot.getFileObject());

                if (env.isLocal()) {
                    if (localhostOSFamily == null || !localhostOSFamily.isUnix()) {
                        return false;
                    }
                } else {
                    if (HostInfoUtils.isHostInfoAvailable(env)
                            && !HostInfoUtils.getHostInfo(env).getOSFamily().isUnix()) {
                        return false;
                    }
                }

            }
        } catch (IOException ex) {
            return false;
        } catch (ConnectionManager.CancellationException ex) {
            return false;
        }
        return true;
    }
}
