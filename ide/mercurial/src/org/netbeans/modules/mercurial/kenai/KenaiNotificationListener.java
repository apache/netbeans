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

package org.netbeans.modules.mercurial.kenai;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor.VCSKenaiModification;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor.VCSKenaiNotification;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class KenaiNotificationListener extends VCSKenaiAccessor.KenaiNotificationListener {

    protected void handleVCSNotification(final VCSKenaiNotification notification) {
        if(notification.getService() != VCSKenaiAccessor.Service.VCS_HG) {
            Mercurial.LOG.fine("rejecting VCS notification " + notification + " because not from hg"); // NOI18N
            return;
        }
        File projectDir = notification.getProjectDirectory();
        if(!Mercurial.getInstance().isManaged(projectDir)) {
            assert false : " project " + projectDir + " not managed";
            Mercurial.LOG.fine("rejecting VCS notification " + notification + " for " + projectDir + " because not versioned by hg"); // NOI18N
            return;
        }
        Mercurial.LOG.fine("accepting VCS notification " + notification + " for " + projectDir); // NOI18N

        File[] files = Mercurial.getInstance().getFileStatusCache().listFiles(new File[] { projectDir }, FileInformation.STATUS_LOCAL_CHANGE);
        List<VCSKenaiModification> modifications = notification.getModifications();

        List<File> notifyFiles = new LinkedList<File>();
        String revision = null;
        for (File file : files) {
            String path = HgUtils.getRelativePath(file);
            if(path == null) {
                assert false : file.getAbsolutePath() + " - no relative path"; // NOI18N
                continue;
            }
            path = trim(path);
            for (VCSKenaiModification modification : modifications) {
                String resource = modification.getResource();
                resource = trim(resource);
                LOG.finer(" changed file " + path + ", " + resource); // NOI18N
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
        }
    }

    @Override
    protected void setupPane(JTextPane pane, final File[] files, final File projectDir, final String url, final String revision) {
        String text = NbBundle.getMessage(
                KenaiNotificationListener.class,
                "MSG_NotificationBubble_Description", 
                getFileNames(files),
                HgKenaiAccessor.getInstance().getRevisionUrl(url, revision)); //NOI18N
        pane.setText(text);

        pane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    URL url = e.getURL();
                    assert url != null;
                    HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
                    assert displayer != null : "HtmlBrowser.URLDisplayer found.";   //NOI18N
                    if (displayer != null) {
                        displayer.showURL (url);
                    } else {
                        Mercurial.LOG.info("No URLDisplayer found.");               //NOI18N
                    }
                }
            }
        });
    }

}
