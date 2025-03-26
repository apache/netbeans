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

import java.util.List;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.xtext.xbase.lib.Pure;
import org.netbeans.modules.project.dependency.ArtifactSpec;

/**
 *
 * @author sdedic
 */
public class DependencyFindRequest {
    /**
     * URI of the project.
     */
    @NonNull
    private String uri;
    
    /**
     * Scope(s) to search.
     */
    private List<String> scopes;
    
    /**
     * Artifacts to search for.
     */
    private List<ArtifactSpec>  artifacts;

    /**
     * Return contents of the dependency
     */
    private boolean returnContents;

    @Pure
    @NonNull
    public String getUri() {
        return uri;
    }

    @Pure
    public List<String> getScopes() {
        return scopes;
    }

    @Pure
    public List<ArtifactSpec> getArtifacts() {
        return artifacts;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public void setArtifacts(List<ArtifactSpec> artifacts) {
        this.artifacts = artifacts;
    }

    public boolean isReturnContents() {
        return returnContents;
    }

    public void setReturnContents(boolean returnContents) {
        this.returnContents = returnContents;
    }
}
