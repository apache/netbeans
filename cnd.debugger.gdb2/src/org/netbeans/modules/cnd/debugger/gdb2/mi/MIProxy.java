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

import org.netbeans.modules.cnd.debugger.gdb2.GdbLogger;

/**
 * The main class for interacting with an MI engine.
 *
 * See Description for how to use this.
 * <br>
 * The general flow is as follows:
 <pre>
 Client code will call processLine(). Which may call back one of:
    result(record);
    statusAsyncOutput(record);
    execAsyncOutput(record);
    notifyAsyncOutput(record);

    consoleStreamOutput(record);
    targetStreamOutput(record);
    logStreamOutput(record);

    prompt();
    errorBadLine(line);
    connectionEstablished();

 The client code will usually override the *StreamOutput() but
 result() or *AsyncOutput() usually implicitly call dispatch()
 which will arrange for the appropriate callback (onDone() etc) through
 the appropriate MICommand instance.
 </pre>
 */

public abstract class MIProxy {

    private boolean connected;
    private final MICommandManager cmdManager;
    private final String prompt;
    private final MIParser parser;
    private final GdbLogger gdbLogger;

    protected MIProxy(MICommandInjector injector, String prompt, String encoding) {
	assert prompt != null;

        gdbLogger = new GdbLogger();
	cmdManager = new MICommandManager(injector, gdbLogger);
	this.prompt = prompt;
        
	parser = new MIParser(encoding);
    }
    
    /**
     * Log NB version and gdb log file name
     */
    public void logInfo() {
        cmdManager.echo("This log is saved to: " + gdbLogger.getFilename() + "\n\r"); // NOI18N
        cmdManager.echo("NB build: " + System.getProperty("netbeans.buildnumber") + "\n\r"); // NOI18N
    }


    /**
     * Send a command off to the engine.
     */

    public void send(MICommand cmd) {
	if (Log.MI.echo) {
	    System.out.printf("MI-> %s\n", cmd); // NOI18N
	}
	cmdManager.send(cmd);
    }


    /**
     * Dispatch an MI input line.
     * LATER: Return true if we want the line echoed.
     */

    public boolean processLine(/* TMP final */ String line) {
        gdbLogger.logMessage(line);

	if (line.trim().equals(prompt)) {
	    if (!connected) {
		connected = true;
		connectionEstablished();
	    }
	    prompt();
	    return true;
	}

	if (Log.MI.echo) {
	    System.out.printf("MI<- %s\n", line); // NOI18N
	}

	if (Log.MI.mimicmac) {
	    // mimic mac format
	    if (line.contains("stack-args")) { // NOI18N
		line = line.replace('[', '{');
		line = line.replace(']', '}');
	    }
	}

	parser.setup(line);
	MIRecord record = parser.parse();
	/* DEBUG
	if (record.isError()) {
	    System.out.println("---------> ERROR: " + record.error());
	} else {
	    System.out.println("---------> " + record.toString());
	}
	*/

	switch (record.type()) {
	    case '^':
		result(record);
		return true;
	    case '+':
		statusAsyncOutput(record);
		return true;
	    case '*':
		execAsyncOutput(record);
		return true;
	    case '=':
		notifyAsyncOutput(record);
		return true;

	    case '~':
		consoleStreamOutput(record);
		return true;
	    case '@':
		targetStreamOutput(record);
		return true;
	    case '&':
		logStreamOutput(record);
		return true;
	    case '?':
		// to work around gdb 6.8 echoing issue
		return true;
	    default:
		errorBadLine(line);
		return true;
	}
    }

    protected void dispatch(MIRecord record) {
	cmdManager.dispatch(record);
    }


    protected void result(MIRecord record) {
	dispatch(record);
    }

    protected void statusAsyncOutput(MIRecord record) {
	dispatch(record);
    }

    protected void execAsyncOutput(MIRecord record) {
	dispatch(record);
    }

    protected void notifyAsyncOutput(MIRecord record) {
	dispatch(record);
    }


    protected void consoleStreamOutput(MIRecord record) {
	cmdManager.logConsole(record.stream());
    }

    protected void targetStreamOutput(MIRecord record) {
    }

    protected void logStreamOutput(MIRecord record) {
	cmdManager.logStream(record.stream());
    }


    protected void prompt() {
    }

    protected void connectionEstablished() {
    }

    protected void errorBadLine(String data) {
	cmdManager.echo(String.format("unrecognized line: %s\r", data)); // NOI18N
    }
    
    protected void clearMessages() {
        cmdManager.clearMessages();
    }
    
    public void setIdleHandler(Runnable handler) {
        cmdManager.setIdleHandler(handler);
    }

    public String getLogger() {
        return gdbLogger.getFilename();
    }
}
