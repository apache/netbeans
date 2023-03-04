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

package org.netbeans.modules.j2ee.weblogic9.config;

import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryImplementation;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Petr Hejl
 */
public class WLServerLibrarySupport {

    private static final Logger LOGGER = Logger.getLogger(WLServerLibrarySupport.class.getName());

    private static final FilenameFilter LIBRARY_FILTER = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar") // NOI18N
                        || name.endsWith(".war") // NOI18N
                        || name.endsWith(".ear"); // NOI18N
        }
    };

    private static final FilenameFilter JAR_FILTER = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar"); // NOI18N
        }
    };

    private final File domainPath;

    private final File serverRoot;

    private final boolean remote;

    public WLServerLibrarySupport(WLDeploymentManager dm) {
        String domainDir = dm.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);
        String serverDir = dm.getInstanceProperties().getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
        assert serverDir != null;

        this.domainPath = domainDir == null ? null : new File(domainDir);
        this.serverRoot = new File(serverDir);
        this.remote = dm.isRemote();
        assert domainPath != null || remote;
    }

    public WLServerLibrarySupport(File serverRoot, File domainPath) {
        this.domainPath = domainPath;
        this.serverRoot = serverRoot;
        this.remote = domainPath == null;
        assert domainPath != null || remote;
    }

    public Map<ServerLibrary, List<File>> getClasspathEntries(Set<ServerLibraryDependency> libraries)
            throws ConfigurationException {

        if (remote) {
            return Collections.emptyMap();
        }

        Set<WLServerLibrary> deployed = getDeployedLibraries();
        Set<WLServerLibrary> classpath = new HashSet<WLServerLibrary>();

        for (ServerLibraryDependency range : libraries) {
            for (WLServerLibrary lib : deployed) {
                // optimize
                if (range.versionMatches(ServerLibraryFactory.createServerLibrary(lib))) {
                    classpath.add((WLServerLibrary) lib);
                    break;
                }
            }
        }

        // TODO deployable files

        Map<ServerLibrary, List<File>> result = new HashMap<ServerLibrary, List<File>>();
        // XXX optimize collection of libs on same server and/or same name
        for (WLServerLibrary lib : classpath) {
            String server = lib.getServer();
            // XXX is this path always the same ??
            File tmpPath = new File(domainPath, "servers" + File.separator + server + File.separator + "tmp" // NOI18N
                   + File.separator + "_WL_user" + File.separator + lib.getName()); // NOI18N
            if (tmpPath.exists() && tmpPath.isDirectory()) {
                File[] subDirs = tmpPath.listFiles();
                if (subDirs != null) {
                    for (File subdir : subDirs) {
                        WLServerLibrary parsed = readFromFile(subdir);
                        if (parsed != null) {
                            if (sameLibraries(lib, parsed)) {
                                // FIXME other libs ?
                                File webInfLib = new File(subdir, "WEB-INF" + File.separator + "lib"); // NOI18N
                                if (webInfLib.exists() && webInfLib.isDirectory()) {
                                    File[] children = webInfLib.listFiles(JAR_FILTER);
                                    if (children != null) {
                                        result.put(ServerLibraryFactory.createServerLibrary(lib), Arrays.asList(children));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public Set<WLServerLibrary> getDeployedLibraries() {
        if (remote) {
            return Collections.emptySet();
        }
        FileObject domainConfig = WLPluginProperties.getDomainConfigFileObject(domainPath);
        if (domainConfig == null) {
            return Collections.emptySet();
        }

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            LibraryHandler handler = new LibraryHandler(domainPath);
            InputStream is = new BufferedInputStream(domainConfig.getInputStream());
            try {
                parser.parse(is, handler);

                Set<WLServerLibrary> libs = new HashSet<WLServerLibrary>();
                for (Library library : handler.getLibraries()) {
                    File file = library.resolveFile();
                    WLServerLibrary parsed = readFromFile(file);
                    if (parsed != null) {
                        // checking the version - maybe we can avoid it
                        if (parsed.getSpecificationVersion() != library.getSpecificationVersion()
                            && (parsed.getSpecificationVersion() == null
                                    || !parsed.getSpecificationVersion().equals(library.getSpecificationVersion()))) {
                            LOGGER.log(Level.INFO, "Inconsistent specification version for {0}", library.getName());
                        } else if (parsed.getImplementationVersion() != library.getImplementationVersion()
                            && (parsed.getImplementationVersion() == null
                                    || !parsed.getImplementationVersion().equals(library.getImplementationVersion()))) {
                            LOGGER.log(Level.INFO, "Inconsistent implementation version for {0}", library.getName());
                        } else {
                            libs.add(new WLServerLibrary(
                                    parsed.getSpecificationTitle(), parsed.getSpecificationVersion(),
                                    parsed.getImplementationTitle(), parsed.getImplementationVersion(),
                                    library.getTarget(), library.getName()));
                        }
                    } else {
                        LOGGER.log(Level.INFO, "Source path does not exists for {0}", library.getName());
                        // XXX we use name as spec title
                        libs.add(new WLServerLibrary(
                                null, library.getSpecificationVersion(),
                                null, library.getImplementationVersion(), library.getTarget(), library.getName()));
                    }
                }
                return libs;
            } finally {
                is.close();
            }
        } catch (IOException ex) {
            return Collections.emptySet();
        } catch (ParserConfigurationException ex) {
            return Collections.emptySet();
        } catch (SAXException ex) {
            return Collections.emptySet();
        }
    }

    Map<ServerLibrary, File> getDeployableFiles() {
        File libPath = FileUtil.normalizeFile(new File(serverRoot,
                "common" + File.separator + "deployable-libraries")); // NOI18N
        if (!libPath.exists() || !libPath.isDirectory()) {
            return Collections.emptyMap();
        }

        Map<ServerLibrary, File> res = new HashMap<ServerLibrary, File>();
        for (File file : libPath.listFiles(LIBRARY_FILTER)) {
            WLServerLibrary lib = readFromFile(file);
            if (lib != null) {
                res.put(ServerLibraryFactory.createServerLibrary(lib), file);
            }
        }
        return res;
    }

    // consider implementation of equals in WLServerLibrary
    public static boolean sameLibraries(WLServerLibrary first, WLServerLibrary second) {
        if ((first.specTitle == null) ? (second.specTitle != null) : !first.specTitle.equals(second.specTitle)) {
            return false;
        }
        if (first.specVersion != second.specVersion
                && (first.specVersion == null || !first.specVersion.equals(second.specVersion))) {
            return false;
        }
        if ((first.implTitle == null) ? (second.implTitle != null) : !first.implTitle.equals(second.implTitle)) {
            return false;
        }
        if (first.implVersion != second.implVersion
                && (first.implVersion == null || !first.implVersion.equals(second.implVersion))) {
            return false;
        }
        if ((first.name == null) ? (second.name != null) : !first.name.equals(second.name)) {
            return false;
        }
        return true;
    }

    private static WLServerLibrary readFromFile(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return null;
        }

        Attributes attributes = null;

        if (file.isDirectory()) {
            File manifestFile = new File(file, "META-INF" + File.separator + "MANIFEST.MF");
            if (manifestFile.exists() && manifestFile.isFile()) {
                Manifest manifest = new Manifest();
                try {
                    InputStream is = new BufferedInputStream(new FileInputStream(manifestFile));
                    try {
                        manifest.read(is);
                        attributes = manifest.getMainAttributes();
                    } finally {
                        is.close();
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        } else {
            try {
                JarFileSystem jar = new JarFileSystem();
                jar.setJarFile(file);
                attributes = jar.getManifest().getMainAttributes();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (PropertyVetoException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        if (attributes == null) {
            return null;
        }

        String specVersionValue = attributes.getValue("Specification-Version"); // NOI18N
        String implVersionValue = attributes.getValue("Implementation-Version"); // NOI18N
        String specTitle = attributes.getValue("Specification-Title"); // NOI18N
        String implTitle = attributes.getValue("Implementation-Title"); // NOI18N
        String name = attributes.getValue("Extension-Name"); // NOI18N

        Version specVersion = specVersionValue == null
                ? null
                : Version.fromJsr277NotationWithFallback(specVersionValue);
        Version implVersion = implVersionValue == null
                ? null
                : Version.fromJsr277NotationWithFallback(implVersionValue);

        return new WLServerLibrary(specTitle, specVersion, implTitle, implVersion, null, name);
    }

    public static class WLServerLibrary implements ServerLibraryImplementation {

        private final String specTitle;

        private final Version specVersion;

        private final String implTitle;

        private final Version implVersion;

        private final String server;

        private final String name;

        public WLServerLibrary(String specTitle, Version specVersion,
                String implTitle, Version implVersion, String server, String name) {
            this.specTitle = specTitle;
            this.specVersion = specVersion;
            this.implTitle = implTitle;
            this.implVersion = implVersion;
            this.server = server;
            this.name = name;
        }

        @Override
        public String getSpecificationTitle() {
            return specTitle;
        }

        @Override
        public Version getSpecificationVersion() {
            return specVersion;
        }

        @Override
        public String getImplementationTitle() {
            return implTitle;
        }

        @Override
        public Version getImplementationVersion() {
            return implVersion;
        }

        @Override
        public String getName() {
            return name;
        }

        public String getServer() {
            return server;
        }
    }

    private static class LibraryHandler extends DefaultHandler {

        private final List<Library> libraries = new ArrayList<Library>();

        private final File domainDir;

        private final StringBuilder value = new StringBuilder();

        private Library library;

        public LibraryHandler(File domainDir) {
            this.domainDir = domainDir;
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
            value.setLength(0);
            if ("library".equals(qName)) { // NOI18N
                library = new Library(domainDir);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (library == null) {
                return;
            }

            if ("library".equals(qName)) { // NOI18N
                libraries.add(library);
                library = null;
            } else if("name".equals(qName)) { // NOI18N
                String[] splitted = value.toString().split("#"); // NOI18N
                if (splitted.length > 1) {
                    library.setName(splitted[0]);
                    splitted = splitted[1].split("@"); // NOI18N
                    library.setSpecificationVersion(Version.fromJsr277NotationWithFallback(splitted[0]));
                    if (splitted.length > 1) {
                        library.setImplementationVersion(Version.fromJsr277NotationWithFallback(splitted[1]));
                    }
                } else {
                    library.setName(value.toString());
                }
            } else if ("target".equals(qName)) { // NOI18N
                library.setTarget(value.toString());
            } else if ("source-path".equals(qName)) { // NOI18N
                library.setFile(value.toString());
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            value.append(ch, start, length);
        }

        public List<Library> getLibraries() {
            return libraries;
        }
    }

    private static class Library {

        private final File baseFile;

        private String name;

        private Version specVersion;

        private Version implVersion;

        private String file;

        private String target;

        public Library(File baseFile) {
            this.baseFile = baseFile;
        }

        @CheckForNull
        public File resolveFile() {
            if (file == null) {
                return null;
            }

            File config = new File(file);
            if (!config.isAbsolute()) {
                if (baseFile != null) {
                    config = new File(baseFile, file);
                } else {
                    return null;
                }
            }
            if (config.exists() && config.isFile() && config.canRead()) {
                return FileUtil.normalizeFile(config);
            }
            return null;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Version getSpecificationVersion() {
            return specVersion;
        }

        public void setSpecificationVersion(Version specVersion) {
            this.specVersion = specVersion;
        }

        public Version getImplementationVersion() {
            return implVersion;
        }

        public void setImplementationVersion(Version implVersion) {
            this.implVersion = implVersion;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

    }
}
