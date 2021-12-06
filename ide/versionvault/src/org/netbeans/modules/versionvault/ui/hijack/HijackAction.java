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
package org.netbeans.modules.versionvault.ui.hijack;

import org.netbeans.modules.versionvault.FileStatusCache;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.Dialog;
import java.io.File;
import java.util.*;

import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.FileInformation;
import org.netbeans.modules.versionvault.ClearcaseModuleConfig;
import org.netbeans.modules.versionvault.client.AfterCommandRefreshListener;
import org.netbeans.modules.versionvault.client.OutputWindowNotificationListener;
import org.netbeans.modules.versionvault.client.UpdateCommand;
import org.netbeans.modules.versionvault.util.ClearcaseUtils;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;

/**
 * Hijacks all files/folders in the context, making them editable by the user.
 * 
 * @author Maros Sandor
 */
public class HijackAction extends AbstractAction {

    private static final int STATUS_DISABLED    = 0;
    private static final int STATUS_HIJACK      = 1;
    private static final int STATUS_UNHIJACK    = 2;
    
    private static int ALLOW_HIJACK     = FileInformation.STATUS_VERSIONED_UPTODATE;
    private static int ALLOW_UNHIJACK   = FileInformation.STATUS_VERSIONED_HIJACKED;
    
    private final VCSContext    context;
    private final int           status;

    public HijackAction(VCSContext context) {
        this.context = context;
        status = getActionStatus();
        putValue(Action.NAME, status == STATUS_UNHIJACK ? NbBundle.getMessage(HijackAction.class, "Action_Unhijack_Name") : NbBundle.getMessage(HijackAction.class, "Action_Hijack_Name")); //NOI18N
    }

    private int getActionStatus() {
        Set<File> files = context.getFiles();
        for (File file : files) {
            if(file.isDirectory()) return STATUS_DISABLED;
        }
        if (!ClearcaseUtils.containsSnapshot(context)) return STATUS_DISABLED;
        FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
        int actionStatus = STATUS_DISABLED;
        for (File file : files) {
            if ((cache.getInfo(file).getStatus() & ALLOW_HIJACK) != 0) {
                if (actionStatus == STATUS_UNHIJACK) return STATUS_DISABLED;
                actionStatus = STATUS_HIJACK;
            }                
            if ((cache.getInfo(file).getStatus() & ALLOW_UNHIJACK) != 0) {
                if (actionStatus == STATUS_HIJACK) return STATUS_DISABLED;
                actionStatus = STATUS_UNHIJACK;
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
        case STATUS_HIJACK:
            hijack(roots.toArray(new File[roots.size()]));
            break;
        case STATUS_UNHIJACK:
            performUnhijack(roots.toArray(new File[roots.size()]));
            break;
        }
    }
    
    private void performUnhijack(File [] files) {
        String contextTitle = Utils.getContextDisplayName(context);
        JButton unHijackButton = new JButton(); 
        unHijackButton.setToolTipText(NbBundle.getMessage(HijackAction.class, "TT_UnhijackAction"));
        UnhijackPanel panel = new UnhijackPanel();

        panel.cbKeep.setEnabled(false);
        for (File file : files) {
            if(file.isFile()) {
                panel.cbKeep.setEnabled(true);        
                break;
            }
        }
        
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(HijackAction.class, "CTL_UnhijackDialog_Title", contextTitle)); // NOI18N
        dd.setModal(true);
        dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        Mnemonics.setLocalizedText(unHijackButton, NbBundle.getMessage(HijackAction.class, "CTL_UnhijackDialog_Unhijack")); //NOI18N
        
        dd.setOptions(new Object[] {unHijackButton, DialogDescriptor.CANCEL_OPTION}); // NOI18N
        dd.setHelpCtx(new HelpCtx("org.netbeans.modules.versionvault.ui.hijack.Unhijack"));
                
        panel.putClientProperty("contentTitle", contextTitle);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);        
        dialog.addWindowListener(new DialogBoundsPreserver(ClearcaseModuleConfig.getPreferences(), "unhijack.dialog")); // NOI18N       
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(HijackAction.class, "ACSD_UnhijackDialog")); // NOI18N
        dialog.pack();        
        dialog.setVisible(true);
        
        Object value = dd.getValue();
        if (value != unHijackButton) return;

        boolean keepFiles = panel.cbKeep.isSelected();
        UpdateCommand cmd = 
                new UpdateCommand(
                    files, 
                    keepFiles ? UpdateCommand.HijackedAction.BackupAndOverwrite : UpdateCommand.HijackedAction.Overwrite,
                    new AfterCommandRefreshListener(files), 
                    new OutputWindowNotificationListener());
        Clearcase.getInstance().getClient().post(NbBundle.getMessage(HijackAction.class, "Progress_Undoing_Hijack"), cmd); //NOI18N
    }

    /**
     * Hijacks files, make them r/w.
     * 
     * @param files files to hijack
     * @return true if all supplies files are now mutable, false otherwise
     */
    public static boolean hijack(File ... files) {
        boolean allWritable = true;
        for (File file : files) {
            if (file.isFile() && !file.canWrite()) {
                Utils.setReadOnly(file, false);
                file.setLastModified(System.currentTimeMillis());
                allWritable &= file.canWrite();
            }
        }
        ClearcaseUtils.afterCommandRefresh(files, false);
        return allWritable;
    }        
}
