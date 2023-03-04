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

package org.netbeans.modules.weblogic.common.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Petr Hejl
 */
public final class DomainConfiguration {

    private static final Logger LOGGER = Logger.getLogger(DomainConfiguration.class.getName());

    private static final String DEFAULT_HOST = "localhost"; // NOI18N

    private static final int DEFAULT_PORT = 7001;

    private static final int DEFAULT_SECURED_PORT = 7002;

    private static final Pattern DOMAIN_NAME_PATTERN =
            Pattern.compile("(?:[a-z]+\\:)?name"); // NOI18N

    private static final Pattern DOMAIN_VERSION_PATTERN =
            Pattern.compile("(?:[a-z]+\\:)?domain-version"); // NOI18N

    private static final Pattern PRODUCTION_MODE_PATTERN =
            Pattern.compile("(?:[a-z]+\\:)?production-mode-enabled"); // NOI18N

    private static final Pattern LISTEN_ADDRESS_PATTERN =
            Pattern.compile("(?:[a-z]+\\:)?listen-address"); // NOI18N

    private static final Pattern SSL_PATTERN =
            Pattern.compile("(?:[a-z]+\\:)?ssl"); // NOI18N

    private static final Pattern ENABLED_PATTERN =
            Pattern.compile("(?:[a-z]+\\:)?enabled"); // NOI18N

    private static final Pattern LISTEN_PORT_PATTERN =
            Pattern.compile("(?:[a-z]+\\:)?listen-port"); // NOI18N

    private static final Pattern NAME_PATTERN =
            Pattern.compile("(?:[a-z]+\\:)?name"); // NOI18N

    private static final  Pattern SERVER_PATTERN =
            Pattern.compile("(?:[a-z]+\\:)?server"); // NOI18N

    private static final  Pattern ADMIN_SERVER_PATTERN =
            Pattern.compile("(?:[a-z]+\\:)?admin-server-name"); // NOI18N

    private static final  Pattern LOG_PATTERN =
            Pattern.compile("(?:[a-z]+\\:)?log"); // NOI18N

    private static final  Pattern FILE_NAME_PATTERN =
            Pattern.compile("(?:[a-z]+\\:)?file-name"); // NOI18N

    private final File domain;

    private final File domainConfig;

    // GuardedBy("this")
    private DomainChangeListener domainListener;

    // GuardedBy("this")
    private String name;

    // GuardedBy("this")
    private Version version;

    // GuardedBy("this")
    private String adminServer;

    // GuardedBy("this")
    private String host;

    // GuardedBy("this")
    private int port;

    // GuardedBy("this")
    private boolean secured;

    // GuardedBy("this")
    private boolean production;

    // GuardedBy("this")
    private File logFile;

    private DomainConfiguration(File domain, File domainConfig) {
        this.domain = domain;
        this.domainConfig = domainConfig;
    }

