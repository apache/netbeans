/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote.ui.status;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.FileStatusCache;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.api.ISVNDirEntryWithLock;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNLock;
import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.nodes.Node;

/**
 * Context sensitive status action. It opens the Subversion
 * view and sets its context.
 *
 * 
 */
public class StatusAction extends ContextAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/subversion/remote/resources/icons/show_changes.png"; //NOI18N

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
        Context ctx = SvnUtils.getCurrentContext(nodes);
        if(!Subversion.getInstance().checkClientAvailable(ctx)) {            
            return;
        }
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
    public static void executeStatus(final Context context, SVNUrl repository, SvnProgressSupport support, boolean contactServer) {

        if (context == null || context.getRoots().isEmpty()) {
            return;
        }
        try {
            SvnClient client;            
            try {
                if (repository == null) {
                    client = Subversion.getInstance().getClient(context, support);
                } else {
                    client = Subversion.getInstance().getClient(context, repository, support);
                }
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(context, ex, true, true);
                return;
            }
            Subversion.getInstance().getStatusCache().refreshCached(context);
            VCSFileProxy[] roots = context.getRootFiles();
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

    public static void executeStatus(VCSFileProxy root, SvnClient client, SvnProgressSupport support, boolean contactServer) throws SVNClientException {
        if (support != null && support.isCanceled()) {
            return;
        }
        ISVNStatus[] statuses;
        Map<VCSFileProxy, ISVNLock> locks = new HashMap<>();
        try {
            statuses = client.getStatus(root, true, false, contactServer); // cache refires events
            if (contactServer && SvnModuleConfig.getDefault(VCSFileProxySupport.getFileSystem(root)).isGetRemoteLocks()) {
                try {
                    ISVNInfo info = client.getInfoFromWorkingCopy(root);
                    if (info != null && info.getUrl() != null && !info.isCopied()) {
                        ISVNDirEntryWithLock[] entries = client.getListWithLocks(info.getUrl(), info.getRevision(), info.getRevision(), true);
                        if (entries != null) {
                            for (ISVNDirEntryWithLock entry : entries) {
                                if (entry.getLock() != null) {
                                    locks.put(root.isFile() ? root : VCSFileProxy.createFileProxy(root,  entry.getDirEntry().getPath()), entry.getLock()); //NOI18N
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
                Subversion.LOG.log(Level.INFO, "StatusAction.executeStatus: file under {0} not under version control, trying offline", root.getPath()); //NOI8N
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
            VCSFileProxy file = status.getFile();
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
