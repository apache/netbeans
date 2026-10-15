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

/**
 * An action over the code.
 *
 * @since 1.3
 */
public class CodeAction {

    private final String title;
    private final String kind;
    private final Command command;
    private final WorkspaceEdit edit;

    /**
     * Construct the {@code CodeAction}.
     *
     * @param title the name of the action
     * @param command the command that should be invoked
     */
    public CodeAction(String title, Command command) {
        this(title, command, null);
    }

    /**
     * Construct the {@code CodeAction}.
     *
     * @param title the name of the action
     * @param edit the {@code WorkspaceEdit} that should be performed
     */
    public CodeAction(String title, WorkspaceEdit edit) {
        this(title, null, edit);
    }

    /**
     * Construct the {@code CodeAction}.
     *
     * @param title the name of the action
     * @param command the command that should be invoked
     * @param edit the {@code WorkspaceEdit} that should be performed
     */
    public CodeAction(String title, Command command, WorkspaceEdit edit) {
        this(title, null, command, edit);
    }

    /**
     * Construct the {@code CodeAction}.
     *
     * @param title the name of the action
     * @param kind optional kind of the action
     * @param command the command that should be invoked
     * @param edit the {@code WorkspaceEdit} that should be performed
     * @since 1.23
     */
    public CodeAction(String title, String kind, Command command, WorkspaceEdit edit) {
        this.title = title;
        this.kind = kind;
        this.command = command;
        this.edit = edit;
    }

    /**
     * Return the name of the action.
     *
     * @return the name of the action
     */
    public String getTitle() {
        return title;
    }

    /**
     * Return the kind of the action.
     *
     * @return the kind of the action
     * @since 1.23
     */
    public String getKind() {
        return kind;
    }

    /**
     * Return the command of the action.
     *
     * @return the command of the action
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Return the edit associated with the action.
     *
     * @return the edit associated with the action.
     */
    public WorkspaceEdit getEdit() {
        return edit;
    }
}
