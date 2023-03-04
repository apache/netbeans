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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

@ActionID(id = "org.netbeans.modules.progress.ui.CancelAction", category = "System")
@ActionRegistration(displayName = "#CTL_CancelAction", iconInMenu=false, asynchronous=true)
@ActionReference(name = "DS-DELETE", path = "Shortcuts")
public final class CancelAction implements ActionListener {
    
    public @Override void actionPerformed(ActionEvent e) {
        InternalHandle handle = Controller.getDefault().getModel().getSelectedHandle();
        if (handle != null) {
            handle.requestCancel();
        }
    }
    
}
