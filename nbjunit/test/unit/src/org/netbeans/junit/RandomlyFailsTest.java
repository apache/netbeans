/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
        runs = new ArrayList<Integer>();
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
