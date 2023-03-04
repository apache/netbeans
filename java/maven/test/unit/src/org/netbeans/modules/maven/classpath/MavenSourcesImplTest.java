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

package org.netbeans.modules.maven.classpath;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.test.MockChangeListener;

public class MavenSourcesImplTest extends NbTestCase {

    public MavenSourcesImplTest(String name) {
        super(name);
    }

    private FileObject d;
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
    }

    public void testITSourceGroups() throws Exception {
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>1.0-SNAPSHOT</version>" +
                "<name>Test</name>" +
                "<build>" +
                "<testSourceDirectory>src/it/java</testSourceDirectory>" +
                "</build>" +
                "</project>");
        FileObject itsrc = FileUtil.createFolder(d, "src/it/java");
        SourceGroup[] grps = ProjectUtils.getSources(ProjectManager.getDefault().findProject(d)).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(1, grps.length);
        assertEquals(itsrc, grps[0].getRootFolder());
    }

    public void testFragmentaryResourceDecl() throws Exception { // #195928
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>1.0-SNAPSHOT</version>" +
                "<build>" +
                "<resources>" +
                "<resource>" +
                "<directory>.</directory>" +
                "<targetPath>META-INF</targetPath>" +
                "<includes>" +
                "<include>changelog.txt</include>" +
                "</includes>" +
                "</resource>" +
                "</resources>" +
                "</build>" +
                "</project>");
        SourceGroup[] grps = ProjectUtils.getSources(ProjectManager.getDefault().findProject(d)).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        assertEquals(0, grps.length);
    }

    public void testGeneratedSources() throws Exception { // #187595
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>0</version>" +
                "</project>");
        FileObject src = FileUtil.createFolder(d, "src/main/java");
        FileObject gsrc = FileUtil.createFolder(d, "target/generated-sources/xjc");
        gsrc.createData("Whatever.class");
        FileObject tsrc = FileUtil.createFolder(d, "src/test/java");
        FileObject gtsrc = FileUtil.createFolder(d, "target/generated-test-sources/jaxb");
        gtsrc.createData("Whatever.class");
        SourceGroup[] grps = ProjectUtils.getSources(ProjectManager.getDefault().findProject(d)).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(2, grps.length);
        assertEquals(src, grps[0].getRootFolder());
        assertEquals(tsrc, grps[1].getRootFolder());
        grps = ProjectUtils.getSources(ProjectManager.getDefault().findProject(d)).getSourceGroups(MavenSourcesImpl.TYPE_GEN_SOURCES);
        assertEquals(2, grps.length);
        assertEquals(gsrc, grps[0].getRootFolder());
        assertEquals(gtsrc, grps[1].getRootFolder());
    }

    public void testNewlyCreatedSourceGroup() throws Exception { // #200969
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version></project>");
        FileObject main = FileUtil.createFolder(d, "src/main/java");
        Project p = ProjectManager.getDefault().findProject(d);
        Sources s = ProjectUtils.getSources(p);
        SourceGroup[] grps = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(1, grps.length);
        assertEquals(main, grps[0].getRootFolder());
        MockChangeListener l = new MockChangeListener();
        s.addChangeListener(l);
        SourceGroup g2 = SourceGroupModifier.createSourceGroup(p, JavaProjectConstants.SOURCES_TYPE_JAVA, JavaProjectConstants.SOURCES_HINT_TEST);
        l.assertEvent();
        grps = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(2, grps.length);
        assertEquals(main, grps[0].getRootFolder());
        assertEquals(g2, grps[1]);
    }

    public void testManuallyDeletedSourceGroup() throws Exception { // #204545
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version></project>");
        FileObject main = FileUtil.createFolder(d, "src/main/java");
        FileObject test = FileUtil.createFolder(d, "src/test/java");
        Project p = ProjectManager.getDefault().findProject(d);
        Sources s = ProjectUtils.getSources(p);
        SourceGroup[] grps = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(2, grps.length);
        assertEquals(main, grps[0].getRootFolder());
        assertEquals(test, grps[1].getRootFolder());
        MockChangeListener l = new MockChangeListener();
        s.addChangeListener(l);
        test.getParent().delete();
        l.assertEvent();
        grps = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(1, grps.length);
        assertEquals(main, grps[0].getRootFolder());
    }

}
