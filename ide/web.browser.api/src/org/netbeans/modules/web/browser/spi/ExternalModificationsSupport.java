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
package org.netbeans.modules.web.browser.spi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.web.browser.Helper;
import org.netbeans.modules.web.common.api.DependentFileQuery;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 * This class has limited purpose for now and is likely going to be refactored.
 * Its only usecase is that Chrome Dev Tools send back to IDE modified content
 * of a URL and this support tries to match the URL with a project source file
 * and update it.
 *
 * @since 1.12
 */
public final class ExternalModificationsSupport {


    /**
     * There was a change in browser which needs to be persisted in the IDE.
     * @param url resource being changed
     * @param type type of resource being changed (??)
     * @param content new content of the file
     * @param currentBrowserURL URL which is currently opened in the browser;
     *   difference from url param is that currentBrowserURL can be index.html
     *   while url might be some.js file on which index.html depends
     */
    public static synchronized void handle(String url, String type, String content, URL currentBrowserURL) {
        Helper.urlBeingRefreshedFromBrowser.set(currentBrowserURL != null ? currentBrowserURL.toExternalForm() : null);
        try {
        URL u = WebUtils.stringToUrl(url);
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            FileObject fo = ServerURLMapping.fromServer(p, u);
            if (fo != null) {
                updateFileObject(fo, content);
                break;
            }
        }
        } finally {
            Helper.urlBeingRefreshedFromBrowser.set(null);
        }
    }

    private static void updateFileObject(FileObject modifiedFile, String content) {
        for (TopComponent tc : TopComponent.getRegistry().getOpened()) {
            FileObject fo = tc.getLookup().lookup(FileObject.class);
            if (fo != null && fo.equals(modifiedFile)) {
                EditorCookie ec = tc.getLookup().lookup(EditorCookie.class);
                if (ec != null) {
                    if (ec.isModified()) {
                        DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(
                                "Content of "+FileUtil.getFileDisplayName(modifiedFile)+" is modified in the IDE and therefore cannot be replaced "
                                + "with changes coming from the Chrome Developer Tools!"));
                        return;
                    }
                    StyledDocument doc = ec.getDocument();
                    if (doc != null) {
                        try {
                            doc.remove(0, doc.getLength());
                            doc.insertString(0, content, null);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        try {
                            ec.saveDocument();
                            return;
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
        OutputStream os = null;
        FileLock lock = null;
        try {
            lock = modifiedFile.lock();
            os = modifiedFile.getOutputStream(lock);
            // TODO: is encoding going to be OK?? what encoding CDT sends the file in??
            FileUtil.copy(new ByteArrayInputStream(content.getBytes()), os);
        } catch (FileAlreadyLockedException ex) {
            DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(
                    "Content of "+FileUtil.getFileDisplayName(modifiedFile)+" cannot be updated with "
                    + "changes coming from the Chrome Developer Tools because file is locked!"));
            return;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }
}
