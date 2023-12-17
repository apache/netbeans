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

import java.util.Collections;
import java.util.List;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.xtext.xbase.lib.Pure;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;

/**
 *
 * @author sdedic
 */
public class DependencyFindResult {
    @NonNull
    private String uri;
    private ArtifactSpec project;
    private List<Dependency> matches = Collections.emptyList();

    @Pure
    public ArtifactSpec getProject() {
        return project;
    }

    public void setProject(ArtifactSpec project) {
        this.project = project;
    }

    @Pure
    @NonNull
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Pure
    @NonNull
    public List<Dependency> getMatches() {
        return matches;
    }

    @Pure
    public void setMatches(List<Dependency> matches) {
        this.matches = matches == null ? Collections.emptyList() : matches;
    }
}
