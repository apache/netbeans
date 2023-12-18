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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.lsp.server.LspGsonSetup;
import org.netbeans.modules.java.lsp.server.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 * A default serialization helper. It handles the following cases:
 * <ul>
 * <li>{@link Throwable}s: excludes some internal fields, and attempts to serialize everything else. Should allow to export error details to vscode.
 * <li>{@link FileObject}: serialized as path, FileObject is looked up on deserialization; both path and URI is supported on deserialization, null if not found + log.
 * <li>{@link Project}: serialized as structure with directory+name, deserialized from the structure (just path/uri is used) or path/uri string
 * </ul>
 * Note: for Exceptions, the serializer does not transmit the class name - which could be useful. In order to do so + serialize the <i>data fields</i> of
 * the exception, some clever delegating would have to be used. The reflective adapter is final / not
 * customizable, so some weird JsonWriter man-in-the-middle could eventually inject a classname property : not implemented yet
 * @author sdedic
 */
@ServiceProvider(service = LspGsonSetup.class)
public class NbGsonAdapter implements LspGsonSetup {
    private static final Logger LOG = Logger.getLogger(NbGsonAdapter.class.getName());
    
    /**
     * Fields from Throwable that will not be serialized.
     */
    private static final Set<String> EXCEPTION_BLOCK_FIELDS = new HashSet<>(Arrays.asList(
        "backtrace", // NOI18N
        "depth", // NOI18N
        "stackTrace", // NOI18N
        "suppressedExceptions" // NOI18N
    ));
    
    private static FileObject deserializeFile(String s) {
        if (s == null || "".equals(s.trim())) {
            return null;
        }
        FileObject fo = FileUtil.toFileObject(Paths.get(s).toFile());
        if (fo == null) {
            try {
                fo = Utils.fromUri(s);
            } catch (IllegalArgumentException | FileSystemNotFoundException | MalformedURLException ex) {
                // will be logged later, along with URI ok but file-not-found case
            }
        }
        if (fo == null) {
            LOG.log(Level.FINE, "Could not deserialize path {0} to a file");
        }
        return fo;
    }
    
    @Override
    public void configureBuilder(GsonBuilder b) {
        
        // block the opaque 'data' field
        b.addSerializationExclusionStrategy(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fa) {
                if (Throwable.class.isAssignableFrom(fa.getDeclaredClass())) {
                    return EXCEPTION_BLOCK_FIELDS.contains(fa.getName());
                }
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> type) {
                return false;
            }
        }).registerTypeAdapterFactory(new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> tt) {
                if (FileObject.class.isAssignableFrom(tt.getRawType())) {
                    return new TypeAdapter<T>() {
                        @Override
                        public void write(JsonWriter writer, T t) throws IOException {
                            FileObject f = (FileObject) t;
                            writer.value(f == null ? null : f.getPath());
                        }

                        @Override
                        public T read(JsonReader reader) throws IOException {
                            if (reader.peek() == JsonToken.NULL) {
                                reader.nextNull();
                                return null;
                            } else {
                                return (T)deserializeFile(reader.nextString());
                            }
                        }
                    };
                } else if (Project.class.isAssignableFrom(tt.getRawType())) {
                    // custom serialize Project as structure
                    return new TypeAdapter<T>() {
                        @Override
                        public void write(JsonWriter writer, T t) throws IOException {
                            Project p = (Project) t;
                            if (p == null) {
                                writer.nullValue();
                            } else {
                                writer.beginObject();
                                writer.name("path").value(p.getProjectDirectory().getPath()); // NOI18N
                                writer.name("name").value(ProjectUtils.getInformation(p).getDisplayName()); // NOI18N
                                writer.endObject();
                            }
                        }

                        @Override
                        public T read(JsonReader reader) throws IOException {
                            if (reader.peek() == JsonToken.NULL) {
                                reader.nextNull();
                                return null;
                            } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                                reader.beginObject();
                                Project p = null;
                                while (reader.hasNext()) {
                                    String n = reader.nextName();
                                    if (n == null) {
                                        continue;
                                    }
                                    String s = reader.nextString();
                                    if ("path".equals(n) || "uri".equals(n)) { // NOI18N
                                        // NOI18N
                                        FileObject fo = deserializeFile(s);
                                        if (fo != null) {
                                            p = ProjectManager.getDefault().findProject(fo);
                                        }
                                    }
                                }
                                reader.endObject();
                                return (T) p;
                            } else if (reader.peek() == JsonToken.STRING) {
                                String s = reader.nextString();
                                if (s == null || s.isEmpty()) {
                                    return null;
                                }
                                FileObject fo = deserializeFile(s);
                                return (T)(fo == null ? null : ProjectManager.getDefault().findProject(fo));
                            } else {
                                return null;
                            }
                        }
                    };
                } else {
                    return null;
                }
            }
        });
        b.registerTypeAdapter(EnumSet.class, new EnumSetDeserializer());
    }


    /**
     * LSP4j obscures EnumSet handling by its Collection type adapter factory. We need to register a type adapter
     * that overrides the deserialization for enumsets. This adapter also accepts a primitive as a (singleton) EnumSet.
     */
    private static final class EnumSetDeserializer implements JsonDeserializer<EnumSet> {
        @Override
        public EnumSet deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            ParameterizedType pt = (ParameterizedType)type;
            Type itemType = pt.getActualTypeArguments()[0];
            if (je.isJsonPrimitive()) {
                Enum e = (Enum)jdc.deserialize(je, itemType);
                if (e != null) {
                    return EnumSet.of(e);
                }
            } else if (!je.isJsonArray()) {
                throw new JsonParseException("Primitive or array expected");
            }
            JsonArray arr = je.getAsJsonArray();
            EnumSet raw = EnumSet.noneOf((Class)itemType);
            for (JsonElement el : arr) {
                if (!el.isJsonPrimitive()) {
                    throw new JsonParseException("Primitive item expected");
                }
                raw.add((Enum)jdc.deserialize(el, itemType));
            }
            return raw;
        }
    }

}
