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

import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;
import org.openide.util.Mutex;


public abstract class Command {

    protected final ClientSideProject project;


    public Command(ClientSideProject project) {
        assert project != null;
        this.project = project;
    }

    public abstract String getCommandId();

    abstract boolean isActionEnabledInternal(Lookup context);

    abstract void invokeActionInternal(Lookup context);

    public final boolean isActionEnabled(Lookup context) {
        if (project.isBroken(false)) {
            // will be handled in invokeAction(), see below
            return true;
        }
        return isActionEnabledInternal(context);
    }

    public final void invokeAction(Lookup context, AtomicBoolean warnUser) {
        if (!validateInvokeAction(context, warnUser)) {
            return;
        }
        invokeActionInternal(context);
    }

    protected boolean validateInvokeAction(Lookup context, AtomicBoolean warnUser) {
        return !project.isBroken(warnUser.compareAndSet(true, false));
    }

    protected static boolean isSupportedAction(String command, ActionProvider actionProvider) {
        for (String action : actionProvider.getSupportedActions()) {
            if (command.equals(action)) {
                return true;
            }
        }
        return false;
    }

    protected static void runInEventThread(Runnable task) {
        Mutex.EVENT.readAccess(task);
    }

}
