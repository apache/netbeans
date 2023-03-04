/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
