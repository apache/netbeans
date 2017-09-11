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

package org.netbeans.modules.subversion.ui.commit;

import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.ui.actions.*;
import org.netbeans.modules.subversion.util.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Delete action enabled for new local files (not yet in repository).
 * It eliminates <tt>.svn/entries</tt> scheduling if exists too.
 *
 * @author Petr Kuzel
 */
public final class DeleteLocalAction extends ContextAction {

    public static final int LOCALLY_DELETABLE_MASK = FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    protected String getBaseName(Node [] activatedNodes) {
        return "Delete";  // NOI18N
    }

    protected int getFileEnabledStatus() {
        return LOCALLY_DELETABLE_MASK;
    }
    
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
            public void perform() {
                performDelete(ctx, this);
            }
        };
        support.start(createRequestProcessor(ctx));
    }
    
    public static void performDelete(Context ctx, final SvnProgressSupport support) {

        final SvnClient client;
        try {
            client = Subversion.getInstance().getClient(ctx, support);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }        

        if(support.isCanceled()) {
            return;
        }
        final File[] files = ctx.getFiles();
        try {
            SvnUtils.runWithoutIndexing(new Callable<Void>() {
                @Override
                public Void call () throws Exception {
                    for (int i = 0; i < files.length; i++) {
                        if(support.isCanceled()) {
                            return null;
                        }

                        File file = files[i];
                        FileObject fo = FileUtil.toFileObject(file);
                        if (fo != null) {
                            FileLock lock = null;
                            try {
                                try {
                                    client.revert(file, false);
                                } catch (SVNClientException ex) {
                                    SvnClientExceptionHandler.notifyException(ex, true, true);
                                }
                                lock = fo.lock();                    
                                fo.delete(lock);       
                            } catch (IOException e) {
                                Subversion.LOG.log(Level.SEVERE, NbBundle.getMessage(DeleteLocalAction.class, "MSG_Cannot_lock", file.getAbsolutePath()), e); // NOI18N
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
            SvnClientExceptionHandler.notifyException(ex, false, false);
        }
    }
    
}
