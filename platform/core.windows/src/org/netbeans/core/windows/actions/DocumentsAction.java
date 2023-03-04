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

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ui.DocumentsDlg;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Shows list of opened documents in dialog.
 * 
 * @author   Peter Zavadsky
 */
public class DocumentsAction extends AbstractAction implements Runnable {

    private final PropertyChangeListener propListener;
    
    public DocumentsAction() {
        putValue(Action.NAME, NbBundle.getMessage(DocumentsAction.class, "CTL_DocumentsAction"));

        propListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())) {
                    updateState();
                }
           }
        };
        TopComponent.Registry registry = TopComponent.getRegistry();
        registry.addPropertyChangeListener(WeakListeners.propertyChange(propListener, registry));

        // #37529 WindowsAPI to be called from AWT thread only.
        if(SwingUtilities.isEventDispatchThread()) {
            updateState();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateState();
                }
            });
        }
    }
    
    /** Perform the action. Tries the performer and then scans the ActionMap
     * of selected topcomponent.
     */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        if (SwingUtilities.isEventDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }
    
    /** Display Documents dialog in AWT thread. */
    public void run () {
        DocumentsDlg.showDocumentsDialog();
    }
    
    private void updateState() {
        // #81939: enable action if documents list isn't empty
        setEnabled(!DocumentsDlg.isEmpty());
    }
    
}

