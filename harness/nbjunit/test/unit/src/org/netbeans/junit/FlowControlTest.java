/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.junit;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestFailure;
import junit.framework.TestResult;

/** Checks that behaviour of LoggingTestCaseHid is correct.
 *
 * @author  Jaroslav Tulach
 */
public class FlowControlTest extends NbTestCase {

    private Logger err;
    private Logger list;

    public FlowControlTest (String name) {
        super (name);
    }


    @Override protected int timeOut() {
        return 0;
    }

    protected Level logLevel() {
        return Level.FINE;
    }
    
    protected void setUp() throws Exception {
        err = Logger.getLogger("TEST-" + getName());
        list = Logger.getLogger("observe");
    }

    public void testCorrectThreadSwitching() throws Exception {
        
        class Run implements Runnable {
            public List<Object> events = new ArrayList<Object>();
            
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
                events.add(1);
                err.info("1");
                events.add(2);
                err.info("2");
                events.add(3);
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
        Log.controlFlow(err, list, order, 0);
        
        
        FutureTask task = new FutureTask(run);
        run.directly();
        if (!task.waitFinished(10000)) {
            fail("Runnable deadlocked");
        }
        
        String res = run.events.toString();
        
        assertEquals("Really changing the execution according to the provided order: " + res, "[A, 1, B, 2, C, 3]", res);
    }
    
    public void testWorksWithRegularExpressionsAsWell() throws Exception {
        
        class Run implements Runnable {
            public List<Object> events = new ArrayList<Object>();
            
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
                events.add(1);
                err.info("1");
                events.add(2);
                err.info("2");
                events.add(3);
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
        Log.controlFlow(err, list, order, 0);
        
        
        FutureTask task = new FutureTask(run);
        run.directly();
        if (!task.waitFinished(10000)) {
            fail("Runnable deadlocked");
        }
        
        String res = run.events.toString();
        
        assertEquals("Really changing the execution according to the provided order: " + res, "[A, 1, B, 2, C, 3]", res);
    }

    public void testLogMessagesCanRepeat() throws Exception {
        
        class Run implements Runnable {
            public List<Object> events = new ArrayList<Object>();
            
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
                events.add(1);
                err.info("1");
                events.add(2);
                err.info("2");
                events.add(3);
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
        Log.controlFlow(err, list, order, 0);
        
        
        FutureTask task = new FutureTask(run);
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
            Logger.getLogger("").info("Ahoj");
            throw throwIt;
        }
        
        FlowControlTest l = new FlowControlTest("testRuntimeExceptionsAlsoGenerateLog");
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


    private static final class FutureTask {
        private Thread thread;

        public FutureTask(Runnable delegate) {
            thread = new Thread(delegate, "Para");
            thread.start();
        }

        private boolean waitFinished(int i) throws InterruptedException {
            thread.join();
            return !thread.isAlive();
        }
    }
}
