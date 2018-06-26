/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
