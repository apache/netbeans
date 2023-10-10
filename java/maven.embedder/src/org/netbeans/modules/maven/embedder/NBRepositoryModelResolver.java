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
package org.netbeans.modules.maven.embedder;


import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.repository.RepositorySystem;
import org.openide.util.Exceptions;

/**
 *
 * @author Anuradha G
 */
class NBRepositoryModelResolver
        implements ModelResolver {

    private final MavenEmbedder embedder;
    private List<ArtifactRepository> remoteRepositories = new ArrayList<ArtifactRepository>();


    NBRepositoryModelResolver(MavenEmbedder embedder) {
        this.embedder = embedder;
    }

    private NBRepositoryModelResolver(NBRepositoryModelResolver original) {
        this(original.embedder);
        this.remoteRepositories = new ArrayList<ArtifactRepository>(original.remoteRepositories);
    }

    @Override
    public ModelResolver newCopy() {
        return new NBRepositoryModelResolver(this);
    }

    @Override
    public void addRepository(Repository repository) throws InvalidRepositoryException {
        addRepository(repository, false);
    }

    @Override
    public ModelSource resolveModel(String groupId, String artifactId, String version)
            throws UnresolvableModelException {
        Artifact artifactParent = embedder.lookupComponent(RepositorySystem.class).createProjectArtifact(groupId, artifactId, version);
        try {
            embedder.resolveArtifact(artifactParent, remoteRepositories, embedder.getLocalRepository());
        } catch (ArtifactResolutionException ex) {
             throw new UnresolvableModelException(ex.getMessage(),  groupId , artifactId , version );
        } catch (ArtifactNotFoundException ex) {
            throw new UnresolvableModelException( ex.getMessage(),  groupId , artifactId , version );
        }

        return new FileModelSource(artifactParent.getFile());
    }

    @Override
    public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
        return resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
    }

    @Override
    public void addRepository(Repository repository, boolean replace) throws InvalidRepositoryException {
        RepositorySystem repositorySystem = embedder.lookupComponent(RepositorySystem.class);
        try {
            ArtifactRepository repo = repositorySystem.buildArtifactRepository(repository);
            if(replace) { 
                remoteRepositories.remove(repo);
            }
            remoteRepositories.add(repo);
            remoteRepositories = repositorySystem.getEffectiveRepositories( remoteRepositories );
        } catch (org.apache.maven.artifact.InvalidRepositoryException ex) {
            throw new InvalidRepositoryException(ex.toString(), repository, ex);
        }
    }

    @Override
    public ModelSource resolveModel(Dependency dpndnc) throws UnresolvableModelException {
        return resolveModel(dpndnc.getGroupId(), dpndnc.getArtifactId(), dpndnc.getVersion());
    }    
}
