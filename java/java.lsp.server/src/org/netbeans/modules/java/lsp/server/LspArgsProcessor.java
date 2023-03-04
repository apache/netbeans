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
package org.netbeans.modules.java.lsp.server;

import java.io.IOException;

import org.netbeans.api.sendopts.CommandException;
import org.netbeans.modules.java.lsp.server.debugging.Debugger;
import org.netbeans.modules.java.lsp.server.protocol.Server;

import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;
import org.openide.util.NbBundle.Messages;

public final class LspArgsProcessor implements ArgsProcessor {

    @Arg(longName="start-java-language-server", defaultValue = "")
    @Description(shortDescription="#DESC_StartJavaLanguageServer")
    @Messages("DESC_StartJavaLanguageServer=Starts the Java Language Server")
    public String lsPort;

    @Arg(longName="start-java-debug-adapter-server")
    @Description(shortDescription="#DESC_StartJavaDebugAdapterServer")
    @Messages("DESC_StartJavaDebugAdapterServer=Starts the Java Debug Adapter Server")
    public String debugPort;

    @Override
    public void process(Env env) throws CommandException {
        LspSession session = new LspSession();
        if (lsPort != null) {
            try {
                ConnectionSpec connectTo = ConnectionSpec.parse(lsPort);
                connectTo.prepare(
                    "Java Language Server",
                    env.getInputStream(),
                    env.getOutputStream(),
                    session,
                    LspSession::setLspServer,
                    Server::launchServer
                );
            } catch (IOException ex) {
                throw (CommandException) new CommandException(554).initCause(ex);
            }
        }
        if (debugPort != null) {
            try {
                ConnectionSpec connectTo = ConnectionSpec.parse(debugPort);
                connectTo.prepare(
                    "Java Debug Server Adapter",
                    env.getInputStream(),
                    env.getOutputStream(),
                    session,
                    LspSession::setDapServer,
                    Debugger::startDebugger
                );
            } catch (IOException ex) {
                throw (CommandException) new CommandException(554).initCause(ex);
            }
        }
    }
}

