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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
