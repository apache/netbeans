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
package org.netbeans.modules.dlight.terminal.action;

import org.netbeans.modules.dlight.terminal.ui.TerminalContainerTopComponent;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Voskresensky
 */
@ActionID(id = "LocalTerminalAction", category = "Window")
@ActionRegistration(iconInMenu = true, displayName = "#LocalTerminalShortDescr", iconBase = "org/netbeans/modules/dlight/terminal/action/local_term.png")
@ActionReference(path = TerminalAction.TERMINAL_ACTIONS_PATH, name = "org-netbeans-modules-dlight-terminal-action-LocalTerminalAction", position = 100)
public final class LocalTerminalAction extends TerminalAction {

    public LocalTerminalAction() {
        super(TerminalContainerTopComponent.LOCAL_TERMINAL_PREFIX + "Action", NbBundle.getMessage(LocalTerminalAction.class, "LocalTerminalShortDescr"), // NOI18N
                ImageUtilities.loadImageIcon("org/netbeans/modules/dlight/terminal/action/local_term.png", false)); // NOI18N
    }

    @Override
    protected ExecutionEnvironment getEnvironment() {
        return ExecutionEnvironmentFactory.getLocal();
    }
}
