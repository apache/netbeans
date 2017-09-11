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
import java.util.logging.Level;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class MavenBinaryForSourceQueryImplTest extends NbTestCase {

    public MavenBinaryForSourceQueryImplTest(String name) {
        super(name);
    }

    private FileObject d;
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
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
        File repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
        File art0 = new File(repo, "grp/art/0/art-0.jar");
        URL url0 = FileUtil.getArchiveRoot(art0.toURI().toURL());        
        
        assertEquals(Arrays.asList(new URL(d.toURL(), "target/classes/"), url0), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(src.toURL()).getRoots()));
        assertEquals(Arrays.asList(new URL(d.toURL(), "target/classes/"), url0), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(gsrc.toURL()).getRoots()));
        assertEquals(Arrays.asList(new URL(d.toURL(), "target/test-classes/"), url0), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(tsrc.toURL()).getRoots()));
        assertEquals(Arrays.asList(new URL(d.toURL(), "target/test-classes/"), url0), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(gtsrc.toURL()).getRoots()));
    }

    public void testResources() throws Exception { // #208816
        TestFileUtils.writeFile(d,
                "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>art</artifactId>" +
                "<packaging>jar</packaging>" +
                "<version>0</version>" +
                "</project>");
        FileObject res = FileUtil.createFolder(d, "src/main/resources");
        FileObject tres = FileUtil.createFolder(d, "src/test/resources");
        CharSequence log = Log.enable(BinaryForSourceQuery.class.getName(), Level.FINE);
        File repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
        File art0 = new File(repo, "grp/art/0/art-0.jar");
        URL url0 = FileUtil.getArchiveRoot(art0.toURI().toURL()); 

        assertEquals(Arrays.asList(new URL(d.toURL(), "target/classes/"), url0), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(res.toURL()).getRoots()));
        assertEquals(Arrays.asList(new URL(d.toURL(), "target/test-classes/"), url0), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(tres.toURL()).getRoots()));
        String logS = log.toString();
        assertFalse(logS, logS.contains("-> nil"));
        assertTrue(logS, logS.contains("ProjectBinaryForSourceQuery"));
    }

}
