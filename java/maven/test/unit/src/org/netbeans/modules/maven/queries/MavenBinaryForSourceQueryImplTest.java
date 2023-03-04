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
