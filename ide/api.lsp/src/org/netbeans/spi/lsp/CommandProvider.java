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
package org.netbeans.spi.lsp;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * A provider for commands that a language client can run in the language server.
 *
 * @since 1.18
 */
public interface CommandProvider {

    /**
     * Names of commands supported by this provider.
     *
     * @return names of commands supported by this provider.
     */
    public Set<String> getCommands();

    /**
     * Run the specified command with the specified arguments.
     *
     * @param command the command to run
     * @param arguments the command-specific arguments
     * @return a result of the command
     */
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments);

}
