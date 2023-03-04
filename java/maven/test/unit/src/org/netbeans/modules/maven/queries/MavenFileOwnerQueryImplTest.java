/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.maven.queries;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.util.test.TestFileUtils;

public class MavenFileOwnerQueryImplTest extends NbTestCase {

    public MavenFileOwnerQueryImplTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testFindCoordinates() throws Exception {
        File repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
        assertEquals("[test, prj, 1.0]", Arrays.toString(MavenFileOwnerQueryImpl.findCoordinates(new File(repo, "test/prj/1.0/prj-1.0.jar"))));
        assertEquals("[my.test, prj, 1.0-SNAPSHOT]", Arrays.toString(MavenFileOwnerQueryImpl.findCoordinates(new File(repo, "my/test/prj/1.0-SNAPSHOT/prj-1.0-SNAPSHOT.pom"))));
        assertEquals("null", Arrays.toString(MavenFileOwnerQueryImpl.findCoordinates(new File(repo, "test/prj/1.0"))));
        assertEquals("null", Arrays.toString(MavenFileOwnerQueryImpl.findCoordinates(getWorkDir())));
    }

    public void testMultipleVersions() throws Exception {
        File prj10 = new File(getWorkDir(), "prj10");
        TestFileUtils.writeFile(new File(prj10, "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version></project>");
        NbMavenProjectImpl p10 = (NbMavenProjectImpl) ProjectManager.getDefault().findProject(FileUtil.toFileObject(prj10));
        File prj11 = new File(getWorkDir(), "prj11");
        TestFileUtils.writeFile(new File(prj11, "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.1</version></project>");
        NbMavenProjectImpl p11 = (NbMavenProjectImpl) ProjectManager.getDefault().findProject(FileUtil.toFileObject(prj11));
        MavenFileOwnerQueryImpl foq = MavenFileOwnerQueryImpl.getInstance();
        File repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
        File art10 = new File(repo, "test/prj/1.0/prj-1.0.jar");
        File art11 = new File(repo, "test/prj/1.1/prj-1.1.jar");
        assertEquals(null, foq.getOwner(Utilities.toURI(art10)));
        assertEquals(null, foq.getOwner(Utilities.toURI(art11)));
        foq.registerProject(p10, true);
        assertEquals(p10, foq.getOwner(Utilities.toURI(art10)));
        assertEquals(null, foq.getOwner(Utilities.toURI(art11)));
        foq.registerProject(p11, true);
        assertEquals(p10, foq.getOwner(Utilities.toURI(art10)));
        assertEquals(p11, foq.getOwner(Utilities.toURI(art11)));
    }
    
    public void testOldEntriesGetRemoved() throws Exception {
        URL url = new URL("file:///users/mkleint/aaa/bbb");
        MavenFileOwnerQueryImpl.getInstance().registerCoordinates("a", "b", "0", url, true);
        assertNotNull(MavenFileOwnerQueryImpl.prefs().get("a:b:0", null));
        MavenFileOwnerQueryImpl.getInstance().registerCoordinates("a", "b", "1", url, true);
        assertNotNull(MavenFileOwnerQueryImpl.prefs().get("a:b:1", null));
        assertNull(MavenFileOwnerQueryImpl.prefs().get("a:b:0", null));
        
    }

}
