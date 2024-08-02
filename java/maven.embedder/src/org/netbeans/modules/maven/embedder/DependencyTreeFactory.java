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

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.CumulativeScopeArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;

/**
 *
 * @author mkleint
 */
public class DependencyTreeFactory {
    private static final Logger LOG = Logger.getLogger(DependencyTreeFactory.class.getName());
    
    @Deprecated
    public static DependencyNode createDependencyTree(MavenProject project, MavenEmbedder embedder, String scope) {
        try {
            return createDependencyTree(project, embedder, List.of(scope));
        } catch (MavenExecutionException ex) {
            LOG.log(Level.INFO, "Dependency tree scan failed", ex);
            return null;
        }
    }
    
    /**
     * Constructs a Dependency tree. Throws MavenExecutionException on any problems.
     * @param project the project
     * @param embedder embedder instance / session to execute the query with
     * @param scopes artifact scopes to include
     * @return root of the constructed tree
     * @throws MavenExecutionException wraps any maven-specific exception thrown by the implementation.
     * @since 2.71
     */
    public static DependencyNode createDependencyTree(MavenProject project, MavenEmbedder embedder, Collection<String> scopes) throws MavenExecutionException {
        //TODO: check alternative for deprecated maven components 
        DependencyTreeBuilder builder = embedder.lookupComponent(DependencyTreeBuilder.class);
        assert builder !=null : "DependencyTreeBuilder component not found in maven";

        ArtifactFactory factory = embedder.lookupComponent(ArtifactFactory.class);
        assert factory !=null : "ArtifactFactory component not found in maven";

        ArtifactMetadataSource source = embedder.lookupComponent(ArtifactMetadataSource.class);
        assert source !=null : "ArtifactMetadataSource component not found in maven";

        ArtifactCollector collector = embedder.lookupComponent(ArtifactCollector.class);
        assert collector !=null : "ArtifactCollector component not found in maven";

        embedder.setUpLegacySupport();
        
        return createDependencyTree(project, builder, embedder.getLocalRepository(), factory, source, collector, scopes);

    }
    
    //copied from dependency:tree mojo
    private static DependencyNode createDependencyTree(MavenProject project,
            DependencyTreeBuilder dependencyTreeBuilder, ArtifactRepository localRepository,
            ArtifactFactory artifactFactory, ArtifactMetadataSource artifactMetadataSource,
            ArtifactCollector artifactCollector,
            Collection<String> scopes) throws MavenExecutionException {
        ArtifactFilter artifactFilter = createResolvingArtifactFilter(scopes);
        
        try {
            // TODO: note that filter does not get applied due to MNG-3236
            return dependencyTreeBuilder.buildDependencyTree(project,
                    localRepository, artifactFactory,
                    artifactMetadataSource, artifactFilter, artifactCollector);
        } catch (DependencyTreeBuilderException exception) {
            throw new MavenExecutionException("Dependency tree scan failed", exception);
        }
    }

    //copied from dependency:tree mojo
    /**
     * Gets the artifact filter to use when resolving the dependency tree.
     *
     * @return the artifact filter
     */
    private static ArtifactFilter createResolvingArtifactFilter(Collection<String> scopes) {
        ArtifactFilter filter;

        // filter scope
        if (scopes != null) {
            if (scopes.size() == 1) {
                filter = new ScopeArtifactFilter(scopes.iterator().next());
            } else {
                filter = new CumulativeScopeArtifactFilter(scopes);
            }
        } else {
            filter = null;
        }

        return filter;
    }
}
