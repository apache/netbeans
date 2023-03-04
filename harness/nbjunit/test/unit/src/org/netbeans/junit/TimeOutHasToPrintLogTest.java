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

import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestFailure;
import junit.framework.TestResult;

/** Provides an example of randomized test.
 */
public class TimeOutHasToPrintLogTest extends NbTestCase {
    private Logger LOG = Logger.getLogger("my.log.for.test");
    
    public TimeOutHasToPrintLogTest(String testName) {
        super(testName);
    }
    
    private boolean isSpecial() {
        return getName().equals("printAhojAndTimeOut") || getName().equals("justTimeOutInOneOfMyMethods");
    }
    
    @Override
    protected Level logLevel() {
        if (isSpecial()) {
            return null;
        } else {
            return Level.FINE;
        }
    }
    
    @Override
    protected int timeOut() {
        if (isSpecial()) {
            return 700;
        } else {
            return 0;
        }
    }
    
    public void printAhojAndTimeOut() throws Exception {
        LOG.fine("Ahoj");
        Thread.sleep(10000);
    }
    
    public void justTimeOutInOneOfMyMethods() throws Exception {
        Thread.sleep(10000);
    }

    public void testThatTheTimeOutStillPrintsTheWarning() throws Exception {
        TimeOutHasToPrintLogTest t = new TimeOutHasToPrintLogTest("printAhojAndTimeOut");
        
        CharSequence seq = Log.enable(LOG.getName(), Level.FINE);
        
        TestResult res = t.run();
        
        assertEquals("One test has been run", 1, res.runCount());
        
        String s = seq.toString();
        
        if (s.indexOf("Ahoj") == -1) {
            fail("Ahoj has to be logged:\n" + s);
        }
        
        assertEquals("No error", 0, res.errorCount());
        assertEquals("One failure", 1, res.failureCount());
        
        TestFailure f = (TestFailure)res.failures().nextElement();
        s = f.exceptionMessage();
        if (s.indexOf("Ahoj") == -1) {
            fail("Ahoj has to be part of the message:\n" + s);
        }
    }

    public void testThreadDumpPrinted() throws Exception {
        TimeOutHasToPrintLogTest t = new TimeOutHasToPrintLogTest("justTimeOutInOneOfMyMethods");
        
        TestResult res = t.run();
        
        assertEquals("One test has been run", 1, res.runCount());
        TestFailure failure = (TestFailure)res.failures().nextElement();
        String s = failure.exceptionMessage();

        if (s.indexOf("justTimeOutInOneOfMyMethods") == -1) {
            fail("There should be thread dump reported in case of timeout:\n" + s);
        }
        
        assertEquals("No error", 0, res.errorCount());
        assertEquals("One failure", 1, res.failureCount());
    }
}
