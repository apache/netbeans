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

package org.netbeans.modules.j2ee.sun.validation;

import junit.framework.*;


/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */


public class AllTests{
    /* A class implementation comment can go here. */

    public static Test suite() {

        TestSuite suite = new TestSuite("Tools Test Suite");            //NOI18N

        //
        // Add one entry for each test class
        // or test suite.
        //
        suite.addTestSuite(
            org.netbeans.modules.j2ee.sun.validation.util.BundleReaderTest.class);
        suite.addTestSuite(
            org.netbeans.modules.j2ee.sun.validation.util.DisplayTest.class);
        suite.addTestSuite(
            org.netbeans.modules.j2ee.sun.validation.util.UtilsTest.class);
        suite.addTestSuite(
            org.netbeans.modules.j2ee.sun.validation.constraints.ConstraintTest.class);
        ///suite.addTestSuite(
            ///org.netbeans.modules.j2ee.sun.validation.constraints.ConstraintUtilsTest.class);
        
        //
        // For a master test suite, use this pattern.
        // (Note that here, it's recursive!)
        //
        //suite.addTest(<ANOTHER_Test_Suite>.suite());
        
        return suite;
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
