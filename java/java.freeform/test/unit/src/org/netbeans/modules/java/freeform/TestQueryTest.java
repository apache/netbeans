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

package org.netbeans.modules.java.freeform;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.modules.ant.freeform.TestBase;
import org.openide.filesystems.FileObject;

/**
 * Check that the unit test query works.
 * @author Jesse Glick
 */
public class TestQueryTest extends TestBase {

    public TestQueryTest(String name) {
        super(name);
    }

    private FileObject src1, src1a, src2, test1, test2;

    protected void setUp() throws Exception {
        super.setUp();
        src1 = simple2.getProjectDirectory().getFileObject("src1");
        assertNotNull("have src1", src1);
        src1a = simple2.getProjectDirectory().getFileObject("src1a");
        assertNotNull("have src1a", src1a);
        src2 = simple2.getProjectDirectory().getFileObject("src2");
        assertNotNull("have src2", src2);
        test1 = simple2.getProjectDirectory().getFileObject("test1");
        assertNotNull("have test1", test1);
        test2 = simple2.getProjectDirectory().getFileObject("test2");
        assertNotNull("have test2", test2);
    }
    
    public void testFindUnitTests() throws Exception {
        URL[] tests = new URL[] {
            test1.getURL(),
            test2.getURL(),
        };
        assertEquals("correct tests for src1", Arrays.asList(tests), Arrays.asList(UnitTestForSourceQuery.findUnitTests(src1)));
        assertEquals("correct tests for src1a", Arrays.asList(tests), Arrays.asList(UnitTestForSourceQuery.findUnitTests(src1a)));
        assertEquals("correct tests for src2", Arrays.asList(tests), Arrays.asList(UnitTestForSourceQuery.findUnitTests(src2)));
        assertEquals("no tests for test1", Collections.EMPTY_LIST, Arrays.asList(UnitTestForSourceQuery.findUnitTests(test1)));
    }
    
    public void testFindSources() throws Exception {
        URL[] sources = new URL[] {
            src1.getURL(),
            src1a.getURL(),
            src2.getURL(),
        };
        assertEquals("correct sources for test1", Arrays.asList(sources), Arrays.asList(UnitTestForSourceQuery.findSources(test1)));
        assertEquals("correct sources for test2", Arrays.asList(sources), Arrays.asList(UnitTestForSourceQuery.findSources(test2)));
        assertEquals("no sources for src1", Collections.EMPTY_LIST, Arrays.asList(UnitTestForSourceQuery.findSources(src1)));
    }

}
