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
package org.netbeans.modules.web.common.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Simple Web Server supporting only GET command on project's source files.
 */
public final class WebServer {
    private static final int PORT = 8383;
    private static final Logger LOGGER = Logger.getLogger(WebServer.class.getName());
    private final WeakHashMap<Project, Pair> deployedApps = new WeakHashMap<>();
    private boolean init = false;
    private Server server;
    private static WebServer webServer;
    
    private WebServer() {
    }

    public static synchronized WebServer getWebserver() {
        if (webServer == null) {
            webServer = new WebServer();
        }
        return webServer;
    }

    private synchronized void checkStartedServer() {
        if (!init) {
            init = true;
            startServer();
        }
    }
    
    /**
     * Start serving project's sources under given web context root.
     * 
     * @param p project whose sources should be served.
     * @param siteRoot site root.
     * @param webContextRoot web context root.
     */
    public void start(Project p, FileObject siteRoot, String webContextRoot) {
        assert webContextRoot != null && webContextRoot.startsWith("/") : // NOI18N
                "webContextRoot must start with slash character"; // NOI18N
        checkStartedServer();
        deployedApps.remove(p);
        forgetAnyProjectWithThisContext(webContextRoot);
        deployedApps.put(p, new Pair(webContextRoot, siteRoot));
    }

    // #236293
    private void forgetAnyProjectWithThisContext(String webContextRoot) {
        for (Iterator<Entry<Project, Pair>> it = deployedApps.entrySet().iterator(); it.hasNext();) {
            Entry<Project, Pair> entry = it.next();
            if (webContextRoot.equals(entry.getValue().webContextRoot)) {
                it.remove();
            }
        }
    }

    private static class Pair {
        String webContextRoot;
        FileObject siteRoot;

        public Pair(String webContextRoot, FileObject siteRoot) {
            this.webContextRoot = webContextRoot;
            this.siteRoot = siteRoot;
        }
    }
    
    /**
     * Stop serving project's sources.
     * 
     * @param p project whose sources should no longer be served.
     */
    public void stop(Project p) {
        deployedApps.remove(p);
        // TODO: if deployedApps is empty we can stop the server
    }

    /**
     * Port server is running on.
     * 
     * @return port the server is running on.
     */
    public int getPort() {
        checkStartedServer();
        return server.getPort();
    }

    /**
     * Converts project's file into server URL.
     * 
     * @param projectFile project's file to convert.
     * @return returns null if project is not currently served
     */
    public URL toServer(FileObject projectFile) {
        Project p = FileOwnerQuery.getOwner(projectFile);
        if (p != null) {
            Pair pair = deployedApps.get(p);
            if (pair != null) {
                String path = pair.webContextRoot + (pair.webContextRoot.equals("/") ? "" : "/") +  //NOI18N
                        FileUtil.getRelativePath(pair.siteRoot, projectFile);
                return WebUtils.stringToUrl("http://localhost:"+getPort()+path); //NOI18N
            }
        } else {
            // fallback if project was not found:
            for (Map.Entry<Project, Pair> entry : deployedApps.entrySet()) {
                Pair pair = entry.getValue();
                String relPath = FileUtil.getRelativePath(pair.siteRoot, projectFile);
                if (relPath != null) {
                    String path = pair.webContextRoot + (pair.webContextRoot.equals("/") ? "" : "/") +  //NOI18N
                            relPath;
                    return WebUtils.stringToUrl("http://localhost:"+getPort()+path); //NOI18N
                }
            }
        }
        return null;
    }

    /**
     * Converts server URL back into project's source file.
     * 
     * @param serverURL server URL to convert.
     * @return project's source file corresponding to the given server URL.
     */
    public FileObject fromServer(URL serverURL) {
        String path;
        try {
            path = serverURL.toURI().getPath();
        } catch (URISyntaxException ex) {
            path = serverURL.getPath(); // fallback
        }
        return fromServer(path);
    }

    private FileObject fromServer(String serverURLPath) {
        Map.Entry<Project, Pair> rootEntry = null;
        for (Map.Entry<Project, Pair> entry : deployedApps.entrySet()) {
            if ("/".equals(entry.getValue().webContextRoot)) { //NOI18N
                rootEntry = entry;
                // process this one as last one:
                continue;
            }
            if (serverURLPath.startsWith(entry.getValue().webContextRoot+"/")) { //NOI18N
                return findFile(entry, serverURLPath);
            }
        }
        if (rootEntry != null && serverURLPath.startsWith("/")) { // NOI18N
            return findFile(rootEntry, serverURLPath);
        }
        return null;
    }

    private FileObject findFile(Entry<Project, Pair> entry, String serverURL) {
        int index = entry.getValue().webContextRoot.length()+1;
        if (entry.getValue().webContextRoot.equals("/")) { //NOI18N
            index = 1;
        }
        String file = serverURL.substring(index);
        return entry.getValue().siteRoot.getFileObject(file);
    }

