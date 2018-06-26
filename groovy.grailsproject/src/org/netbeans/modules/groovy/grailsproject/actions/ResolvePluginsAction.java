/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.grailsproject.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
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
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ResolvePluginsAction.class, "LBL_Resolving_Project_Plugins_progress"));

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
