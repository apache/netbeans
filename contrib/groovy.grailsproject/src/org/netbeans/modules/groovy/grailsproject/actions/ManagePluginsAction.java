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

package org.netbeans.modules.groovy.grailsproject.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.ui.GrailsPluginsPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author David Calavera
 */
public class ManagePluginsAction extends AbstractAction {
    
    private final GrailsProject project;
    
    public ManagePluginsAction(Project project) {
        super(NbBundle.getMessage(ManagePluginsAction.class, "CTL_ManagePluginsAction"));
        this.project = (GrailsProject) project;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void actionPerformed(ActionEvent arg0) {
        GrailsPluginsPanel panel = new GrailsPluginsPanel(project);
        javax.swing.JButton close =
            new javax.swing.JButton(NbBundle.getMessage(ManagePluginsAction.class, "CTL_Close"));
        close.getAccessibleContext()
             .setAccessibleDescription(NbBundle.getMessage(ManagePluginsAction.class, "CTL_Close"));

        DialogDescriptor descriptor =
            new DialogDescriptor(panel, NbBundle.getMessage(ManagePluginsAction.class, "CTL_PluginTitle"),
                true, new Object[] { close }, close, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(GrailsPluginsPanel.class), null); // NOI18N
        Dialog dlg = null;

        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
        } finally {
            try {
                if (dlg != null) {
                    dlg.dispose();
                }
            } finally {
                panel.dispose();
            }
        }
    }
    
}
