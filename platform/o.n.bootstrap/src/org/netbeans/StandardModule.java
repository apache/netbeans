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

package org.netbeans;

// THIS CLASS OUGHT NOT USE NbBundle NOR org.openide CLASSES
// OUTSIDE OF openide-util.jar! UI AND FILESYSTEM/DATASYSTEM
// INTERACTIONS SHOULD GO ELSEWHERE.
// (NbBundle.getLocalizedValue is OK here.)

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.netbeans.LocaleVariants.FileWithSuffix;
import org.openide.modules.Dependency;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.BaseUtilities;

/** Object representing one module, possibly installed.
 * Responsible for opening of module JAR file; reading
 * manifest; parsing basic information such as dependencies;
 * and creating a classloader for use by the installer.
 * Methods not defined in ModuleInfo must be called from within
 * the module manager's read mutex as a rule.
 * @author Jesse Glick, Allan Gregersen
 */
class StandardModule extends Module {

    /** JAR file holding the module */
    private final File jar;
    /** if reloadable, temporary JAR file actually loaded from */
    private File physicalJar;
    private Manifest manifest;
    
    /** Simple registry of JAR files used as modules.
     * Used only for debugging purposes, so that we can be sure
     * that no one is using Class-Path to refer to other modules.
     */
    private static final Set<File> moduleJARs = new HashSet<File>();

    /** Patches added at the front of the classloader (or null).
     * Files are assumed to be JARs; directories are themselves.
     */
    private Set<File> patches;
    
    /** localized properties, only non-null if requested from disabled module */
    private Properties localizedProps;
    
