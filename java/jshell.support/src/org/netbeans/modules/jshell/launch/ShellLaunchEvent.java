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

import java.util.EventObject;

/**
 * Event informing on Shell launch events.
 * 
 * @author sdedic
 */
public final class ShellLaunchEvent extends EventObject {
    private JShellConnection connection;
    private ShellAgent       agent;
    private boolean          remoteClose;

    /*
    Enable after refactoring
    ShellLaunchEvent(ShellLaunchManager mgr, JShellConnection source) {
        this(mgr, source, false);
    }
     */
    
    ShellLaunchEvent(ShellLaunchManager mgr, JShellConnection source, boolean remoteClose) {
        super(mgr);
        this.connection = source;
        this.remoteClose = remoteClose;
    }


    ShellLaunchEvent(ShellLaunchManager mgr, ShellAgent agent) {
        super(mgr);
        this.agent = agent;
    }
    
    /**
     * True, if the connection was shut down remotely.
     * @return true for remote-initiated close.
     */
    public boolean isRemoteClose() {
        return remoteClose;
    }
    
    public ShellAgent       getAgent() {
        return agent;
    }
    
    public JShellConnection getSource() {
        return (JShellConnection)super.getSource();
    }

    public JShellConnection getConnection() {
        return connection;
    }
}
