/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.dashboard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.dashboard.DashboardManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.dashboard.ShowDashboardAction", category = "Window")
@ActionRegistration(displayName = "#LBL_Action")
@ActionReferences({
    @ActionReference(path = "Menu/Window",
            name = "org-netbeans-modules-dashboard-ShowDashboardAction", position = 1400),
    @ActionReference(path = "Shortcuts", name = "SO-W")
})
@Messages("LBL_Action=Show Dashboard")
public class ShowDashboardAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        DashboardManager.getDefault().show();
    }

}
