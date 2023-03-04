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


import java.awt.EventQueue;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * @author   Peter Zavadsky
 */
public class CloneDocumentAction extends AbstractAction implements PropertyChangeListener, Runnable {

    public CloneDocumentAction() {
        putValue(NAME, NbBundle.getMessage(CloneDocumentAction.class, "CTL_CloneDocumentAction"));
        TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        // #126355 - may be called outside dispatch thread
        if (EventQueue.isDispatchThread()) {
            updateEnabled();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if(!(tc instanceof TopComponent.Cloneable)) {
            return;
        }
        
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);
        if(mode == null) {
            return;
        }
        
        if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
            ActionUtils.cloneWindow(tc);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            updateEnabled();
        }
    }
    
    private void updateEnabled() {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if (tc != null) {
            ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);
            setEnabled(tc instanceof TopComponent.Cloneable
                && mode != null && mode.getKind() == Constants.MODE_KIND_EDITOR);
        }
    }

    /** Runnable implementation, for invokeLater */
    @Override
    public void run() {
        updateEnabled();
    }
    
}

