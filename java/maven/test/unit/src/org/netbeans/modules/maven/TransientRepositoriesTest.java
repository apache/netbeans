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

package org.netbeans.modules.maven;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.repository.RepositorySystem;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class TransientRepositoriesTest extends NbTestCase {

    private static final String CENTRAL_ANON = RepositorySystem.DEFAULT_REMOTE_REPO_ID + ":" + RepositorySystem.DEFAULT_REMOTE_REPO_ID + ":" + RepositorySystem.DEFAULT_REMOTE_REPO_URL + /*MNG-5164*/"/";
    /** @see RepositoryPreferences#RepositoryPreferences() */
    private static final String CENTRAL_NAMED = RepositorySystem.DEFAULT_REMOTE_REPO_ID + ":Central Repository:" + RepositorySystem.DEFAULT_REMOTE_REPO_URL + "/";

    public TransientRepositoriesTest(String name) {
        super(name);
    }

    @Override protected void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("no.local.settings", "true");
        //synchronous reload of maven project asserts sanoty in some tests..
        System.setProperty("test.reload.sync", "true");        
    }

    public void testSimpleRegistration() throws Exception {
        FileObject d = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(d, "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n"
                + "    <modelVersion>4.0.0</modelVersion>\n"
                + "    <groupId>grp</groupId>\n"
                + "    <artifactId>art</artifactId>\n"
                + "    <version>1.0</version>\n"
                + "    <repositories>\n"
                + "        <repository>\n"
                + "            <id>stuff</id>\n"
                + "            <name>Stuff</name>\n"
                + "            <url>http://nowhere.net/stuff</url>\n"
                + "        </repository>\n"
                + "    </repositories>\n"
                + "</project>\n");
        NbMavenProject p = ProjectManager.getDefault().findProject(d).getLookup().lookup(NbMavenProject.class);
        TransientRepositories tr = new TransientRepositories(p);
        assertRepos(CENTRAL_ANON);
        tr.register();
        assertRepos("stuff:Stuff:http://nowhere.net/stuff/", CENTRAL_NAMED);
        tr.unregister();
        assertRepos(CENTRAL_ANON);
    }

    public void testListening() throws Exception {
        FileObject d = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(d, "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n"
                + "    <modelVersion>4.0.0</modelVersion>\n"
                + "    <groupId>grp</groupId>\n"
                + "    <artifactId>art</artifactId>\n"
                + "    <version>1.0</version>\n"
                + "</project>\n");
        Project p = ProjectManager.getDefault().findProject(d);
        TransientRepositories tr = new TransientRepositories(p.getLookup().lookup(NbMavenProject.class));
        assertRepos(CENTRAL_ANON);
        tr.register();
        assertRepos(CENTRAL_NAMED);
        TestFileUtils.writeFile(d, "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>\n"
                + "    <modelVersion>4.0.0</modelVersion>\n"
                + "    <groupId>grp</groupId>\n"
                + "    <artifactId>art</artifactId>\n"
                + "    <version>1.0</version>\n"
                + "    <repositories>\n"
                + "        <repository>\n"
                + "            <id>stuff</id>\n"
                + "            <name>Stuff</name>\n"
                + "            <url>http://nowhere.net/stuff</url>\n"
                + "        </repository>\n"
                + "    </repositories>\n"
                + "</project>\n");
        NbMavenProject.fireMavenProjectReload(p);
        assertRepos("stuff:Stuff:http://nowhere.net/stuff/", CENTRAL_NAMED);
        tr.unregister();
        assertRepos(CENTRAL_ANON);
    }

    // XXX test mirrors; current code mistakenly suppresses <name> of a mirrored repo when mirrored 1-1

    private void assertRepos(String... expected) {
        List<String> actual = new ArrayList<String>();
        for (RepositoryInfo info : RepositoryPreferences.getInstance().getRepositoryInfos()) {
            if (info.isLocal()) {
                continue;
            }
            actual.add(info.getId() + ":" + info.getName() + ":" + info.getRepositoryUrl()); // XXX add mirrored repos too?
        }
        assertEquals(Arrays.toString(expected), actual.toString());
    }

}
