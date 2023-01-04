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


package org.netbeans.core.windows.actions;


import org.openide.util.NbBundle;

import javax.swing.*;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.windows.TopComponent;


/**
 * @author   Peter Zavadsky
 */
public class CloseAllDocumentsAction extends AbstractAction {
    
    /** true when action is context aware (like in popup menu),
     * false means global action
     */
    private boolean isContext;

    /**
     * default constructor with label containing mnemonics.
     */
    public CloseAllDocumentsAction() {
        this(false);
    }

    /**
     * can decide whether to have label with mnemonics or without it.
     */
    public CloseAllDocumentsAction(boolean isContext) {
        this.isContext = isContext;
        String key;
        if (isContext) {
            key = "LBL_CloseAllDocumentsAction"; //NOI18N
        } else {
            key = "CTL_CloseAllDocumentsAction"; //NOI18N
        }
        putValue(NAME, NbBundle.getMessage(CloseAllDocumentsAction.class, key));
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        ActionUtils.closeAllDocuments(isContext);
    }

    /** Overriden to share accelerator with 
     * org.netbeans.core.windows.actions.ActionUtils.CloseAllDocumentsAction
     */
    @Override
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("CloseAllDocuments", newValue);
        } else {
            super.putValue(key, newValue);
        }
    }
    
    /** Overriden to share accelerator with 
     * org.netbeans.core.windows.actions.ActionUtils.CloseAllDocumentsAction
     */
    @Override
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("CloseAllDocuments");
        } else {
            return super.getValue(key);
        }
    }

    @Override
    public boolean isEnabled() {
        if( !Switches.isEditorTopComponentClosingEnabled() )
            return false;
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        if (isContext) {
            TopComponent activeTC = TopComponent.getRegistry().getActivated();
            ModeImpl mode = (ModeImpl)wmi.findMode(activeTC);

            return mode != null && mode.getKind() == Constants.MODE_KIND_EDITOR
                    && !mode.getOpenedTopComponents().isEmpty();
        } else {
            return wmi.getEditorTopComponents().length > 0;
        }
    }
    
}

