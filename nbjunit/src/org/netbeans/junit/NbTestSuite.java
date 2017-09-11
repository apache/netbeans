/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
     * @param slowness this must be true: slowness * min < max
     * @param repeat number of times to repeat the test
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
