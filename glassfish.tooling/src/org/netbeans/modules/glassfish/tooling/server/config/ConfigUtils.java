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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.netbeans.modules.glassfish.tooling.data.GlassFishLibrary;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ConfigUtils {

    /** Maven Group ID property name. */
    private static final String MVN_PROP_GROUP_ID = "groupId";

    /** Maven Artifact ID property name. */
    private static final String MVN_PROP_ARTIFACT_ID = "artifactId";

    /** Maven Version property name. */
    private static final String MVN_PROP_VERSION = "version";

    /** */
    private static final Pattern MVN_PROPS_PATTERN
            = Pattern.compile("META-INF/maven/[^/]+/[^/]+/pom.properties");

    /**
     * Convert {@link File} to {@link URL}.
     * <p/>
     * @param file {@link File} to be converted to {@link URL}.
     */
    static URL fileToURL(File file) {
        try {
            return file != null ? file.toURI().normalize().toURL() : null;
        } catch (MalformedURLException ex) {
            Logger.log(Level.WARNING, "Unable to convert file "
                    + file.getAbsolutePath() + " to URL", ex);
            return null;
        }
    }

    /**
     * Process <code>List</code> of links from library node and convert them
     * to <code>List</code> of {@link URL}s.
     * <p/>
     * @param fileset Library node.
     * @return <code>List</code> of {@link URL}s from library node.
     */
    static List<URL> processLinks(FileSet fileset) {
        List<String> links = fileset.getLinks();
        ArrayList<URL> result = new ArrayList<>(links.size());
        for (String urlString : links) {
            try {
                result.add(new URL(urlString));
            } catch (MalformedURLException mue) {
                Logger.log(Level.WARNING, "Cannot process URL: " + urlString
                        + ".", mue);
            }
        }
        return result;
    }

    /**
     * Process <code>List</code> of links from library node and convert them
     * of <code>List</code> of {@link File}s.
     * <p/>
     * @param fileset Library node.
     * @param rootDir File system search root.
     * @return <code>List</code> of {@link File}s from library node.
     * @throws FileNotFoundException When file from paths element was not found.
     */
    static List<File> processFileset(FileSet fileset, String rootDir)
            throws FileNotFoundException {
        Map<String, List<String>> filesets = fileset.getFilesets();
        List<String> paths = fileset.getPaths();
        ArrayList<File> result = new ArrayList<>();

        for (String dir : filesets.keySet()) {
            File d = new File(dir);
            String dirPrefix;
            if (!d.isAbsolute()) {
                dirPrefix = new File(rootDir, d.getPath()).getAbsolutePath();
            } else {
                dirPrefix = d.getAbsolutePath();
            }
            
            List<Pattern> patterns = compilePatterns(filesets.get(dir));
            File[] fileArray = new File(dirPrefix).listFiles(createFilter(
                    patterns));
            if (fileArray != null) {
                Collections.addAll(result, fileArray);
            }
        }
        
        for (String path : paths) {
            File f = new File(path);
            if (!f.isAbsolute()) {
                f = new File(rootDir, f.getPath());
            }
            if (!f.exists()) {
                throw new FileNotFoundException("File with name "
                        + path + " does not exist.");
            }
            result.add(f);
        }
        
        return result;
    }

    /**
     * Search class path for Maven information.
     * <p/>
     * @param classpath List of class path JAR files.
     * @return List of Maven information
     */
    static List<GlassFishLibrary.Maven> processClassPath(List<File> classpath) {
        List<GlassFishLibrary.Maven> mvnList = new LinkedList<>();
        for (File jar : classpath) {
            ZipFile zip = null;
            try {
                zip = new ZipFile(jar);
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    Matcher matcher
                            = MVN_PROPS_PATTERN.matcher(entry.getName());
                    if (matcher.matches()) {
                        GlassFishLibrary.Maven mvnInfo
                                = getMvnInfoFromProperties(zip.getInputStream(
                                entry));
                        if (mvnInfo != null) {
                            mvnList.add(mvnInfo);
                            break;
                        }
                    }
                }
            } catch (ZipException ze) {
                 Logger.log(Level.WARNING, "Cannot open JAR file "
                         + jar.getAbsolutePath() + ":", ze);
            } catch (IOException ioe) {
                 Logger.log(Level.WARNING, "Cannot process JAR file "
                         + jar.getAbsolutePath() + ":", ioe);
            } catch (IllegalStateException ise) {
                 Logger.log(Level.WARNING, "Cannot process JAR file "
                         + jar.getAbsolutePath() + ":", ise);
            } finally {
                if (zip != null) try {
                    zip.close();
                } catch (IOException ioe) {
                    Logger.log(Level.WARNING, "Cannot close JAR file "
                         + jar.getAbsolutePath() + ":", ioe);
                }
            }
            
        }
        return mvnList;
    }

    /**
     * Process <code>pom.properties</code> content to retrieve Maven information
     * from JAR.
     * <p/>
     * @param propStream Input stream to read <code>pom.properties</code>
     *                   file from JAR.
     */
    private static GlassFishLibrary.Maven getMvnInfoFromProperties(
            InputStream propStream) throws IOException {
        Properties props = new Properties();
        props.load(propStream);
        String groupId = props.getProperty(MVN_PROP_GROUP_ID);
        String artifactId = props.getProperty(MVN_PROP_ARTIFACT_ID);
        String version = props.getProperty(MVN_PROP_VERSION);
        if (groupId != null && artifactId != null && version != null) {
            return new GlassFishLibrary.Maven(groupId, artifactId, version);
        } else {
            return null;
        }
    }

    /**
     * Creates file name filter from <code>List</code>
     * of <cpode>Pattern</code>s.
     * <p/>
     * @param patterns <code>List</code> of <cpode>Pattern</code>s.
     * @return File name filter.
     */
    private static FilenameFilter createFilter(final List<Pattern> patterns) {
        return new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                for (Pattern p : patterns) {
                    if (p.matcher(name).matches()) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Compile pattern <code>String</code>s.
     * <p/>
     * @param names <code>List</code> of pattern <code>String</code>s.
     * @return <code>List</code> of compiled <code>Pattern</code>s.
     */
    private static List<Pattern> compilePatterns(List<String> names) {
        ArrayList<Pattern> patterns = new ArrayList<>(names.size());
        for (String name : names) {
            patterns.add(Pattern.compile(name));
        }
        return patterns;
    }

}
