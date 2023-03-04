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

package org.netbeans.modules.extbrowser;

import java.awt.EventQueue;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 * Basic support for default browser funcionality on Unix system,
 * currently using "xdg-open".
 * Note this class is not used for JDK 6 and up, for that purpose is used
 * build-in JDK mechanism (java.awt.Desktop#browse).
 *
 * @author Peter Zavadsky
 */
class NbDefaultUnixBrowserImpl extends ExtBrowserImpl {
    
    private static final Logger LOGGER = Logger.getLogger(NbDefaultUnixBrowserImpl.class.getName());

    private static final String XDG_OPEN_COMMAND = "xdg-open"; // NOI18N
    private static final String XDG_SETTINGS_COMMAND = "xdg-settings"; // NOI18N
    private static final String XBROWSER_COMMAND = "x-www-browser"; // NOI18N
    
    private static final RequestProcessor REQUEST_PROCESSOR = 
        new RequestProcessor( NbDefaultUnixBrowserImpl.class );

    private static final boolean XDG_OPEN_AVAILABLE;
    private static final boolean XDG_SETTINGS_AVAILABLE;
    private static final boolean XBROWSER_AVAILABLE;
    
    static {
        // XXX Lame check to find out whether the functionality is installed.
        // TODO Find some better way to ensure it is there.
        XDG_OPEN_AVAILABLE = new File("/usr/bin/" + XDG_OPEN_COMMAND).exists(); // NOI18N
        XDG_SETTINGS_AVAILABLE = new File("/usr/bin/" + XDG_SETTINGS_COMMAND).exists(); // NOI18N
        XBROWSER_AVAILABLE = new File("/usr/bin/" + XBROWSER_COMMAND).exists(); // NOI18N
    }
    
    static boolean isAvailable() {
        return XDG_OPEN_AVAILABLE || XBROWSER_AVAILABLE;
    }
    
    
    NbDefaultUnixBrowserImpl(ExtWebBrowser extBrowser) {
        super ();
        this.extBrowserFactory = extBrowser;
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + "NbDefaultUnixBrowserImpl created with factory: " + extBrowserFactory); // NOI18N
        }
    }

    @Override
    protected PrivateBrowserFamilyId detectPrivateBrowserFamilyId() {
        PrivateBrowserFamilyId browserFamilyId = detectBrowserFamily();
        if (browserFamilyId != null) {
            return browserFamilyId;
        }
        return super.detectPrivateBrowserFamilyId();
    }

    @CheckForNull
    private PrivateBrowserFamilyId detectBrowserFamily() {
        String browserIdent = detectDefaultWebBrowser();
        if (browserIdent == null
                && XBROWSER_AVAILABLE) {
            // fallback
            try {
                browserIdent = Paths.get("/usr/bin/x-www-browser").toRealPath().getFileName().toString().toLowerCase(Locale.US); // NOI18N
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Could not detect browser", ex);
            }
        }
        if (browserIdent == null) {
            return null;
        }
        if (browserIdent.indexOf("chrome") != -1) { // NOI18N
            return PrivateBrowserFamilyId.CHROME;
        }
        if (browserIdent.indexOf("chromium") != -1) { // NOI18N
            return PrivateBrowserFamilyId.CHROMIUM;
        }
        if (browserIdent.indexOf("firefox") != -1) { // NOI18N
            return PrivateBrowserFamilyId.FIREFOX;
        }
        if (browserIdent.indexOf("opera") != -1) { // NOI18N
            return PrivateBrowserFamilyId.OPERA;
        }
        if (browserIdent.indexOf("mozilla") != -1) { // NOI18N
            return PrivateBrowserFamilyId.MOZILLA;
        }
        return null;
    }

    @Override
    protected void loadURLInBrowserInternal(URL url) {
        assert !EventQueue.isDispatchThread();
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "" + System.currentTimeMillis() + "NbDeaultUnixBrowserImpl.setUrl: " + url); // NOI18N
        }
        url = URLUtil.createExternalURL(url, false);
        String urlArg = url.toExternalForm();
        // prefer xdg-open, then x-www-browser
        String command = XDG_OPEN_AVAILABLE ? XDG_OPEN_COMMAND : XBROWSER_COMMAND;

        ProcessBuilder pb = new ProcessBuilder(new String[] { command, urlArg });
        try {
            Process p = pb.start();
            REQUEST_PROCESSOR.post(new ProcessWatcher(p));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static String detectDefaultWebBrowser() {
        // XXX hotfix for #233047
        // assert !EventQueue.isDispatchThread();
        // #233145
        if (!XDG_SETTINGS_AVAILABLE) {
            return null;
        }
        OutputProcessorFactory outputProcessorFactory = new OutputProcessorFactory();
        ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(XDG_SETTINGS_COMMAND) // NOI18N
                .addArgument("get") // NOI18N
                .addArgument("default-web-browser"); // NOI18N
        ExecutionDescriptor silentDescriptor = new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL)
                .inputVisible(false)
                .frontWindow(false)
                .showProgress(false)
                .outProcessorFactory(outputProcessorFactory);
        Future<Integer> result = ExecutionService.newService(processBuilder, silentDescriptor, "Detecting default web browser") // NOI18N
                .run();
        try {
            result.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        String output = outputProcessorFactory.getOutput();
        if (output == null) {
            return null;
        }
        return output.toLowerCase(Locale.US);
    }

    private static final class ProcessWatcher implements Runnable {

        private final Process p;

        ProcessWatcher (Process p) {
            this.p = p;
        }

        public void run() {
            try {
                int exitValue = p.waitFor();
                if (exitValue != 0) {
                    StringBuilder sb = new StringBuilder();
                    InputStream is = p.getErrorStream();
                    try {
                        int curByte = 0;
                        while ((curByte = is.read()) != -1) {
                            sb.append((char)curByte);
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    ExtWebBrowser.getEM().log(Level.WARNING, sb.toString()); // NOI18N
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                // XXX #155906 Cleanup of the finished process.
                cleanupProcess(p);
            }
        }
    } // ProcessWatcher

    private static void cleanupProcess(Process p) {
        closeStream(p.getOutputStream());
        closeStream(p.getInputStream());
        closeStream(p.getErrorStream());
        p.destroy();
    }

    private static void closeStream(Closeable stream) {
        try {
            stream.close();
        } catch (IOException ioe) {
            log(ioe);
        }
    }

    private static void log(Exception e) {
        Logger.getLogger(NbDefaultUnixBrowserImpl.class.getName()).log(Level.INFO, null, e);
    }

    static final class OutputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {

        private volatile String output;


        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.bridge(new LineProcessor() {

                @Override
                public void processLine(String line) {
                    assert output == null : output + " :: " + line;
                    output = line;
                }

                @Override
                public void reset() {
                }

                @Override
                public void close() {
                }

            });
        }

        public String getOutput() {
            return output;
        }

    }

}
