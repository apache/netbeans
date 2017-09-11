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

package org.netbeans.modules.apisupport.project.universe;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import static org.netbeans.modules.apisupport.project.universe.Bundle.*;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * Represents one NetBeans platform, i.e. installation of the NB platform or IDE
 * or some derivative product.
 * Has a code id and can have associated sources and Javadoc, just like e.g. Java platforms.
 *
 * @author Jesse Glick
 */
public final class NbPlatform implements SourceRootsProvider, JavadocRootsProvider {
    
    private static final String PLATFORM_PREFIX = "nbplatform."; // NOI18N
    private static final String PLATFORM_DEST_DIR_SUFFIX = ".netbeans.dest.dir"; // NOI18N
    private static final String PLATFORM_LABEL_SUFFIX = ".label"; // NOI18N
    public static final String PLATFORM_SOURCES_SUFFIX = ".sources"; // NOI18N
    public static final String PLATFORM_JAVADOC_SUFFIX = ".javadoc"; // NOI18N
    private static final String PLATFORM_HARNESS_DIR_SUFFIX = ".harness.dir"; // NOI18N
    public static final String PLATFORM_ID_DEFAULT = "default"; // NOI18N
    private static final Logger LOG = Logger.getLogger(NbPlatform.class.getName());
    
    private static volatile Set<NbPlatform> platforms;
    
    private final PropertyChangeSupport pcs;
    private final SourceRootsSupport srs;
    private final JavadocRootsSupport jrs;

    private static volatile boolean inited;
    private static Map<String,String> initBuildProperties() {
        if (inited) {
            return null;
        }
        final File install = NbPlatform.defaultPlatformLocation();
        if (install == null) {
            inited = true;
            return null;
        }
        return ProjectManager.mutex().readAccess(new Mutex.Action<Map<String,String>>() {
            private EditableProperties loadWithProcessing() {
                EditableProperties p = PropertyUtils.getGlobalProperties();
                String installS = install.getAbsolutePath();
                p.setProperty("nbplatform.default.netbeans.dest.dir", installS); // NOI18N
                if (!p.containsKey("nbplatform.default.harness.dir")) { // NOI18N
                    p.setProperty("nbplatform.default.harness.dir", "${nbplatform.default.netbeans.dest.dir}/harness"); // NOI18N
                }
                return p;
            }
            public @Override Map<String,String> run() {
                ProjectManager.mutex().postWriteRequest(new Runnable() {
                    public @Override void run() {
                        if (inited) {
                            return;
                        }
                        try {
                            PropertyUtils.putGlobalProperties(loadWithProcessing());
                        } catch (IOException e) {
                            LOG.log(Level.INFO, null, e);
                        }
                        inited = true;
                    }
                });
                return PropertyUtils.sequentialPropertyEvaluator(null, PropertyUtils.fixedPropertyProvider(loadWithProcessing())).getProperties();
            }
        });
    }
    
    /**
     * Reset cached info so unit tests can start from scratch.
     * <p><b>Do not use outside of tests!</b> Concurrent call may cause
     * {@link #getPlatformsInternal()} to fail.</p>
     */
    public static void reset() {
        platforms = null;
    }
    
    /**
     * Get a set of all registered platforms.
     */
    public static Set<NbPlatform> getPlatforms() {
        Set<NbPlatform> plafs = getPlatformsInternal();
        synchronized (plafs) {
            return new HashSet<NbPlatform>(plafs);
        }
    }

    /**
     * Gets registered platforms, if these have already been computed for some other reason, else an empty set.
     */
    public static Set<NbPlatform> getPlatformsOrNot() {
        Set<NbPlatform> plafs;
        synchronized (lock) {
            plafs = platforms;
        }
        if (plafs != null) {
            synchronized (plafs) {
                return new HashSet<NbPlatform>(plafs);
            }
        } else {
            return Collections.emptySet();
        }
    }

