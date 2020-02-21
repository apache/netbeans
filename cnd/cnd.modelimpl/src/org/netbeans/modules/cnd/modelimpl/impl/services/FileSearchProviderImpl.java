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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport;
import org.netbeans.modules.cnd.apt.support.spi.APTFileSearchImplementation;
import org.netbeans.modules.cnd.apt.support.spi.APTProjectFileSearchProvider;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.utils.FSPath;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.apt.support.spi.APTProjectFileSearchProvider.class, position=1000)
public class FileSearchProviderImpl implements APTProjectFileSearchProvider {

    @Override
    public APTFileSearchImplementation getSearchImplementation(final Key prjKey) {
        for(CsmProject project : ModelImpl.instance().projects()){
            Object platformProject = project.getPlatformProject();
            if (platformProject instanceof NativeProject) {
                NativeProject nativeProject = (NativeProject) platformProject;
                Key aKey = KeyUtilities.createProjectKey(nativeProject);
                if (prjKey.equals(aKey)) {
                    return new APTFileSearchImplementationImpl(nativeProject);
                }
            }
        }
        return null;
    }

    private static final class APTFileSearchImplementationImpl implements APTFileSearchImplementation {
        private final NativeProject project;

        private APTFileSearchImplementationImpl(NativeProject project) {
            this.project = project;
        }

        @Override
        public FSPath searchInclude(String include, CharSequence basePath) {
            NativeFileSearch provider = NativeProjectSupport.getNativeFileSearch(project);
            if (provider != null) {
                Collection<FSPath> searchFile = provider.searchFile(project, include);
                if (searchFile.size() > 0) {
                    include = trimUpFolder(include);
                    List<FSPath> candidates = new ArrayList<>(1);
                    for(FSPath p : searchFile) {
                        String path = p.getPath();
                        path = path.replace('\\', '/');
                        if (path.endsWith(include)) {
                            candidates.add(p);
                        }
                    }
                    if (candidates.size() == 1) {
                        return candidates.get(0);
                    } else if (candidates.size() > 1) {
                        int best = -1;
                        FSPath candidate = null;
                        String from = basePath.toString();
                        from = from.replace('\\', '/');
                        for(FSPath p : candidates) {
                            String path = p.getPath();
                            path = path.replace('\\', '/');
                            int dist = distance(from, path);
                            if (best < dist) {
                                candidate = p;
                                best = dist;
                            }
                        }
                        return candidate;
                    }
                }
            }
            return null;
        }

        private int distance(String from, String to) {
            int res = 0;
            for(int i = 0; i < Math.min(from.length(), to.length()); i++) {
                if (from.charAt(i) != to.charAt(i)){
                    return res;
                }
                res++;
            }
            return res;
        }

        private String trimUpFolder(String include){
            while(include.startsWith("../")) { // NOI18N
                include = include.substring(3);
            }
            return include;
        }

        @Override
        public String toString() {
            return "APTFileSearchImplementationImpl{" + "project=" + project + '}'; // NOI18N
        }
        
        
    }
}
