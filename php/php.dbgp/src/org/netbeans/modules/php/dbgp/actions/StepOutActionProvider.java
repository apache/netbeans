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
import org.netbeans.modules.php.dbgp.packets.StepOutCommand;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * @author ads
 *
 */
public class StepOutActionProvider extends AbstractActionProvider {

    public StepOutActionProvider(ContextProvider contextProvider) {
        super(contextProvider);
    }

    @Override
    public void doAction(Object action) {
        DebugSession session = getSession();
        if (session == null) {
            return;
        }
        hideSuspendAnnotations();
        StepOutCommand command = new StepOutCommand(session.getTransactionId());
        session.sendCommandLater(command);
    }

    @Override
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_STEP_OUT);
    }

}
