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

package org.openide.loaders;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import org.openide.util.RequestProcessor;


/** Checks that behaviour of LoggingTestCaseHid is correct.
 *
 * @author  Jaroslav Tulach
 */
public class LoggingControlTest extends LoggingTestCaseHid {

    private Logger err;

    public LoggingControlTest (String name) {
        super (name);
    }

    protected void setUp() throws Exception {
        err = Logger.getLogger("TEST-" + getName());
    }
    
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override protected int timeOut() {
        return 0;
    }

    public void testCorrectThreadSwitching() throws Exception {
        
        class Run implements Runnable {
            public ArrayList events = new ArrayList();
            
            public void run() {
                events.add("A");
                err.info("A");
                events.add("B");
                err.info("B");
                events.add("C");
                err.info("C");
            }
            
            public void directly() {
                err.info("0");
                events.add(new Integer(1));
                err.info("1");
                events.add(new Integer(2));
                err.info("2");
                events.add(new Integer(3));
                err.info("3");
            }
        }
        
        Run run = new Run();
        
        String order = 
            "THREAD:Para MSG:A" + 
            "THREAD:main MSG:0" + 
            "THREAD:main MSG:1" +
            "THREAD:Para MSG:B" +
            "THREAD:main MSG:2" +
            "THREAD:Para MSG:C" +
            "THREAD:main MSG:3";
        registerSwitches(order, 0);
        
        
        RequestProcessor rp = new RequestProcessor("Para");
        RequestProcessor.Task task = rp.post(run);
        run.directly();
        if (!task.waitFinished(10000)) {
            fail("Runnable deadlocked");
        }
        
        String res = run.events.toString();
        
        assertEquals("Really changing the execution according to the provided order: " + res, "[A, 1, B, 2, C, 3]", res);
    }
    
    public void testWorksWithRegularExpressionsAsWell() throws Exception {
        
        class Run implements Runnable {
            public ArrayList events = new ArrayList();
            
            public void run() {
                events.add("A");
                err.info("4329043A");
                events.add("B");
                err.info("B");
                events.add("C");
                err.info("CCCC");
            }
            
            public void directly() {
                err.info("0");
                events.add(new Integer(1));
                err.info("1");
                events.add(new Integer(2));
                err.info("2");
                events.add(new Integer(3));
                err.info("3");
            }
        }
        
        Run run = new Run();
        
        String order = 
            "THREAD:Para MSG:[0-9]*A" + 
            "THREAD:main MSG:0" + 
            "THREAD:main MSG:^1$" +
            "THREAD:Para MSG:B" +
            "THREAD:main MSG:2" +
            "THREAD:Para MSG:C+" +
            "THREAD:main MSG:3";
        registerSwitches(order, 0);
        
        
        RequestProcessor rp = new RequestProcessor("Para");
        RequestProcessor.Task task = rp.post(run);
        run.directly();
        if (!task.waitFinished(10000)) {
            fail("Runnable deadlocked");
        }
        
        String res = run.events.toString();
        
        assertEquals("Really changing the execution according to the provided order: " + res, "[A, 1, B, 2, C, 3]", res);
    }

    public void testLogMessagesCanRepeat() throws Exception {
        
        class Run implements Runnable {
            public ArrayList events = new ArrayList();
            
            public void run() {
                events.add("A");
                err.info("A");
                events.add("A");
                err.info("A");
                events.add("A");
                err.info("A");
            }
            
            public void directly() {
                err.info("0");
                events.add(new Integer(1));
                err.info("1");
                events.add(new Integer(2));
                err.info("2");
                events.add(new Integer(3));
                err.info("3");
            }
        }
        
        Run run = new Run();
        
        String order = 
            "THREAD:Para MSG:A" + 
            "THREAD:main MSG:0" + 
            "THREAD:main MSG:^1$" +
            "THREAD:Para MSG:A" +
            "THREAD:main MSG:2" +
            "THREAD:Para MSG:A" +
            "THREAD:main MSG:3";
        registerSwitches(order, 0);
        
        
        RequestProcessor rp = new RequestProcessor("Para");
        RequestProcessor.Task task = rp.post(run);
        run.directly();
        if (!task.waitFinished(10000)) {
            fail("Runnable deadlocked");
        }
        
        String res = run.events.toString();
        
        assertEquals("Really changing the execution according to the provided order: " + res, "[A, 1, A, 2, A, 3]", res);
    }

    private Exception throwIt;
    public void testRuntimeExceptionsAlsoGenerateLog() throws Exception {
        if (throwIt != null) {
            Logger.getLogger("global").info("Ahoj");
            throw throwIt;
        }
        
        LoggingControlTest l = new LoggingControlTest("testRuntimeExceptionsAlsoGenerateLog");
        l.throwIt = new NullPointerException();
        TestResult res = l.run();
        assertEquals("No failures", 0, res.failureCount());
        assertEquals("One error", 1, res.errorCount());
        
        Object o = res.errors().nextElement();
        TestFailure f = (TestFailure)o;
        
        if (f.exceptionMessage() == null || f.exceptionMessage().indexOf("Ahoj") == -1) {
            fail("Logged messages shall be in exception message: " + f.exceptionMessage());
        }
    }
}