    private static final Object lock = new Object();
    /**
     * Returns lazily initialized set of known platforms.
     * Returned set is synchronized, so you must synchronize on it when iterating, like this:
     * <pre>
     * Set&lt;NbPlatform&gt; plafs = getPlatformsInternal();
     * synchronized (plafs) {
     *   for (NbPlatform plaf : plafs) {
     *     // ...
     *   }
     * }</pre>
     * Note: do not pass returned set outside of NbPlatform class
     * @return
     */
    private static Set<NbPlatform> getPlatformsInternal() {
        Map<String,String> p = null;
        if (platforms == null) {
            // evaluator and prop. provider must be obtained outside of synchronized section,
            // as it acquires PM.mutex() read lock internally and can deadlock
            // when getPlatformsInternal() is called from PM.mutex() write lock;
            // see issue #173345
            p = initBuildProperties();
            if (p == null) {
                p = PropertyUtils.sequentialPropertyEvaluator(null, PropertyUtils.globalPropertyProvider()).getProperties();
            }
        }
        synchronized (lock) {
            if (platforms == null) {
                platforms = Collections.synchronizedSet(new HashSet<NbPlatform>());
                if (p == null) { // #115909
                    p = Collections.emptyMap();
                }
                boolean foundDefault = false;
                for (Map.Entry<String, String> entry : p.entrySet()) {
                    String key = entry.getKey();
                    if (key.startsWith(PLATFORM_PREFIX) && key.endsWith(PLATFORM_DEST_DIR_SUFFIX)) {
                        String id = key.substring(PLATFORM_PREFIX.length(), key.length() - PLATFORM_DEST_DIR_SUFFIX.length());
                        String label = p.get(PLATFORM_PREFIX + id + PLATFORM_LABEL_SUFFIX);
                        String destdir = entry.getValue();
                        String harnessdir = p.get(PLATFORM_PREFIX + id + PLATFORM_HARNESS_DIR_SUFFIX);
                        String sources = p.get(PLATFORM_PREFIX + id + PLATFORM_SOURCES_SUFFIX);
                        String javadoc = p.get(PLATFORM_PREFIX + id + PLATFORM_JAVADOC_SUFFIX);
                        File destdirF = FileUtil.normalizeFile(new File(destdir));
                        File harness;
                        if (harnessdir != null) {
                            harness = FileUtil.normalizeFile(new File(harnessdir));
                        } else {
                            harness = findHarness(destdirF);
                        }
                        platforms.add(new NbPlatform(id, label, destdirF, harness, ApisupportAntUtils.findURLs(sources), ApisupportAntUtils.findURLs(javadoc)));
                        foundDefault |= id.equals(PLATFORM_ID_DEFAULT);
                    }
                }
                if (!foundDefault) {
                    File loc = defaultPlatformLocation();
                    if (loc != null) {
                        platforms.add(new NbPlatform(PLATFORM_ID_DEFAULT, null, loc, findHarness(loc), new URL[0], new URL[0]));
                    }
                }
                LOG.log(Level.FINE, "NbPlatform initial list: {0}", platforms);
            }
        }
        return platforms;
    }
    
    /**
     * Get the default platform.
     * @return the default platform, if there is one (usually should be)
     */
    public static @CheckForNull NbPlatform getDefaultPlatform() {
        return NbPlatform.getPlatformByID(PLATFORM_ID_DEFAULT);
    }
    
    /**
     * Get the location of the default platform, or null.
     */
    public static File defaultPlatformLocation() {
        // XXX cache the result?
        // Semi-arbitrary platform* component.
        File bootJar = InstalledFileLocator.getDefault().locate("core/core.jar", "org.netbeans.core.startup", false); // NOI18N
        if (bootJar == null) {
            LOG.warning("no core/core.jar");
            return null;
        }
        File platformCluster = bootJar.getParentFile().getParentFile();
        if (!platformCluster.getName().startsWith("platform")) { // NOI18N
            LOG.log(Level.WARNING, "{0} found in unexpected cluster", bootJar);
            return null;
        }
        // Semi-arbitrary harness component.
        File harnessJar = InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-apisupport-harness.jar", "org.netbeans.modules.apisupport.harness", false); // NOI18N
        if (harnessJar == null) {
            LOG.warning("Cannot resolve default platform. Probably either \"org.netbeans.modules.apisupport.harness\" module is missing or is corrupted.");
            return null;
        }
        if (!harnessJar.getParentFile().getParentFile().getName().startsWith("harness")) { // NOI18N
            LOG.log(Level.WARNING, "{0} found in unexpected cluster", harnessJar);
            return null;
        }
        File loc = harnessJar.getParentFile().getParentFile().getParentFile();
        try {
            String netbeansHomeS = System.getProperty("netbeans.home"); // NOI18N
            if (netbeansHomeS != null) {
                File netbeansHome = new File(netbeansHomeS);
                if (!platformCluster.getCanonicalFile().equals(netbeansHome.getCanonicalFile())) {
                    LOG.log(Level.WARNING, "{0} does not match {1}", new Object[] {platformCluster, netbeansHome});
                    return null;
                }
            }
            if (!loc.getCanonicalFile().equals(platformCluster.getParentFile().getCanonicalFile())) {
                // Unusual installation structure, punt.
                LOG.log(Level.WARNING, "core.jar & harness.jar locations do not match: {0} vs. {1}", new Object[] {bootJar, harnessJar});
                return null;
            }
        } catch (IOException x) {
            LOG.log(Level.INFO, null, x);
        }
        // Looks good.
        return FileUtil.normalizeFile(loc);
    }
    
