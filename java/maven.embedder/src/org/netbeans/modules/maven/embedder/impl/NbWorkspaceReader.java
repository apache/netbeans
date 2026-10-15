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
package org.netbeans.modules.maven.embedder.impl;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.netbeans.modules.maven.embedder.ArtifactFixer;
import org.openide.util.Lookup;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;

/**
 *
 * @author mkleint
 */
public class NbWorkspaceReader implements WorkspaceReader {
    private final WorkspaceRepository repo;
    private final NbVersionResolver2 resolver;
    private final Collection<? extends ArtifactFixer> fixers;
    boolean silence = false;
    
    public NbWorkspaceReader(NbVersionResolver2 resolver) {
        repo = new WorkspaceRepository("ide", getClass());
        fixers = Lookup.getDefault().lookupAll(ArtifactFixer.class);
        this.resolver = resolver;
    }

    @Override
    public WorkspaceRepository getRepository() {
        return repo;
    }

    @Override
    public File findArtifact(org.eclipse.aether.artifact.Artifact artifact) {
        if (silence) {
            return null;
        }
        if (resolver != null) {
            org.eclipse.aether.artifact.Artifact a = resolver.getResolvingArtifact();
            if (a != null &&
                (!Objects.equals(a.getClassifier(), artifact.getClassifier()) ||
                 !Objects.equals(a.getExtension(), artifact.getExtension()) ||
                 !Objects.equals(a.getVersion(), artifact.getVersion()))) {
                // POM is requested, but it is not required by the project itself, but implied
                // as metadata of some really requested artifact. Add the classifier/extension
                // of the true artifact as attributes, so that ArtifactFixer may read it.
                
                // TODO: improve interface to ArtifactFixer, so the info can be provided in a
                // more sane way.
                Map<String, String> m = new HashMap<>(artifact.getProperties());
                m.put("nbResolvingArtifact.group", artifact.getGroupId());
                m.put("nbResolvingArtifact.id", artifact.getArtifactId());
                m.put("nbResolvingArtifact.classifier", a.getClassifier());
                m.put("nbResolvingArtifact.extension", a.getExtension());
                artifact = artifact.setProperties(m);
            }
        }
        for (ArtifactFixer fixer : fixers) {
            File f = fixer.resolve(artifact);
            if (f != null) {
                return f;
            }
        }
        return null;
    }

    @Override
    public List<String> findVersions(org.eclipse.aether.artifact.Artifact artifact) {
        if (silence) {
            return Collections.emptyList();
        }
        //this is important for snapshots, without it the SNAPSHOT will be attempted to be resolved to time-based snapshot version
        if (resolver != null) {
            org.eclipse.aether.artifact.Artifact a = resolver.getResolvingArtifact();
            if (a != null &&
                (!Objects.equals(a.getClassifier(), artifact.getClassifier()) ||
                 !Objects.equals(a.getExtension(), artifact.getExtension()) ||
                 !Objects.equals(a.getVersion(), artifact.getVersion()))) {
                // see note in findArtifact
                Map<String, String> m = new HashMap<>(artifact.getProperties());
                m.put("nbResolvingArtifact.group", artifact.getGroupId());
                m.put("nbResolvingArtifact.id", artifact.getArtifactId());
                m.put("nbResolvingArtifact.classifier", a.getClassifier());
                m.put("nbResolvingArtifact.extension", a.getExtension());
                artifact = artifact.setProperties(m);
            }
        }
        for (ArtifactFixer fixer : fixers) {
            File f = fixer.resolve(artifact);
            if (f != null) {
                return Collections.singletonList(artifact.getBaseVersion());
            }
        }
        return Collections.emptyList();
    }

    /**
     * signals the class to start delegating to workspace
     */
    void normal() {
        silence = false;
    }

    /**
     * signals the class to stop delegating to workspace
     */
    void silence() {
        silence = true;
    }

}
