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

package org.netbeans.core;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.core.NbErrorManager.Exc;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaroslav Tulach
 */
public class NotifyExcPanelTest extends NbTestCase {
    Logger main;
    
    public NotifyExcPanelTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        main = Logger.getLogger("");
        for (Handler h : main.getHandlers()) {
            main.removeHandler(h);
        }
    }
    
    public void testHandlesThatImplementCallableForJButtonAreIncluded() throws Exception {
        class H extends Handler 
        implements Callable<JButton> {
            public JButton button = new JButton("Extra");
        
            public void publish(LogRecord arg0) {
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }

            public JButton call() throws Exception {
                return button;
            }
        } // end of H
        
        H handler = new H();
        
        main.addHandler(handler);
        
        List<Object> options = Arrays.asList(NotifyExcPanel.computeOptions("prev", "next"));
        
        assertTrue("Contains our button: " + options, options.contains(handler.button));
    }

    public void testHandlesThatImplementCallableForOtherObjectsAreNotIncluded() throws Exception {
        class H extends Handler 
        implements Callable<Object> {
            public JButton button = new JButton("Extra");
        
            public void publish(LogRecord arg0) {
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }

            public JButton call() throws Exception {
                return button;
            }
        } // end of H
        
        H handler = new H();
        
        main.addHandler(handler);
        
        List<Object> options = Arrays.asList(NotifyExcPanel.computeOptions("prev", "next"));
        
        assertFalse("Does not contain our button: " + options, options.contains(handler.button));
    }
    
    public void testLimitOf20() {
        NotifyExcPanel.ArrayListPos arr = new NotifyExcPanel.ArrayListPos();
        for (int i = 0; i < (NotifyExcPanel.ArrayListPos.SOFT_MAX_SIZE-1); i++) {
            arr.add(null);
        }
        assertEquals("Nineteen elements", NotifyExcPanel.ArrayListPos.SOFT_MAX_SIZE-1, arr.size());
        arr.add(null);
        assertEquals("Twenty", NotifyExcPanel.ArrayListPos.SOFT_MAX_SIZE, arr.size());
        
        Exc ex = new Exc(null, Level.OFF, new LogRecord[0], new LogRecord[0]);
        arr.add(ex);
        
        assertEquals("Still twenty", NotifyExcPanel.ArrayListPos.SOFT_MAX_SIZE, arr.size());
        assertEquals(ex, arr.get(NotifyExcPanel.ArrayListPos.SOFT_MAX_SIZE-1));
        
    }
    
    public void testManyExceptionsReported() {
        int numExceptions = 10 * NotifyExcPanel.ArrayListPos.HARD_MAX_SIZE;
        NbErrorManager errorManager = new NbErrorManager();
        Logger excLogger = Logger.getLogger(Exceptions.class.getName());
        final LogRecord[] logRecordRef = new LogRecord[] { null };
        Handler logHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                logRecordRef[0] = record;
            }
            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };
        excLogger.addHandler(logHandler);
        for (int i = 0; i < numExceptions; i++) {
            Exception exc = new ClassCastException(Integer.toString(i));
            exc = Exceptions.attachMessage(exc, "A message "+i);
            Exceptions.printStackTrace(exc);
            errorManager.publish(logRecordRef[0]);
        }
        // Wait for the Exc creations...
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override public void run() {}
            });
        } catch (InterruptedException | InvocationTargetException ex) {}
        int excSize = NotifyExcPanel.exceptions.size();
        assertTrue("We have less exceptions than the hard limit", excSize <= NotifyExcPanel.ArrayListPos.HARD_MAX_SIZE);
        for (int i = 0; i < excSize; i++) {
            NbErrorManager.Exc exc = (NbErrorManager.Exc) NotifyExcPanel.exceptions.get(i);
            LogRecord[] arr = exc.arr;
            LogRecord[] arrAll = exc.arrAll;
            // Size should be the exception + annotation = 2
            assertEquals("Records size", 2, arr.length);
            assertEquals("All records size", 2, arrAll.length);
            if (i >= (excSize - 10)) {  // Check that the last 10 exceptions are the last 10 thrown:
                int fromEnd = excSize - i;
                assertEquals("Correct record", Integer.toString(numExceptions - fromEnd), exc.getMessage());
                assertEquals("Correct record", "A message "+(numExceptions - fromEnd), arr[0].getMessage());
            }
        }
        excLogger.removeHandler(logHandler);
    }
}
