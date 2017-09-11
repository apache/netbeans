/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
            notifyFileChange(notifyFiles.toArray(new File[notifyFiles.size()]), projectDir, notification.getUri().toString(), revision);
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
