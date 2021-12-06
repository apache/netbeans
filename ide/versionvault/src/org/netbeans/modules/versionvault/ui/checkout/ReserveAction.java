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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.ui.checkout;

import org.netbeans.modules.versionvault.client.ReserveCommand;
import org.netbeans.modules.versionvault.client.OutputWindowNotificationListener;
import org.netbeans.modules.versionvault.client.AfterCommandRefreshListener;
import org.netbeans.modules.versionvault.client.UnreserveCommand;
import org.netbeans.modules.versionvault.FileInformation;
import org.netbeans.modules.versionvault.FileStatusCache;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.ClearcaseModuleConfig;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.Dialog;
import org.netbeans.modules.versionvault.util.ProgressSupport;

/**
 * Checks all files/folders in the context out, making them editable by the user.
 * 
 * @author Maros Sandor
 */
public class ReserveAction extends AbstractAction {

    private static final int STATUS_DISABLED    = 0;
    private static final int STATUS_RESERVE     = 1;
    private static final int STATUS_UNRESERVE   = 2;
    
    private static int ALLOW_RESERVE = FileInformation.STATUS_VERSIONED_CHECKEDOUT | FileInformation.STATUS_UNRESERVED;
    private static int ALLOW_UNRESERVE = FileInformation.STATUS_VERSIONED_CHECKEDOUT;
    
    private final VCSContext    context;
    private final int           status;

    public ReserveAction(VCSContext context) {
        this.context = context;
        status = getActionStatus();
        putValue(Action.NAME, status == STATUS_UNRESERVE ? NbBundle.getMessage(ReserveAction.class, "Action_Unreserve_Name") : NbBundle.getMessage(ReserveAction.class, "Action_Reserve_Name")); //NOI18N
    }

    private int getActionStatus() {
        FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
        int status = STATUS_DISABLED;
        Set<File> files = context.getFiles();
        for (File file : files) {
            if (cache.getInfo(file).getStatus() == ALLOW_RESERVE) {
                if (status == STATUS_UNRESERVE) return STATUS_DISABLED;
                status = STATUS_RESERVE;
            }                
            if (cache.getInfo(file).getStatus() == ALLOW_UNRESERVE) {
                if (status == STATUS_RESERVE) return STATUS_DISABLED;
                status = STATUS_UNRESERVE;
            }
        }
        return status;
    }
    
    @Override
    public boolean isEnabled() {
        return status != STATUS_DISABLED;
    }
    
    public void actionPerformed(ActionEvent ev) {
        Utils.logVCSActionEvent("CC");
        Set<File> roots = context.getFiles();
        switch (status) {
        case STATUS_RESERVE:
            performReserve(roots.toArray(new File[roots.size()]), Utils.getContextDisplayName(context));
            break;
        case STATUS_UNRESERVE:
            performUnreserve(roots.toArray(new File[roots.size()]), Utils.getContextDisplayName(context));
            break;
        }
    }
    
