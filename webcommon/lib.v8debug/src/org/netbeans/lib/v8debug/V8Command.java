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
package org.netbeans.lib.v8debug;

/**
 * Debugger command.
 * 
 * @author Martin Entlicher
 */
public enum V8Command {
    
    Continue,
    Suspend,
    Evaluate,
    Lookup,
    References,
    Backtrace,
    Frame,
    Scope,
    Scopes,
    Scripts,
    Source,
    Setbreakpoint,
    Changebreakpoint,
    Clearbreakpoint,
    Clearbreakpointgroup,
    Setexceptionbreak,
    Threads,
    Flags,
    V8flags,
    Version,
    Disconnect,
    Gc,
    Listbreakpoints,
    SetVariableValue,
    Restartframe,
    Changelive;
    
    @Override
    public String toString() {
        String commandName = super.toString();
        commandName = Character.toLowerCase(commandName.charAt(0)) + commandName.substring(1);
        return commandName;
    }
    
    public static V8Command fromString(String commandName) {
        commandName = Character.toUpperCase(commandName.charAt(0)) + commandName.substring(1);
        return V8Command.valueOf(commandName);
    }
        
}
