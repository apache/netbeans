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
package org.netbeans.modules.cordova.platforms.ios;

import com.dd.plist.Base64;
import com.dd.plist.BinaryPropertyListWriter;
import com.dd.plist.NSData;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.XMLPropertyListParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.netbeans.modules.cordova.platforms.spi.MobileDebugTransport;
import org.netbeans.modules.cordova.platforms.api.WebKitDebuggingSupport;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Becicka
 */
public abstract class IOSDebugTransport extends MobileDebugTransport implements TransportImplementation {
    
    private final RequestProcessor RP = new RequestProcessor(IOSDebugTransport.class);
    private RequestProcessor.Task socketListener;
    protected volatile boolean keepGoing = true;
    private Tabs tabs = new IOSDebugTransport.Tabs();
    private final Object init = new Object();
    private static final Logger LOGGER = Logger.getLogger(IOSDebugTransport.class.getName());
    private final AtomicBoolean flush = new AtomicBoolean();
    
    

    public IOSDebugTransport() {
        setBundleIdentifier("com.apple.mobilesafari"); // NOI18N
    }
    
    @Override
    public boolean attach() {
        try {
            init();
            socketListener = RP.post(new Runnable() {
                @Override
                public void run() {
                    while (keepGoing) {
                        try {
                            process();
                        } catch (SocketException e) {
                            Logger.getLogger(IOSDebugTransport.class.getName()).log(Level.FINE, "Debugging Connection Closed", e);
                            return;
                        } catch (Exception exception) {
                            Exceptions.printStackTrace(exception);
                        }
                    }
                }
            });
            sendInitCommands();

            return true;
        } catch (IllegalStateException ise) {
            synchronized(tabs.monitor) {
                if (!flush.get()) {
                    throw ise;
                }
            }
            return false;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    @Override
    public void flush() {
        synchronized(tabs.monitor) {
            flush.set(true);
            tabs.monitor.notifyAll();
        }
    }
    
    
    
    private void process() throws Exception {
        NSObject object = readData();
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "\nReceived: {0}",                 //NOI18N
                    nsObjectToString(object));
        }
        if (object == null) {
            return;
        }

        JSONObject jmessage = extractResponse(object);
        if (jmessage != null) {
            if (callBack == null) {
                LOGGER.info("callBack is null. Ignoring response: " + jmessage.toString());
            } else {
                callBack.handleResponse(new Response(jmessage));
            }
        } else {
            if (!tabs.update(object)) {
                checkClose(object);
            }
        }
    }

    /**
     * Fix application identifier key when it is available. Before knowing the
     * application listing, incorrect identifier key is used (set in
     * WebKitDebuggingSupport#startDebugging(...). When the listing for our
     * application (or web browser with our page) is received, we should fix it
     * and use WIRApplicationIdentifierKey in form PID:XXXX.
     *
     * See bug #247400.
     *
     * @param object
     */
    private void fixApplicationIdentifierKey(NSObject object) {
        String appId = stringInNSObject(object,
                "__argument", "WIRApplicationIdentifierKey");           //NOI18N
        setBundleIdentifier(appId);
    }

    /**
     * Convert complex (nested dictionaries) NS object to string. Used mainly
     * for debugging.
     *
     * @param o
     * @return
     */
    String nsObjectToString(NSObject o) {
        return nsObjectToString(o, 0);
    }

