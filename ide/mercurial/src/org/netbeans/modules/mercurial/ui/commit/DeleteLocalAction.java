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

package org.netbeans.modules.mercurial.ui.commit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Delete action enabled for new local files and added but not yet committed files.
 *
 * @author Petr Kuzel
 */
public final class DeleteLocalAction extends ContextAction {

    public static final int LOCALLY_DELETABLE_MASK = FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Delete"; //NOI18N
    }

    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        return Mercurial.getInstance().getFileStatusCache().listFiles(context, LOCALLY_DELETABLE_MASK).length > 0;
    }
    
    @Override
    protected void performContextAction (final Node[] nodes) {
        final VCSContext context = HgUtils.getCurrentContext(nodes);
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        final Set<File> files = context.getRootFiles();
        for (File f : files) {
            if ((cache.getCachedStatus(f).getStatus() & LOCALLY_DELETABLE_MASK) == 0) {
                return;
            }
        }

        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(DeleteLocalAction.class, "CTL_DeleteLocal_Prompt")); // NOI18N
        descriptor.setTitle(NbBundle.getMessage(DeleteLocalAction.class, "CTL_DeleteLocal_Title")); // NOI18N
        descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
        descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

        Object res = DialogDisplayer.getDefault().notify(descriptor);
        if (res != NotifyDescriptor.YES_OPTION) {
            return;
        }
        
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                final Map<File, Set<File>> sortedFiles = HgUtils.sortUnderRepository(files);
                try {
                    HgUtils.runWithoutIndexing(new Callable<Void>() {
                        @Override
                        public Void call () throws Exception {
                            for (Map.Entry<File, Set<File>> e : sortedFiles.entrySet()) {
                                try {
                                    HgCommand.doRevert(e.getKey(), new ArrayList<File>(e.getValue()), null, false, OutputLogger.getLogger(null));
                                } catch (HgException ex) {
                                    Mercurial.LOG.log(Level.INFO, null, ex);
                                }
                                for (File file : e.getValue()) {
                                    if(isCanceled()) {
                                        return null;
                                    }
                                    FileObject fo = FileUtil.toFileObject(file);
                                    if (fo != null) {
                                        FileLock lock = null;
                                        try {
                                            lock = fo.lock();                    
                                            fo.delete(lock);       
                                        } catch (IOException ex) {
                                            Mercurial.LOG.log(Level.SEVERE, NbBundle.getMessage(DeleteLocalAction.class, "MSG_Cannot_lock", file.getAbsolutePath()), e); //NOI18N
                                        } finally {
                                            if (lock != null) {
                                                lock.releaseLock();
                                            }
                                        }
                                    }
                                }
                            }
                            return null;
                        }
                    }, files.toArray(new File[0]));
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }
            }
        };
        support.start(Mercurial.getInstance().getRequestProcessor(), NbBundle.getMessage(DeleteLocalAction.class, "LBL_DeleteLocalAction.progress")); //NOI18N
    }
    
}
