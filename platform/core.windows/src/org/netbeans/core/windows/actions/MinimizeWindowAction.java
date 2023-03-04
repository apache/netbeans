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
import org.netbeans.core.windows.view.ui.slides.SlideController;
import org.openide.windows.WindowManager;


/**
 * Minimize active TopComponent.
 * 
 * @author S. Aubrecht
 * @since 2.30
 */
public final class MinimizeWindowAction extends AbstractAction
implements PropertyChangeListener {

    public MinimizeWindowAction() {
        putValue(NAME, NbBundle.getMessage(CloseModeAction.class, "CTL_MinimizeWindowAction"));
        TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        WindowManager.getDefault().addPropertyChangeListener(
            WeakListeners.propertyChange(this, WindowManager.getDefault()));
        if (SwingUtilities.isEventDispatchThread()) {
            setEnabled( checkEnabled() );
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setEnabled( checkEnabled() );
                }
            });
        }
    }
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        TopComponent context = TopComponent.getRegistry().getActivated();
        if( null == context )
            return;
        Action a = ActionUtils.createMinimizeWindowAction( context );
        if( a.isEnabled() )
            a.actionPerformed( ev );
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())
                || WindowManager.PROP_MODES.equals(evt.getPropertyName())
                || WindowManagerImpl.PROP_ACTIVE_MODE.equals(evt.getPropertyName()) ) {
            setEnabled( checkEnabled() );
        }
    }
    
    private boolean checkEnabled() {
        TopComponent context = TopComponent.getRegistry().getActivated();
        if( null == context ) {
            return false;
        }
        SlideController slideController = ( SlideController ) SwingUtilities.getAncestorOfClass( SlideController.class, context );
        if( null == slideController )
            return false;
        ModeImpl mode = ( ModeImpl ) WindowManagerImpl.getInstance().findMode( context );
        if( null == mode )
            return false;
        if( WindowManagerImpl.getInstance().isTopComponentMinimized( context ) )
            return false;
        if( mode.getState() != Constants.MODE_STATE_JOINED )
            return false;
        if( mode.getKind() != Constants.MODE_KIND_VIEW )
            return false;
        return Switches.isTopComponentSlidingEnabled() && Switches.isSlidingEnabled( context );
    }
    
    @Override
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("MinimizeWindow", newValue); //NOI18N
        } else {
            super.putValue(key, newValue);
        }
    }
    
    @Override
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("MinimizeWindow"); //NOI18N
        } else {
            return super.getValue(key);
        }
    }
}

