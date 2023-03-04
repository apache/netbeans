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
package org.netbeans.modules.web.clientproject.ui.action.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.util.FileUtilities;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

// unfortunately, this class represents Cordova as well :/
public class BrowserCommand extends Command {

    private final String commandId;


    public BrowserCommand(ClientSideProject project, String commandId) {
        super(project);
        assert commandId != null;
        this.commandId = commandId;
    }

    @Override
    public String getCommandId() {
        return commandId;
    }

    @Override
    boolean isActionEnabledInternal(Lookup context) {
        if (project.isJsLibrary()) {
            return false;
        }
        if (!project.isRunBrowser()) {
            return false;
        }
        if (isFileCommand()
                && isJsFileCommand(context)) {
            return false;
        }
        ActionProvider actionProvider = getBrowserActionProvider();
        if (actionProvider != null
                && isSupportedAction(getCommandId(), actionProvider)) {
            return actionProvider.isActionEnabled(getCommandId(), context);
        }
        return false;
    }

    @Override
    void invokeActionInternal(final Lookup context) {
        if (project.isJsLibrary()) {
            return;
        }
        if (!project.isRunBrowser()) {
            return;
        }
        if (isFileCommand()
                && isJsFileCommand(context)) {
            return;
        }
        project.logBrowserUsage();
        final ActionProvider actionProvider = getBrowserActionProvider();
        if (actionProvider != null) {
            assert isSupportedAction(getCommandId(), actionProvider) : getCommandId() + " :: " + actionProvider;
            if (actionProvider.isActionEnabled(commandId, context)) {
                runInEventThread(new Runnable() {
                    @Override
                    public void run() {
                        actionProvider.invokeAction(getCommandId(), context);
                    }
                });
            }
        }
    }

    public List<String> getSupportedActions() {
        ActionProvider actionProvider = getBrowserActionProvider();
        if (actionProvider == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(actionProvider.getSupportedActions());
    }

    @CheckForNull
    private ActionProvider getBrowserActionProvider() {
        ClientProjectEnhancedBrowserImplementation browserImplementation = project.getEnhancedBrowserImpl();
        if (browserImplementation != null) {
            return browserImplementation.getActionProvider();
        }
        return null;
    }

    private boolean isFileCommand() {
        switch (commandId) {
            case ActionProvider.COMMAND_RUN_SINGLE:
            case ActionProvider.COMMAND_DEBUG_SINGLE:
                return true;
            default:
                return false;
        }
    }

    private boolean isJsFileCommand(Lookup context) {
        FileObject fo = context.lookup(FileObject.class);
        return fo != null
                && FileUtilities.isJavaScriptFile(fo);
    }

}
