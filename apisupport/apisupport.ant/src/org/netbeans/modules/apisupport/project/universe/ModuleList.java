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

package org.netbeans.modules.apisupport.project.universe;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleType;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.ui.customizer.ClusterInfo;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.netbeans.modules.apisupport.project.universe.Bundle.*;

/**
 * Represents list of known modules.
 * @author Jesse Glick
 */
public final class ModuleList {

    public static final String NETBEANS_DEST_DIR = "netbeans.dest.dir";

    private static final Logger LOG = Logger.getLogger(ModuleList.class.getName());

    /** for performance measurement from ModuleListTest */
    static long timeSpentInXmlParsing;
    static int xmlFilesParsed;
    static int directoriesChecked;
    static int jarsOpened;

    /** Synch with org.netbeans.nbbuild.ModuleListParser.FOREST: */
    private final String[] FOREST;

    /**
     * Cache of source-derived lists, by source root.
     */
    private static final Map<File,ModuleList> sourceLists = new HashMap<File,ModuleList>();
    /**
     * Cache of binary-derived lists, by cluster (was by binary root (~ dest dir)).
     * Stores not only NB clusters (mapped by <tt>clusterLists</tt>) but also external clusters
     * stored in cluster.path property of projects.
     * @see <tt>clusterLists</tt>.
     */
    private static final Map<File,ModuleList> binaryLists = new HashMap<File,ModuleList>();
    /**
     * Cache of clusters, by nb binary root.
     */
    private static final Map<File, File[]> clusterLists = new HashMap<File,File[]>();
    /**
     * Map from netbeans.org source roots to cluster.properties loads.
     */
    private static final Map<File,Map<String,String>> clusterPropertiesFiles = new HashMap<File,Map<String,String>>();
    /**
     * Map from netbeans.org source roots, to cluster definitions,
     * where a cluster definition is from netbeans.org relative source path
     * to physical cluster directory.
     * Source path is relative to netbeans.org root, cluster dir is relative to
     * <tt>nbbuild/netbeans</tt>, i.e. it only contains cluster name.
     */
    private static final Map<File,Map<String,String>> clusterLocations = new HashMap<File,Map<String,String>>();
    
    /** All entries known to exist for a given included file path. */
    private static final Map<File,Set<ModuleEntry>> knownEntries = new HashMap<File,Set<ModuleEntry>>();

    /**
     * Find the list of modules associated with a project (itself, others in
     * its suite, others in its platform, or others in netbeans.org). <p>Do not
     * cache the result; always call this method fresh, in case {@link
     * #refresh} has been called. This method actually call {@link
     * #getModuleList(File, File)} with the <code>null</code> for the
     * <code>customNbDestDir</code> parameter.
     *
     * @param basedir the project directory to start in
     * @return a module list
     */
    public static ModuleList getModuleList(File basedir) throws IOException {
        return getModuleList(basedir, null);
    }

    private static File getClusterPropertiesFile(File nbroot) {
        return new File(nbroot, "nbbuild" + File.separatorChar + "cluster.properties");
    }

