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

import java.io.File;
import java.util.logging.Level;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.openide.modules.api.PlacesTestUtils;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.test.TestFileUtils;

abstract class NexusTestBase extends NbTestCase {

    protected NexusTestBase(String n) {
        super(n);
    }

    private ArtifactRepository defaultArtifactRepository;
    private MavenEmbedder embedder;
    private ArtifactInstaller artifactInstaller;
    protected RepositoryInfo info;
    protected NexusRepositoryIndexManager nrii;

    @Override protected void setUp() throws Exception {
        clearWorkDir();
        PlacesTestUtils.setUserDirectory(getWorkDir());
        File repo = new File(getWorkDir(), "repo");
        embedder = EmbedderFactory.getProjectEmbedder();
        System.setProperty("no.local.settings", "true");
        defaultArtifactRepository = embedder.lookupComponent(ArtifactRepositoryFactory.class).createArtifactRepository("test", Utilities.toURI(repo).toString(), "default", null, null);
        embedder.setUpLegacySupport(); // XXX could use org.sonatype.aether.RepositorySystem to avoid maven-compat
        artifactInstaller = embedder.lookupComponent(ArtifactInstaller.class);
        info = new RepositoryInfo("test", "Test", repo.getAbsolutePath(), null);
        RepositoryPreferences.getInstance().addOrModifyRepositoryInfo(info);
        nrii = Lookup.getDefault().lookup(NexusRepositoryIndexManager.class);
    }

    @Override protected Level logLevel() {
        return Level.FINER;
    }

    @Override protected String logRoot() {
        return "org.netbeans.modules.maven.indexer";
    }

    protected final void install(File f, String groupId, String artifactId, String version, String packaging) throws Exception {
        //XXX: this can behave unpredictably when ~/.m2/settings.xml file contains mirror declarations. can result in failed tests
        artifactInstaller.install(f, embedder.createArtifact(groupId, artifactId, version, packaging), defaultArtifactRepository);
    }

    protected final void installPOM(String groupId, String artifactId, String version, String packaging) throws Exception {
        install(TestFileUtils.writeFile(new File(getWorkDir(), artifactId + ".pom"),
                "<project><modelVersion>4.0.0</modelVersion>" +
                "<groupId>" + groupId + "</groupId><artifactId>" + artifactId + "</artifactId>" +
                "<version>" + version + "</version><packaging>" + packaging + "</packaging></project>"), groupId, artifactId, version, "pom");
    }

}
