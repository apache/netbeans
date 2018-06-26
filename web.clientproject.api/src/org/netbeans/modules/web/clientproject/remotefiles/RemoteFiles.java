/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject.remotefiles;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.html.editor.api.index.HtmlIndex;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.RemoteFileCache;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 */
public class RemoteFiles {

    private static final RequestProcessor RP = new RequestProcessor(RemoteFiles.class);
    
    private final Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private List<URL> urls;
    private HtmlIndex index;
    private ChangeListener listener;
    
    public RemoteFiles(Project project) {
        this.project = project;
    }
    
    private synchronized HtmlIndex getHtmlIndex() {
        if (index == null) {
            try {
                index = HtmlIndex.get(project);
                listener = new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        update();
                    }
                };
                index.addChangeListener(WeakListeners.change(listener, index));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return index;
    }
    
    private void update() {
        try {
            ParserManager.parseWhenScanFinished("text/html", new UserTask() { //NOI18N
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    updateRemoteFiles();
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void updateRemoteFiles() {
        final List<URL> deps;
        try {
            deps = getHtmlIndex().getAllRemoteDependencies();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        //http://netbeans.org/bugzilla/show_bug.cgi?id=217384#c5
        //do not set the children keys directly from the parsing task
        RP.post(new Runnable() {
            @Override
            public void run() {
                setUrls(filter(deps));
                fireChange();
            }
        });
    }
    
    public synchronized List<URL> getRemoteFiles() {
        if (urls == null) {
            urls = new ArrayList<URL>();
            // first time asked for remote files: return empty array and
            // initialize index etc and fire even when real data are available:
            update();
         }
        return urls;
    }

    private synchronized void setUrls(List<URL> urls) {
        this.urls = urls;
        prefetchRemoteFiles(urls);
    }
    
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    private void fireChange() {
        changeSupport.fireChange();
    }
    
    // TODO:
    
    // content of remote URL is downloaded and cached - cache is shared by all projects
    
    // ability to force cache refresh

    private void prefetchRemoteFiles(final List<URL> urls) {
        for (final URL u : urls) {
            try {
                RemoteFileCache.getRemoteFile(u);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    // #217664 - for now filter out all remote files which are not JavaScript or CSS files:
    private List<URL> filter(List<URL> allRemoteDependencies) {
        List<URL> res = new ArrayList<URL>();
        for (URL u : allRemoteDependencies) {
            String uu = u.toExternalForm().toLowerCase();
            // XXX mime type should be used
            if (uu.endsWith(".js") // NOI18N
                    || uu.endsWith(".css")) { //NOI18N
                res.add(u);
            }
        }
        return res;
    }
    
}
