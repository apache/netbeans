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

package org.netbeans.modules.maven.indexer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.Field;
import org.apache.maven.index.IndexerField;
import org.apache.maven.index.IndexerFieldVersion;
import org.apache.maven.index.creator.AbstractIndexCreator;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.apache.maven.repository.supplier.RepositorySystemSupplier;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;

import static java.util.function.Predicate.not;

public final class ArtifactDependencyIndexCreator extends AbstractIndexCreator implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(ArtifactDependencyIndexCreator.class.getName());

    private static final String NS = "urn:NbIndexCreator";
    private static final String NB_DEPENDENCY_GROUP = "nbdg";
    private static final String NB_DEPENDENCY_ARTIFACT = "nbda";
    private static final String NB_DEPENDENCY_VERSION = "nbdv";

    private static final IndexerField FLD_NB_DEPENDENCY_GROUP = new IndexerField(new Field(null, NS, NB_DEPENDENCY_GROUP, "Dependency group"), IndexerFieldVersion.V3, NB_DEPENDENCY_GROUP, "Dependency group", IndexerField.KEYWORD_NOT_STORED);
    private static final IndexerField FLD_NB_DEPENDENCY_ARTIFACT = new IndexerField(new Field(null, NS, NB_DEPENDENCY_ARTIFACT, "Dependency artifact"), IndexerFieldVersion.V3, NB_DEPENDENCY_ARTIFACT, "Dependency artifact", IndexerField.KEYWORD_NOT_STORED);
    private static final IndexerField FLD_NB_DEPENDENCY_VERSION = new IndexerField(new Field(null, NS, NB_DEPENDENCY_VERSION, "Dependency version"), IndexerFieldVersion.V3, NB_DEPENDENCY_VERSION, "Dependency version", IndexerField.KEYWORD_NOT_STORED);

    private final RepositorySystem repositorySystem;
    private final RepositorySystemSession.CloseableSession repositorySession;

    private final List<RemoteRepository> remoteRepositories;

    private final Map<ArtifactInfo, List<Dependency>> dependenciesByArtifact = new WeakHashMap<>();

    public ArtifactDependencyIndexCreator(RepositoryInfo info) {
        super(ArtifactDependencyIndexCreator.class.getName(), Arrays.asList(MinimalArtifactInfoIndexCreator.ID));

        if (!info.isLocal()) {
            throw new IllegalArgumentException(info + " not local");
        }
        
        this.remoteRepositories = RepositoryPreferences.getInstance().getRepositoryInfos().stream()
                .filter(not(RepositoryInfo::isLocal))
                .map(r -> new RemoteRepository.Builder(r.getId(), "default", r.getRepositoryUrl()).build())
                .toList();

        this.repositorySystem = new RepositorySystemSupplier().get();
        this.repositorySession = repositorySystem.createSessionBuilder()
                .withLocalRepositories(new LocalRepository(Path.of(info.getRepositoryPath())))
                .setSystemProperties(EmbedderFactory.getOnlineEmbedder().getSystemProperties())
                .build();
    }

    @Override
    public void populateArtifactInfo(ArtifactContext context)  throws IOException {

        ArtifactInfo ai = context.getArtifactInfo();
        if (ai.getClassifier() != null) {
            return;
        }
        try {
            List<Dependency> deps = getDirectDependencies(ai);
            LOG.log(Level.FINER, "Successfully loaded project descriptor for {0} with {1} dependencies", new Object[]{ai, deps.size()});
            dependenciesByArtifact.put(ai, deps);
        } catch (ArtifactDescriptorException | RuntimeException ex) {
            LOG.log(Level.FINER, "Failed to load artifact descriptor for " + ai, ex);
        }
    }

    @Override
    public void updateDocument(ArtifactInfo ai, Document doc) {
        List<Dependency> dependencies = dependenciesByArtifact.get(ai);
        if (dependencies == null) {
            return;
        }
        for (Dependency dependency : dependencies) {
            Artifact artifact = dependency.getArtifact();
            doc.add(FLD_NB_DEPENDENCY_GROUP.toField(artifact.getGroupId()));
            doc.add(FLD_NB_DEPENDENCY_ARTIFACT.toField(artifact.getArtifactId()));
            doc.add(FLD_NB_DEPENDENCY_VERSION.toField(artifact.getVersion()));
        }
    }

    static Query query(String groupId, String artifactId, String version) {
        return new BooleanQuery.Builder()
            .add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_GROUP, groupId)), BooleanClause.Occur.MUST))
            .add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_ARTIFACT, artifactId)), BooleanClause.Occur.MUST))
            .add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_VERSION, version)), BooleanClause.Occur.MUST))
            .build();
    }

    @Override
    public Collection<IndexerField> getIndexerFields() {
        return List.of(FLD_NB_DEPENDENCY_GROUP, FLD_NB_DEPENDENCY_ARTIFACT, FLD_NB_DEPENDENCY_VERSION);
    }

    private List<Dependency> getDirectDependencies(ArtifactInfo ai) throws ArtifactDescriptorException {

        Artifact artifact = new DefaultArtifact(
                ai.getGroupId(),
                ai.getArtifactId(),
                ai.getClassifier(),
                Objects.requireNonNullElse(ai.getPackaging(), "jar"),
                ai.getVersion()
        );

        ArtifactDescriptorRequest request = new ArtifactDescriptorRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteRepositories);

        return repositorySystem.readArtifactDescriptor(repositorySession, request)
                               .getDependencies();
    }

    @Override
    public boolean updateArtifactInfo(Document doc, ArtifactInfo ai) {
        return false;
    }

    @Override
    public void close() {
        repositorySession.close();
        repositorySystem.close();
    }

}
