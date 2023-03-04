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

import java.util.EventObject;
import jdk.jshell.JShell;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.jshell.support.ShellSession;

/**
 * Basic JShell state event.
 *
 * @author sdedic
 */
public final class ShellEvent extends EventObject {
    private ShellSession    session;
    private ShellSession    replacedSession;
    private ShellStatus     status;
    private boolean         remote;
    private JShell          engine;
    
    ShellEvent(JShellEnvironment source) {
        super(source);
    }
    
    ShellEvent(JShellEnvironment source, ShellSession session, ShellStatus status, boolean remote) {
        super(source);
        this.session = session;
        this.status = status;
        this.remote = remote;
    }
    
    ShellEvent(JShellEnvironment source, ShellSession session, ShellSession replaced) {
        super(source);
        this.session = session;
        this.replacedSession = replaced;
    }
    
    public boolean isRemoteEvent() {
        return remote;
    }

    /**
     * The affected session. May be {@code null}, if the whole environment/process is affected.
     * @return the session, or {@code null}
     */
    public ShellSession getSession() {
        return session;
    }

    /**
     * The former session, if the JShell was restarted. Will report the <b>current session</b>
     * in the case the JShell engine has been recycled within the same session.
     * @return the former session or {@code null} for first start
     */
    public @CheckForNull ShellSession getReplacedSession() {
        return replacedSession;
    }
    
    /**
     * @return the affected environment
     */
    public @NonNull JShellEnvironment getEnvironment() {
        return (JShellEnvironment)getSource();
    }
    
    public JShell getEngine() {
        return session.getShell();
    }
    
    public boolean isEngineRestart() {
        return session == replacedSession;
    }
}
