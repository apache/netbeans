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
package org.netbeans.modules.mercurial.ui.clone;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.mercurial.ui.wizards.CloneWizardAction;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Clone action for mercurial: 
 * Clone an external repository. This invokes a wizard to determine the
 * location of the repository and the target location of the repository.
 * 
 * @author Padraig O'Briain
 */
@ActionID(id = "org.netbeans.modules.mercurial.ui.clone.CloneExternalAction", category = "Mercurial")
@ActionRegistration(displayName = "#CTL_MenuItem_CloneExternal")
@ActionReferences({
   @ActionReference(path="Versioning/Mercurial/Actions/Global", position=301)
})
@NbBundle.Messages({
    "CTL_MenuItem_CloneExternal=Clone Othe&r..."
})
public class CloneExternalAction implements ActionListener, HelpCtx.Provider {
    @Override
    public void actionPerformed(ActionEvent e) {
        HgUtils.runIfHgAvailable(new Runnable() {
            @Override
            public void run () {
                Utils.logVCSActionEvent("HG"); //NOI18N
                CloneWizardAction wiz = CloneWizardAction.getInstance();
                wiz.performAction();
            }
        });
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.mercurial.ui.clone.CloneExternalAction");
    }
}