    private void startServer() {
        server = new Server();
        new Thread( server ).start();
        Thread shutdown = new Thread(){
            @Override
            public void run() {
                server.stop();
            }
        };
        Runtime.getRuntime().addShutdownHook( shutdown);
    }

    private static class Server implements Runnable {

        private AtomicBoolean stop = new AtomicBoolean(false);
        private ServerSocket sock;
        private int port;
        private static final Map<String, String> mimeTypes = new HashMap<>();

        public Server() {
            port = PORT;
            while (true) {
                try {
                    sock = new ServerSocket(port);
                } catch (IOException ex) {
                    // port used:
                    port++;
                    continue;
                }
                break;
            }
        }
        
        @Override
        public void run() {
            readMimeTypes();
            ExecutorService pool = new RequestProcessor(WebServer.class.getName(), 10);
            while (!stop.get()) {
                final Socket s;
                try {
                    s = sock.accept();
                } catch (SocketException ex) {
                    if (!stop.get()) {
                        Exceptions.printStackTrace(ex);
                    }
                    // abort server:
                    return;
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    // abort server:
                    return;
                }
                if (stop.get()) {
                    break;
                }
                pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            read(s.getInputStream(), s.getOutputStream());
                        } catch (IOException ex) {
                            // do not abort server in this case
                            LOGGER.log(Level.FINE, "reading socket failed", ex); // NOI18N
                        }
                    }
                });
            }
        }

        private void stop() {
            stop.set(true);
            try {
                sock.close();
            } catch (IOException ex) {
            }
        }

        public int getPort() {
            return port;
        }
        
        private void read(InputStream inputStream, OutputStream outputStream) throws IOException {
            BufferedReader r = null;
            DataOutputStream out = null;
            InputStream fis = null;
            try {
                r = new BufferedReader(new InputStreamReader(inputStream));
                String line = r.readLine();
                if (line == null || line.length() == 0) {
                    return;
                }
                if (line.startsWith("GET ")) { //NOI18N
                    StringTokenizer st = new StringTokenizer(line, " "); //NOI18N
                    st.nextToken();
                    String file = st.nextToken();
                    try {
                        file = URLDecoder.decode(file, "UTF-8"); //NOI18N
                    } catch (IllegalArgumentException ex) {
                        // #222858 - IllegalArgumentException: URLDecoder: Illegal hex characters in escape (%) pattern - For input string: "%2"
                        // silently ignore
                        LOGGER.log(Level.FINE, "cannot decode '"+file+"'", ex); // NOI18N
                    }
                    // #223770
                    int queryIndex = file.indexOf('?');
                    if (queryIndex != -1) {
                        file = file.substring(0, queryIndex);
                    }
                    FileObject fo = getWebserver().fromServer(file);
                    if (fo != null && fo.isFolder()) {
                        fo = fo.getFileObject("index", "html"); //NOI18N
                    }
                    if (fo != null) {
                        fis = fo.getInputStream();
                        out = new DataOutputStream(outputStream);
                        String mime = fo.getMIMEType();
                        if ("content/unknown".equals(mime)) { //NOI18N
                            String m = guessMimeTypeFromExtension(fo);
                            if (m != null) {
                                mime = m;
                            }
                        }
                        if ("content/unknown".equals(mime)) { //NOI18N
                            mime = "text/plain"; //NOI18N
                        }

                        // #228966 - Run an xhtml file in a Html5 Project, Browser Treats xhtml like text
                        if ("text/xhtml".equals(mime)) { //NOI18N
                            mime = "application/xhtml+xml"; //NOI18N
                        }
                        
                        try {
                            out.writeBytes("HTTP/1.1 200 OK\nContent-Length: "+fo.getSize()+"\n" //NOI18N
                                    + "Content-Type: "+mime+"\n\n"); //NOI18N
                            FileUtil.copy(fis, out);
                        } catch (SocketException se) {
                            // browser refused to accept data or closed the connection;
                            // not much we can do about this
                        }
                    }
                }
            } finally {
                if (fis != null) {
                    fis.close();
                }
                if (r != null) {
                    r.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }

        private void readMimeTypes() {
            InputStream is = WebServer.class.getResourceAsStream("mime.types"); // NOI18N
            Pattern p = Pattern.compile("[ \\t]+");
            assert is != null;
            Scanner line = new Scanner(is).useDelimiter("\n");
            while (line.hasNext()) {
                String lineString = line.next();
                if(lineString.trim().startsWith("#") || lineString.trim().isEmpty()) {
                    continue;
                }
                Scanner elements = new Scanner(lineString).useDelimiter(p);
                String mimeType = null;
                while (elements.hasNext()) {
                    String s = elements.next();
                    if (mimeType == null) {
                        mimeType = s;
                    } else {
                        mimeTypes.put(s, mimeType);
                    }
                }
            }
        }

        private String guessMimeTypeFromExtension(FileObject fo) {
            return mimeTypes.get(fo.getExt().toLowerCase());
        }
    
    }
    
}
