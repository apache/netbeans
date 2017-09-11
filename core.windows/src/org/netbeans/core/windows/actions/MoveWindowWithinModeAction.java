/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

