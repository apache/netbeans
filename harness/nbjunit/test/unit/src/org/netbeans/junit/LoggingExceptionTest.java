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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Checks that we can do proper logging of exceptions.
 *
 * @author Jaroslav Tulach
 */
public class LoggingExceptionTest extends NbTestCase {
    private Throwable toThrow;
    
    public LoggingExceptionTest(String testName) {
        super(testName);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    @Override
    protected void setUp() throws IOException {
        clearWorkDir();
    }

    @Override
    protected int timeOut() {
        return getName().contains("Time") ? 10000 : 0;
    }
    
    
    public void testLoggedExceptionIsPrinted() throws Exception {
        Exception ex = new IOException("Ahoj");
        LogRecord rec = new LogRecord(Level.WARNING, "Cannot process {0}");
        rec.setThrown(ex);
        rec.setParameters(new Object[] { "Jardo" });
        Logger.getLogger("global").log(rec);
        
        File[] arr = getWorkDir().listFiles();
        assertEquals("One log file", 1, arr.length);
        String s = LoggingTest.readFile(arr[0]);
        
        if (s.indexOf("Ahoj") == -1) {
            fail("There needs to be 'Ahoj':\n" + s);
        }
        if (s.indexOf("Jardo") == -1) {
            fail("There needs to be 'Jardo':\n" + s);
        }
        if (s.indexOf("testLoggedExceptionIsPrinted") == -1) {
            fail("There needs to be name of the method:\n" + s);
        }
    }
    public void testLoggedExceptionIsPrintedWithTimeout() throws Exception {
        testLoggedExceptionIsPrinted();
    }
    public void testLoggedExceptionIsPrintedNoFormat() throws Exception {
        Exception ex = new IOException("Ahoj");
        Logger.getLogger("global").log(Level.WARNING, "No format Jardo", ex);
        
        File[] arr = getWorkDir().listFiles();
        assertEquals("One log file", 1, arr.length);
        String s = LoggingTest.readFile(arr[0]);
        
        if (s.indexOf("Ahoj") == -1) {
            fail("There needs to be 'Ahoj':\n" + s);
        }
        if (s.indexOf("Jardo") == -1) {
            fail("There needs to be 'Jardo':\n" + s);
        }
        if (s.indexOf("testLoggedExceptionIsPrinted") == -1) {
            fail("There needs to be name of the method:\n" + s);
        }
    }
    public void testLoggedExceptionIsPrintedWithTimeoutNoFormat() throws Exception {
        testLoggedExceptionIsPrintedNoFormat();
    }
}
