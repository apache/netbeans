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
            notifyFileChange(notifyFiles.toArray(new File[notifyFiles.size()]), projectDir, notification.getUri().toString(), revision);
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
