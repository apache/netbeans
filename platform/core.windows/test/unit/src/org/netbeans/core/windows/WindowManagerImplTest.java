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

package org.netbeans.core.windows;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.*;

import org.openide.util.RequestProcessor;
import org.openide.util.Task;


/** 
 * 
 * @author Dafe Simonek
 */
public class WindowManagerImplTest extends NbTestCase {

    public WindowManagerImplTest (String name) {
        super (name);
    }

    public void testEDTAssert () {
        // run off EQ thread and check
        Task task = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                // test both versions, assertions on and off
                checkEDTAssert(WindowManagerImpl.assertsEnabled);
                WindowManagerImpl.assertsEnabled = !WindowManagerImpl.assertsEnabled;
                checkEDTAssert(WindowManagerImpl.assertsEnabled);
            }
        });

        task.waitFinished();

        assertTrue(failMsg, checkOK);
    }

    private boolean checkOK = false;
    private String failMsg = null;

    private void checkEDTAssert (final boolean assertionsEnabled) {
        Logger logger = Logger.getLogger(WindowManagerImpl.class.getName());

        logger.setFilter(new java.util.logging.Filter() {
            public boolean isLoggable(LogRecord record) {
                Level level = record.getLevel();

                if (assertionsEnabled && !level.equals(Level.WARNING)) {
                    checkOK = false;
                    failMsg = "Logging on Level WARNING expected when assertions are enabled";
                    return true;
                }

                if (!assertionsEnabled && !level.equals(Level.FINE)) {
                    checkOK = false;
                    failMsg = "Logging on Level FINE expected when assertions are disabled";
                    return true;
                }

                checkOK = true;

                // don't log anything if test passes
                return false;
            }
        });

        WindowManagerImpl.warnIfNotInEDT();
    }


    
}
