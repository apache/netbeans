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

import junit.extensions.TestDecorator;
import java.io.*;
import org.netbeans.junit.diff.*;

/**
 * A Decorator for Tests. Use TestDecorator as the base class
 * for defining new test decorators. Test decorator subclasses
 * can be introduced to add behaviour before or after a test
 * is run.
 *
 */

import junit.framework.*;

/** NetBeans extension to JUnit's TestDecorator class. Tests created
 * with the help of this class can use method assertFile and can be
 * filtered.
 */
public class NbTestDecorator extends TestDecorator implements NbTest {
	
    /**
     * @param test junit test instance
     */
	public NbTestDecorator(Test test) {
		super(test);
	}

        /**
         * Sets active filter.
         * @param filter Filter to be set as active for current test, null will reset filtering.
         */
        public void setFilter(Filter filter) {
            //System.out.println("NbTestDecorator.setFilter()");
            if (fTest instanceof NbTest) {
                //System.out.println("NbTestDecorator.setFilter(): test="+fTest+" filter="+filter);
                ((NbTest)fTest).setFilter(filter);
            }
        }
        
        /**
         * Returns expected fail message.
         * @return expected fail message if it's expected this test fail, null otherwise.
         */
        public String getExpectedFail() {
            if (fTest instanceof NbTest) {
                return ((NbTest)fTest).getExpectedFail();
            } else {
                return null;
            }
        }
        
        /**
         * Checks if a test isn't filtered out by the active filter.
         */
        public boolean canRun() {
            //System.out.println("NbTestDecorator.canRun()");
            if (fTest instanceof NbTest) {
                //System.out.println("fTest is NbTest");
                return ((NbTest)fTest).canRun();
            } else {
                // standard JUnit tests can always run
                return true;
            }
        }        

        
        
        // additional asserts !!!!
        // these methods only wraps functionality from asserts from NbTestCase,
        // because java does not have multiinheritance :-)
        // please see more documentatino in this file.
        
        
    /**
     * for description, see this method in NbTestCase class.
     *
     * @param message the detail message for this assertion
     * @param test first file to be compared, by the convention this should be
     * the test-generated file
     * @param pass second file to be compared, it should be so called 'golden'
     * file, which defines the correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences
     * will not be stored. In case it points to directory the result file name
     * is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file
     * (without extension and path) appended by the '.diff'.
     * @param externalDiff instance of class implementing the
     * {@link org.netbeans.junit.diff.Diff} interface, it has to be already
     * initialized, when passed in this assertFile function.
     */
        public static void assertFile(String message, String test, String pass, String diff, Diff externalDiff) {
            NbTestCase.assertFile(message,test,pass,diff,externalDiff);
        }
        
    /**
     * for description, see this method in NbTestCase class.
     *
     * @param test first file to be compared, by the convention this should be
     * the test-generated file
     * @param pass second file to be compared, it should be so called 'golden'
     * file, which defines the correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences
     * will not be stored. In case it points to directory the result file name
     * is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file
     * (without extension and path) appended by the '.diff'.
     * @param externalDiff instance of class implementing the
     * {@link org.netbeans.junit.diff.Diff} interface, it has to be already
     * initialized, when passed in this assertFile function.
     */
        public static void assertFile(String test, String pass, String diff, Diff externalDiff) {
            NbTestCase.assertFile(test, pass, diff, externalDiff);
        }
        
    /**
     * for description, see this method in NbTestCase class.
     *
     * @param message the detail message for this assertion
     * @param test first file to be compared, by the convention this should be
     * the test-generated file
     * @param pass second file to be compared, it should be so called 'golden'
     * file, which defines the correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences
     * will not be stored. In case it points to directory the result file name
     * is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file
     * (without extension and path) appended by the '.diff'.
     */
        public static void assertFile(String message, String test, String pass, String diff) {
            NbTestCase.assertFile(message, test, pass, diff);
        }
        
    /**
     * for description, see this method in NbTestCase class.
     *
     * @param test first file to be compared, by the convention this should be
     * the test-generated file
     * @param pass second file to be compared, it should be so called 'golden'
     * file, which defines the correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences
     * will not be stored. In case it points to directory the result file name
     * is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file
     * (without extension and path) appended by the '.diff'.
     */
        public static void assertFile(String test, String pass, String diff) {
            NbTestCase.assertFile(test, pass, diff);
        }
        
    /**
     * for description, see this method in NbTestCase class.
     *
     * @param test first file to be compared, by the convention this should be
     * the test-generated file
     * @param pass second file to be compared, it should be so called 'golden'
     * file, which defines the correct content for the test-generated file.
     */
        public static void assertFile(String test, String pass) {
            NbTestCase.assertFile(test, pass);
        }
        
        
    /**
     * for description, see this method in NbTestCase class.
     *
     * @param message the detail message for this assertion
     * @param test first file to be compared, by the convention this should be
     * the test-generated file
     * @param pass second file to be compared, it should be so called 'golden'
     * file, which defines the correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences
     * will not be stored. In case it points to directory the result file name
     * is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file
     * (without extension and path) appended by the '.diff'.
     * @param externalDiff instance of class implementing the
     * {@link org.netbeans.junit.diff.Diff} interface, it has to be already
     * initialized, when passed in this assertFile function.
     */
        public static void assertFile(String message, File test, File pass, File diff, Diff externalDiff) {
            NbTestCase.assertFile(message,test,pass,diff,externalDiff);
        }
        
        
    /**
     * for description, see this method in NbTestCase class.
     *
     * @param test first file to be compared, by the convention this should be
     * the test-generated file
     * @param pass second file to be compared, it should be so called 'golden'
     * file, which defines the correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences
     * will not be stored. In case it points to directory the result file name
     * is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file
     * (without extension and path) appended by the '.diff'.
     * @param externalDiff instance of class implementing the
     * {@link org.netbeans.junit.diff.Diff} interface, it has to be already
     * initialized, when passed in this assertFile function.
     */
        public static void assertFile(File test, File pass, File diff, Diff externalDiff) {
            NbTestCase.assertFile(test, pass, diff, externalDiff);
        }
        
        
    /**
     * for description, see this method in NbTestCase class.
     *
     * @param message the detail message for this assertion
     * @param test first file to be compared, by the convention this should be
     * the test-generated file
     * @param pass second file to be compared, it should be so called 'golden'
     * file, which defines the correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences
     * will not be stored. In case it points to directory the result file name
     * is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file
     * (without extension and path) appended by the '.diff'.
     */
        public static void assertFile(String message, File test, File pass, File diff) {
            NbTestCase.assertFile(message, test, pass, diff);
        }
        
    /**
     * for description, see this method in NbTestCase class.
     *
     * @param test first file to be compared, by the convention this should be
     * the test-generated file
     * @param pass second file to be compared, it should be so called 'golden'
     * file, which defines the correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences
     * will not be stored. In case it points to directory the result file name
     * is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file
     * (without extension and path) appended by the '.diff'.
     */
        public static void assertFile(File test, File pass, File diff) {
            NbTestCase.assertFile(test,pass,diff);
        }
        
    /**
     * for description, see this method in NbTestCase class.
     *
     * @param test first file to be compared, by the convention this should be
     * the test-generated file
     * @param pass second file to be compared, it should be so called 'golden'
     * file, which defines the correct content for the test-generated file.
     */
        public static void assertFile(File test, File pass) {
            NbTestCase.assertFile(test, pass);
        }
        
        
}
