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

package org.netbeans.modules.apisupport.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.apisupport.project.ProjectXMLManager.CyclicDependencyException;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.ClusterInfo;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.TestModuleDependency;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.SpecificationVersion;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

public class ApisupportAntUtils {

    private ApisupportAntUtils() {}

    /**
     * Tries to find {@link Project} in the given directory. If succeeds
     * delegates to {@link ProjectInformation#getDisplayName}. Returns {@link
     * FileUtil#getFileDisplayName} otherwise.
     */
    public static String getDisplayName(FileObject projectDir) {
        if (projectDir.isFolder()) {
            try {
                Project p = ProjectManager.getDefault().findProject(projectDir);
                if (p != null) {
                    return ProjectUtils.getInformation(p).getDisplayName();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return FileUtil.getFileDisplayName(projectDir);
    }
    
    /**
     * Check whether a given name can serve as a legal <ol>
     * <li>Java class name
     * <li>Java package name
     * <li>NB module code name base
     * </ol>
     */
    public static boolean isValidJavaFQN(String name) {
        if (name.length() == 0) {
            return false;
        }
        StringTokenizer tk = new StringTokenizer(name,".",true); //NOI18N
        boolean delimExpected = false;
        while (tk.hasMoreTokens()) {
            String namePart = tk.nextToken();
            if (delimExpected ^ namePart.equals(".")) { // NOI18N
                return false;
            }
            if (!delimExpected && !Utilities.isJavaIdentifier(namePart)) {
                return false;
            }
            delimExpected = !delimExpected;
        }
        return delimExpected;
    }
    
    /**
     * Check whether a given path can serve as a legal <ol>
     * <li>File path name
     * </ol>
     */
    public static boolean isValidFilePath(String name) {
        if (name.length() == 0) {
            return false;
        }
        name = name.substring(0, name.lastIndexOf("."));
        StringTokenizer tk = new StringTokenizer(name,"/",true); //NOI18N
        boolean delimExpected = false;
        while (tk.hasMoreTokens()) {
            String namePart = tk.nextToken();
            if (delimExpected ^ namePart.equals("/")) { // NOI18N
                return false;
            }
            if (!delimExpected && !Utilities.isJavaIdentifier(namePart)) {
                return false;
            }
            delimExpected = !delimExpected;
        }
        return delimExpected;
    }
    
    /**
     * Search for an appropriate localized bundle (i.e.
     * OpenIDE-Module-Localizing-Bundle) entry in the given
     * <code>manifest</code> taking into account branding and localization
     * (using {@link NbBundle#getLocalizingSuffixes}) and returns an
     * appropriate <em>valid</em> {@link LocalizedBundleInfo} instance. By
     * <em>valid</em> it's meant that a found localized bundle contains at
     * least a display name. If <em>valid</em> bundle is not found
     * <code>null</code> is returned.
     *
     * @param sourceDir source directory to be used for as a <em>searching
     *        path</em> for the bundle
     * @param manifest manifest the bundle's path should be extracted from
     * @return localized bundle info for the given project or <code>null</code>
     */
    public static LocalizedBundleInfo findLocalizedBundleInfo(FileObject sourceDir, Manifest manifest) {
        String locBundleResource =
                ManifestManager.getInstance(manifest, false).getLocalizingBundle();
        try {
            if (locBundleResource != null) {
                List<FileObject> bundleFOs = new ArrayList<FileObject>();
                for (String resource : getPossibleResources(locBundleResource)) {
                    FileObject bundleFO = sourceDir.getFileObject(resource);
                    if (bundleFO != null) {
                        bundleFOs.add(bundleFO);
                    }
                }
                if (!bundleFOs.isEmpty()) {
                    Collections.reverse(bundleFOs);
                    return LocalizedBundleInfo.load(bundleFOs.toArray(new FileObject[0]));
                }
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        return null;
    }
    
    /**
     * Actually deletages to {@link #findLocalizedBundleInfo(FileObject, Manifest)}.
     */
    public static LocalizedBundleInfo findLocalizedBundleInfo(File projectDir) {
        FileObject projectDirFO = FileUtil.toFileObject(projectDir);
        if (projectDirFO == null) {
            return null;
        }
        NbModuleProject p;
        try {
            p = (NbModuleProject) ProjectManager.getDefault().findProject(projectDirFO);
        } catch (IOException e) {
            return null;
        }
        if (p == null) {
            return null;
        }
        String src = p.evaluator().getProperty("src.dir"); // NOI18N
        if (src == null) {
            return null;
        }
        File srcF = FileUtil.normalizeFile(new File(projectDir, src));
        FileObject sourceDir = FileUtil.toFileObject(srcF);
        FileObject manifestFO = FileUtil.toFileObject(FileUtil.normalizeFile(new File(projectDir, "manifest.mf"))); // NOI18N
        
        LocalizedBundleInfo locInfo = null;
        Manifest mf = Util.getManifest(manifestFO);
        if (sourceDir != null && mf != null) {
            locInfo = findLocalizedBundleInfo(sourceDir, mf);
        }
        return locInfo;
    }
    
    /**
     * The same as {@link #findLocalizedBundleInfo(FileObject, Manifest)} but
     * searching in the given JAR representing a NetBeans module.
     */
    public static LocalizedBundleInfo findLocalizedBundleInfoFromJAR(File binaryProject) {
        try {
            JarFile main = new JarFile(binaryProject, false);
            try {
                Manifest mf = main.getManifest();
                String locBundleResource =
                        ManifestManager.getInstance(mf, false).getLocalizingBundle();
                if (locBundleResource != null) {
                    List<InputStream> bundleISs = new ArrayList<InputStream>();
                    Collection<JarFile> extraJarFiles = new ArrayList<JarFile>();
                    try {
                        // Look for locale variant JARs too.
                        // XXX the following could be simplified with #29580:
                        String name = binaryProject.getName();
                        int dot = name.lastIndexOf('.');
                        if (dot == -1) {
                            dot = name.length();
                        }
                        String base = name.substring(0, dot);
                        String suffix = name.substring(dot);
                        for (String infix : NbCollections.iterable(NbBundle.getLocalizingSuffixes())) {
                            File variant = new File(binaryProject.getParentFile(), "locale" + File.separatorChar + base + infix + suffix); // NOI18N
                            if (variant.isFile()) {
                                JarFile jf = new JarFile(variant, false);
                                extraJarFiles.add(jf);
                                addBundlesFromJar(jf, bundleISs, locBundleResource);
                            }
                        }
                        // Add main last, since we are about to reverse it:
                        addBundlesFromJar(main, bundleISs, locBundleResource);
                        if (!bundleISs.isEmpty()) {
                            Collections.reverse(bundleISs);
                            return LocalizedBundleInfo.load(bundleISs.toArray(new InputStream[0]));
                        }
                    } finally {
                        for (InputStream bundleIS : bundleISs) {
                            bundleIS.close();
                        }
                        for (JarFile jarFile : extraJarFiles) {
                            jarFile.close();
                        }
                    }
                }
                if (mf.getMainAttributes().getValue(ManifestManager.BUNDLE_SYMBOLIC_NAME) != null) {
                    Properties p = new Properties();
                    String[] from = {"Bundle-Name", "Bundle-Category", "Bundle-Description", "Bundle-Description"};
                    String[] to = {LocalizedBundleInfo.NAME, LocalizedBundleInfo.DISPLAY_CATEGORY,
                                   LocalizedBundleInfo.SHORT_DESCRIPTION, LocalizedBundleInfo.LONG_DESCRIPTION};
                    for (int i = 0; i < from.length; i++) {
                        String v = mf.getMainAttributes().getValue(from[i]);
                        if (v != null) {
                            p.setProperty(to[i], v);
                        }
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    p.store(baos, null);
                    return LocalizedBundleInfo.load(new InputStream[] {new ByteArrayInputStream(baos.toByteArray())});
                }
            } finally {
                main.close();
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        return null;
    }
    
    private static void addBundlesFromJar(JarFile jf, List<InputStream> bundleISs, String locBundleResource) throws IOException {
        for (String resource : getPossibleResources(locBundleResource)) {
            ZipEntry entry = jf.getEntry(resource);
            if (entry != null) {
                InputStream bundleIS = jf.getInputStream(entry);
                bundleISs.add(bundleIS);
            }
        }
    }
    
    private static Iterable<String> getPossibleResources(String locBundleResource) {
        String locBundleResourceBase, locBundleResourceExt;
        int idx = locBundleResource.lastIndexOf('.');
        if (idx != -1 && idx > locBundleResource.lastIndexOf('/')) {
            locBundleResourceBase = locBundleResource.substring(0, idx);
            locBundleResourceExt = locBundleResource.substring(idx);
        } else {
            locBundleResourceBase = locBundleResource;
            locBundleResourceExt = "";
        }
        Collection<String> resources = new LinkedHashSet<String>();
        for (String suffix : NbCollections.iterable(NbBundle.getLocalizingSuffixes())) {
            String resource = locBundleResourceBase + suffix + locBundleResourceExt;
            resources.add(resource);
            resources.add(resource);
    }
        return resources;
    }
    
    /**
     * Makes <code>target</code> project to be dependend on the given
     * <code>dependency</code> project. I.e. adds new &lt;module-dependency&gt;
     * element into target's <em>project.xml</em>. If such a dependency already
     * exists the method does nothing. If the given code name base cannot be
     * found in the module's universe or if adding the dependency creates dependency
     * cycle (since 6.8) the method logs informational message and does nothing otherwise.
     * <p>
     * Note that the method does <strong>not</strong> save the
     * <code>target</code> project. You need to do so explicitly (see {@link
     * ProjectManager#saveProject}).
     *
     * @param codeNameBase codename base.
     * @param releaseVersion release version, if <code>null</code> will be taken from the
     *        entry found in platform.
     * @param version {@link SpecificationVersion specification version}, if
     *        <code>null</code>, will be taken from the entry found in the
     *        module's target platform.
     * @param useInCompiler whether this this module needs a
     *        <code>dependency</code> module at a compile time.
     * @return true if a dependency was successfully added; false otherwise
     *         (e.g. when such dependency already exists)
     */
    public static boolean addDependency(final NbModuleProject target,
            final String codeNameBase, final @NullAllowed String releaseVersion,
            final @NullAllowed SpecificationVersion version, final boolean useInCompiler, String clusterName) throws IOException {
        if(target.getModuleType() == NbModuleType.SUITE_COMPONENT && clusterName != null) {
            final Project suiteProject = getSuiteProject(target);
            if(suiteProject!=null) {
                final SuiteProperties suiteProps = getSuiteProperties((SuiteProject)suiteProject);
                boolean isClusterIncludedInTargetPlatform;
                if((isClusterIncludedInTargetPlatform = isClusterIncludedInTargetPlatform(suiteProps, clusterName))
                        && !isModuleIncludedInTargetPlatform(suiteProps, codeNameBase)) {
                        addModuleToTargetPlatform(suiteProject, suiteProps, codeNameBase);
                } else if(!isClusterIncludedInTargetPlatform) {
                    addClusterToTargetPlatform(suiteProject, suiteProps, clusterName, codeNameBase);
                }
            }
        }
        ModuleEntry me = target.getModuleList().getEntry(codeNameBase);
        if (me == null) { // ignore semi-silently (#72611)
            Util.err.log(ErrorManager.INFORMATIONAL, "Trying to add " + codeNameBase + // NOI18N
                    " which cannot be found in the module's universe."); // NOI18N
            return false;
        }
        
        ProjectXMLManager pxm = new ProjectXMLManager(target);
        
        // firstly check if the dependency is already not there
        for (ModuleDependency md : pxm.getDirectDependencies()) {
            if (codeNameBase.equals(md.getModuleEntry().getCodeNameBase())) {
                if (version != null && !md.hasImplementationDependency()) {
                    String old = md.getSpecificationVersion();
                    if (old == null || version.compareTo(new SpecificationVersion(old)) > 0) {
                        pxm.removeDependency(codeNameBase);
                        break;
                    }
                }
                Util.err.log(ErrorManager.INFORMATIONAL, codeNameBase + " already added"); // NOI18N
                return false;
            }
        }
        
        ModuleDependency md = new ModuleDependency(me,
                (releaseVersion == null) ?  me.getReleaseVersion() : releaseVersion,
                version == null ? me.getSpecificationVersion() : version.toString(),
                useInCompiler, false);
        try {
            pxm.addDependency(md);
        } catch (CyclicDependencyException ex) {
            Util.err.log(ErrorManager.INFORMATIONAL, ex.getLocalizedMessage());
            return false;
        }
        return true;
    }
    
    static void addTestDependency(NbModuleProject prj, String codeNameBase, String clusterName) throws IOException {
        if(prj.getModuleType() == NbModuleType.SUITE_COMPONENT && clusterName != null) {
            final Project suiteProject = getSuiteProject(prj);
            if(suiteProject!=null) {
                final SuiteProperties suiteProps = getSuiteProperties((SuiteProject)suiteProject);
                boolean isClusterIncludedInTargetPlatform;
                if((isClusterIncludedInTargetPlatform = isClusterIncludedInTargetPlatform(suiteProps, clusterName))
                        && !isModuleIncludedInTargetPlatform(suiteProps, codeNameBase)) {
                    addModuleToTargetPlatform(suiteProject, suiteProps, codeNameBase);
                } else if(!isClusterIncludedInTargetPlatform) {
                    addClusterToTargetPlatform(suiteProject, suiteProps, clusterName, codeNameBase);
                }
            }
        }
        ModuleEntry me = prj.getModuleList().getEntry(codeNameBase);
        if (me == null) { // ignore semi-silently (#72611)
            Util.err.log(ErrorManager.INFORMATIONAL, "Trying to add " + codeNameBase + // NOI18N
                    " which cannot be found in the module's universe."); // NOI18N
            return;
        }
        ProjectXMLManager pxm = new ProjectXMLManager(prj);
        Map<String, Set<TestModuleDependency>> map = pxm.getTestDependencies(prj.getModuleList());
        if (map != null && map.get("unit") != null) {
            // firstly check if the dependency is already not there
            for (TestModuleDependency md : map.get("unit")) {
                if (codeNameBase.equals(md.getModule().getCodeNameBase())) {
                    Util.err.log(ErrorManager.INFORMATIONAL, codeNameBase + " already added"); // NOI18N
                    return;
                }
            }
        }
        
        TestModuleDependency md = new TestModuleDependency(me, true, true, true);
        pxm.addTestDependency("unit", md);
    }
    
    
    
    public static URL findJavadocURL(final String cnbdashes, final URL[] roots) {
        URL indexURL = null;
        for (int i = 0; i < roots.length; i++) {
            URL root = roots[i];
            try {
                indexURL = normalizeURL(new URL(root, cnbdashes + "/index.html")); // NOI18N
                if (indexURL == null && (root.toExternalForm().indexOf(cnbdashes) != -1)) {
                    indexURL = normalizeURL(new URL(root, "index.html")); // NOI18N
                }
            } catch (MalformedURLException ex) {
                // ignore - let the indexURL == null
            }
            if (indexURL != null) {
                break;
            }
        }
        return indexURL;
    }
    
    private static URL normalizeURL(URL url) {
        FileObject fo = URLMapper.findFileObject(url);
        if (fo != null) {
            return URLMapper.findURL(fo, URLMapper.EXTERNAL);
        } else {
            return null;
        }
    }
    
    /**
     * Property provider which computes one or more properties based on some properties coming
     * from an intermediate evaluator, and is capable of firing changes correctly.
     */
    public abstract static class ComputedPropertyProvider implements PropertyProvider, PropertyChangeListener {
        private final PropertyEvaluator eval;
        private final ChangeSupport cs = new ChangeSupport(this);
        protected ComputedPropertyProvider(PropertyEvaluator eval) {
            this.eval = eval;
            eval.addPropertyChangeListener(WeakListeners.propertyChange(this, eval));
        }
        /** get properties based on the incoming properties */
        protected abstract Map<String,String> getProperties(Map<String,String> inputPropertyValues);
        /** specify interesting input properties */
        protected abstract Collection<String> inputProperties();
        @Override
        public final Map<String,String> getProperties() {
            Map<String,String> vals = new HashMap<String, String>();
            for (String k : inputProperties()) {
                vals.put(k, eval.getProperty(k));
            }
            return getProperties(vals);
        }
        @Override
        public final void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }
        @Override
        public final void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }
        @Override
        public final void propertyChange(PropertyChangeEvent evt) {
            String p = evt.getPropertyName();
            if (p != null && !inputProperties().contains(p)) {
                return;
            }
            cs.fireChange();
        }
    }
    
    public static final class UserPropertiesFileProvider implements PropertyProvider, PropertyChangeListener, ChangeListener {
        private final PropertyEvaluator eval;
        private final File basedir;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private final ChangeListener listener = WeakListeners.change(this, null);
        private PropertyProvider delegate;
        public UserPropertiesFileProvider(PropertyEvaluator eval, File basedir) {
            this.eval = eval;
            this.basedir = basedir;
            eval.addPropertyChangeListener(WeakListeners.propertyChange(this, eval));
            computeDelegate();
        }
        private void computeDelegate() {
            if (delegate != null) {
                delegate.removeChangeListener(listener);
            }
            String buildS = eval.getProperty("user.properties.file"); // NOI18N
            if (buildS != null) {
                delegate = PropertyUtils.propertiesFilePropertyProvider(PropertyUtils.resolveFile(basedir, buildS));
            } else {
                /* XXX what should we do?
                delegate = null;
                 */
                delegate = PropertyUtils.globalPropertyProvider();
            }
            delegate.addChangeListener(listener);
        }
        @Override
        public Map<String,String> getProperties() {
            if (delegate != null) {
                return delegate.getProperties();
            } else {
                return Collections.emptyMap();
            }
        }
        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }
        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String p = evt.getPropertyName();
            if (p == null || p.equals("user.properties.file")) { // NOI18N
                computeDelegate();
                changeSupport.fireChange();
            }
        }
        @Override
        public void stateChanged(ChangeEvent e) {
            changeSupport.fireChange();
        }
    }
    
    /**
     * Finds all available packages in a given project directory, including <tt>%lt;class-path-extension&gt;</tt>-s.
     * See {@link #scanJarForPackageNames(java.util.Set, java.io.File)} for details.
     * 
     * @param prjDir directory containing project to be scanned
     * @return a set of found packages
     */
    public static SortedSet<String> scanProjectForPackageNames(final File prjDir) {
        return scanProjectForPackageNames(prjDir, true);
    }

    /**
     * Finds all available packages in a given project directory. Found entries
     * are in the form of a regular java package (x.y.z).
     *
     * @param prjDir directory containing project to be scanned
     * @param withCPExt When <tt>false</tt> only source roots are scanned, otherwise scans <tt>%lt;class-path-extension&gt;</tt>-s as well.
     * @return a set of found packages
     */
    public static SortedSet<String> scanProjectForPackageNames(final File prjDir, boolean withCPExt) {
        NbModuleProject project = null;
        // find all available public packages in classpath extensions
        FileObject source = FileUtil.toFileObject(prjDir);
        if (source != null) { // ??
            try {
                project = (NbModuleProject) ProjectManager.getDefault().findProject(source);
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        if (project == null) {
            return new TreeSet<String>(Collections.<String>emptySet());
        }
        
        SortedSet<String> availablePublicPackages = new TreeSet<String>();
        // find all available public packages in a source root
        Set<FileObject> pkgs = new HashSet<FileObject>();
        FileObject srcDirFO = project.getSourceDirectory();
        scanForPackages(pkgs, srcDirFO, "java"); // NOI18N
        for (FileObject pkg : pkgs) {
            if (srcDirFO.equals(pkg)) { // default package #71532
                continue;
            }
            String pkgS = PropertyUtils.relativizeFile(FileUtil.toFile(srcDirFO), FileUtil.toFile(pkg));
            availablePublicPackages.add(pkgS.replace('/', '.'));
        }
        
        if (withCPExt) {
            String[] libsPaths = new ProjectXMLManager(project).getBinaryOrigins();
            for (int i = 0; i < libsPaths.length; i++) {
                scanJarForPackageNames(availablePublicPackages, project.getHelper().resolveFile(libsPaths[i]));
            }
        }
        
        // #72669: remove invalid packages.
        Iterator<String> it = availablePublicPackages.iterator();
        while (it.hasNext()) {
            String pkg = it.next();
            if (!isValidJavaFQN(pkg)) {
                it.remove();
            }
        }
        return availablePublicPackages;
    }
    
    /**
     * Scans a given jar file for all packages which contains at least one
     * .class file. Found entries are in the form of a regular java package
     * (x.y.z).
     * 
     * @param jarFile jar file to be scanned
     * @param packages a set into which found packages will be added
     */
    public static void scanJarForPackageNames(final Set<String> packages, final File jarFile) {
        FileObject jarFileFO = FileUtil.toFileObject(jarFile);
        if (jarFileFO == null) {
            // Broken classpath entry, perhaps.
            return;
        }
        FileObject root = FileUtil.getArchiveRoot(jarFileFO);
        if (root == null) {
            // Not really a JAR?
            return;
        }
        Set<FileObject> pkgs = new HashSet<FileObject>();
        scanForPackages(pkgs, root, "class"); // NOI18N
        for (FileObject pkg : pkgs) {
            if (root.equals(pkg)) { // default package #71532
                continue;
            }
            String pkgS = pkg.getPath().replace('/', '.');
            if (isValidJavaFQN(pkgS))
                packages.add(pkgS);
        }
    }
    
    /**
     * Scan recursively through all folders in the given <code>dir</code> and
     * add every directory/package, which contains at least one file with the
     * given extension (probably class or java), into the given
     * <code>validPkgs</code> set. Added entries are in the form of regular java
     * package (x.y.z)
     */
    private static void scanForPackages(final Set<FileObject> validPkgs, final FileObject dir, final String ext) {
        if (dir == null) {
            return;
        }
        for (Enumeration<? extends FileObject> en1 = dir.getFolders(false); en1.hasMoreElements(); ) {
            FileObject subDir = (FileObject) en1.nextElement();
            if (VisibilityQuery.getDefault().isVisible(subDir)) {
                scanForPackages(validPkgs, subDir, ext);
            }
        }
        for (Enumeration<? extends FileObject> en2 = dir.getData(false); en2.hasMoreElements(); ) {
            FileObject kid = (FileObject) en2.nextElement();
            if (kid.hasExt(ext) && Utilities.isJavaIdentifier(kid.getName())) {
                // at least one class inside directory -> valid package
                validPkgs.add(dir);
                break;
            }
        }
    }
    
    public static String urlsToAntPath(final URL[] urls) {
        return ClassPathSupport.createClassPath(urls).toString(ClassPath.PathConversionMode.WARN);
    }

    public static URL[] findURLs(final String path) {
        if (path == null) {
            return new URL[0];
        }
        String[] pieces = PropertyUtils.tokenizePath(path);
        URL[] urls = new URL[pieces.length];
        for (int i = 0; i < pieces.length; i++) {
            // XXX perhaps also support http: URLs somehow?
            urls[i] = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(new File(pieces[i])));
        }
        return urls;
    }

    public static final String CPEXT_BINARY_PATH = "release/modules/ext/";
    public static final String CPEXT_RUNTIME_RELATIVE_PATH = "ext/";

    /**
     * Copies given JAR file into <tt>release/modules/ext</tt> folder under <tt>projectDir</tt>.
     * <tt>release/modules/ext</tt> will be created if necessary.
     *
     * @param projectDir Project folder
     * @param jar JAR file to be copied
     * @return If JAR copied successfully, returns string array <tt>{&lt;runtime-relative path&gt, &lt;binary origin path&gt;}</tt>,
     * otherwise <tt>null</tt>.
     * @throws IOException When <tt>release/modules/ext</tt> folder cannot be created.
     */
    public static String[] copyClassPathExtensionJar(File projectDir, File jar) throws IOException {
        String[] ret = null;

        File releaseDir = new File(projectDir, CPEXT_BINARY_PATH); //NOI18N
        if (! releaseDir.isDirectory() && !releaseDir.mkdirs()) {
            throw new IOException("cannot create release directory '" + releaseDir + "'.");    // NOI18N
        }
        
        FileObject relDirFo = FileUtil.toFileObject(releaseDir);
        FileObject orig = FileUtil.toFileObject(FileUtil.normalizeFile(jar));
        if (orig != null) {
            FileObject existing = relDirFo.getFileObject(orig.getName(), orig.getExt());
            if (existing != null)
                existing.delete();
            FileUtil.copyFile(orig, relDirFo, orig.getName());
            ret = new String[2];
            ret[0] = CPEXT_RUNTIME_RELATIVE_PATH + orig.getNameExt();    // NOI18N
            ret[1] = CPEXT_BINARY_PATH + orig.getNameExt(); // NOI18N
        }
        return ret;
    }

    static SuiteProperties getSuiteProperties(NbModuleProject target) {
        final Project suiteProject = getSuiteProject(target);
        if(suiteProject!=null) {
            return getSuiteProperties((SuiteProject) suiteProject);
}
        return null; 
    }
    
    static SuiteProperties getSuiteProperties(SuiteProject suiteProject) {
        if(suiteProject!=null) {
            Set<NbModuleProject> subModules = SuiteUtils.getSubProjects(suiteProject);
            return new SuiteProperties(suiteProject, suiteProject.getHelper(),
                suiteProject.getEvaluator(), subModules);
        }
        return null; 
    }
    
    static Project getSuiteProject(NbModuleProject target) {
        File suiteDirectory = target.getLookup().lookup(SuiteProvider.class).getSuiteDirectory();
        if(suiteDirectory!=null) {
            FileObject suiteDirectoryFO = FileUtil.toFileObject(suiteDirectory);
            if(suiteDirectoryFO != null) {
                try {
                    return ProjectManager.getDefault().findProject(suiteDirectoryFO);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return null; 
    }
    
    static boolean isModuleIncludedInTargetPlatform(SuiteProperties suiteProps, String codeNameBase) {
        if(suiteProps != null) {
            for(String disabledModuleIter : suiteProps.getDisabledModules()) {
                if(disabledModuleIter.equals(codeNameBase)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    static boolean isClusterIncludedInTargetPlatform(SuiteProperties suiteProps, String clusterName) {
        if(suiteProps != null) {
            Set<ClusterInfo> clusterInfoSet = suiteProps.getClusterPath();
            if(clusterInfoSet!=null) {
                for(ClusterInfo infoIter : clusterInfoSet) {
                    if(infoIter.getClusterDir().getName().equals(clusterName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    static void addModuleToTargetPlatform(final Project suiteProject, final SuiteProperties suiteProps, String codeNameBase) {
        if(suiteProps != null) {
            Set<String> disabledModules = new HashSet<String>(Arrays.asList(suiteProps.getDisabledModules()));
            for(String disableModuleIter:disabledModules) {
                if(codeNameBase.equals(disableModuleIter)) {
                    disabledModules.remove(codeNameBase);
                    break;
                }
            }
            String [] updatedDiasabledModules = new String[disabledModules.size()];
            disabledModules.toArray(updatedDiasabledModules);
            suiteProps.setDisabledModules(updatedDiasabledModules);
            ProjectManager.mutex().writeAccess(new Runnable() {
                @Override
                public void run() {
                    try {
                        suiteProps.storeProperties();
                        ProjectManager.getDefault().saveProject(suiteProject);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    }
    
    static void addClusterToTargetPlatform(final Project suiteProject, final SuiteProperties suiteProps, String clusterName, String codeNameBase) {
        if(suiteProps != null) {
            List<ClusterInfo> clusterInfoSet = new ArrayList<ClusterInfo>(suiteProps.getClusterPath());
            if(suiteProps.getActivePlatform() != null) {
                File clusterDirectory = null;
                Set<ModuleEntry> moduleList = suiteProps.getActivePlatform().getModules();
                for(ModuleEntry entryIter:moduleList) {
                    if(entryIter.getClusterDirectory().getName().equals(clusterName)) {
                        clusterDirectory = entryIter.getClusterDirectory();
                        break;
                    }
                }
                if(clusterDirectory != null) {
                    ClusterInfo newClusterInfo = ClusterInfo.create(clusterDirectory, 
                        true, true);
                    clusterInfoSet.add(newClusterInfo);
                    Set<String> disabledModules = new HashSet<String>(Arrays.asList(suiteProps.getDisabledModules()));
                    for(ModuleEntry entryIter:moduleList) {
                        if(entryIter.getClusterDirectory().equals(clusterDirectory)) {
                            disabledModules.add(entryIter.getCodeNameBase());
                        }
                    }
                    suiteProps.setClusterPath(clusterInfoSet);
                    disabledModules.remove(codeNameBase);
                    String [] updatedDiasabledModules = new String[disabledModules.size()];
                    disabledModules.toArray(updatedDiasabledModules);
                    suiteProps.setDisabledModules(updatedDiasabledModules);
                    ProjectManager.mutex().writeAccess(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                suiteProps.storeProperties();
                                ProjectManager.getDefault().saveProject(suiteProject);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                }
            }
        }
    }

}
