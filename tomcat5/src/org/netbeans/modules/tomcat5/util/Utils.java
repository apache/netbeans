/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import java.util.ArrayList;
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
        try {
            ServerSocket soc = new ServerSocket(port);
            try {
                soc.close();
            } finally {
                return true;
            }
        } catch (IOException ioe) {
            return false;
        }
    }
    
    /** Return true if a Tomcat server is running on the specifed port */
    public static boolean pingTomcat(int port, int timeout, String serverHeader, String managerUrl) {
        // checking whether a socket can be created is not reliable enough, see #47048
        Socket socket = new Socket();
        try {
            try {
                socket.connect(new InetSocketAddress("localhost", port), timeout); // NOI18N
                socket.setSoTimeout(timeout);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    try {
                        // request
                        out.println("HEAD /netbeans-tomcat-status-test HTTP/1.1\r\nHost: localhost:" + port + "\r\n"); // NOI18N
                        out.flush();

                        // response
                        String text = in.readLine();
                        if (text == null || !text.startsWith("HTTP/")) { // NOI18N
                            return false; // not an http response
                        }
                        Map<String, List<String>> headerFileds = new HashMap();
                        while ((text = in.readLine()) != null && text.length() > 0) {
                            int colon = text.indexOf(':');
                            if (colon <= 0) {
                                return false; // not an http header
                            }
                            String name = text.substring(0, colon).trim();
                            String value = text.substring(colon + 1).trim();
                            List<String> list = headerFileds.get(name);
                            if (list == null) {
                                list = new ArrayList();
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
                            return pingTomcatManager(managerUrl, port, timeout);
                        }
                        return false;
                    } finally {
                        in.close();
                    }
                } finally {
                    out.close();
                }
            } finally {
                socket.close();
            }
        } catch (IOException ioe) {
            return false;
        }
    }

    public static boolean pingTomcatManager(String managerUrl, int port, int timeout) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(managerUrl).openConnection();
            try {
                int response = conn.getResponseCode();
                return response == HttpURLConnection.HTTP_OK
                        || response == HttpURLConnection.HTTP_FORBIDDEN
                        || response == HttpURLConnection.HTTP_UNAUTHORIZED;
            } finally {
                conn.disconnect();
            }
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return false;
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
