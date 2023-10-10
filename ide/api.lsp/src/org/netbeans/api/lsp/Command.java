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
package org.netbeans.api.lsp;

import java.util.List;

/**
 * A command. The exact list of known commands depends on
 * the implementation of the server.
 *
 * @since 1.3
 */
public class Command {

    private final String title;
    private final String command;
    private final List<Object> arguments;

    /**
     * Construct a new {@code Command}.
     *
     * @param title the title of the command
     * @param command the code of the command that should be invoked
     */
    public Command(String title, String command) {
        this(title, command, null);
    }

    /**
     * Construct a new {@code Command}.
     *
     * @param title the title of the command
     * @param command the code of the command that should be invoked
     * @param arguments command arguments
     */
    public Command(String title, String command, List<Object> arguments) {
        this.title = title;
        this.command = command;
        this.arguments = arguments;
    }

    /**
     * The title of the command.
     *
     * @return the title of the command
     */
    public String getTitle() {
        return title;
    }

    /**
     * The code of the command that should be invoked.
     *
     * @return the code of the command that should be invoked
     */
    public String getCommand() {
        return command;
    }

    /**
     * The arguments of the command that should be invoked.
     *
     * @return the arguments of the command that should be invoked
     */
    public List<Object> getArguments() {
        return arguments;
    }

}
