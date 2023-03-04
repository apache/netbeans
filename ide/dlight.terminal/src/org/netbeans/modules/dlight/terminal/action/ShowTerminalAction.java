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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.dlight.terminal.ui.TerminalContainerTopComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 *
 * @author Vladimir Voskresensky
 */
@ActionID(id = "ShowTerminalTCAction", category = "Window")
@ActionRegistration(iconInMenu = true, displayName = "#CTL_ShowTerminalAction", iconBase = "org/netbeans/modules/dlight/terminal/action/local_term.png")
@ActionReference(path = "Menu/Window/Tools", name = "org-netbeans-modules-dlight-terminal-action-ShowTerminal", position = 900)
public class ShowTerminalAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        final TerminalContainerTopComponent instance = TerminalContainerTopComponent.findInstance();
        instance.open();
        instance.requestActive();
    }
}
