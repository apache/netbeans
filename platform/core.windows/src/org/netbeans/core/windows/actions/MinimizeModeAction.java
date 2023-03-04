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


import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.windows.WindowManager;


/**
 * Minimize all TopComponent in a given non-editor Mode.
 * 
 * @author S. Aubrecht
 * @since 2.30
 */
public final class MinimizeModeAction extends AbstractAction
implements PropertyChangeListener {

    public MinimizeModeAction() {
        putValue(NAME, NbBundle.getMessage(CloseModeAction.class, "CTL_MinimizeModeAction"));
        TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        WindowManager.getDefault().addPropertyChangeListener(
            WeakListeners.propertyChange(this, WindowManager.getDefault()));
        if (SwingUtilities.isEventDispatchThread()) {
            updateEnabled();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateEnabled();
                }
            });
        }
    }
    
    private ModeImpl mode;
    // dno't update enable state, is tied to one component only
    public MinimizeModeAction(ModeImpl mode) {
        //Include the name in the label for the popup menu - it may be clicked over
        //a component that is not selected
        putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class,
        "CTL_MinimizeModeAction")); //NOI18N
        this.mode = mode;
        setEnabled(Switches.isModeSlidingEnabled() 
                && mode.getKind() == Constants.MODE_KIND_VIEW 
                && mode.getState() == Constants.MODE_STATE_JOINED );
    }
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        ModeImpl contextMode = mode;
        if (contextMode == null) {
            // the updating instance will get the TC to close from winsys
            TopComponent tc = TopComponent.getRegistry().getActivated();
            if( null != tc )
                contextMode = ( ModeImpl ) WindowManagerImpl.getInstance().findMode( tc );
        }
        if(contextMode != null) {
            WindowManagerImpl.getInstance().userMinimizedMode( contextMode );
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())
                || WindowManager.PROP_MODES.equals(evt.getPropertyName())
                || WindowManagerImpl.PROP_ACTIVE_MODE.equals(evt.getPropertyName()) ) {
            updateEnabled();
        }
    }
    
    private void updateEnabled() {
        ModeImpl contextMode = mode;
        if (contextMode == null) {
            // the updating instance will get the TC to close from winsys
            TopComponent tc = TopComponent.getRegistry().getActivated();
            if( null != tc )
                contextMode = ( ModeImpl ) WindowManagerImpl.getInstance().findMode( tc );
        }
        if( null == contextMode ) {
            setEnabled(false);
            return;
        }
        setEnabled(Switches.isModeSlidingEnabled() 
                && contextMode.getKind() == Constants.MODE_KIND_VIEW 
                && contextMode.getState() == Constants.MODE_STATE_JOINED );
    }
    
    @Override
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("MinimizeMode", newValue); //NOI18N
        } else {
            super.putValue(key, newValue);
        }
    }
    
    @Override
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("MinimizeMode"); //NOI18N
        } else {
            return super.getValue(key);
        }
    }
}

