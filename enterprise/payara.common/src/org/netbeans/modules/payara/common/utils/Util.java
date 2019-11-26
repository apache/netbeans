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

package org.netbeans.modules.payara.common.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.common.PortCollection;
import org.netbeans.modules.payara.common.parser.TreeParser;
import org.netbeans.modules.payara.spi.Utils;
import org.openide.filesystems.FileUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.netbeans.modules.payara.spi.PayaraModule;

/**
 *
 * @author vkraemer
 */
public final class Util {
    private static String DOMAIN_XML_PATH = "config/domain.xml";

    private Util() {
    }

    public static final String PF_LOOKUP_PATH = "Servers/Payara"; // NOI18N
    
    private static String INDICATOR = File.separatorChar == '/' ? "jrunscript" : "jrunscript.exe";
    private static FilenameFilter JDK6_DETECTION_FILTER = new FilenameFilter() {
        @Override
            public boolean accept(File arg0, String arg1) {
                if (arg1.equalsIgnoreCase(INDICATOR)) {
                    return true;
                }
                return false;
            }
    };

    public static boolean appearsToBeJdk6OrBetter(File javaExecutable) {
        File dir = javaExecutable.getParentFile();
        if (null != dir) {
            String[] hits = dir.list(Util.JDK6_DETECTION_FILTER);
            if (null != hits) {
                return hits.length > 0;
            }
        }
        return false;
    }


    /**
     * Add quotes to string if and only if it contains space characters.
     *
     * Note: does not handle generalized white space (tabs, localized white
     * space, etc.)
     *
     * @param path file path in string form.
     * @return quote path if it contains any space characters, otherwise same.
     */
    public static String quote(String path) {
        return path.indexOf(' ') == -1 ? path : "\"" + path + "\""; // NOI18N
    }

    /**
     * Add escape characters for backslash and dollar sign characters in
     * path field.
     *
     * @param path file path in string form.
     * @return adjusted path with backslashes and dollar signs escaped with
     *   backslash character.
     * @deprecated use spi.Utils.escapePath(String)
     */
    @Deprecated
    public static String escapePath(String path) {
        return Utils.escapePath(path);
    }

    /**
     * Convert classpath fragment using standard separator to a list of
     * normalized files (nonexistent jars will be removed).
     *
     * @param cp classpath string
     * @param root root folder for expanding relative path names
     * @return list of existing jars, normalized
     */
    public final List<File> classPathToFileList(String cp, File root) {
        List<File> result = new ArrayList<>();
        if(cp != null && cp.length() > 0) {
            String [] jars = cp.split(File.pathSeparator);
            for(String jar: jars) {
                File jarFile = new File(jar);
                if(!jarFile.isAbsolute() && root != null) {
                    jarFile = new File(root, jar);
                }
                if(jarFile.exists()) {
                    result.add(FileUtil.normalizeFile(jarFile));
                }
            }
        }
        return result;
    }
    
