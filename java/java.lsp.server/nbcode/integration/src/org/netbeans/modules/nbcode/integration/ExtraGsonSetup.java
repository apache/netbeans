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
package org.netbeans.modules.nbcode.integration;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.java.lsp.server.LspGsonSetup;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyChange;
import org.netbeans.modules.project.dependency.DependencyChangeRequest;
import org.netbeans.modules.project.dependency.Scope;
import org.netbeans.modules.project.dependency.Scopes;
import org.openide.util.lookup.ServiceProvider;

/**
 * Adds some more type adapters for ArtifactSpec.
 *
 * @author sdedic
 */
@ServiceProvider(service = LspGsonSetup.class)
public class ExtraGsonSetup implements LspGsonSetup {

    private static final Set<String> ARTIFACT_BLOCK_FIELDS = new HashSet<>(Arrays.asList(
            "data" // NOI18N
    ));

    private static final Set<String> DEPENDENCY_BLOCK_FIELDS = new HashSet<>(Arrays.asList(
            "parent", // NOI18N
            "data" // NOI18N
    ));

    @Override
    public void configureBuilder(GsonBuilder b) {
        b.addSerializationExclusionStrategy(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fa) {
                // this is hacky, but avoids dependency on project artifacts. An alternative is to place
                //this part into VSNetbeans integration module that contains the relevant commands.
                if (fa.getDeclaringClass() == ArtifactSpec.class) {
                    return ARTIFACT_BLOCK_FIELDS.contains(fa.getName());
                } else if (Throwable.class.isAssignableFrom(fa.getDeclaredClass())) {
                    return DEPENDENCY_BLOCK_FIELDS.contains(fa.getName());
                } else if (fa.getDeclaringClass() == Dependency.class) {

                }
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> type) {
                return false;
            }
        });
        b.registerTypeAdapter(ArtifactSpec.class, new ArtifactDeserializer());
        b.registerTypeAdapter(Scope.class, new ScopeSerializer());
        b.registerTypeAdapter(Dependency.class, new DependencySerializer());
        b.registerTypeAdapter(DependencyChangeRequest.class, (InstanceCreator)(t) -> new DependencyChangeRequest(Collections.emptyList()));
        b.registerTypeAdapter(DependencyChange.class, (InstanceCreator)(t) -> DependencyChange.builder(DependencyChange.Kind.ADD).create());
        b.registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory());
    }
    
    class ArtifactDeserializer implements JsonDeserializer<ArtifactSpec> {

        @Override
        public ArtifactSpec deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            if (je.isJsonNull()) {
                return null;
            } else if (je.isJsonPrimitive()) {
                return deserializeArtifactFromString(je.getAsString());
            } else if (!je.isJsonObject()) {
                throw new JsonParseException("Expected artifact string or structure");
            }
            JsonObject obj = je.getAsJsonObject();
            String g = obj.has("groupId") ? obj.getAsJsonPrimitive("groupId").getAsString() : null;
            String a = obj.has("artifactId") ? obj.getAsJsonPrimitive("artifactId").getAsString() : null;
            String v = obj.has("versionSpec") ? obj.getAsJsonPrimitive("versionSpec").getAsString() : null;
            String c = obj.has("classifier") ? obj.getAsJsonPrimitive("classifier").getAsString() : null;
            String t = obj.has("type") ? obj.getAsJsonPrimitive("type").getAsString() : null;

            ArtifactSpec.Builder b = ArtifactSpec.builder(g, a, v, null).classifier(c).type(t);
            if (v != null && v.contains("-SNAPSHOT")) {
                b.versionKind(ArtifactSpec.VersionKind.SNAPSHOT);
            }
            return b.build();
        }
    }
    
    class DependencyChangeDeserializer implements JsonDeserializer<DependencyChange> {
        private final Type OPTION_SET_TYPE = new TypeToken<EnumSet<DependencyChange.Options>>() {}.getType(); 
        @Override
        public DependencyChange deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            if (je.isJsonNull()) {
                return null;
            }
            if (!je.isJsonObject()) {
                throw new JsonParseException("Expected DependencyChange structure");
            }
            JsonObject o = je.getAsJsonObject();
            
            DependencyChange.Kind kind = jdc.deserialize(o.getAsJsonPrimitive("kind"), DependencyChange.Kind.class);
            EnumSet<DependencyChange.Options> opts = jdc.deserialize(o.getAsJsonPrimitive("kind"), OPTION_SET_TYPE);
            return null;
        }
    }
    
    private static ArtifactSpec deserializeArtifactFromString(String s) {
        int scopeIndex = s.lastIndexOf('[');
        if (scopeIndex > -1) {
            s = s.substring(0, scopeIndex);
        }
        String[] parts = s.split(":");
        boolean snap = parts.length > 2 && parts[2].endsWith("-SNAPSHOT");
        ArtifactSpec spec;
        if (snap) {
            return ArtifactSpec.createSnapshotSpec(parts[0], parts[1], null, parts.length > 3 ? parts[3] : null, parts.length > 2 ? parts[2] : null, false, null, null);
        } else {
            return ArtifactSpec.createVersionSpec(parts[0], parts[1], null, parts.length > 3 ? parts[3] : null, parts.length > 2 ? parts[2] : null, false, null, null);
        }
    }

    static class DependencySerializer implements JsonDeserializer<Dependency> {
        private static final Type DEPENDENCY_LIST_TYPE = new TypeToken<List<Dependency>>() {}.getType();
        @Override
        public Dependency deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            Scope scope = Scopes.COMPILE;
            ArtifactSpec a;
            
            if (je.isJsonNull()) {
                return null;
            } else if (je.isJsonPrimitive()) {
                // attempt to interpret the dependency as a string
                String s = je.getAsString();
                int scopeIndex = s.lastIndexOf('[');
                if (scopeIndex > -1) {
                    int end = s.indexOf(']', scopeIndex);
                    if (end == -1) {
                        end = s.length();
                    }
                    scope = Scope.named(s.substring(scopeIndex, end));
                    s = s.substring(0, scopeIndex);
                }
                a = deserializeArtifactFromString(s);
                return Dependency.make(a, scope);
            } else if (!je.isJsonObject()) {
                throw new JsonParseException("Expected dependency string or structure");
            }
            JsonObject o = je.getAsJsonObject();
            a = jdc.deserialize(o.get("artifact"), ArtifactSpec.class);
            List<Dependency> children = new ArrayList<>();
            if (o.has("scope")) {
                scope = jdc.deserialize(o.get("scope"), Scope.class);
            }
            if (o.has("children")) {
                children = jdc.deserialize(o.getAsJsonArray("children"), DEPENDENCY_LIST_TYPE);
            }
            return Dependency.create(a, scope, children, null);
        }
    }

    public class LowercaseEnumTypeAdapterFactory implements TypeAdapterFactory {

        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            Class<T> rawType = (Class<T>) type.getRawType();
            if (!rawType.isEnum() || !type.getType().getTypeName().startsWith("org.netbeans.modules.project.dependency")) {
                return null;
            }

            final Map<String, T> lowercaseToConstant = new HashMap<String, T>();
            for (T constant : rawType.getEnumConstants()) {
                lowercaseToConstant.put(toLowercase(constant), constant);
            }

            return new TypeAdapter<T>() {
                public void write(JsonWriter out, T value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(value.toString());
                    }
                }

                public T read(JsonReader reader) throws IOException {
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull();
                        return null;
                    } else {
                        return lowercaseToConstant.get(toLowercase(reader.nextString()));
                    }
                }
            };
        }

        private String toLowercase(Object o) {
            return o.toString().toLowerCase(Locale.US);
        }
    }

    public class ScopeSerializer implements JsonDeserializer<Scope>, JsonSerializer<Scope> {

        @Override
        public Scope deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            return Scope.named(je.getAsString());
        }

        @Override
        public JsonElement serialize(Scope t, Type type, JsonSerializationContext jsc) {
            return jsc.serialize(t.name());
        }
    }

}
