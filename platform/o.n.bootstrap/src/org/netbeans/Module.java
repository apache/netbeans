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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.Union2;

/** Object representing one module, possibly installed.
 * Responsible for opening of module JAR file; reading
 * manifest; parsing basic information such as dependencies;
 * and creating a classloader for use by the installer.
 * Methods not defined in ModuleInfo must be called from within
 * the module manager's read mutex as a rule.
 * @author Jesse Glick
 * @since 2.1 the class was made public abstract
 */
public abstract class Module extends ModuleInfo {
    
    public static final String PROP_RELOADABLE = "reloadable"; // NOI18N
    public static final String PROP_CLASS_LOADER = "classLoader"; // NOI18N
    public static final String PROP_MANIFEST = "manifest"; // NOI18N
    public static final String PROP_VALID = "valid"; // NOI18N
    public static final String PROP_PROBLEMS = "problems"; // NOI18N

    /** manager which owns this module */
    protected final ModuleManager mgr;
    /** event logging (should not be much here) */
    protected final Events events;
    /** associated history object
     * @see ModuleHistory
     */
    private final Object history;
    /** true if currently enabled; manipulated by ModuleManager */
    private boolean enabled;
    /** whether it is supposed to be automatically loaded when required */
    private final boolean autoload;
    /** */
    protected boolean reloadable;
    /** if true, this module is eagerly turned on whenever it can be */
    private final boolean eager;
    /** currently active module classloader */
    protected ClassLoader classloader;

    private ModuleData data;
    private NbInstrumentation instr;
    
    private static Method findResources;
    private static final Object DATA_LOCK = new Object();

