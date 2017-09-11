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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.hudson.ui.wizard;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.api.Utilities;
import org.netbeans.modules.hudson.ui.api.UI;
import org.netbeans.modules.hudson.ui.util.UsageLogging;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class InstanceDialog extends DialogDescriptor {

    private static final Logger LOG = Logger.getLogger(InstanceDialog.class.getName());

    private final InstancePropertiesVisual panel;
    private final JButton addButton;
    private Dialog dialog;
    private HudsonInstance created;
    
    public InstanceDialog() {
        this(new InstancePropertiesVisual());
    }
    private InstanceDialog(InstancePropertiesVisual panel) {
        super(panel, NbBundle.getMessage(InstanceDialog.class, "LBL_InstanceWiz_Title"));
        this.panel = panel;
        addButton = new JButton(NbBundle.getMessage(InstanceDialog.class, "InstanceDialog.add"));
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tryToAdd();
            }
        });
        panel.init(createNotificationLineSupport(), addButton);
        setOptions(new Object[] {addButton, NotifyDescriptor.CANCEL_OPTION});
        setClosingOptions(new Object[] {NotifyDescriptor.CANCEL_OPTION});
    }

    public HudsonInstance show() {
        dialog = DialogDisplayer.getDefault().createDialog(this);
        dialog.setVisible(true);
        LOG.log(Level.FINE, "Added Hudson instance: {0}", created);
        return created;
    }

    @NbBundle.Messages({
        "# UI logging of adding new server",
        "UI_HUDSON_SERVER_REGISTERED=Hudson server registered",
        "# Usage Logging",
        "USG_HUDSON_SERVER_REGISTERED=Hudson server registered"
    })
    private void tryToAdd() {
        addButton.setEnabled(false);
        panel.showChecking();
        dialog.pack();
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                Utilities.HudsonURLCheckResult checkResult
                        = Utilities.checkHudsonURL(panel.getUrl());

                switch (checkResult) {
                    case OK:
                        break;
                    case WRONG_VERSION:
                        problem(NbBundle.getMessage(InstanceDialog.class,
                                "MSG_WrongVersion", //NOI18N
                                HudsonVersion.SUPPORTED_VERSION));
                        return;
                    case INCORRECT_REDIRECTS:
                        problem(NbBundle.getMessage(InstanceDialog.class,
                                "MSG_incorrect_redirects")); //NOI18N
                        return;
                    default:
                        problem(NbBundle.getMessage(InstanceDialog.class,
                                "MSG_FailedToConnect")); //NOI18N
                        return;
                }
                created = HudsonManager.addInstance(panel.getDisplayName(),
                        panel.getUrl(), panel.getSyncTime(), true);

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dispose();
                        UI.selectNode(panel.getUrl());
                    }
                });
                // stats
                UsageLogging.logUI(NbBundle.getBundle(InstanceDialog.class),
                        "UI_HUDSON_SERVER_REGISTERED");                 //NOI18N
                UsageLogging.logUsage(InstanceDialog.class,
                        "USG_HUDSON_SERVER_REGISTERED");                //NOI18N
            }
            private void problem(final String explanation) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        addButton.setEnabled(true);
                        panel.checkFailed(explanation);
                        dialog.pack();
                    }
                });
            }
        });
    }

}