    @CheckForNull
    static DomainConfiguration getInstance(File domain, boolean listen) {
        File domainConfig = WebLogicLayout.getDomainConfigFile(domain);
        if (!domainConfig.isFile()) {
            return null;
        }

        DomainConfiguration instance = new DomainConfiguration(domain, domainConfig);
        if (listen) {
            instance.init();
        }
        instance.reload();
        return instance;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized Version getVersion() {
        return version;
    }

    public synchronized String getAdminServer() {
        return adminServer;
    }

    public synchronized String getHost() {
        return host;
    }

    public synchronized int getPort() {
        return port;
    }

    public synchronized boolean isSecured() {
        return secured;
    }

    public synchronized boolean isProduction() {
        return production;
    }

    public synchronized File getLogFile() {
        return logFile;
    }

    private void init() {
        if (domainConfig != null) {
            domainListener = new DomainChangeListener();
            // weak reference
            FileUtil.addFileChangeListener(domainListener, domainConfig);
        }
    }

    private synchronized void reload() {
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(domainConfig);
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);

            Element root = document.getDocumentElement();
            NodeList children = root.getChildNodes();

            Map<String, Server> servers = new LinkedHashMap<>();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (DOMAIN_NAME_PATTERN.matcher(child.getNodeName()).matches()) {
                    String domainName = child.getFirstChild().getNodeValue();
                    name = domainName;
                } else if (DOMAIN_VERSION_PATTERN.matcher(child.getNodeName()).matches()) {
                    String domainVersion = child.getFirstChild().getNodeValue();
                    if (domainVersion != null) {
                        version = Version.fromJsr277OrDottedNotationWithFallback(domainVersion);
                    } else {
                        version = null;
                    }
                } else if (PRODUCTION_MODE_PATTERN.matcher(child.getNodeName()).matches()) {
                    String domainProduction = child.getFirstChild().getNodeValue();
                    production = Boolean.parseBoolean(domainProduction);
                } else if (ADMIN_SERVER_PATTERN.matcher(child.getNodeName()).matches()) {
                    adminServer = child.getFirstChild().getNodeValue();
                } else if (LOG_PATTERN.matcher(child.getNodeName()).matches()) {
                    NodeList nl = child.getChildNodes();
                    // iterate over the children
                    for (int k = 0; k < nl.getLength(); k++) {
                        Node ch = nl.item(k);

                        if (FILE_NAME_PATTERN.matcher(ch.getNodeName()).matches()) {
                            String value = ch.getFirstChild().getNodeValue();
                            logFile = new File(value);
                            break;
                        }
                    }
                } else if (SERVER_PATTERN.matcher(child.getNodeName()).matches()) {
                    NodeList nl = child.getChildNodes();

                    String serverName = null;
                    String serverPort = null;
                    String serverHost = null;
                    String serverSecured = null;

                    // iterate over the children
                    for (int k = 0; k < nl.getLength(); k++) {
                        Node ch = nl.item(k);

                        if (NAME_PATTERN.matcher(ch.getNodeName()).matches()) {
                            serverName = ch.getFirstChild().getNodeValue();
                        }
                        if (LISTEN_PORT_PATTERN.matcher(ch.getNodeName()).matches()) {
                            serverPort = ch.getFirstChild().getNodeValue();
                        }
                        if (LISTEN_ADDRESS_PATTERN.matcher(ch.getNodeName()).matches()) {
                            if (ch.hasChildNodes()) {
                                serverHost = ch.getFirstChild().getNodeValue();
                            }
                        }
                        if (SSL_PATTERN.matcher(ch.getNodeName()).matches()) {
                            Node enabled = ch.getFirstChild();
                            if (ENABLED_PATTERN.matcher(enabled.getNodeName()).matches()) {
                                serverSecured = ch.getFirstChild().getNodeValue();
                            }
                        }
                    }

                    if (serverName != null) {
                        serverName = serverName.trim();
                    }
                    if (serverHost != null) {
                        serverHost = serverHost.trim();
                    }
                    if (serverPort != null) {
                        serverPort = serverPort.trim();
                    }
                    if (serverSecured != null) {
                        serverSecured = serverSecured.trim();
                    }

                    if (serverName != null && !serverName.isEmpty()) {
                        // address and port have minOccurs=0
                        if (serverHost == null || serverHost.isEmpty()) {
                            serverHost = DEFAULT_HOST;
                        }
                        boolean parsedServerSecured = false;
// we are prepared even for localhosts on ssl but it does
// not make any sense so far
//                        if (serverSecured != null && !serverSecured.isEmpty()) {
//                            // xsd boolean type ?
//                            parsedServerSecured = Boolean.parseBoolean(serverSecured);
//                        }
                        int parsedServerPort = parsedServerSecured ? DEFAULT_SECURED_PORT : DEFAULT_PORT;
                        if (serverPort != null && !serverPort.isEmpty()) {
                            try {
                                parsedServerPort = Integer.parseInt(serverPort);
                            } catch (NumberFormatException ex) {
                                LOGGER.log(Level.INFO, null, ex);
                            }
                        }
                        servers.put(serverName, new Server(serverName, serverHost,
                                parsedServerPort, parsedServerSecured));
                    }
                }
            }
            if (name != null && name.isEmpty()) {
                name = null;
            }
            Server admin = null;
            if (adminServer != null) {
                admin = servers.get(adminServer);
            }
            if (admin == null && !servers.isEmpty()) {
                admin = servers.entrySet().iterator().next().getValue();
            }
            if (admin != null) {
                host = admin.getHost();
                port = admin.getPort();
                secured = admin.isSecured();
            } else {
                host = DEFAULT_HOST;
                port = DEFAULT_PORT;
                secured = false;
            }

            if (logFile == null) {
                logFile = new File("logs" + File.separator + name + ".log"); // NOI18N
            }
            if (!logFile.isAbsolute()) {
                if (admin != null) {
                    // it is relative to servers
                    logFile = new File(domain, "servers" + File.separator + admin.getName() + File.separator + logFile.getPath()); // NOI18N
                } else {
                    // FIXME is there a better heuristic ?
                    logFile = new File(domain, "servers" + File.separator + "AdminServer" + File.separator + logFile.getPath()); // NOI18N
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            LOGGER.log(Level.INFO, null, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
    }

    private class DomainChangeListener implements FileChangeListener {

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }

        @Override
        public void fileChanged(FileEvent fe) {
            reload();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            // realistically this would not happen
            reload();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            // realistically this would not happen
            reload();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            // noop
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            // realistically this would not happen
            reload();
        }
    }

    private static class Server {

        private final String name;

        private final String host;

        private final int port;

        private final boolean secured;

        public Server(String name, String host, int port, boolean secured) {
            this.name = name;
            this.host = host;
            this.port = port;
            this.secured = secured;
        }

        public String getName() {
            return name;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public boolean isSecured() {
            return secured;
        }
    }
}
