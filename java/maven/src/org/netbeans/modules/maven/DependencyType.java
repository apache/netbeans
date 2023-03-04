/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.maven;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.maven.artifact.Artifact;

/**
 *
 * @author Tomas Stupka
 */
public enum DependencyType {
    COMPILE(Artifact.SCOPE_COMPILE, Artifact.SCOPE_PROVIDED, Artifact.SCOPE_SYSTEM), 
    TEST(Artifact.SCOPE_TEST),
    RUNTIME(Artifact.SCOPE_RUNTIME), 
    /** any scope */NONCP;

    private final List<String> scopes;

    private DependencyType(String... artifactScopes) {
        this.scopes = Collections.unmodifiableList(artifactScopes == null || artifactScopes.length == 0 ? Collections.emptyList() : Arrays.asList(artifactScopes));
    }        

    public List<String> artifactScopes() {
        return scopes;
    }
    
    static DependencyType forArtifact(Artifact a) {
        String scope = a.getScope();
        for (DependencyType t : DependencyType.values()) {
            for (String s : t.artifactScopes()) {
                if (s.equals(scope)) {
                    return t;
                }
            }
        }
        return NONCP;
    }
}
