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
package org.netbeans.modules.nbcode.integration.commands;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.netbeans.modules.java.lsp.server.LspServerUtils;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.SaveDocumentRequestParams;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyChangeException;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectModificationResult;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.Scope;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = CommandProvider.class)
public class ProjectDependenciesCommand implements CommandProvider {
    
    private static final RequestProcessor RP = new RequestProcessor(ProjectDependenciesCommand.class.getName(), 5);
                                      
    /**
     * Finds dependencies in a project. The command expects {@link DependencyFindRequest} as a sole input, and produces
     * {@link DependencyFindResult} as the output. Throws an exception if the operation fails.
     */
    private static final String COMMAND_GET_DEPENDENCIES = "nbls.project.dependencies.find";
    private static final String COMMAND_CHANGE_DEPENDENCIES = "nbls.project.dependencies.change";
    
    private static final Set COMMANDS = new HashSet<>(Arrays.asList(
            COMMAND_GET_DEPENDENCIES,
            COMMAND_CHANGE_DEPENDENCIES
    ));

    @Override
    public Set<String> getCommands() {
        return COMMANDS;
    }

    private final Gson gson = new Gson();
    
    static class K {
        final Dependency d;

        public K(Dependency d) {
            this.d = d;
        }
        
        public int hashCode() {
            return d.getArtifact().hashCode() << 7 + d.getScope().name().hashCode();
        }
        
        public boolean equals(Object o) {
            if (!(o instanceof K)) {
                return false;
            }
            K other = (K)o;
            return other.d.getArtifact().equals(d.getArtifact()) && other.d.getScope().equals(d.getScope());
        }
    }
    
    private Gson gson() {
        Gson inst = Lookup.getDefault().lookup(Gson.class);
        return inst != null ? inst : gson;
    }

