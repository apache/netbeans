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

import java.awt.Dialog;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Utilities;

/** The action that shows the AboutBox.
*
* @author Ian Formanek
*/
public class AboutAction extends CallableSystemAction {

    public AboutAction () {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public void performAction () {
        DialogDescriptor descriptor = new DialogDescriptor(
            new org.netbeans.core.ui.ProductInformationPanel (),
            NbBundle.getMessage(AboutAction.class, "About_title"),
            true,
                new Object[0],
                null,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            if( Utilities.isMac() && dlg instanceof JDialog ) {
                JDialog d = (JDialog) dlg;
                InputMap map = d.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                map.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.META_MASK), "Escape"); //NOI18N
                //#221571
                d.getRootPane().putClientProperty("nb.about.dialog", Boolean.TRUE); //NOI18N
            }
            dlg.setResizable(false);
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(AboutAction.class);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(AboutAction.class, "About");
    }

}
