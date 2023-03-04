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
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.core.windows.EditorOnlyDisplayer;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;


/**
 * Moves currently active TopComponent left or right in its current mode.
 * 
 * @author S. Aubrecht
 * @since 2.62
 */
public final class MoveWindowWithinModeAction extends AbstractAction
implements PropertyChangeListener {

    private final boolean moveLeft;
    private final TopComponent tc;

    private MoveWindowWithinModeAction( boolean moveLeft ) {
        this( null, moveLeft );
    }

    private MoveWindowWithinModeAction( TopComponent tc, boolean moveLeft ) {
        this.moveLeft = moveLeft;
        this.tc = tc;

        if( null != tc ) {
            putValue(Action.NAME, NbBundle.getMessage(MoveWindowWithinModeAction.class, moveLeft
                    ? "CTL_MoveWindowLeftContextAction"
                    : "CTL_MoveWindowRightContextAction")); //NOI18N
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
        } else {
            putValue(Action.NAME, NbBundle.getMessage(MoveWindowWithinModeAction.class, moveLeft
                    ? "CTL_MoveWindowLeftAction"
                    : "CTL_MoveWindowRightAction")); //NOI18N
        }
    }

    public static Action createMoveLeft() {
        return new MoveWindowWithinModeAction( null, true );
    }

    public static Action createMoveRight() {
        return new MoveWindowWithinModeAction( null, false );
    }

    static Action createMoveLeft( TopComponent tc ) {
        return new MoveWindowWithinModeAction( tc, true );
    }

    static Action createMoveRight( TopComponent tc ) {
        return new MoveWindowWithinModeAction( tc, false );
    }

    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        if( EditorOnlyDisplayer.getInstance().isActive() ) {
            return;
        }
        TopComponent contextTc = null == tc ? TopComponent.getRegistry().getActivated() : tc;
        if( null == contextTc )
            return;

        ModeImpl mode = ( ModeImpl ) WindowManagerImpl.getInstance().findMode( contextTc );
        if( null == mode )
            return;
        int position = mode.getTopComponentTabPosition( contextTc );
        if( moveLeft )
            position--;
        else
            position++;
        if( position >= 0 && position < mode.getOpenedTopComponents().size() )
            mode.addOpenedTopComponent( contextTc, position );
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            updateEnabled();
        }
    }
    
    private void updateEnabled() {
        if( null == tc ) {
            return;
        }
        ModeImpl mode = ( ModeImpl ) WindowManagerImpl.getInstance().findMode( tc );
        if( null == mode )
            return;
        int position = mode.getTopComponentTabPosition( tc );
        if( 0 == position && moveLeft ) {
            setEnabled( false );
            return;
        }
        if( position == mode.getOpenedTopComponents().size()-1 && !moveLeft ) {
            setEnabled( false );
            return;
        }
        if( EditorOnlyDisplayer.getInstance().isActive() ) {
            setEnabled( false) ;
            return;
        }
        setEnabled( true );
    }
    
    @Override
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator(moveLeft ? "MoveWindowLeft" : "MoveWindowRight", newValue); //NOI18N
        } else {
            super.putValue(key, newValue);
        }
    }
    
    @Override
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator(moveLeft ? "MoveWindowLeft" : "MoveWindowRight"); //NOI18N
        } else {
            return super.getValue(key);
        }
    }
}

