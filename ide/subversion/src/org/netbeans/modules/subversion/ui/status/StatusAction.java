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

package org.netbeans.modules.subversion.ui.status;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.subversion.util.*;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.ISVNDirEntryWithLock;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLock;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 * Context sensitive status action. It opens the Subversion
 * view and sets its context.
 *
 * @author Petr Kuzel
 */
public class StatusAction  extends ContextAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/subversion/resources/icons/show_changes.png"; //NOI18N

    public StatusAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    private static final int enabledForStatus = FileInformation.STATUS_MANAGED;
    
    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_ShowChanges"; // NOI18N
    }

    @Override
    protected int getFileEnabledStatus() {
        return enabledForStatus;
    }

    @Override
    public void performContextAction(Node[] nodes) {
        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        
        Context ctx = SvnUtils.getCurrentContext(nodes);
        final SvnVersioningTopComponent stc = SvnVersioningTopComponent.getInstance();
        stc.setContentTitle(getContextDisplayName(nodes));
        stc.setContext(ctx);
        stc.open(); 
        stc.requestActive();
        stc.performRefreshAction();
    }

    /**
     * Connects to repository and gets recent status.
     */
    public static void executeStatus(final Context context, SvnProgressSupport support, boolean contactServer) {

        if (context == null || context.getRoots().isEmpty()) {
            return;
        }
            
        try {
            SvnClient client;            
            try {
                client = Subversion.getInstance().getClient(context, support);
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, true, true);
                return;
            }
            Subversion.getInstance().getStatusCache().refreshCached(context);
            File[] roots = context.getRootFiles();
            for (int i = 0; i < roots.length; i++) {
                executeStatus(roots[i], client, support, contactServer);
                if (support.isCanceled()) {
                    return;
                }
            }
        } catch (SVNClientException ex) {
            if(!support.isCanceled()) {
                support.annotate(ex);
            } else {
                Subversion.LOG.log(Level.INFO, "Action canceled", ex);
            }
        }
    }

    public static void executeStatus(File root, SvnClient client, SvnProgressSupport support, boolean contactServer) throws SVNClientException {
        if (support != null && support.isCanceled()) {
            return;
        }
        ISVNStatus[] statuses;
        Map<File, ISVNLock> locks = new HashMap<File, ISVNLock>();
        try {
            statuses = client.getStatus(root, true, false, contactServer); // cache refires events
            if (contactServer && SvnModuleConfig.getDefault().isGetRemoteLocks()) {
                try {
                    ISVNInfo info = client.getInfoFromWorkingCopy(root);
                    if (info != null && info.getUrl() != null && !info.isCopied()) {
                        ISVNDirEntryWithLock[] entries = client.getListWithLocks(info.getUrl(), info.getRevision(), info.getRevision(), true);
                        if (entries != null) {
                            for (ISVNDirEntryWithLock entry : entries) {
                                if (entry.getLock() != null) {
                                    locks.put(root.isFile() ? root : new File(root, entry.getDirEntry().getPath().replace("/", File.separator)), entry.getLock()); //NOI18N
                                }
                            }
                        }
                    }
                } catch (SVNClientException ex) {
                    Subversion.LOG.log(SvnClientExceptionHandler.isNotUnderVersionControl(ex.getMessage())
                            || SvnClientExceptionHandler.isWrongUrl(ex.getMessage()) ? Level.FINE : Level.INFO, null, ex);
                }
            }
        } catch (SVNClientException ex) {
            if (contactServer && SvnClientExceptionHandler.isNotUnderVersionControl(ex.getMessage())) {
                Subversion.LOG.log(Level.INFO, "StatusAction.executeStatus: file under {0} not under version control, trying offline", root.getAbsolutePath()); //NOI8N
                statuses = client.getStatus(root, true, false, false); // cache refires events
            } else {
                throw ex;
            }
        }
        if (support != null && support.isCanceled()) {
            return;
        }
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        for (int s = 0; s < statuses.length; s++) {
            if (support != null && support.isCanceled()) {
                return;
            }
            ISVNStatus status = statuses[s];
            File file = status.getFile();
            if (file.isDirectory() && status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
                // could have been created externally and the cache ignores by designe
                // a newly created folders children.
                // As this is the place were such files should be recognized,
                // we will force the refresh recursivelly.
                cache.refreshRecursively(file);
            } else {
                cache.refresh(file, new FileStatusCache.RepositoryStatus(status, locks.get(file)));
            }
        }
    }
}
