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
package org.netbeans.modules.versioning.util;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

import java.awt.event.ActionEvent;

/**
 * Open the Versioning Output view.
 *
 * @author Maros Sandor
 */
public class OpenVersioningOutputAction extends SystemAction {

    public OpenVersioningOutputAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        setIcon(null);
    }

    public String getName() {
        return NbBundle.getMessage(OpenVersioningOutputAction.class, "CTL_MenuItem_OpenVersioningOutput"); // NOI18N
    }

    /**
     * Window/Versioning should be always enabled.
     * 
     * @return true
     */ 
    public boolean isEnabled() {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(OpenVersioningOutputAction.class);
    }

    public void actionPerformed(ActionEvent e) {
        VersioningOutputTopComponent stc = VersioningOutputTopComponent.getInstance();
        stc.open();
        stc.requestActive();
    }
}
