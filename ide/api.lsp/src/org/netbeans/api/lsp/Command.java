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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Represents a reference to a command.
 *
 * @author Dusan Balek
 */
public final class Command {

    private final String title;
    private final String command;
    private final List<Object> arguments;

    public Command(@NonNull String title, @NonNull String command, @NullAllowed List<Object> arguments) {
        this.title = title;
        this.command = command;
        this.arguments = arguments;
    }

    /**
     * Title of the command, like `save`.
     */
    @NonNull
    public String getTitle() {
        return title;
    }

    /**
     * The identifier of the actual command handler.
     */
    @NonNull
    public String getCommand() {
        return command;
    }

    /**
     * Arguments that the command handler should be invoked with. Note that actual
     * objects passed should be carried over LSP.
     */
    @CheckForNull
    public List<Object> getArguments() {
        return arguments;
    }
}
