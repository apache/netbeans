/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
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

class ArtifactDependencyIndexCreator extends AbstractIndexCreator {

    private static final Logger LOG = Logger.getLogger(ArtifactDependencyIndexCreator.class.getName());

    private static final String NS = "urn:NbIndexCreator";
    private static final String NB_DEPENDENCY_GROUP = "nbdg";
    private static final String NB_DEPENDENCY_ARTIFACT = "nbda";
    private static final String NB_DEPENDENCY_VERSION = "nbdv";
    private static IndexerField FLD_NB_DEPENDENCY_GROUP = new IndexerField(new Field(null, NS, NB_DEPENDENCY_GROUP, "Dependency group"), IndexerFieldVersion.V3, NB_DEPENDENCY_GROUP, "Dependency group", Store.NO, Index.NOT_ANALYZED);
    private static IndexerField FLD_NB_DEPENDENCY_ARTIFACT = new IndexerField(new Field(null, NS, NB_DEPENDENCY_ARTIFACT, "Dependency artifact"), IndexerFieldVersion.V3, NB_DEPENDENCY_ARTIFACT, "Dependency artifact", Store.NO, Index.NOT_ANALYZED);
    private static IndexerField FLD_NB_DEPENDENCY_VERSION = new IndexerField(new Field(null, NS, NB_DEPENDENCY_VERSION, "Dependency version"), IndexerFieldVersion.V3, NB_DEPENDENCY_VERSION, "Dependency version", Store.NO, Index.NOT_ANALYZED);

    private final List<ArtifactRepository> remoteRepos;
    private final Map<ArtifactInfo, List<Dependency>> dependenciesByArtifact = new WeakHashMap<ArtifactInfo, List<Dependency>>();
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
            ex.printStackTrace();
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
        final BooleanQuery q = new BooleanQuery();
        q.add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_GROUP, groupId)), BooleanClause.Occur.MUST));
        q.add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_ARTIFACT, artifactId)), BooleanClause.Occur.MUST));
        q.add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_VERSION, version)), BooleanClause.Occur.MUST));
        return q;
    }

    @Override public Collection<IndexerField> getIndexerFields() {
        return Arrays.asList(FLD_NB_DEPENDENCY_GROUP, FLD_NB_DEPENDENCY_ARTIFACT, FLD_NB_DEPENDENCY_VERSION);
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
