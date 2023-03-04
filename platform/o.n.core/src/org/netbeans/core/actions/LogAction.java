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

package org.netbeans.core.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.netbeans.core.actions.Bundle.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.modules.Places;
import org.openide.util.NbBundle.Messages;

@ActionID(category="View", id="org.netbeans.core.actions.LogAction")
@ActionRegistration(displayName = "#MSG_LogTab_name", iconInMenu = false, iconBase = "org/netbeans/core/resources/log-file.gif")
@ActionReference(path = "Menu/View", position = 500)
@Messages("MSG_LogTab_name=IDE &Log")
public class LogAction implements ActionListener {

    @Messages("MSG_ShortLogTab_name=IDE Log")
    @Override public void actionPerformed(ActionEvent evt) {
        File userDir = Places.getUserDirectory();
        if (userDir == null) {
            return;
        }
        File f = new File(userDir, "/var/log/messages.log");
        LogViewerSupport p = new LogViewerSupport(f, MSG_ShortLogTab_name());
        try {
            p.showLogViewer();
        } catch (IOException e) {
            Logger.getLogger(LogAction.class.getName()).log(Level.INFO, "Showing IDE log action failed", e);
        }
    }

}
