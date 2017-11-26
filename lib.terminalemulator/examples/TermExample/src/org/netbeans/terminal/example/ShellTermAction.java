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
package org.netbeans.terminal.example;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;

/**
 * Action which starts a shell under a Term component.
 */
public class ShellTermAction extends AbstractAction {

    public ShellTermAction() {
        super(NbBundle.getMessage(ShellTermAction.class, "CTL_ShellTermAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(TermTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {
	Config config = Config.getShellConfig();
	final TerminalIOProviderSupport support = new TerminalIOProviderSupport(config);

	IOContainer container = TerminalIOProviderSupport.getIOContainer(config);
	container = null;	// work with default IO container

	IOProvider iop = TerminalIOProviderSupport.getIOProvider();
	support.executeRichCommand(iop, container);
    }
}
