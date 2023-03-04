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
package org.netbeans.api.java.source.gen;

import java.util.Arrays;
import java.util.Comparator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.save.ListMatcher;
import org.netbeans.modules.java.source.save.Measure;

/**
 * Test ListMatcher.
 * 
 * @author Pavel Flaska
 */
public class ListMatcherTest extends NbTestCase {
    
    /** Creates a new instance of ListMatcherTest */
    public ListMatcherTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ListMatcherTest.class);
        return suite;
    }
    
    public void testAddToEmpty() {
        String[] oldL = { };
        String[] newL = { "A", "B", "C" };
        String golden = 
                "{insert} A\n" +
                "{insert} B\n" +
                "{insert} C\n";
        ListMatcher<String> matcher = ListMatcher.<String>instance(oldL, newL);
        if (matcher.match()) {
            String result = matcher.printResult(false);
            System.err.println("-------------");
            System.err.println("testAddToEmpty");
            System.err.println("-------------");
            System.err.println(result);
            assertEquals(golden, result);
        } else {
            assertTrue("No match!", false);
        }
    }

    public void testRemoveAll() {
        String[] oldL= { "A", "B", "C" };
        String[] newL = { };
        String golden = 
                "{delete} A\n" +
                "{delete} B\n" +
                "{delete} C\n";
        ListMatcher<String> matcher = ListMatcher.<String>instance(oldL, newL);
        if (matcher.match()) {
            String result = matcher.printResult(false);
            System.err.println("-------------");
            System.err.println("testRemoveAll");
            System.err.println("-------------");
            System.err.println(result);
            assertEquals(golden, result);
        } else {
            assertTrue("No match!", false);
        }
    }
    
    public void testAddToIndex0() {
        String[] oldL= { "B" };
        String[] newL = { "A", "B" };
        String golden = 
                "{insert} A\n" +
                "{nochange} B\n";
        ListMatcher<String> matcher = ListMatcher.<String>instance(oldL, newL);
        if (matcher.match()) {
            String result = matcher.printResult(false);
            System.err.println("---------------");
            System.err.println("testAddToIndex0");
            System.err.println("---------------");
            System.err.println(result);
            assertEquals(golden, result);
        } else {
            assertTrue("No match!", false);
        }
    }
    
    public void testRemoveAtIndex0() {
        String[] oldL = { "A", "B" };
        String[] newL = { "B" };
        String golden = 
                "{delete} A\n" +
                "{nochange} B\n";
        ListMatcher<String> matcher = ListMatcher.<String>instance(oldL, newL);
        if (matcher.match()) {
            String result = matcher.printResult(false);
            System.err.println("------------------");
            System.err.println("testRemoveAtIndex0");
            System.err.println("------------------");
            System.err.println(result);
            assertEquals(golden, result);
        } else {
            assertTrue("No match!", false);
        }
    }
    
    public void testComplex() {
        String[] oldL = { "A", "B", "C", "D", "E", "F", "G" };
        String[] newL = { "B", "C", "C1", "D", "E", "G", "H" };
        String golden = 
                "{delete} A\n" +
                "{nochange} B\n" +
                "{nochange} C\n" +
                "{insert} C1\n" +
                "{nochange} D\n" +
                "{nochange} E\n" +
                "{delete} F\n" +
                "{nochange} G\n" +
                "{insert} H\n";
        ListMatcher<String> matcher = ListMatcher.<String>instance(oldL, newL);
        if (matcher.match()) {
            String result = matcher.printResult(false);
            System.err.println("-----------");
            System.err.println("testComplex");
            System.err.println("-----------");
            System.err.println(result);
            assertEquals(golden, result);
        } else {
            assertTrue("No match!", false);
        }
    }

    public void testSimilar1() {
        String[] oldL = {       "A" };
        String[] newL = { "A1", "A" };
        String golden =
                "{insert} A1\n" +
                "{nochange} A\n";
        ListMatcher<String> matcher = ListMatcher.<String>instance(Arrays.asList(oldL), Arrays.asList(newL), new Comparator<String>(){
            @Override
            public int compare(String o1, String o2) {
                if (o1.equals(o2)) return Measure.OBJECTS_MATCH;
                if (o1.startsWith(o2) || o2.startsWith(o1)) return Measure.ALMOST_THE_SAME;
                return Measure.INFINITE_DISTANCE;
            }
        });
        if (matcher.match()) {
            String result = matcher.printResult(false);
            System.err.println("-----------");
            System.err.println("testSimilar1");
            System.err.println("-----------");
            System.err.println(result);
            assertEquals(golden, result);
        } else {
            assertTrue("No match!", false);
        }
    }

    public void testSimilar2() {
        String[] oldL = {       "A1" };
        String[] newL = { "A3", "A2" };
        String golden =
                "{insert} A3\n" +
                "{modify} A2\n";
        ListMatcher<String> matcher = ListMatcher.<String>instance(Arrays.asList(oldL), Arrays.asList(newL), new Comparator<String>(){
            @Override
            public int compare(String o1, String o2) {
                return Math.abs(o1.charAt(1) - o2.charAt(1));
            }
        });
        if (matcher.match()) {
            String result = matcher.printResult(false);
            System.err.println("-----------");
            System.err.println("testSimilar2");
            System.err.println("-----------");
            System.err.println(result);
            assertEquals(golden, result);
        } else {
            assertTrue("No match!", false);
        }
    }
}
