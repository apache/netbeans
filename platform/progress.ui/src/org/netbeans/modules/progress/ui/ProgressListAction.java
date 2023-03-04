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

package org.netbeans.modules.progress.ui;

import org.netbeans.modules.progress.spi.Controller;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.progress.spi.ProgressUIWorkerWithModel;
import org.netbeans.modules.progress.spi.SwingController;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
@ActionID(id = "org.netbeans.modules.progress.ui.ProgressListAction", category = "Window")
@ActionRegistration(displayName = "#CTL_ProcessListAction", lazy=false)
@ActionReference(position = 1000, name = "ProgressListAction", path = "Menu/Window/Tools")
public class ProgressListAction extends AbstractAction implements ListDataListener, Runnable {

    /** Creates a new instance of ProcessListAction */
    public ProgressListAction() {
        this(NbBundle.getMessage(ProgressListAction.class, "CTL_ProcessListAction"));
    }
    
    public ProgressListAction(String name) {
        putValue(NAME, name);
//        putValue(MNEMONIC_KEY, new Integer((int)NbBundle.getMessage(ProgressListAction.class, "ProcessListAction.mnemonic").charAt(0)));
        Controller.getDefault().getModel().addListDataListener(this);
        updateEnabled();
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
       //need to invoke later becauseotherwise the awtlistener possibly catches a mouse event
        SwingUtilities.invokeLater(this);
    }
    
    public void run() {
        ((ProgressUIWorkerWithModel)SwingController.getDefault().getVisualComponent()).showPopup();
    }

    private void updateEnabled() {
        setEnabled(Controller.getDefault().getModel().getSize() != 0);
    }    

    public void contentsChanged(javax.swing.event.ListDataEvent listDataEvent) {
        updateEnabled();
    }

    public void intervalAdded(javax.swing.event.ListDataEvent listDataEvent) {
        updateEnabled();
    }

    public void intervalRemoved(javax.swing.event.ListDataEvent listDataEvent) {
        updateEnabled();
    }

}
