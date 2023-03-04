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


import java.awt.Component;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ui.MultiSplitPane;


/**
 * Resize the given or currently active Mode container using keyboard arrows.
 * 
 * @author S. Aubrecht
 * @since 2.37
 */
public final class ResizeModeAction extends AbstractAction
implements PropertyChangeListener {

    private final ModeImpl mode;
    
    public ResizeModeAction() {
        this( null );
        TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
    }
    
    public ResizeModeAction(ModeImpl mode) {
        putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class, "CTL_ResizeModeAction")); //NOI18N
        this.mode = mode;
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
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        updateEnabled();
        if( !isEnabled() )
            return;
        ModeImpl contextMode = getModeToWorkWith();
        Component c = contextMode.getSelectedTopComponent();
        MultiSplitPane splitPane = ( MultiSplitPane ) SwingUtilities.getAncestorOfClass( MultiSplitPane.class, c);
        splitPane.startResizing( c );
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            updateEnabled();
        }
    }
    
    private void updateEnabled() {
        ModeImpl contextMode = getModeToWorkWith();
        if( null == contextMode 
                || contextMode.getKind() == Constants.MODE_KIND_EDITOR 
                || contextMode.getState() == Constants.MODE_STATE_SEPARATED
                || null == contextMode.getSelectedTopComponent() ) {
            setEnabled( false );
            return;
        }
        Component c = contextMode.getSelectedTopComponent();
        MultiSplitPane splitPane = ( MultiSplitPane ) SwingUtilities.getAncestorOfClass( MultiSplitPane.class, c);
        if( null == splitPane ) {
            setEnabled( false );
            return;
        }
        setEnabled( true );
    }
    
    private ModeImpl getModeToWorkWith() {
        if( null != mode )
            return mode;
        
        TopComponent activeTc = TopComponent.getRegistry().getActivated();
        if( null == activeTc )
            return null;
        return ( ModeImpl ) WindowManagerImpl.getInstance().findMode( activeTc );
    }
    
    @Override
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("ResizeMode", newValue); //NOI18N
        } else {
            super.putValue(key, newValue);
        }
    }
    
    @Override
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("ResizeMode"); //NOI18N
        } else {
            return super.getValue(key);
        }
    }
    
}

