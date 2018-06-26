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
package org.netbeans.modules.j2ee.weblogic9.j2ee;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Petr Hejl
 */
public class JerseyLibraryHelper {

    private static final String LIBRARY_PROVIDER_TYPE = "j2se"; // NOI18N

    private static final String LIBRARY_TYPE = "wl_jersey"; // NOI18N

    @NbBundle.Messages({"# {0} - server version", "library.displayName=Jersey WebLogic {0}"})
    @CheckForNull
    static Library getJerseyLibrary(
            Version serverVersion, FileObject modulesFolder) {

        if (serverVersion == null) {
            // TODO some default?
            return null;
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            LibraryHandler handler = new LibraryHandler();

            InputStream is = JaxRsStackSupportImpl.class.getClassLoader().getResourceAsStream(
                    "org/netbeans/modules/j2ee/weblogic9/resources/netbeans-jersey.xml"); // NOI18N
            try {
                parser.parse(is, handler);

                Server serverToUse = null;
                for (Server server : handler.getServers()) {
                    if (serverVersion.isAboveOrEqual(server.getVersion())) {
                        if (serverToUse == null || serverToUse.getVersion().isBelowOrEqual(server.getVersion())) {
                            serverToUse = server;
                        }
                    }
                }
                if (serverToUse != null) {
                    Library lib = LibraryManager.getDefault().getLibrary(LIBRARY_TYPE + "_" + serverToUse.getVersion());
                    if (lib != null) {
                        return lib;
                    }

                    StringBuilder mavenDeps = new StringBuilder();
                    List<URL> cp = new ArrayList<URL>();
                    for (ServerJar jar : serverToUse.getServerJars()) {
                        FileObject fo = getJarFile(modulesFolder, jar.getFilename());
                        if (fo != null) {
                            cp.add(URLMapper.findURL(fo, URLMapper.EXTERNAL));
                        }
                        mavenDeps.append(jar.getGroupId())
                                .append(':') // NOI18N
                                .append(jar.getArtifactId())
                                .append(':') // NOI18N
                                .append(jar.getVersion())
                                .append(":jar"); // NOI18N
                        mavenDeps.append(' ');
                    }
                    if (mavenDeps.length() > 0) {
                        mavenDeps.setLength(mavenDeps.length() - 1);
                    }

                    Map<String,List<URL>> contents = new HashMap<String, List<URL>>(1);
                    contents.put("classpath", cp); // NOI18N
                    Map<String, String> properties = new HashMap<String, String>(2);
                    properties.put("maven-dependencies", mavenDeps.toString()); // NOI18N
                    properties.put("maven-repositories", "default"); // NOI18N

                    lib = LibraryManager.getDefault().createLibrary(LIBRARY_PROVIDER_TYPE,
                            LIBRARY_TYPE + "_" + serverToUse.getVersion(), // NOI18N
                            Bundle.library_displayName(serverToUse.getVersion()),
                            null,
                            contents,
                            properties);

                    return lib;
                }
            } finally {
                is.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(JerseyLibraryHelper.class.getName()).log(Level.WARNING, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(JerseyLibraryHelper.class.getName()).log(Level.WARNING, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(JerseyLibraryHelper.class.getName()).log(Level.WARNING, null, ex);
        }
        return null;
    }

    private static FileObject getJarFile(FileObject modulesFolder, String jarName) {
        if (modulesFolder == null) {
            return null;
        }
        FileObject[] children = modulesFolder.getChildren();
        for (FileObject child : children) {
            if (child.getNameExt().equals(jarName)) {
                return child;
            }
        }
        return null;
    }

    private static class LibraryHandler extends DefaultHandler {

        private final List<Server> servers = new ArrayList<Server>();

        private Version version;

        private List<ServerJar> serverJars;

        private boolean isServer;

        public List<Server> getServers() {
            if (servers != null) {
                return servers;
            }
            return Collections.emptyList();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("server".equals(qName)) { // NOI18N
                isServer= true;
                version = Version.fromDottedNotationWithFallback(attributes.getValue("version")); // NOI18N
                serverJars = new ArrayList<ServerJar>();
            } else if ("jar".equals(qName)) { // NOI18N
                String filename = attributes.getValue("name"); // NOI18N
                String groupId = attributes.getValue("groupId"); // NOI18N
                String artifactId = attributes.getValue("artifactId"); // NOI18N
                String jarVersion = attributes.getValue("version"); // NOI18N
                serverJars.add(new ServerJar(filename, groupId, artifactId, jarVersion));
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (isServer) {
                if ("server".equals(qName)) { // NOI18N
                    servers.add(new Server(version, serverJars));
                    isServer = false;
                    serverJars = null;
                }
            }
        }
    }

    private static class Server {

        private final Version version;

        private final List<ServerJar> serverJars;

        public Server(Version version, List<ServerJar> serverJars) {
            this.version = version;
            this.serverJars = serverJars;
        }

        public Version getVersion() {
            return version;
        }

        public List<ServerJar> getServerJars() {
            return serverJars;
        }
    }

    private static class ServerJar {

        private final String filename;

        private final String groupId;

        private final String artifactId;

        private final String version;

        public ServerJar(String filename, String groupId, String artifactId, String version) {
            this.filename = filename;
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        public String getFilename() {
            return filename;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            return "ServerJar{" + "filename=" + filename + ", groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version + '}';
        }
    }
}
