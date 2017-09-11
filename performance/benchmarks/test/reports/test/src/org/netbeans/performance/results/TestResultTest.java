/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
/*
 * TestResultTest.java
 * JUnit based test
 *
 * Created on March 23, 2006, 9:28 AM
 */

package org.netbeans.performance.results;

import java.util.Collections;
import junit.framework.TestCase;
import junit.framework.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author radim
 */
public class TestResultTest extends TestCase {

    public TestResultTest(String testName) {
        super(testName);
    }

    public static class StatisticsTest extends TestCase {

        public StatisticsTest(String testName) {
            super(testName);
        }

    }


    public static Test suite() {
        TestSuite suite = new TestSuite(TestResultTest.class);
//        suite.addTestSuite(StatisticsTest.class);

        return suite;
    }

    /**
     * Test of computeStatistics method, of class org.netbeans.performance.results.TestResult.
     */
    public void testComputeStatistics() {

        Collection<Integer> values;
        TestResult.Statistics result;

        values = Collections.emptySet();
        result = TestResult.computeStatistics(values);
        assertEquals("wrong count", 0.0, result.getAverage());

        values = new ArrayList();
        values.add(1);
        result = TestResult.computeStatistics(values);
        assertEquals("wrong average", 1.0, result.getAverage());
        assertEquals("wrong count", 1, result.getCount());
        assertEquals("wrong variance", 0.0, result.getVariance());

        values.add(1);
        result = TestResult.computeStatistics(values);
        assertEquals("wrong average", 1.0, result.getAverage());
        assertEquals("wrong count", 2, result.getAverage());
        assertEquals("wrong variance", 0.0, result.getVariance());

        values.add(4);
        result = TestResult.computeStatistics(values);
        assertEquals("wrong average", 2.0, result.getAverage());
        assertEquals("wrong count", 3, result.getAverage());
        assertFalse("wrong variance", result.getVariance() == 0.0);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of compareTo method, of class org.netbeans.performance.results.TestResult.
     */
    public void testCompareTo() {
        TestResult r1 = new TestResult("Test a", 1000, "ms", 1, "Suite 1");
        TestResult r2 = new TestResult("Test b", 1000, "ms", 1, "Suite 1");
        TestResult r3 = new TestResult("Test a", 1000, "ms", 1, "Suite 2");
        TestResult r1a = new TestResult("Test a", 1000, "ms", 1, "Suite 1");

        assertTrue (r1.compareTo(r1) == 0);
        assertTrue (r1.compareTo(r1a) == 0);

        assertTrue (r1.compareTo(r2) < 0);
        assertTrue (r2.compareTo(r1) > 0);
        assertTrue (r1.compareTo(r3) < 0);
        assertTrue (r3.compareTo(r1) > 0);
        assertTrue (r2.compareTo(r3) < 0);
        assertTrue (r3.compareTo(r2) > 0);
    }
    
}