    /** Use ModuleManager.create as a factory. */
    public StandardModule(ModuleManager mgr, Events ev, File jar, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException {
        super(mgr, ev, history, JaveleonModule.isJaveleonPresent || reloadable, autoload, eager);
        this.jar = jar;
        moduleJARs.add(jar);
    }

    @Override
    ModuleData createData(ObjectInput in, Manifest mf) throws IOException {
        if (in != null) {
            return new StandardModuleData(in);
        } else {
            return new StandardModuleData(mf, this);
        }
    }

    public @Override Manifest getManifest() {
        if (manifest == null) {
            try {
                loadManifest();
            } catch (IOException x) {
                Util.err.log(Level.WARNING, "While loading manifest for " + getJarFile(), x);
                manifest = new Manifest();
            }
        }
        return manifest;
    }

    public @Override void releaseManifest() {
        manifest = null;
    }
    
    /** Get a localized attribute.
     * First, if OpenIDE-Module-Localizing-Bundle was given, the specified
     * bundle file (in all locale JARs as well as base JAR) is searched for
     * a key of the specified name.
     * Otherwise, the manifest's main attributes are searched for an attribute
     * with the specified name, possibly with a locale suffix.
     * If the attribute name contains a slash, and there is a manifest section
     * named according to the part before the last slash, then this section's attributes
     * are searched instead of the main attributes, and for the attribute listed
     * after the slash. Currently this would only be useful for localized filesystem
     * names. E.g. you may request the attribute org/foo/MyFileSystem.class/Display-Name.
     * In the future certain attributes known to be dangerous could be
     * explicitly suppressed from this list; should only be used for
     * documented localizable attributes such as OpenIDE-Module-Name etc.
     */
    public Object getLocalizedAttribute(String attr) {
        String locb = getManifest().getMainAttributes().getValue("OpenIDE-Module-Localizing-Bundle"); // NOI18N
        boolean usingLoader = false;
        if (locb != null) {
            if (classloader != null) {
                if (locb.endsWith(".properties")) { // NOI18N
                    usingLoader = true;
                    String basename = locb.substring(0, locb.length() - 11).replace('/', '.');
                    try {
                        ResourceBundle bundle = NbBundle.getBundle(basename, Locale.getDefault(), classloader);
                        try {
                            return bundle.getString(attr);
                        } catch (MissingResourceException mre) {
                            // Fine, ignore.
                        }
                    } catch (MissingResourceException mre) {
                        String resource = basename.replace('.', '/') + ".properties";
                        Exceptions.attachMessage(mre, "#149833: failed to find " + basename +
                                " in locale " + Locale.getDefault() + " in " + classloader + " for " + jar +
                                "; resource lookup of " + resource + " -> " + classloader.getResource(resource));
                        Exceptions.printStackTrace(mre);
                    }
                } else {
                    Util.err.warning("cannot efficiently load non-*.properties OpenIDE-Module-Localizing-Bundle: " + locb);
                }
            }
            if (!usingLoader) {
                if (localizedProps == null) {
                    Util.err.log(Level.FINE, "Trying to get localized attr {0} from disabled module {1}", new Object[] {attr, getCodeNameBase()});
                    try {
                        // check if the jar file still exists (see issue 82480)
                        if (jar != null && jar.isFile ()) {
                            JarFile jarFile = new JarFile(jar, false);
                            try {
                                loadLocalizedProps(jarFile, getManifest());
                            } finally {
                                jarFile.close();
                            }
                        } else {
                            Util.err.log(Level.FINE, "Cannot get localized attr {0} from module {1} (missing or deleted JAR file: {2})", new Object[] {attr, getCodeNameBase(), jar});
                        }
                    } catch (IOException ioe) {
                        Util.err.log(Level.WARNING, jar.getAbsolutePath(), ioe);
                    }
                }
                if (localizedProps != null) {
                    String val = localizedProps.getProperty(attr);
                    if (val != null) {
                        return val;
                    }
                }
            }
        }
        // Try in the manifest now.
        int idx = attr.lastIndexOf('/'); // NOI18N
        if (idx == -1) {
            // Simple main attribute.
            return NbBundle.getLocalizedValue(getManifest().getMainAttributes(), new Attributes.Name(attr));
        } else {
            // Attribute of a manifest section.
            String section = attr.substring(0, idx);
            String realAttr = attr.substring(idx + 1);
            Attributes attrs = getManifest().getAttributes(section);
            if (attrs != null) {
                return NbBundle.getLocalizedValue(attrs, new Attributes.Name(realAttr));
            } else {
                return null;
            }
        }
    }
    
    public boolean isFixed() {
        return false;
    }
    
    /** Get the JAR this module is packaged in.
     * May be null for modules installed specially, e.g.
     * automatically from the classpath.
     * @see #isFixed
     */
    public @Override File getJarFile() {
        return jar;
    }
    
    /** Create a temporary test JAR if necessary.
     * This is primarily necessary to work around a Java bug,
     * #4405789, which is marked as fixed so might be obsolete.
     */
    private void ensurePhysicalJar() throws IOException {
        if (reloadable && physicalJar == null) {
            physicalJar = Util.makeTempJar(jar);
        }
    }
    private void destroyPhysicalJar() {
        if (physicalJar != null) {
            if (physicalJar.isFile()) {
                if (! physicalJar.delete()) {
                    Util.err.warning("temporary JAR " + physicalJar + " not currently deletable.");
                } else {
                    Util.err.fine("deleted: " + physicalJar);
                }
            }
            physicalJar = null;
        } else {
            Util.err.fine("no physicalJar to delete for " + this);
        }
    }
    
    /** Open the JAR, load its manifest, and do related things. */
    private void loadManifest() throws IOException {
        if (Util.err.isLoggable(Level.FINE)) {
            Util.err.fine("loading manifest of " + jar);
        }
        File jarBeingOpened = null; // for annotation purposes
        try {
            if (reloadable) {
                // Never try to cache reloadable JARs.
                jarBeingOpened = physicalJar; // might be null
                ensurePhysicalJar();
                jarBeingOpened = physicalJar; // might have changed
                JarFile jarFile = new JarFile(physicalJar, false);
                try {
                    Manifest m = jarFile.getManifest();
                    if (m == null) throw new IOException("No manifest found in " + physicalJar); // NOI18N
                    manifest = m;
                } finally {
                    jarFile.close();
                }
            } else {
                jarBeingOpened = jar;
                manifest = getManager().loadManifest(jar);
            }
        } catch (IOException e) {
            if (jarBeingOpened != null) {
                Exceptions.attachMessage(e,
                                         "While loading manifest from: " +
                                         jarBeingOpened); // NOI18N
            }
            throw e;
        }
    }
    
    private Set<File> findPatches() {
        if (patches == null) {
            // #9273: load any modules/patches/this-code-name/*.jar files first:
            File patchdir = new File(new File(jar.getParentFile(), "patches"), // NOI18N
                getCodeNameBase().replace('.', '-')); // NOI18N
            if (patchdir.isDirectory()) {
                File[] jars = patchdir.listFiles(Util.jarFilter());
                if (jars != null) {
                    for (File patchJar : jars) {
                        if (patches == null) {
                            patches = new HashSet<File>(5);
                        }
                        patches.add(patchJar);
                    }
                } else {
                    Util.err.warning("Could not search for patches in " + patchdir);
                }
            }
            // The following system property is used
            // by XTest, Maven Compile On Save & co.
            // to influence installed modules without changing the build.
            // Format is -Dnetbeans.patches.org.nb.mods.foo=/path/to.file.jar:/path/to/dir
            String patchesClassPath = System.getProperty("netbeans.patches." + getCodeNameBase()); // NOI18N
            if (patchesClassPath != null) {
                StringTokenizer tokenizer = new StringTokenizer(patchesClassPath, File.pathSeparator);
                while (tokenizer.hasMoreTokens()) {
                    String element = tokenizer.nextToken();
                    File fileElement = new File(element);
                    if (fileElement.exists()) {
                        if (patches == null) {
                            patches = new HashSet<File>(15);
                        }
                        patches.add(fileElement);
                    }
                }
            }
            if (Util.err.isLoggable(Level.FINE)) {
                Util.err.log(Level.FINE, "patches of {0}: {1}", new Object[]{jar, patches});
            }
            if (patches != null) {
                for (File patch : patches) {
                    events.log(Events.PATCH, patch);
                }
            }
            if (patches == null) {
                patches = Collections.emptySet();
            }
        }
        
        return patches;
    }
    
    
    /** Check if there is any need to load localized properties.
     * If so, try to load them. Throw an exception if they cannot
     * be loaded for some reason. Uses an open JAR file for the
     * base module at least, though may also open locale variants
     * as needed.
     * Note: due to #19698, this cache is not usually used; only if you
     * specifically go to look at the display properties of a disabled module.
     * @see <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=12549">#12549</a>
     */
    private void loadLocalizedProps(JarFile jarFile, Manifest m) throws IOException {
        String locbundle = m.getMainAttributes().getValue("OpenIDE-Module-Localizing-Bundle"); // NOI18N
        if (locbundle != null) {
            // Something requested, read it in.
            // locbundle is a resource path.
            {
                ZipEntry bundleFile = jarFile.getEntry(locbundle);
                // May not be present in base JAR: might only be in e.g. default locale variant.
                if (bundleFile != null) {
                    localizedProps = new Properties();
                    InputStream is = jarFile.getInputStream(bundleFile);
                    try {
                        localizedProps.load(is);
                    } finally {
                        is.close();
                    }
                }
            }
            {
                // Check also for localized variant JARs and load in anything from them as needed.
                // Note we need to go in the reverse of the usual search order, so as to
                // overwrite less specific bundles with more specific.
                int idx = locbundle.lastIndexOf('.'); // NOI18N
                String name, ext;
                if (idx == -1) {
                    name = locbundle;
                    ext = ""; // NOI18N
                } else {
                    name = locbundle.substring(0, idx);
                    ext = locbundle.substring(idx);
                }
                List<FileWithSuffix> pairs = LocaleVariants.findLocaleVariantsWithSuffixesOf(jar, getCodeNameBase());
                Collections.reverse(pairs);
                for (FileWithSuffix pair : pairs) {
                    File localeJar = pair.file;
                    String suffix = pair.suffix;
                    String rsrc = name + suffix + ext;
                    JarFile localeJarFile = new JarFile(localeJar, false);
                    try {
                        ZipEntry bundleFile = localeJarFile.getEntry(rsrc);
                        // Need not exist in all locale variants.
                        if (bundleFile != null) {
                            if (localizedProps == null) {
                                localizedProps = new Properties();
                            } // else append and overwrite base-locale values
                            InputStream is = localeJarFile.getInputStream(bundleFile);
                            try {
                                localizedProps.load(is);
                            } finally {
                                is.close();
                            }
                        }
                    } finally {
                        localeJarFile.close();
                    }
                }
            }
            if (localizedProps == null) {
                // We should have loaded from at least some bundle in there...
                throw new IOException("Could not find localizing bundle: " + locbundle); // NOI18N
            }
            /* Don't log; too large and annoying:
            if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                Util.err.fine("localizedProps=" + localizedProps);
            }
            */
        }
    }
    
    /** Get all JARs loaded by this module.
     * Includes the module itself, any locale variants of the module,
     * any extensions specified with Class-Path, any locale variants
     * of those extensions.
     * The list will be in classpath order (patches first).
     * Currently the temp JAR is provided in the case of test modules, to prevent
     * sporadic ZIP file exceptions when background threads (like Java parsing) tries
     * to open libraries found in the library path.
     * JARs already present in the classpath are <em>not</em> listed.
     * @return a <code>List&lt;File&gt;</code> of JARs
     */
    @Override
    public List<File> getAllJars() {
        List<File> l = new ArrayList<File>();
        Set<File> ptchs = findPatches();
        if (ptchs != null) l.addAll(ptchs);
        if (physicalJar != null) {
            l.add(physicalJar);
        } else if (jar != null) {
            l.add(jar);
        }
        ((StandardModuleData)data()).addCp(l);
        return l;
    }

    /** Set whether this module is supposed to be reloadable.
     * Has no immediate effect, only impacts what happens the
     * next time it is enabled (after having been disabled if
     * necessary).
     * Must be called from within a write mutex.
     * @param r whether the module should be considered reloadable
     */
    public void setReloadable(boolean r) {
        getManager().assertWritable();
        if (reloadable != r) {
            reloadable = r;
            getManager().fireReloadable(this);
        }
    }
    
    /** Reload this module. Access from ModuleManager.
     * If an exception is thrown, the module is considered
     * to be in an invalid state.
     */
    public void reload() throws IOException {
        // Probably unnecessary but just in case:
        destroyPhysicalJar();
        String codeNameBase1 = getCodeNameBase();
        localizedProps = null;
        loadManifest();
        parseManifest();
    // JST:  reload not solved yet
    // JST   findExtensionsAndVariants(manifest);
        String codeNameBase2 = getCodeNameBase();
        if (! codeNameBase1.equals(codeNameBase2)) {
            throw new InvalidException("Code name base changed during reload: " + codeNameBase1 + " -> " + codeNameBase2); // NOI18N
        }
    }
    
    // Access from ModuleManager:
    /** Turn on the classloader. Passed a list of parent modules to use.
     * The parents should already have had their classloaders initialized.
     */
    protected void classLoaderUp(Set<Module> parents) throws IOException {
        if (Util.err.isLoggable(Level.FINE)) {
            Util.err.fine("classLoaderUp on " + this + " with parents " + parents);
        }
        // Find classloaders for dependent modules and parent to them.
        List<ClassLoader> loaders = new ArrayList<ClassLoader>(parents.size() + 1);
        // This should really be the base loader created by org.nb.Main for loading openide etc.:
        loaders.add(Module.class.getClassLoader());
        for (Module parent: parents) {
            PackageExport[] exports = parent.getPublicPackages();
            if (exports != null && exports.length == 0) {
                // Check if there is an impl dep here.
                boolean implDep = false;
                for (Dependency dep : getDependenciesArray()) {
                    if (dep.getType() == Dependency.TYPE_MODULE &&
                            dep.getComparison() == Dependency.COMPARE_IMPL &&
                            dep.getName().equals(parent.getCodeName())) {
                        implDep = true;
                        break;
                    }
                }
                if (!implDep) {
                    // Nothing exported from here at all, no sense even adding it.
                    // Shortcut; would not harm anything to add it now, but we would
                    // never use it anyway.
                    // Cf. #27853.
                    continue;
                }
            }
            ClassLoader l = getParentLoader(parent);
            if (parent.isFixed() && loaders.contains(l)) {
                Util.err.log(Level.FINE, "#24996: skipping duplicate classloader from {0}", parent);
                continue;
            }
            loaders.add(l);
        }
        List<File> classp = new ArrayList<File>(3);
        Set<File> ptchs = findPatches();
        if (ptchs != null) classp.addAll(ptchs);

        if (reloadable) {
            ensurePhysicalJar();
            // Using OPEN_DELETE does not work well with test modules under 1.4.
            // Random code (URL handler?) still expects the JAR to be there and
            // it is not.
            classp.add(physicalJar);
        } else {
            classp.add(jar);
        }
        
        ((StandardModuleData)data()).addCp(classp);

        // possibly inject some patches
        getManager().refineModulePath(this, classp);
        
        // #27853
        ClassLoader cld = getManager().refineClassLoader(this, loaders);
        // the classloader may be shared, if this module is a fragment
        if (cld != null) {
            classloader = cld;
        } else {
            try {
                classloader = createNewClassLoader(classp, loaders);
            } catch (IllegalArgumentException iae) {
                // Should not happen, but just in case.
                throw (IOException) new IOException(iae.toString()).initCause(iae);
            }
        }
    }
    
    /** Setup a new module with the given class path and the set of parent
     * class loaders.
     */
    protected ClassLoader createNewClassLoader(List<File> classp, List<ClassLoader> parents) {
        return new OneModuleClassLoader(classp, parents.toArray(new ClassLoader[0]));
    }

    /** Get the class loader of a particular parent module. */
    protected ClassLoader getParentLoader(Module parent) {
        return parent.getClassLoader();
    }

    /** Turn off the classloader and release all resources. */
    protected void classLoaderDown() {
        if (classloader instanceof ProxyClassLoader) {
            ((ProxyClassLoader)classloader).destroy();
        }
        classloader = null;
    }
    /** Should be called after turning off the classloader of one or more modules &amp; GC'ing. */
    protected void cleanup() {
        if (isEnabled()) throw new IllegalStateException("cleanup on enabled module: " + this); // NOI18N
        if (classloader != null) throw new IllegalStateException("cleanup on module with classloader: " + this); // NOI18N
        // XXX should this rather be done when the classloader is collected?
        destroyPhysicalJar();
    }
    
    /** Notify the module that it is being deleted. */
    public void destroy() {
        moduleJARs.remove(jar);
    }
    
    /** String representation for debugging. */
    public @Override String toString() {
        String s = "StandardModule:" + getCodeNameBase() + " jarFile: " + jar.getAbsolutePath(); // NOI18N
        if (!isValid()) s += "[invalid]"; // NOI18N
        return s;
    }
    
    /** PermissionCollection with an instance of AllPermission. */
    private static PermissionCollection modulePermissions;
    /** @return initialized @see #modulePermission */
    private static synchronized PermissionCollection getAllPermission() {
        if (modulePermissions == null) {
            modulePermissions = new Permissions();
            modulePermissions.add(new AllPermission());
            modulePermissions.setReadOnly();
        }
        return modulePermissions;
    }
    
    static boolean isModuleJar(File f) {
        return moduleJARs.contains(f);
    }

    private static final Logger CL_LOG = Logger.getLogger(OneModuleClassLoader.class.getName());
    /** Class loader to load a single module.
     * Auto-localizing, multi-parented, permission-granting, the works.
     */
    class OneModuleClassLoader extends JarClassLoader implements Util.ModuleProvider {
        /** Create a new loader for a module.
         * @param classp the List of all module jars of code directories;
         *      includes the module itself, its locale variants,
         *      variants of extensions and Class-Path items from Manifest.
         *      The items are JarFiles for jars and Files for directories
         * @param parents a set of parent classloaders (from other modules)
         */
        public OneModuleClassLoader(List<File> classp, ClassLoader[] parents) throws IllegalArgumentException {
            super(classp, parents, false, StandardModule.this);
            JaveleonModule.registerClassLoader(this, getCodeNameBase());
        }
        
        public Module getModule() {
            return StandardModule.this;
        }
        
        /** Inherited.
         * @param cs is ignored
         * @return PermissionCollection with an AllPermission instance
         */
        protected @Override PermissionCollection getPermissions(CodeSource cs) {
            return getAllPermission();
        }
        
        /**
         * Look up a native library as described in modules documentation.
         * @see http://bits.netbeans.org/dev/javadoc/org-openide-modules/org/openide/modules/doc-files/api.html#jni
         */
        protected @Override String findLibrary(String libname) {
            InstalledFileLocator ifl = InstalledFileLocator.getDefault();
            String arch = System.getProperty("os.arch"); // NOI18N
            String system = System.getProperty("os.name").toLowerCase(Locale.ENGLISH); // NOI18N
            String mapped = System.mapLibraryName(libname);
            File lib;

            lib = ifl.locate("modules/lib/" + mapped, getCodeNameBase(), false); // NOI18N
            if (lib != null) {
                CL_LOG.log(Level.FINE, "found {0}", lib);
                return lib.getAbsolutePath();
            }

            lib = ifl.locate("modules/lib/" + arch + "/" + mapped, getCodeNameBase(), false); // NOI18N
            if (lib != null) {
                CL_LOG.log(Level.FINE, "found {0}", lib);
                return lib.getAbsolutePath();
            }

            lib = ifl.locate("modules/lib/" + arch + "/" + system + "/" + mapped, getCodeNameBase(), false); // NOI18N
            if (lib != null) {
                CL_LOG.log(Level.FINE, "found {0}", lib);
                return lib.getAbsolutePath();
            }
            
            if( BaseUtilities.isMac() ) {
                String jniMapped = mapped.replaceFirst("\\.dylib$",".jnilib");
                lib = ifl.locate("modules/lib/" + jniMapped, getCodeNameBase(), false); // NOI18N
                if (lib != null) {
                    CL_LOG.log(Level.FINE, "found {0}", lib);
                    return lib.getAbsolutePath();
                }

                lib = ifl.locate("modules/lib/" + arch + "/" + jniMapped, getCodeNameBase(), false); // NOI18N
                if (lib != null) {
                    CL_LOG.log(Level.FINE, "found {0}", lib);
                    return lib.getAbsolutePath();
                }

                lib = ifl.locate("modules/lib/" + arch + "/" + system + "/" + jniMapped, getCodeNameBase(), false); // NOI18N
                if (lib != null) {
                    CL_LOG.log(Level.FINE, "found {0}", lib);
                    return lib.getAbsolutePath();
                }
                CL_LOG.log(Level.FINE, "found nothing like modules/lib/{0}/{1}/{2}", new Object[] {arch, system, jniMapped});
            }

            CL_LOG.log(Level.FINE, "found nothing like modules/lib/{0}/{1}/{2}", new Object[] {arch, system, mapped});
            return null;
        }

        protected @Override boolean shouldDelegateResource(String pkg, ClassLoader parent) {
            if (!super.shouldDelegateResource(pkg, parent)) {
                return false;
            }
            Module other;
            if (parent instanceof Util.ModuleProvider) {
                other = ((Util.ModuleProvider)parent).getModule();
            } else {
                other = null;
            }
            return getManager().shouldDelegateResource(StandardModule.this, other, pkg, parent);
        }
        
        public @Override String toString() {
            return "ModuleCL@" + Integer.toHexString(System.identityHashCode(this)) + "[" + getCodeNameBase() + "]"; // NOI18N
        }
    }
}
