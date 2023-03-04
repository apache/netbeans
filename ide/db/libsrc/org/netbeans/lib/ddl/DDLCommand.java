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

/**
* Interface of database action command. Instances should remember connection
* information of DatabaseSpecification and use it in execute() method.
*
* @author Slavek Psenicka
*/
public interface DDLCommand
{
    /** Returns specification (DatabaseSpecification) for this command */
    public DatabaseSpecification getSpecification();

    /** Returns name of modified object */
    public String getObjectName();

    /** Sets name to be used in command
    * @param name New name
    */
    public void setObjectName(String name);

    /** Executes command */
    public void execute() throws DDLException;

    /**
    * Returns full string representation of command. This string needs no 
    * formatting and could be used directly as argument of executeUpdate() 
    * command. Throws DDLException if format is not specified or CommandFormatter
    * can't format it (it uses MapFormat to process entire lines and can solve []
    * enclosed expressions as optional.
    */
    public String getCommand()
    throws DDLException;
    
    /** information about appearance some exception in the last execute a bunch of commands */
    public boolean wasException();

}
