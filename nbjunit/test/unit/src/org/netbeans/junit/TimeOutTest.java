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

import java.util.Enumeration;
import javax.swing.SwingUtilities;
import junit.framework.AssertionFailedError;
import junit.framework.TestFailure;
import junit.framework.TestResult;

/** Check that the test can timeout.
 *
 * @author Jaroslav Tulach
 */
public class TimeOutTest extends NbTestCase {
    private Thread main;
    private boolean expectedResult;

    public TimeOutTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
    }

    protected void tearDown () throws Exception {
    }
    
    protected boolean runInEQ () {
        return getName().indexOf("AWT") >= 0;
    }
    
    protected int timeOut() {
        return 2500;
    }

    public void run(TestResult result) {
        if (!canRun()) {
            return;
        }
        this.main = Thread.currentThread();
        
        TestResult mine = new TestResult();
        result.startTest(this);
        super.run(mine);
        
        if (mine.errorCount() != 0) {
            Enumeration en = mine.errors();
            while(en.hasMoreElements()) {
                TestFailure f = (TestFailure)en.nextElement();
                result.addError(this, f.thrownException());
            }
            return;
        }
        if (expectedResult != (mine.failureCount() == 0)) {
            result.addFailure(this, 
                new AssertionFailedError(
                    "expectedResult: " + expectedResult + "failureCount: " + mine.failureCount() + " for " + getName()
                )
            );
            return;
        }
        
        result.endTest(this);
    }
    
    public void testRunsInAWTThreadAndShallSucceed () {
        assertTrue(SwingUtilities.isEventDispatchThread());
        expectedResult = true;
    }

    @RandomlyFails // NB-Core-Build #8326: expectedResult: truefailureCount: 1
    public void testRunsInAWTThreadAndShallSucceedWith1sDelay () throws Exception {
        assertTrue(SwingUtilities.isEventDispatchThread());
        expectedResult = true;
        Thread.sleep(1000);
    }

    public void testRunsInAWTThreadAndShallFailWith5sDelay () throws Exception {
        assertTrue(SwingUtilities.isEventDispatchThread());
        expectedResult = false;
        Thread.sleep(5000);
    }

    public void testRunsShallSucceedWithNoDelay () {
        assertFalse(SwingUtilities.isEventDispatchThread());
        if (Thread.currentThread() == main) {
            fail("We should run in dedicated thread");
        }
        expectedResult = true;
    }

    @RandomlyFails // NB-Core-Build #8142
    public void testRunsShallSucceedWith1sDelay () throws InterruptedException {
        assertFalse(SwingUtilities.isEventDispatchThread());
        if (Thread.currentThread() == main) {
            fail("We should run in dedicated thread");
        }
        expectedResult = true;
        Thread.sleep(1000);
    }
    
    public void testRunsShallFailWith5sDelay () throws InterruptedException {
        assertFalse(SwingUtilities.isEventDispatchThread());
        if (Thread.currentThread() == main) {
            fail("We should run in dedicated thread");
        }
        expectedResult = false;
        Thread.sleep(5000);
    }
}
