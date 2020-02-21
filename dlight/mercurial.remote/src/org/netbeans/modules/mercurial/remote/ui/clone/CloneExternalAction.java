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
package org.netbeans.modules.mercurial.remote.ui.clone;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.mercurial.remote.ui.wizards.CloneWizardAction;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

/**
 * Clone action for mercurial: 
 * Clone an external repository. This invokes a wizard to determine the
 * location of the repository and the target location of the repository.
 * 
 * 
 */
@ActionID(id = "org.netbeans.modules.mercurial.remote.ui.clone.CloneExternalAction", category = "MercurialRemote")
@ActionRegistration(displayName = "#CTL_MenuItem_CloneExternal")
@ActionReferences({
   @ActionReference(path="Versioning/MercurialRemote/Actions/Global", position=301)
})
@Messages({
    "CTL_MenuItem_CloneExternal=Clone Othe&r..."
})
public class CloneExternalAction implements ActionListener, HelpCtx.Provider {

    @Override
    public void actionPerformed(ActionEvent e) {
        //TODO: provide way to select FS
        //FileSystem[] fileSystems = VCSFileProxySupport.getConnectedFileSystems();
        //if (fileSystems.length == 0) {
        //    return;
        //}
        // Now use default FS
        FileSystem defaultFileSystem = VCSFileProxySupport.getDefaultFileSystem();
        if (defaultFileSystem == null) {
            return;
        }
        final VCSFileProxy root = VCSFileProxy.createFileProxy(defaultFileSystem.getRoot());
        HgUtils.runIfHgAvailable(root, new Runnable() {
            @Override
            public void run () {
                Utils.logVCSActionEvent("HG"); //NOI18N
                CloneWizardAction wiz = CloneWizardAction.getInstance();
                wiz.setRoot(root);
                wiz.performAction();
            }
        });
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.mercurial.remote.ui.clone.CloneExternalAction");
    }
}
