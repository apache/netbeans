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
package org.netbeans.modules.web.inspect.webkit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.live.LiveUpdater;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.css.CSS;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetHeader;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Listens on parsing of CSS documents and propagates updates to associated
 * webkit. This class is singleton. Only one debugging session is allowed.
 *
 * @author Jan Becicka
 */
public class CSSUpdater {

    /**
     * Singleton instance.
     */
    private static CSSUpdater instance;
    
    /**
     * Current webkit session.
     */
    private WebKitDebugging webKit;
    
    /**
     * Owning project.
     */
    private Project project;
    
    /**
     * Address of the local-host.
     */
    private InetAddress localhost;

    /**
     * Listener for the addition/removal of style-sheets.
     */
    private CSS.Listener listener;
    
    /**
     * Mapping between url represented by string and StyleSheetHeader
     */
    private final Map<String, StyleSheetHeader> sheetsMap = new HashMap<String, StyleSheetHeader>();
    private final Map<FileObject, StyleSheetHeader> fobToSheetMap = new HashMap<FileObject,StyleSheetHeader>();

    private CSSUpdater() {
    }

    /**
     * Singleton instance.
     * @return 
     */
    static synchronized CSSUpdater getDefault() {
        if (instance == null) {
            instance = new CSSUpdater();
        }
        return instance;
    }

    /**
     * Start listening on CSS. Propagate changes to given webkit.
     * @param webKit 
     */
    synchronized void start(WebKitDebugging webKit, Project project) {
        assert webKit !=null : "webKit allready assigned"; // NOI18N
        this.webKit = webKit;
        this.project = project;
        localhost = null;
        try {
            localhost = WebUtils.getLocalhostInetAddress();
        } catch (IllegalStateException isex) {
            Logger.getLogger(CSSUpdater.class.getName()).log(Level.INFO, null, isex);
        }
        this.listener = new Listener();
        webKit.getCSS().addListener(listener);
        for (StyleSheetHeader header : webKit.getCSS().getAllStyleSheets()) {
            registerStyleSheet(header);
        }
    }

    /**
     * Registers the specified style-sheet (so that it is refreshed
     * in the browser when its source file is modified).
     * 
     * @param header information about the style-sheet.
     */
    synchronized void registerStyleSheet(StyleSheetHeader header) {
        try {
            //need to convert file:///
            URL url = new URL(header.getSourceURL());
            sheetsMap.put(url.toString(), header);

            if (project != null) {
                FileObject fob = ServerURLMapping.fromServer(project, url);
                if (fob != null) {
                    fobToSheetMap.put(fob, header);
                }
            }

            //TODO: hack to workaround #221791
            if (localhost != null && localhost.equals(InetAddress.getByName(url.getHost()))) {
                sheetsMap.put(new URL(url.toExternalForm().replace(url.getHost(), "localhost")).toString(), header); // NOI18N
            }
        } catch (IOException ex) {
            //ignore unknown sheets
        }
    }

    /**
     * Stop listening on changes.
     */
    synchronized void stop() {
        if (webKit != null) {
            webKit.getCSS().removeListener(listener);
        }
        this.webKit = null;
        this.localhost = null;
        this.project = null;
        this.listener = null;
        sheetsMap.clear();
        fobToSheetMap.clear();
    }

    /**
     * @return true if listener is active. false otherwise. 
     */
    synchronized boolean isStarted() {
        return this.webKit != null;
    }

    /**
     * Updates css in browser using webKit.
     * @param snapshot 
     */
    synchronized void update(FileObject fileObject, String content) {
        if (webKit == null) {
            return;
        }
        Project owner = FileOwnerQuery.getOwner(fileObject);
        if (owner == null) {
            return;
        }
        URL serverUrl = ServerURLMapping.toServer(owner, fileObject);
        if (serverUrl == null) {
            return;
        }
        String mimeType = fileObject.getMIMEType();
        if (mimeType.equals("text/html")) { // Should we be more strict, i.e., !mimeType.equals("text/css")? // NOI18N
            return; // Issue 225630
        }
        StyleSheetHeader header = sheetsMap.get(serverUrl.toString());
        if (header == null) {
            header = fobToSheetMap.get(fileObject);
        }
        if (header != null) {
            webKit.getCSS().setStyleSheetText(header.getStyleSheetId(), content);
        }
    }

    @ServiceProvider(service = LiveUpdater.class)
    public static class LiveUpdaterImpl implements LiveUpdater {

        private final RequestProcessor RP = new RequestProcessor(LiveUpdaterImpl.class);

        @Override
        public boolean update(final Document doc) {
            if (!CSSUpdater.getDefault().isStarted()) {
                return false;
            }
            RP.post(new Runnable() {
                @Override
                public void run() {
                    doc.render(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String text = doc.getText(0, doc.getLength());
                                //hopefully it's safe to stay in the read lock...
                                CSSUpdater.getDefault().update(getDataObject(doc).getPrimaryFile(), text);
                            } catch (BadLocationException badLocationException) {
                                Exceptions.printStackTrace(badLocationException);
                            }
                        }
                    });
                }
            });
            return false;
        }
        
        private static DataObject getDataObject(Document doc) {
            Object sdp = doc == null ? null : doc.getProperty(Document.StreamDescriptionProperty);
            if (sdp instanceof DataObject) {
                return (DataObject) sdp;
            }
            return null;
        }
        
    }

    /**
     * Listener for addition/removal of style-sheets.
     */
    private class Listener implements CSS.Listener {

        @Override
        public void mediaQueryResultChanged() {
        }

        @Override
        public void styleSheetChanged(String styleSheetId) {
        }

        @Override
        public void styleSheetAdded(StyleSheetHeader header) {
            registerStyleSheet(header);
        }

        @Override
        public void styleSheetRemoved(String styleSheetId) {
        }
        
    }
}
