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
