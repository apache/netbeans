/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
