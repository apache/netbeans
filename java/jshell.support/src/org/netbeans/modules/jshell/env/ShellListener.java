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

import java.util.EventListener;

/**
 * Listener which receives basic state events from running Shell. Attach the listener
 * to {@link JShellEnvironment}.
 * 
 * @author sdedic
 */
public interface ShellListener extends EventListener {
    /**
     * JShellEnvironment was crated and put alive. This event is the only one
     * broadcasted to listeners registered through 
     * @param ev 
     */
    public void shellCreated(ShellEvent ev);
    
    /**
     * Fired when the shell was started, or restarted.
     * The JShellEnvironment instance will already contain a new instance. The event will
     * fire also in case the ShellSession has recycled the JShell engine.
     * of ShellSession.
     * @param ev 
     */
    public void shellStarted(ShellEvent ev);
    
    /**
     * The status of the shell has been changed
     * @param ev 
     */
    public void shellStatusChanged(ShellEvent ev);
    
    /**
     * The entire JShellEnvironment has been shut down.
     * @param ev 
     */
    public void shellShutdown(ShellEvent ev);
    
    /**
     * Fired when shell settings change. This includes library changes, project
     * change, ... 
     * @param ev 
     */
    public void shellSettingsChanged(ShellEvent ev);
}
