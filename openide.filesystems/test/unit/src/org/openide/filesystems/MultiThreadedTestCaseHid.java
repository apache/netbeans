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

package org.openide.filesystems;
import junit.framework.*;
import org.netbeans.junit.*;

/**
 * A multi-threaded JUnit test case.
 *
*/
public class MultiThreadedTestCaseHid extends NbTestCase {
    /**
     * The threads that are executing.
     */
    private Thread threads[] = null;
    /**
     * The tests TestResult.*/
    private TestResult testResult = null;
    /**
     * Simple constructor.
     */

    public MultiThreadedTestCaseHid(String s) {
        super(s);
    }
    /**
     * Interrupt the running threads.
     */

    public void interruptThreads() {
        if(threads != null) {
            for(int i = 0;i < threads.length;i++) {
                //System.out.println("interrupted");
                threads[i].interrupt();
            }
        }
    }
    /**
     * Override run so we can squirrel away the test result.*/
    
    public void run(final TestResult result) {
        testResult = result;
        super.run(result);
        testResult = null;
    }
    /**
     * Run the test case threads.*/
    
    protected void runTestCaseRunnables (final TestCaseRunnable[] runnables, int ms) {
        if(runnables == null) {
            throw new IllegalArgumentException("runnables is null");
        }
        threads = new Thread[runnables.length];
        for(int i = 0;i < threads.length;i++) {
            threads[i] = new Thread(runnables[i]);
        }
        for(int i = 0;i < threads.length;i++) {
            threads[i].start();
        }
        try {
            long start = System.currentTimeMillis();
            for(int i = 0;i < threads.length;i++) {
                if (System.currentTimeMillis()-start > ms ) {
                    interruptThreads();
                    throw new InterruptedException ("Time ammount elapsed: probably deadlock.");
                }                
                threads[i].join((ms - (System.currentTimeMillis()-start)),0);
            }
        }
        catch(InterruptedException ignore) {
            handleException(ignore);
        }
        threads = null;
    }
    /**
     * Handle an exception. Since multiple threads won't have their
     * exceptions caught the threads must manually catch them and call
     * <code>handleException ()</code>.
     * @param t Exception to handle.*/
    
    private void handleException(final Throwable t) {
        synchronized(testResult) {
            if(t instanceof AssertionFailedError) {
                testResult.addFailure(this, (AssertionFailedError)t);
            }
            else {
                testResult.addError(this, t);
            }
        }
    }
    /**
     * A test case thread. Override runTestCase () and define
     * behaviour of test in there.*/
    protected abstract class TestCaseRunnable implements Runnable {
        /**
         * Override this to define the test*/
        
        public abstract void runTestCase()
                              throws Throwable;
        /**
         * Run the test in an environment where
         * we can handle the exceptions generated by the test method.*/
        
        public void run() {
            try {
                runTestCase();
            }
            catch(Throwable t) /* Any other exception we handle and then we interrupt the other threads.*/ {
                handleException(t);
                interruptThreads();
            }
        }
    }
}
