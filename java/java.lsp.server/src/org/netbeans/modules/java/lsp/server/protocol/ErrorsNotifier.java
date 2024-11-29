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
package org.netbeans.modules.java.lsp.server.protocol;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.netbeans.api.lsp.Diagnostic;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dusan Balek
 */
public final class ErrorsNotifier {

    private final Map<LspServerState, Future<Void>> servers = new WeakHashMap<>();

    public void connect(LspServerState server, Future<Void> future) {
        synchronized (servers) {
            servers.put(server, future);
        }
    }

    public void notifyErrors(URL root) {
        List<LspServerState> toRemove = new ArrayList<>();
        List<LspServerState> toProcess = new ArrayList<>();
        synchronized (servers) {
            for (Map.Entry<LspServerState, Future<Void>> entry : servers.entrySet()) {
                if (entry.getValue().isDone()) {
                    toRemove.add(entry.getKey());
                } else {
                    toProcess.add(entry.getKey());
                }
            }
            servers.keySet().removeAll(toRemove);
        }
        try {
            Collection<? extends URL> filesWithErrors = ErrorsCache.getAllFilesWithRecord(root);
            if (!filesWithErrors.isEmpty()) {
                Project project = FileOwnerQuery.getOwner(root.toURI());
                for (LspServerState server : toProcess) {
                    for (Project p : server.openedProjects().getNow(new Project[0])) {
                        if (p == project) {
                            Diagnostic.ReporterControl control = Diagnostic.findReporterControl(Lookups.fixed(server), null);
                            control.diagnosticChanged(filesWithErrors.stream().map(url -> URLMapper.findFileObject(url)).filter(fo -> fo != null).collect(Collectors.toList()), null);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
