/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
