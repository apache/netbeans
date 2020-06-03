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

import java.util.List;
import java.util.ArrayList;
import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.*;

/**
 *
 * 
 */
public final class ExcludeFromCommitAction extends ContextAction {

    public static final int UNDEFINED = -1;
    public static final int EXCLUDING = 1;
    public static final int INCLUDING = 2;

    @Override
    protected boolean enable(Node[] nodes) {
        Context cachedContext = getCachedContext(nodes);
        final FileSystem fileSystem = cachedContext.getFileSystem();
        if (fileSystem == null || !VCSFileProxySupport.isConnectedFileSystem(fileSystem)) {
            return false;
        }
        return isCacheReady() && getActionStatus(nodes) != UNDEFINED;
    }

    @Override
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_LOCAL_CHANGE;
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_LOCAL_CHANGE;
    }

    @Override
    protected String getBaseName(Node [] activatedNodes) {
        int actionStatus = getActionStatus(activatedNodes);
        switch (actionStatus) {
        case UNDEFINED:
        case EXCLUDING:
            return "popup_commit_exclude"; // NOI18N
        case INCLUDING:
            return "popup_commit_include"; // NOI18N
        default:
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }
    }
    
    public int getActionStatus(Node[] nodes) {
        VCSFileProxy [] files = getCachedContext(nodes).getFiles();
        int status = UNDEFINED;
        for (int i = 0; i < files.length; i++) {
            SvnModuleConfig config = SvnModuleConfig.getDefault(VCSFileProxySupport.getFileSystem(files[i]));
            if (config.isExcludedFromCommit(files[i].getPath())) {
                if (status == EXCLUDING) {
                    return UNDEFINED;
                }
                status = INCLUDING;
            } else {
                if (status == INCLUDING) {
                    return UNDEFINED;
                }
                status = EXCLUDING;
            }
        }
        return status;
    }

    @Override
    public void performContextAction(final Node[] nodes) {
        ProgressSupport support = new ContextAction.ProgressSupport(this, nodes, getCachedContext(nodes)) {
            @Override
            public void perform() {
                int status = getActionStatus(nodes);
                List<VCSFileProxy> files = new ArrayList<>();
                for (Node node : nodes) {
                    VCSFileProxy aFile = node.getLookup().lookup(VCSFileProxy.class);
                    FileObject fo = node.getLookup().lookup(FileObject.class);
                    if (aFile != null) {
                        files.add(aFile);
                    } else if (fo != null) {
                        VCSFileProxy f = VCSFileProxy.createFileProxy(fo);
                        if (f != null) {
                            files.add(f);
                        }
                    }
                }
                if (files.size() == 0) {
                    return;
                }
                SvnModuleConfig config = SvnModuleConfig.getDefault(VCSFileProxySupport.getFileSystem(files.get(0)));
                List<String> paths = new ArrayList<>(files.size());
                for (VCSFileProxy file : files) {
                    paths.add(file.getPath());
                }
                if (isCanceled()) {
                    return;
                }
                if (status == EXCLUDING) {
                    config.addExclusionPaths(paths);
                } else if (status == INCLUDING) {
                    config.removeExclusionPaths(paths);
                }
            }
        };
        support.start(createRequestProcessor(nodes));
    }

}
