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

package org.netbeans.modules.python.platform;

import org.netbeans.modules.python.api.PythonAutoDetector;
import java.io.File;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PythonAutoDetectorTest {

    public PythonAutoDetectorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of traverse method, of class PythonAutoDetector.
     */
    @Test
    public void testTraverseBin() {
        System.out.println("traverse lib");
        File dir = new File("/usr/bin");
        PythonAutoDetector instance = new PythonAutoDetector();
        instance.traverse(dir, false);
        // TODO review the generated test code and remove the default call to fail.
        assertTrue(instance.getMatches().size() > 0);
        System.out.println("OS Name: " + System.getProperty("os.name"));
        ArrayList<String> result = instance.getMatches();
        assertNotNull(result);
        for (String name : result)
            System.out.println("Matches: " + name);
    }
//     @Test
//    public void testTraverseLib() {
//        System.out.println("traverse lib");
//        File dir = new File("/usr/lib");
//        PythonAutoDetector instance = new PythonAutoDetector();
//        instance.traverse(dir, false);
//        // TODO review the generated test code and remove the default call to fail.
//        assertTrue(instance.getMatches().size() > 0);
//        System.out.println("OS Name: " + System.getProperty("os.name"));
//        ArrayList<String> result = instance.getMatches();
//        assertNotNull(result);
//        for (String name : result)
//            System.out.println("Matches: " + name);
//    }
   

}
