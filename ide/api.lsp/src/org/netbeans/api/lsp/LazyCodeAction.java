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

import java.util.function.Supplier;

/**
 * An action over the code with lazy edit computation.
 *
 * @since 1.18
 */
public final class LazyCodeAction extends CodeAction {

    private final Supplier<WorkspaceEdit> lazyEdit;

    /**
     * Constructs the {@code LazyCodeAction}.
     *
     * @param title the name of the action
     * @param lazyEdit the lazily computed {@code WorkspaceEdit} that should be performed
     */
    public LazyCodeAction(String title, Supplier<WorkspaceEdit> lazyEdit) {
        super(title, null, null);
        this.lazyEdit = lazyEdit;
    }

    /**
     * Constructs the {@code LazyCodeAction}.
     *
     * @param title the name of the action
     * @param command the command that should be invoked
     * @param lazyEdit the lazily computed {@code WorkspaceEdit} that should be performed
     */
    public LazyCodeAction(String title, Command command, Supplier<WorkspaceEdit> lazyEdit) {
        super(title, command, null);
        this.lazyEdit = lazyEdit;
    }

    /**
     * Constructs the {@code LazyCodeAction}.
     *
     * @param title the name of the action
     * @param kind optional kind of the action
     * @param command the command that should be invoked
     * @param lazyEdit the lazily computed {@code WorkspaceEdit} that should be performed
     * @since 1.23
     */
    public LazyCodeAction(String title, String kind, Command command, Supplier<WorkspaceEdit> lazyEdit) {
        super(title, kind, command, null);
        this.lazyEdit = lazyEdit;
    }

    @Override
    public WorkspaceEdit getEdit() {
        try {
            return lazyEdit.get();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Returns the the lazily computed edit associated with the action.
     *
     * @return the the lazily computed edit associated with the action.
     */
    public Supplier<WorkspaceEdit> getLazyEdit() {
        return lazyEdit;
    }
}
