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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.netbeans.modules.java.lsp.server.URITranslator;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public final class ErrorsNotifier {

    private final Map<LspServerState, Future<Void>> servers = new WeakHashMap<>();
    private final Map<URL, Collection<? extends URL>> lastFilesWithErrors = new HashMap<>();

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
            Collection<? extends URL> last = lastFilesWithErrors.getOrDefault(root, Collections.emptyList());
            Collection<? extends URL> filesWithErrors = ErrorsCache.getAllFilesWithRecord(root);
            if (filesWithErrors.isEmpty()) {
                lastFilesWithErrors.remove(root);
            } else {
                lastFilesWithErrors.put(root, new ArrayList<>(filesWithErrors));
                last.removeAll(filesWithErrors);
            }
            if (!filesWithErrors.isEmpty() || !last.isEmpty()) {
                Project project = FileOwnerQuery.getOwner(root.toURI());
                boolean inOpenedProject = false;
                for (LspServerState server : toProcess) {
                    for (Project p : server.openedProjects().getNow(new Project[0])) {
                        if (p == project) {
                            inOpenedProject = true;
                            for (URL fileWithError : filesWithErrors) {
                                FileObject fo = URLMapper.findFileObject(fileWithError);
                                if (fo != null) {
                                    List<Diagnostic> diags = ErrorsCache.getErrors(fo, (kind, range, message) -> {
                                        Position start = new Position(range.start().line() - 1, range.start().column() - 1);
                                        Position end = range.end() != null ? new Position(range.end().line() - 1, range.end().column() - 1) : start;
                                        Diagnostic d = new Diagnostic(new Range(start, end), message);
                                        d.setSeverity(kind == ErrorsCache.ErrorKind.WARNING ? DiagnosticSeverity.Warning : DiagnosticSeverity.Error);
                                        return d;
                                    });
                                    if (!diags.isEmpty()) {
                                        String lspUri = URITranslator.getDefault().uriToLSP(fileWithError.toURI().toString());
                                        ((TextDocumentServiceImpl) server.getTextDocumentService()).publishDiagnostics(lspUri, diags);
                                    }
                                }
                            }
                            for (URL fileWithError : last) {
                                FileObject fo = URLMapper.findFileObject(fileWithError);
                                if (fo != null) {
                                    String lspUri = URITranslator.getDefault().uriToLSP(fileWithError.toURI().toString());
                                    ((TextDocumentServiceImpl) server.getTextDocumentService()).publishDiagnostics(lspUri, Collections.emptyList());
                                }
                            }
                        }
                    }
                }
                if (!inOpenedProject) {
                    lastFilesWithErrors.remove(root);
                }
            }
        } catch (IOException | URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
