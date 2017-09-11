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

import javax.swing.*;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.windows.TopComponent;


/**
 * @author   Peter Zavadsky
 */
public class CloseAllDocumentsAction extends AbstractAction {
    
    /** true when action is context aware (like in popup menu),
     * false means global action
     */
    private boolean isContext;

    /**
     * default constructor with label containing mnemonics.
     */
    public CloseAllDocumentsAction() {
        this(false);
    }

    /**
     * can decide whether to have label with mnemonics or without it.
     */
    public CloseAllDocumentsAction(boolean isContext) {
        this.isContext = isContext;
        String key;
        if (isContext) {
            key = "LBL_CloseAllDocumentsAction"; //NOI18N
        } else {
            key = "CTL_CloseAllDocumentsAction"; //NOI18N
        }
        putValue(NAME, NbBundle.getMessage(CloseAllDocumentsAction.class, key));
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        ActionUtils.closeAllDocuments(isContext);
    }

    /** Overriden to share accelerator with 
     * org.netbeans.core.windows.actions.ActionUtils.CloseAllDocumentsAction
     */ 
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("CloseAllDocuments", newValue);
        } else {
            super.putValue(key, newValue);
        }
    }
    
    /** Overriden to share accelerator with 
     * org.netbeans.core.windows.actions.ActionUtils.CloseAllDocumentsAction
     */ 
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("CloseAllDocuments");
        } else {
            return super.getValue(key);
        }
    }

    @Override
    public boolean isEnabled() {
        if( !Switches.isEditorTopComponentClosingEnabled() )
            return false;
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        if (isContext) {
            TopComponent activeTC = TopComponent.getRegistry().getActivated();
            ModeImpl mode = (ModeImpl)wmi.findMode(activeTC);

            return mode != null && mode.getKind() == Constants.MODE_KIND_EDITOR
                    && !mode.getOpenedTopComponents().isEmpty();
        } else {
            return wmi.getEditorTopComponents().length > 0;
        }
    }
    
}