    /** Use ModuleManager.create as a factory. */
    protected Module(ModuleManager mgr, Events ev, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException {
        if (autoload && eager) throw new IllegalArgumentException("A module may not be both autoload and eager"); // NOI18N
        this.mgr = mgr;
        this.events = ev;
        this.history = history;
        this.reloadable = reloadable;
        this.autoload = autoload;
        this.eager = eager;
        this.enabled = false;
    }
    
    /** Create a special-purpose "fixed" JAR. */
    protected Module(ModuleManager mgr, Events ev, Object history, ClassLoader classloader) throws InvalidException {
        this(mgr, ev, history, classloader, false, false);
    }
    
    /**
     * Create a special-purpose "fixed" JAR which may nonetheless be marked eager or autoload.
     * @since 2.7
     */
    protected Module(ModuleManager mgr, Events ev, Object history, ClassLoader classloader, boolean autoload, boolean eager) throws InvalidException {
        if (autoload && eager) throw new IllegalArgumentException("A module may not be both autoload and eager"); // NOI18N
        this.mgr = mgr;
        this.events = ev;
        this.history = history;
        this.classloader = classloader;
        reloadable = false;
        this.autoload = autoload;
        this.eager = eager;
        enabled = false;
    }
    
    ModuleData createData(ObjectInput in, Manifest mf) throws IOException {
        if (in != null) {
            return new ModuleData(in);
        } else {
            return new ModuleData(mf, this);
        }
    }
    
    final void writeData(ObjectOutput out) throws IOException {
        data().write(out);
    }
    
    final ModuleData data() {
        try {
            return dataWithCheck();
        } catch (InvalidException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    final ModuleData dataWithCheck() throws InvalidException {
        synchronized (DATA_LOCK) {
            if (data != null) {
                return data;
            }
            Util.err.log(Level.FINE, "Initialize data {0}", getJarFile()); // NOI18N
            InputStream is = mgr.dataFor(getJarFile());
            if (is != null) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(is);
                    ModuleData mine = createData(ois, null);
                    ois.close();
                    assert data == null;
                    data = mine;
                    return mine;
                } catch (IOException ex) {
                    Util.err.log(Level.INFO, "Cannot read cache for " + getJarFile(), ex); // NOI18N
                }
            }
            try {
                ModuleData mine = createData(null, getManifest());
                assert mine == data;
                return mine;
            } catch (InvalidException ex) {
                throw ex;
            } catch (IOException ex) {
                // no I/O needed when reading from manifest
                throw new IllegalStateException(ex);
            }
        }
    }
    
    final void assignData(ModuleData data) {
        assert Thread.holdsLock(DATA_LOCK);
        this.data = data;
    }
    
    /** Get the associated module manager. */
    public ModuleManager getManager() {
        return mgr;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    // Access from ModuleManager:
    void setEnabled(boolean enabled) {
        /* #13647: actually can happen if loading of bootstrap modules is rolled back: */
        if (isFixed() && ! enabled) throw new IllegalStateException("Cannot disable a fixed module: " + this); // NOI18N
        this.enabled = enabled;
    }
    
    /** Normally a module once created and managed is valid
     * (that is, either installed or not, but at least managed).
     * If it is deleted any remaining references to it become
     * invalid.
     */
    public boolean isValid() {
        return mgr.get(getCodeNameBase()) == this;
    }
    
    /** Is this module automatically loaded?
     * If so, no information about its state is kept
     * permanently beyond the existence of its JAR file;
     * it is enabled when some real module needs it to be,
     * and disabled when this is no longer the case.
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=9779">#9779</a>
     */
    public boolean isAutoload() {
        return autoload;
    }
    
    /** Is this module eagerly enabled?
     * If so, no information about its state is kept permanently.
     * It is turned on whenever it can be, i.e. whenever it meets all of
     * its dependencies. This may be used to implement "bridge" modules with
     * simple functionality that just depend on two normal modules.
     * A module may not be simultaneously eager and autoload.
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=17501">#17501</a>
     * @since org.netbeans.core/1 1.3
     */
    public boolean isEager() {
        return eager;
    }
    
    /** Get an associated arbitrary attribute.
     * Right now, simply provides the main attributes of the manifest.
     * In the future some of these could be suppressed (if only of dangerous
     * interest, e.g. Class-Path) or enhanced with other information available
     * from the core (if needed).
     */
    @Override
    public Object getAttribute(String attr) {
        return getManifest().getMainAttributes().getValue(attr);
    }
    
    @Override
    public String getCodeName() {
        return data().getCodeName();
    }
    
    String getFragmentHostCodeName() {
        String fragmentHostCodeName = mgr.fragmentFor(getJarFile());
        if (fragmentHostCodeName != null) {
            return fragmentHostCodeName.isEmpty() ? null : fragmentHostCodeName;
        }
        try {
            fragmentHostCodeName = data().getFragmentHostCodeName();
        } catch (IllegalStateException ex) {
            fragmentHostCodeName = null;
        }
        return fragmentHostCodeName;
    }
    
    @Override
    public String getCodeNameBase() {
        String cnb = mgr.cnbFor(getJarFile());
        if (cnb != null) {
            return cnb;
        }
        return data().getCodeNameBase();
    }
    
    @Override
    public int getCodeNameRelease() {
        return data().getCodeNameRelease();
    }
    
    public @Override String[] getProvides() {
        return data().getProvides();
    }
    /** Test whether the module provides a given token or not. 
     * @since JST-PENDING again used from NbProblemDisplayer
     */
    public final boolean provides(String token) {
        String[] provides = getProvides();
        if (provides == null) {
            return false;
        }
        for (int i = 0; i < provides.length; i++) {
            if (provides[i].equals(token)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Set<Dependency> getDependencies() {
        return new HashSet<Dependency>(Arrays.asList(getDependenciesArray()));
    }
    public final Dependency[] getDependenciesArray() {
        Dependency[] dependenciesA;
        try {
            dependenciesA = data().getDependencies();
        } catch (IllegalStateException ex) {
            dependenciesA = null;
        }
        return dependenciesA == null ? new Dependency[0] : dependenciesA;
    }
    
    @Override
    public SpecificationVersion getSpecificationVersion() {
        return data().getSpecificationVersion();
    }

    @Override
    public String getImplementationVersion() {
        return data().getImplementationVersion();
    }

    @Override
    public String getBuildVersion() {
        return data().getBuildVersion();
    }
    
    
    
    public @Override boolean owns(Class<?> clazz) {
        ClassLoader cl = clazz.getClassLoader();
        if (cl instanceof Util.ModuleProvider) {
            return ((Util.ModuleProvider) cl).getModule() == this;
        }
        if (cl != classloader) {
            return false;
        }
        String _codeName = findClasspathModuleCodeName(clazz);
        if (_codeName != null) {
            return _codeName.equals(getCodeName());
        }
        return true; // not sure...
    }
    
    static String findClasspathModuleCodeName(Class<?> clazz) {
        // #157798: in JNLP or otherwise classpath mode, all modules share a CL.
        CodeSource src = clazz.getProtectionDomain().getCodeSource();
        if (src != null) {
            try {
                URL loc = src.getLocation();
                if (loc.toString().matches(".+\\.jar")) {
                    // URLClassLoader inconsistency.
                    loc = new URL("jar:" + loc + "!/");
                }
                URL manifest = new URL(loc, "META-INF/MANIFEST.MF");
                InputStream is = manifest.openStream();
                try {
                    return new Manifest(is).getMainAttributes().getValue("OpenIDE-Module");
                } finally {
                    is.close();
                }
            } catch (IOException x) {
                Logger.getLogger(Module.class.getName()).log(Level.FINE, null, x);
            }
        }
        return null;
    }
    
    /** Get all packages exported by this module to other modules.
     * @return a list (possibly empty) of exported packages, or null to export everything
     * @since org.netbeans.core/1 > 1.4
     * @see "#19621"
     */
    public PackageExport[] getPublicPackages() {
        return data().getPublicPackages();
    }
    
    /** Checks whether we use friends attribute and if so, then
     * whether the name of module is listed there.
     */
    boolean isDeclaredAsFriend (Module module) {
        Set<String> friendNames = data().getFriendNames();
        if (friendNames == null) {
            return true;
        }
        return friendNames.contains(module.getCodeNameBase());
    }
    
    /** Parse information from the current manifest.
     * Includes code name, specification version, and dependencies.
     * If anything is in an invalid format, throws an exception with
     * some kind of description of the problem.
     */
    protected void parseManifest() throws InvalidException {
        data();
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
     * @return a list of JARs
     */
    public abstract List<File> getAllJars();

    /** Is this module supposed to be easily reloadable?
     * If so, it is suitable for testing inside the IDE.
     * Controls whether a copy of the JAR file is made before
     * passing it to the classloader, which can affect locking
     * and refreshing of the JAR.
     */
    public boolean isReloadable() {
        return reloadable;
    }
    
    /** Set whether this module is supposed to be reloadable.
     * Has no immediate effect, only impacts what happens the
     * next time it is enabled (after having been disabled if
     * necessary).
     * Must be called from within a write mutex.
     * @param r whether the module should be considered reloadable
     */
    public abstract void setReloadable(boolean r);

    /** Reload this module. Access from ModuleManager.
     * If an exception is thrown, the module is considered
     * to be in an invalid state.
     * @since JST-PENDING: needed from ModuleSystem
     */
    public abstract void reload() throws IOException;
    
    // impl of ModuleInfo method
    public @Override ClassLoader getClassLoader() throws IllegalArgumentException {
        if (!enabled) {
            throw new IllegalArgumentException("Not enabled: " + getCodeNameBase()); // NOI18N
        }
        assert classloader != null : "Should have had a non-null loader for " + this;
        return classloader;
    }

    // Access from ModuleManager:
    /** Turn on the classloader. Passed a list of parent modules to use.
     * The parents should already have had their classloaders initialized.
     */
    protected abstract void classLoaderUp(Set<Module> parents) throws IOException;

    /** Turn off the classloader and release all resources. */
    protected abstract void classLoaderDown();
    /** Should be called after turning off the classloader of one or more modules & GC'ing. */
    protected abstract void cleanup();
    
    /** Notify the module that it is being deleted. */
    protected abstract void destroy();
    
    /**
     * Fixed modules are treated differently.
     * @see FixedModule
     */
    public abstract boolean isFixed();
    
    /** Get the JAR this module is packaged in.
     * May be null for modules installed specially, e.g.
     * automatically from the classpath.
     * @see #isFixed
     */
    public File getJarFile() {
        return null;
    }

    /** Get the JAR manifest.
     * Should never be null, even if disabled.
     * Might change if a module is reloaded.
     * It is not guaranteed that change events will be fired
     * for changes in this property.
     */
    public abstract Manifest getManifest();

    /**
     * Release memory storage for the JAR manifest, if applicable.
     */
    public void releaseManifest() {}
    
    /** Get a set of {@link org.openide.modules.Dependency} objects representing missed dependencies.
     * This module is examined to see
     * why it would not be installable.
     * If it is enabled, there are no problems.
     * If it is in fact installable (possibly only
     * by also enabling some other managed modules which are currently disabled), and
     * all of its non-module dependencies are met, the returned set will be empty.
     * Otherwise it will contain a list of reasons why this module cannot be installed:
     * non-module dependencies which are not met; and module dependencies on modules
     * which either do not exist in the managed set, or are the wrong version,
     * or themselves cannot be installed
     * for some reason or another (which may be separately examined).
     * Note that in the (illegal) situation of two or more modules forming a cyclic
     * dependency cycle, none of them will be installable, and the missing dependencies
     * for each will be stated as the dependencies on the others. Again other modules
     * dependent on modules in the cycle will list failed dependencies on the cyclic modules.
     * Missing package dependencies are not guaranteed to be reported unless an install
     * of the module has already been attempted, and failed due to them.
     * The set may also contain {@link InvalidException}s representing known failures
     * of the module to be installed, e.g. due to classloader problems, missing runtime
     * resources, or failed ad-hoc dependencies. Again these are not guaranteed to be
     * reported unless an install has already been attempted and failed due to them.
     */
    public Set<Object> getProblems() { // cannot use Union2<Dependency,InvalidException> without being binary-incompatible
        if (! isValid()) throw new IllegalStateException("Not valid: " + this); // NOI18N
        if (isEnabled()) return Collections.emptySet();
        Set<Object> problems = new HashSet<Object>();
        for (Union2<Dependency,InvalidException> problem : mgr.missingDependencies(this)) {
            if (problem.hasFirst()) {
                problems.add(problem.first());
            } else {
                problems.add(problem.second());
            }
        }
        return problems;
    }
    
    // Access from ChangeFirer:
    final void firePropertyChange0(String prop, Object old, Object nue) {
        if (Util.err.isLoggable(Level.FINE)) {
            Util.err.log(Level.FINE, "Module.propertyChange: {0} {1}: {2} -> {3}", new Object[]{this, prop, old, nue});
        }
        firePropertyChange(prop, old, nue);
    }
    
    /** Get the history object representing what has happened to this module before.
     * @see org.netbeans.core.startup.ModuleHistory
     */
    public final Object getHistory() {
        return history;
    }

    /** Finds out if a module has been assigned with a specific start level.
     * Start level is only useful for OSGi bundles. Otherwise it is always zero.
     * 
     * @return -1, if no specific level is assigned, non-negative integer if so
     * @since 2.43
     */
    public final int getStartLevel() {
        return getStartLevelImpl();
    }
    
    int getStartLevelImpl() {
        return -1;
    }
    
    /** String representation for debugging. */
    public @Override String toString() {
        String s = "Module:" + getCodeNameBase(); // NOI18N
        if (!isValid()) s += "[invalid]"; // NOI18N
        return s;
    }

    /** Locates resource in this module. May search only the main JAR
     * of the module (which is what it does in case of OSGi bundles). 
     * Should be as lightweight as possible - e.g. if it is OK to not
     * initialize something in the module while performing this method,
     * the something should not be initialized (e.g. OSGi bundles are
     * not resolved).
     * 
     * @param resources path to the resources we are looking for
     * @since 2.49
     */
    public Enumeration<URL> findResources(String resources) {
        try { // #149136
            // Cannot use getResources because we do not wish to delegate to parents.
            // In fact both URLClassLoader and ProxyClassLoader override this method to be public.
            if (findResources == null) {
                findResources = ClassLoader.class.getDeclaredMethod("findResources", String.class); // NOI18N
                findResources.setAccessible(true);
            }
            ClassLoader cl = getClassLoader();
            @SuppressWarnings("unchecked")
            Enumeration<URL> en = (Enumeration<URL>) findResources.invoke(cl, resources); // NOI18N
            return en;
        } catch (Exception x) {
            Exceptions.printStackTrace(x);
            return Enumerations.empty();
        }
    }

    /** To be overriden to empty in FixedModule & co. */
    void refineDependencies(Set<Dependency> dependencies) {
        // Permit the concrete installer to make some changes:
        mgr.refineDependencies(this, dependencies);
    }

    void registerCoveredPackages(Set<String> known) {
        data().registerCoveredPackages(known);
    }

    Set<String> getCoveredPackages() {
        return data().getCoveredPackages();
    }

    /** Is this module a wrapper around OSGi?
     * @return true, if the module is build around OSGi
     * @since 2.51
     */
    public final boolean isNetigso() {
        return isNetigsoImpl();
    }
    
    boolean isNetigsoImpl() {
        return false;
    }

    final void assignInstrumentation(NbInstrumentation agent) {
        instr = agent;
    }

    void unregisterInstrumentation() {
        NbInstrumentation.unregisterAgent(instr);
    }

    /** Struct representing a package exported from a module.
     * @since org.netbeans.core/1 > 1.4
     * @see Module#getPublicPackages
     */
    public static final class PackageExport {
        /** Package to export, in the form <samp>org/netbeans/modules/foo/</samp>. */
        public final String pkg;
        /** If true, export subpackages also. */
        public final boolean recursive;
        /** Create a package export struct with the named parameters. */
        public PackageExport(String pkg, boolean recursive) {
            this.pkg = pkg;
            this.recursive = recursive;
        }
        public @Override String toString() {
            return "PackageExport[" + pkg + (recursive ? "**/" : "") + "]"; // NOI18N
        }
        public @Override boolean equals(Object obj) {
            if (!(obj instanceof PackageExport)) {
                return false;
            }
            final PackageExport other = (PackageExport) obj;
            return pkg.equals(other.pkg) && recursive == other.recursive;
        }
        public @Override int hashCode() {
            return pkg.hashCode();
        }
        
        static void write(DataOutput dos, PackageExport[] arr) throws IOException {
            if (arr == null) {
                dos.writeInt(0);
                return;
            }
            dos.writeInt(arr.length);
            for (PackageExport pe : arr) {
                dos.writeUTF(pe.pkg);
                dos.writeBoolean(pe.recursive);
            }
        }
        
        static PackageExport[] read(DataInput is) throws IOException {
            int cnt = is.readInt();
            if (cnt == 0) {
                return null;
            }
            PackageExport[] arr = new PackageExport[cnt];
            for (int i = 0; i < cnt; i++) {
                String pkg = is.readUTF();
                boolean recursive = is.readBoolean();
                arr[i] = new PackageExport(pkg, recursive);
            }
            return arr;
        }
    }
}
