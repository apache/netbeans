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

package org.netbeans.modules.cnd.debugger.gdb2;

/*
 * GdbLogger.java
 *
 *
 * Originally this class was in org.netbeans.modules.cnd.debugger.gdb package.
 * Later a new "proxy" package was created and this class was moved, that's how
 * it lost its history. To view the history look at the previous location.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class GdbLogger is used to log all incoming and outgoing messages
 */
public class GdbLogger {
    
    private final FileWriter logFile;
    private final String filename;
    //private Logger log = Logger.getLogger("gdb.gdbproxy.logger"); // NOI18N
    
    /** Creates a new instance of GdbLogger */
    public GdbLogger() {
        FileWriter logFileValue = null;
        String logFilename = null;
        try {
//            if (!GdbDebugger.isUnitTest()) {
                File tmpfile = File.createTempFile("gdb-cmds", ".log"); // NOI18N
                logFilename = tmpfile.getAbsolutePath();
                if (!Boolean.getBoolean("gdb.console.savelog")) { // NOI18N - This lets me save logss
                    tmpfile.deleteOnExit();
                }
//            } else {
//                tmpfile = File.createTempFile("gdb-unit_test", ".log"); // NOI18N
//            }
            logFileValue = new FileWriter(tmpfile);
        } catch (IOException ex) {
        }
        this.logFile = logFileValue;
        this.filename = logFilename;
        System.setProperty("LAST_DEBUGGER_LOGFILE_PATH", filename); // NOI18N
    }
    
    /**
     * Sends message to the debugger log.
     *
     * @param message - a message from/to the debugger
     */
    public void logMessage(String message) {
        if (message != null && message.length() > 0) {
            if (logFile != null) {
                synchronized (logFile) {
                    try {
                        logFile.write(message);
                        if (message.charAt(message.length()-1) != '\n') {
                            logFile.write('\n');
                        }
                        logFile.flush();
                    } catch (IOException ioex) {
                    }
                }
            }
        }
    }

    public String getFilename() {
        return filename;
    }
}
