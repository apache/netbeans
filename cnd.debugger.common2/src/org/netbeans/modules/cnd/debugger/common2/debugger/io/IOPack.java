/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
