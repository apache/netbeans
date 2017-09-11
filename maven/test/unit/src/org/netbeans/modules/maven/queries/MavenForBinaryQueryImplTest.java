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

package org.netbeans.modules.maven.queries;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.spi.queries.ForeignClassBundler;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Utilities;

public class MavenForBinaryQueryImplTest extends NbTestCase {

    public MavenForBinaryQueryImplTest(String n) {
        super(n);
    }

    private FileObject d;
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
        //synchronous reload of maven project asserts sanoty in some tests..
        System.setProperty("test.reload.sync", "true");

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
        assertEquals(Arrays.asList(src, gsrc), Arrays.asList(SourceForBinaryQuery.findSourceRoots(new URL(d.toURL(), "target/classes/")).getRoots()));
        assertEquals(Arrays.asList(tsrc, gtsrc), Arrays.asList(SourceForBinaryQuery.findSourceRoots(new URL(d.toURL(), "target/test-classes/")).getRoots()));
    }

    public void testJarify() throws Exception {
        assertEquals("org/jvnet/hudson/plugins/analysis-core/1.24/analysis-core-1.24.jar", MavenForBinaryQueryImpl.jarify("org/jvnet/hudson/plugins/analysis-core/1.24/analysis-core-1.24.jar"));
        assertEquals("org/jvnet/hudson/plugins/analysis-core/1.24/analysis-core-1.24.jar", MavenForBinaryQueryImpl.jarify("org/jvnet/hudson/plugins/analysis-core/1.24/analysis-core-1.24.hpi"));
        assertEquals(null, MavenForBinaryQueryImpl.jarify(null)); // #202079
    }

    @RandomlyFails // NB-Core-Build #9101, #9777, #9985, #9990, #10007
    public void testForeignClassBundler() throws Exception { // #155091 and deps
        TestFileUtils.writeFile(d,
                "a/pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>0</version>" +
                "</project>");
        FileObject src = FileUtil.createFolder(d, "a/src/main/java");
        
        
        File repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
        File art = new File(repo, "grp" + File.separator + "art" + File.separator + "0" + File.separator +  "art-0.jar");
        URL root = FileUtil.getArchiveRoot(Utilities.toURI(art).toURL());
        Project p = ProjectManager.getDefault().findProject(d.getFileObject("a"));
        FileOwnerQuery.markExternalOwner(Utilities.toURI(art), p, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        MavenFileOwnerQueryImpl.getInstance().registerCoordinates("grp", "art", "0", d.getFileObject("a").toURL(), true);
        
        
        SourceForBinaryQuery.Result2 r = SourceForBinaryQuery.findSourceRoots2(root);
        assertEquals(Collections.singletonList(src), Arrays.asList(r.getRoots()));
        assertTrue(r.preferSources());
        TestFileUtils.writeFile(d,
                "b/pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>war</packaging>" +
                "<version>0</version>" +
                "</project>");
        src = FileUtil.createFolder(d, "b/src/main/java");
        
        art = new File(repo, "grp" + File.separator + "art" + File.separator + "0" + File.separator +  "art-0.jar");
        FileOwnerQuery.markExternalOwner(Utilities.toURI(art), ProjectManager.getDefault().findProject(d.getFileObject("b")), FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        root = FileUtil.getArchiveRoot(Utilities.toURI(art).toURL());
        p = ProjectManager.getDefault().findProject(d.getFileObject("b"));
        FileOwnerQuery.markExternalOwner(Utilities.toURI(art), p, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        final Object LOCK = new Object();
        final Boolean[] fired = new Boolean[] {Boolean.FALSE};
        r.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                synchronized (LOCK) {
                    fired[0] = Boolean.TRUE;
                    LOCK.notifyAll();
                }
            }
        });
        MavenFileOwnerQueryImpl.getInstance().registerCoordinates("grp", "art", "0", d.getFileObject("b").toURL(), true);
        synchronized (LOCK) {
            if (!fired[0]) {
                LOCK.wait(20000);
            }
        }
        r = SourceForBinaryQuery.findSourceRoots2(root);
        assertEquals(Collections.singletonList(src), Arrays.asList(r.getRoots()));
        assertFalse(r.preferSources());
        
        //215242 assert that output dir does prefer sources
        r = SourceForBinaryQuery.findSourceRoots2(new URL(d.toURL(), "b/target/classes/"));
        assertEquals(Collections.singletonList(src), Arrays.asList(r.getRoots()));
        assertTrue(r.preferSources());
    }

    @ProjectServiceProvider(service=ForeignClassBundler.class, projectType="org-netbeans-modules-maven/war")
    public static class ForeignClassBundlerMock implements ForeignClassBundler {
        @Override public boolean preferSources() {
            return false;
        }

        @Override
        public void resetCachedValue() {
        }
    }

}
