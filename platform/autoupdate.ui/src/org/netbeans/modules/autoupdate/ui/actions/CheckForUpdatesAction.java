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
package org.netbeans.modules.autoupdate.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.CheckForUpdatesProvider;
import org.openide.util.Lookup;

@ActionID(id = "org.netbeans.modules.autoupdate.ui.actions.CheckForUpdatesAction", category = "System")
@ActionRegistration(displayName = "#CTL_CheckForUpdatesAction", iconInMenu = true)
@ActionReference(path = "Menu/Help", position = 1300)
public final class CheckForUpdatesAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent ev) {
        final CheckForUpdatesProvider checkForUpdatesProvider = Lookup.getDefault().lookup(CheckForUpdatesProvider.class);
        assert checkForUpdatesProvider != null : "An instance of CheckForUpdatesProvider found in Lookup: " + Lookup.getDefault();
        if (checkForUpdatesProvider != null) {
            checkForUpdatesProvider.openCheckForUpdatesWizard(true);
        }
    }
}
