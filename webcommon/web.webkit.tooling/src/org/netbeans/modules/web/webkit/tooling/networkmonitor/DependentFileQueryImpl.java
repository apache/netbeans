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
package org.netbeans.modules.web.webkit.tooling.networkmonitor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.common.spi.DependentFileQueryImplementation;
import org.netbeans.modules.web.webkit.debugging.api.network.Network;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=DependentFileQueryImplementation.class)
public class DependentFileQueryImpl implements DependentFileQueryImplementation {
    private static final RequestProcessor RP = new RequestProcessor(DependentFileQueryImpl.class);
    private static final DependencyInfo DEFAULT = new DependencyInfo();

    @Override
    public Dependency isDependent(FileObject master, FileObject dependent) {
        return DEFAULT.isDependent(master, dependent);
    }

    static void networkRequest(Project p, Network.Request request) {
        DEFAULT.networkRequestHandler(p, request);
    }

    /**
     * Class which listens on runtime Network requests and provides dependency information
     * based on what resources were loaded as part of a page loading in the browser.
     */
    private static class DependencyInfo {
        private final WeakHashMap<Project, Map<FileObject, List<FileObject>>> dependecies = new WeakHashMap<>();

        public Dependency isDependent(FileObject master, FileObject dependent) {
            Project p = FileOwnerQuery.getOwner(master);
            if (p == null) {
                return Dependency.UNKNOWN;
            }
            Map<FileObject, List<FileObject>> projDep = dependecies.get(p);
            if (projDep == null) {
                return Dependency.UNKNOWN;
            }
            List<FileObject> deps = projDep.get(master);
            if (deps == null) {
                return Dependency.UNKNOWN;
            }
            return deps.contains(dependent) ? Dependency.YES : Dependency.UNKNOWN;
        }

        public void networkRequest(final Project p, final Network.Request request) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    networkRequestHandler(p, request);
                }
            });
        }

        private void networkRequestHandler(Project p, Network.Request request) {
            Map<FileObject, List<FileObject>> map = dependecies.get(p);
            if (map == null) {
                map = new HashMap<>();
                dependecies.put(p, map);
            }

            String documentUrl = request.getDocumentUrl();
            String url = (String)request.getRequest().get("url"); // NOI18N
            if (documentUrl == null || url == null) {
                return;
            }
            FileObject documentUrlFO = findProjectFile(p, documentUrl);
            if (documentUrlFO == null) {
                return;
            }
            if (documentUrl.equals(url)) {
                // documentUrl represent the primary page being loaded;
                // use this to erase existing cache information
                map.put(documentUrlFO, new ArrayList<FileObject>());
            } else {
                // dep is dependency which was loaded in order to complete
                // loading of the page stored in documentUrl
                FileObject dep = findProjectFile(p, url);
                if (dep == null) {
                    return;
                }
                List<FileObject> deps = map.get(documentUrlFO);
                if (deps == null) {
                    deps = new ArrayList<>();
                    map.put(documentUrlFO, deps);
                }
                deps.add(dep);
            }
        }

        private FileObject findProjectFile(Project project, String urlStr) {
            try {
                URL url = new URL(urlStr);
                if (project != null) {
                    return ServerURLMapping.fromServer(project, url);
                }
            } catch (MalformedURLException murl) {
                // ignore
            }
            return null;
        }

    }

}
