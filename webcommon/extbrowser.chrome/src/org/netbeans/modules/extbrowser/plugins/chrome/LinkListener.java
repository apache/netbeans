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
package org.netbeans.modules.extbrowser.plugins.chrome;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputReaderTask;
import org.netbeans.api.extexecution.input.InputReaders;

import org.openide.awt.HtmlBrowser;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

final class LinkListener implements HyperlinkListener {
    
    private static final Logger LOGGER = Logger.getLogger(LinkListener.class.getName());

    private final RequestProcessor myProcessor = new RequestProcessor(LinkListener.class);

    @Override
    public void hyperlinkUpdate( HyperlinkEvent e ) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            final URL url = e.getURL();
            if (url.getProtocol().equals("file")) { // NOI18N
                // first, try java api
                try {
                    final Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.OPEN)) {
                        // #225130
                        myProcessor.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    desktop.open(Utilities.toFile(url.toURI()));
                                }
                                catch (IOException ex) {
                                    LOGGER.log(Level.FINE, null, ex);
                                    openNativeFileManager(url);
                                }
                                catch (URISyntaxException ex) {
                                    LOGGER.log(Level.FINE, null, ex);
                                    openNativeFileManager(url);
                                }
                            }
                        });
                    }
                    else {
                        openNativeFileManager(url);
                    }
                }
                // Fix for BZ#218782 - UnsupportedOperationException: Desktop API is not supported on the current platform
                catch (UnsupportedOperationException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                    openNativeFileManager(url);
                }
            } else {
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            }
        }
    }

    private void openNativeFileManager(URL url) {
        String executable;
        if (Utilities.isWindows()) {
            executable = "explorer.exe"; // NOI18N
        } else if (Utilities.isMac()) {
            executable = "open"; // NOI18N
        } else {
            assert Utilities.isUnix() : "Unix expected";
            executable = "xdg-open"; // NOI18N
        }
        try {
            Process process = new ExternalProcessBuilder(executable)
                    .addArgument(url.toURI().toString())
                    .redirectErrorStream(true)
                    .call();
            InputReaderTask task = InputReaderTask.newTask(
                    InputReaders.forStream(process.getInputStream(), 
                            Charset.defaultCharset()), null);
            myProcessor.post(task);
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

}