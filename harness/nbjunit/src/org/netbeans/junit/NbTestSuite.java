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

import java.lang.reflect.Constructor;
import java.util.*;
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

/**
 * NetBeans extension to JUnit's TestSuite class.
 */
public class NbTestSuite extends TestSuite implements NbTest {


    private Filter fFilter;

    static boolean ignoreRandomFailures() {
        return Boolean.getBoolean("ignore.random.failures");
    }

    /**
     * Constructs an empty TestSuite.
     */
    public NbTestSuite() {
        super();
    }

    /**
     * Constructs a TestSuite from the given class. Adds all the methods
     * starting with "test" as test cases to the suite.
     * @param theClass the class to create tests for (from methods starting with test)
     *
     */
    public NbTestSuite(Class<? extends TestCase> theClass) {       
        super(testCaseClassOrDummy(theClass));
    }
    private static Class<? extends TestCase> testCaseClassOrDummy(Class<? extends TestCase> testClass) {
        if (ignoreRandomFailures() && testClass.isAnnotationPresent(RandomlyFails.class)) {
            System.err.println("Skipping " + testClass.getName());
            return APIJail.Dummy.class;
        } else {
            return testClass;
        }
    }
    private static class APIJail { // prevents Dummy from appearing as a public member
        public static class Dummy extends TestCase {
            public Dummy(String name) {
                super(name);
            }
            public void testNothing() {}
        }
    }

    /**
     * Constructs an empty TestSuite.
     * @param name name of the test suite
     */
    public NbTestSuite(String name) {
        super(name);
    }
    
    void addTests(Class<? extends TestCase> clazz, String... names) throws Exception {
        Constructor cnt = clazz.getConstructor(String.class);
        for (String n : names) {
            Test t = (Test)cnt.newInstance(n);
            addTest(t);
        }
    }
    
    
    /**
     * Adds a test to the suite.
     */
    @Override
    public void addTest(Test test) {
        if (test instanceof NbTest) {
            //System.out.println("NbTestSuite.addTest(): Adding test with filter, test:"+test);
            ((NbTest)test).setFilter(fFilter);
        } else {
            //System.out.println("NbTestSuite.addTest(): Adding test, test:"+test);
        }
        super.addTest(test);
    }

    
    /**
     * adds a test suite to this test suite
     */
    @Override
    public void addTestSuite(Class<? extends TestCase> testClass) {
        if (ignoreRandomFailures() && testClass.isAnnotationPresent(RandomlyFails.class)) {
            System.err.println("Skipping " + testClass.getName());
            return;
        }
        NbTest t = new NbTestSuite(testClass);
        t.setFilter(fFilter);
        addTest(t);
    }

            /**
         * Sets active filter.
         * @param filter Filter to be set as active for current test, null will reset filtering.
         */
        public void setFilter(Filter filter) {
                Enumeration e;

                this.fFilter = filter;
                e = this.tests();
                while(e.hasMoreElements()) {
                    Object test = e.nextElement();
                    if (test instanceof NbTest) {
                        //System.out.println("NbTestSuite.setFilter(): Setting filter:"+filter);
                        ((NbTest)test).setFilter(filter);
                    }
                      
                }
        }
        /**
         * Checks if a test isn't filtered out by the active filter.
         */
        public boolean canRun() {
                return true; // suite can always run
        }
        
        public String getExpectedFail() {
            return null;
        }
        
        
    /** Factory method to create a special execution suite that not only
     * executes the tests but also measures the times each execution took.
     * It then compares the times and fails if the difference is too big.
     * Test tests can be executed more times to eliminate the effect
     * of GC and hotspot compiler.
     *
     * @param clazz the class to create tests for (from methods starting with test)
     * @param slowness this must be true: slowness * min &lt; max
     * @param repeat number of times to repeat the test
     * @return testsuite
     */
    public static NbTestSuite speedSuite (Class<? extends TestCase> clazz, int slowness, int repeat) {
        if (ignoreRandomFailures()) {
            System.err.println("Skipping " + clazz.getName());
            return new NbTestSuite("skipping");
        }
        return new SpeedSuite (clazz, repeat, slowness, SpeedSuite.CONSTANT);
    }
    
    /** Factory method to create a special execution suite that not only
     * executes the tests but also measures the times each execution took.
     * It then compares the times devided by the size of query 
     * and fails if the difference is too big.
     * Test tests can be executed more times to eliminate the effect
     * of GC and hotspot compiler.
     *
     * @param clazz the class to create tests for (from methods starting with test)
     * @param slowness this must be true: slowness * min &lt; max
     * @param repeat number of times to repeat the test
     * @return testsuite
     */
    public static NbTestSuite linearSpeedSuite (Class<? extends TestCase> clazz, int slowness, int repeat) {
        if (ignoreRandomFailures()) {
            System.err.println("Skipping " + clazz.getName());
            return new NbTestSuite("skipping");
        }
        return new SpeedSuite (clazz, repeat, slowness, SpeedSuite.LINEAR);
    }

    
    /** Allows enhanced execution and comparition of speed of a set of 
     * tests.
     */
    private static final class SpeedSuite extends NbTestSuite {
        public static final int CONSTANT = 0;
        public static final int LINEAR = 1;
        
        /** number of repeats to try, if there is a failure */
        private int repeat;
        /** the maximum difference between the slowest and fastest test */
        private int slowness;
        /** type of query CONSTANT, LINEAR, etc. */
        private int type;
        
        public SpeedSuite (Class<? extends TestCase> clazz, int repeat, int slowness, int type) {
            super (clazz);
            this.repeat = repeat;
            this.slowness = slowness;
            this.type = type;
        }
        
        @Override
        public void run (junit.framework.TestResult result) {
            StringBuffer error = new StringBuffer ();
            for (int i = 0; i < repeat; i++) {
                super.run(result);
                
                error.setLength (0);
                
                if (!result.wasSuccessful ()) {
                    // if there was a failure, end the test
                    return;
                }
                
                {
                    Enumeration en = tests ();
                    while (en.hasMoreElements ()) {
                        Object t = en.nextElement ();
                        if (t instanceof NbTestCase) {
                            NbTestCase test = (NbTestCase)t;
                            error.append ("Test "); error.append (test.getName ());
                            error.append(" took "); error.append(test.getExecutionTime() / 1000000);
                            error.append (" ms\n");
                        } else {
                            error.append ("Test "); error.append (t); 
                            error.append (" is not NbTestCase");
                        }
                         
                    }
                }

                double min = Long.MAX_VALUE;
                double max = Long.MIN_VALUE;

                {
                    Enumeration en = tests ();
                    while (en.hasMoreElements ()) {
                        Object t = en.nextElement ();
                        if (t instanceof NbTestCase) {
                            double l = ((NbTestCase)t).getExecutionTime ();
                            
                            if (type == LINEAR) {
                                l = l / ((NbTestCase)t).getTestNumber ();
                            }
                            
                            if (l > max) max = l;
                            if (l < min) min = l;
                        }
                    }
                }

                System.err.println(error.toString ());

                if (max <= min * slowness) {
                    // ok
                    return;
                }
            }
            
            result.addFailure (this, new junit.framework.AssertionFailedError (
                "Execution times of tests differ too much;\n" +
                "the results are supposed to be " + typeName () + ":\n"
                + error.toString ()
            ));
        }
        
        private String typeName () {
            switch (type) {
                case CONSTANT: return "constant"; 
                case LINEAR: return "linear";
                default: NbTestCase.fail ("This is not supported type: " + type);
            }
            return null;
        }
    }        
}
