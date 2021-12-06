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

import org.netbeans.modules.versionvault.client.OutputWindowNotificationListener;
import org.netbeans.modules.versionvault.client.UnCheckoutCommand;
import org.netbeans.modules.versionvault.client.AfterCommandRefreshListener;
import org.netbeans.modules.versionvault.client.CheckoutCommand;
import org.netbeans.modules.versionvault.FileStatusCache;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.netbeans.modules.versioning.util.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.Dialog;
import java.io.File;
import java.util.*;

import java.util.ArrayList;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.ClearcaseModuleConfig;
import org.netbeans.modules.versionvault.FileInformation;

import org.netbeans.modules.versionvault.util.ProgressSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Checks all files/folders in the context out, making them editable by the user.
 * 
 * @author Maros Sandor
 */
public class CheckoutAction extends AbstractAction {

    private static final int STATUS_DISABLED    = 0;
    private static final int STATUS_CHECKOUT    = 1;
    private static final int STATUS_UNCHECKOUT  = 2;
    
    static final String RECENT_CHECKOUT_MESSAGES = "checkout.messages"; 

    private static int ALLOW_CHECKOUT = FileInformation.STATUS_VERSIONED_UPTODATE | FileInformation.STATUS_VERSIONED_HIJACKED;
    private static int ALLOW_UNCO = FileInformation.STATUS_VERSIONED_CHECKEDOUT;
    
    private final VCSContext    context;
    private final int           status;

    public CheckoutAction(VCSContext context) {
        this.context = context;
        status = getActionStatus();
        putValue(Action.NAME, status == STATUS_UNCHECKOUT ? NbBundle.getMessage(CheckoutAction.class, "Action_Uncheckout_Name") : NbBundle.getMessage(CheckoutAction.class, "Action_Checkout_Name")); //NOI18N
    }

    private int getActionStatus() {
        FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
        int actionStatus = STATUS_DISABLED;
        Set<File> files = context.getFiles();
        for (File file : files) {
            if ((cache.getInfo(file).getStatus() & ALLOW_CHECKOUT) != 0) {
                if (actionStatus == STATUS_UNCHECKOUT) return STATUS_DISABLED;
                actionStatus = STATUS_CHECKOUT;
            }                
            if ((cache.getInfo(file).getStatus() & ALLOW_UNCO) != 0) {
                if (actionStatus == STATUS_CHECKOUT) return STATUS_DISABLED;
                actionStatus = STATUS_UNCHECKOUT;
            }
        }
        return actionStatus;
    }
    
    @Override
    public boolean isEnabled() {
        return status != STATUS_DISABLED;
    }
    
    public void actionPerformed(ActionEvent ev) {
        Utils.logVCSActionEvent("CC");
        Set<File> roots = context.getFiles();
        switch (status) {
        case STATUS_CHECKOUT:
            performCheckout(roots.toArray(new File[roots.size()]), Utils.getContextDisplayName(context));
            break;
        case STATUS_UNCHECKOUT:
            performUncheckout(roots.toArray(new File[roots.size()]));
            break;
        }
    }
    
    private void performUncheckout(final File [] files) {
        String contextTitle = Utils.getContextDisplayName(context);
        JButton unCheckoutButton = new JButton(); 
        unCheckoutButton.setToolTipText(NbBundle.getMessage(CheckoutAction.class, "TT_UncheckoutAction"));
        UncheckoutPanel panel = new UncheckoutPanel();

        panel.cbKeep.setEnabled(false);
        for (File file : files) {
            if(file.isFile()) {
                panel.cbKeep.setEnabled(true);        
                panel.onlyFolders = false;
                break;
            }
        }
        
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(CheckoutAction.class, "CTL_UncheckoutDialog_Title", contextTitle)); // NOI18N
        dd.setModal(true);
        dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        Mnemonics.setLocalizedText(unCheckoutButton, NbBundle.getMessage(CheckoutAction.class, "CTL_UncheckoutDialog_Unheckout")); //NOI18N
        
        dd.setOptions(new Object[] {unCheckoutButton, DialogDescriptor.CANCEL_OPTION}); // NOI18N
        dd.setHelpCtx(new HelpCtx("org.netbeans.modules.versionvault.ui.checkout.Uncheckout"));
                
