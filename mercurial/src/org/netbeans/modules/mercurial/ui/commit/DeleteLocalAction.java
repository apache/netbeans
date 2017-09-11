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
                    }, files.toArray(new File[files.size()]));
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }
            }
        };
        support.start(Mercurial.getInstance().getRequestProcessor(), NbBundle.getMessage(DeleteLocalAction.class, "LBL_DeleteLocalAction.progress")); //NOI18N
    }
    
}
