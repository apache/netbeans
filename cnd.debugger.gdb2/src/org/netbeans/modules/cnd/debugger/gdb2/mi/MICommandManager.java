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

package org.netbeans.modules.cnd.debugger.gdb2.mi;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.netbeans.modules.cnd.debugger.gdb2.GdbLogger;

/*
 * Manages ...
 * <ul>
 * <li> Queueing of command and sending them to the engine.
 * <li> Receiving of output from engine and parsing and dispatching it.
 */

class MICommandManager {
    private final MICommandInjector injector;
    private final GdbLogger gdbLogger;
    
    private static final int MIN_TOKEN = 2;
    private static final int MAX_TOKEN = Integer.MAX_VALUE-1000;

    private int commandToken = MIN_TOKEN;
    
    private final ConcurrentLinkedQueue<MICommand> pendingCommands = new ConcurrentLinkedQueue<MICommand>();
    
    private Runnable idleHandler = null;

    public MICommandManager(MICommandInjector injector, GdbLogger gdbLogger) {
	this.injector = injector;
        this.gdbLogger = gdbLogger;
    } 

    /**
     * Send a command immediately or queue it up if there are
     * pending commands.
     * <br>
     * NOTE: Currently commands are always sent immediately.
     */

    public synchronized void send(MICommand cmd) {
	cmd.setManagerData(this, commandToken++);
        // limit max token to avoid prsing errors
        if (commandToken > MAX_TOKEN) {
            commandToken = MIN_TOKEN;
        }
	pendingCommands.add(cmd);
        String commandStr = String.valueOf(cmd.getToken()) + cmd.command() + '\n';
        gdbLogger.logMessage(commandStr);
	injector.inject(commandStr);
    }
    
    /**
     * We're done with this command. 
     * Take it off the pending list send off any queued up commands.
     * <br>
     * NOTE: Currently commands are always sent immediately.
     */

    void finish(MICommand cmd) {
        pendingCommands.remove(cmd);
        
        // idle handler that work one time when no commands need to be processed
        if (idleHandler != null && pendingCommands.isEmpty()) {
            idleHandler.run();
            idleHandler = null;
        }
        
	if (Log.MI.finish) {
	    echo(String.format("## finished %d\n\r", cmd.getToken())); // NOI18N
	    echo(String.format("## outstanding: ")); // NOI18N
            synchronized (pendingCommands) {
                if (pendingCommands.isEmpty()) {
                    echo(String.format("none")); // NOI18N
                } else {
                    for (MICommand oc : pendingCommands) {
                        echo(String.format(" %d", oc.getToken())); // NOI18N
                    }
                }
            }
	    echo(String.format("\n\r")); // NOI18N
	}
    }
    
    /**
     * Executed only once when all commands are sent and all answers dispatched
     */
    void setIdleHandler(Runnable handler) {
        assert !pendingCommands.isEmpty();
        this.idleHandler = handler;
    }
    
    // check for async error like
    // "Cannot execute command ... while target running"
    // see IZ 200046
    private boolean processAsyncError(MIRecord record) {
        if ("error".equals(record.cls) && !record.isEmpty()) { //NOI18N
            if (record.results().getConstValue("msg").endsWith("while target running")) { //NOI18N
                for (Iterator<MICommand> iter = pendingCommands.iterator(); iter.hasNext();) {
                    if (iter.next().getToken() == record.token) {
                        iter.remove();
                        break;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * To be called from specialization of MIProxy.
     */

    public void dispatch(MIRecord record) {
        int token = record.token();
	MICommand cmd = pendingCommands.peek();

        if (processAsyncError(record)) {
            return;
        }
        
        while (cmd != null && cmd.getToken() < token) {
            // an error happened somewhere
            // delete all unanswered commands
            cmd = pendingCommands.poll();
            gdbLogger.logMessage(String.format("No answer for: %s\n\r", cmd.toString())); // NOI18N
            cmd = pendingCommands.peek();
        }
	if (cmd == null || cmd.getToken() != token) {
	    gdbLogger.logMessage(String.format("No command for record %s\n\r", record)); // NOI18N
            streamMessages.clear();
            consoleMessages.clear();
	    return;
	}

	record.setCommand(cmd);
        cmd.recordLogStream(streamMessages);
        streamMessages.clear();
        cmd.recordConsoleStream(consoleMessages);
        consoleMessages.clear();

	if (record.isError()) {
	    echo(record.error() + "\n\r"); // NOI18N
	    finish(cmd);
	    return;
	}

	if (record.type == '^') {
	    if (record.cls.equals("done")) { // NOI18N
		cmd.onDone(record);
	    } else if (record.cls.equals("running")) { // NOI18N
		cmd.onRunning(record);
	    } else if (record.cls.equals("error")) { // NOI18N
		cmd.onError(record);
	    } else if (record.cls.equals("exit")) { // NOI18N
		cmd.onExit(record);
	    } else {
		cmd.onOther(record);
	    }
	} else if (record.type == '*') {
	    if (record.cls.equals("stopped")) { // NOI18N
		cmd.onStopped(record);
	    } else {
		cmd.onOther(record);
	    } 
	} else {
	    cmd.onOther(record);
	} 
    }

    private final LinkedList<String> streamMessages = new LinkedList<String>();
    /**
     * Record logStream data into the current pending command.
     */
    void logStream(String data) {
        streamMessages.add(data);
    }

    private final LinkedList<String> consoleMessages = new LinkedList<String>();
    /**
     * Record logConsole data into the current pending command.
     */
    void logConsole(String data) {
        consoleMessages.add(data);
    }
    
    void clearMessages() {
        streamMessages.clear();
        consoleMessages.clear();
    }

    /**
     * Echo something on the debugger console.
     */
    void echo(String data) {
	injector.log(data);
        gdbLogger.logMessage(data);
    }
}

