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

package org.netbeans.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.runner.BaseTestRunner;
import junit.textui.TestRunner;

public class RandomlyFailsTest extends TestCase {

    private static List<Integer> runs;

    public RandomlyFailsTest(String name) {
        super(name);
    }

    public static class One extends NbTestCase {
        public One(String n) {super(n);}
        public void testReliable() {runs.add(10);}
        @RandomlyFails public void testUnreliable() {runs.add(11);}
    }

    public static class Two extends NbTestCase {
        public Two(String n) {super(n);}
        public void testReliable() {runs.add(20);}
    }

    @RandomlyFails public static class Three extends NbTestCase {
        public Three(String n) {super(n);}
        public void testUnreliable() {runs.add(30);}
    }

    public static class Four extends NbTestCase {
        public Four(String n) {super(n);}
        public void testReliable() {runs.add(40);}
        @RandomlyFails public void testUnreliable() {runs.add(41);}
    }

    @RandomlyFails public static class Five extends NbTestCase {
        public Five(String n) {super(n);}
        public void testUnreliable() {runs.add(50);}
    }

    public static class Six extends TestCase {
        public static Test suite() {return new NbTestSuite(Four.class);}
    }

    public static class Seven extends TestCase {
        public static Test suite() {return new NbTestSuite(Five.class);}
    }

    public static class Eight extends TestCase {
        public static Test suite() {
            NbTestSuite suite = new NbTestSuite();
            suite.addTestSuite(Four.class);
            suite.addTestSuite(Five.class);
            return suite;
        }
    }

    private void run(Class... tests) {
        runs = new ArrayList<>();
        BaseTestRunner runner = new TestRunner();
        for (Class test : tests) {
            TestResult result = new TestResult();
            //result.addListener(new ResultPrinter(System.err));
            runner.getTest(test.getName()).run(result);
            assertEquals("failures in " + test, Collections.emptyList(), Collections.list((Enumeration<?>) result.failures()));
            assertEquals("errors in " + test, Collections.emptyList(), Collections.list((Enumeration<?>) result.errors()));
        }
        Collections.sort(runs);
    }

    private void runAll() throws Exception {
        run(One.class, Two.class, Three.class, Six.class, Seven.class, Eight.class);
    }

    public void testRegularMode() throws Exception {
        System.setProperty("ignore.random.failures", "false");
        runAll();
        assertEquals(Arrays.asList(10, 11, 20, 30, 40, 40, 41, 41, 50, 50), runs);
    }

    public void testIgnoreRandomFailuresMode() throws Exception {
        System.setProperty("ignore.random.failures", "true");
        runAll();
        assertEquals(Arrays.asList(10, 20, 40, 40), runs);
    }

    @RandomlyFails public static class Sub1 extends One {
        public Sub1(String n) {super(n);}
    }
    @RandomlyFails public static class Sub2 extends Two {
        public Sub2(String n) {super(n);}
    }
    public static class Sub3 extends Three {
        public Sub3(String n) {super(n);}
    }

    public void testHeritability() throws Exception {
        System.setProperty("ignore.random.failures", "false");
        run(Sub1.class, Sub2.class, Sub3.class);
        assertEquals(Arrays.asList(10, 11, 20, 30), runs);
        System.setProperty("ignore.random.failures", "true");
        run(Sub1.class, Sub2.class, Sub3.class);
        assertEquals(Collections.emptyList(), runs);
    }

}
