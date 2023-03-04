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
package org.netbeans.modules.jshell.env;

/**
 * Status of a JShellEnvironment. The status changes during the JShellEnvironemnt's
 * lifecycle as follows:
 * <code><pre>
 * INIT - { STARTING - { READY - EXECUTE }* - DISCONNECTED? }+ - SHUTDOWN
 * </pre></code>
 * The shell may go down to SHUTDOWN state at any time, which is the terminal one.
 * 
 * @author sdedic
 */
public enum ShellStatus {
    /**
     * Initial state, before the ShellSession initializes. JShellEnvironment is just allocated.
     * Shells in this state should not be observed / attached in the UI.
     */
    INIT, 
    
    /**
     * The startup initialization sequence is pending; the JShell is executing initial commands
     */
    STARTING, 
    
    /**
     * The shell is ready to acccept user commands
     */
    READY, 
    
    /**
     * Shell is just executing the use command or snippet
     */
    EXECUTE, 
    
    /**
     * The shell session is disconnected, but possibly can be run a new one
     */
    DISCONNECTED, 
    
    /**
     * The agent or VM has shut down; the JShellEnvironment is dead
     */
    SHUTDOWN
}
