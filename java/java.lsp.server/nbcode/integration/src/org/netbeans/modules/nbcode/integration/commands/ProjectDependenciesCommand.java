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
import java.net.MalformedURLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.Scope;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = CommandProvider.class)
public class ProjectDependenciesCommand implements CommandProvider {
    
    private static final RequestProcessor RP = new RequestProcessor(ProjectDependenciesCommand.class.getName(), 5);
                                                            
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

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        switch (command) {
            case COMMAND_GET_DEPENDENCIES: {
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
                        if (matches != null && !matches.isEmpty()) {
                            for (ArtifactSpec test : matches) {
                                if (test.getGroupId() != null && !test.getGroupId().equals(a.getGroupId())) {
                                    continue NEXT;
                                }
                                if (test.getArtifactId() != null && !test.getArtifactId().equals(a.getArtifactId())) {
                                    continue NEXT;
                                }
                                if (test.getVersionSpec() != null && !test.getVersionSpec().equals(a.getVersionSpec())) {
                                    continue NEXT;
                                }
                                if (test.getClassifier() != null && !test.getClassifier().equals(a.getClassifier())) {
                                    continue NEXT;
                                }
                                if (test.getType()!= null && !test.getType().equals(a.getType())) {
                                    continue NEXT;
                                }
                                // match found, OK
                                break;
                            }
                        }
                        
                        if (request.isReturnContents()) {
                            accepted.add(d);
                        } else {
                            accepted.add(Dependency.create(a, d.getScope(), Collections.emptyList(), null));
                        }
                    }
                    res.setMatches(accepted);
                    future.complete(res);
                });
                return future;
            }

            case COMMAND_CHANGE_DEPENDENCIES:
        }
        return null;
    }
    
}
