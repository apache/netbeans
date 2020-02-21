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

package org.netbeans.modules.cnd.debugger.common2.debugger.io;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.pty.Pty;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.terminal.api.IOTerm;
import org.openide.windows.InputOutput;

/**
 *
 */
class InternalTerminalPack extends IOPack {
    private Pty pty = null;
    private final InputOutput io;
    private String slaveName = null;
    
    private static final Boolean fixEraseKeyInTerminal = Boolean.valueOf(System.getProperty("fixEraseKeyInTerminal", "true")); // NOI18N;

    public InternalTerminalPack(TermComponent console, InputOutput io, ExecutionEnvironment exEnv) {
        super(console, exEnv, false);
        this.io = io;
    }

    @Override
    public boolean start() {
        try {
            pty = PtySupport.allocate(exEnv);
        } catch (IOException ex) {
            slaveName = null;
            return false;
        }
        PtySupport.connect(io, pty);
        slaveName = pty.getSlaveName();
        if (fixEraseKeyInTerminal) {
            PtySupport.setBackspaceAsEraseChar(exEnv, slaveName);
        }
        return true;
    }

    @Override
    public String getSlaveName() {
        return slaveName;
    }

    @Override
    public void switchTo() {
        super.switchTo();
        // show output
        io.select();
    }

    @Override
    public void close() {
        IOTerm.disconnect(io, null);
        if (pty != null) {
            try {
                pty.close();
            } catch (IOException ex) {
                Logger.getLogger(InternalTerminalPack.class.getName()).log(Level.INFO, "Pty is already closed: ", ex);
            }
        }
    }
}
