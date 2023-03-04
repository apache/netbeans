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


import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;


/**
 * @author   Peter Zavadsky
 */
public class SwitchToRecentDocumentAction extends AbstractAction
implements PropertyChangeListener {

    public SwitchToRecentDocumentAction() {
        putValue(Action.NAME, NbBundle.getMessage(SwitchToRecentDocumentAction.class, "CTL_SwitchToRecentDocumentAction"));
        TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        updateEnabled();
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        String[] ids = wm.getRecentViewIDList();
        
        if(ids.length == 0) {
            return;
        }

        for(int i = 0; i < ids.length; i++) {
            String tcId = ids[i];
            
            ModeImpl mode = (ModeImpl)wm.findModeForOpenedID(tcId);
            if(mode == null) {
                continue;
            }
            
            if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
                // #37030 Unmaximize other mode if needed.
                if(mode != wm.getCurrentMaximizedMode()) {
                    wm.switchMaximizedMode(null);
                }
                TopComponent tc = wm.findTopComponent(tcId);
                if( null != tc ) {
                    tc.requestActive();
                    break;
                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())) {
            updateEnabled();
        }
    }
    
    private void updateEnabled() {
        for(Iterator it = WindowManagerImpl.getInstance().getModes().iterator(); it.hasNext(); ) {
            ModeImpl mode = (ModeImpl)it.next();
            if(mode.getKind() == Constants.MODE_KIND_EDITOR
            && !mode.getOpenedTopComponents().isEmpty()) {
                setEnabled(true);
                return;
            }
        }
        setEnabled(false);
    }
}

