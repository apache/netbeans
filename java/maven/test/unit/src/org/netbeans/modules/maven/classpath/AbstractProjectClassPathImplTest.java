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

package org.netbeans.modules.maven.classpath;

import java.io.File;
import java.util.Collections;
import java.util.logging.Level;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.codehaus.plexus.util.FileUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.embedder.EmbedderFactory;

public class AbstractProjectClassPathImplTest extends NbTestCase {

    public AbstractProjectClassPathImplTest(String name) {
        super(name);
    }

    private File repo;

    @Override
    protected void setUp() throws Exception {
        repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
    }

    @Override
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File(repo, "nbtest"));
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected String logRoot() {
        return AbstractProjectClassPathImpl.class.getName();
    }

    public void testGetFile() throws Exception {
        File installed = new File(repo, "nbtest/grp/art/1.10-SNAPSHOT/art-1.10-SNAPSHOT.jar");
        File downloaded = new File(repo, "nbtest/grp/art/1.10-SNAPSHOT/art-1.10-20210520.222429-1.jar");
        Artifact a = EmbedderFactory.getProjectEmbedder().createArtifact("nbtest.grp", "art", "1.10-20210520.222429-1", "jar");
        assertNull(AbstractProjectClassPathImpl.getFile(a));
        try {
            EmbedderFactory.getProjectEmbedder().resolve(a, Collections.emptyList(), EmbedderFactory.getProjectEmbedder().getLocalRepository());
        } catch (ArtifactNotFoundException ex) {
            // the downloaded artifact was not found, expected as only -SNAPSHOT is installed.
        }
        assertEquals(installed, a.getFile());
        assertEquals(installed, AbstractProjectClassPathImpl.getFile(a));
        FileUtils.mkdir(downloaded.getParent());
        FileUtils.fileWrite(downloaded, "<irrelevant>");
        assertEquals(downloaded, AbstractProjectClassPathImpl.getFile(a));
    }

}
