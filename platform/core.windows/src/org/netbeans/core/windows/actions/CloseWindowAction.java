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
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.WindowManagerImpl;


/**
 * @author   Peter Zavadsky
 */
public class CloseWindowAction extends AbstractAction
implements PropertyChangeListener {

    public CloseWindowAction() {
        putValue(NAME, NbBundle.getMessage(CloseWindowAction.class, "CTL_CloseWindowAction_MainMenu"));
        TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        // #161406 WindowsAPI to be called from AWT thread only.
        if (SwingUtilities.isEventDispatchThread()) {
            updateEnabled();
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    updateEnabled();
                }
            });
        }
    }
    
    private TopComponent tc;
    // dno't update enable state, is tied to one component only
    public CloseWindowAction(TopComponent topcomp) {
        tc = topcomp;
        //Include the name in the label for the popup menu - it may be clicked over
        //a component that is not selected
        putValue(Action.NAME, NbBundle.getMessage(CloseWindowAction.class, "CTL_CloseWindowAction")); //NOI18N
        if( WindowManagerImpl.getInstance().isEditorTopComponent(tc) ) {
            setEnabled(Switches.isEditorTopComponentClosingEnabled() && Switches.isClosingEnabled(tc));
        } else {
            setEnabled(Switches.isViewTopComponentClosingEnabled() && Switches.isClosingEnabled(tc));
        }
    }
    
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        TopComponent topC = tc;
        if (topC == null) {
            // the updating instance will get the TC to close from winsys
            topC = TopComponent.getRegistry().getActivated();
        }
        if(topC != null) {
            //132852 - if the close action is canceled then the input focus may be lost
            //so let's make sure the window get input focus first
            topC.requestFocusInWindow();
            final TopComponent toClose = topC;
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    ActionUtils.closeWindow(toClose);
                }
            });
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            updateEnabled();
        }
    }
    
    private void updateEnabled() {
        TopComponent activeTc = TopComponent.getRegistry().getActivated();
        if( null == activeTc ) {
            setEnabled(false);
            return;
        }
        if( WindowManagerImpl.getInstance().isEditorTopComponent(activeTc) ) {
            setEnabled( Switches.isEditorTopComponentClosingEnabled() && Switches.isClosingEnabled(activeTc) );
        } else {
            setEnabled( Switches.isViewTopComponentClosingEnabled() && Switches.isClosingEnabled(activeTc) );
        }
    }
    
    /** Overriden to share accelerator with 
     * org.netbeans.core.windows.actions.ActionUtils.CloseWindowAction
     */
    @Override
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("CloseWindow", newValue);
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
            return ActionUtils.getSharedAccelerator("CloseWindow");
        } else {
            return super.getValue(key);
        }
    }
    
}

