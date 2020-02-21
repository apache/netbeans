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

package org.netbeans.modules.subversion.remote.ui.ignore;

import java.util.*;
import org.openide.nodes.Node;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.FileStatusCache;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNProperty;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 * Adds/removes files to svn:ignore property.
 * It does not support patterns.
 *
 * 
 */
public class IgnoreAction extends ContextAction {
    
    public static final int UNDEFINED  = 0;
    public static final int IGNORING   = 1;
    public static final int UNIGNORING = 2;
    
    @Override
    protected String getBaseName(Node [] activatedNodes) {
        int actionStatus = getActionStatus(activatedNodes);
        switch (actionStatus) {
        case UNDEFINED:
        case IGNORING:
            return "CTL_MenuItem_Ignore";                                           // NOI18N
        case UNIGNORING:
            return "CTL_MenuItem_Unignore";                                         // NOI18N
        default:
            throw new RuntimeException("Invalid action status: " + actionStatus);   // NOI18N
        }
    }

    @Override
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }
    
    public int getActionStatus(Node [] nodes) {
        return getActionStatus(getCachedContext(nodes).getFiles());
    }

    public int getActionStatus(VCSFileProxy [] files) {
        int actionStatus = -1;
        if (files.length == 0) {
            return UNDEFINED;
        } 
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(SvnUtils.SVN_ADMIN_DIR)) { // NOI18N
                actionStatus = UNDEFINED;
                break;
            }
            FileInformation info = cache.getStatus(files[i]);
            if ((info.getStatus()
                    & (FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)
                    ) != 0) {
                if (actionStatus == UNIGNORING) {
                    actionStatus = UNDEFINED;
                    break;
                }
                actionStatus = IGNORING;
            } else if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                if (actionStatus == IGNORING) {
                    actionStatus = UNDEFINED;
                    break;
                }
                actionStatus = UNIGNORING;
            } else {
                actionStatus = UNDEFINED;
                break;
            }
        }
        return actionStatus == -1 ? UNDEFINED : actionStatus;
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        return isCacheReady() && getActionStatus(nodes) != UNDEFINED;
    }

    @Override
    public void performContextAction(final Node[] nodes) {
        final int actionStatus = getActionStatus(nodes);
        if (actionStatus != IGNORING && actionStatus != UNIGNORING) {
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }

        final Context ctx = SvnUtils.getCurrentContext(nodes);
        if(!Subversion.getInstance().checkClientAvailable(ctx)) {            
            return;
        }
        final VCSFileProxy files[] = ctx.getRootFiles();

        ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes, getCachedContext(nodes)) {
            @Override
            public void perform() {
                Map<VCSFileProxy, Set<String>> names = splitByParent(files);
                // do not attach onNotify listeners because the ignore command forcefully fires change events on ALL files
                // in the parent directory and NONE of them interests us, see #89516
                SvnClient client;
                try {
                    client = Subversion.getInstance().getClient(false, ctx);               
                } catch (SVNClientException e) {
                    SvnClientExceptionHandler.notifyException(ctx, e, true, true);
                    return;
                }
                if (actionStatus == IGNORING) {
                    FileStatusCache cache = Subversion.getInstance().getStatusCache();
                    try {
                        for (VCSFileProxy file : files) {
                            // revert all locally added files (svn added but not comitted)
                            // #108369 - added files cannot be ignored
                            FileInformation s = cache.getStatus(file);
                            if (s.getStatus() == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) {
                                ISVNInfo info = client.getInfo(file);
                                if (info == null || !info.isCopied()) { // do not revert copied files
                                    client.revert(file, true); // revert the tree to NEWLOCALLY
                                }
                            }
                        }
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(ctx, ex, true, true);
                        return;
                    }
                }
                for (VCSFileProxy parent : names.keySet()) {
                    Set<String> patterns = names.get(parent);
                    if(isCanceled()) {
                        return;
                    }
                    try {
                        Collection<String> c = client.getIgnoredPatterns(parent);
                        if (c == null) {
                            Subversion.LOG.log(Level.WARNING, IgnoreAction.class.toString() + ": cannot acquire ignored patterns for " + parent.getPath()); // NOI18N
                            if (parent.exists()) {
                                Subversion.LOG.log(Level.WARNING, IgnoreAction.class.toString() + ": file does exist: " + parent.getPath()); // NOI18N
                            }
                        } else {
                            Set<String> currentPatterns = new HashSet<>(c);
                            if (actionStatus == IGNORING) {
                                ensureVersioned(parent);
                                currentPatterns.addAll(patterns);
                            } else if (actionStatus == UNIGNORING) {
                                currentPatterns.removeAll(patterns);
                            }
                            client.setIgnoredPatterns(parent, new ArrayList<>(currentPatterns));
                        }
                    } catch (SVNClientException e) {
                        SvnClientExceptionHandler.notifyException(ctx, e, true, true);
                    }
                }
                // refresh files manually, we do not suppport wildcards in ignore patterns so this is sufficient
                for (VCSFileProxy file : files) {
                    Subversion.getInstance().getStatusCache().refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                }
                // refresh also the parents
                for (VCSFileProxy parent : names.keySet()) {
                    Subversion.getInstance().getStatusCache().refresh(parent, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                }
            }
        };            
        support.start(createRequestProcessor(ctx));
    }

    private Map<VCSFileProxy, Set<String>> splitByParent(VCSFileProxy[] files) {
        Map<VCSFileProxy, Set<String>> map = new HashMap<>(2);
        for (VCSFileProxy file : files) {
            VCSFileProxy parent = file.getParentFile();
            if (parent == null) {
                continue;
            }
            Set<String> names = map.get(parent);
            if (names == null) {
                names = new HashSet<>(5);
                map.put(parent, names);
            }
            names.add(file.getName());
        }
        return map;
    }    
    
    /**
     * Adds this file and all its parent folders to repository if they are not yet added. 
     * 
     * @param file file to add
     * @throws SVNClientException if something goes wrong in subversion
     */ 
    private static void ensureVersioned(VCSFileProxy file) throws SVNClientException {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED) != 0) {
            return;
        }
        ensureVersioned(file.getParentFile());
        add(file);
        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
    }

    /**
     * Adds the file to repository with 'svn add', non-recursively.
     * 
     * @param file file to add
     */ 
    private static void add(VCSFileProxy file) throws SVNClientException {
        SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(file);
        SvnClient client = Subversion.getInstance().getClient(new Context(file), repositoryUrl);               
        if (file.isDirectory()) {
            client.addDirectory(file, false);
        } else {
            client.addFile(file);
        }
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    public static void ignore(VCSFileProxy file) throws SVNClientException {
        VCSFileProxy parent = file.getParentFile();
        ensureVersioned(parent);
        // technically, this block need not be synchronized but we want to have svn:ignore property set correctly at all times
        synchronized(IgnoreAction.class) {                        
            List<String> patterns = Subversion.getInstance().getClient(true, new Context(file)).getIgnoredPatterns(parent);
            if (patterns != null && patterns.contains(file.getName()) == false) {
                patterns.add(file.getName());
                // cannot use client.setIgnoredPatterns since there's a bug in the implementation in the svnClientAdapter
                // which doesn't respect a svn 1.6 contract about non-cr/cr-lf line-endings
                String value = getPatternsAsString(patterns);
                Subversion.getInstance().getClient(true, new Context(file)).propertySet(parent, ISVNProperty.IGNORE, value, false);
            }            
        }
    }

    private static String getPatternsAsString(List<String> patterns) {
        StringBuilder value = new StringBuilder();
        for (String pattern : patterns) {
            value.append(pattern).append('\n');
        }
        return value.toString();
    }
}
