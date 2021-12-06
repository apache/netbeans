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
package org.netbeans.modules.versionvault.ui.checkin;

import java.awt.BorderLayout;
import javax.swing.event.TableModelEvent;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.VersioningOutputManager;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.Dialog;
import java.io.File;
import java.util.*;

import java.util.ArrayList;
import javax.swing.event.TableModelListener;
import org.netbeans.modules.versionvault.*;
import org.netbeans.modules.versionvault.client.AfterCommandRefreshListener;
import org.netbeans.modules.versionvault.ui.add.AddAction;
import org.netbeans.modules.versionvault.client.OutputWindowNotificationListener;
import org.netbeans.modules.versionvault.client.CheckinCommand;
import org.netbeans.modules.versionvault.util.ProgressSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

/**
 * Checkin action.
 * 
 * @author Maros Sandor
 */
public class CheckinAction extends AbstractAction {
    
    private final VCSContext context;
    protected final VersioningOutputManager voutput;
    
    static int ALLOW_CHECKIN = 
            FileInformation.STATUS_VERSIONED_CHECKEDOUT |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;

    static String RECENT_CHECKIN_MESSAGES = "checkin.messages";
    
    private File[] files;
    
    public CheckinAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
        voutput = VersioningOutputManager.getInstance();
    }
    
    @Override
    public boolean isEnabled() {
        FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
        Set<File> roots = context.getRootFiles();        
        for (File root : roots) {
            FileInformation info = cache.getCachedInfo(root);
            if (info != null && info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED) continue;
            if(root.isDirectory()) {
                return true;
            }
            if(info != null && ((info.getStatus() & ALLOW_CHECKIN) != 0)) {
                return true;
            }
        }
        return false;
    }
    
    public void actionPerformed(ActionEvent ev) {
        LifecycleManager.getDefault().saveAll();
        Utils.logVCSActionEvent("CC");
        String contextTitle = Utils.getContextDisplayName(context);
        final JButton checkinButton = new JButton(); 
        checkinButton.setToolTipText(NbBundle.getMessage(CheckinAction.class, "TT_CheckinAction"));
        checkinButton.setEnabled(false);
        JButton cancelButton = new JButton(NbBundle.getMessage(CheckinAction.class, "Checkin_Cancel")); //NOI18N
        cancelButton.setToolTipText(NbBundle.getMessage(CheckinAction.class, "TT_CancelAction"));
        
        final CheckinPanel panel = new CheckinPanel();        
        
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(CheckinAction.class, "CTL_CheckinDialog_Title", contextTitle)); // NOI18N
        dd.setModal(true);        
        org.openide.awt.Mnemonics.setLocalizedText(checkinButton, org.openide.util.NbBundle.getMessage(CheckinAction.class, "CTL_CheckinDialog_Checkin")); //NOI18N
        
        dd.setOptions(new Object[] {checkinButton, cancelButton}); // NOI18N
        dd.setHelpCtx(new HelpCtx(CheckinAction.class));

        panel.cbForceUnmodified.setSelected(ClearcaseModuleConfig.getForceUnmodifiedCheckin());      
        panel.cbPreserveTime.setSelected(ClearcaseModuleConfig.getPreserveTimeCheckin());      
        
        final CheckinTable checkinTable = new CheckinTable(panel.jLabel2, CheckinTable.CHECKIN_COLUMNS, new String [] { CheckinTableModel.COLUMN_NAME_NAME });        
        panel.setCheckinTable(checkinTable);
        checkinTable.getTableModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                if(checkinTable.getTableModel().getRowCount() < 1) {
                    checkinButton.setEnabled(false);        
                    return;
                }                        
                Map<ClearcaseFileNode, CheckinOptions> filesToCheckin = checkinTable.getAddFiles();
                boolean enabled = false;
                for (CheckinOptions option : filesToCheckin.values()) {                    
                    if (option != CheckinOptions.EXCLUDE) {
                        enabled = true;
                        break;
                    }
                }
                checkinButton.setEnabled(enabled);        
            }
        });
        computeNodes(checkinTable, cancelButton, panel);
        
        
        panel.putClientProperty("contentTitle", contextTitle);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);        
        dialog.addWindowListener(new DialogBoundsPreserver(ClearcaseModuleConfig.getPreferences(), "add.dialog")); // NOI18N       
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CheckinAction.class, "ACSD_CheckinDialog")); // NOI18N
        dialog.pack();        
        dialog.setVisible(true);                
                
        Object value = dd.getValue();
        if (value != checkinButton) return;

        ProgressSupport ps = new ProgressSupport(Clearcase.getInstance().getClient().getRequestProcessor(), NbBundle.getMessage(CheckinAction.class, "Progress_Checking_in")) { //NOI18N
            @Override
            protected void perform() {
                performCheckin(panel, checkinTable, this);
            }
        };   
        ps.start();
    }

    // XXX temporary solution...
    private void computeNodes(final CheckinTable checkinTable, JButton cancel, final CheckinPanel checkinPanel) {
        final ProgressSupport ps = new FileStatusCache.RefreshSupport(new RequestProcessor("Clearcase-AddTo"), context, NbBundle.getMessage(CheckinAction.class, "Progress_Preparing_Checkin"), cancel) { //NOI18N
            @Override
            protected void perform() {
                try {
                    checkinPanel.progressPanel.setVisible(true);
                    FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();

                    // refresh the cache first so we will
                    // know all checkin candidates
                    refresh();
                            
                    // get all files to be checked in
                    File [] files = cache.listFiles(context, FileInformation.STATUS_LOCAL_CHANGE);
                    List<ClearcaseFileNode> nodes = new ArrayList<ClearcaseFileNode>(files.length);
                    for (File file : files) {
                        nodes.add(new ClearcaseFileNode(file));   
                    }                            
                    ClearcaseFileNode[] fileNodes = nodes.toArray(new ClearcaseFileNode[nodes.size()]);
                    checkinTable.setNodes(fileNodes);
                } finally {
                    checkinPanel.progressPanel.setVisible(false);                    
                }
            }
        };
        checkinPanel.barPanel.add(ps.getProgressComponent(), BorderLayout.CENTER);                                
        ps.start();        
    }

    /**
     * Programmatically invoke the checkin action on some context.
     * 
     * @param context a context to check in
     */
    public static void checkin(VCSContext context) {
        new CheckinAction("", context).actionPerformed(null);        
    }

    private void performCheckin(CheckinPanel panel, final CheckinTable checkinTable, ProgressSupport ps) {
        String message = panel.taMessage.getText();
        boolean forceUnmodified = panel.cbForceUnmodified.isSelected();
        boolean preserveTime = panel.cbPreserveTime.isSelected();

        ps.setDisplayMessage(NbBundle.getMessage(CheckinAction.class, "Progress_Checkin_Adding_new_Files")); //NOI18N
        Map<ClearcaseFileNode, CheckinOptions> filesToCheckin = checkinTable.getAddFiles();
        // XXX false means they stay checked out and 
        // still have to be checked in later. reconsider using true instead
        AddAction.addFiles(null, false, filesToCheckin, ps);  

        ps.setDisplayMessage(NbBundle.getMessage(CheckinAction.class, "Progress_Checking_in")); //NOI18N
        List<String> addExclusions = new ArrayList<String>();
        
        List<String> removeExclusions = new ArrayList<String>();
        List<File> ciFiles = new ArrayList<File>();
        for (Map.Entry<ClearcaseFileNode, CheckinOptions> entry : filesToCheckin.entrySet()) {
            File file = entry.getKey().getFile();
            if (entry.getValue() != CheckinOptions.EXCLUDE) {
                ciFiles.add(file);
                removeExclusions.add(file.getAbsolutePath());
            } else {
                addExclusions.add(file.getAbsolutePath());
            }
        }
        ClearcaseModuleConfig.addExclusionPaths(addExclusions);
        ClearcaseModuleConfig.removeExclusionPaths(removeExclusions);
        ClearcaseModuleConfig.setForceUnmodifiedCheckin(forceUnmodified);
        ClearcaseModuleConfig.setPreserveTimeCheckin(preserveTime);
        Utils.insert(ClearcaseModuleConfig.getPreferences(), RECENT_CHECKIN_MESSAGES, message.trim(), 20);

        files = ciFiles.toArray(new File[ciFiles.size()]);
        CheckinCommand cmd = new CheckinCommand(files, message, forceUnmodified, preserveTime, new OutputWindowNotificationListener(), new AfterCommandRefreshListener(files));
        Clearcase.getInstance().getClient().exec(cmd, true, ps);
    }
}
