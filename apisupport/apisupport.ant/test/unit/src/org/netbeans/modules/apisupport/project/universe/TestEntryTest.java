/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.util.Utilities;

/**
 * @author pzajac
 */
public class TestEntryTest extends TestBase {
    
    public TestEntryTest(String testName) {
        super(testName);
    }
    
    public void testGetSourcesNbOrgModule() throws IOException {
        File test = new File(nbRootFile(),"nbbuild/build/testdist/unit/" + CLUSTER_IDE + "/org-netbeans-modules-apisupport-project/tests.jar"); // NOI18N
        TestEntry entry = TestEntry.get(test);
        assertNotNull("TestEntry for aisupport/project tests",entry);
        assertNotNull("Nbroot wasn't found.", entry.getNBRoot());
        URL srcDir = entry.getSrcDir();
        assertEquals(Utilities.toURI(new File(nbRootFile(), "apisupport/apisupport.project/test/unit/src")).toURL(), srcDir);
    }
    
    public void testGetSourcesFromExternalModule() throws IOException {
        File test = resolveEEPFile("/suite4/build/testdist/unit/cluster/module1/tests.jar");
        TestEntry entry = TestEntry.get(test);
        assertNotNull("TestEntry for suite tests",entry);
        assertNull("Nbroot was found.", entry.getNBRoot());
        URL srcDir = entry.getSrcDir();
        assertEquals(Utilities.toURI(resolveEEPFile("/suite4/module1/test/unit/src")).toURL().toExternalForm(),srcDir.toExternalForm());
    }

    public void testNullsOnShortUNCPath144758() throws IOException {
        if (!Utilities.isWindows()) {
            return;
        }
        TestEntry entry = TestEntry.get(new File("\\\\server\\shared\\tests.jar"));
        assertNotNull(entry);
        assertNull(entry.getTestDistRoot());
        assertNull(entry.getSrcDir());
        assertNull(entry.getNetBeansOrgPath());
        assertNull(entry.getNBRoot());
    }

}
