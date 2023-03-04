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

package org.netbeans.lib.ddl;

/** Command is not supported by system.
* System is not able to locate appropriate resources to create a command.
* It can't find relevant section in definition file, can't allocate or
* initialize command object.
*
* @author Slavek Psenicka
*/
public class CommandNotSupportedException extends Exception
{
    /** Unsuccessfull command */
    private String cmd;

    static final long serialVersionUID =3121142575910991422L;
    /** Creates new exception
    * @param command The text describing the exception
    */
    public CommandNotSupportedException (String command) {
        super ();
        cmd = command;
    }

    /** Creates new exception with text specified string.
    * @param command Executed command
    * @param desc The text describing the exception
    */
    public CommandNotSupportedException (String command, String desc) {
        super (desc);
        cmd = command;
    }

    /** Returns executed command */
    public String getCommand()
    {
        return cmd;
    }
}

/*
 * <<Log>>
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
 * $
 */
