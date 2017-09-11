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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
