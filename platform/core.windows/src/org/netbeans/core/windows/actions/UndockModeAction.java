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

package org.netbeans.core.windows.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.util.NbBundle;

/**
 * Action perform undock either of given or active Mode.
 * Undock means that all TopCompoments in the given Mode are moved to new, separate floating window,
 * 
 * @author S. Aubrecht
 * @since 2.30
 */
public final class UndockModeAction extends AbstractAction {

    private final ModeImpl mode;

    /**
     * Creates instance of action to Undock the whole mode of currently active top
     * component in the system. For use in main menu.
     */
    public UndockModeAction () {
        this.mode = null;
        putValue(Action.NAME, NbBundle.getMessage(DockModeAction.class, "CTL_UndockModeAction")); //NOI18N
    }

    /**
     * Undock of given Mode.
     * For use in the context menus.
     */
    public UndockModeAction (ModeImpl mode) {
        this.mode = mode;
        putValue(Action.NAME, NbBundle.getMessage(DockModeAction.class, "CTL_UndockModeAction")); //NOI18N
    }
    
    @Override
    public void actionPerformed (ActionEvent e) {
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        ModeImpl contextMode = getMode2WorkWith();
        boolean isDocked = contextMode.getState() == Constants.MODE_STATE_JOINED;

        if (isDocked) {
            wmi.userUndockedMode(contextMode);
        } else {
            wmi.userDockedMode(contextMode);
        }
    }
    
    /** Overriden to share accelerator between instances of this action.
     */ 
    @Override
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("UndockModeAction", newValue); //NOI18N
        } else {
            super.putValue(key, newValue);
        }
    }

    /** Overriden to share accelerator between instances of this action.
     */ 
    @Override
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("UndockModeAction"); //NOI18N
        } else {
            return super.getValue(key);
        }
    }

    @Override
    public boolean isEnabled() {
        ModeImpl contextMode = getMode2WorkWith();
        if( null == contextMode )
            return false;
        boolean docked = contextMode.getState() == Constants.MODE_STATE_JOINED;
        if( !docked )
            return false;
        if( contextMode.getKind() == Constants.MODE_KIND_EDITOR )
            return Switches.isEditorModeUndockingEnabled();
        return contextMode.getKind() == Constants.MODE_KIND_VIEW && Switches.isViewModeUndockingEnabled();
    }

    private ModeImpl getMode2WorkWith () {
        if (mode != null) {
            return mode;
        }
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        return ( ModeImpl ) wm.findMode( wm.getRegistry().getActivated() );
    }
}