    private String nsObjectToString(NSObject o, int lvl) {
        String basicIndent = "    ";                                    //NOI18N
        String levelIndent = "";                                        //NOI18N
        for (int i = 0; i < lvl; i++) {
            levelIndent += basicIndent;
        }
        if (o instanceof NSDictionary) {
            String[] allKeys = ((NSDictionary) o).allKeys();
            StringBuilder sb = new StringBuilder(lvl == 0 ? "\n" : ""); //NOI18N
            sb.append("{\n");                                           //NOI18N
            for (String key: allKeys) {
                NSObject objectForKey = ((NSDictionary) o).objectForKey(key);
                sb.append(levelIndent);
                sb.append(basicIndent);
                sb.append("\"");                                        //NOI18N
                sb.append(key);
                sb.append("\": ");                                      //NOI18N
                sb.append(nsObjectToString(objectForKey, lvl + 1));
                sb.append("\n");                                        //NOI18N
            }
            sb.append(levelIndent);
            sb.append("}\n");                                           //NOI18N
            return sb.toString();
        } else if (o instanceof NSString) {
            return "\"" + ((NSString) o).toString() + "\"";             //NOI18N
        } else if (o instanceof NSData) {
            NSData data = (NSData) o;
            String asStr = new String(data.bytes(), StandardCharsets.UTF_8);
            return "Data: " + asStr;
        } else if (o != null) {
            return o.toString();
        } else {
            return "null";                                              //NOI18N
        }
    }

    protected abstract NSObject readData() throws Exception;
    
