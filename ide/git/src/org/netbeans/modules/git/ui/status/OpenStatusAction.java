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

package org.netbeans.modules.git.ui.status;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Open the status window. It focuses recently opened
 * view unless it's not initialized yet.
 *
 * @author Ondra Vrabec
 */
@ActionID(id = "org.netbeans.modules.git.ui.status.OpenStatusAction", category = "Git")
@ActionRegistration(displayName = "#CTL_MenuItem_OpenStatusAction")
@NbBundle.Messages({
    "CTL_MenuItem_OpenStatusAction=Open Status Window"
})
public class OpenStatusAction implements ActionListener {

    public boolean isEnabled() {
        return GitVersioningTopComponent.findInstance().hasContext();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GitVersioningTopComponent stc = GitVersioningTopComponent.findInstance();
        if (stc.hasContext()) {
            stc.open();
            stc.requestActive();
        }
    }

    protected boolean shouldPostRefresh() {
        return false;
    }
}