        panel.putClientProperty("contentTitle", contextTitle);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);        
        dialog.addWindowListener(new DialogBoundsPreserver(ClearcaseModuleConfig.getPreferences(), "uncheckout.dialog")); // NOI18N       
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CheckoutAction.class, "ACSD_UncheckoutDialog")); // NOI18N
        dialog.pack();        
        dialog.setVisible(true);
        
        Object value = dd.getValue();
        if (value != unCheckoutButton) return;

        final boolean keepFiles = panel.cbKeep.isSelected();
        final boolean expand = panel.cbRecursively.isSelected();
        final ProgressSupport ps = new FileStatusCache.RefreshSupport(Clearcase.getInstance().getClient().getRequestProcessor(),
                context, NbBundle.getMessage(CheckoutAction.class, "Progress_Undoing_Checkout_Preparing")) { //NOI18N
            @Override
            protected void perform() {
                File[] targetFiles = files;
                // list target files recursively if user selected the option
                if (expand) {
                    FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();

                    // refresh the cache first so we will
                    // know all uncheckout candidates
                    refresh();

                    // get all files to be un-checked out
                    targetFiles = cache.listFiles(context, ALLOW_UNCO);
                }
                if (isCanceled()) {
                    return;
                }

                // the direct children for every root have to be refeshed as
                // the unceheckout might have changed the files structure -
                // - newly added file won't be referenced etc.
                final Set<File> filesToRefresh = new HashSet<File>();
                for (File file : targetFiles) {
                    filesToRefresh.add(file);
                    File[] children = file.listFiles();
                    if (children != null) {
                        for (File child : children) {
                            filesToRefresh.add(child);
                        }
                    }
                }
        
                if (isCanceled()) {
                    return;
                }

        UnCheckoutCommand cmd = 
                new UnCheckoutCommand(
                        targetFiles,
                    keepFiles, 
                    new AfterCommandRefreshListener(filesToRefresh.toArray(new File[filesToRefresh.size()])), 
                    new OutputWindowNotificationListener());
        Clearcase.getInstance().getClient().post(NbBundle.getMessage(CheckoutAction.class, "Progress_Undoing_Checkout"), cmd); //NOI18N
    }
        };
        ps.start();
    }

    private void performCheckout(final File[] files, String title) {
        JButton checkoutButton = new JButton(); 
        CheckoutPanel panel = new CheckoutPanel();
        checkoutButton.setToolTipText(NbBundle.getMessage(CheckoutAction.class, "TT_CheckoutAction"));
        
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(CheckoutAction.class, "CTL_CheckoutDialog_Title", title)); // NOI18N
        dd.setModal(true);        
        org.openide.awt.Mnemonics.setLocalizedText(checkoutButton, org.openide.util.NbBundle.getMessage(CheckoutAction.class, "CTL_CheckoutDialog_Checkout")); //NOI18N
        
        dd.setOptions(new Object[] {checkoutButton, DialogDescriptor.CANCEL_OPTION}); // NOI18N
        dd.setHelpCtx(new HelpCtx("org.netbeans.modules.versionvault.ui.checkout.Checkout"));
                
        panel.putClientProperty("contentTitle", title);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);        
        dialog.addWindowListener(new DialogBoundsPreserver(ClearcaseModuleConfig.getPreferences(), "checkout.dialog")); // NOI18N       
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CheckoutAction.class, "ACSD_CheckoutDialog")); // NOI18N
        dialog.pack();        
        dialog.setVisible(true);
        
        Object value = dd.getValue();
        if (value != checkoutButton) return;
        
        final String message = panel.taMessage.getText();
        final boolean doReserved = panel.cbReserved.isSelected();
        final boolean doUnreservedNonMaster = !panel.cbReserved.isSelected() && panel.cbNonMaster.isSelected();
        final boolean expand = panel.cbRecursive.isSelected();

        final ProgressSupport ps = new FileStatusCache.RefreshSupport(Clearcase.getInstance().getClient().getRequestProcessor(),
                context, NbBundle.getMessage(CheckoutAction.class, "Progress_Checkout_Preparing")) { //NOI18N
            @Override
            protected void perform() {
                File[] targetFiles = files;
                // list target files recursively if user selected the option
                if (expand) {
                    FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
                    // refresh the cache first so we will
                    // know all checkout candidates
                    refresh();
                    // get all files to be checked out
                    targetFiles = cache.listFiles(context, ALLOW_CHECKOUT);
                }
                if (isCanceled()) {
                    return;
                }

        Utils.insert(ClearcaseModuleConfig.getPreferences(), RECENT_CHECKOUT_MESSAGES, message.trim(), 20);
        CheckoutCommand cmd = 
                new CheckoutCommand(
                        targetFiles,
                        message, 
                        doReserved ? 
                            CheckoutCommand.Reserved.Reserved :
                            doUnreservedNonMaster ?
                                CheckoutCommand.Reserved.UnreservedNonMaster :
                                CheckoutCommand.Reserved.Unreserved,
                        false, 
                        new AfterCommandRefreshListener(targetFiles),
                        new OutputWindowNotificationListener());                
        Clearcase.getInstance().getClient().post(NbBundle.getMessage(CheckoutAction.class, "Progress_Checkout"), cmd); //NOI18N
    }        
        };
        ps.start();
}
}
