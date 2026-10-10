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
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.CumulativeScopeArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.util.graph.manager.ClassicDependencyManager;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.transformer.JavaScopeDeriver;
import org.eclipse.aether.util.graph.transformer.JavaScopeSelector;
import org.eclipse.aether.util.graph.transformer.NearestVersionSelector;
import org.eclipse.aether.util.graph.transformer.SimpleOptionalitySelector;
import org.eclipse.aether.util.graph.traverser.FatArtifactTraverser;

/**
 * Builds the dependency tree of a project with Maven Resolver. The dependencies are collected with the
 * conflict resolver in verbose mode, so that the nodes dropped by the conflict resolution stay in the graph
 * and are reported as omitted.
 *
 * @author mkleint
 */
public class DependencyTreeFactory {
    private static final Logger LOG = Logger.getLogger(DependencyTreeFactory.class.getName());
    
    @Deprecated
    public static org.netbeans.modules.maven.embedder.tree.DependencyNode createDependencyTree(MavenProject project, MavenEmbedder embedder, String scope) {
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
    public static org.netbeans.modules.maven.embedder.tree.DependencyNode createDependencyTree(MavenProject project, MavenEmbedder embedder, Collection<String> scopes) throws MavenExecutionException {
        RepositorySystem repositorySystem = embedder.lookupComponent(RepositorySystem.class);
        assert repositorySystem != null : "RepositorySystem component not found in maven";

        embedder.setUpLegacySupport();

        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession(embedder.newRepositorySession());
        configureVerboseSession(session);

        ArtifactTypeRegistry stereotypes = session.getArtifactTypeRegistry();
        CollectRequest request = new CollectRequest();
        request.setRootArtifact(RepositoryUtils.toArtifact(project.getArtifact()));
        request.setRepositories(RepositoryUtils.toRepos(project.getRemoteArtifactRepositories()));
        for (Dependency dependency : project.getDependencies()) {
            request.addDependency(RepositoryUtils.toDependency(dependency, stereotypes));
        }
        if (project.getDependencyManagement() != null) {
            for (Dependency dependency : project.getDependencyManagement().getDependencies()) {
                request.addManagedDependency(RepositoryUtils.toDependency(dependency, stereotypes));
            }
        }

        try {
            DependencyNode root = repositorySystem.collectDependencies(session, request).getRoot();
            return createDependencyTree(root, project.getArtifact(), createResolvingArtifactFilter(scopes));
        } catch (DependencyCollectionException exception) {
            throw new MavenExecutionException("Dependency tree scan failed", exception);
        }
    }

    static void configureVerboseSession(DefaultRepositorySystemSession session) {
        // keep the nodes dropped by the conflict resolution, marked with the winner
        session.setDependencyGraphTransformer(new ConflictResolver(
                new NearestVersionSelector(),
                new JavaScopeSelector(),
                new SimpleOptionalitySelector(),
                new JavaScopeDeriver()));
        session.setConfigProperty(ConflictResolver.CONFIG_PROP_VERBOSE, true);
        // a session that was not created by Maven has none of the following
        if (session.getDependencySelector() == null) {
            session.setDependencySelector(new AndDependencySelector(
                    new ScopeDependencySelector("test", "provided"),
                    new OptionalDependencySelector(),
                    new ExclusionDependencySelector()));
        }
        if (session.getDependencyManager() == null) {
            session.setDependencyManager(new ClassicDependencyManager());
        }
        if (session.getDependencyTraverser() == null) {
            session.setDependencyTraverser(new FatArtifactTraverser());
        }
    }

    /**
     * Maps the verbose graph collected by Maven Resolver to the tree. The nodes that lost the conflict
     * resolution carry the winner in {@link ConflictResolver#NODE_DATA_WINNER} and have no children.
     * A loser is {@link org.netbeans.modules.maven.embedder.tree.DependencyNode#OMITTED_FOR_CYCLE} if the winner is its ancestor,
     * {@link org.netbeans.modules.maven.embedder.tree.DependencyNode#OMITTED_FOR_DUPLICATE} if the winner has the same version, and
     * {@link org.netbeans.modules.maven.embedder.tree.DependencyNode#OMITTED_FOR_CONFLICT} otherwise.
     *
     * @param root the root of the collected graph
     * @param rootArtifact the artifact of the project
     * @param filter filters the dependencies, may be {@code null}
     */
    static org.netbeans.modules.maven.embedder.tree.DependencyNode createDependencyTree(DependencyNode root, Artifact rootArtifact, ArtifactFilter filter) {
        Set<DependencyNode> ancestors = Collections.newSetFromMap(new IdentityHashMap<>());
        return convert(root, rootArtifact, filter, ancestors, new ArrayList<>());
    }

    private static org.netbeans.modules.maven.embedder.tree.DependencyNode convert(DependencyNode node, Artifact artifact,
            ArtifactFilter filter, Set<DependencyNode> ancestors, List<String> trail) {
        trail.add(artifact.getId());
        if (trail.size() > 1) { // do not modify the artifact of the project
            artifact.setDependencyTrail(new ArrayList<>(trail));
        }
        DependencyNode winner = (DependencyNode) node.getData().get(ConflictResolver.NODE_DATA_WINNER);
        org.netbeans.modules.maven.embedder.tree.DependencyNode result;
        if (winner == null) {
            result = new org.netbeans.modules.maven.embedder.tree.DependencyNode(artifact);
        } else if (ancestors.contains(winner)) {
            result = new org.netbeans.modules.maven.embedder.tree.DependencyNode(artifact,
                    org.netbeans.modules.maven.embedder.tree.DependencyNode.OMITTED_FOR_CYCLE, null);
        } else {
            Artifact related = toArtifact(winner);
            boolean sameVersion = winner.getArtifact().getBaseVersion().equals(node.getArtifact().getBaseVersion());
            result = new org.netbeans.modules.maven.embedder.tree.DependencyNode(artifact,
                    sameVersion ? org.netbeans.modules.maven.embedder.tree.DependencyNode.OMITTED_FOR_DUPLICATE
                                : org.netbeans.modules.maven.embedder.tree.DependencyNode.OMITTED_FOR_CONFLICT,
                    related);
        }
        ancestors.add(node);
        for (DependencyNode child : node.getChildren()) {
            Artifact childArtifact = toArtifact(child);
            if (filter == null || filter.include(childArtifact)) {
                result.addChild(convert(child, childArtifact, filter, ancestors, trail));
            }
        }
        ancestors.remove(node);
        trail.remove(trail.size() - 1);
        return result;
    }

    private static Artifact toArtifact(DependencyNode node) {
        Artifact artifact = RepositoryUtils.toArtifact(node.getArtifact());
        if (node.getDependency() != null) {
            artifact.setScope(node.getDependency().getScope());
            artifact.setOptional(node.getDependency().isOptional());
        }
        return artifact;
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
