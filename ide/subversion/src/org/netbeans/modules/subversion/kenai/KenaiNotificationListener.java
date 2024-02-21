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

package org.netbeans.modules.subversion.kenai;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JTextPane;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.notifications.NotificationsManager;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor.VCSKenaiModification;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor.VCSKenaiNotification;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 * @author Tomas Stupka
 */
public class KenaiNotificationListener extends VCSKenaiAccessor.KenaiNotificationListener {
    
    @Override
    protected void handleVCSNotification(final VCSKenaiNotification notification) {
        if(notification.getService() != VCSKenaiAccessor.Service.VCS_SVN) {
            LOG.fine("rejecting VCS notification " + notification + " because not from svn"); // NOI18N
            return;
        }
        File projectDir = notification.getProjectDirectory();
        if(!SvnUtils.isManaged(projectDir)) {
            assert false : " project " + projectDir + " not managed"; // NOI18N
            LOG.fine("rejecting VCS notification " + notification + " for " + projectDir + " because not versioned by svn"); // NOI18N
            return;
        }
        LOG.fine("accepting VCS notification " + notification + " for " + projectDir); // NOI18N
        
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File[] files = cache.listFiles(new File[] {projectDir}, FileInformation.STATUS_LOCAL_CHANGE);
        List<VCSKenaiModification> modifications = notification.getModifications();

        List<File> notifyFiles = new LinkedList<File>();
        String revision = null;
        for (File file : files) {
            String path;
            try {
                path = SvnUtils.getRepositoryPath(file);
            } catch (SVNClientException ex) {
                LOG.log(Level.WARNING, file.getAbsolutePath(), ex); 
                continue;
            }
            path = trim(path);
            for (VCSKenaiModification modification : modifications) {
                String resource = modification.getResource();
                LOG.finer(" changed file " + path + ", " + resource); // NOI18N

                resource = trim(resource);
                if(path.equals(resource)) {
                    LOG.fine("  will notify " + file + ", " + notification); // NOI18N
                    notifyFiles.add(file);
                    if(revision == null) {
                        revision = modification.getId();
                    }
                    break;
                }
            }
        }
        if(notifyFiles.size() > 0) {
            notifyFileChange(notifyFiles.toArray(new File[0]), projectDir, notification.getUri().toString(), revision);
            try {
                NotificationsManager.getInstance().notfied(files, Long.parseLong(revision));
            } catch (NumberFormatException e) {
                LOG.log(Level.WARNING, revision, e);
            }
        }
    }

    @Override
    protected void setupPane(JTextPane pane, final File[] files, final File projectDir, final String url, final String revision) {        
        NotificationsManager.getInstance().setupPane(pane, files, getFileNames(files), projectDir, url, revision);
    }

}
