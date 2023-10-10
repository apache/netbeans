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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidArtifactRTException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.Field;
import org.apache.maven.index.IndexerField;
import org.apache.maven.index.IndexerFieldVersion;
import org.apache.maven.index.creator.AbstractIndexCreator;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.openide.util.Exceptions;

class ArtifactDependencyIndexCreator extends AbstractIndexCreator {

    private static final Logger LOG = Logger.getLogger(ArtifactDependencyIndexCreator.class.getName());

    private static final String NS = "urn:NbIndexCreator";
    private static final String NB_DEPENDENCY_GROUP = "nbdg";
    private static final String NB_DEPENDENCY_ARTIFACT = "nbda";
    private static final String NB_DEPENDENCY_VERSION = "nbdv";

    private static final IndexerField FLD_NB_DEPENDENCY_GROUP = new IndexerField(new Field(null, NS, NB_DEPENDENCY_GROUP, "Dependency group"), IndexerFieldVersion.V3, NB_DEPENDENCY_GROUP, "Dependency group", IndexerField.KEYWORD_NOT_STORED);
    private static final IndexerField FLD_NB_DEPENDENCY_ARTIFACT = new IndexerField(new Field(null, NS, NB_DEPENDENCY_ARTIFACT, "Dependency artifact"), IndexerFieldVersion.V3, NB_DEPENDENCY_ARTIFACT, "Dependency artifact", IndexerField.KEYWORD_NOT_STORED);
    private static final IndexerField FLD_NB_DEPENDENCY_VERSION = new IndexerField(new Field(null, NS, NB_DEPENDENCY_VERSION, "Dependency version"), IndexerFieldVersion.V3, NB_DEPENDENCY_VERSION, "Dependency version", IndexerField.KEYWORD_NOT_STORED);

    private final List<ArtifactRepository> remoteRepos;
    private final Map<ArtifactInfo, List<Dependency>> dependenciesByArtifact = new WeakHashMap<>();
    private final MavenEmbedder embedder;

    ArtifactDependencyIndexCreator() {
        super(ArtifactDependencyIndexCreator.class.getName(), Arrays.asList(MinimalArtifactInfoIndexCreator.ID));
        embedder = EmbedderFactory.getProjectEmbedder();
        remoteRepos = RepositoryPreferences.getInstance().remoteRepositories(embedder);
    }

    @Override public void populateArtifactInfo(ArtifactContext context) throws IOException {
        ArtifactInfo ai = context.getArtifactInfo();
        if (ai.getClassifier() != null) {
            return;
        }
        try {
            MavenProject mp = load(ai);
            if (mp != null) {
                List<Dependency> dependencies = mp.getDependencies();
                LOG.log(Level.FINER, "Successfully loaded project model from repository for {0} with {1} dependencies", new Object[] {ai, dependencies.size()});
                dependenciesByArtifact.put(ai, dependencies);
            }
        } catch (InvalidArtifactRTException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override public void updateDocument(ArtifactInfo ai, Document doc) {
        List<Dependency> dependencies = dependenciesByArtifact.get(ai);
        if (dependencies != null) {
            for (Dependency d : dependencies) {
                doc.add(FLD_NB_DEPENDENCY_GROUP.toField(d.getGroupId()));
                doc.add(FLD_NB_DEPENDENCY_ARTIFACT.toField(d.getArtifactId()));
                doc.add(FLD_NB_DEPENDENCY_VERSION.toField(d.getVersion()));
            }
        }
    }

    static Query query(String groupId, String artifactId, String version) {
        return new BooleanQuery.Builder()
            .add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_GROUP, groupId)), BooleanClause.Occur.MUST))
            .add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_ARTIFACT, artifactId)), BooleanClause.Occur.MUST))
            .add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_VERSION, version)), BooleanClause.Occur.MUST))
            .build();
    }

    @Override public Collection<IndexerField> getIndexerFields() {
        return List.of(FLD_NB_DEPENDENCY_GROUP, FLD_NB_DEPENDENCY_ARTIFACT, FLD_NB_DEPENDENCY_VERSION);
    }

    private MavenProject load(ArtifactInfo ai) {
        try {
            Artifact projectArtifact = embedder.createArtifact(ai.getGroupId(), ai.getArtifactId(), ai.getVersion(), ai.getPackaging() != null ? ai.getPackaging() : "jar");
            ProjectBuildingRequest dpbr = embedder.createMavenExecutionRequest().getProjectBuildingRequest();
            //mkleint: remote repositories don't matter we use project embedder.
            dpbr.setRemoteRepositories(remoteRepos);
            dpbr.setProcessPlugins(false);
            dpbr.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);

            ProjectBuildingResult res = embedder.buildProject(projectArtifact, dpbr);
            if (res.getProject() != null) {
                return res.getProject();
            } else {
                LOG.log(Level.FINER, "No project model from repository for {0}: {1}", new Object[] {ai, res.getProblems()});
            }
        } catch (ProjectBuildingException ex) {
            LOG.log(Level.FINER, "Failed to load project model from repository for {0}: {1}", new Object[] {ai, ex});
        } catch (Exception exception) {
            LOG.log(Level.FINER, "Failed to load project model from repository for " + ai, exception);
        }
        return null;
    }
    
    @Override public boolean updateArtifactInfo(Document doc, ArtifactInfo ai) {
        return false;
    }

}
