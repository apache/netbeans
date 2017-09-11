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

