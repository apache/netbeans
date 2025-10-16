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
package org.netbeans.modules.groovy.grailsproject.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.ProgressSupport;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class ResolvePluginsAction extends AbstractAction {

    private final GrailsProject prj;

    public ResolvePluginsAction(GrailsProject prj) {
        super(NbBundle.getMessage(ResolvePluginsAction.class, "LBL_ResolvePlugins"));
        this.prj = prj;

    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        final ProgressHandle handle = ProgressHandle.createHandle(NbBundle.getMessage(ResolvePluginsAction.class, "LBL_Resolving_Project_Plugins_progress"));

        final Dialog dialog = DialogDisplayer.getDefault().createDialog(
                ProgressSupport.createProgressDialog(NbBundle.getMessage(ResolvePluginsAction.class, "LBL_Resolving_Project_Plugins"),
                handle, null));

        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                try {
                    handle.start();
                    handle.progress(NbBundle.getMessage(ResolvePluginsAction.class, "LBL_Resolving_Project_Plugins_progress"));
                    prj.getBuildConfig().reload();
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            dialog.setVisible(false);
                            dialog.dispose();
                            handle.finish();
                        }
                    });
                }
            }
        });
        
        dialog.setVisible(true);

    }
}