    private void performUnreserve(final File [] files, String title) {
        JButton reserveButton = new JButton(); 
        reserveButton.setToolTipText(NbBundle.getMessage(ReserveAction.class, "TT_UnreserveAction"));
        CheckoutPanel panel = new CheckoutPanel();
        panel.cbReserved.setVisible(false);
        
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ReserveAction.class, "CTL_UnreserveDialog_Title", title)); // NOI18N
        dd.setModal(true);        
        Mnemonics.setLocalizedText(reserveButton, NbBundle.getMessage(ReserveAction.class, "CTL_UnreserveDialog_Unreserve")); //NOI18N
        
        dd.setOptions(new Object[] {reserveButton, DialogDescriptor.CANCEL_OPTION}); // NOI18N
        dd.setHelpCtx(new HelpCtx(" org.netbeans.modules.versionvault.ui.checkout.Unreserve"));
                
        panel.putClientProperty("contentTitle", title);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);        
        dialog.addWindowListener(new DialogBoundsPreserver(ClearcaseModuleConfig.getPreferences(), "checkout.dialog")); // NOI18N       
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ReserveAction.class, "ACSD_UnreserveDialog")); // NOI18N
        dialog.pack();        
        dialog.setVisible(true);
        
        Object value = dd.getValue();
        if (value != reserveButton) return;
        
        final boolean expand = panel.cbRecursive.isSelected();
        final String message = panel.taMessage.getText();
        final ProgressSupport ps = new FileStatusCache.RefreshSupport(Clearcase.getInstance().getClient().getRequestProcessor(),
                context, NbBundle.getMessage(CheckoutAction.class, "Progress_Modifying_Checkout_Preparing")) { //NOI18N
            @Override
            protected void perform() {
                File[] targetFiles = files;
                // list target files recursively if user selected the option
                if (expand) {
                    FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
                    // refresh the cache first so we will
                    // know all unreserve candidates
                    refresh();
                    // get all files to be reserved
                    targetFiles = cache.listFiles(context, ALLOW_UNRESERVE, true);
                }
                if (isCanceled()) {
                    return;
                }

        Utils.insert(ClearcaseModuleConfig.getPreferences(), CheckoutAction.RECENT_CHECKOUT_MESSAGES, message.trim(), 20);
        UnreserveCommand cmd = 
                new UnreserveCommand(
                        targetFiles,
                    message, 
                        new AfterCommandRefreshListener(targetFiles),
                    new OutputWindowNotificationListener());                
        Clearcase.getInstance().getClient().post(NbBundle.getMessage(ReserveAction.class, "Progress_Modifying_Checkout"), cmd); //NOI18N
    }
        };
        ps.start();
    }

    public void performReserve(final File[] files, String title) {
        JButton reserveButton = new JButton(); 
        reserveButton.setToolTipText(NbBundle.getMessage(ReserveAction.class, "TT_ReserveAction"));
        CheckoutPanel panel = new CheckoutPanel();
        panel.cbReserved.setVisible(false);
        
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ReserveAction.class, "CTL_ReserveDialog_Title", title)); // NOI18N
        dd.setModal(true);        
        Mnemonics.setLocalizedText(reserveButton, NbBundle.getMessage(ReserveAction.class, "CTL_ReserveDialog_Reserve")); //NOI18N
        
        dd.setOptions(new Object[] {reserveButton, DialogDescriptor.CANCEL_OPTION}); // NOI18N
        dd.setHelpCtx(new HelpCtx(" org.netbeans.modules.versionvault.ui.checkout.Reserve"));
                
        panel.putClientProperty("contentTitle", title);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);        
        dialog.addWindowListener(new DialogBoundsPreserver(ClearcaseModuleConfig.getPreferences(), "checkout.dialog")); // NOI18N       
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ReserveAction.class, "ACSD_ReserveDialog")); // NOI18N
        dialog.pack();        
        dialog.setVisible(true);
        
        Object value = dd.getValue();
        if (value != reserveButton) return;
        
        final String message = panel.taMessage.getText();
        final boolean expand = panel.cbRecursive.isSelected();
        final ProgressSupport ps = new FileStatusCache.RefreshSupport(Clearcase.getInstance().getClient().getRequestProcessor(),
                context, NbBundle.getMessage(CheckoutAction.class, "Progress_Modifying_Checkout_Preparing")) { //NOI18N
            @Override
            protected void perform() {
                File[] targetFiles = files;
                // list target files recursively if user selected the option
                if (expand) {
                    FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
                    // refresh the cache first so we will
                    // know all reserve candidates
                    refresh();
                    // get all files to be reserved
                    targetFiles = cache.listFiles(context, ALLOW_RESERVE, true);
                }
                if (isCanceled()) {
                    return;
                }
        ReserveCommand cmd = 
                new ReserveCommand(
                        targetFiles,
                    message, 
                    new OutputWindowNotificationListener(), 
                        new AfterCommandRefreshListener(targetFiles));
        Clearcase.getInstance().getClient().post(NbBundle.getMessage(ReserveAction.class, "Progress_Modifying_Checkout"), cmd); //NOI18N
    }        
        };
        ps.start();
}
}
