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
package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.glassfish.tooling.data.GlassFishLibrary;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.server.config.ConfigBuilder;
import org.netbeans.modules.glassfish.tooling.server.config.ConfigBuilderProvider;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.glassfish.common.GlassFishLogger;
import static org.netbeans.modules.glassfish.javaee.ide.Hk2PluginProperties.fileToUrl;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 * GlassFish bundled libraries provider.
 * <p/>
 * Builds <code>Library</code> instance containing Jersey library from GlassFish
 * modules. Only GlassFish v3 up to v7 are supported.
 * 
 * @author Tomas Kraus
 * @author Peter Benedikovic
 */
public class Hk2LibraryProvider /*implements JaxRsStackSupportImplementation*/ {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////
    
    /** Logger for this class. */
    private static final Logger LOGGER = GlassFishLogger.get(Hk2LibraryProvider.class);

    /** Library provider type. */
    private static final String PROVIDER_TYPE = "j2se";

    /** Java EE library name suffix to be added after server instance name.
     *  Java EE library name must be unique so combination of instance name
     *  and some common suffix is used. */
    private static final String JAVAEE_NAME_SUFFIX = " Java EE";

    /** Java EE library name suffix to be added after server instance name.
     *  Jersey library name must be unique so combination of instance name
     *  and some common suffix is used. */
    private static final String JERSEY_NAME_SUFFIX = " Jersey";

    /** JAX-RS library name suffix to be added after server instance name.
     *  JAX-RS library name must be unique so combination of instance name
     *  and some common suffix is used. */
    private static final String JAXRS_NAME_SUFFIX = " JAX-RS";

    /** Java EE library name pattern to search for it in
     *  <code>GlassFishLibrary</code> list. */
    private final Pattern JAVAEE_PATTERN = Pattern.compile("[jJ]ava {0,1}[eE]{2}");

    /** Jersey library name pattern to search for it in
     *  <code>GlassFishLibrary</code> list. */
    private final Pattern JERSEY_PATTERN = Pattern.compile("[jJ]ersey.*");

    /** JAX-RS library name pattern to search for it in
     *  <code>GlassFishLibrary</code> list. */
    private final Pattern JAXRS_PATTERN
            = Pattern.compile("[jJ][aA][xX][ -]{0,1}[rR][sS]");

    /** JEE 8 Code base for file locator. */
    static final String JAVAEE_DOC_CODE_BASE
            = "org.netbeans.modules.j2ee.platform";
    
    /** JAKARTA EE 9 Code base for file locator. */
    static final String JAKARTAEE9_DOC_CODE_BASE
            = "org.netbeans.modules.jakartaee9.platform";
    
    /** JAKARTA EE 10 Code base for file locator. */
    static final String JAKARTAEE10_DOC_CODE_BASE
            = "org.netbeans.modules.jakartaee10.platform";

