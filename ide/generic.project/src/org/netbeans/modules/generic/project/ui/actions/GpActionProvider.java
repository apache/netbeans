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

package org.netbeans.modules.generic.project.ui.actions;

import org.netbeans.modules.generic.project.GenericProject;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.util.Lookup;

/**
 * Advertises the default project operations of generic projects so
 * that the standard project actions ("Rename...", "Move...",
 * "Copy...", "Delete") become available in the project's popup
 * menu.
 *
 * <p>The operations are delegated to the default implementations,
 * which show the standard confirmation dialogs and update the
 * project through the {@link GpProjectOperations} registered in
 * the lookup.
 */
public final class GpActionProvider implements ActionProvider {

    private final GenericProject prj;

    public GpActionProvider(GenericProject prj) {
        this.prj = prj;
    }

    @Override
    public String[] getSupportedActions() {
        return new String[] {
            ActionProvider.COMMAND_RENAME,
            ActionProvider.COMMAND_MOVE,
            ActionProvider.COMMAND_COPY,
            ActionProvider.COMMAND_DELETE,
        };
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        switch (command) {
            case ActionProvider.COMMAND_RENAME -> DefaultProjectOperations.performDefaultRenameOperation(prj, null);
            case ActionProvider.COMMAND_MOVE -> DefaultProjectOperations.performDefaultMoveOperation(prj);
            case ActionProvider.COMMAND_COPY -> DefaultProjectOperations.performDefaultCopyOperation(prj);
            case ActionProvider.COMMAND_DELETE -> DefaultProjectOperations.performDefaultDeleteOperation(prj);
            default -> throw new IllegalArgumentException(command);
        }
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return switch (command) {
            case ActionProvider.COMMAND_RENAME -> ProjectOperations.isMoveOperationSupported(prj);
            case ActionProvider.COMMAND_MOVE -> ProjectOperations.isMoveOperationSupported(prj);
            case ActionProvider.COMMAND_COPY -> ProjectOperations.isCopyOperationSupported(prj);
            case ActionProvider.COMMAND_DELETE -> ProjectOperations.isDeleteOperationSupported(prj);
            default -> throw new IllegalArgumentException(command);
        };
    }

}