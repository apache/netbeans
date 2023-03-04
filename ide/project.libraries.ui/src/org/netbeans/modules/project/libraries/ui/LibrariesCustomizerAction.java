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
package org.netbeans.modules.project.libraries.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static org.netbeans.modules.project.libraries.ui.Bundle.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(id = "org.netbeans.modules.project.libraries.ui.LibrariesCustomizerAction", category = "Tools")
@ActionRegistration(iconInMenu = false, displayName = "#CTL_LibrariesManager")
@ActionReference(position = 500, name = "LibrariesCustomizerAction", path = "Menu/Tools")
@Messages("CTL_LibrariesManager=&Libraries")
public final class LibrariesCustomizerAction implements ActionListener {

    @Override public void actionPerformed(ActionEvent e) {
        showCustomizer();
    }

    /**
     * Shows libraries customizer displaying all currently open library managers.
     * @return true if user pressed OK and libraries were successfully modified
     */
    @Messages("TXT_LibrariesManager=Ant Library Manager")
    private static boolean showCustomizer () {
        AllLibrariesCustomizer  customizer =
                new AllLibrariesCustomizer();
        DialogDescriptor descriptor = new DialogDescriptor(customizer, TXT_LibrariesManager());
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            dlg.setVisible(true);
            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                return customizer.apply();
            } else {
                return false;
            }
        } finally {
            dlg.dispose();
        }
    }

}
