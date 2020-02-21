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

package org.netbeans.modules.subversion.remote.ui.update;

import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.RepositoryFile;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.nodes.Node;

/**
 *
 * 
 */
public class UpdateToAction extends UpdateAction {

    public UpdateToAction () {
        super(null);
    }
    
    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_UpdateTo"; //NOI18N
    }

    @Override
    protected SVNRevision getRevision(Context ctx) {
        VCSFileProxy[] roots = ctx.getRootFiles();
        SVNRevision revision = null;
        if(roots == null || roots.length == 0) {
            return null;
        }

        VCSFileProxy interestingFile = roots[0];

        final SVNUrl rootUrl;
        final SVNUrl url;

        try {
            rootUrl = ContextAction.getSvnUrl(ctx);
            if (rootUrl == null) {
                SvnClientExceptionHandler.notifyNullUrl(ctx);
                return null; // otherwise NPE, see #267975
            }
            url = SvnUtils.getRepositoryUrl(interestingFile);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ctx, ex, true, true);
            return null;
        }
        
        final RepositoryFile repositoryFile = new RepositoryFile(ctx.getFileSystem(), rootUrl, url, SVNRevision.HEAD);

        final UpdateTo updateTo = new UpdateTo(repositoryFile, Subversion.getInstance().getStatusCache().containsFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE, true));
        if(updateTo.showDialog()) {
            revision = updateTo.getSelectedRevision();
        }
        return revision;
    }

    @Override
    protected String iconResource () {
        return null;
    }
}
