/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.server.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishConfig;
import org.netbeans.modules.glassfish.tooling.data.GlassFishJavaEEConfig;
import org.netbeans.modules.glassfish.tooling.data.GlassFishJavaSEConfig;
import org.netbeans.modules.glassfish.tooling.data.GlassFishLibrary;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;

/**
 * Provides GlassFish library information from XML configuration files.
 * <p/>
 * Instance of library builder for single version of GlassFish server.
 * Version of GlassFish server is supplied with first configuration getter call.
 * Each subsequent configuration getter call on the same instance must be used
 * with the same GlassFish version.
 * <p/>
 * XML configuration file is read just once with first configuration getter
 * call. Returned values are cached for subsequent getter calls which
 * are very fast.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ConfigBuilder {

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build <code>List</code> of <code>GlassFishLibrary</code> objects
     * representing libraries found in particular GlassFish server installation.
     * <p/>
     * @param libConfigs    List of libraries configuration nodes.
     * @param classpathHome Directory tree to search for class path elements.
     * @param javadocsHome  Directory tree to search for java doc.
     * @param srcHome       Directory tree to search for source files.
     * @return <code>List</code> of <code>GlassFishLibrary</code> objects
     *         representing libraries found in particular GlassFish server
     *         installation.
     */
    private static List<GlassFishLibrary> getLibraries(
            final List<LibraryNode> libConfigs, final File classpathHome,
            final File javadocsHome, final File srcHome) {

        List<GlassFishLibrary> result = new LinkedList<>();

        try {
            for (LibraryNode libConfig : libConfigs) {
               
                List<File> classpath = ConfigUtils.processFileset(
                        libConfig.classpath, classpathHome.getAbsolutePath());
                List<File> javadocs = ConfigUtils.processFileset(
                        libConfig.javadocs, javadocsHome.getAbsolutePath());
                List<URL>  javadocUrls
                        = ConfigUtils.processLinks(libConfig.javadocs);
                List<File> sources = ConfigUtils.processFileset(
                        libConfig.sources, srcHome.getAbsolutePath());
                result.add(new GlassFishLibrary(libConfig.libraryID,
                        buildUrls(classpath),
                        buildUrls(javadocs, javadocUrls),
                        libConfig.javadocs.getLookups(),
                        buildUrls(sources),
                        ConfigUtils.processClassPath(classpath)));
            }
        } catch (FileNotFoundException e) {
            throw new GlassFishIdeException(
                    "Some files required by configuration were not found.", e);
        }
        return result;
    }

    /**
     * Converts provided list of files to <code>URL</code> objects and appends
     * supplied <code>URL</code> objects to this list.
     * <p/>
     * @param files List of files to convert to <code>URL</code> objects.
     * @param urls  <code>URL</code> objects to append to this list.
     * @return List of <code>URL</code> objects containing content of both
     *         supplied lists.
     */
    private static List<URL> buildUrls(
            final List<File> files, final List<URL> urls) {
        List<URL> result = buildUrls(files);
        result.addAll(urls);
        return result;
    }

   /**
     * Converts provided list of files to <code>URL</code> objects.
     * <p/>
     * @param files List of files to convert to <code>URL</code> objects.
     * @return List of <code>URL</code> objects containing files from
     *         supplied list.
     */
    private static List<URL> buildUrls(final List<File> files) {
        ArrayList<URL> result = new ArrayList<>(files.size());
        for (File file : files) {
            URL url = ConfigUtils.fileToURL(file);
            if (url != null) {
                result.add(url);
            }
        }
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Library builder configuration. */
    private final Config config;

    /** Classpath search prefix. */
    private final File classpathHome;

    /** Javadoc search prefix. */
    private final File javadocsHome;

    /** Source code search prefix. */
    private final File srcHome;

    /** Libraries cache. */
    /* GuarderBy(this)*/
    private List<GlassFishLibrary> libraryCache;

    /** GlassFish JavaEE configuration cache. */
    /* GuarderBy(this)*/
    private GlassFishJavaEEConfig javaEEConfigCache;

    /** GlassFish JavaSE configuration cache. */
    /* GuarderBy(this)*/
    private GlassFishJavaSEConfig javaSEConfigCache;

    /** Version check. */
    /* GuarderBy(this)*/
    private GlassFishVersion version;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Creates an instance of GlassFish library builder.
     * <p/>
     * Stores provided GlassFish version to configuration file mapping.
     * <p/>
     * @param config         Library builder configuration. Should not
     *                       be <code>null</code>.
     * @param classpathHome  Classpath search prefix.
     * @param javadocsHome   Javadoc search prefix.
     * @param srcHome        Source code search prefix.
     */
    ConfigBuilder(final Config config, final String classpathHome,
            final String javadocsHome, final String srcHome) {
        this.config = config;
        this.classpathHome = new File(classpathHome);
        this.javadocsHome = new File(javadocsHome);
        this.srcHome = new File(srcHome);
    }

    /**
     * Creates an instance of GlassFish library builder.
     * <p/>
     * Stores provided GlassFish version to configuration file mapping.
     * <p/>
     * @param config         Library builder configuration. Should not
     *                       be <code>null</code>.
     * @param classpathHome  Classpath search prefix.
     * @param javadocsHome   Javadoc search prefix.
     * @param srcHome        Source code search prefix.
     */
    ConfigBuilder(final Config config, final File classpathHome,
            File javadocsHome, File srcHome) {
        this.config = config;
        this.classpathHome = classpathHome;
        this.javadocsHome = javadocsHome;
        this.srcHome = srcHome;
    }

    /**
     * Internal version check to avoid usage of a single builder instance
     * for multiple GlassFish versions.
     * <p/>
     * @param version GlassFish version being checked.
     * @throws ServerConfigException when builder is used with multiple
     *         GlassFish versions.
     */
    private synchronized void versionCheck(final GlassFishVersion version)
            throws ServerConfigException {
        if (this.version == null) {
            this.version = version;
        } else if (this.version != version) {
            throw new ServerConfigException(
                    "Library builder was already used for GlassFish "
                    + this.version + " use new instance for GlassFish"
                    + version);
        }
    }

    /**
     * Get GlassFish libraries configured for provided GlassFish version.
     * <p/>
     * This method shall not be used with multiple GlassFish versions
     * for the same instance of {@link ConfigBuilder} class.
     * <p/>
     * @param version GlassFish version.
     * @return List of libraries configured for GlassFish of given version.
     * @throws ServerConfigException when builder instance is used with multiple
     *         GlassFish versions.
     */
    public List<GlassFishLibrary> getLibraries(
            final GlassFishVersion version) throws ServerConfigException {
        versionCheck(version);
        synchronized (this) {
            if (libraryCache != null) {
                return libraryCache;
            }
            GlassFishConfig configAdapter
                    = GlassFishConfigManager.getConfig(
                            ConfigBuilderProvider.getBuilderConfig(version));
            List<LibraryNode> libConfigs
                    = configAdapter.getLibrary();
            libraryCache = getLibraries(
                    libConfigs, classpathHome, javadocsHome, srcHome);
            return libraryCache;
        }
    }

    /**
     * Get GlassFish JavaEE configuration for provided GlassFish version.
     * <p/>
     * This method shall not be used with multiple GlassFish versions
     * for the same instance of {@link ConfigBuilder} class.
     * <p/>
     * @param version GlassFish version.
     * @return GlassFish JavaEE configuration for provided GlassFish
     *         of given version.
     * @throws ServerConfigException when builder instance is used with multiple
     *         GlassFish versions.
     */
    public GlassFishJavaEEConfig getJavaEEConfig(
            final GlassFishVersion version) throws ServerConfigException {
        versionCheck(version);
        synchronized (this) {
            if (javaEEConfigCache != null) {
                return javaEEConfigCache;
            }
            GlassFishConfig configAdapter
                    = GlassFishConfigManager.getConfig(
                            ConfigBuilderProvider.getBuilderConfig(version));
            javaEEConfigCache = new GlassFishJavaEEConfig(
                    configAdapter.getJavaEE(), classpathHome);
            return javaEEConfigCache;
        }
    }

    /**
     * Get GlassFish JavaSE configuration for provided GlassFish version.
     * <p/>
     * This method shall not be used with multiple GlassFish versions
     * for the same instance of {@link ConfigBuilder} class.
     * <p/>
     * @param version GlassFish version.
     * @return GlassFish JavaSE configuration for provided GlassFish
     *         of given version.
     * @throws ServerConfigException when builder instance is used with multiple
     *         GlassFish versions.
     */
    public GlassFishJavaSEConfig getJavaSEConfig(
            final GlassFishVersion version) throws ServerConfigException {
        versionCheck(version);
        synchronized (this) {
            if (javaSEConfigCache != null) {
                return javaSEConfigCache;
            }
            GlassFishConfig configAdapter
                    = GlassFishConfigManager.getConfig(
                            ConfigBuilderProvider.getBuilderConfig(version));
            javaSEConfigCache = new GlassFishJavaSEConfig(
                    configAdapter.getJavaSE());
            return javaSEConfigCache;
        }
    }

}
