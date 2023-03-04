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
package org.netbeans.modules.php.dbgp.actions;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.netbeans.modules.php.dbgp.packets.BrkpntCommandBuilder;
import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand;
import org.netbeans.modules.php.dbgp.packets.RunCommand;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.text.Line;

/**
 * @author ads
 *
 */
public class RunToCursorActionProvider extends AbstractActionProvider {

    public RunToCursorActionProvider(ContextProvider contextProvider) {
        super(contextProvider);
    }

    @Override
    public void doAction(Object action) {
        SessionId id = getSessionId();
        if (id == null) {
            return;
        }
        hideSuspendAnnotations();
        DebugSession session = getSession();
        if (session == null) {
            return;
        }
        Line line = Utils.getCurrentLine();
        if (line == null) {
            return;
        }

        BrkpntSetCommand command = BrkpntCommandBuilder.buildLineBreakpoint(
                id, session.getTransactionId(),
                EditorContextDispatcher.getDefault().getCurrentFile(),
                line.getLineNumber());
        command.setTemporary(true);
        session.sendCommandLater(command);

        hideSuspendAnnotations();
        RunCommand runCommand = new RunCommand(session.getTransactionId());
        session.sendCommandLater(runCommand);
    }

    @Override
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_RUN_TO_CURSOR);
    }

}
