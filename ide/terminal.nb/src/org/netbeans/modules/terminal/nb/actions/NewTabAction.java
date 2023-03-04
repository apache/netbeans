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

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.netbeans.modules.terminal.ioprovider.Terminal;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.util.Lookup;

@ActionID(id = ActionFactory.NEW_TAB_ACTION_ID, category = ActionFactory.CATEGORY)
@ActionRegistration(displayName = "#CTL_NewTab", lazy = true) //NOI18N
@ActionReferences({
    @ActionReference(path = ActionFactory.ACTIONS_PATH, name = "NewTabAction"), //NOI18N
    @ActionReference(path = "Shortcuts", name = "DAS-T") //NOI18N
})
public class NewTabAction extends TerminalAction {

    public NewTabAction(Terminal context) {
        super(context);
    }

    @Override
    protected void performAction() {
        Action forID = Actions.forID("Window", "LocalTerminalAction"); //NOI18N
        forID.actionPerformed(new ActionEvent(getTerminal(), ActionEvent.ACTION_PERFORMED, null));
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new NewTabAction(actionContext.lookup(Terminal.class));
    }
}