    @NbBundle.Messages({
        "# {0} - file uri",
        "ERR_FileNotInProject=File {0} is not in any project.",
        "# {0} - file uri",
        "ERR_InvalidFileUri=Malformed URI: {0}"
    })

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        switch (command) {
            case COMMAND_GET_DEPENDENCIES: {
                // Finds dependencies in a project.
                DependencyFindRequest request = gson().fromJson(gson().toJson(arguments.get(0)), DependencyFindRequest.class);
                FileObject dir;
                try {
                    dir = Utils.fromUri(request.getUri());
                } catch (MalformedURLException ex) {
                    CompletableFuture res = new CompletableFuture();
                    res.completeExceptionally(ex);
                    return res;
                }
                if (dir == null) {
                    throw new IllegalArgumentException("Not a file");
                }
                
                Project p = FileOwnerQuery.getOwner(dir);
                if (p == null) {
                    return CompletableFuture.completedFuture(null);
                }
                List<ArtifactSpec> matches = request.getArtifacts();
                // PENDING: make 'online' a parameter
                Scope[] scopes = request.getScopes() == null ? 
                        null : request.getScopes().stream().map(sn -> Scope.named(sn)).toArray(s -> new Scope[s]);
                ProjectDependencies.DependencyQueryBuilder b = ProjectDependencies.newBuilder().
                        online().
                        scope(scopes);
                CompletableFuture future = new CompletableFuture();
                // do not block the main thread
                RP.post(() -> {
                    DependencyResult r;
                    
                    try {
                        r = ProjectDependencies.findDependencies(p, b.build());
                    } catch (ProjectOperationException ex) {
                        future.completeExceptionally(ex);
                        return;
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                        return;
                    }
                    DependencyFindResult res = new DependencyFindResult();
                    res.setUri(URLMapper.findURL(p.getProjectDirectory(), URLMapper.EXTERNAL).toString());
                    res.setProject(r.getRoot().getArtifact());
                    
                    Queue<Dependency> toProcess = new ArrayDeque<>();
                    List<Dependency> accepted = new ArrayList<>();
                    toProcess.addAll(r.getRoot().getChildren());
                    
                    Set<K> seen = new HashSet<>();
                    NEXT: while (!toProcess.isEmpty()) {
                        Dependency d = toProcess.poll();
                        ArtifactSpec a = d.getArtifact();
                        if (a == null) {
                            // PENDING: not supported atm
                            continue;
                        }
                        if (!seen.add(new K(d))) {
                            continue;
                        }
                        toProcess.addAll(d.getChildren());
                        boolean found = false;
                        if (matches != null && !matches.isEmpty()) {
                            for (ArtifactSpec test : matches) {
                                if (test.getGroupId() != null && !test.getGroupId().equals(a.getGroupId())) {
                                    continue;
                                }
                                if (test.getArtifactId() != null && !test.getArtifactId().equals(a.getArtifactId())) {
                                    continue;
                                }
                                if (test.getVersionSpec() != null && !test.getVersionSpec().equals(a.getVersionSpec())) {
                                    continue;
                                }
                                if (test.getClassifier() != null && !test.getClassifier().equals(a.getClassifier())) {
                                    continue;
                                }
                                if (test.getType()!= null && !test.getType().equals(a.getType())) {
                                    continue;
                                }
                                // match found, OK
                                found = true;
                                break;
                            }
                        } else {
                            found = true;
                        }
                        if (found) {
                            if (request.isReturnContents()) {
                                accepted.add(d);
                            } else {
                                accepted.add(Dependency.create(a, d.getScope(), Collections.emptyList(), null));
                            }
                        }
                    }
                    res.setMatches(accepted);
                    future.complete(res);
                });
                return future;
            }

            case COMMAND_CHANGE_DEPENDENCIES: {
                // Finds dependencies in a project.
                LspDependencyChangeRequest request = gson().fromJson(gson().toJson(arguments.get(0)), LspDependencyChangeRequest.class);
                FileObject dir;
                Project p;
                
                try {
                    dir = Utils.fromUri(request.getUri());
                    p = FileOwnerQuery.getOwner(dir);
                    if (p == null) {
                        throw new IllegalArgumentException(Bundle.ERR_FileNotInProject(request.getUri()));
                    }
                } catch (MalformedURLException ex) {
                    throw new IllegalArgumentException(Bundle.ERR_InvalidFileUri(request.getUri()));
                }
                
                CompletableFuture future = new CompletableFuture();
                RP.post(() -> {
                    LspDependencyChangeResult res = new LspDependencyChangeResult();
                    ProjectModificationResult mod;
                    try {
                        mod = ProjectDependencies.modifyDependencies(p, request.getChanges());
                    } catch (DependencyChangeException ex) {
                        future.completeExceptionally(ex);
                        return;
                    }
                    if (mod == null) {
                        future.complete(null);
                        return;
                    }
                    NbCodeLanguageClient client = LspServerUtils.requireLspClient(Lookup.getDefault());
                    WorkspaceEdit wEdit = mod.getWorkspaceEdit();
                    org.eclipse.lsp4j.WorkspaceEdit lspEdit = Utils.workspaceEditFromApi(wEdit, null, client);
                    res.setEdit(lspEdit);
                    if (request.isApplyChanges()) {
                        if (request.isSaveFromServer()) {
                            try {
                                mod.commit();
                                future.complete(res);
                            } catch (IOException ex) {
                                future.completeExceptionally(ex);
                            }
                        } else {
                            client.applyEdit(new ApplyWorkspaceEditParams(lspEdit)).thenApply((x) -> {
                                String[] uris = new String[mod.getFilesToSave().size()];
                                int index = 0;
                                
                                for (FileObject f : mod.getFilesToSave()) {
                                    URL u = URLMapper.findURL(f, URLMapper.EXTERNAL);
                                    if (u != null) {
                                        String s = u.toString();
                                        if (s.indexOf(f.getPath()) == 5) {
                                            s = "file://" + s.substring(5);
                                        }
                                        uris[index++] = s;
                                    }
                                }
                                return client.requestDocumentSave(new SaveDocumentRequestParams(Arrays.asList(uris)));
                            }).exceptionally(t -> {
                                future.completeExceptionally(t);
                                return CompletableFuture.completedFuture(false);
                            }).thenAccept((b) -> {
                                // only complete the action after a save attempt. Will have no effect if already completed exceptionally.
                                future.complete(res);
                            });
                        }
                    } else {
                        future.complete(res);
                    }
                    // must broadcast instructions to the client
                });
                return future;
            }
        }
        return null;
    }
    
}
