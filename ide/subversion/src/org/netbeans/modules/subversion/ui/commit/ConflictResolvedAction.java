/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.subversion.ui.commit;

import java.io.*;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.*;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Represnts <tt>svn resolved</tt> command.
 *
 * @author Petr Kuzel
 */
public class ConflictResolvedAction extends ContextAction {
    
    @Override
    protected String getBaseName(Node[] activatedNodes) {
        return "resolve";  // NOI18N
    }

    @Override
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_VERSIONED_CONFLICT;
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return 0;
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        final Context ctx = getContext(nodes);
        final File[] files = ctx.getFiles();

        ProgressSupport support = new ContextAction.ProgressSupport(this, nodes, ctx) {
            @Override
            public void perform() {

                SvnClient client = null;
                try {
                    client = Subversion.getInstance().getClient(ctx, this);
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, false, false);
                }

                if (client == null) {
                    return;
                }
                
                for (int i = 0; i<files.length; i++) {
                    if(isCanceled()) {
                        return;
                    }
                    File file = files[i];
                    try {
                        ConflictResolvedAction.perform(file, client);
                    } catch (SVNClientException ex) {
                        annotate(ex);                        
                    }
                }
            }
        };
        support.start(createRequestProcessor(ctx));
    }


    /** Marks as resolved or shows error dialog. */
    public static void perform(final File file) throws SVNClientException {
        SvnProgressSupport support = new SvnProgressSupport() {
            @Override
            protected void perform() {
                try {
                    SvnClient client = Subversion.getInstance().getClient(file);
                    if (client != null) {
                        ConflictResolvedAction.perform(file, client);
                    }
                } catch (SVNClientException ex){
                    annotate(ex);
                }
            }
        };
        SVNUrl url = SvnUtils.getRepositoryRootUrl(file);
        support.start(Subversion.getInstance().getRequestProcessor(url), url, NbBundle.getMessage(ConflictResolvedAction.class, "LBL_ResolvingConflicts")); //NOI18N
    }

    private static void perform(File file, SvnClient client) throws SVNClientException {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();

        client.resolved(file);
        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);

        // auxiliary files disappear, synch with FS
        File parent = file.getParentFile();
        if (parent != null) {
            FileObject folder = FileUtil.toFileObject(parent);
            if (folder != null) {
                folder.refresh();
            }
        }        
    }

}
