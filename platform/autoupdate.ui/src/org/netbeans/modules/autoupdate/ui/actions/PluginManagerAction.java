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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import org.netbeans.modules.autoupdate.ui.PluginManagerUI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

@ActionID(id = "org.netbeans.modules.autoupdate.ui.actions.PluginManagerAction", category = "System")
@ActionRegistration(displayName = "#PluginManagerAction_Name", iconInMenu=false)
@ActionReference(path = "Menu/Tools", position = 1400)
public final class PluginManagerAction extends AbstractAction {
    private static PluginManagerUI pluginManagerUI = null;
    private Dialog dlg = null;
    
    @Override
    public void actionPerformed(ActionEvent ev) {
        if (dlg == null) {
            JButton close = new JButton ();
            close.setDefaultCapable(false);
            Mnemonics.setLocalizedText (close,NbBundle.getMessage (PluginManagerAction.class, "PluginManager_CloseButton_Name"));
            
            String initialTab = null;
            if (ev.getID() == 100) {
                initialTab = ev.getActionCommand();
            }
            
            pluginManagerUI = new PluginManagerUI (
                close,
                initialTab
            );
            DialogDescriptor dd = new DialogDescriptor (
                                        pluginManagerUI,
                                        NbBundle.getMessage (PluginManagerAction.class, "PluginManager_Panel_Name"),
                                        false, // modal
                                        new JButton[] { close },
                                        close,
                                        DialogDescriptor.DEFAULT_ALIGN,
                                        null,
                                        null /*final ActionListener bl*/);
            dd.setOptions (new Object [0]);

            dlg = DialogDisplayer.getDefault ().createDialog (dd);
            dlg.setVisible (true);
            dlg.addWindowListener(new WindowListener () {
                public void windowOpened (WindowEvent e) {}
                public void windowClosing (WindowEvent e) {
                    dlg = null;
                    pluginManagerUI = null;
                }
                public void windowClosed (WindowEvent e) {
                    dlg = null;
                    pluginManagerUI = null;
                }
                public void windowIconified (WindowEvent e) {}
                public void windowDeiconified (WindowEvent e) {}
                public void windowActivated (WindowEvent e) {}
                public void windowDeactivated (WindowEvent e) {}
            });
        } else {
            dlg.requestFocus ();
        }
    }
    
    public static PluginManagerUI getPluginManagerUI () {
        return pluginManagerUI;
    }
    
}
