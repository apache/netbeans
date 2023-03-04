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
package org.netbeans.modules.terminal.nb.actions;

import javax.swing.Action;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.modules.terminal.ioprovider.Terminal;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;

/**
 *
 * @author igromov
 */
@ActionID(id = ActionFactory.CLEAR_ACTION_ID, category = ActionFactory.CATEGORY)
@ActionRegistration(displayName = "#CTL_Clear", lazy = true) //NOI18N
@ActionReferences({
    @ActionReference(path = ActionFactory.ACTIONS_PATH, name = "ClearAction"), //NOI18N
    /* Conflicts with CS-L in collab.ui
    @ActionReference(path = "Shortcuts", name = "CS-L")		//NOI18N
    */
})
public class ClearAction extends TerminalAction {

    public ClearAction(Terminal context) {
	super(context);
	putValue(NAME, getMessage("CTL_Clear")); //NOI18N
	/* OLD
	 KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_E,
	 InputEvent.ALT_MASK);
	 putValue(ACCELERATOR_KEY, accelerator);
	 */
    }

    @Override
    public void performAction() {
	Terminal terminal = getTerminal();
	Term term = terminal.term();

	if (!terminal.isEnabled()) {
	    return;
	}
	term.clearHistory();
    }

    // --------------------------------------------- 
    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
	return new ClearAction(actionContext.lookup(Terminal.class));
    }
}
