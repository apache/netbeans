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

import java.util.regex.Matcher;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.test.MockChangeListener;

public class MavenSourceLevelImplTest extends NbTestCase {

    public MavenSourceLevelImplTest(String name) {
        super(name);
    }

    private FileObject wd;

    protected @Override void setUp() throws Exception {
        System.setProperty("level", "1.4");// workaround for testSystemPropertySourceLevel()
        clearWorkDir();
        wd = FileUtil.toFileObject(getWorkDir());
    }

    public void testNoCompilerPluginSpecified() throws Exception {
        TestFileUtils.writeFile(wd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version></project>");
        FileObject source = TestFileUtils.writeFile(wd, "src/main/java/p/C.java", "package p; class C {}");
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(source));
    }

    public void testCompilerPluginSpecifiedWithoutVersion() throws Exception {
        TestFileUtils.writeFile(wd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version>"
                + "<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId></plugin></plugins></build>"
                + "</project>");
        FileObject source = TestFileUtils.writeFile(wd, "src/main/java/p/C.java", "package p; class C {}");
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(source));
    }

    public void testCompilerPluginSpecifiedWithOldVersion() throws Exception {
        TestFileUtils.writeFile(wd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version>"
                + "<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId><version>2.2</version></plugin></plugins></build>"
                + "</project>");
        FileObject source = TestFileUtils.writeFile(wd, "src/main/java/p/C.java", "package p; class C {}");
        assertEquals("1.3", SourceLevelQuery.getSourceLevel(source));
    }

    public void testCompilerPluginSpecifiedWithNewVersion() throws Exception {
        TestFileUtils.writeFile(wd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version>"
                + "<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId><version>2.3.1</version></plugin></plugins></build>"
                + "</project>");
        FileObject source = TestFileUtils.writeFile(wd, "src/main/java/p/C.java", "package p; class C {}");
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(source));
    }

    public void testCompilerPluginSpecifiedWithSourceLevel() throws Exception {
        TestFileUtils.writeFile(wd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version>"
                + "<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId><version>2.1</version>"
                + "<configuration><source>1.4</source></configuration></plugin></plugins></build>"
                + "</project>");
        FileObject source = TestFileUtils.writeFile(wd, "src/main/java/p/C.java", "package p; class C {}");
        assertEquals("1.4", SourceLevelQuery.getSourceLevel(source));
    }

    public void testSystemPropertySourceLevel() throws Exception { // #201938
        TestFileUtils.writeFile(wd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version>"
                + "<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId><version>2.1</version>"
                + "<configuration><source>${level}</source></configuration></plugin></plugins></build>"
                + "</project>");
        FileObject source = TestFileUtils.writeFile(wd, "src/main/java/p/C.java", "package p; class C {}");
        System.setProperty("level", "1.4"); //to late, also set in setup();
        assertEquals("1.4", SourceLevelQuery.getSourceLevel(source));
    }

    public void testUnrecognizedPackaging() throws Exception {
        TestFileUtils.writeFile(wd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>stuff</packaging><version>1.0</version></project>");
        FileObject source = TestFileUtils.writeFile(wd, "src/main/java/p/C.java", "package p; class C {}");
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(source));
    }

    public void testTestSourceLevel() throws Exception { // e.g. org.apache.felix.configadmin
        TestFileUtils.writeFile(wd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId><version>1.0</version>"
                + "<build><pluginManagement><plugin><artifactId>maven-compiler-plugin</artifactId><version>2.3.2</version><configuration><source>1.4</source></configuration></plugin></pluginManagement>"
                + "<plugins><plugin><artifactId>maven-compiler-plugin</artifactId><configuration><source>1.2</source></configuration>"
                + "<executions><execution><id>test-compile-java5</id><goals><goal>testCompile</goal></goals><configuration><source>1.6</source></configuration></execution></executions></plugin></plugins>"
                + "</build></project>");
        FileObject source = TestFileUtils.writeFile(wd, "src/test/java/p/C.java", "package p; class C {}");
        assertEquals("1.6", SourceLevelQuery.getSourceLevel(source));
    }

    public void testGeneratedSources() throws Exception { // #187595
        TestFileUtils.writeFile(wd,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>0</version>" +
                "<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId><version>2.1</version><executions>" +
                "<execution><id>comp-src</id><phase>compile</phase><goals><goal>compile</goal></goals><configuration><source>1.4</source></configuration></execution>" +
                "<execution><id>comp-tsrc</id><phase>test-compile</phase><goals><goal>testCompile</goal></goals><configuration><source>1.6</source></configuration></execution>" +
                "</executions></plugin></plugins></build>" +
                "</project>");
        FileObject src = FileUtil.createFolder(wd, "src/main/java");
        FileObject gsrc = FileUtil.createFolder(wd, "target/generated-sources/xjc");
        gsrc.createData("Whatever.class");
        FileObject tsrc = FileUtil.createFolder(wd, "src/test/java");
        FileObject gtsrc = FileUtil.createFolder(wd, "target/generated-test-sources/jaxb");
        gtsrc.createData("Whatever.class");
        assertEquals("1.4", SourceLevelQuery.getSourceLevel(src));
        assertEquals("1.4", SourceLevelQuery.getSourceLevel(gsrc));
        assertEquals("1.6", SourceLevelQuery.getSourceLevel(tsrc));
        assertEquals("1.6", SourceLevelQuery.getSourceLevel(gtsrc));
    }

    public void testChanges() throws Exception {
        TestFileUtils.writeFile(wd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version>"
                + "<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId><version>2.1</version>"
                + "<configuration><source>1.4</source></configuration></plugin></plugins></build>"
                + "</project>");
        FileObject source = TestFileUtils.writeFile(wd, "src/main/java/p/C.java", "package p; class C {}");
        SourceLevelQuery.Result r = SourceLevelQuery.getSourceLevel2(source);
        assertEquals("1.4", r.getSourceLevel());
        assertTrue(r.supportsChanges());
        MockChangeListener l = new MockChangeListener();
        r.addChangeListener(l);
        TestFileUtils.writeFile(wd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version>"
                + "<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId><version>2.1</version>"
                + "<configuration><source>1.6</source></configuration></plugin></plugins></build>"
                + "</project>");
        NbMavenProject.fireMavenProjectReload(ProjectManager.getDefault().findProject(wd));
        l.expectEvent(9999);
        assertEquals("1.6", r.getSourceLevel());
    }

    public void testPattern() throws Exception {
        assertTrue(MavenSourceLevelImpl.PROFILE.matcher("-profile compact1").find());
        assertTrue(MavenSourceLevelImpl.PROFILE.matcher("    -profile compact1    ").find());
        assertFalse(MavenSourceLevelImpl.PROFILE.matcher("-profile compact4").find());
        
        Matcher m = MavenSourceLevelImpl.PROFILE.matcher("-Xlint -profile compact1 -agentlib:jdwp=something");
        assertTrue(m.find());
        assertEquals("compact1", m.group(1));
    }

    public void testRelease() throws Exception { // #NETBEANS-353
        TestFileUtils.writeFile(wd,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>0</version>" +
                "<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId><version>3.6</version><executions>" +
                "<execution><id>comp-src</id><phase>compile</phase><goals><goal>compile</goal></goals><configuration><release>1.8</release></configuration></execution>" +
                "<execution><id>comp-tsrc</id><phase>test-compile</phase><goals><goal>testCompile</goal></goals><configuration><release>1.9</release></configuration></execution>" +
                "</executions></plugin></plugins></build>" +
                "</project>");
        FileObject src = FileUtil.createFolder(wd, "src/main/java");
        FileObject gsrc = FileUtil.createFolder(wd, "target/generated-sources/xjc");
        gsrc.createData("Whatever.class");
        FileObject tsrc = FileUtil.createFolder(wd, "src/test/java");
        FileObject gtsrc = FileUtil.createFolder(wd, "target/generated-test-sources/jaxb");
        gtsrc.createData("Whatever.class");
        assertEquals("1.8", SourceLevelQuery.getSourceLevel(src));
        assertEquals("1.8", SourceLevelQuery.getSourceLevel(gsrc));
        assertEquals("9", SourceLevelQuery.getSourceLevel(tsrc));
        assertEquals("9", SourceLevelQuery.getSourceLevel(gtsrc));
    }

}