    private String getCommand(String name, boolean replace) {
        try {
            Properties props = new Properties();
            props.load(IOSDebugTransport.class.getResourceAsStream("Command.properties"));
            final String cmd = props.getProperty(name).replace("$bundleId", getBundleIdentifier());
            if (!replace) {
                return cmd;
            }
            return cmd.replace("$tabIdentifier", getBundleIdentifier().equals("com.apple.mobilesafari") ? tabs.getActive() : "1"); // NOI18N
        } catch (java.util.NoSuchElementException nse) {
            LOGGER.log(Level.INFO, nse.getMessage(), nse);
            throw new IllegalStateException(nse);
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected final String createJSONCommand(JSONObject command) throws IOException {
        String json = translate(command.toString());
        String s = Base64.encodeBytes(json.getBytes());
        String res = getCommand("sendJSONCommand", true).replace("$json_encoded", s); // NOI18N
        return res;
    }

    protected final byte[] plistXmlToBinary(String msg) throws Exception {
        NSObject object = XMLPropertyListParser.parse(msg.getBytes());
        return BinaryPropertyListWriter.writeToArray(object);

    }
    
    private void checkClose(NSObject r) throws Exception {
        if (!(r instanceof NSDictionary)) {
            return;
        }
        String selector = stringInNSObject(r, "__selector");            //NOI18N
        if (selector == null) {
            return;
        }
        switch (selector) {
            case "_rpc_reportConnectedApplicationList:":                //NOI18N
                NSDictionary applications = dictInNSObject(r,
                        "__argument", //NOI18N
                        "WIRApplicationDictionaryKey");                 //NOI18N
                if (applications != null && applications.count() == 0) {
                    WebKitDebuggingSupport.getDefault().stopDebugging(false);
                }
                break;
            case "_rpc_applicationDisconnected:":                       //NOI18N
                String appId = stringInNSObject(r,
                        "__argument", //NOI18N
                        "WIRApplicationIdentifierKey");                 //NOI18N
                if (appId != null
                        && appId.equals(getBundleIdentifier())) {
                    WebKitDebuggingSupport.getDefault().stopDebugging(false);
                }
                break;
        }
    }

    private JSONObject extractResponse(NSObject r) throws Exception {
        if (r == null) {
            return null;
        }
        if (!(r instanceof NSDictionary)) {
            return null;
        }
        NSDictionary root = (NSDictionary) r;
        NSDictionary argument = (NSDictionary) root.objectForKey("__argument"); // NOI18N
        if (argument == null) {
            return null;
        }
        NSData data = (NSData) argument.objectForKey("WIRMessageDataKey"); // NOI18N
        if (data == null) {
            return null;
        }
        byte[] bytes = data.bytes();
        String s = new String(bytes);
        JSONObject o = (JSONObject) JSONValue.parseWithException(s);
        return o;
    }

    protected static InputStream fromString(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(bytes);
    }

    protected void stop() {
        keepGoing = false;
        synchronized (init) {
            init.notifyAll();
        }
        if (socketListener != null) {
            socketListener.cancel();
        }
    }

    @Override
    public boolean detach() {
        stop();
        return true;
    }

    @Override
    public final void sendCommandImpl(Command command) {
        try {
            sendCommand(command.getCommand());
            JSONObject o = command.getCommand();
            if (o != null) {
                Object methodO = o.get("method");                       //NOI18N
                if (methodO instanceof String) {
                    String method = (String) methodO;
                    // iOS sends no reposonse for some of the methods. Calling
                    // callbacks directly here as workaround.
                    if (callBack != null && (method.endsWith(".enable") //NOI18N
                            || method.endsWith(".disable")              //NOI18N
                            || method.endsWith(".navigate"))) {         //NOI18N
                        JSONObject resp = new JSONObject();
                        JSONObject empty = new JSONObject();
                        resp.put("id", command.getID());
                        resp.put("result", empty);                      //NOI18N
                        callBack.handleResponse(new Response(resp));
                    }
                }
            }
        } catch (Exception ex) {
            boolean s = keepGoing;
            stop();
            if (s) {
                WebKitDebuggingSupport.getDefault().stopDebugging(false);
            }
        }
    }

    protected void sendInitCommands() throws Exception {
        sendCommand(getCommand("setConnectionKey", false)); // NOI18N
        synchronized (init) {
            init.wait();
        }
        sendCommand(getCommand("connectToApp", false)); // NOI18N
        sendCommand(getCommand("setSenderKey", true)); // NOI18N
    }
    
    protected abstract void sendCommand(String command) throws Exception;
    protected abstract void sendCommand(JSONObject command) throws Exception;

    protected abstract void init() throws Exception;

    private class Tabs {

        private HashMap<String, TabDescriptor> map = new HashMap<>();
        private Object monitor = new Object();
        private boolean inited = false;

        public boolean update(NSObject r) throws Exception {
            if (!(r instanceof NSDictionary)) {
                return false;
            }
            NSDictionary listing = dictInNSObject(r,
                    "__argument", "WIRListingKey");                     //NOI18N
            if (listing == null) {
                return false;
            }
            boolean wasEmpty = map.isEmpty();
            boolean connectionUrlFound = false;
            HashMap<String, TabDescriptor> currentMap = new HashMap<>();
            for (String s : listing.allKeys()) {
                NSDictionary o = (NSDictionary) listing.objectForKey(s);
                NSObject identifier = o.objectForKey("WIRPageIdentifierKey"); // NOI18N
                NSObject url = o.objectForKey("WIRURLKey"); // NOI18N
                if (url == null) {
                    continue;
                }
                String urlString = url.toString().trim();
                if(urlString.isEmpty()) {
                    continue;
                }
                NSObject title = o.objectForKey("WIRTitleKey"); // NOI18N
                if (getConnectionURL()==null) {
                    //auto setup for phonegap. There is always on tab
                    setBaseUrl(url.toString());
                }
                currentMap.put(s, new TabDescriptor(urlString, title.toString(), identifier.toString()));
                if (checkUrlMatchesConnectionUrl(urlString)) {
                    connectionUrlFound = true;
                    fixApplicationIdentifierKey(r);
                }
            }
            if (!connectionUrlFound) {
                return false;
            }
            map.clear();
            map.putAll(currentMap);
            if (map.isEmpty()) {
                return !wasEmpty; // was not empty and now is empty -> updated
            } else {
                synchronized (init) {
                    init.notifyAll();
                }
                synchronized (monitor) {
                    inited = true;
                    monitor.notifyAll();
                }
                if (getTabForUrl() == null) {
                    WebKitDebuggingSupport.getDefault().stopDebugging(false);
                }
                return true;
            }
        }

        public TabDescriptor get(String key) {
            return map.get(key);
        }

        private String getActive() throws InterruptedException {
            synchronized (monitor) {
                if (!inited) {
                    monitor.wait(2 * 60 * 1000);
                }
            }
            final String tabForUrl = getTabForUrl();
            if (tabForUrl != null) {
                return tabForUrl;
            }
            return map.entrySet().iterator().next().getKey();
        }

        private String lastTab = null;

        private boolean checkUrlMatchesConnectionUrl(String url) {

            String normUrl = url;
            int hash = normUrl.indexOf("#"); // NOI18N
            if (hash != -1) {
                normUrl = normUrl.substring(0, hash);
            }
            if (normUrl.endsWith("/")) { // NOI18N
                normUrl = normUrl.substring(0, normUrl.length() - 1);
            }
            return getConnectionURL().toString().equals(normUrl.replace("file:///", "file:/"));
        }

        private String getTabForUrl() {
            for (Map.Entry<String, TabDescriptor> entry : map.entrySet()) {
                String urlFromBrowser = entry.getValue().getUrl();
                if (urlFromBrowser.startsWith("file:/")) { // NOI18N
                    //phonegap
                    return lastTab="1"; // NOI18N
                }
                if (checkUrlMatchesConnectionUrl(urlFromBrowser)) {
                    return lastTab = entry.getKey();
                }
            }
            for (Map.Entry<String, TabDescriptor> entry : map.entrySet()) {
                if (entry.getValue().getIdentifier().equals(lastTab)) {
                    return lastTab;
                }
            }
            return null;
        }

        private class TabDescriptor {

            String url;
            String title;
            String identifier;

            public TabDescriptor(String url, String title, String identifier) {
                this.url = url;
                this.title = title;
                this.identifier = identifier;
            }

            public String getUrl() {
                return url;
            }

            public String getTitle() {
                return title;
            }

            public String getIdentifier() {
                return identifier;
            }

            @Override
            public String toString() {
                return "TabDescriptor{" + "url=" + url + ", title=" + title + ", identifier=" + identifier + '}'; // NOI18N
            }
        }
    }

    /**
     * Helper method for accessing keys deeper in NSObject hierarchy.
     *
     * @param obj Root object.
     * @param path Path to contained object.
     *
     * @return Object found under specified key {@code path}, or null some of
     * its path elements is not available.
     */
    static NSObject findInNSObject(NSObject obj, String... path) {
        return findInNSObject(0, obj, path);
    }

    private static NSObject findInNSObject(int pos, NSObject obj, String... path) {
        if (obj == null) {
            throw new NullPointerException("obj is null");
        } else if (path == null) {
            throw new NullPointerException("path is null");
        } else if (path.length == pos) {
            return obj;
        } else if (obj instanceof NSDictionary) {
            NSDictionary dict = (NSDictionary) obj;
            NSObject next = dict.objectForKey(path[pos]);
            if (next == null) {
                return null;
            } else {
                return findInNSObject(pos + 1, next, path);
            }
        } else {
            return null;
        }
    }

    /**
     * Get string contained in NSObject hierarchy.
     *
     * @param obj Root object.
     * @param path Path to string value.
     *
     * @return String value, or null if not fount or if data type is not string.
     */
    static String stringInNSObject(NSObject obj, String... path) {
        NSObject res = findInNSObject(obj, path);
        if (res instanceof NSString) {
            return ((NSString) res).toString();
        } else {
            return null;
        }
    }

    /**
     * Get dictionary contained in NSObject hierarchy.
     *
     * @param obj Root object.
     * @param path Path to the dictionary.
     *
     * @return The dictionary, or null if not found or if data type is not
     * dictionary.
     */
    static NSDictionary dictInNSObject(NSObject obj, String... path) {
        NSObject res = findInNSObject(obj, path);
        if (res instanceof NSDictionary) {
            return (NSDictionary) res;
        } else {
            return null;
        }
    }
}