    /** Internal {@see GlassFishServer} to {@see Hk2LibraryProvider}
     *  mapping. */
    private static final ConcurrentMap <GlassFishServer, Hk2LibraryProvider> providers
            = new ConcurrentHashMap<>();

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns {@see Hk2LibraryProvider} class instance for specific server
     * instance.
     * <p/>
     * Provider instances for individual {@see GlassFishServer} instances
     * are shared.
     * <p/>
     * @param server {@see GlassFishServer} instance for which provider
     *               is returned.
     * @return {@see Hk2LibraryProvider} class instance for given server
     *         instance.
     */
    public static Hk2LibraryProvider getProvider(GlassFishServer server) {
        Hk2LibraryProvider provider;
        provider = providers.computeIfAbsent(server, k -> 
                new Hk2LibraryProvider(server));
        return provider;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Library builder associated with current platform.
      * This attribute should be accessed only using {@see #getBuilder()} even
      * internally. */
    private volatile ConfigBuilder builder;

    /** GlassFish server home directory. */
    private final String serverHome;

    /** GlassFish server name. */
    private final String serverName;
    
    /** GlassFish server version. */
    private final GlassFishVersion serverVersion;

    /** GlassFish server instance. */
    private final GlassFishServer server;

    /** Java EE library name associated with current GlassFish server context.
     *  This is lazy initialized internal cache. Do not access this attribute
     *  outside {@see #getJavaEEName()} method! */
    private volatile String javaEEName = null;

    /** Jersey library name associated with current GlassFish server context.
     *  This is lazy initialized internal cache. Do not access this attribute
     *  outside {@see #getJerseyName()} method! */
    private volatile String jerseyName = null;

    /** Jersey JAX-RS name associated with current GlassFish server context.
     *  This is lazy initialized internal cache. Do not access this attribute
     *  outside {@see #getJaxRsName()} method! */
    private volatile String jaxRsName = null;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Jersey library provider.
     * <p/>
     * @param server GlassFish server entity.
     */
    private Hk2LibraryProvider(GlassFishServer server) {
        if (server == null) {
            throw new IllegalArgumentException(
                    "GlassFish server entity shall not be null.");
        }
        serverHome = server.getServerHome();
        serverName = server.getName();
        serverVersion = server.getVersion();
        builder = ConfigBuilderProvider.getBuilder(server);
        this.server = server;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get Java EE library name for this server context.
     * <p/>
     * This library name shall be registered in default {@see LibraryManager}
     * and is unique for Jersey modules of given GlassFish server instance.
     * Library name is cached after first usage.
     * <p/>
     * @return Java EE library name for this server context.
     */
    public String getJavaEEName() {
        if (javaEEName != null) {
            return javaEEName;
        }
        synchronized (this) {
            StringBuilder sb = new StringBuilder(
                    serverName.length() + JAVAEE_NAME_SUFFIX.length());
            sb.append(serverName);
            sb.append(JAVAEE_NAME_SUFFIX);
            javaEEName = sb.toString();
        }
        return javaEEName;
    }

    /**
     * Get Jersey library name for this server context.
     * <p/>
     * This library name shall be registered in default {@see LibraryManager}
     * and is unique for Jersey modules of given GlassFish server instance.
     * Library name is cached after first usage.
     * <p/>
     * @return Jersey library name for this server context.
     */
    public String getJerseyName() {
        if (jerseyName != null) {
            return jerseyName;
        }
        synchronized (this) {
            StringBuilder sb = new StringBuilder(
                    serverName.length() + JERSEY_NAME_SUFFIX.length());
            sb.append(serverName);
            sb.append(JERSEY_NAME_SUFFIX);
            jerseyName = sb.toString();
        }
        return jerseyName;
    }

    /**
     * Get JAX-RS library name for this server context.
     * <p/>
     * This library name shall be registered in default {@see LibraryManager}
     * and is unique for Jersey modules of given GlassFish server instance.
     * Library name is cached after first usage.
     * <p/>
     * @return JAX-RS library name for this server context.
     */
    public String getJaxRsName() {
        if (jaxRsName != null) {
            return jaxRsName;
        }
        synchronized (this) {
            StringBuilder sb = new StringBuilder(
                    serverName.length() + JAXRS_NAME_SUFFIX.length());
            sb.append(serverName);
            sb.append(JAXRS_NAME_SUFFIX);
            jaxRsName = sb.toString();
        }
        return jaxRsName;
    }

    /**
     * Return Jersey libraries available in GlassFish.
     * <p/>
     * @return Jersey libraries available in GlassFish.
     */
    public Library getJerseyLibrary() {
        return getLibrary(JERSEY_PATTERN, getJerseyName());
    }

    /**
     * Set {@see LibraryImplementation} content for Jersey libraries
     * available in GlassFish.
     * <p/>
     * @param lib         Target {@see LibraryImplementation}.
     * @param libraryName Library name in returned Library instance.
     */
    public void setJerseyImplementation(
            LibraryImplementation lib, String libraryName) {
        setLibraryImplementationContent(lib, JERSEY_PATTERN, libraryName);
    }

    /**
     * Get {@see List} of class path {@see URL}s for Jersey libraries.
     * <p/>
     * @return {@see List} of class path {@see URL}s for Jersey libraries.
     */
    public List<URL> getJerseyClassPathURLs() {
        return getLibraryClassPathURLs(JERSEY_PATTERN);
    }

    /**
     * Return JAX-RS libraries available in GlassFish.
     * <p/>
     * @return JAX-RS libraries available in GlassFish.
     */
    public Library getJaxRsLibrary() {
        return getLibrary(JAXRS_PATTERN, getJaxRsName());
    }

   /**
     * Set {@see LibraryImplementation} content for JAX-RS libraries
     * available in GlassFish.
     * <p/>
     * @param lib         Target {@see LibraryImplementation}.
     * @param libraryName Library name in returned Library instance.
     */
    public void setJaxRsLibraryImplementation(
            LibraryImplementation lib, String libraryName) {
        setLibraryImplementationContent(lib, JAXRS_PATTERN, libraryName);
    }

    /**
     * Get {@see List} of class path {@see URL}s for JAX-RS libraries.
     * <p/>
     * @return {@see List} of class path {@see URL}s for JAX-RS libraries.
     */
    public List<URL> getJaxRsClassPathURLs() {
        return getLibraryClassPathURLs(JAXRS_PATTERN);
    }

    /**
     * Return Java EE libraries available in GlassFish.
     * <p/>
     * @return Java EE libraries available in GlassFish\.
     */
    public Library getJavaEELibrary() {
        return getLibrary(JAVAEE_PATTERN, getJavaEEName());
    }

     /**
     * Set {@see LibraryImplementation} content for Java EE libraries
     * available in GlassFish.
     * <p/>
     * @param lib         Target {@see LibraryImplementation}.
     * @param libraryName Library name in returned Library instance.
     */
    public void setJavaEELibraryImplementation(
            LibraryImplementation lib, String libraryName) {
        setLibraryImplementationContent(lib, JAVAEE_PATTERN, libraryName);
    }

    /**
     * Get {@see List} of class path {@see URL}s for Java EE libraries.
     * <p/>
     * @return {@see List} of class path {@see URL}s for Java EE libraries.
     */
    public List<URL> getJavaEEClassPathURLs() {
        return getLibraryClassPathURLs(JAVAEE_PATTERN);
    }

    /**
     * Return libraries available in GlassFish.
     * <p/>
     * @param namePattern Library name pattern to search for it in
     *                    <code>GlassFishLibrary</code> list.
     * @param libraryName Library name in returned Library instance.
     * @return Requested GlassFish library.
     */
    private Library getLibrary(Pattern namePattern, String libraryName) {
        Library lib = LibraryManager.getDefault().getLibrary(libraryName);
        if (lib != null) {
            return lib;
        }
        List<GlassFishLibrary> gfLibs = builder.getLibraries(serverVersion);
        for (GlassFishLibrary gfLib : gfLibs) {
            if (namePattern.matcher(gfLib.getLibraryID()).matches()) {
                Map<String, List<URL>> contents = new HashMap<>(4);
                Map<String, String> properties = new HashMap<>(4);
                contents.put("classpath", translateArchiveUrls(gfLib.getClasspath()));
                contents.put("javadoc", translateArchiveUrls(gfLib.getJavadocs()));
                properties.put("maven-dependencies", gfLib.getMavenDeps());
                properties.put("maven-repositories", "default");
                try {
                    return LibraryManager.getDefault().createLibrary(
                            PROVIDER_TYPE,
                            libraryName,
                            null,
                            null,
                            contents,
                            properties);
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, 
                            "Could not create Jersey library for {0}: {1}", 
                            new Object[] {serverName, ioe});
                }
            }
        }
        return null;
    }

    /**
     * Set {@see LibraryImplementation} content for given library name.
     * <p/>
     * @param lib         Target {@see LibraryImplementation}.
     * @param namePattern Library name pattern to search for it in
     *                    <code>GlassFishLibrary</code> list.
     * @param libraryName Library name in returned Library instance.
     */
    private void setLibraryImplementationContent(LibraryImplementation lib,
            Pattern namePattern, String libraryName) {
        List<GlassFishLibrary> gfLibs = builder.getLibraries(serverVersion);
        for (GlassFishLibrary gfLib : gfLibs) {
            if (namePattern.matcher(gfLib.getLibraryID()).matches()) {
                List<String> javadocLookups = gfLib.getJavadocLookups();
                lib.setName(libraryName);
                // Build class path
                List<URL> cp = new ArrayList<>();
                for (URL url : gfLib.getClasspath()) {
                    if (FileUtil.isArchiveFile(url)) {
                        cp.add(FileUtil.getArchiveRoot(url));
                    } else {
                        cp.add(url);
                    }
                }
                // Build java docs
                List<URL> javadoc = new ArrayList<>();
                if (javadocLookups != null) {
                    for (String lookup : javadocLookups) {
                        try {
                            File eeDoc;
                            if (GlassFishVersion.ge(serverVersion, GlassFishVersion.GF_7_0_0)) {
                                eeDoc = InstalledFileLocator
                                    .getDefault().locate(lookup,
                                    JAKARTAEE10_DOC_CODE_BASE, false);
                            } else if (GlassFishVersion.ge(serverVersion, GlassFishVersion.GF_6)) {
                                eeDoc = InstalledFileLocator
                                    .getDefault().locate(lookup,
                                    JAKARTAEE9_DOC_CODE_BASE, false);
                            } else {
                                eeDoc = InstalledFileLocator
                                    .getDefault().locate(lookup,
                                    JAVAEE_DOC_CODE_BASE, false);
                            }
                            if (eeDoc != null) {
                                javadoc.add(fileToUrl(eeDoc));
                            }
                        } catch (MalformedURLException e) {
                            ErrorManager.getDefault()
                                    .notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }
                }
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH,
                        cp);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC,
                        javadoc);
            }
        }
    }

    /**
     * Get list of class path {@see URL}s for given library name.
     * <p/>
     * @param namePattern Library name pattern to search for it in
     *                    <code>GlassFishLibrary</code> list.
     * @param libraryName Library name in returned Library instance.
     */
    private List<URL> getLibraryClassPathURLs(Pattern namePattern) {
        List<GlassFishLibrary> gfLibs = builder.getLibraries(serverVersion);
        for (GlassFishLibrary gfLib : gfLibs) {
            if (namePattern.matcher(gfLib.getLibraryID()).matches()) {
                return gfLib.getClasspath();
            }
        }
        return Collections.<URL>emptyList();
    }

    private List<URL> translateArchiveUrls(List<URL> urls) {
        List<URL> result = new ArrayList<>(urls.size());
        for (URL u : urls) {
            if (FileUtil.isArchiveFile(u)) {
                result.add(FileUtil.getArchiveRoot(u));
            } else {
                result.add(u);
            }
        }
        return result;
    }
}
