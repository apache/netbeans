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
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action to move either either given or active editor top component to a new 
 * document tab group.
 * 
 * @author S. Aubrecht
 * @since 2.36
 */
public final class NewTabGroupAction extends AbstractAction {

    private final TopComponent tc;

    /**
     * Creates instance of action to move currently active editor top
     * component to a new document tab group. For use in main menu.
     */
    public NewTabGroupAction () {
        this( null );
    }

    /**
     * Move the given editor TopComponent to a new document tab group.
     * For use in the context menus.
     */
    public NewTabGroupAction (TopComponent tc) {
        this.tc = tc;
        putValue(Action.NAME, NbBundle.getMessage(NewTabGroupAction.class, "CTL_NewTabGroupAction")); //NOI18N
    }
    
    @Override
    public void actionPerformed (ActionEvent e) {
        // contextTC shound never be null thanks to isEnabled impl
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        TopComponent contextTC = getTC2WorkWith();
        if( null == contextTC )
            return; //just being paranoid
        ModeImpl currentMode = ( ModeImpl ) wmi.findMode( contextTC );
        if( null == currentMode || currentMode.getKind() != Constants.MODE_KIND_EDITOR 
                || !wmi.isDocked( contextTC ) )
            return;
        wmi.newTabGroup( contextTC );
    }
    
    /** Overriden to share accelerator between instances of this action.
     */ 
    @Override
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("NewTabGroupAction", newValue); //NOI18N
        } else {
            super.putValue(key, newValue);
        }
    }

    /** Overriden to share accelerator between instances of this action.
     */ 
    @Override
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("NewTabGroupAction"); //NOI18N
        } else {
            return super.getValue(key);
        }
    }

    @Override
    public boolean isEnabled() {
        TopComponent context = getTC2WorkWith();
        boolean res = null != context;
        if( res ) {
            WindowManagerImpl wm = WindowManagerImpl.getInstance();
            ModeImpl mode = ( ModeImpl ) wm.findMode( context );
            res &= null != mode;
            if( res ) {
                res &= mode.getKind() == Constants.MODE_KIND_EDITOR;
                res &= mode.getOpenedTopComponents().size() > 1;
                res &= wm.isDocked( context );
            }
        }
        return res;
    }

    private TopComponent getTC2WorkWith () {
        if (tc != null) {
            return tc;
        }
        return WindowManager.getDefault().getRegistry().getActivated();
    }
}
