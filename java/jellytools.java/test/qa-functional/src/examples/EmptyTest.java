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
package examples;

import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;

/** It is just an example how test could look like. */
public class EmptyTest extends JellyTestCase {

    /** Constructor required by JUnit */
    public EmptyTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. */
    public static Test suite() {
        // run tests in particular order
        //return createModuleTest(EmptyTest.class, "test2", "test1");

        // run all tests 
        //return createModuleTest(EmptyTest.class);

        // run tests with specific configuration
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(EmptyTest.class).
                clusters(".*").
                enableModules(".*").
                failOnException(Level.INFO).
                failOnMessage(Level.SEVERE);
        conf = conf.addTest("test1");
        return NbModuleSuite.create(conf);
    }

    /** Method called before each test case. */
    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    /** Method called after each test case. */
    @Override
    public void tearDown() {
    }

    /** Test case 1. */
    public void test1() {
        System.out.println("test case 1");
    }

    /** Test case 2. */
    public void test2() {
        System.out.println("test case 2");
    }
}
