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

package org.netbeans.lib.ddl.util;

import java.util.*;
import org.netbeans.lib.ddl.*;

/**
* Command buffer used to execute a bunch of commands. Main advantages of using
* buffer is:
* - Optimized connection handling. Buffer opens JDBC connection before executing
* of first command and closes it after a last one. It's safely then manually
* handling connection and better then leaving commands open and close connection
* for each comand separately.
* - Exception handler. You can assign an exception handler to buffer. When any
* error occures during the execution, this handler catches it and lets user to
* decide if continue or not (when you're dropping nonexisting table, you probably
* would like to continue).
* - Debgging. You can set up debug mode and buffer will print each command to
* System.out before execution.
*
* @author   Slavek Psenicka
*/
public class CommandBuffer
{
    /** Buffered items */
    Vector commands;

    /** Debug mode */
    boolean debugmode;

    /** Execution command with some exception */
    boolean executionWithException;

    /** Adds command to buffer
    * @param cmd Command to add.
    */
    public void add(DDLCommand cmd)
    {
        if (commands == null) commands = new Vector();
        commands.add(cmd);
    }

    /** Returns true if debugging mode is on.
    * You can set up debug mode and buffer will print each command to
    * System.out before execution.
    */
    public boolean isDebugMode()
    {
        return debugmode;
    }

    /** Sets debug mode on/off.
    * You can set up debug mode and buffer will print each command to
    * System.out before execution.
    * @param flag true = debugging enabled
    */
    public void setDebugMode(boolean flag)
    {
        debugmode = flag;
    }

    /** Returns a string with string representation of all commands in buffer
    */
    public String getCommands()
    throws DDLException
    {
        String cmds = "";
        Enumeration cmd_e = commands.elements();
        while (cmd_e.hasMoreElements()) {
            DDLCommand e_cmd = (DDLCommand)cmd_e.nextElement();
            cmds = cmds + e_cmd.getCommand() + "\n";
        }

        return cmds;
    }

    /** Executes commnds in buffer.
    * Buffer opens JDBC connection before executing (if isn't already open)
    * of first command and closes it after a last one. It's safely then manually
    * handling connection and better then leaving commands open and close connection
    * for each comand separately. You can also assign an exception handler to buffer.
    * When any error occures during the execution, this handler catches it and lets user to
    * decide if continue or not (when you're dropping nonexisting table, you probably
    * would like to continue).
    */
    public void execute()
    throws DDLException
    {
        boolean opencon = false;
        executionWithException = false;
        DatabaseSpecification spec = null;
        Enumeration cmd_e = commands.elements();
        while (cmd_e.hasMoreElements()) {
            DDLCommand e_cmd = (DDLCommand)cmd_e.nextElement();
            if (spec == null) {
                spec = e_cmd.getSpecification();
                if (spec.getJDBCConnection() == null) {
                    opencon = true;
                    spec.openJDBCConnection();
                }
            }
            if (debugmode) System.out.println(e_cmd);
            e_cmd.execute();
            executionWithException = e_cmd.wasException();
        }

        if (opencon) spec.closeJDBCConnection();
    }

    /** information about appearance some exception in the last execute a bunch of commands */
    public boolean wasException() {
        return executionWithException;
    }
}
