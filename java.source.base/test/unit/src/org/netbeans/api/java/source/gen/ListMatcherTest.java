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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
