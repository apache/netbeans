/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.debugger.common2.debugger.io;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
class ExternalTerminalPack extends IOPack {
    private DebuggerExternalTerminal terminal = null;
    private final String termPath;
    private String slaveName = null;

    public ExternalTerminalPack(TermComponent console, String termPath, ExecutionEnvironment exEnv) {
        super(console, exEnv, false);
        this.termPath = termPath;
    }

    @Override
    public boolean start() {
        if (termPath != null) {
            terminal = new DebuggerExternalTerminal(termPath);
            slaveName = terminal.getTty();
            return slaveName != null;
        }
        return false;
    }

    @Override
    public String getSlaveName() {
        return slaveName;
    }

    @Override
    public void close() {
        if (terminal != null) {
            terminal.finish();
        }
    }
}