    /**
     * Runs given action both in Project read lock and synchronized on given cache
     * @param protectedCache Synchronized cache list, e.g. sourceLists, binaryLists, etc.
     * @param action Action to run
     * @return created or cached module list
     * @throws java.io.IOException
     */
    private static ModuleList runProtected(final Object protectedCache, final Mutex.ExceptionAction<ModuleList> action) throws IOException {
        try {
            LOG.log(Level.FINER, "runProtected: sync 0");
            return ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<ModuleList>() {
                public ModuleList run() throws Exception {
                    LOG.log(Level.FINER, "runProtected: sync 1");
                    synchronized (protectedCache) {
                        return action.run();
                    }
                }
            });
        } catch (MutexException e){
            throw (IOException) e.getException();
        }
    }
    /**
     * The same as {@link #getModuleList(File)}, but giving chance to specify a
     * custom NetBeans platform.
     *
     * @param basedir the project directory to start in
     * @param customNbDestDir custom NetBeans platform directory to be used for
     *        searching NB module instead of using the currently set one in a
     *        module's properties. If <code>null</code> is passed the
     *        default(active) platform from module's properties will be used
     * @return a module list
     */
    public static ModuleList getModuleList(final File basedir, final File customNbDestDir) throws IOException {
        return runProtected(binaryLists, new Mutex.ExceptionAction<ModuleList>() { // #69971
                public ModuleList run() throws IOException {
        timeSpentInXmlParsing = 0L;
        xmlFilesParsed = 0;
        directoriesChecked = 0;
        jarsOpened = 0;
        Element data = parseData(basedir);
        if (data == null) {
            throw new IOException("Not an NBM project in " + basedir); // NOI18N
        }
        boolean suiteComponent = XMLUtil.findElement(data, "suite-component", NbModuleProject.NAMESPACE_SHARED) != null; // NOI18N
        boolean standalone = XMLUtil.findElement(data, "standalone", NbModuleProject.NAMESPACE_SHARED) != null; // NOI18N
        assert !(suiteComponent && standalone) : basedir;
        if (suiteComponent) {
            PropertyEvaluator eval = parseProperties(basedir, null, NbModuleType.SUITE_COMPONENT, "irrelevant"); // NOI18N
            String suiteS = eval.getProperty("suite.dir");
            if (suiteS == null) {
                throw new IOException("No suite.dir defined from " + basedir); // NOI18N
            }
            File suite = PropertyUtils.resolveFile(basedir, suiteS);
            return findOrCreateModuleListFromSuite(suite, customNbDestDir);
        } else if (standalone) {
            return findOrCreateModuleListFromStandaloneModule(basedir, customNbDestDir);
        } else {
            // netbeans.org module.
            File nbroot = findNetBeansOrg(basedir);
            if (nbroot == null) {
                throw new IOException("Could not find netbeans.org source root from " + basedir + "; note that 3rd-level modules (a/b/c) are permitted at the maximum"); // NOI18N
            }
            return findOrCreateModuleListFromNetBeansOrgSources(nbroot);
        }
                    }
                });
    }
    
    /**
     * Check to see if there are <em>any</em> known module list entries.
     * @return false if {@link #getKnownEntries} cannot return a nonempty set at the moment, true if it might
     */
    public static boolean existKnownEntries() {
        synchronized (knownEntries) {
            return !knownEntries.isEmpty();
        }
    }
    
    /**
     * Find the known module entries which build to a given built file path (e.g. module JAR).
     * Applies to any entries which have been scanned.
     * @param file some file built as part of the module
     * @return a set of entries thought to build to this file (may be empty but not null)
     */
    public static Set<ModuleEntry> getKnownEntries(File file) {
        synchronized (knownEntries) {
            Set<ModuleEntry> entries = knownEntries.get(file);
            if (entries != null) {
                return Collections.unmodifiableSet(entries);
            } else {
                return Collections.emptySet();
            }
        }
    }

    public static URL[] getSourceRootsForExternalModule(File binaryRootF) {
        Set<ModuleEntry> candidates = getKnownEntries(binaryRootF);
        List<URL> roots = new ArrayList<URL>();

        for (ModuleEntry entry : candidates) {
            if (entry instanceof BinaryClusterEntry) {
                BinaryClusterEntry bce = (BinaryClusterEntry) entry;
                roots.addAll(Arrays.asList(bce.getSourceRoots()));
            }
        }
        return roots.toArray(new URL[0]);
    }

    public static URL[] getJavadocRootsForExternalModule(File binaryRootF) {
        Set<ModuleEntry> candidates = getKnownEntries(binaryRootF);
        List<URL> roots = new ArrayList<URL>();

        for (ModuleEntry entry : candidates) {
            if (entry instanceof BinaryClusterEntry) {
                BinaryClusterEntry bce = (BinaryClusterEntry) entry;
                roots.addAll(Arrays.asList(bce.getJavadocRoots()));
            }
        }
        return roots.toArray(new URL[0]);
    }

    private static void registerEntry(ModuleEntry entry, Set<File> files) {
        synchronized (knownEntries) {
            for (File f : files) {
                Set<ModuleEntry> entries = knownEntries.get(f);
                if (entries == null) {
                    entries = new HashSet<ModuleEntry>();
                    knownEntries.put(f, entries);
                }
                entries.add(entry);
            }
        }
    }
    
    static ModuleList findOrCreateModuleListFromNetBeansOrgSources(final File root) throws IOException {
        return runProtected(sourceLists, new Mutex.ExceptionAction<ModuleList>() {
            public ModuleList run() throws IOException {
                ModuleList list = sourceLists.get(root);
                if (list == null) {
                    File nbdestdir = findNetBeansOrgDestDir(root);
                    if (nbdestdir.equals(new File(new File(root, "nbbuild"), "netbeans"))) { // NOI18N
                        list = createModuleListFromNetBeansOrgSources(root);
                    } else {
                        // #143236: have a customized dest dir, perhaps referenced from orphan modules.
                        Map<String, ModuleEntry> entries = new HashMap<String, ModuleEntry>();
                        doScanNetBeansOrgSources(entries, root, 1, root, nbdestdir, null, Collections.<File>emptySet());
                        ModuleList sources = new ModuleList(entries, root);
                        ModuleList binaries = findOrCreateModuleListFromBinaries(nbdestdir);
                        list = merge(new ModuleList[] {sources, binaries}, root);
                    }
                    sourceLists.put(root, list);
                }
                return list;
            }
        });
    }

    /**
     * Gets the platform build directory associated with a netbeans.org source root.
     * Normally nbbuild/netbeans/ but can be overridden.
     * @param nb_all the (possibly partial) netbeans.org source root
     */
    public static File findNetBeansOrgDestDir(File nb_all) {
        synchronized (netbeansOrgDestDirs) {
            File d = netbeansOrgDestDirs.get(nb_all);
            if (d == null) {
                File nbbuild = new File(nb_all, "nbbuild"); // NOI18N
                d = checkForNetBeansOrgDestDir(new File(nbbuild, "user.build.properties")); // NOI18N
                if (d == null) {
                    d = checkForNetBeansOrgDestDir(new File(nbbuild, "site.build.properties")); // NOI18N
                    if (d == null) {
                        d = checkForNetBeansOrgDestDir(new File(System.getProperty("user.home"), ".nbbuild.properties")); // NOI18N
                        if (d == null) {
                            d = new File(nbbuild, "netbeans"); // NOI18N
                        }
                    }
                }
                netbeansOrgDestDirs.put(nb_all, d);
            }
            return d;
        }
    }
    private static final Map<File,File> netbeansOrgDestDirs = new HashMap<File,File>();
    private static File checkForNetBeansOrgDestDir(File properties) {
        if (properties.isFile()) {
            try {
                InputStream is = new FileInputStream(properties);
                try {
                    Properties p = new Properties();
                    p.load(is);
                    String d = p.getProperty(NETBEANS_DEST_DIR);
                    if (d != null) {
                        return new File(d);
                    }
                } finally {
                    is.close();
                }
            } catch (IOException x) {
                LOG.log(Level.INFO, "Could not read " + properties, x);
            }
        }
        return null;
    }

    private static ModuleList createModuleListFromNetBeansOrgSources(File root) throws IOException {
        LOG.log(Level.FINE, "ModuleList.createModuleListFromSources: " + root);
        Map<String,ModuleEntry> entries = new HashMap<String,ModuleEntry>();
        return new ModuleList(entries, root);
    }

    private static void scanJars(
        File dir, ClusterInfo ci, @NullAllowed File nbDestDir, File cluster,
        Map<String, ModuleEntry> entries, boolean registerEntry,
        File... jars
    ) throws IOException {
        for (File m : jars) {
            if (!m.getName().endsWith(".jar")) {
                continue;
            }
            jarsOpened++;
            ManifestManager mm = ManifestManager.getInstanceFromJAR(m);
            String codenamebase = mm.getCodeNameBase();
            if (codenamebase == null) {
                continue;
            }
            String cp = mm.getClassPath();
            File[] exts;
            if (cp == null) {
                exts = new File[0];
            } else {
                String[] pieces = cp.trim().split(" +");
                exts = new File[pieces.length];
                for (int l = 0; l < pieces.length; l++) {
                    exts[l] = FileUtil.normalizeFile(new File(dir, pieces[l].replace('/', File.separatorChar)));
                }
            }
            ModuleEntry entry = (ci == null || ci.isPlatformCluster()) ? new BinaryEntry(codenamebase, m, exts, nbDestDir != null ? nbDestDir : cluster, cluster, mm.getReleaseVersion(), mm.getSpecificationVersion(), mm.getProvidedTokens(), mm.getPublicPackages(), mm.getFriends(), mm.isDeprecated(), mm.getModuleDependencies()) : new BinaryClusterEntry(codenamebase, m, exts, cluster, mm.getReleaseVersion(), mm.getSpecificationVersion(), mm.getProvidedTokens(), mm.getPublicPackages(), mm.getFriends(), mm.isDeprecated(), mm.getModuleDependencies(), ci.getSourceRoots(), ci.getJavadocRoots());
            ModuleEntry prev = entries.get(codenamebase);
            if (prev != null) {
                LOG.log(Level.WARNING, "Warning: two modules found with the same code name base (" + codenamebase + "): " + entries.get(codenamebase) + " and " + entry);
            } else {
                entries.put(codenamebase, entry);
            }
            if (registerEntry) {
                registerEntry(entry, findBinaryNBMFiles(cluster, codenamebase, m));
            }
        }
    }

    /**
     * Look just for stable modules in netbeans.org, assuming that this is most commonly what is wanted.
     * @see "#62221"
     */
    private void scanNetBeansOrgStableSources() throws IOException {
        if (lazyNetBeansOrgList >= 1) {
            return;
        }
        File nbdestdir = findNetBeansOrg(home);
        LOG.log(Level.INFO, "full scan of {0}", home);
        Map<String,String> clusterProps = getClusterProperties(home);
        // Use ${clusters.list}, *not* ${nb.clusters.list}: we do want to include testtools,
        // since those modules contribute sources for JARs which are used in unit test classpaths for stable modules.
        String clusterList = clusterProps.get("clusters.list"); // NOI18N
        if (clusterList == null) {
                String config = clusterProps.get("cluster.config"); // NOI18N
                if (config != null) {
                    clusterList = clusterProps.get("clusters.config." + config + ".list"); // NOI18N
                }
            }
        if (clusterList == null) {
            throw new IOException("Neither ${clusters.list} nor ${cluster.config} + ${clusters.config.<cfg>.list} found in "    // NOI18N
                    + getClusterPropertiesFile(home));
        }
        Set<File> knownProjects = new HashSet<File>();
        for (ModuleEntry known : entries.values()) {
            knownProjects.add(known.getSourceLocation());
        }
        Map<String,ModuleEntry> _entries = new HashMap<String,ModuleEntry>(entries);
        StringTokenizer tok = new StringTokenizer(clusterList, ", "); // NOI18N
        while (tok.hasMoreTokens()) {
            String clusterName = tok.nextToken();
            String moduleList = clusterProps.get(clusterName);
            if (moduleList == null) {
                throw new IOException("No ${" + clusterName + "} found in " + home); // NOI18N
            }
            final String clusterDir = clusterProps.get(clusterName + ".dir");
            StringTokenizer tok2 = new StringTokenizer(moduleList, ", "); // NOI18N
            while (tok2.hasMoreTokens()) {
                final String module = clusterDir + "/" + tok2.nextToken(); //NETBEANS-3330
                File basedir = new File(home, module.replace('/', File.separatorChar));
                if (!knownProjects.contains(basedir)) { // we may already have scanned some
                    scanPossibleProject(basedir, _entries, NbModuleType.NETBEANS_ORG, home, nbdestdir, module);
                }
            }
        }
        entries = _entries;
        LOG.log(Level.FINER, "scanning NetBeans.org stable sources finished");
        lazyNetBeansOrgList = 1;
    }
    
    /** Only useful for pre-Hg layout. */
    public static final Set<String> EXCLUDED_DIR_NAMES = new HashSet<String>();
    static {
        EXCLUDED_DIR_NAMES.add("CVS"); // NOI18N
        EXCLUDED_DIR_NAMES.add("nbproject"); // NOI18N
        EXCLUDED_DIR_NAMES.add("www"); // NOI18N
        EXCLUDED_DIR_NAMES.add("test"); // NOI18N
        EXCLUDED_DIR_NAMES.add("build"); // NOI18N
        EXCLUDED_DIR_NAMES.add("src"); // NOI18N
        EXCLUDED_DIR_NAMES.add("org"); // NOI18N
    }
    private static void doScanNetBeansOrgSources(Map<String,ModuleEntry> entries, File dir, int depth,
            File root, File nbdestdir, String pathPrefix, Set<File> knownProjects) {
        if (depth == 1) {
            LOG.log(Level.INFO, "exhaustive scan of {0}", dir);
        }
        File[] kids = dir.listFiles();
        if (kids == null) {
            return;
        }
        for (File kid : kids) {
            if (!kid.isDirectory()) {
                continue;
            }
            String name = kid.getName();
            if (name.startsWith(".") || EXCLUDED_DIR_NAMES.contains(name)) { // NOI18N
                // #61579/[NETBEANS-3898]: known to not be project dirs, so skip to save time.
                continue;
            }
            String newPathPrefix = (pathPrefix != null) ? pathPrefix + "/" + name : name; // NOI18N
            if (!knownProjects.contains(kid)) {
                try {
                    scanPossibleProject(kid, entries, NbModuleType.NETBEANS_ORG, root, nbdestdir, newPathPrefix);
                } catch (IOException e) {
                    // #60295: make it nonfatal.
                    Util.err.annotate(e, ErrorManager.UNKNOWN, "Malformed project metadata in " + kid + ", skipping...", null, null, null); // NOI18N
                    Util.err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            if (depth > 1) {
                doScanNetBeansOrgSources(entries, kid, depth - 1, root, nbdestdir, newPathPrefix, knownProjects);
            }
        }
    }
    
    /**
     * Tries to interpret basedir as NBM project dir and if project files are found, stores
     * its <tt>ModuleEntry</tt> to entries.
     * Does nothing if project files are not found.
     * @param basedir Project folder
     * @param entries CNB =&gt; ModuleEntry mapping is stored here if project found.
     * @param type Type of module (suite component, standalone or NB.org)
     * @param suiteComponent True if project is a suite component (asserts if both suiteComponent and standalone is true)
     * @param standalone True if project is a standalone module
     * @param root Root of NB.org sources, may be null. For NB.org modules only.
     * When not null, both both suiteComponent and standalone must be false.
     * @param nbdestdir NB build dest. folder, relative to root, "nbbuild/netbeans" by default. For NB.org modules only.
     * @param path Path to parent of project folder, relative to root. Usually "" or "contrib", NB.org modules only.
     * @throws java.io.IOException
     */
    static void scanPossibleProject(File basedir, Map<String,ModuleEntry> entries,
            NbModuleType type, File root, File nbdestdir, String path) throws IOException {
        LOG.log(Level.FINE, "scanning {0}", basedir);
        // TODO C.P many args can be extracted from metadata, just like cnb, separate methods for suite comp., standalone and NB.org
        directoriesChecked++;
        Element data = parseData(basedir);
        if (data == null) {
            return;
        }
        assert root != null ^ type != NbModuleType.NETBEANS_ORG;
        assert path != null ^ type != NbModuleType.NETBEANS_ORG;
        String cnb = XMLUtil.findText(XMLUtil.findElement(data, "code-name-base", NbModuleProject.NAMESPACE_SHARED)); // NOI18N
        PropertyEvaluator eval = parseProperties(basedir, root, type, cnb);
        String module = eval.getProperty("module.jar"); // NOI18N
        // Cf. ParseProjectXml.computeClasspath:
        StringBuilder cpextra = new StringBuilder();
        try {
            for (Element ext : XMLUtil.findSubElements(data)) {
                if (!ext.getLocalName().equals("class-path-extension")) { // NOI18N
                    continue;
                }
                Element binaryOrigin = XMLUtil.findElement(ext, "binary-origin", NbModuleProject.NAMESPACE_SHARED); // NOI18N
                String text;
                if (binaryOrigin != null) {
                    text = XMLUtil.findText(binaryOrigin);
                } else {
                    Element runtimeRelativePath = XMLUtil.findElement(ext, "runtime-relative-path", NbModuleProject.NAMESPACE_SHARED); // NOI18N
                    assert runtimeRelativePath != null : "Malformed <class-path-extension> in " + basedir;
                    String reltext = XMLUtil.findText(runtimeRelativePath);
                    // XXX assumes that module.jar is not overridden independently of module.jar.dir:
                    text = "${cluster}/${module.jar.dir}/" + reltext; // NOI18N
                }
                String evaluated = eval.evaluate(text);
                if (evaluated == null) {
                    continue;
                }
                File binary = PropertyUtils.resolveFile(basedir, evaluated);
                cpextra.append(File.pathSeparatorChar);
                cpextra.append(binary.getAbsolutePath());
            }
        } catch(IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Error getting subelements, malformed xml");
            cpextra = new StringBuilder();
        }
        File manifest = new File(basedir, "manifest.mf"); // NOI18N
        ManifestManager mm = (manifest.isFile() ? 
            ManifestManager.getInstance(manifest, false) : ManifestManager.NULL_INSTANCE);
        File clusterDir = PropertyUtils.resolveFile(basedir, eval.getProperty("cluster")); // NOI18N
        ModuleEntry entry;
        ManifestManager.PackageExport[] publicPackages = ProjectXMLManager.findPublicPackages(data);
        String[] friends = ProjectXMLManager.findFriends(data);
        String src = eval.getProperty("src.dir"); // NOI18N
        if (src == null) {
            src = "src"; // NOI18N
        }
        if (type == NbModuleType.NETBEANS_ORG) {
            entry = new NetBeansOrgEntry(root, cnb, path, clusterDir, module, cpextra.toString(),
                    mm.getReleaseVersion(), mm.getProvidedTokens(),
                    publicPackages, friends, mm.isDeprecated(), src);
        } else {
            entry = new ExternalEntry(basedir, cnb, clusterDir, PropertyUtils.resolveFile(clusterDir, module),
                    cpextra.toString(), nbdestdir, mm.getReleaseVersion(),
                    mm.getProvidedTokens(), publicPackages, friends, mm.isDeprecated(), src);
        }
        if (entries.containsKey(cnb)) {
            LOG.log(Level.WARNING, "Warning: two modules found with the same code name base (" + cnb + "): " + entries.get(cnb) + " and " + entry);
        } else {
            entries.put(cnb, entry);
        }
        registerEntry(entry, findSourceNBMFiles(entry, eval));
        LOG.log(Level.FINER, "scanPossibleProject: " + basedir + " scanned successfully");  // see ModuleListTest#testConcurrentScanning
    }
    
    /**
     * Look for files to be included in the NBM.
     * Some stock entries are always present: the module JAR, update_tracking/*.xml, config/Modules/*.xml,
     * config/ModuleAutoDeps/*.xml, ant/nblib/*.jar, modules/docs/*.jar (cf. common.xml#files-init).
     * Additionally, ${extra.module.files} if defined is parsed. Literal entries (no wildcards) are
     * always included; entries with Ant-style wildcards are included if matches can be found on disk.
     * And anything in the release/ directory is added.
     */
    private static Set<File> findSourceNBMFiles(ModuleEntry entry, PropertyEvaluator eval) throws IOException {
        Set<File> files = new HashSet<File>();
        files.add(entry.getJarLocation());
        File cluster = entry.getClusterDirectory();
        String cnbd = entry.getCodeNameBase().replace('.', '-');
        String[] STANDARD_FILES = {
            "update_tracking/*.xml", // NOI18N
            "config/Modules/*.xml", // NOI18N
            "config/ModuleAutoDeps/*.xml", // NOI18N
            "ant/nblib/*.jar", // NOI18N
            "modules/docs/*.jar", // NOI18N
        };
        for (String f : STANDARD_FILES) {
            int x = f.indexOf('*');
            findSourceNBMFilesMaybeAdd(files, cluster, f.substring(0, x) + cnbd + f.substring(x + 1));
        }
        String emf = eval.getProperty("extra.module.files"); // NOI18N
        if (emf != null) {
            for (String pattern : emf.split(" *, *")) { // NOI18N
                if (pattern.endsWith("/")) { // NOI18N
                    // Shorthand for /**
                    pattern += "**"; // NOI18N
                }
                if (pattern.indexOf('*') == -1) {
                    // Literal file location relative to cluster dir.
                    findSourceNBMFilesMaybeAdd(files, cluster, pattern);
                } else {
                    // Wildcard. Convert to regexp and do a brute-force search.
                    // Not the most efficient option but should probably suffice.
                    String regex = "\\Q" + pattern.replace("**", "__DBLASTERISK__") // NOI18N
                                                  .replace("*", "\\E[^/]*\\Q") // NOI18N
                                                  .replace("__DBLASTERISK__", "\\E.*\\Q") + "\\E"; // NOI18N
                    Pattern regexp = Pattern.compile(regex);
                    for (String clusterFile : scanDirForFiles(cluster)) {
                        if (regexp.matcher(clusterFile).matches()) {
                            findSourceNBMFilesMaybeAdd(files, cluster, clusterFile);
                        }
                    }
                }
            }
        }
        File src = entry.getSourceLocation();
        assert src != null && src.isDirectory() : entry;
        // XXX handle overrides of release.dir
        File releaseDir = new File(src, "release"); // NOI18N
        if (releaseDir.isDirectory()) {
            for (String releaseFile : scanDirForFiles(releaseDir)) {
                findSourceNBMFilesMaybeAdd(files, cluster, releaseFile);
            }
        }
        return files;
    }
    private static void findSourceNBMFilesMaybeAdd(Set<File> files, File cluster, String path) {
        File f = new File(cluster, path.replace('/', File.separatorChar));
        files.add(f);
    }
    private static final Map<File,String[]> DIR_SCAN_CACHE = new HashMap<File,String[]>();
    private static String[] scanDirForFiles(File dir) {
        String[] files = DIR_SCAN_CACHE.get(dir);
        if (files == null) {
            List<String> l = new ArrayList<String>(250);
            doScanDirForFiles(dir, l, "");
            files = l.toArray(new String[0]);
        }
        return files;
    }
    private static void doScanDirForFiles(File d, List<String> files, String prefix) {
        directoriesChecked++;
        File[] kids = d.listFiles();
        if (kids != null) {
            for (File f : kids) {
                if (f.isFile()) {
                    files.add(prefix + f.getName());
                } else if (f.isDirectory()) {
                    doScanDirForFiles(f, files, prefix + f.getName() + '/');
                }
            }
        }
    }
    
    public static ModuleList findOrCreateModuleListFromSuite(
            File root, File customNbDestDir) throws IOException {
        PropertyEvaluator eval = parseSuiteProperties(root);
        File nbdestdir = resolveNbDestDir(root, customNbDestDir, eval);

        Set<ClusterInfo> clup = ClusterUtils.evaluateClusterPath(root, eval, nbdestdir);
        LOG.log(Level.FINE, "Scanning suite in " + root + ", cluster.path is: " + clup);
        if (! clup.isEmpty()) {
            List<ModuleList> lists = new ArrayList<ModuleList>();
            lists.add(findOrCreateModuleListFromSuiteWithoutBinaries(root, nbdestdir, eval));
            lists.addAll(findOrCreateModuleListsFromClusterPath(clup, nbdestdir));
            // XXX should this also omit excluded modules? or should that be done only in e.g. LayerUtils.getPlatformJarsForSuiteComponentProject?
            return merge(lists.toArray(new ModuleList[0]), root);
        } else {
            return merge(new ModuleList[]{
                        findOrCreateModuleListFromSuiteWithoutBinaries(root, nbdestdir, eval),
                        findOrCreateModuleListFromBinaries(nbdestdir)
            }, root);
        }
    }

    private static List<ModuleList> findOrCreateModuleListsFromClusterPath(Set<ClusterInfo> clup, File customNbDestDir) throws IOException {
        List<ModuleList> lists = new ArrayList<ModuleList>();
        for (ClusterInfo ci : clup) {
            Project prj = ci.getProject();
            if (prj != null) {
                File prjDir = FileUtil.toFile(prj.getProjectDirectory());
                if (SuiteUtils.isSuite(prjDir)) {
                    // non-recursive for suites
                    lists.add(findOrCreateModuleListFromSuiteWithoutBinaries(prjDir, customNbDestDir));
                } else {
                    // should be standalone module
                    lists.add(findOrCreateModuleListFromStandaloneModule(prjDir, customNbDestDir));
                }
            } else {
                File cd = ci.getClusterDir();
                // null nbdestdir for external clusters
                ModuleList ml = findOrCreateModuleListFromCluster(cd, ci.isPlatformCluster() ? cd.getParentFile() : null, ci);
//                for (ModuleEntry e : ml.getAllEntriesSoft()) {
//                    if (e.)
//                }
                lists.add(ml);
            }
        }
        return lists;
    }

    private static ModuleList findOrCreateModuleListFromSuiteWithoutBinaries(File root, File nbdestdir, PropertyEvaluator eval) throws IOException {
        synchronized (sourceLists) {
            ModuleList sources = sourceLists.get(root);
            if (sources == null) {
                Map<String, ModuleEntry> entries = new HashMap<String, ModuleEntry>();
                for (File module : findModulesInSuite(root, eval)) {
                    try {
                        scanPossibleProject(module, entries, NbModuleType.SUITE_COMPONENT, null, nbdestdir, null);
                    } catch (IOException e) {
                        Util.err.annotate(e, ErrorManager.UNKNOWN, "Malformed project metadata in " + module + ", skipping...", null, null, null); // NOI18N
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
                sources = new ModuleList(entries, root);
                sourceLists.put(root, sources);
            }
            return sources;
        }
    }
    
    private static File resolveNbDestDir(File root, File customNbDestDir, PropertyEvaluator eval) throws IOException {
        File nbdestdir;
        if (customNbDestDir == null) {
            String nbdestdirS = eval.getProperty(NETBEANS_DEST_DIR);
            if (nbdestdirS == null) {
                throw new IOException("No netbeans.dest.dir defined in " + root); // NOI18N
            }
            nbdestdir = PropertyUtils.resolveFile(root, nbdestdirS);
        } else {
            nbdestdir = customNbDestDir;
        }
        if (! nbdestdir.exists()) {
            LOG.log(Level.INFO, "Project in " + root // NOI18N
                    + " is missing its platform '" + eval.getProperty("nbplatform.active") + "', switching to default platform");    // NOI18N
            NbPlatform p2 = NbPlatform.getDefaultPlatform();
            if (p2 != null)
                nbdestdir = p2.getDestDir();
        }
        return nbdestdir;
    }

    private static ModuleList findOrCreateModuleListFromSuiteWithoutBinaries(
            File root, File customNbDestDir) throws IOException {
        PropertyEvaluator eval = parseSuiteProperties(root);
        File nbdestdir = resolveNbDestDir(root, customNbDestDir, eval);
        return findOrCreateModuleListFromSuiteWithoutBinaries(root, nbdestdir, eval);
    }

    static ModuleList findOrCreateModuleListFromSuiteWithoutBinaries(File root) throws IOException {
        return findOrCreateModuleListFromSuiteWithoutBinaries(root, null);
    }
    
    private static PropertyEvaluator parseSuiteProperties(File root) throws IOException {
        Properties p = System.getProperties();
        Map<String,String> predefs;
        synchronized (p) {
            predefs = NbCollections.checkedMapByCopy(p, String.class, String.class, false);
        }
        predefs.put("basedir", root.getAbsolutePath()); // NOI18N
        PropertyProvider predefsProvider = PropertyUtils.fixedPropertyProvider(predefs);
        List<PropertyProvider> providers = new ArrayList<PropertyProvider>();
        providers.add(loadPropertiesFile(new File(root, "nbproject" + File.separatorChar + "private" + File.separatorChar + "platform-private.properties"))); // NOI18N
        providers.add(loadPropertiesFile(new File(root, "nbproject" + File.separatorChar + "platform.properties"))); // NOI18N
        PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, providers.toArray(new PropertyProvider[0]));
        String buildS = eval.getProperty("user.properties.file"); // NOI18N
        if (buildS != null) {
            providers.add(loadPropertiesFile(PropertyUtils.resolveFile(root, buildS)));
        } else {
            // Never been opened, perhaps - so fake it.
            providers.add(PropertyUtils.globalPropertyProvider());
        }
        providers.add(loadPropertiesFile(new File(root, "nbproject" + File.separatorChar + "private" + File.separatorChar + "private.properties"))); // NOI18N
        providers.add(loadPropertiesFile(new File(root, "nbproject" + File.separatorChar + "project.properties"))); // NOI18N
        eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, providers.toArray(new PropertyProvider[0]));
        providers.add(new DestDirProvider(eval));
        return PropertyUtils.sequentialPropertyEvaluator(predefsProvider, providers.toArray(new PropertyProvider[0]));
    }
    
    static File[] findModulesInSuite(File root) throws IOException {
        return findModulesInSuite(root, parseSuiteProperties(root));
    }
    
    private static File[] findModulesInSuite(File root, PropertyEvaluator eval) throws IOException {
        String modulesS = eval.getProperty("modules"); // NOI18N
        if (modulesS == null) {
            modulesS = ""; // NOI18N
        }
        String[] modulesA = PropertyUtils.tokenizePath(modulesS);
        File[] modules = new File[modulesA.length];
        for (int i = 0; i < modulesA.length; i++) {
            modules[i] = PropertyUtils.resolveFile(root, modulesA[i]);
        }
        return modules;
    }
    
    private static ModuleList findOrCreateModuleListFromStandaloneModule(
            File basedir, File customNbDestDir) throws IOException {
        PropertyEvaluator eval = parseProperties(basedir, null, NbModuleType.STANDALONE, "irrelevant"); // NOI18N
        File nbdestdir = resolveNbDestDir(basedir, customNbDestDir, eval);
        synchronized (sourceLists) {
            ModuleList binaries = findOrCreateModuleListFromBinaries(nbdestdir);
            ModuleList sources = sourceLists.get(basedir);
            if (sources == null) {
                Map<String,ModuleEntry> entries = new HashMap<String,ModuleEntry>();
                scanPossibleProject(basedir, entries, NbModuleType.STANDALONE, null, nbdestdir, null);
                if (entries.isEmpty()) {
                    throw new IOException("No module in " + basedir); // NOI18N
                }
                sources = new ModuleList(entries, basedir);
                sourceLists.put(basedir, sources);
            }
            return merge(new ModuleList[] {sources, binaries}, basedir);
        }
    }

    static ModuleList findOrCreateModuleListFromBinaries(File root) throws IOException {
        File[] clusters;
        synchronized (clusterLists) {    // no need to lock ProjectManager.mutex()
            clusters = clusterLists.get(root);
            if (clusters == null) {
                clusters = root.listFiles(new FileFilter() {
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });
                if (clusters == null) {
                    throw new IOException("Cannot examine dir " + root); // NOI18N
                }
                clusterLists.put(root, clusters);
            }
        }
        ModuleList[] lists = new ModuleList[clusters.length];
        for (int i = 0; i < clusters.length; i++) {
            lists[i] = findOrCreateModuleListFromCluster(clusters[i], root, null);
        }
        ModuleList ml = merge(lists, root);
        if (ml.getEntry(JUnitPlaceholderEntry.CNB) == null) { // #198739
            ml.entries.put(JUnitPlaceholderEntry.CNB, new JUnitPlaceholderEntry(root));
        }
        return ml;
    }
    private static class JUnitPlaceholderEntry extends AbstractBinaryEntry {
        static final String CNB = "org.netbeans.libs.junit4";
        JUnitPlaceholderEntry(File root) {
            super(CNB, new File(root, "platform/modules/" + CNB.replace('.', '-') + ".jar"),
                    new File[] {new File(System.getProperty("user.home"), ".m2/repository/junit/junit/4.13.2/junit-4.13.2.jar")},
                    new File(root, "platform"), null, null, new String[0],
                    new ManifestManager.PackageExport[] {new ManifestManager.PackageExport("junit", true), new ManifestManager.PackageExport("org.junit", true)},
                    null, false, Collections.<Dependency>emptySet());
        }
        @Override public File getSourceLocation() {
            return null;
        }
        @Messages("junit_placeholder=JUnit from Maven")
        @Override protected LocalizedBundleInfo getBundleInfo() {
            try {
                return LocalizedBundleInfo.load(new InputStream[] {new ByteArrayInputStream((LocalizedBundleInfo.NAME + '=' + junit_placeholder()).getBytes(StandardCharsets.ISO_8859_1))});
            } catch (IOException x) {
                assert false : x;
                return LocalizedBundleInfo.EMPTY;
            }
        }
        @Override protected Set<String> computePublicClassNamesInMainModule() {
            return new HashSet<String>();
        }
    }
    
    private static final String[] MODULE_DIRS = {
        "modules", // NOI18N
        "modules/eager", // NOI18N
        "modules/autoload", // NOI18N
        "lib", // NOI18N
        "core", // NOI18N
    };

    /**
     * Returns module entries list for given cluster.
     * Cached, call ModuleList#refresh() to clear cache.
     *
     * @param cluster Cluster dir
     * @param nbDestDir <tt>netbeans.dest.dir</tt> folder for NB.org modules, may be <tt>null</tt>
     * @param ci Cluster info, passed when scanning external cluster, may be <tt>null</tt>
     * @return List of modules in the cluster
     * @throws java.io.IOException
     */
    private static ModuleList findOrCreateModuleListFromCluster(final File cluster, final File nbDestDir, final ClusterInfo ci) throws IOException {
        synchronized (binaryLists) {    // no need to lock ProjectManager.mutex()
            ModuleList list = binaryLists.get(cluster);
            if (list == null) {
                list = scanCluster(cluster, nbDestDir, true, ci);
                binaryLists.put(cluster, list);
            }
            return list;
        }
    }

    /**
     * Scans cluster on disk and fills <tt>entries</tt> with found module entries.
     * @param cluster Path to cluster dir
     * @param nbDestDir <tt>netbeans.dest.dir</tt> folder for NB.org modules, may be <tt>null</tt>
     * @param entries Map to be filled with module entries found in cluster
     * @param registerEntry Whether register entries in known entries in ModuleList
     * @param ci Cluster info, passed when scanning external cluster, may be <tt>null</tt>
     * @throws java.io.IOException
     */
    public static ModuleList scanCluster(File cluster, @NullAllowed File nbDestDir, boolean registerEntry, ClusterInfo ci) throws IOException {
        Map<String, ModuleEntry> entries = new HashMap<String, ModuleEntry>();
        for (String moduleDir : MODULE_DIRS) {
            File dir = new File(cluster, moduleDir.replace('/', File.separatorChar));
            if (!dir.isDirectory()) {
                continue;
            }
            File[] jars = dir.listFiles();
            if (jars == null) {
                throw new IOException("Cannot examine dir " + dir); // NOI18N
            }
            scanJars(dir, ci, nbDestDir, cluster, entries, registerEntry, jars);
        }
        File configs = new File(new File(cluster, "config"), "Modules"); // NOI18N
        File[] xmls = configs.listFiles();
        if (xmls != null) {
            XPathExpression xpe = null;
            for (File xml : xmls) {
                String n = xml.getName();
                if (!n.endsWith(".xml")) { // NOI18N
                    continue;
                }
                n = n.substring(0, n.length() - 4).replace('-', '.');
                if (entries.get(n) != null) {
                    continue;
                }

                String res;
                Document doc;
                try {
                    doc = XMLUtil.parse(new InputSource(Utilities.toURI(xml).toString()), false, false, null, EntityCatalog.getDefault());
                    if (xpe == null) {
                        xpe = XPathFactory.newInstance().newXPath().compile("module/param[@name='jar']/text()");
                    }
                    res = xpe.evaluate(doc);
                    File jar = new File(cluster, res);
                    if (jar.exists()) {
                        scanJars(cluster, ci, nbDestDir, cluster, entries, registerEntry, jar);
                    }
                } catch (Exception ex) {
                    throw (IOException) new IOException(ex.toString()).initCause(ex);
                }
            }
        }
        LOG.log(Level.FINER, "scanCluster: " + cluster + " succeeded.");    // see ModuleListTest#testConcurrentScanning
        return new ModuleList(entries, nbDestDir);
    }

    /**
     * Try to find which files are part of a module's binary build (i.e. slated for NBM).
     * Tries to scan update tracking for the file, but also always adds in the module JAR
     * as a fallback (since this is the most important file for various purposes).
     * Note that update_tracking/*.xml is added as well as files it lists.
     */
    private static Set<File> findBinaryNBMFiles(File cluster, String cnb, File jar) throws IOException {
        Set<File> files = new HashSet<File>();
        files.add(jar);
        File tracking = new File(new File(cluster, "update_tracking"), cnb.replace('.', '-') + ".xml"); // NOI18N
        if (tracking.isFile()) {
            files.add(tracking);
            Document doc;
            try {
                xmlFilesParsed++;
                timeSpentInXmlParsing -= System.currentTimeMillis();
                doc = XMLUtil.parse(new InputSource(Utilities.toURI(tracking).toString()), false, false, null, null);
                timeSpentInXmlParsing += System.currentTimeMillis();
            } catch (SAXException e) {
                throw (IOException) new IOException(e.toString()).initCause(e);
            }
            for (Element moduleVersion : XMLUtil.findSubElements(doc.getDocumentElement())) {
                if (moduleVersion.getTagName().equals("module_version") && moduleVersion.getAttribute("last").equals("true")) { // NOI18N
                    for (Element fileEl : XMLUtil.findSubElements(moduleVersion)) {
                        if (fileEl.getTagName().equals("file")) { // NOI18N
                            String name = fileEl.getAttribute("name"); // NOI18N
                            File f = new File(cluster, name.replace('/', File.separatorChar));
                            if (f.isFile()) {
                                files.add(f);
                            }
                        }
                    }
                }
            }
        }
        return files;
    }
    
    private static final String PROJECT_XML = "nbproject" + File.separatorChar + "project.xml"; // NOI18N
    /**
     * Load a project.xml from a project.
     * @param basedir a putative project base directory
     * @return its primary configuration data (if there is an NBM project here), else null
     */
    static Element parseData(File basedir) throws IOException {
        File projectXml = new File(basedir, PROJECT_XML);
        // #61579: tboudreau claims File.exists is much cheaper on some systems
        //System.err.println("parseData: " + basedir);
        if (!projectXml.exists() || !projectXml.isFile()) {
            return null;
        }
        Document doc;
        try {
            xmlFilesParsed++;
            timeSpentInXmlParsing -= System.currentTimeMillis();
            doc = XMLUtil.parse(new InputSource(Utilities.toURI(projectXml).toString()), false, true, null, null);
            timeSpentInXmlParsing += System.currentTimeMillis();
        } catch (SAXException e) {
            throw (IOException) new IOException(projectXml + ": " + e.toString()).initCause(e); // NOI18N
        }
        Element docel = doc.getDocumentElement();
        Element type = XMLUtil.findElement(docel, "type", "http://www.netbeans.org/ns/project/1"); // NOI18N
        if (!XMLUtil.findText(type).equals("org.netbeans.modules.apisupport.project")) { // NOI18N
            return null;
        }
        Element cfg = XMLUtil.findElement(docel, "configuration", "http://www.netbeans.org/ns/project/1"); // NOI18N
        Element data = XMLUtil.findElement(cfg, "data", NbModuleProject.NAMESPACE_SHARED); // NOI18N
        if (data != null) {
            return data;
        } else {
            data = XMLUtil.findElement(cfg, "data", NbModuleProject.NAMESPACE_SHARED_2); // NOI18N
            if (data != null) {
                return XMLUtil.translateXML(data, NbModuleProject.NAMESPACE_SHARED);
            } else {
                return null;
            }
        }
    }
    
    /**
     * Load properties for a project.
     * Only deals with certain properties of interest here (all file-type values assumed relative to basedir):
     * netbeans.dest.dir (file-valued)
     * module.jar (plain string)
     * module.jar.dir (plain string)
     * cluster (file-valued)
     * suite.dir (file-valued)
     * @param basedir project basedir
     * @param root root of sources (netbeans.org only)
     * @param suiteComponent whether this is an external module in a suite
     * @param standalone whether this is an external standalone module
     * @param cnb code name base of this project
     */
    static PropertyEvaluator parseProperties(File basedir, File root, NbModuleType type, String cnb) throws IOException {
        Properties p = System.getProperties();
        Map<String,String> predefs;
        synchronized (p) {
            predefs = NbCollections.checkedMapByCopy(p, String.class, String.class, false);
        }
        predefs.put("basedir", basedir.getAbsolutePath()); // NOI18N
        PropertyProvider predefsProvider = PropertyUtils.fixedPropertyProvider(predefs);
        List<PropertyProvider> providers = new ArrayList<PropertyProvider>();
        if (type == NbModuleType.SUITE_COMPONENT) {
            providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "private" + File.separatorChar + "suite-private.properties"))); // NOI18N
            providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "suite.properties"))); // NOI18N
            PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, providers.toArray(new PropertyProvider[0]));
            String suiteS = eval.getProperty("suite.dir"); // NOI18N
            if (suiteS != null) {
                File suite = PropertyUtils.resolveFile(basedir, suiteS);
                providers.add(loadPropertiesFile(new File(suite, "nbproject" + File.separatorChar + "private" + File.separatorChar + "platform-private.properties"))); // NOI18N
                providers.add(loadPropertiesFile(new File(suite, "nbproject" + File.separatorChar + "platform.properties"))); // NOI18N
            }
        } else if (type == NbModuleType.STANDALONE) {
            providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "private" + File.separatorChar + "platform-private.properties"))); // NOI18N
            providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "platform.properties"))); // NOI18N
        }
        if (type != NbModuleType.NETBEANS_ORG) {
            PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, providers.toArray(new PropertyProvider[0]));
            String buildS = eval.getProperty("user.properties.file"); // NOI18N
            if (buildS != null) {
                providers.add(loadPropertiesFile(PropertyUtils.resolveFile(basedir, buildS)));
            } else {
                providers.add(PropertyUtils.globalPropertyProvider());
            }
            eval = PropertyUtils.sequentialPropertyEvaluator(predefsProvider, providers.toArray(new PropertyProvider[0]));
            providers.add(new DestDirProvider(eval));
        }
        // private.properties & project.properties.
        providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "private" + File.separatorChar + "private.properties"))); // NOI18N
        providers.add(loadPropertiesFile(new File(basedir, "nbproject" + File.separatorChar + "project.properties"))); // NOI18N
        // Implicit stuff.
        Map<String,String> defaults = new HashMap<String,String>();
        if (type == NbModuleType.NETBEANS_ORG) {
            defaults.put("nb_all", root.getAbsolutePath()); // NOI18N
            defaults.put(NETBEANS_DEST_DIR, findNetBeansOrgDestDir(root).getAbsolutePath());
        }
        defaults.put("code.name.base.dashes", cnb.replace('.', '-')); // NOI18N
        defaults.put("module.jar.dir", "modules"); // NOI18N
        defaults.put("module.jar.basename", "${code.name.base.dashes}.jar"); // NOI18N
        defaults.put("module.jar", "${module.jar.dir}/${module.jar.basename}"); // NOI18N
        defaults.put("build.dir", "build"); // NOI18N
        if (type == NbModuleType.SUITE_COMPONENT) {
            defaults.put("suite.build.dir", "${suite.dir}/build"); // NOI18N
        }
        providers.add(PropertyUtils.fixedPropertyProvider(defaults));
        defaults.put("cluster", findClusterLocation(basedir, root, type));
        return PropertyUtils.sequentialPropertyEvaluator(predefsProvider, providers.toArray(new PropertyProvider[0]));
    }
    
    private static PropertyProvider loadPropertiesFile(File f) throws IOException {
        if (!f.isFile()) {
            return PropertyUtils.fixedPropertyProvider(Collections.<String,String>emptyMap());
        }
        Properties p = new Properties();
        InputStream is = new FileInputStream(f);
        try {
            p.load(is);
        } finally {
            is.close();
        }
        return PropertyUtils.fixedPropertyProvider(NbCollections.checkedMapByFilter(p, String.class, String.class, true));
    }
    
    /**
     * Refresh any existing lists, e.g. in response to a new module being created.
     */
    public static void refresh() {
        synchronized (sourceLists) { sourceLists.clear(); }
        synchronized (binaryLists) { binaryLists.clear(); }
        synchronized (clusterLists) { clusterLists.clear(); }
        synchronized (knownEntries) { knownEntries.clear(); }
    }
    
    /**
     * Refresh cached module list for the given root dir.
     *
     * Root dir is suite dir for suites and suite components,
     * project dir for standalone modules and NB root dir
     * for NB.org modules. If there is not such a
     * cached list yet, the method is just no-op.
     */
    public static void refreshModuleListForRoot(File rootDir) {
        synchronized (sourceLists) {
            sourceLists.remove(rootDir);    // XXX knownEntries ?
        }
    }

    /**
     * Refresh cached module list for given cluster. If there is not such a
     * cached list yet, the method is just no-op.
     */
    public static void refreshClusterModuleList(File clusterDir) {
        // TODO C.P refresh not working yet for sources, need to clear knownEntries selectively
        // and ensure entries are loaded again when needed
//        binaryLists.remove(clusterDir);
    }
    
    /**
     * Whether whether a given dir is root of netbeans.org sources.
     */
    public static boolean isNetBeansOrg(File dir) {
        return new File(dir, "nbbuild").isDirectory(); // NOI18N
    }
    
    /**
     * Find the root of netbeans.org sources starting from a project basedir.
     */
    public static File findNetBeansOrg(File basedir) {
        File f = basedir;
        // Check for post-Hg layout:
        File repo = f.getParentFile();
        if (repo != null) {
            for (String tree : new String[] { null, "contrib" }) {
                File mainrepo;
                if (tree == null) {
                    mainrepo = repo;
                } else if (repo.getName().equals(tree)) {
                    mainrepo = repo.getParentFile();
                } else {
                    continue;
                }
                if (new File(mainrepo, "nbbuild").isDirectory()) { // NOI18N
                    return mainrepo;
                }
            }
        }
        // Check for pre-Hg layout:
        for (int i = 0; i < 3; i++) {
            f = f.getParentFile();
            if (f == null) {
                return null;
            }
            if (new File(f, "nbbuild").isDirectory()) { // NOI18N
                return f;
            }
        }
        // Not here.
        return null;
    }
    
    public static Map<String,String> getClusterProperties(File nbroot) throws IOException {
        Map<String, String> clusterDefs = null;
        synchronized (clusterPropertiesFiles) {
            clusterDefs = clusterPropertiesFiles.get(nbroot);
            if (clusterDefs == null) {
                PropertyProvider pp = loadPropertiesFile(getClusterPropertiesFile(nbroot)); // NOI18N
                PropertyEvaluator clusterEval = PropertyUtils.sequentialPropertyEvaluator(
                        PropertyUtils.fixedPropertyProvider(Collections.<String, String>emptyMap()), pp);
                clusterDefs = clusterEval.getProperties();
                if (clusterDefs == null) {
                    // Definition failure of some sort.
                    clusterDefs = Collections.emptyMap();
                }
                clusterPropertiesFiles.put(nbroot, clusterDefs);
            }
        }
        return clusterDefs;
    }
    
    /**
     * Find cluster location of a netbeans module (standalone, suite components and NB.org).
     * @param basedir project basedir
     * @param nbroot location of netbeans.org source root; not used for standalone modules and suite components
     */
    public static String findClusterLocation(File basedir, File nbroot, NbModuleType type) throws IOException {
        String cluster;
        switch (type) {
            case SUITE_COMPONENT:
                cluster = "${suite.build.dir}/cluster"; // NOI18N
                break;
            case STANDALONE:
                cluster = "${build.dir}/cluster"; // NOI18N
                break;
            case NETBEANS_ORG:
            default:
                String path = PropertyUtils.relativizeFile(nbroot, basedir);
        // #163744: can happen with symlinks       assert path.indexOf("..") == -1 : path;
                Map<String,String> clusterLocationsHere = clusterLocations.get(nbroot);
                if (clusterLocationsHere == null) {
                    clusterLocationsHere = new HashMap<String,String>();
                    Map<String,String> clusterDefs = getClusterProperties(nbroot);
                    for (Map.Entry<String,String> entry : clusterDefs.entrySet()) {
                        String key = entry.getKey();
                        String clusterDir = clusterDefs.get(key + ".dir"); // NOI18N
                        if (clusterDir == null) {
                            // Not a list of modules.
                            // XXX could also just read clusters.list
                            continue;
                        }
                        String val = entry.getValue();
                        StringTokenizer tok = new StringTokenizer(val, ", "); // NOI18N
                        while (tok.hasMoreTokens()) {
                            String p = tok.nextToken();
                            clusterLocationsHere.put(p, clusterDir);
                        }
                    }
                    clusterLocations.put(nbroot, clusterLocationsHere);
                }
                cluster = clusterLocationsHere.get(path);
                if (cluster == null && path != null) {
                    int clusterSep = path.lastIndexOf('/');
                    if (clusterSep >= 0) {
                        String id = path.substring(clusterSep + 1);
                        String expCluster = path.substring(0, clusterSep);

                        cluster = clusterLocationsHere.get(id);
                        if (!expCluster.equals(cluster)) {
                            cluster = null;
                        }
                    }
                }
                if (cluster == null) {
                    cluster = "extra"; // NOI18N
                }
                cluster = "${netbeans.dest.dir}/" + cluster;
        }
        return cluster;
    }
    
    // NONSTATIC PART
    
    /** all module entries, indexed by cnb */
    private Map<String,ModuleEntry> entries;
    
    /** originally passed top-level dir */
    private final File home;

    /**
     * If this list is for netbeans.org, we normally scan modules incrementally.
     * But sometimes it is necessary to get a more or less complete list.
     * Levels: 0 - only assorted modules known; 1 - all stable modules known; 2 - all modules known.
     * Cf. #62221.
     */
    private int lazyNetBeansOrgList = 0;
    
    private ModuleList(Map<String,ModuleEntry> entries, File home) {
        this.entries = entries;
        this.home = home;
        Set<String> forest = new LinkedHashSet<>();
        forest.add(null);
        forest.add("contrib");
        File cluster = new File(new File(home, "nbbuild"), "cluster.properties");
        if (cluster.exists()) {
            Properties p = new Properties();
            try (FileInputStream is = new FileInputStream(cluster)) {
                p.load(is);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Enumeration<?> en = p.propertyNames();
            while (en.hasMoreElements()) {
                String propName = (String) en.nextElement();
                if (propName.endsWith(".dir")) {
                    forest.add(p.getProperty(propName));
                }
            }
        }
        FOREST = forest.toArray(new String[0]);
    }
    
    public @Override String toString() {
        return "ModuleList[" + home + ",lazy=" + lazyNetBeansOrgList + "]" + entries.values(); // NOI18N
    }
    
    /**
     * Merge a bunch of module lists into one.
     * In case of conflict (by CNB), earlier entries take precedence.
     */
    private static ModuleList merge(ModuleList[] lists, File home) {
        Map<String,ModuleEntry> entries = new HashMap<String,ModuleEntry>();
        for (ModuleList list : lists) {
            for (Map.Entry<String,ModuleEntry> entry : list.entries.entrySet()) {
                String cnb = entry.getKey();
                if (!entries.containsKey(cnb)) {
                    entries.put(cnb, entry.getValue());
                }
            }
        }
        return new ModuleList(entries, home);
    }
    
    private void maybeRescanNetBeansOrgSources() {
        if (lazyNetBeansOrgList < 2) {
            lazyNetBeansOrgList = 2;
            File nbdestdir = findNetBeansOrgDestDir(home);
            Map<String,ModuleEntry> _entries = new HashMap<String,ModuleEntry>(entries); // #68513: possible race condition
            Set<File> knownProjects = new HashSet<File>();
            for (ModuleEntry known : entries.values()) {
                knownProjects.add(known.getSourceLocation());
            }
            if (new File(home, "openide.util").isDirectory()) { // NOI18N
                // Post-Hg layout.
                for (String tree : FOREST) {
                    doScanNetBeansOrgSources(_entries, tree == null ? home : new File(home, tree), 1, home, nbdestdir, tree, knownProjects);
                }
            } else {
                // Pre-Hg layout.
                doScanNetBeansOrgSources(_entries, home, 3, home, nbdestdir, null, knownProjects);
            }
            entries = _entries;
        }
    }
    
    /**
     * Find an entry by name.
     * @param codeNameBase code name base of the module
     * @return the matching module, or null if there is none such
     */
    public ModuleEntry getEntry(String codeNameBase) {
        if (codeNameBase == null)
            return null;
        ModuleEntry e = entries.get(codeNameBase);
        if (e != null) {
            return e;
        }
        if (home == null || !isNetBeansOrg(home)) {
            return null;
        }
            File nbdestdir = findNetBeansOrgDestDir(home);
            for (String tree : FOREST) {
                String name = abbreviate(codeNameBase);
                File basedir = new File(tree == null ? home : new File(home, tree), name);
                Map<String,ModuleEntry> _entries = new HashMap<String,ModuleEntry>();
                try {
                    scanPossibleProject(basedir, _entries, NbModuleType.NETBEANS_ORG, home, nbdestdir, tree == null ? name : tree + "/" + name);
                } catch (IOException x) {
                    LOG.log(Level.INFO, null, x);
                    continue;
                }
                if (!_entries.isEmpty()) {
                    _entries.putAll(entries);
                    entries = _entries;
                    e = _entries.get(codeNameBase);
                    if (e != null) {
                        LOG.log(Level.FINE, "Found entry for {0} by direct guess in {1}", new Object[] {codeNameBase, basedir});
                        return e;
                    }
                }
            }
        LOG.log(Level.WARNING, "could not find entry for {0} by direct guess in {1}", new Object[] {codeNameBase, home});
         try {
             scanNetBeansOrgStableSources();
         } catch (IOException x) {
             LOG.log(Level.INFO, null, x);
         }
         if (!entries.containsKey(codeNameBase)) {
             LOG.log(Level.WARNING, "could not find entry for {0} even among stable sources in {1}", new Object[] {codeNameBase, home});
             maybeRescanNetBeansOrgSources();
             if (!entries.containsKey(codeNameBase)) {
                 LOG.log(Level.WARNING, "failed to find entry for {0} at all in {1}", new Object[] {codeNameBase, home});
             }
         }
        return entries.get(codeNameBase);
    }

    public static String abbreviate(String cnb) {
        return cnb.replaceFirst("^org\\.netbeans\\.modules\\.", ""). // NOI18N
                   replaceFirst("^org\\.netbeans\\.(libs|lib|api|spi|core)\\.", "$1."). // NOI18N
                   replaceFirst("^org\\.netbeans\\.", "o.n."). // NOI18N
                   replaceFirst("^org\\.openide\\.", "openide."). // NOI18N
                   replaceFirst("^org\\.", "o."). // NOI18N
                   replaceFirst("^com\\.sun\\.", "c.s."). // NOI18N
                   replaceFirst("^com\\.", "c."); // NOI18N
    }
    
    /**
     * Get all known entries at once.
     * @return all known module entries
     */
    public Set<ModuleEntry> getAllEntries() {
        if (home != null && isNetBeansOrg(home)) {
            try {
                scanNetBeansOrgStableSources();
            } catch (IOException x) {
                LOG.log(Level.INFO, null, x);
            }
        }
        return new HashSet<ModuleEntry>(entries.values());
    }
    
    public static LocalizedBundleInfo loadBundleInfo(File projectDir) {
        LocalizedBundleInfo bundleInfo = ApisupportAntUtils.findLocalizedBundleInfo(projectDir);
        return bundleInfo == null ? LocalizedBundleInfo.EMPTY : bundleInfo;
    }

}
