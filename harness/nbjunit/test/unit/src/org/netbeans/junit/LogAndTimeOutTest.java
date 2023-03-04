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
import junit.framework.TestResult;


/**
 * @author Jaroslav Tulach
 */
@RandomlyFails // NB-Core-Build #1319
public class LogAndTimeOutTest extends NbTestCase {
    public LogAndTimeOutTest(String name) {
        super(name);
    }
    
    
    public void testLoggingAndTimeOut() throws Exception {
        TestResult result = new TestResult();
        
        T t = new T("testLoadFromSubdirTheSFS");
        t.run(result);
        
        assertEquals("No error", 0, result.errorCount());
        assertEquals("One failure", 1, result.failureCount());
        
        Object o = result.failures().nextElement();
        
        String output = o.toString();
        if (output.indexOf("LogAndTimeOutTest$T") == -1) {
            fail("There should be a stacktrace:\n" + output);
        }
        if (output.indexOf("Adding 5") == -1) {
            fail("There should be a 'Adding 5' message:\n" + output);
        }
    }
    
    
    public static class T extends NbTestCase {
        
        
        public T(String name) {
            super(name);
        }

        @Override
        protected Level logLevel() {
            return Level.FINE;
        }

        @Override
        protected int timeOut() {
            return 1000;
        }

        public void testLoadFromSubdirTheSFS() throws Exception {
            Logger log = Logger.getLogger(T.class.getName());
            for (int i = 0; i < 100; i++) {
                log.fine("Adding " + i);
                Thread.sleep(100);
            }
        }
    } // end of T
    
}
