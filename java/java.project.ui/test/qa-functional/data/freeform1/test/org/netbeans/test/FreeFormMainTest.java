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

/*
 * FreeFormMainTest.java
 * JUnit based test
 *
 * Created on June 14, 2004, 3:58 PM
 */

package org.netbeans.test;
import junit.framework.*;


import junit.framework.*;

/**
 *
 * @author mkubec
 */
public class FreeFormMainTest extends TestCase {

    public FreeFormMainTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(FreeFormMainTest.class);
        return suite;
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    /**
     * Test of getString method, of class org.netbeans.test.FreeFormMain.
     */
    public void testGetString() {
        System.out.println("testGetString");
        assertEquals("Ahoj", FreeFormMain.getString());
    }

}
