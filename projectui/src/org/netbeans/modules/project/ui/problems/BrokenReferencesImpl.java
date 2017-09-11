/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.project.ui.problems;

import java.awt.Dialog;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.uiapi.BrokenReferencesImplementation;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;
import static org.netbeans.modules.project.ui.problems.Bundle.*;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=BrokenReferencesImplementation.class)
public class BrokenReferencesImpl implements BrokenReferencesImplementation {

    private static final boolean suppressBrokenRefAlert = Boolean.getBoolean("BrokenReferencesSupport.suppressBrokenRefAlert"); //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(BrokenReferencesImpl.class);
    private static int BROKEN_ALERT_TIMEOUT = 1000;

    private BrokenReferencesModel.Context context;
    private RequestProcessor.Task rpTask;
    

    @Override
    @NbBundle.Messages({
        "CTL_Broken_References_Resolve=Resolve Problems...",
        "AD_Broken_References_Resolve=N/A",
        "CTL_Broken_References_Close=Close",
        "AD_Broken_References_Close=N/A",
        "MSG_Broken_References_Title=Open Project",
        "LBL_Broken_References_Resolve_Panel_Close=Close",
        "AD_Broken_References_Resolve_Panel_Close=N/A",
        "LBL_Broken_References_Resolve_Panel_Title=Resolve Project Problems"
    })
    public void showAlert(Project project) {
        Parameters.notNull("project", project); //NOI18N
        if (!BrokenReferencesSettings.isShowAgainBrokenRefAlert() || suppressBrokenRefAlert) {
            return;
        } else if (context == null) {
            assert rpTask == null;

            final Runnable task = new Runnable() {
                @Override
                public void run() {
                    final BrokenReferencesModel.Context ctx;
                    synchronized (BrokenReferencesImpl.this) {
                        rpTask = null;
                        ctx = context;
                    }
                    if (ctx == null) {
                        return;
                    }
                    try {
                        final JButton resolveOption = new JButton(CTL_Broken_References_Resolve());
                        resolveOption.getAccessibleContext().setAccessibleDescription(AD_Broken_References_Resolve());
                        JButton closeOption = new JButton (CTL_Broken_References_Close());
                        closeOption.getAccessibleContext().setAccessibleDescription(AD_Broken_References_Close());
                        DialogDescriptor dd = new DialogDescriptor(new BrokenReferencesAlertPanel(),
                            MSG_Broken_References_Title(),
                            true,
                            new Object[] {resolveOption, closeOption},
                            closeOption,
                            DialogDescriptor.DEFAULT_ALIGN,
                            null,
                            null);
                        dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
                        ctx.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                resolveOption.setVisible(!ctx.isEmpty());
                            }
                        });
                        resolveOption.setVisible(!ctx.isEmpty());
                        if (DialogDisplayer.getDefault().notify(dd) == resolveOption) {
                            final BrokenReferencesModel model = new BrokenReferencesModel(ctx, true);
                            final BrokenReferencesCustomizer customizer = new BrokenReferencesCustomizer(model);
                            JButton close = new JButton (Bundle.LBL_Broken_References_Resolve_Panel_Close());
                            close.getAccessibleContext ().setAccessibleDescription (Bundle.AD_Broken_References_Resolve_Panel_Close());
                            dd = new DialogDescriptor(customizer,
                                Bundle.LBL_Broken_References_Resolve_Panel_Title(),
                                true,
                                new Object[] {closeOption},
                                closeOption,
                                DialogDescriptor.DEFAULT_ALIGN,
                                null,
                                null);
                            customizer.setNotificationLineSupport(dd.createNotificationLineSupport());
                            DialogDisplayer.getDefault().notify(dd);
                        }
                    } finally {
                        synchronized (BrokenReferencesImpl.this) {
                            //Clean seen references and start from empty list
                            context = null;
                        }
                    }
                }
            };

            context = new BrokenReferencesModel.Context();
            rpTask = RP.create(new Runnable() {
                @Override
                public void run() {
                    WindowManager.getDefault().invokeWhenUIReady(task);
                }
            });
        }

        assert context != null;
        if (project != null) {
            context.offer(project);
        }
        if (rpTask != null) {
            //Not yet shown, move
            rpTask.schedule(BROKEN_ALERT_TIMEOUT);
        }
    }


    @NbBundle.Messages({
        "LBL_BrokenLinksCustomizer_Close=Close",
        "ACSD_BrokenLinksCustomizer_Close=N/A",
        "LBL_BrokenLinksCustomizer_Title=Resolve Project Problems - \"{0}\" Project"
    })
    @Override
    public void showCustomizer(@NonNull Project project) {
        Parameters.notNull("project", project); //NOI18N
        BrokenReferencesModel model = new BrokenReferencesModel(project);
        BrokenReferencesCustomizer customizer = new BrokenReferencesCustomizer(model);
        JButton close = new JButton (LBL_BrokenLinksCustomizer_Close()); // NOI18N
        close.getAccessibleContext ().setAccessibleDescription (ACSD_BrokenLinksCustomizer_Close()); // NOI18N
        String projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
        DialogDescriptor dd = new DialogDescriptor(customizer,
            LBL_BrokenLinksCustomizer_Title(projectDisplayName), // NOI18N
            true, new Object[] {close}, close, DialogDescriptor.DEFAULT_ALIGN, null, null);
        customizer.setNotificationLineSupport(dd.createNotificationLineSupport());
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(dd);
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
    }

}
