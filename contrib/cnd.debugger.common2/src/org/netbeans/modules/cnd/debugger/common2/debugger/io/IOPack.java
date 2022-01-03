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

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerInfo;
import org.netbeans.modules.cnd.debugger.common2.utils.Executor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 * Only console, base for all IOPacks
 */
public class IOPack {
    private final TermComponent console;
    protected final ExecutionEnvironment exEnv;
    private final boolean external;

    protected IOPack(TermComponent console, ExecutionEnvironment exEnv, boolean external) {
        this.console = console;
        this.exEnv = exEnv;
        this.external = external;
    }

    public TermComponent console() {
	return console;
    }

    public boolean start() {
        return true;
    }

    public void open() {
	console.open();
    }

    public void bringDown() {
	console.bringDown();
    }

    public void bringUp() {
	console.bringUp();
    }

    public void switchTo() {
	console.switchTo();
    }

    public static TermComponent makeConsole(int flags) {
	return TermComponentFactory.createNewTermComponent(ConsoleTopComponent.findInstance(), flags);
    }

    public String[] getIOFiles() {
        return null;
    }

    public String getSlaveName() {
        return null;
    }
    
    public MacroMap updateEnv(MacroMap macroMap) {
        return macroMap;
    }
    
    public boolean isExternal() {
        return external;
    }

    public void close() {
    }

    public static IOPack create(boolean remote,
                                NativeDebuggerInfo ndi,
                                Executor executor) {
        int consoleType = ndi.getConsoleType(remote);

        TermComponent console;
        if (remote || Utilities.isWindows()) {
            console = IOPack.makeConsole(0);
        } else {
            console = IOPack.makeConsole(TermComponentFactory.PTY | TermComponentFactory.RAW_PTY);
        }

        InputOutput io = ndi.getInputOutput();
        IOPack res;
        final ExecutionEnvironment exEnv = executor.getExecutionEnvironment();
        
        if (ndi.isClone()) { // follow fork clone
            res = new IOPack(console, exEnv, false);
        } else if (NativeDebuggerManager.isStandalone()) {
            TermComponent pio;
            if (remote || Utilities.isWindows()) {
                pio = PioPack.makePio(0);
            } else {
                pio = PioPack.makePio(TermComponentFactory.PTY | TermComponentFactory.PACKET_MODE);
            }
            res = new PioPack(console, pio, exEnv);
        } else if (io == null) { // Attach or other non-start mode
            res = new IOPack(console, exEnv, false);
        } else if (consoleType == RunProfile.CONSOLE_TYPE_EXTERNAL) {
            if (!remote && Utilities.isWindows()) {
                res = new IOPack(console, exEnv, true);
            } else {
                res = new ExternalTerminalPack(console, ndi.getProfile().getTerminalPath(), exEnv);
            }
        } else if (consoleType == RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW) {
            // no support on windows, IZ 193740, switch to external
            if (Utilities.isWindows() && !remote) {
                notifyAboutConsoleTypeOnce();
                res = new IOPack(console, exEnv, true);
            } else {
                res = new OutputPack(console, io, exEnv);
            }
        } else {
            // switch to external if no pty on windows
            if (Utilities.isWindows() && !remote && !PtySupport.isSupportedFor(exEnv)) {
                notifyAboutConsoleTypeOnce();
                res = new IOPack(console, exEnv, true);
            } else {
                res = new InternalTerminalPack(console, io, exEnv);
            }
        }

	res.bringUp();
	// OLD bug #181165 let "debugger" group open it
	// OLD open();

	// PioWindow multiplexes consoles so need to explicitly
	// bring the new ones to front.
	res.switchTo();

        return res;
    }
    
    private static boolean notified = false;
    private static void notifyAboutConsoleTypeOnce() {
        if (!notified) {
            NativeDebuggerManager.warning(Catalog.get("MSG_Console_Type_Unsupported"));
            notified = true;
        }
    }
}
