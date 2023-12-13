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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.java.lsp.server.LspGsonSetup;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.Scope;
import org.openide.util.lookup.ServiceProvider;

/**
 * Adds some more type adapters for ArtifactSpec.
 * 
 * @author sdedic
 */
@ServiceProvider(service = LspGsonSetup.class)
public class ExtraGsonSetup implements LspGsonSetup{

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
    }
    
    class ScopeSerializer implements JsonSerializer<Scope> {

        @Override
        public JsonElement serialize(Scope t, Type type, JsonSerializationContext jsc) {
            return jsc.serialize(t.name());
        }
    }
    
    
    class ArtifactDeserializer implements JsonDeserializer<ArtifactSpec> {

        @Override
        public ArtifactSpec deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
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
}
