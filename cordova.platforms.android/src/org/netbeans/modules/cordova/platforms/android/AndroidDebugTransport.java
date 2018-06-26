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
package org.netbeans.modules.cordova.platforms.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.modules.cordova.platforms.spi.MobileDebugTransport;
import org.netbeans.modules.cordova.platforms.api.ProcessUtilities;
import org.netbeans.modules.cordova.platforms.api.WebKitDebuggingSupport;
import org.netbeans.modules.netserver.api.ProtocolDraft;
import org.netbeans.modules.netserver.api.WebSocketClient;
import org.netbeans.modules.netserver.api.WebSocketReadHandler;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class AndroidDebugTransport extends MobileDebugTransport implements WebSocketReadHandler {

    private WebSocketClient webSocket;
    private static final Logger LOGGER = Logger.getLogger(AndroidDebugTransport.class.getName());
    private boolean flush;

    @Override
    public boolean detach() {
        if (webSocket != null) {
            webSocket.stop();
        }
        return true;
    }

    @Override
    public void sendCommandImpl(Command command) {
        String toString = translate(command.toString());
        webSocket.sendMessage(toString);
    }
    
    @Override
    public void accepted(SelectionKey key) {
        synchronized(webSocket) {
            webSocket.notifyAll();
        }
    }

    @Override
    public void read(SelectionKey key, byte[] message, Integer dataType) {
        final String string;
        string = new String(message, Charset.forName("UTF-8")).trim(); //NOI18N
        try {
            final Object parse = JSONValue.parseWithException(string);
            if (callBack == null) {
                LOGGER.info("callBack is null. Ignoring response: " + string);
            } else {
                callBack.handleResponse(new Response((JSONObject) parse));
            }
        } catch (ParseException ex) {
            Exceptions.attachMessage(ex, string);
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void closed(SelectionKey key) {
        WebKitDebuggingSupport.getDefault().stopDebugging(false);
    }

    public String getConnectionName() {
        return "Android"; //NOI18N
    }

    public WebSocketClient createWebSocket(WebSocketReadHandler handler) throws IOException {
        final URI uri = getURI();
        if (uri != null) {
            return new WebSocketClient(uri, ProtocolDraft.getRFC(), handler);
        }
        return null;
    }

    @Override
    public boolean attach() {
        try {
            String s = ProcessUtilities.callProcess(
                    ((AndroidPlatform) AndroidPlatform.getDefault()).getAdbCommand(), 
                    true, 
                    AndroidPlatform.DEFAULT_TIMEOUT, 
                    "forward", // NOI18N
                    "tcp:9222", getRedirectString()); //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        try {
            webSocket = createWebSocket(this);
            if (webSocket != null) {
            webSocket.start();
            return true;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
            return false;
        }

    private String getRedirectString() {
        String appName = getBundleIdentifier();
        if (appName == null) {
            //chrome
            return "localabstract:chrome_devtools_remote";
        } else {
            return "localabstract:webview_devtools_remote_" + AndroidPlatform.getDefault().getProcessIdByName(appName);
        }
    }
    
    

    @Override
    public String getVersion() {
        return "1.0"; //NOI18N
    }

    @NbBundle.Messages({
        "LBL_UriTitle=Could not connect to device or emulator",
        "ERR_CouldNotConnect=Please connect Android device (or run Android emulator) and make sure that:\n"
        + "\u2022 No more than one device or emulator is connected at the same time\n"
        + "\u2022 Device or emulator is listed in the output when invoking command \"adb devices\"\n"
        + "\u2022 USB Debugging is enabled on your device (if applicable)\n"
        + "\u2022 Your computer and Android device are connected to the same WiFi network (if applicable)"
    })
    private URI getURI() {
        JSONArray array = null;
        for (long stop = System.nanoTime() + TimeUnit.MINUTES.toNanos(2); stop > System.nanoTime() && !flush;) {
            try {
                JSONParser parser = new JSONParser();

                URL chromeJson = new URL("http://localhost:9222/json");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(chromeJson.openConnection(Proxy.NO_PROXY).getInputStream()))) {
                    Object obj = parser.parse(reader);
                    array = (JSONArray) obj;
                    if (array.size() == 0) {
                        try (BufferedReader r = new BufferedReader(new InputStreamReader(chromeJson.openConnection(Proxy.NO_PROXY).getInputStream()))) {
                            String line;
                            while ((line = r.readLine()) != null) {
                                LOGGER.info(line);
                            }
                        }
                    }
                    for (int i = 0; i < array.size(); i++) {
                        JSONObject object = (JSONObject) array.get(i);
                        String urlFromBrowser = object.get("url").toString(); // NOI18N
                        int hash = urlFromBrowser.indexOf("#"); // NOI18N
                        if (hash != -1) {
                            urlFromBrowser = urlFromBrowser.substring(0, hash);
                        }
                        if (urlFromBrowser.endsWith("/")) { // NOI18N
                            urlFromBrowser = urlFromBrowser.substring(0, urlFromBrowser.length() - 1);
                        }
                        URL conURL = getConnectionURL();
                        if (conURL ==null) {
                            //phonegap
                            setBaseUrl(urlFromBrowser);
                            if (object.containsKey("webSocketDebuggerUrl")) { // NOI18N
                                return new URI(object.get("webSocketDebuggerUrl").toString()); // NOI18N
                            } else {
                                continue;
                            }
                        }
                        final String connectionUrl = conURL.toExternalForm();
                        final String shortenedUrl = connectionUrl.replace(":80/", "/"); // NOI18N

                        if (connectionUrl.equals(urlFromBrowser) || shortenedUrl.equals(urlFromBrowser)) {
                            if (object.containsKey("webSocketDebuggerUrl")) { // NOI18N
                                return new URI(object.get("webSocketDebuggerUrl").toString()); // NOI18N
                            }
                        }
                    }
                }
            } catch (IOException | ParseException | URISyntaxException ex) {
                LOGGER.log(Level.FINE, "Cannot get websocket address, trying again...", ex);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex1) {
                    Exceptions.printStackTrace(ex1);
                }
            }
        }
        if (array != null) {
            LOGGER.info(array.toJSONString());
        }

        NotifyDescriptor not = new NotifyDescriptor(
                Bundle.ERR_CouldNotConnect(),
                Bundle.LBL_UriTitle(),
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object[]{NotifyDescriptor.OK_OPTION},
                null);
        DialogDisplayer.getDefault().notify(not);
        LOGGER.info("Cannot get websocket address.");
        return null;
    }

    @Override
    public void flush() {
        flush=true;
    }
}
