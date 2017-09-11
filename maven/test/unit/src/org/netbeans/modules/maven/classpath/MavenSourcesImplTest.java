/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
