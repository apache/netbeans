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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.java.lsp.server.LspGsonSetup;
import org.netbeans.modules.project.dependency.ArtifactSpec;
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

                }
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> type) {
                return false;
            }
        });
    }
    
}
