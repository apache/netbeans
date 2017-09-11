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
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.util.NbBundle;

/**
 * Action perform undock either of given or active Mode.
 * Undock means that all TopCompoments in the given Mode are moved to new, separate floating window,
 * 
 * @author S. Aubrecht
 * @since 2.30
 */
public final class UndockModeAction extends AbstractAction {

    private final ModeImpl mode;

    /**
     * Creates instance of action to Undock the whole mode of currently active top
     * component in the system. For use in main menu.
     */
    public UndockModeAction () {
        this.mode = null;
        putValue(Action.NAME, NbBundle.getMessage(DockModeAction.class, "CTL_UndockModeAction")); //NOI18N
    }

    /**
     * Undock of given Mode.
     * For use in the context menus.
     */
    public UndockModeAction (ModeImpl mode) {
        this.mode = mode;
        putValue(Action.NAME, NbBundle.getMessage(DockModeAction.class, "CTL_UndockModeAction")); //NOI18N
    }
    
    @Override
    public void actionPerformed (ActionEvent e) {
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        ModeImpl contextMode = getMode2WorkWith();
        boolean isDocked = contextMode.getState() == Constants.MODE_STATE_JOINED;

        if (isDocked) {
            wmi.userUndockedMode(contextMode);
        } else {
            wmi.userDockedMode(contextMode);
        }
    }
    
    /** Overriden to share accelerator between instances of this action.
     */ 
    @Override
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("UndockModeAction", newValue); //NOI18N
        } else {
            super.putValue(key, newValue);
        }
    }

    /** Overriden to share accelerator between instances of this action.
     */ 
    @Override
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("UndockModeAction"); //NOI18N
        } else {
            return super.getValue(key);
        }
    }

    @Override
    public boolean isEnabled() {
        ModeImpl contextMode = getMode2WorkWith();
        if( null == contextMode )
            return false;
        boolean docked = contextMode.getState() == Constants.MODE_STATE_JOINED;
        if( !docked )
            return false;
        if( contextMode.getKind() == Constants.MODE_KIND_EDITOR )
            return Switches.isEditorModeUndockingEnabled();
        return contextMode.getKind() == Constants.MODE_KIND_VIEW && Switches.isViewModeUndockingEnabled();
    }

    private ModeImpl getMode2WorkWith () {
        if (mode != null) {
            return mode;
        }
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        return ( ModeImpl ) wm.findMode( wm.getRegistry().getActivated() );
    }
}
