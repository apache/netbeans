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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.netbeans.modules.versionvault.client;

import java.util.*;
import java.io.File;

import org.netbeans.modules.versionvault.*;
import org.openide.util.Exceptions;

/**
 * Encapsulates a command given to a ClearCase client. 
 * 
 * @author Maros Sandor
 */
public abstract class ClearcaseCommand implements NotificationListener {
        
    private final Set<NotificationListener> listeners = new HashSet<NotificationListener>(1);

    protected File commandWorkingDirectory;
    
    private final List<String> cmdOutput = new ArrayList<String>(10);
    private final List<String> cmdError = new ArrayList<String>(10);

    private String stringValue;
    
    /**
     * If the command thrown an execption, this is it.
     */
    private Exception thrownException;

    /**
     * True if the command produced errors (messages in error stream), false otherwise.
     */
    private boolean hasFailed;

    /**
     * Internal check mechanism to prevent commands reuse.
     */
    private boolean commandExecuted;

    protected ClearcaseCommand() {
    }

    protected ClearcaseCommand(NotificationListener... listeners) {
        this.listeners.addAll(Arrays.asList(listeners));        
    }

    /**
     * Prepare the command: fill list of arguments to cleartool and compute commandWorkingDirectory.
     * 
     * @param arguments 
     * @throws ClearcaseException
     */
    public abstract void prepareCommand(Arguments arguments) throws ClearcaseException;

    public File getCommandWorkingDirectory() {
        return commandWorkingDirectory;
    }

    public void commandStarted() {
        assert !commandExecuted : "Command re-use is not supported";
        commandExecuted = true;
        for (NotificationListener listener : listeners) {
            listener.commandStarted();
        }
    }

    public void outputText(String line) {
        cmdOutput.add(line);
        for (NotificationListener listener : listeners) {
            listener.outputText(line);
        }
    }

    public void errorText(String line) {
        cmdError.add(line);
        if (isErrorMessage(line)) hasFailed = true;
        for (NotificationListener listener : listeners) {
            listener.errorText(line);
        }
    }

    public void commandFinished() {
        for (NotificationListener listener : listeners) {
            listener.commandFinished();
        }
    }
    
    public boolean hasFailed() {
        return hasFailed;
    }

    public List<String> getCmdOutput() {
        return cmdOutput;
    }

    public List<String> getCmdError() {
        return cmdError;
    }

    public void setException(Exception e) {
        thrownException = e;
    }

    public Exception getThrownException() {
        return thrownException;
    }

    /**
     * Tests if the given message printed to the error stream indicates an actual command error.
     * Commands sometimes print diagnostic messages to error stream which are not errors and should not be reported as such. 
     * 
     * @param s a message printed to the output stream
     * @return true if the message is an error that should be reported, false otherwise
     */
    protected boolean isErrorMessage(String s) {
        return true;
    }

    @Override
    public String toString() {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("\"");
            sb.append(getStringCommand());
            sb.append("\"");
            return sb.toString();
        } catch (ClearcaseException ex) {
            return super.toString();
        }
    }

    public String getStringCommand() throws ClearcaseException {
        if(stringValue == null) {
            Arguments args = new Arguments();
            prepareCommand(args);
            stringValue = toString(args).toString();
        }
        return stringValue;
    }

    private static StringBuilder toString(Arguments args) {
        StringBuilder cmd = new StringBuilder(100);
        for (String arg : args) {
            cmd.append(arg);
            cmd.append(' ');
        }
        cmd.delete(cmd.length() - 1, cmd.length());
        return cmd;
    }    
}
