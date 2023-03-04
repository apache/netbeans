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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.modules.terminal.ioprovider.Terminal;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 *
 * @author igromov
 */
@ActionID(id = ActionFactory.WRAP_ACTION_ID, category = ActionFactory.CATEGORY)
@ActionRegistration(displayName = "#CTL_Wrap", lazy = true) //NOI18N
@ActionReferences({
    @ActionReference(path = ActionFactory.ACTIONS_PATH, name = "Wrap") //NOI18N
})
public class WrapAction extends TerminalAction implements Presenter.Popup {

    private static final String BOOLEAN_STATE_ACTION_KEY = "boolean_state_action";	// NOI18N
    private static final String BOOLEAN_STATE_ENABLED_KEY = "boolean_state_enabled";	// NOI18N

    public WrapAction(Terminal context) {
	super(context);	// NOI18N
	// LATER KeyStroke accelerator = Utilities.stringToKey("A-R");
	putValue(NAME, getMessage("CTL_Wrap")); //NOI18N
	putValue(BOOLEAN_STATE_ACTION_KEY, true);
    }

    @Override
    public void performAction() {
	Terminal terminal = getTerminal();
	Term term = terminal.term();

	if (!terminal.isEnabled()) {
	    return;
	}
	boolean hs = term.isHorizontallyScrollable();
	term.setHorizontallyScrollable(!hs);
    }

    @Override
    public Object getValue(String key) {
	if (key.equals(BOOLEAN_STATE_ENABLED_KEY)) {
	    Terminal terminal = getTerminal();
	    if (terminal == null) {
		return false;
	    }
	    Term term = terminal.term();
	    return !term.isHorizontallyScrollable();
	} else {
	    return super.getValue(key);
	}
    }

    // --------------------------------------------- 
    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
	return new WrapAction(actionContext.lookup(Terminal.class));
    }

    @Override
    public JMenuItem getPopupPresenter() {
	JCheckBoxMenuItem item = new JCheckBoxMenuItem(this);
	item.setSelected((Boolean) this.getValue(BOOLEAN_STATE_ENABLED_KEY));
	return item;
    }
}
