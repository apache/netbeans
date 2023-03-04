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
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;
import org.openide.util.test.TestFileUtils;

public class MavenSharabilityQueryImplTest extends NbTestCase {

    public MavenSharabilityQueryImplTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testSharability() throws Exception {
        File prj = new File(getWorkDir(), "prj");
        TestFileUtils.writeFile(new File(prj, "pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version></project>");
        TestFileUtils.writeFile(new File(prj, "src/main/java/p/C.java"), "package p; class C {}");
        TestFileUtils.writeFile(new File(prj, "target/classes/java/p/C.class"), "whatever");
        TestFileUtils.writeFile(new File(prj, "modules/mod/pom.xml"), "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>mod</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version></project>");
        // XXX check nbactions*.xml
        /* Considered owned by NBM project, so skip this:
        assertSharability(SharabilityQuery.UNKNOWN, "");
         */
        assertSharability(SharabilityQuery.Sharability.MIXED, "prj");
        assertSharability(SharabilityQuery.Sharability.SHARABLE, "prj/src");
        assertSharability(SharabilityQuery.Sharability.SHARABLE, "prj/pom.xml");
        assertSharability(SharabilityQuery.Sharability.NOT_SHARABLE, "prj/target");
        assertSharability(SharabilityQuery.Sharability.UNKNOWN, "prj/stuff.xml");
        assertSharability(SharabilityQuery.Sharability.MIXED, "prj/modules");
        assertSharability(SharabilityQuery.Sharability.MIXED, "prj/modules/mod");
        assertSharability(SharabilityQuery.Sharability.SHARABLE, "prj/modules/mod/pom.xml");
        assertSharability(SharabilityQuery.Sharability.NOT_SHARABLE, "prj/modules/mod/target");
    }
    private void assertSharability(SharabilityQuery.Sharability expected, String path) throws Exception {
        assertEquals(path, expected, SharabilityQuery.getSharability(Utilities.toURI(new File(getWorkDir(), path))));
    }

}
