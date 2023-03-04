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

package org.netbeans.modules.maven.execute;

import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class ReactorCheckerTest extends NbTestCase {

    public ReactorCheckerTest(String name) {
        super(name);
    }
    
    private FileObject d;
    @Override protected void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
        //synchronous reload of maven project asserts sanoty in some tests..
        System.setProperty("test.reload.sync", "true");
        //works for me locally even without this, fails on hudson. candidate for @randomlyfails
    }
    
    private FileObject project(String subdir, String body) throws Exception {
        TestFileUtils.writeFile(d, (subdir != null ? subdir + "/" : "") + "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'><modelVersion>4.0.0</modelVersion>" + body + "</project>");
        return subdir != null ? d.getFileObject(subdir) : d;
    }

    private NbMavenProject load(String subdir) throws Exception {
        Project p = ProjectManager.getDefault().findProject(subdir != null ? d.getFileObject(subdir) : d);
        assertNotNull(p);
        NbMavenProject nbmp = p.getLookup().lookup(NbMavenProject.class);
        assertNotNull(nbmp);
        return nbmp;
    }

    public void testFindReactorStandalone() throws Exception {
        project(null, "<groupId>g</groupId><artifactId>m</artifactId><version>0</version>");
        NbMavenProject p = load(null);
        assertEquals(p, ReactorChecker.findReactor(p));
    }

    public void testFindReactorFromDirectChild() throws Exception {
        project(null, "<groupId>g</groupId><artifactId>p</artifactId><version>0</version><packaging>pom</packaging><modules><module>m</module></modules>");
        project("m", "<parent><groupId>g</groupId><artifactId>p</artifactId><version>0</version></parent><artifactId>m</artifactId>");
        NbMavenProject p = load(null);
        assertEquals(p, ReactorChecker.findReactor(load("m")));
    }

    public void testFindReactorFromIndirectChild() throws Exception {
        project(null, "<groupId>g</groupId><artifactId>super</artifactId><version>0</version><packaging>pom</packaging><modules><module>sub</module></modules>");
        project("sub", "<parent><groupId>g</groupId><artifactId>super</artifactId><version>0</version></parent><artifactId>sub</artifactId><packaging>pom</packaging><modules><module>m</module></modules>");
        project("sub/m", "<parent><groupId>g</groupId><artifactId>sub</artifactId><version>0</version></parent><artifactId>m</artifactId>");
        NbMavenProject p = load(null);
        assertEquals(p, ReactorChecker.findReactor(load("sub")));
        assertEquals(p, ReactorChecker.findReactor(load("sub/m")));
    }

    public void testFindReactorFromExplicitParentPath() throws Exception {
        // PENDING: uses unique artifact coordinates g:px:0 instead of g:p:0, see NETBEANS-5316
        project("root", "<groupId>g</groupId><artifactId>px</artifactId><version>0</version><packaging>pom</packaging><modules><module>../m</module></modules>");
        project("m", "<parent><groupId>g</groupId><artifactId>px</artifactId><version>0</version><relativePath>../root/pom.xml</relativePath></parent><artifactId>m</artifactId>");
        NbMavenProject p = load("root");
        assertEquals(p, ReactorChecker.findReactor(load("m")));
    }

    public void testFindReactorWrongParentVersion() throws Exception {
        project(null, "<groupId>g</groupId><artifactId>p</artifactId><version>0</version><packaging>pom</packaging><modules><module>m</module></modules>");
        project("m", "<parent><groupId>g</groupId><artifactId>p</artifactId><version>1</version></parent><artifactId>m</artifactId>");
        NbMavenProject m = load("m");
        assertEquals(load(null), ReactorChecker.findReactor(m));
    }

    public void testFindReactorNonexistentParent() throws Exception {
        project("m", "<parent><groupId>g</groupId><artifactId>p</artifactId><version>0</version></parent><artifactId>m</artifactId>");
        NbMavenProject m = load("m");
        assertEquals(m, ReactorChecker.findReactor(m));
    }

    public void testFindReactorNonexistentSiteParent() throws Exception {
        project(null, "<parent><groupId>site</groupId><artifactId>parent</artifactId><version>0</version></parent><groupId>g</groupId><artifactId>p</artifactId><version>0</version><packaging>pom</packaging><modules><module>m</module></modules>");
        project("m", "<parent><groupId>g</groupId><artifactId>p</artifactId><version>0</version></parent><artifactId>m</artifactId>");
        NbMavenProject p = load(null);
        assertEquals(p, ReactorChecker.findReactor(load("m")));
    }

    public void testFindReactorNonAggregatorParent() throws Exception {
        project("parent", "<groupId>g</groupId><artifactId>p</artifactId><version>0</version><packaging>pom</packaging>");
        project("m", "<parent><groupId>g</groupId><artifactId>p</artifactId><version>0</version><relativePath>../parent/pom.xml</relativePath></parent><artifactId>m</artifactId>");
        NbMavenProject m = load("m");
        assertEquals(m, ReactorChecker.findReactor(m));
    }

    public void testFindReactorAggregatorPlusPureParent() throws Exception {
        project(null, "<groupId>g</groupId><artifactId>r</artifactId><version>0</version><packaging>pom</packaging><modules><module>m</module></modules>");
        project("parent", "<groupId>g</groupId><artifactId>p</artifactId><version>0</version><packaging>pom</packaging>");
        project("m", "<parent><groupId>g</groupId><artifactId>p</artifactId><version>0</version><relativePath>../parent/pom.xml</relativePath></parent><artifactId>m</artifactId>");
        NbMavenProject p = load(null);
        assertEquals(p, ReactorChecker.findReactor(load("m")));
    }

    public void testFindReactorParentDoesNotListMe() throws Exception {
        project(null, "<groupId>g</groupId><artifactId>p</artifactId><version>0</version><packaging>pom</packaging><modules><module>other</module></modules>");
        project("m", "<parent><groupId>g</groupId><artifactId>p</artifactId><version>0</version></parent><artifactId>m</artifactId>");
        NbMavenProject m = load("m");
        assertEquals(m, ReactorChecker.findReactor(m));
    }

    public void testFindReactorParentListsMeOnlyInInactiveProfile() throws Exception {
        project(null, "<groupId>g</groupId><artifactId>p</artifactId><version>0</version><packaging>pom</packaging><profiles><profile><id>inactive</id><modules><module>m</module></modules></profile></profiles>");
        project("m", "<parent><groupId>g</groupId><artifactId>p</artifactId><version>0</version></parent><artifactId>m</artifactId>");
        NbMavenProject m = load("m");
        assertEquals(m, ReactorChecker.findReactor(m));
    }

    public void testFindReactorParentListsMeInSelectedProfile() throws Exception {
        project(null, "<groupId>g</groupId><artifactId>p</artifactId><version>0</version><packaging>pom</packaging><profiles><profile><id>sel</id><modules><module>m</module></modules></profile></profiles>");
        project("m", "<parent><groupId>g</groupId><artifactId>p</artifactId><version>0</version></parent><artifactId>m</artifactId>");
        M2ConfigProvider cp = ProjectManager.getDefault().findProject(d).getLookup().lookup(M2ConfigProvider.class);
        boolean found = false;
        for (M2Configuration cfg : cp.getConfigurations()) {
            if (cfg.getActivatedProfiles().equals(Collections.singletonList("sel"))) {
                cp.setActiveConfiguration(cfg);
                found = true;
                break;
            }
        }
        assertTrue(found);
        assertEquals(load(null), ReactorChecker.findReactor(load("m")));
    }

}
