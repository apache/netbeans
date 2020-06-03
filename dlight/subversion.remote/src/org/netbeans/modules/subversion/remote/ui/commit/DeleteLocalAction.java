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

package org.netbeans.modules.subversion.remote.ui.commit;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 * Delete action enabled for new local files (not yet in repository).
 * It eliminates <tt>.svn/entries</tt> scheduling if exists too.
 *
 * 
 */
public final class DeleteLocalAction extends ContextAction {

    public static final int LOCALLY_DELETABLE_MASK = FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    @Override
    protected String getBaseName(Node [] activatedNodes) {
        return "Delete";  // NOI18N
    }

    @Override
    protected int getFileEnabledStatus() {
        return LOCALLY_DELETABLE_MASK;
    }
    
    @Override
    protected void performContextAction(final Node[] nodes) {
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(DeleteLocalAction.class, "CTL_DeleteLocal_Prompt")); // NOI18N
        descriptor.setTitle(NbBundle.getMessage(DeleteLocalAction.class, "CTL_DeleteLocal_Title")); // NOI18N
        descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
        descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

        Object res = DialogDisplayer.getDefault().notify(descriptor);
        if (res != NotifyDescriptor.YES_OPTION) {
            return;
        }
        
        final Context ctx = getContext(nodes);
        ProgressSupport support = new ContextAction.ProgressSupport(this, nodes, ctx) {
            @Override
            public void perform() {
                performDelete(ctx, this);
            }
        };
        support.start(createRequestProcessor(ctx));
    }
    
    public static void performDelete(final Context ctx, final SvnProgressSupport support) {

        final SvnClient client;
        try {
            client = Subversion.getInstance().getClient(ctx, support);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ctx, ex, true, true);
            return;
        }        

        if(support.isCanceled()) {
            return;
        }
        final VCSFileProxy[] files = ctx.getFiles();
        try {
            SvnUtils.runWithoutIndexing(new Callable<Void>() {
                @Override
                public Void call () throws Exception {
                    for (int i = 0; i < files.length; i++) {
                        if(support.isCanceled()) {
                            return null;
                        }

                        VCSFileProxy file = files[i];
                        FileObject fo = file.toFileObject();
                        if (fo != null) {
                            FileLock lock = null;
                            try {
                                try {
                                    client.revert(file, false);
                                } catch (SVNClientException ex) {
                                    SvnClientExceptionHandler.notifyException(ctx, ex, true, true);
                                }
                                lock = fo.lock();                    
                                fo.delete(lock);       
                            } catch (IOException e) {
                                Subversion.LOG.log(Level.SEVERE, NbBundle.getMessage(DeleteLocalAction.class, "MSG_Cannot_lock", file.getPath()), e); // NOI18N
                            } finally {
                                if (lock != null) {
                                    lock.releaseLock();
                                }
                            }
                        }
                    }
                    return null;
                }
            }, files);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(null, ex, false, false);
        }
    }
    
}
