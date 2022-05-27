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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyGraphBuilder;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class DependencyTreeFactory {
    private static final Logger LOG = Logger.getLogger(DependencyTreeFactory.class.getName());
    
    public static DependencyNode createDependencyTree(MavenProject project, MavenEmbedder embedder, String scope) {
        
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
        
        return createDependencyTree(project, builder, embedder.getLocalRepository(), factory, source, collector, scope);

    }
    
    public static org.apache.maven.shared.dependency.graph.DependencyNode createDependencyGraph(MavenProject project, MavenEmbedder embedder, String scope) {
        ArtifactFilter artifactFilter = createResolvingArtifactFilter(scope);
        MavenExecutionRequest req = embedder.createMavenExecutionRequest();
        req.setPom(project.getFile());
        req.setOffline(true);
        
        ProjectBuildingRequest configuration = req.getProjectBuildingRequest();
        configuration.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        configuration.setResolveDependencies(true);
        
        DependencyGraphBuilder depBuilder = embedder.lookupComponent(DependencyGraphBuilder.class);
        org.apache.maven.shared.dependency.graph.DependencyNode graphNode;
        try {
            DefaultMaven maven = (DefaultMaven)embedder.getPlexus().lookup(Maven.class);
            configuration.setRepositorySession(maven.newRepositorySession(req));
            MavenProject copy = project.clone();
            copy.setProjectBuildingRequest(configuration);
            graphNode = depBuilder.buildDependencyGraph(copy, artifactFilter);
            return graphNode;
        } catch (ComponentLookupException | DependencyGraphBuilderException ex) {
            LOG.log(Level.INFO, "Dependency tree scan failed", ex);
            return null;
        }
    }


    //copied from dependency:tree mojo
    private static DependencyNode createDependencyTree(MavenProject project,
            DependencyTreeBuilder dependencyTreeBuilder, ArtifactRepository localRepository,
            ArtifactFactory artifactFactory, ArtifactMetadataSource artifactMetadataSource,
            ArtifactCollector artifactCollector,
            String scope) {
        ArtifactFilter artifactFilter = createResolvingArtifactFilter(scope);
        
        try {
            // TODO: note that filter does not get applied due to MNG-3236
            return dependencyTreeBuilder.buildDependencyTree(project,
                    localRepository, artifactFactory,
                    artifactMetadataSource, artifactFilter, artifactCollector);
        } catch (DependencyTreeBuilderException exception) {
            LOG.log(Level.INFO, "Dependency tree scan failed", exception);
            return null;
        }
    }

    //copied from dependency:tree mojo
    /**
     * Gets the artifact filter to use when resolving the dependency tree.
     *
     * @return the artifact filter
     */
    private static ArtifactFilter createResolvingArtifactFilter(String scope) {
        ArtifactFilter filter;

        // filter scope
        if (scope != null) {

            filter = new ScopeArtifactFilter(scope);
        } else {
            filter = null;
        }

        return filter;
    }
}
