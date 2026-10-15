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
package org.netbeans.modules.web.clientproject.ui.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.web.clientproject.util.FileUtilities;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "RunSourceFileAction.name.long=Run File",
    "RunSourceFileAction.name.short=Run",
})
@ActionID(id = "org.netbeans.modules.web.clientproject.ui.action.RunSourceFileAction", category = "Project")
@ActionRegistration(lazy = false, displayName = "#RunSourceFileAction.name.long", menuText = "#RunSourceFileAction.name.short",
        popupText = "#RunSourceFileAction.name.short")
@ActionReferences({
        @ActionReference(path = "Loaders/text/javascript/Actions", position = 250),
        @ActionReference(path = "Editors/text/javascript/Popup", position = 800),
        @ActionReference(path = "Loaders/application/x-typescript/Actions", position = 250),
        @ActionReference(path = "Editors/application/x-typescript/Popup", position = 800),
})
public class RunSourceFileAction extends AbstractAction implements ContextAwareAction {

    public RunSourceFileAction() {
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        // hide this action from Tools > Keymap
        putValue(Action.NAME, ""); // NOI18N
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        if (FileUtilities.lookupSourceFileOnly(actionContext) == null) {
            return this;
        }
        return FileSensitiveActions.fileCommandAction(ActionProvider.COMMAND_RUN_SINGLE, Bundle.RunSourceFileAction_name_long(), null);
    }

}
