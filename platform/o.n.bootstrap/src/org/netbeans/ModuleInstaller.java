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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.openide.modules.Dependency;
import org.openide.util.Task;

/** Responsible for actually installing the contents of module JARs into the IDE.
 * While the manager tracks which modules are enabled and their dependencies,
 * the installer actually understands the manifest contents and is able to
 * add layers, sections, etc. to the IDE's runtime.
 * @author Jesse Glick
 */
public abstract class ModuleInstaller {
    
    /** No-op constructor for subclasses. */
    protected ModuleInstaller() {}
    
    /** Called to ensure that a module is valid to be installed, before continuing.
     * Should examine the manifest and check its syntax (may keep cached parse
     * information about that syntax). When this method is called, the module's
     * classloader should be ready for use, but the system classloader will not
     * yet contain it. It may (but need not) try to load
     * mentioned resources from that classloader in order to catch errors early
     * on. For some resources it may be preferable to load them lazily.
     * If the installer indicates the module is invalid, it will not be installed.
     * InvalidException thrown from here must include a reference to the module.
     */
    public abstract void prepare(Module m) throws InvalidException;
    
    /** Called when a module is being uninstalled and runtime information
     * about it is no longer needed.
     * The installer should remove any associated cache entries and try
     * to release anything that might have been loaded with the module's
     * classloader. The classloader may still be valid at this point but
     * better not to use it.
     */
    public abstract void dispose(Module m);

    /** Tells the installer that new classloader is ready for use.
     * This does not mean that all modules are ready to be used yet,
     * but the classloader will be allowed to load classes from those
     * modules already prepared.
     * @since 2.32
     */
    protected void classLoaderUp(ClassLoader cl) {
    }
    
    /** Actually load some modules into the IDE.
     * They will all have been prepared already, and their classloaders
     * will be contained in the system classloader.
     * It is expected to handle any resultant exceptions appropriately
     * (e.g. skipping over a module, or skipping over that piece of a module).
     * Where load order is significant, items should be installed in the order
     * supplied: starting with basic modules and ending with dependent modules.
     * Installers may choose to install all resources of a certain type (e.g.
     * layers) from every module, then all of the next type, etc., for purposes
     * of efficiency.
     * Note that "loading" could really mean installing or restoring or upgrading,
     * depending on module history.
     */
    public abstract void load(List<Module> modules);
    
    /** Unload some modules from the IDE.
     * Where unload order is significant, items should be uninstalled in the order
     * supplied: starting with dependent modules and ending with basic modules.
     * Module classloaders will still be in the system classloader.
     */
    public abstract void unload(List<Module> modules);
    
    /** Ask to shut down the IDE from a set of modules.
     * Will begin with dependent and end with basic modules.
     */
    public abstract boolean closing(List<Module> modules);
    
    /** Notify modules the IDE will be shut down.
     * Will begin with dependent and end with basic modules.
     */
    public abstract void close(List<Module> modules);
    
    /** Initializes closing sequence on given modules. Certain
     * operations may remain unfinished and can be carried out in 
     * parallel. A {@link Task} is returned for callers to wait
     * till closing sequence is successfully finished.
     * 
     * @since 2.56
     * @param modules list of modules to close
     * @return 
     */
    public Task closeAsync(List<Module> modules) {
        close(modules);
        return Task.EMPTY;
    }
    
    /** Optionally refine the dependencies for a module.
     * For example, an installer might decide to automatically add a dependency
     * on a "stock" library module for all client modules meeting some criterion.
     * The default implementation makes no change.
     * @param m a module to possibly refine dependencies for; overriders must not call
     *          getDependencies on this module nor attempt to directly change it
     *          in any way; overriders may ask for module code name, version, etc.
     * @param dependencies a set of Dependency's; mutable, entries may be added or removed
     *                     during the dynamic scope of this call
     * @since org.netbeans.core/1 1.2
     */
    public void refineDependencies(Module m, Set<Dependency> dependencies) {}
    
    /** Optionally mask package use in a module classloader.
     * For example, an installer might decide that a module may not use a
     * package in the application classpath because it is an unguaranteed
     * implementation package and the module has not explicitly requested
     * to use it.
     * <p>The module system automatically excludes improper access
     * to non-public packages (as declared via <code>OpenIDE-Module-Public-Packages</code>)
     * and cross-module access to the <code>META-INF/</code> directory.
     * <p>The default implementation of this method permits all other
     * package delegation.
     * @param m the module requesting use of a given package
     * @param parent the module which might possibly supply that package, or
     *               null if the possible provider is not a module (i.e. application
     *               classpath)
     * @param pkg the name of the package in use, in the form "org/netbeans/modules/foo/"
     *            (i.e. slash-separated and ending in a slash as well)
     * @since org.netbeans.core/1 1.3
     */
    public boolean shouldDelegateResource(Module m, Module parent, String pkg) {
        return true;
    }

    /**
     * Similar to {@link #shouldDelegateResource} but checks whether the
     * {@linkplain ModuleManager#getClassLoader() system class loader} should delegate to the classpath.
     * @param pkg as in {@link #shouldDelegateResource}
     * @return true if it is acceptable to delegate to startup JARs or the JRE
     * @since org.netbeans.bootstrap/1 2.17
     */
    public boolean shouldDelegateClasspathResource(String pkg) {
        return true;
    }
    
    /** Scan a disabled module JAR file for its manifest contents.
     * Subclasses may implement this efficiently, e.g. to use a special cache.
     * <p>The default implementation simply opens the JAR and gets its manifest
     * using the standard JRE calls.
     * <p>Never called for reloadable JARs.
     * @param jar a module JAR to open
     * @return its manifest
     * @throws IOException if the JAR cannot be opened or does not have a manifest at all
     * @since org.netbeans.core/1 1.5
     * @see "#26786"
     */
    public Manifest loadManifest(File jar) throws IOException {
        JarFile jarFile = new JarFile(jar, false);
        try {
            Manifest m = jarFile.getManifest();
            if (m == null) throw new IOException("No manifest found in " + jar); // NOI18N
            return m;
        } finally {
            jarFile.close();
        }
    }
    
    /** Permit a module installer to add extra parent classloaders for a module.
     * Called during enablement of a module.
     * The default implementation does nothing.
     * @param m a module which is about to be enabled
     * @param parents current list of <code>ClassLoader</code> parents; may be mutated (appended to)
     * @since org.netbeans.core/1 > 1.6
     * @see "#27853"
     */
    public void refineClassLoader(Module m, List<? extends ClassLoader> parents) {
        // do nothing
    }

    /** Optionally adds additional token for the module.
     * @param m the module to add token to 
     * @return null or list of tokens this module provides
     * @since org.netbeans.core/1 > 1.25
     * @see "#46833"
     */
    public String[] refineProvides (Module m) {
        return null;
    }

    /** Loads dependencies cached from previous run, if possible.
     * @param cnb the code name base of the module to get dependencies for
     * @return null or set of dependencies for the module
     * @since 2.18
     */
    protected Set<Dependency> loadDependencies(String cnb) {
        return null;
    }

}
