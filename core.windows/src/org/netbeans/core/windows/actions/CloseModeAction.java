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


/**
 * Close all TopComponents in a non-editor Mode.
 * 
 * @author S. Aubrecht
 * @since 2.30
 */
public final class CloseModeAction extends AbstractAction
implements PropertyChangeListener {

    public CloseModeAction() {
        putValue(NAME, NbBundle.getMessage(CloseModeAction.class, "CTL_CloseModeAction"));
        TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
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
    public CloseModeAction(ModeImpl mode) {
        //Include the name in the label for the popup menu - it may be clicked over
        //a component that is not selected
        putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class,
        "CTL_CloseModeAction")); //NOI18N
        this.mode = mode;
        if( mode.getKind() == Constants.MODE_KIND_EDITOR ) {
            setEnabled(false);
        } else {
            setEnabled(Switches.isModeClosingEnabled());
        }
    }
    
    
    /** Perform the action. Sets/unsets maximzed mode. */
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
            WindowManagerImpl.getInstance().userClosedMode( contextMode );
        }
    }

    @Override
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
            setEnabled( false );
        } else {
            setEnabled( Switches.isModeClosingEnabled() );
        }
    }
    
    /** Overriden to share accelerator with 
     * org.netbeans.core.windows.actions.ActionUtils.CloseWindowAction
     */ 
    @Override
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("CloseMode", newValue); //NOI18N
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
            return ActionUtils.getSharedAccelerator("CloseMode"); //NOI18N
        } else {
            return super.getValue(key);
        }
    }
    
}

