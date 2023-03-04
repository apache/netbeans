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

package org.openide;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.netbeans.junit.*;

/** Verify that things delegation of ErrorManager to logging and back does not cause
 * stack overflows.
 *
 * @author Jaroslav Tulach
 */
public class ErrorManagerCyclicDepTest extends NbTestCase {

    
    public ErrorManagerCyclicDepTest(java.lang.String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        Logger l = new LoggerAdapter("double");
        LogManager.getLogManager().addLogger(l);
    }

    public void testSendLogMsg() {
        ErrorManager e = ErrorManager.getDefault().getInstance("double");
        e.log(ErrorManager.WARNING, "Ahoj");
    }

    public void testSendNotify() {
        ErrorManager e = ErrorManager.getDefault().getInstance("double");
        e.notify(ErrorManager.WARNING, new Exception("Ahoj"));
    }

    /** based on
     * https://thinnbeditor.dev.java.net/source/browse/thinnbeditor/thinnbeditor/src/net/java/dev/thinnbeditor/logging/LoggerAdapter.java?rev=1.1&view=auto&content-type=text/vnd.viewcvs-markup
     */
    private static final class LoggerAdapter extends Logger {
        private static final Map<Level,Integer> levelMap = new HashMap<Level,Integer>();
        private static final Map<Integer,Level> errorManagerMap = new TreeMap<Integer,Level>();
        private static final Map<Level,Integer> exceptionLevelMap = new HashMap<Level,Integer>();
        
        static {
            levelMap.put(Level.SEVERE, new Integer(ErrorManager.ERROR));
            levelMap.put(Level.WARNING, new Integer(ErrorManager.WARNING));
            levelMap.put(Level.INFO, new Integer(ErrorManager.INFORMATIONAL));
            levelMap.put(Level.CONFIG, new Integer(ErrorManager.INFORMATIONAL));
            levelMap.put(Level.FINE, new Integer(3));
            levelMap.put(Level.FINER, new Integer(2));
            levelMap.put(Level.FINEST, new Integer(1));
            
            for (Iterator<Map.Entry<Level,Integer>> i = levelMap.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<Level,Integer> entry = i.next();
                errorManagerMap.put(entry.getValue(), entry.getKey());
            }
            
            errorManagerMap.put(new Integer(ErrorManager.INFORMATIONAL), Level.CONFIG);
            
            exceptionLevelMap.put(Level.SEVERE, new Integer(ErrorManager.USER));
            exceptionLevelMap.put(Level.WARNING, new Integer(ErrorManager.USER));
            exceptionLevelMap.put(Level.INFO, new Integer(ErrorManager.INFORMATIONAL));
            exceptionLevelMap.put(Level.CONFIG, new Integer(ErrorManager.INFORMATIONAL));
            exceptionLevelMap.put(Level.FINE, new Integer(3));
            exceptionLevelMap.put(Level.FINER, new Integer(2));
            exceptionLevelMap.put(Level.FINEST, new Integer(1));
        }
        
        private ErrorManager errorManager;
        private final Formatter formatter = new SimpleFormatter();
        
        public LoggerAdapter(String name) {
            super(name, null);
        }

        private void init() {
            if (errorManager != null) {
                return;
            }
            
            errorManager = ErrorManager.getDefault().getInstance(getName());
            
            for (Iterator i = errorManagerMap.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry entry = (Map.Entry)i.next();
                
                int level = ((Integer)entry.getKey()).intValue();
                
                if (errorManager.isLoggable(level)) {
                    setLevel((Level)entry.getValue());
                    break;
                }
            }
        }

        @Override
        public void log(LogRecord record) {
            init();

            errorManager.log(((Integer)levelMap.get(record.getLevel())).intValue(),
                formatter.format(record));
            
            if (record.getThrown() != null) {
                errorManager.notify(((Integer)exceptionLevelMap.get(
                    record.getLevel())).intValue(), record.getThrown());
            }
        }
    }

}