    public static boolean readServerConfiguration(File domainDir, PortCollection wi) {
        boolean result = false;
        File domainXml = new File(domainDir, DOMAIN_XML_PATH);
        final Map<String, HttpData> httpMap = new LinkedHashMap<>();

        if (domainXml.exists()) {
            List<TreeParser.Path> pathList = new ArrayList<>();
            pathList.add(new TreeParser.Path("/domain/configs/config/http-service/http-listener",
                    new TreeParser.NodeReader() {
                @Override
                public void readAttributes(String qname, Attributes attributes) throws SAXException {
                    // <http-listener
                    //   id="http-listener-1" port="8080" xpowered-by="true"
                    //   enabled="true" address="0.0.0.0" security-enabled="false"
                    //   family="inet" default-virtual-server="server"
                    //   server-name="" blocking-enabled="false" acceptor-threads="1">
                    try {
                        String id = attributes.getValue("id");
                        if(id != null && id.length() > 0) {
                            int port = Integer.parseInt(attributes.getValue("port"));
                            boolean secure = "true".equals(attributes.getValue("security-enabled"));
                            boolean enabled = !"false".equals(attributes.getValue("enabled"));
                            if(enabled) {
                                HttpData data = new HttpData(id, port, secure);
                                Logger.getLogger("payara").log(Level.FINER, " Adding {0}", data); // NOI18N
                                httpMap.put(id, data);
                            } else {
                                Logger.getLogger("payara").log(Level.FINER,
                                        "http-listener {0} is not enabled and won''t be used.", id); // NOI18N
                            }
                        } else {
                            Logger.getLogger("payara").log(Level.FINEST, "http-listener found with no name");
                        }
                    } catch(NumberFormatException ex) {
                        throw new SAXException(ex);
                    }
                }
            }));

            pathList.add(new TreeParser.Path("/domain/configs/config/network-config/network-listeners/network-listener",
                    new TreeParser.NodeReader() {
                @Override
                public void readAttributes(String qname, Attributes attributes) throws SAXException {
                    // <http-listener
                    //   id="http-listener-1" port="8080" xpowered-by="true"
                    //   enabled="true" address="0.0.0.0" security-enabled="false"
                    //   family="inet" default-virtual-server="server"
                    //   server-name="" blocking-enabled="false" acceptor-threads="1">
                    try {
                        String id = attributes.getValue("name");
                        if(id != null && id.length() > 0) {
                            String portAttr = attributes.getValue("port");
                            if (null == portAttr || portAttr.startsWith("$")) {
                                return;
                            }
                            int port = Integer.parseInt(portAttr);
                            boolean secure = "true".equals(attributes.getValue("security-enabled"));
                            boolean enabled = !"false".equals(attributes.getValue("enabled"));
                            if(enabled) {
                                HttpData data = new HttpData(id, port, secure);
                                Logger.getLogger("payara").log(Level.FINER, " Adding {0}", data);  // NOI18N
                                httpMap.put(id, data);
                            } else {
                                Logger.getLogger("payara").log(Level.FINER,
                                        "http-listener {0} is not enabled and won''t be used.", id);  // NOI18N
                            }
                        } else {
                            Logger.getLogger("payara").log(Level.FINEST, "http-listener found with no name");
                        }
                    } catch(NumberFormatException ex) {
                        throw new SAXException(ex);
                    }
                }
            }));

            try {
                TreeParser.readXml(domainXml, pathList);

                // !PW This probably more convoluted than it had to be, but while
                // http-listeners are usually named "http-listener-1", "http-listener-2", ...
                // technically they could be named anything.
                //
                // For now, the logic is as follows:
                //   admin port is the one named "admin-listener"
                //   http port is the first non-secure enabled port - typically http-listener-1
                //   https port is the first secure enabled port - typically http-listener-2
                // disabled ports are ignored.
                //
                HttpData adminData = httpMap.remove("admin-listener");
                if (null != wi) {
                    wi.setAdminPort(adminData != null ? adminData.getPort() : -1);
                }

                HttpData httpData = null;
                HttpData httpsData = null;

                for(HttpData data: httpMap.values()) {
                    if(data.isSecure()) {
                        if(httpsData == null) {
                            httpsData = data;
                        }
                    } else {
                        if(httpData == null) {
                            httpData = data;
                        }
                    }
                    if(httpData != null && httpsData != null) {
                        break;
                    }
                }

                int httpPort = httpData != null ? httpData.getPort() : -1;
                int adminPort = null!=adminData ? adminData.getPort() : -1;
                if (null != wi) {
                    wi.setHttpPort(httpPort);
                    wi.setHttpsPort(httpsData != null ? httpsData.getPort() : -1);
                }
                result = httpPort != -1 && adminPort != -1;
            } catch(IllegalStateException ex) {
                Logger.getLogger("payara").log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
        return result;
    }
    private static class HttpData {

        private final String id;
        private final int port;
        private final boolean secure;

        public HttpData(String id, int port, boolean secure) {
            this.id = id;
            this.port = port;
            this.secure = secure;
        }

        public String getId() {
            return id;
        }

        public int getPort() {
            return port;
        }

        public boolean isSecure() {
            return secure;
        }

        @Override
        public String toString() {
            return "{ " + id + ", " + port + ", " + secure + " }";
        }

    }

    static public String computeTarget(Map<String, String> ip) {
        String retVal = null;
        String url = ip.get(PayaraModule.URL_ATTR);
        if (null != url) {
            int lastColon = url.lastIndexOf(':');
            if (lastColon != -1) {
                String candidate = url.substring(lastColon+1);
                if (!Character.isDigit(candidate.charAt(0))) {
                    retVal = candidate;
                }
            }
        }
        return retVal;
    }
    
    static public boolean isDefaultOrServerTarget(Map<String, String> ip) {
        String target = Util.computeTarget(ip);
        return null == target || "server".equals(target);
    }

}
