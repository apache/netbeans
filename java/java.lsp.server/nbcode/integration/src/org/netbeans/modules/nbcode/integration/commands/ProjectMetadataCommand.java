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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.Server;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.ProjectArtifactsQuery;
import org.netbeans.modules.project.dependency.ProjectArtifactsQuery.ArtifactsResult;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = CodeActionsProvider.class)
public class ProjectMetadataCommand extends CodeActionsProvider {
    private static final String COMMAND_ARTIFACTS = "nbls.gcn.project.artifacts"; // NOI18N

    private static final Set<String> COMMANDS = new HashSet<>(Arrays.asList(
            COMMAND_ARTIFACTS
    ));
    private static final Set<String> ARTIFACT_BLOCK_FIELDS = new HashSet<>(Arrays.asList(
        "data" // NOI18N
    ));
    
    private static final RequestProcessor METADATA_PROCESSOR = new RequestProcessor(ProjectMetadataCommand.class);
    
    private final Gson gson;
    
    public ProjectMetadataCommand() {
        gson = new GsonBuilder()
             // block the opaque 'data' field 
            .addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes fa) {
                        if (fa.getDeclaringClass() == ArtifactSpec.class) {
                          return ARTIFACT_BLOCK_FIELDS.contains(fa.getName());
                        }
                        return false;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> type) {
                        return false;
                    }
            })
            // serialize FileObject as null|path
            .registerTypeAdapterFactory(new TypeAdapterFactory() {
                @Override
                public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> tt) {
                    if (tt.getRawType() != FileObject.class) {
                        return null;
                    }
                    return new TypeAdapter<T>() {
                        @Override
                        public void write(JsonWriter writer, T t) throws IOException {
                            FileObject f = (FileObject)t;
                            writer.value(f == null ? null : f.getPath());
                        }

                        @Override
                        public T read(JsonReader reader) throws IOException {
                            if (reader.peek() == JsonToken.NULL) {
                                reader.nextNull();
                                return null;
                            } else {
                                String s = reader.nextString();
                                if (s == null) {
                                    return null;
                                }
                                FileObject fo = FileUtil.toFileObject(Paths.get(s).toFile());
                                return (T)fo;
                            }
                        }
                    };
                }
            })
            .create();
    }
    
    @Override
    public List<CodeAction> getCodeActions(ResultIterator resultIterator, CodeActionParams params) throws Exception {
        return Collections.emptyList();
    }

    @Override
    public Set<String> getCommands() {
        return COMMANDS;
    }
    
    @Override
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        if (arguments.size() < 1) {
            throw new IllegalArgumentException("Expected at least project URI/path");
        }
        FileObject f = Utils.extractFileObject(arguments.get(0), gson);
        Project p = FileOwnerQuery.getOwner(f);
        if (p == null) {
            throw new IllegalArgumentException("Not a project " + f);
        }
        String artifactType = null;
        ProjectActionContext ctx = null;
        String[] tags = null;
        String classifier = null;
        
        if (arguments.size() > 1) {
            // 2nd parameter is the project action
            Object o = arguments.get(1);
            if (o instanceof JsonObject) {
                JsonObject request = (JsonObject)o;
                if (request.has("action")) {
                    Object a = request.get("action");
                    if (a instanceof JsonPrimitive) {
                        ctx = ProjectActionContext.newBuilder(p).forProjectAction(((JsonPrimitive)a).getAsString()).context();
                    } else {
                        throw new IllegalArgumentException("String expected as action, got " + a);
                    }
                }
                if (request.has("type")) {
                    Object t = request.get("type");
                    if (t instanceof JsonPrimitive) {
                        artifactType = ((JsonPrimitive)t).getAsString();
                    } else {
                        throw new IllegalArgumentException("String expected as type, got " + t);
                    }
                }
                if (request.has("classifier")) {
                    Object c = request.get("classifier");
                    if (c instanceof JsonPrimitive) {
                        classifier = ((JsonPrimitive)c).getAsString();
                    } else {
                        throw new IllegalArgumentException("String expected as classifier, got " + c);
                    }
                }
                if (request.has("tags")) {
                    Object t = request.get("tags");
                    if (t instanceof JsonPrimitive) {
                        tags = new String[] { ((JsonPrimitive)t).getAsString() };
                    } else if (t instanceof JsonArray) {
                        JsonArray arr = (JsonArray)t;
                        tags = new String[arr.size()];
                        int index = 0;
                        for (Object item : arr) {
                            if (item instanceof JsonPrimitive) {
                                tags[index++] = ((JsonPrimitive)item).getAsString();
                            } else {
                                throw new IllegalArgumentException("String expected as tag, got " + item);
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("String or array expected as tags, got " + t);
                    }
                }
                
            } else if (o instanceof JsonPrimitive) {
                ctx = ProjectActionContext.newBuilder(p).forProjectAction(((JsonPrimitive)o).getAsString()).context();
            } else {
                throw new IllegalArgumentException("String, structure, or null expected as parameter #2, got " + o);
            }
            
        }
        if (arguments.size() > 2) {
            // 3rd parameter is the type of artifact
            Object o = arguments.get(2);
            if (!(o instanceof JsonPrimitive)) {
                throw new IllegalArgumentException("String or null expected as parameter #3, got " + o);
            }
            artifactType = ((JsonPrimitive)o).getAsString();
        }
        ProjectArtifactsQuery.Filter filter = ProjectArtifactsQuery.newQuery(artifactType, classifier, ctx, tags);
        return Lookup.getDefault().lookup(LspServerState.class).asyncOpenFileOwner(f).thenApplyAsync((project) -> {
            ArtifactsResult arts = ProjectArtifactsQuery.findArtifacts(p, filter);
            // must serialize in advance, since we cannot configure gson instance in lsp4j
            Object o = gson.toJsonTree(arts.getArtifacts());
            return o;
        }, METADATA_PROCESSOR);
    }
}
