/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.queries;

import java.util.regex.Matcher;
import static junit.framework.Assert.assertTrue;
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
}
