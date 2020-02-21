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
package org.netbeans.modules.subversion.remote.client;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.api.SVNNodeKind;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
public class SvnClientRefreshHandler implements ISVNNotifyListener {

    private final Set<VCSFileProxy>  filesToRefresh = new HashSet<>();

    @Override
    public void setCommand(ISVNNotifyListener.Command arg0)                { /* boring */ }
    @Override
    public void logCommandLine(String arg0)         { /* boring */ }
    @Override
    public void logMessage(String arg0)             { /* boring */ }
    @Override
    public void logError(String arg0)               { /* boring */ }
    @Override
    public void logRevision(long arg0, String arg1) { /* boring */ }
    @Override
    public void logCompleted(String arg0)           { /* boring */ }

    /**
     * Notifies that a file was handled by the svn client adpater.
     * @param file
     * @param kind
     * @see {@link #refresh()}
     */
    @Override
    public void onNotify(VCSFileProxy file, SVNNodeKind kind) {
        if(file == null) {
            return;
        }
        file = file.normalizeFile(); // I saw "./"

        synchronized(filesToRefresh) {
            if(Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.fine("scheduling for refresh: [" + file + "]"); // NOI18N
            }
            filesToRefresh.add(file);
        }
    }

//    /**
//     * Refresh the nb filesystem and the cache for all given files
//     * @param files files to be refreshed
//     */
//    public void refreshImediately(File... files) {
//        for (int i = 0; i < files.length; i++) {
//            files[i] = FileUtil.normalizeFile(files[i]); // I saw "./"
//        }
//        refresh(files);
//    }

    /**
     * Refreshes all yet notified files
     * @see {@link #onNotify(java.io.File, org.tigris.subversion.svnclientadapter.SVNNodeKind)}
     */
    public void refresh() {
        VCSFileProxy[] fileArray;
        synchronized(filesToRefresh) {
            fileArray = filesToRefresh.toArray(new VCSFileProxy[filesToRefresh.size()]);
            filesToRefresh.clear();
        }
        refresh(fileArray);
    }

    /**
     * Refresh the nb filesystem and the cache for all given files
     * @param files files to be refreshed
     */
    private void refresh(VCSFileProxy... files) {
        if(Subversion.LOG.isLoggable(Level.FINE)) {
            for (VCSFileProxy file : files) {
                Subversion.LOG.fine("refreshing: [" + file + "]"); // NOI18N
            }
        }
        // refresh the filesystems first as the following cache refesh might fire events
        // which are intercepted by the nb filesystem - it has to be aware about possible changes made
//        refreshFS(files); // XXX hm. this hapens asynchronoulsy now

        // async cache refesh - notifications from the svnclientadapter may be caused
        // by a synchronously handled FS event. If we want to (have to) prevent
        // reentrant calls on the FS api
        Subversion.getInstance().getStatusCache().refreshAsync(files);
    }

//    /**
//     * All parents from the given files and their children will be refreshed
//     * @param files files to refresh
//     */
//    private void refreshFS(File... files) {
//        final Set<File> parents = new HashSet<File>();
//        for (File f : files) {
//            File parent = f.getParentFile();
//            if (parent != null) {
//                parents.add(parent);
//                Subversion.LOG.fine("scheduling for fs refresh: [" + parent + "]"); // NOI18N
//            }
//        }
//        if (parents.size() > 0) {
//            // let's give the filesystem some time to wake up and to realize that the file has really changed
//            RequestProcessor.getDefault().post(new Runnable() {
//                public void run() {
//                    FileUtil.refreshFor(parents.toArray(new File[parents.size()]));
//                }
//            }, getDelay());
//        }
//    }
//
//    private int getDelay() {
//        if (delayBeforeRefresh == -1) {
//            String delayProp = System.getProperty("subversion.SvnClientRefreshHandler.delay", Integer.toString(DEFAULT_DELAY)); //NOI18N
//            int delay = DEFAULT_DELAY;
//            try {
//                delay = Integer.parseInt(delayProp);
//            } catch (NumberFormatException e) {
//                Subversion.LOG.log(Level.FINE, null, e);
//            }
//            delayBeforeRefresh = delay;
//        }
//        return delayBeforeRefresh;
//    }
}
