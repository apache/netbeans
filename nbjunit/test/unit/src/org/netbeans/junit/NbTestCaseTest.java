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

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestResult;
import junit.framework.AssertionFailedError;

/** Regular test of the behaviour.
 *
 * @author jarda
 */
public class NbTestCaseTest extends NbTestCase {

    public NbTestCaseTest(String testName) {
        super(testName);
    }
    
    public void testNetBeansFullHackIsSet() throws Exception {
        assertEquals("true", System.getProperty("netbeans.full.hack"));
    }

    public void testJustRunTestCase() {
        class Fail extends NbTestCase {
            public Fail() {
                super("testFail");
            }

            public void testFail() {
                throw new IllegalStateException();
            }
        }

        Fail f = new Fail();

        TestResult res = new TestResult();
        f.run(res);

        assertEquals("One error", 1, res.errorCount());


    }

    public void testLoggingUtil() throws Exception {
        CharSequence seq = Log.enable("", Level.WARNING);

        Logger log = Logger.getLogger(getName());
        log.log(Level.SEVERE, "Ahoj");
        log.log(Level.FINE, "Jardo");



        String s = seq.toString();
        if (s.indexOf("Ahoj") == -1) {
            fail("There should be Ahoj\n" + s);
        }
        assertEquals("Not logged for FINE: " + s, -1, s.indexOf("Jardo"));

        WeakReference<CharSequence> r = new WeakReference<CharSequence>(seq);
        seq = null;
        assertGC("Sequence can go away", r);

        int len = Logger.getLogger("").getHandlers().length;

        log.log(Level.WARNING, "Go away");

        assertEquals("One logger is gone", len - 1, Logger.getLogger("").getHandlers().length);
    }
    
    public void testAssertGcPasses() {
        Object o = new Object();
        WeakReference<Object> wr = new WeakReference<Object>(o);
        
        o = null;
        assertGC("The object is really not referenced", wr);
    }

    static Object REF_O;

    public void testAssertGcFails() {
        REF_O = new Object();
        WeakReference<Object> wr = new WeakReference<Object>(REF_O);
        try {
            assertGC("The object is really not referenced", wr);
        } catch (AssertionFailedError afe) {
            assertTrue("Found the reference", afe.getMessage().indexOf("REF_O") >= 0);
            return;
        } finally {
            REF_O = null;
        }
        fail("The assertion should fail");
    }

    static Object REF_O2;

    static class Node {
        Object next;
        Node(Object o) {
            next = o;
        }
    }

    @RandomlyFails // NB-Core-Build #2880
    public void testAssertGcFailsWithTwoPaths() {
        Object target = new Object();
        REF_O = new Node(new Node(new Node(target)));
        REF_O2 = new Node(new Node(new Node(target)));
        WeakReference<Object> wr = new WeakReference<Object>(target);
        target = null;
        
        String old = System.getProperty("assertgc.paths");
        System.setProperty("assertgc.paths", "3");
        try {
            assertGC("The object is really not referenced", wr);
        } catch (AssertionFailedError afe) {
            assertTrue("Found the reference", afe.getMessage().indexOf("REF_O") >= 0);
            assertTrue("Found the other reference", afe.getMessage().indexOf("REF_O2") >= 0);
            return;
        } finally {
            REF_O = null;
            REF_O2 = null;
            if (old == null) {
                System.clearProperty("assertgc.paths");
            } else {
                System.setProperty("assertgc.paths", old);
            }
        }
        fail("The assertion should fail");
    }

    @RandomlyFails // NB-Core-Build #1987
    public void testAssertGcFailsForUntraceableObject() {
        Object o = new Object();
        WeakReference<Object> wr = new WeakReference<Object>(o);
        
        try {
            assertGC("The object is really not referenced", wr);
        } catch (AssertionFailedError afe) {
            assertTrue("Found the reference:\n" + afe.getMessage(), afe.getMessage().indexOf("Not found") >= 0);
            return;
        }
        fail("The assertion should fail");
    }
    
    private static String previous;
    
    public void testWorkDirLength() throws IOException {
        System.setProperty("nbjunit.too.long", "10");

        getLog().println("Ahoj");
        clearWorkDir();

        String s = getWorkDirPath();
        if (s.equals(previous)) {
            fail("Inside one testcase, one directory shall not be used multiple times: " + previous + " now: " + s);
        }
        previous = s;
            
        if (s.length() > 200) {
            fail("Too long (" + s.length() + "): " + s);
        }
        if (s.indexOf(".netbeans.") > 0) {
            fail("package names shall be abbreviated: " + s);
        }
        if (s.indexOf("testWorkDir") > 0) {
            fail("no testWorkDir: " + s);
        }
    }
    
    public void testWorkDirLengthsimilarname() throws IOException {
        testWorkDirLength();
    }
    
    /**
     * Tests workdir abbreviation of test method names like test1,
     * test_12345, testabcd, test_a, abcd, etc.
     *
     * @throws IOException
     */
    public void testWorkdirLengthWithoutUppercase() throws IOException {
        System.setProperty("nbjunit.too.long", "10");
        File workdirRoot = new File(Manager.getWorkDirPath());
        String[] names = {"test1", "test_12345", "testabcd", "test_a", "abcd"};
        for (String name : names) {
            File workdir = new NbTestCaseTest(name).getWorkDir();
            assertTrue("Workdir is invalid:" + workdir, workdir.isDirectory() && workdir.getParentFile().getParentFile().equals(workdirRoot));
        }
    }
}
