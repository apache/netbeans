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
