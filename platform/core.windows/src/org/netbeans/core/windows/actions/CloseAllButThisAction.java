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
import java.awt.event.ActionListener;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.Mutex;
import org.openide.windows.TopComponent;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.Constants;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.core.windows.Switches;


/**
 * @author   Tim Boudreau
 */
public class CloseAllButThisAction extends AbstractAction
implements PropertyChangeListener, Runnable {
    
    /** TopComponent to exclude or null for global version of action */
    private TopComponent tc;

    /** context flag - when true, close only in active mode, otherwise in 
     * whole window system.
     */
    private boolean isContext;

    private Timer updateTimer;
    private final Object LOCK = new Object();

    public CloseAllButThisAction() {
        this.isContext = false;
        putValue(NAME, NbBundle.getMessage(CloseAllButThisAction.class,
            "CTL_CloseAllButThisAction_MainMenu")); //NOI18N

        updateTimer = new Timer( 300, new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                updateEnabled();
            }
        });
        updateTimer.setRepeats( false );

        TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        updateEnabled();
    }
    
    public CloseAllButThisAction(TopComponent topComp, boolean isContext) {
        tc = topComp;
        this.isContext = isContext;
        //Include the name in the label for the popup menu - it may be clicked over
        //a component that is not selected
        putValue(Action.NAME, NbBundle.getMessage(CloseAllButThisAction.class,
            "CTL_CloseAllButThisAction")); //NOI18N
        
    }

    /** Perform the action. Sets/unsets maximzed mode. */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        TopComponent topC = obtainTC();
        if(topC != null) {
            ActionUtils.closeAllExcept(topC, isContext);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if(TopComponent.Registry.PROP_ACTIVATED.equals(propName) ||
                TopComponent.Registry.PROP_OPENED.equals(propName)) {
            //#216454 
            scheduleUpdate();
        }
    }

    private void scheduleUpdate() {
        synchronized( LOCK ) {
            if( updateTimer.isRunning() ) {
                updateTimer.restart();
            } else {
                updateTimer.start();
            }
        }
    }
    
    private void updateEnabled() {
        Mutex.EVENT.readAccess(this);
    }
    
    @Override
    public void run() {
        TopComponent tc = obtainTC();
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        ModeImpl mode = (ModeImpl)wmi.findMode(tc);
        
        boolean areOtherDocs;
        if (isContext) {
            areOtherDocs = mode.getOpenedTopComponents().size() > 1;
        } else {
            areOtherDocs = wmi.getEditorTopComponents().length > 1;
        }
        
        setEnabled(mode != null && mode.getKind() == Constants.MODE_KIND_EDITOR
                    && areOtherDocs && Switches.isEditorTopComponentClosingEnabled());
    }
    
    private TopComponent obtainTC () {
        TopComponent res = tc;
        if( null == res ) {
            WindowManagerImpl wmi = WindowManagerImpl.getInstance();
            String[] ids = wmi.getRecentViewIDList();

            for( String tcId : ids ) {
                ModeImpl mode = wmi.findModeForOpenedID(tcId);
                if (mode == null || mode.getKind() != Constants.MODE_KIND_EDITOR ) {
                    continue;
                }
                res = wmi.findTopComponent( tcId );
                break;
            }
        }
        if( null == res )
            res = TopComponent.getRegistry().getActivated();
        return res;
    }
    
    /** Overriden to share accelerator with 
     * org.netbeans.core.windows.actions.ActionUtils.CloseWindowAction
     */ 
    @Override
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("CloseAllButThis", newValue); //NOI18N
        } else {
            super.putValue(key, newValue);
        }
    }
    
    /** Overriden to share accelerator with 
     * org.netbeans.core.windows.actions.ActionUtils.CloseWindowAction
     */ 
    @Override
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("CloseAllButThis"); //NOI18N
        } else {
            return super.getValue(key);
        }
    }

}

