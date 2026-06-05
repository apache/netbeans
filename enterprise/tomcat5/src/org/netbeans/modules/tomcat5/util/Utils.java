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
package org.netbeans.modules.tomcat5.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class.
 *
 * @author sherold
 */
public final class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

    /** Creates a new instance of Utils */
    private Utils() {
        super();
    }

    /** Return true if the specified port is free, false otherwise. */
    public static boolean isPortFree(int port) {
        try (ServerSocket soc = new ServerSocket(port)) {
            soc.close();
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    /** Return true if a Tomcat server is running on the specifed port */
    public static boolean pingTomcat(int port, int timeout, String serverHeader, String managerUrl, String username, String password) {
        // checking whether a socket can be created is not reliable enough, see #47048
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), timeout); // NOI18N
            socket.setSoTimeout(timeout);
            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                // request
                out.println("HEAD /netbeans-tomcat-status-test HTTP/1.1\r\nHost: localhost:" + port + "\r\n\r\n"); // NOI18N
                out.flush();

                // response
                String text = in.readLine();
                if (text == null || !text.startsWith("HTTP/")) { // NOI18N
                    return false; // not an http response
                }
                Map<String, List<String>> headerFileds = new HashMap<>();
                while ((text = in.readLine()) != null && text.length() > 0) {
                    int colon = text.indexOf(":");
                    if (colon <= 0) {
                        return false; // not an http header
                    }
                    String name = text.substring(0, colon).trim();
                    String value = text.substring(colon + 1).trim();
                    List<String> list = headerFileds.get(name);
                    if (list == null) {
                        list = new ArrayList<>();
                        headerFileds.put(name, list);
                    }
                    list.add(value);
                }
                List<String> server = headerFileds.get("Server"); // NIO18N
                if (server != null) {
                    if (server.contains(serverHeader)) { // NOI18N
                        return true;
                    } else if (server.contains("Sun-Java-System/Web-Services-Pack-1.4")) {  // NOI18N
                        // it is probably Tomcat with JWSDP installed
                        return true;
                    }
                }
                if (managerUrl != null) {
                    return pingTomcatManager(managerUrl, port, timeout, username, password);
                }
                return false;
            }
        } catch (IOException ioe) {
            return false;
        }
    }

    public static boolean pingTomcatManager(String managerUrl, int port, int timeout, String username, String password) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(managerUrl).openConnection();
            if (username != null && password != null) {
                String auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
                conn.setRequestProperty("Authorization", "Basic " + auth);
            }
            try {
                int response = conn.getResponseCode();
                return response == HttpURLConnection.HTTP_OK
                        || response == HttpURLConnection.HTTP_FORBIDDEN
                        || response == HttpURLConnection.HTTP_UNAUTHORIZED;
            } finally {
                conn.disconnect();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return false;
        }
    }

    public static String generatePassword(int length) {
        int ran2 = 0;
        Random random = new Random();
        StringBuilder pwd = new StringBuilder();
        for (int i = 0; i < length; i++) {
            //ran2 = (int)(Math.random()*61);
            ran2 = random.nextInt(61);
            if (ran2 < 10) {
                ran2 += 48;
            } else {
                if (ran2 < 35) {
                    ran2 += 55;
                } else {
                    ran2 += 62;
                }
            }
            char c = (char) ran2;
            pwd.append(c);
        }
        return pwd.toString();
    }
}
