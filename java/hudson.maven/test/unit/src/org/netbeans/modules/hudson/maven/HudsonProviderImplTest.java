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

package org.netbeans.modules.hudson.maven;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider.Association;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Lookup;

public class HudsonProviderImplTest extends NbTestCase {

    public HudsonProviderImplTest(String n) {
        super(n);
    }

    private Project project(String pomXml) throws Exception {
        clearWorkDir();
        FileObject d = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(d, "pom.xml", pomXml);
        return ProjectManager.getDefault().findProject(d);
    }

    public void testFindAssociation1() throws Exception {
        final FileObject d = FileUtil.toFileObject(getWorkDir());
        assertNull(new HudsonProviderImpl().findAssociation(new Project() {
            public @Override FileObject getProjectDirectory() {
                return d;
            }
            public @Override Lookup getLookup() {
                return Lookup.EMPTY;
            }
        }));
    }

    private static final String BASIC_PROJECT_START = "<project xmlns='http://maven.apache.org/POM/4.0.0'><modelVersion>4.0.0</modelVersion><groupId>grp</groupId><artifactId>art</artifactId><version>1.0</version>";
    private static final String BASIC_PROJECT_END = "</project>";

    public void testFindAssociation2() throws Exception {
        assertNull(new HudsonProviderImpl().findAssociation(project(BASIC_PROJECT_START + BASIC_PROJECT_END)));
    }

    public void testFindAssociation3() throws Exception {
        Association a = new HudsonProviderImpl().findAssociation(project(BASIC_PROJECT_START + "<ciManagement><system>hudson</system><url>https://hudson.geomatys.fr/job/GeoAPI/</url></ciManagement>" + BASIC_PROJECT_END));
        assertNotNull(a);
        assertEquals("https://hudson.geomatys.fr/", a.getServerUrl());
        assertEquals("GeoAPI", a.getJobName());
    }

    public void testFindAssociation4() throws Exception {
        Association a = new HudsonProviderImpl().findAssociation(project(BASIC_PROJECT_START + "<ciManagement><system>Jenkins</system><url>https://builds.apache.org/hudson/job/maven-plugins/</url></ciManagement>" + BASIC_PROJECT_END));
        assertNotNull(a);
        assertEquals("https://builds.apache.org/hudson/", a.getServerUrl());
        assertEquals("maven-plugins", a.getJobName());
    }

    public void testRecordAssociation() throws Exception {
        //synchronous reload of maven project asserts sanoty in some tests..
        System.setProperty("test.reload.sync", "true");        
        Project p = project(BASIC_PROJECT_START + BASIC_PROJECT_END);
        assertTrue(new HudsonProviderImpl().recordAssociation(p, new Association("http://nowhere.net/", "foo bar")));
        assertEquals(BASIC_PROJECT_START + " <ciManagement> <system>hudson</system> <url>http://nowhere.net/job/foo%20bar/</url> </ciManagement> " + BASIC_PROJECT_END,
                p.getProjectDirectory().getFileObject("pom.xml").asText().replaceAll("\\s+", " "));
        NbMavenProject.fireMavenProjectReload(p);
        assertEquals("http://nowhere.net/job/foo%20bar/", String.valueOf(new HudsonProviderImpl().findAssociation(p)));
    }

}
