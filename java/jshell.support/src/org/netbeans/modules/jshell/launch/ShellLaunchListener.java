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
package org.netbeans.modules.jshell.launch;

/**
 * Receives lifecycle events for the remote agent communication. The listener is informed
 * when the remote starts, becomes available, closes a connection or the entire remote
 * agent shuts down (assuming the entire process has been shut down, or the network broke).
 * 
 * @author sdedic
 */
public interface ShellLaunchListener {
    /**
     * Called when the connection to the target machine is initiated, for example
     * when debugger reports machine attach.
     * @param ev 
     */
    public void connectionInitiated(ShellLaunchEvent ev);
    
    /**
     * The handshake with the target VM has succeeded, or failed. The 'agent' field
     * is valid.
     * @param ev 
     */
    public void handshakeCompleted(ShellLaunchEvent ev);
    
    /**
     * Called when the connection has been closed.  The connection and
     * agent fields are valid. Agent may be still live, so a new Connection
     * can be opened.
     * @param ev 
     */
    public void connectionClosed(ShellLaunchEvent ev);
    
    /**
     * The VM agent has been destroyed. Perhaps the entire VM went down or something.
     * @param ev 
     */
    public void agentDestroyed(ShellLaunchEvent ev);
}
