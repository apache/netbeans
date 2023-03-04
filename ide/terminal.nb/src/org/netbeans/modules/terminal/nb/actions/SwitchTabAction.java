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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.netbeans.modules.terminal.api.ui.TerminalContainer;
import org.netbeans.modules.terminal.ioprovider.Terminal;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.windows.IOContainer;

/**
 *
 * @author igromov
 */
@ActionID(id = ActionFactory.SWITCH_TAB_ACTION_ID, category = ActionFactory.CATEGORY)
@ActionRegistration(displayName = "#CTL_SwitchTab", lazy = false) //NOI18N
@ActionReferences({
    @ActionReference(path = ActionFactory.ACTIONS_PATH, name = "SwitchTabAction"), //NOI18N
})
public class SwitchTabAction extends TerminalAction {

    public SwitchTabAction() {
        this(null);
    }

    public SwitchTabAction(Terminal context) {
        super(context);

        KeyStroke[] keyStrokes = new KeyStroke[10];
        for (int i = 0; i < 10; i++) {
            keyStrokes[i] = KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, InputEvent.ALT_MASK);
        }
        putValue(ACCELERATOR_KEY, keyStrokes);

        putValue(NAME, getMessage("CTL_SwitchTab")); //NOI18N
    }

    @Override
    protected void performAction() {
        Container container = SwingUtilities.getAncestorOfClass(TerminalContainer.class, getTerminal());
        if (container instanceof TerminalContainer) {
            TerminalContainer tc = (TerminalContainer) container;
            List<? extends Component> allTabs = tc.getAllTabs();
            try {
                int requested = Integer.parseInt(getEvent().getActionCommand());
                requested = (requested == 0)
                        ? 9
                        : requested - 1;
                if (requested >= allTabs.size() || requested < 0) {
                    return;
                }
                if (allTabs.get(requested) instanceof Terminal) {
                    Terminal terminal = (Terminal) allTabs.get(requested);
                    if (tc instanceof IOContainer.Provider) {
                        ((IOContainer.Provider) tc).select(terminal);
                    }
                }
            } catch (NumberFormatException x) {
            }
        }
    }

    // --------------------------------------------- 
    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new SwitchTabAction(actionContext.lookup(Terminal.class));
    }
}
