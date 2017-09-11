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
    protected NexusRepositoryIndexerImpl nrii;

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
        nrii = Lookup.getDefault().lookup(NexusRepositoryIndexerImpl.class);
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