    /**
     * Find a platform by its ID.
     * @param id an ID (as in {@link #getID})
     * @return the platform with that ID, or null
     */
    public @CheckForNull static NbPlatform getPlatformByID(String id) {
        for (NbPlatform p : getPlatformsInternal()) {
            if (p.getID().equals(id)) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Find a platform by its installation directory.
     * If there is a registered platform for that directory, returns it.
     * Otherwise will create an anonymous platform ({@link #getID} will be null).
     * An anonymous platform might have sources associated with it;
     * currently this will be true in case the dest dir is nbbuild/netbeans/ inside a netbeans.org checkout.
     * @param the installation directory (as in {@link #getDestDir})
     * @param the harness dir if known; if null, will use the harness associated with the platform (if any)
     * @return the platform with that destination directory
     */
    public static @NonNull NbPlatform getPlatformByDestDir(@NonNull File destDir, @NullAllowed File harnessDir) {
        Set<NbPlatform> plafs = getPlatformsInternal();
        synchronized (plafs) {
            for (NbPlatform p : plafs) {
                // DEBUG only
//                int dif = p.getDestDir().compareTo(destDir);

                if (p.getDestDir().equals(destDir)) {
                    return p;
                }
            }
        }
        URL[] sources = new URL[0];
        if (destDir.getName().equals("netbeans")) { // NOI18N
            File parent = destDir.getParentFile();
            if (parent != null && parent.getName().equals("nbbuild")) { // NOI18N
                File superparent = parent.getParentFile();
                if (superparent != null && ModuleList.isNetBeansOrg(superparent)) {
                    sources = new URL[] {FileUtil.urlForArchiveOrDir(superparent)};
                }
            }
        }
        // XXX might also check OpenProjectList for NbModuleProject's and/or SuiteProject's with a matching
        // dest dir and look up property 'sources' to use; TBD whether Javadoc could also be handled in a
        // similar way
        return new NbPlatform(null, null, destDir, harnessDir != null ? harnessDir : findHarness(destDir), sources, new URL[0]);
    }
    
    /**
     * Find the location of the harness inside a platform.
     * Guaranteed to be a child directory (but might not exist yet).
     */
    private static File findHarness(File destDir) {
        File[] kids = destDir.listFiles();
        if (kids != null) {
            for (int i = 0; i < kids.length; i++) {
                if (isHarness(kids[i])) {
                    return kids[i];
                }
            }
        }
        return new File(destDir, "harness"); // NOI18N
    }
    
    /**
     * Check whether a given directory is really a valid harness.
     */
    public static boolean isHarness(File dir) {
        return new File(dir, "modules" + File.separatorChar + "org-netbeans-modules-apisupport-harness.jar").isFile(); // NOI18N
    }
    
    /**
     * Returns whether the platform within the given directory is already
     * registered.
     */
    public static boolean contains(File destDir) {
        boolean contains = false;
        Set<NbPlatform> plafs = getPlatformsInternal();
        synchronized (plafs) {
            for (NbPlatform p : plafs) {
                if (p.getDestDir().equals(destDir)) {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }
    
    /**
     * Register a new platform.
     * @param id unique ID string for the platform
     * @param destdir destination directory (i.e. top-level directory beneath which there are clusters)
     * @param label display label
     * @return the created platform
     * @throws IOException in case of problems (e.g. destination directory does not exist)
     */
    public static NbPlatform addPlatform(final String id, final File destdir, final String label) throws IOException {
        return addPlatform(id, destdir, findHarness(destdir), label);
    }
    
    /**
     * Register a new platform.
     * @param id unique ID string for the platform
     * @param destdir destination directory (i.e. top-level directory beneath which there are clusters)
     * @param harness harness directory
     * @param label display label
     * @return the created platform
     * @throws IOException in case of problems (e.g. destination directory does not exist)
     */
    public static NbPlatform addPlatform(final String id, final File destdir, final File harness, final String label) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override public Void run() throws IOException {
                    if (getPlatformByID(id) != null) {
                        throw new IOException("ID " + id + " already taken");
                    }
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    String plafDestDir = PLATFORM_PREFIX + id + PLATFORM_DEST_DIR_SUFFIX;
                    props.setProperty(plafDestDir, destdir.getAbsolutePath());
                    if (!destdir.isDirectory()) {
                        throw new FileNotFoundException(destdir.getAbsolutePath());
                    }
                    storeHarnessLocation(id, destdir, harness, props);
                    props.setProperty(PLATFORM_PREFIX + id + PLATFORM_LABEL_SUFFIX, label);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        NbPlatform plaf = new NbPlatform(id, label, FileUtil.normalizeFile(destdir), harness,
                ApisupportAntUtils.findURLs(null), ApisupportAntUtils.findURLs(null));
        getPlatformsInternal().add(plaf);
        LOG.log(Level.FINE, "NbPlatform added: {0}", plaf);
        return plaf;
    }
    
    private static void storeHarnessLocation(String id, File destdir, File harness, EditableProperties props) {
        String harnessDirKey = PLATFORM_PREFIX + id + PLATFORM_HARNESS_DIR_SUFFIX;
        if (harness.equals(findHarness(destdir))) {
            // Common case.
            String plafDestDir = PLATFORM_PREFIX + id + PLATFORM_DEST_DIR_SUFFIX;
            props.setProperty(harnessDirKey, "${" + plafDestDir + "}/" + harness.getName()); // NOI18N
        } else {
            NbPlatform plaf = getDefaultPlatform();
            if (plaf != null && harness.equals(plaf.getHarnessLocation())) {
                // Also common.
                props.setProperty(harnessDirKey, "${" + PLATFORM_PREFIX + PLATFORM_ID_DEFAULT + PLATFORM_HARNESS_DIR_SUFFIX + "}"); // NOI18N
            } else {
                // Some random location.
                props.setProperty(harnessDirKey, harness.getAbsolutePath());
            }
        }
    }
    
    public static void removePlatform(final NbPlatform plaf) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override public Void run() throws IOException {
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_DEST_DIR_SUFFIX);
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_HARNESS_DIR_SUFFIX);
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_LABEL_SUFFIX);
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_SOURCES_SUFFIX);
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_JAVADOC_SUFFIX);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        getPlatformsInternal().remove(plaf);
        ModuleList.refresh(); // #97262
        LOG.log(Level.FINE, "NbPlatform removed: {0}", plaf);
    }
    
    private final String id;
    private String label;
    private File nbdestdir;
    private File harness;
    private HarnessVersion harnessVersion;
    
    private NbPlatform(String id, String label, File nbdestdir, File harness, URL[] sources, URL[] javadoc) {
        this.id = id;
        this.label = label;
        this.nbdestdir = nbdestdir;
        this.harness = harness;
        pcs = new PropertyChangeSupport(this);
        srs = new SourceRootsSupport(sources, this);
        srs.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                // re-fire
                pcs.firePropertyChange(evt);
            }
        });
        jrs = new JavadocRootsSupport(javadoc, this);
        jrs.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                // re-fire
                pcs.firePropertyChange(evt);
            }
        });
    }
    
    /**
     * Get a unique ID for this platform.
     * Used e.g. in <code>nbplatform.active</code> in <code>platform.properties</code>.
     * @return a unique ID, or <code>null</code> for <em>anonymous</em>
     *         platforms (see {@link #getPlatformByDestDir}).
     */
    public String getID() {
        return id;
    }

    /**
     * Check if this is the default platform.
     * @return true for the one default platform
     */
    public boolean isDefault() {
        return PLATFORM_ID_DEFAULT.equals(id);
    }
    
    /**
     * Get a display label suitable for the user.
     * If not set, {@link #getComputedLabel} is used.
     * The {@link #isDefault default platform} is specially marked.
     * @return a display label
     */
    @Messages("LBL_default_plaf=Development IDE")
    public String getLabel() {
        if (isDefault()) {
            return LBL_default_plaf();
        }
        if (label == null) {
            label = getComputedLabel(nbdestdir);
        }
        return label;
    }

    /**
     * Finds label based on intrinsic metadata in the platform, regardless of user configuration.
     * {@link #computeDisplayName} is used where possible.
     * @see #getLabel
     */
    @Messages({"# {0} - folder location", "MSG_InvalidPlatform=Invalid Platform ({0})"})
    public static String getComputedLabel(File destdir) {
        try {
            return isPlatformDirectory(destdir) ? computeDisplayName(destdir) : MSG_InvalidPlatform(destdir);
        } catch (IOException e) {
            Logger.getLogger(NbPlatform.class.getName()).log(Level.FINE, "could not get label for " + destdir, e);
            return destdir.getAbsolutePath();
        }
    }
    
    /**
     * Get the installation directory.
     * @return the installation directory
     */
    public File getDestDir() {
        return nbdestdir;
    }
    
    public void setDestDir(File destdir) {
        this.nbdestdir = destdir;
        // XXX write build.properties too
    }

    /**
     * Gets Javadoc which should by default be associated with a platform.
     */
    public @Override URL[] getDefaultJavadocRoots() {
        if (isDefault()) {
            File apidocsZip = InstalledFileLocator.getDefault().locate("docs/NetBeansAPIs.zip", "org.netbeans.modules.apisupport.apidocs", true); // NOI18N
            if (apidocsZip != null) {
                return new URL[] {FileUtil.urlForArchiveOrDir(apidocsZip)};
            }
        }
        // Use a representative module present in all 6.x versions.
        ModuleEntry platform = getModule("org.netbeans.modules.core.kit"); // NOI18N
        if (platform != null) {
            String spec = platform.getSpecificationVersion();
            if (spec != null) {
                Matcher m = Pattern.compile("(\\d+[.]\\d+)([.]\\d+)*").matcher(spec);
                if (m.matches()) {
                    String trunkSpec = m.group(1);
                    try {
                        String loc = NbBundle.getMessage(NbPlatform.class, "NbPlatform.web.javadoc." + trunkSpec);
                        return new URL[] {new URL(loc)};
                    } catch (MissingResourceException x) {
                        // fine, some other trunk version, ignore
                    } catch (MalformedURLException x) {
                        assert false : x;
                    }
                }
            }
        }
        return null;
    }

    @Override public void addJavadocRoot(URL root) throws IOException {
        jrs.addJavadocRoot(root);
    }

    @Override public URL[] getJavadocRoots() {
        return jrs.getJavadocRoots();
    }

    @Override public void moveJavadocRootDown(int indexToDown) throws IOException {
        jrs.moveJavadocRootDown(indexToDown);
    }

    @Override public void moveJavadocRootUp(int indexToUp) throws IOException {
        jrs.moveJavadocRootUp(indexToUp);
    }

    @Override public void removeJavadocRoots(URL[] urlsToRemove) throws IOException {
        jrs.removeJavadocRoots(urlsToRemove);
    }

    @Override public void setJavadocRoots(URL[] roots) throws IOException {
        putGlobalProperty(
                PLATFORM_PREFIX + getID() + PLATFORM_JAVADOC_SUFFIX,
                ApisupportAntUtils.urlsToAntPath(roots));
        jrs.setJavadocRoots(roots);
    }

    private URL[] defaultSourceRoots;

    /**
     * Get any sources which should by default be associated with the default platform.
     */
    public @Override URL[] getDefaultSourceRoots() {
        if (! isDefault()) {
            return null;
        }
        // location of platform won't change, safe to cache
        if (defaultSourceRoots != null) {
            return defaultSourceRoots.clone();
        }
        defaultSourceRoots = new URL[0];
        File loc = getDestDir();
        if (loc.getName().equals("netbeans") && loc.getParentFile().getName().equals("nbbuild")) { // NOI18N
            try {
                defaultSourceRoots = new URL[] {Utilities.toURI(loc.getParentFile().getParentFile()).toURL()};
            } catch (MalformedURLException e) {
                assert false : e;
            }
        }
        return defaultSourceRoots.clone();
    }

    /**
     * Get associated source roots for this platform.
     * Each root could be a netbeans.org source checkout or a module suite project directory.
     * @return a list of source root URLs (may be empty but not null)
     */
    @Override public URL[] getSourceRoots() {
        return srs.getSourceRoots();
    }
    
    /**
     * Add given source root to the current source root list and save the
     * result into the global properties in the <em>userdir</em> (see {@link
     * PropertyUtils#putGlobalProperties})
     */
    @Override public void addSourceRoot(URL root) throws IOException {
        srs.addSourceRoot(root);
    }
    
    /**
     * Remove given source roots from the current source root list and save the
     * result into the global properties in the <em>userdir</em> (see {@link
     * PropertyUtils#putGlobalProperties})
     */
    @Override public void removeSourceRoots(URL[] urlsToRemove) throws IOException {
        srs.removeSourceRoots(urlsToRemove);
    }
    
    @Override public void moveSourceRootUp(int indexToUp) throws IOException {
        srs.moveSourceRootUp(indexToUp);
    }
    
    @Override public void moveSourceRootDown(int indexToDown) throws IOException {
        srs.moveSourceRootDown(indexToDown);
    }
    
    @Override public void setSourceRoots(URL[] roots) throws IOException {
        putGlobalProperty(
                PLATFORM_PREFIX + getID() + PLATFORM_SOURCES_SUFFIX,
                ApisupportAntUtils.urlsToAntPath(roots));
        srs.setSourceRoots(roots);
    }
    
    /**
     * Test whether this platform is valid or not. See
     * {@link #isPlatformDirectory}
     */
    public boolean isValid() {
        return NbPlatform.isPlatformDirectory(getDestDir());
    }
    
    private void putGlobalProperty(final String key, final String value) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override public Void run() throws IOException {
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    if ("".equals(value)) { // NOI18N
                        props.remove(key);
                    } else {
                        props.setProperty(key, value);
                    }
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /**
     * Find sources for a module JAR file contained in this destination directory.
     * @param jar a JAR file in the destination directory
     * @return the directory of sources for this module (a project directory), or null
     */
    @Override public File getSourceLocationOfModule(File jar) {
        return srs.getSourceLocationOfModule(jar);
    }
    
    /**
     * Returns (naturally sorted) array of all module entries pertaining to
     * <code>this</code> NetBeans platform. This is just a convenient delegate
     * to the {@link ModuleList#findOrCreateModuleListFromBinaries}.
     *
     * This may be a time-consuming method, consider using much faster
     * ModuleList#getModules instead, which doesn't sort the modules. Do not call
     * from AWT thread (not checked so that it may be used in tests).
     */
    public ModuleEntry[] getSortedModules() {
        SortedSet<ModuleEntry> set = new TreeSet<ModuleEntry>(getModules());
        ModuleEntry[] entries = new ModuleEntry[set.size()];
        set.toArray(entries);
        return entries;
    }

    /**
     * Returns a set of all module entries pertaining to
     * <code>this</code> NetBeans platform. This is just a convenient delegate
     * to the {@link ModuleList#findOrCreateModuleListFromBinaries}.
     */
    public Set<ModuleEntry> getModules() {
        if (nbdestdir.isDirectory()) {
            try {
                return getModuleList().getAllEntries();
            } catch (IOException x) {
                LOG.log(Level.INFO, null, x);
            }
        } else {
            LOG.log(Level.WARNING, "Platform directory {0} does not exist", nbdestdir);
        }
        return Collections.emptySet();
    }

    /**
     * Gets a module from the platform by name.
     */
    public @CheckForNull ModuleEntry getModule(String cnb) {
        if (nbdestdir.isDirectory()) {
            try {
                return getModuleList().getEntry(cnb);
            } catch (IOException x) {
                LOG.log(Level.INFO, null, x);
            }
        } else {
            LOG.log(Level.WARNING, "Platform directory {0} does not exist", nbdestdir);
        }
        return null;
    }
    
    private ModuleList getModuleList() throws IOException {
        if (nbdestdir.getName().equals("netbeans")) { // #206805
            File nbbuild = nbdestdir.getParentFile();
            if (nbbuild != null && nbbuild.getName().equals("nbbuild")) {
                File root = nbbuild.getParentFile();
                if (root != null) {
                    LOG.log(Level.FINE, "creating module list for nb.org: {0}", root);
                    return ModuleList.findOrCreateModuleListFromNetBeansOrgSources(root);
                }
            }
        }
        LOG.log(Level.FINE, "creating binary module list: {0}", nbdestdir);
        return ModuleList.findOrCreateModuleListFromBinaries(nbdestdir);
    }

    private static File findCoreJar(File destdir) {
        File[] subdirs = destdir.listFiles();
        if (subdirs != null) {
            for (int i = 0; i < subdirs.length; i++) {
                if (!subdirs[i].isDirectory()) {
                    continue;
                }
                if (!subdirs[i].getName().matches("platform\\d*")) { // NOI18N
                    continue;
                }
                File coreJar = new File(subdirs[i], "core" + File.separatorChar + "core.jar"); // NOI18N
                if (coreJar.isFile()) {
                    return coreJar;
                }
            }
        }
        return null;
    }
    
    /**
     * Check whether a given directory on disk is a valid destdir as per {@link #getDestDir}.
     * @param destdir a candidate directory
     * @return true if it can be used as a platform
     */
    public static boolean isPlatformDirectory(File destdir) {
        return findCoreJar(destdir) != null;
    }
    
    public static boolean isSupportedPlatform(File destdir) {
        boolean valid = false;
        File coreJar = findCoreJar(destdir);
        if (coreJar != null) {
            String platformDir = coreJar.getParentFile().getParentFile().getName();
            assert platformDir.startsWith("platform"); // NOI18N
            String version = platformDir.substring("platform".length());
            valid = /* NB 6.9+ */version.isEmpty() || Integer.parseInt(version) >= 6;
        }
        return valid;
    }
    
    /**
     * Find a display name for a NetBeans platform on disk.
     * @param destdir a dir passing {@link #isPlatformDirectory}
     * @return a display name
     * @throws IllegalArgumentException if {@link #isPlatformDirectory} was false
     * @throws IOException if its labelling info could not be read
     */
    public static String computeDisplayName(File destdir) throws IOException {
        File coreJar = findCoreJar(destdir);
        if (coreJar == null) {
            throw new IllegalArgumentException(destdir.getAbsolutePath());
        }
        String currVer, implVers;
        JarFile jf = new JarFile(coreJar);
        try {
            currVer = findCurrVer(jf, "");
            if (currVer == null) {
                throw new IOException(coreJar.getAbsolutePath());
            }
            implVers = jf.getManifest().getMainAttributes().getValue("OpenIDE-Module-Build-Version"); // NOI18N
            if (implVers == null) {
                implVers = jf.getManifest().getMainAttributes().getValue("OpenIDE-Module-Implementation-Version"); // NOI18N
            }
            if (implVers == null) {
                throw new IOException(coreJar.getAbsolutePath());
            }
        } finally {
            jf.close();
        }
        // Also check in localizing bundles for 'currentVersion', since it may be branded.
        // We do not know what the runtime branding will be, so look for anything.
        File[] clusters = destdir.listFiles();
        BRANDED_CURR_VER: if (clusters != null) {
            for (int i = 0; i < clusters.length; i++) {
                File coreLocaleDir = new File(clusters[i], "core" + File.separatorChar + "locale"); // NOI18N
                if (!coreLocaleDir.isDirectory()) {
                    continue;
                }
                String[] kids = coreLocaleDir.list();
                if (kids != null) {
                    for (int j = 0; j < kids.length; j++) {
                        String name = kids[j];
                        String prefix = "core"; // NOI18N
                        String suffix = ".jar"; // NOI18N
                        if (!name.startsWith(prefix) || !name.endsWith(suffix)) {
                            continue;
                        }
                        String infix = name.substring(prefix.length(), name.length() - suffix.length());
                        int uscore = infix.lastIndexOf('_');
                        if (uscore == -1) {
                            // Malformed.
                            continue;
                        }
                        String lastPiece = infix.substring(uscore + 1);
                        if (Arrays.asList(Locale.getISOCountries()).contains(lastPiece) ||
                                (!lastPiece.equals("nb") && Arrays.asList(Locale.getISOLanguages()).contains(lastPiece))) { // NOI18N
                            // Probably a localization, not a branding... so skip it. (We want to show English only.)
                            // But hardcode support for branding 'nb' since this is also Norwegian Bokmal, apparently!
                            // XXX should this try to use Locale.getDefault() localization if possible?
                            continue;
                        }
                        jf = new JarFile(new File(coreLocaleDir, name));
                        try {
                            String brandedCurrVer = findCurrVer(jf, infix);
                            if (brandedCurrVer != null) {
                                currVer = brandedCurrVer;
                                break BRANDED_CURR_VER;
                            }
                        } finally {
                            jf.close();
                        }
                    }
                }
            }
        }
        return MessageFormat.format(currVer, new Object[] {implVers});
    }
    private static String findCurrVer(JarFile jar, String infix) throws IOException {
        // first try to find the Bundle for 5.0+ (after openide split)
        ZipEntry bundle = jar.getEntry("org/netbeans/core/startup/Bundle" + infix + ".properties"); // NOI18N
        if (bundle == null) {
            // might be <5.0 (before openide split)
            bundle = jar.getEntry("org/netbeans/core/Bundle" + infix + ".properties"); // NOI18N
        }
        if (bundle == null) {
            return null;
        }
        Properties props = new Properties();
        InputStream is = jar.getInputStream(bundle);
        try {
            props.load(is);
        } finally {
            is.close();
        }
        return props.getProperty("currentVersion"); // NOI18N
    }
    
    /**
     * Returns whether the given label (see {@link #getLabel}) is valid.
     * <em>Valid</em> label must be non-null and must not be used by any
     * already defined platform.
     */
    public static boolean isLabelValid(String supposedLabel) {
        if (supposedLabel == null) {
            return false;
        }
        for (NbPlatform p : NbPlatform.getPlatforms()) {
            String label = p.getLabel();
            if (supposedLabel.equals(label)) {
                return false;
            }
        }
        return true;
    }
    
    public @Override String toString() {
        return "NbPlatform[" + getID() + ":" + getDestDir() + "]";
    }
    
    /**
     * Get the version of this platform's harness.
     */
    public HarnessVersion getHarnessVersion() {
        if (harnessVersion != null) {
            return harnessVersion;
        }
        if (!isValid()) {
            return harnessVersion = HarnessVersion.UNKNOWN;
        }
        File harnessJar = new File(harness, "modules" + File.separatorChar + "org-netbeans-modules-apisupport-harness.jar"); // NOI18N
        if (harnessJar.isFile()) {
            try {
                JarFile jf = new JarFile(harnessJar);
                try {
                    String spec = jf.getManifest().getMainAttributes().getValue(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION);
                    if (spec != null) {
                        SpecificationVersion v = new SpecificationVersion(spec);
                        return harnessVersion = HarnessVersion.forHarnessModuleVersion(v);
                    }
                } finally {
                    jf.close();
                }
            } catch (IOException e) {
                LOG.log(Level.INFO, null, e);
            } catch (NumberFormatException e) {
                LOG.log(Level.INFO, null, e);
            }
        }
        return harnessVersion = HarnessVersion.UNKNOWN;
    }
    
    /**
     * Get the current location of this platform's harness
     */
    public File getHarnessLocation() {
        return harness;
    }
    
    /**
     * Get the location of the harness bundled with this platform.
     */
    public File getBundledHarnessLocation() {
        return findHarness(nbdestdir);
    }
    
    /**
     * Set a new location for this platform's harness.
     */
    public void setHarnessLocation(final File harness) throws IOException {
        if (harness.equals(this.harness)) {
            return;
        }
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override public Void run() throws IOException {
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    storeHarnessLocation(id, nbdestdir, harness, props);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        this.harness = harness;
        harnessVersion = null;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
}
