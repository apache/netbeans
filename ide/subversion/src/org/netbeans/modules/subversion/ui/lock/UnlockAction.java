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
package org.netbeans.modules.subversion.ui.lock;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author ondra
 */
public class UnlockAction extends ContextAction {
    
    private static final Logger LOG = Logger.getLogger(UnlockAction.class.getName());
    
    @Override
    protected int getDirectoryEnabledStatus () {
        return 0;
    }

    @Override
    protected int getFileEnabledStatus () {
        return FileInformation.STATUS_LOCKED | FileInformation.STATUS_LOCKED_REMOTELY;
    }

    @Override
    protected String getBaseName (Node[] activatedNodes) {
        return "CTL_Unlock_Title"; //NOI18N
    }

    @Override
    protected void performContextAction (Node[] nodes) {
        if(!Subversion.getInstance().checkClientAvailable()) {
            return;
        }
        Context ctx = getContext(nodes);
        final File[] files = ctx.getFiles();
        if (files.length == 0) {
            return;
        }
        final SVNUrl url;
        try {
            url = SvnUtils.getRepositoryRootUrl(files[0]);
        } catch (SVNClientException ex) {
            LOG.log(Level.INFO, "No url for {0}", files[0]); //NOI18N
            return;
        }
        new SvnProgressSupport() {
            @Override
            protected void perform () {
                try {
                    SvnClient client = Subversion.getInstance().getClient(url, this);
                    Map<File, String> paths = new HashMap<File, String>();
                    for (File f : files) {
                        paths.put(f, f.getAbsolutePath());
                    }
                    boolean cont;
                    boolean force = false;
                    do {
                        cont = false;
                        boolean resumeAuth = true;
                            LockedFilesListener list;
                            do {
                                list = new LockedFilesListener(paths);
                                client.addNotifyListener(list);
                                client.unlock(files, force);
                                if (list.isAuthError() && (resumeAuth = SvnClientExceptionHandler.handleAuth(url))) {
                                    client.removeNotifyListener(list);
                                    client = Subversion.getInstance().getClient(url, this);
                                } else {
                                    break;
                                }
                            } while (resumeAuth);
                            if (!resumeAuth) {
                                break;
                            }
                        client.removeNotifyListener(list);
                        if (!force && !list.lockedFiles.isEmpty() && !lockedByOther(client, list.lockedFiles).isEmpty()) {
                            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(UnlockAction.class, "MSG_UnlockAction.lockedFiles.description"), //NOI18N
                                    NbBundle.getMessage(UnlockAction.class, "MSG_UnlockAction.lockedFiles.title"), //NOI18N
                                    NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE);
                            if (NotifyDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify(nd)) {
                                cont = force = true;
                            }
                        }
                    } while (cont);
                    Subversion.getInstance().getStatusCache().refreshAsync(files);
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, true, false);
                }
            }

            private Collection<File> lockedByOther (SvnClient client, Set<File> lockedFiles) throws SVNClientException {
                List<File> lockedByOtherFiles = new LinkedList<File>();
                ISVNStatus[] statuses = client.getStatus(lockedFiles.toArray(new File[0]));
                for (ISVNStatus status : statuses) {
                    if (status.getLockOwner() == null) {
                        // not locked in this WC
                        lockedByOtherFiles.add(status.getFile());
                    }
                }
                return lockedByOtherFiles;
            }
        }.start(Subversion.getInstance().getRequestProcessor(url), url, NbBundle.getMessage(UnlockAction.class, "LBL_Unlock_Progress")); //NOI18N
    }
    
    private static class LockedFilesListener implements ISVNNotifyListener {
        private final Map<File, String> paths;
        private final Set<File> lockedFiles = new HashSet<File>();
        private boolean authError;

        public LockedFilesListener (Map<File, String> paths) {
            this.paths = paths;
        }
        
        @Override
        public void setCommand (int i) {
        }

        @Override
        public void logCommandLine (String string) {
        }

        @Override
        public void logMessage (String string) {
        }

        @Override
        public void logError (String error) {
            if (error == null) {
                // not interested
            } else if (error.contains("is not locked in this working copy")) { //NOI18N
                for (Iterator<Map.Entry<File, String>> it = paths.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<File, String> e = it.next();
                    String path = e.getValue();
                    File file = e.getKey();
                    if (error.contains(MessageFormat.format("''{0}'' is not locked in this working copy", path))) { //NOI18N
                        // svnkit
                        lockedFiles.add(new File(path));
                        it.remove();
                        break;
                    } else if (error.contains(MessageFormat.format("''{0}'' is not locked in this working copy", file.getName()))) { //NOI18N
                        // javahl
                        lockedFiles.add(file);
                        it.remove();
                        break;
                    }
                }
            } else if (SvnClientExceptionHandler.isAuthentication(error)) {
                authError = true;
            }
        }

        @Override
        public void logRevision (long l, String string) {
        }

        @Override
        public void logCompleted (String string) {
        }

        @Override
        public void onNotify (File file, SVNNodeKind svnnk) {
        }

        private boolean isAuthError () {
            return authError;
        }
    }
}
