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

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.queries.MavenFileOwnerQueryImpl;
import org.openide.util.Utilities;
import org.openide.util.test.TestFileUtils;
import org.eclipse.aether.artifact.DefaultArtifact;

public class NbArtifactFixerTest extends NbTestCase {

    public NbArtifactFixerTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testResolve() throws Exception {
        File pom = TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project xmlns='http://maven.apache.org/POM/4.0.0'><modelVersion>4.0.0</modelVersion>" +
            "<groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
            "</project>");
        MavenFileOwnerQueryImpl.getInstance().registerCoordinates("g", "a", "0", Utilities.toURI(getWorkDir()).toURL(), true);
        assertEquals(pom, new NbArtifactFixer().resolve(new DefaultArtifact("g:a:pom:0")));
        assertEquals(null, new NbArtifactFixer().resolve(new DefaultArtifact("g:a:jar:0")));
        File fallback = new NbArtifactFixer().resolve(new DefaultArtifact("g:a:pom:1"));
        assertNotNull(fallback);
        assertFalse(fallback.equals(pom));
        assertEquals(null, new NbArtifactFixer().resolve(new DefaultArtifact("g:a:pom:stuff:0")));
    }

}
