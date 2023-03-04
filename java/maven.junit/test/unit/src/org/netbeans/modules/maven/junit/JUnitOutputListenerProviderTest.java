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

package org.netbeans.modules.maven.junit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import static org.junit.Assert.*;

/**
 *
 * @author mkleint
 */
public class JUnitOutputListenerProviderTest {

    public JUnitOutputListenerProviderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of constructTrouble method, of class JUnitOutputListenerProvider.
     */
    @Test
    public void testConstructTrouble() {
        Trouble t = JUnitOutputListenerProvider.constructTrouble("junit.framework.AssertionFailedError", "message",
                "junit.framework.AssertionFailedError: failed\n" +
                "	at junit.framework.Assert.fail(Assert.java:47)\n" +
                "	at com.mycompany.mavenproject30.AppTest.testApp(AppTest.java:39)", true);
        assertNull(t.getComparisonFailure());
        String[] stack = t.getStackTrace();
        assertNotNull(stack);
        assertArrayEquals(new String[] {
            "message",
            "junit.framework.AssertionFailedError",
                "	at junit.framework.Assert.fail(Assert.java:47)",
                "	at com.mycompany.mavenproject30.AppTest.testApp(AppTest.java:39)"

        }, stack);


        t = JUnitOutputListenerProvider.constructTrouble("junit.framework.AssertionFailedError", "hello? expected:<2> but was:<1>",
                "junit.framework.AssertionFailedError: hello? expected:&lt;2&gt; but was:&lt;1&gt;\n" +
"	at junit.framework.Assert.fail(Assert.java:47)\n" +
"	at junit.framework.Assert.failNotEquals(Assert.java:282)\n" +
"	at junit.framework.Assert.assertEquals(Assert.java:64)\n" +
"	at junit.framework.Assert.assertEquals(Assert.java:201)\n" +
"	at com.mycompany.mavenproject30.AppTest.testApp2(AppTest.java:44)\n", true);

        assertNotNull(t.getComparisonFailure());
        stack = t.getStackTrace();
        assertNotNull(stack);

        t = JUnitOutputListenerProvider.constructTrouble("java.lang.AssertionError", "hello? expected [2] but found [1]",
	"hello? expected [2] but found [1]\n" +
"java.lang.AssertionError\n" +
"	at org.testng.Assert.fail(Assert.java:94)\n" +
"	at org.testng.Assert.failNotEquals(Assert.java:494)\n" +
"	at org.testng.Assert.assertEquals(Assert.java:123)\n" +
"	at org.testng.Assert.assertEquals(Assert.java:370)\n" +
"	at test.mavenproject1.AppNGTest.testMain(AppNGTest.java:50)\n", true);

        assertNotNull(t.getComparisonFailure());
        stack = t.getStackTrace();
        assertNotNull(stack);
        
    }

    @Test
    public void testIsFullJavaId() {
        assertTrue("This is a java FQN", JUnitOutputListenerProvider.isFullJavaId("org.netbeans.modules.MyClass"));
        assertTrue("This is a java FQN", JUnitOutputListenerProvider.isFullJavaId("o.n.m.MyClass"));
        assertTrue("This is a java FQN", JUnitOutputListenerProvider.isFullJavaId("a.b.c"));
        assertTrue("This is a java FQN", JUnitOutputListenerProvider.isFullJavaId("aa.aa.a"));
        assertFalse("This is not a java FQN", JUnitOutputListenerProvider.isFullJavaId("org.netbeans.modules.MyClass."));
        assertFalse("This is not a java FQN", JUnitOutputListenerProvider.isFullJavaId("a.b. c"));
    }

    @Test
    public void testTestSuiteStats() {
        assertTrue("This is a test suite stats", JUnitOutputListenerProvider.isTestSuiteStats("Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.054 sec - in com.mycompany.mavenproject5.NewClass2Test"));
        assertFalse("This is not a test suite stats", JUnitOutputListenerProvider.isTestSuiteStats("Tests run: 5, Failures: 0, Errors: 0, Aborted: 0, Time elapsed: 5.054 sec - in com.mycompany.mavenproject5.NewClass2Test"));
        assertTrue("Correct test suite", JUnitOutputListenerProvider.getTestSuiteFromStats("Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.054 sec - in com.mycompany.mavenproject5.NewClass2Test").equals("com.mycompany.mavenproject5.NewClass2Test"));
        assertFalse("Wrong test suite", JUnitOutputListenerProvider.getTestSuiteFromStats("Tests run: 5, Failures: 0, Errors: 0, Aborted: 0, Time elapsed: 5.054 sec") != null);
    }

}
