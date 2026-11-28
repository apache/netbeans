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
package org.netbeans.modules.subversion.ui.relocate;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author  Peter Pis
 */
public class RelocateAction extends ContextAction {
         
    @Override
    protected boolean enable(Node[] nodes) {
        if(nodes.length != 1) {
            return false;
        }
        if (isCacheReady()) {
            final Context ctx = getCachedContext(nodes);
            File[] roots = ctx.getRootFiles();
            if(roots == null || roots.length < 1) {
                return false;
            }
            for (File file : roots) {
                if(file.isDirectory()) {
                    return true; // at least one dir
                }
            }
        }
        return false;
    }
    
    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void validate(RelocatePanel panel, JButton btnOk) {
        String urlString = getNewUrl(panel);
        if (urlString.isEmpty() || urlString.equals(panel.getCurrentURL().getText().trim())) {
            btnOk.setEnabled(false);
            return;
        }
        try {
            new SVNUrl(urlString);
            btnOk.setEnabled(true);
        } catch (MalformedURLException e) {
            btnOk.setEnabled(false);
        }
    }

    private static String getNewUrl(RelocatePanel panel) {
        Component editor = panel.getNewURL().getEditor().getEditorComponent();
        if (editor instanceof JTextComponent jTextComponent) {
            return jTextComponent.getText().trim();
        }
        Object item = panel.getNewURL().getEditor().getItem();
        return item != null ? item.toString().trim() : "";
    }

    @Override
    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_Relocate_Title";
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        ResourceBundle loc = NbBundle.getBundle(RelocateAction.class);
        
        final Context ctx = getContext(nodes);
        File[] roots = ctx.getRootFiles();
        if (roots == null) {
            return;
        }
        
        final RelocatePanel panel = new RelocatePanel();

        roots = SvnUtils.getActionRoots(ctx);
        if(roots == null || roots.length == 0) {
            return;
        }

        // grab the first file:
        // 1.) it can be only a folder - see isEnabled
        // 2.) even if its a dataobject with more files, we just don't care,
        // the action will affect the whole working copy
        final File root = roots[0];

        SVNUrl repositoryUrl;
        try {
            repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        if(repositoryUrl == null) {
            Subversion.LOG.log(Level.WARNING, "Could not retrieve repository root for context file {0}", new Object[]{ root });
            return;
        }
        final String wc = root.getAbsolutePath();
        panel.getCurrentURL().setText(repositoryUrl.toString());
        panel.getWorkingCopy().setText(wc);

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        SvnUtils.getKnownRepositoryUrls().forEach(model::addElement);
        panel.getNewURL().setModel(model);
        panel.getNewURL().setSelectedItem("");
        Component editor = panel.getNewURL().getEditor().getEditorComponent();
        if (editor instanceof JTextComponent jTextComponent) {
            jTextComponent.setText("");
        }

        final JButton btnRelocate = new JButton(loc.getString("CTL_Relocate_Action_Name"));
        btnRelocate.setEnabled(false);
        btnRelocate.setToolTipText(loc.getString("TT_Relocate_Action"));

        if (editor instanceof JTextComponent jTextComponent) {
            jTextComponent.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    validate(panel, btnRelocate);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    validate(panel, btnRelocate);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    validate(panel, btnRelocate);
                }
            });
        }
        panel.getNewURL().addActionListener(e -> validate(panel, btnRelocate));

        JButton btnCancel = new JButton(loc.getString("CTL_Relocate_Action_Cancel"));
        btnCancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RelocateAction.class, "ACSD_Relocate_Action_Cancel"));  // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(panel,  loc.getString("CTL_Relocate_Dialog_Title"),  true, new Object [] {btnRelocate, btnCancel}, btnRelocate, DialogDescriptor.BOTTOM_ALIGN, null, null);
        descriptor.setClosingOptions(null);
        descriptor.setHelpCtx(new HelpCtx(RelocateAction.class));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(loc.getString("ACSD_Relocate"));
        
        dialog.setVisible(true);
        if (descriptor.getValue() != btnRelocate) 
            return;
        
        final String newUrl = getNewUrl(panel);
        final SVNUrl newSvnUrl;
        try {
            newSvnUrl = new SVNUrl(newUrl);
        } catch (MalformedURLException ex) {
            // shouldn't happen since newUrl has been validated
            return;
        }

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(newSvnUrl);
        final SVNUrl fromUrl = repositoryUrl;
        SvnProgressSupport support = new SvnProgressSupport() {
            @Override
            protected void perform() {                    
                try {
                    SvnClient client = Subversion.getInstance().getClient(newSvnUrl, this);
                    client.relocate(fromUrl.toString(), newUrl, wc, true);
                    patchCache();
                    RepositoryConnection rc = SvnModuleConfig.getDefault().getRepositoryConnection(newSvnUrl.toString());
                    if (rc == null) {
                        rc = new RepositoryConnection(newUrl);
                    }
                    SvnModuleConfig.getDefault().insertRecentUrl(rc);
                } catch (SVNClientException ex) {
                    annotate(ex);
                } 
            }

            private void patchCache () {
                FileStatusCache cache = Subversion.getInstance().getStatusCache();
                // refresh status only for status entries already cached
                File[] files = cache.listFiles(new File[] { root }, FileInformation.STATUS_MANAGED);
                for (File f : files) {
                    FileInformation fi = cache.getCachedStatus(f);
                    if (fi != null && fi.getEntry(null) != null) {
                        // cache needs to be refreshed
                        cache.refresh(f, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                    }
                }
            }
        };
        support.start(rp, newSvnUrl, loc.getString("LBL_Relocate_Progress"));
    }
}
