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

