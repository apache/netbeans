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
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.logging.Level;
import org.openide.modules.ModuleInfo;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/** This class contains abstracted calls to OSGi provided by core.netigso
 * module. No other module can implement this class, except core.netigso.
 *
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 */
public abstract class NetigsoFramework {
    private ModuleManager mgr;
    
    protected NetigsoFramework() {
        if (!getClass().getName().equals("org.netbeans.core.netigso.Netigso")) { // NOI18N
            throw new IllegalStateException();
        }
    }
    
    final NetigsoFramework bindTo(ModuleManager mgr) {
        try {
            NetigsoFramework nf = (NetigsoFramework)clone();
            assert nf != this;
            nf.mgr = mgr;
            return nf;
        } catch (CloneNotSupportedException ex) {
            Util.err.log(Level.INFO, null, ex);
            this.mgr = mgr;
            return this;
        }
    }

    /** Starts the framework.
     */
    protected abstract void prepare(
        Lookup loadFrameworkFrom,
        Collection<? extends Module> preregister
    );

    /** Get's ready for start of the OSGi framework.
     * @param allModules the modules that are in the system
     * @return returns set of additional modules (usually autoloads that need to be turned on)
     * @since 2.31 it returns set of module code name bases
     */
    protected abstract Set<String> start(Collection<? extends Module> allModules);
    
    /** Starts the OSGi framework by activating all bundles that shall be activated.
     * @since 2.35
     */
    protected abstract void start();

    /** Shutdowns the framework */
    protected abstract void shutdown();

    /** Initializes a classloader for given module.
     * @param m the module description
     * @param pcl proxy classloader that shall be configured
     * @param jar the module JAR file
     * @return set of covered packages
     */
    protected abstract Set<String> createLoader(
        ModuleInfo m, ProxyClassLoader pcl, File jar
    ) throws IOException;

    /**
     * Find given resource inside provide module representing an OSGi bundle.
     * The search should happen without resolving the bundle, if possible. E.g.
     * by using <code>Bundle.getEntry</code>.
     * @param resName  name of the resource to seek for
     * @return empty enumeration or enumeration with one element.
     * @since 2.49
     */
    protected Enumeration<URL> findResources(Module module, String resName) {
        return Enumerations.empty();
    }

    
    /** Reloads one module
     * @since 2.27
     */
    protected abstract void reload(Module m) throws IOException;

    /** Deinitializes a classloader for given module */
    protected abstract void stopLoader(ModuleInfo m, ClassLoader loader);

    /** Allows the OSGi support to identify the classloader that loads
     * all OSGi framework classes.
     * 
     * @since 2.37
     */
    protected ClassLoader findFrameworkClassLoader() {
        return getClass().getClassLoader();
    }

    /** Default start level for all bundles that don't specify any own.
     * 
     * @since 2.44.2
     * @return 
     */
    protected int defaultStartLevel() {
        return 0;
    }
    
    //
    // Access to Archive
    //
    
    /** Get an array of bytes from archive. If not found, it remembers the
     * request and later calls <code>#toArchive(java.lang.String, java.lang.String)</code>
     * method to store it for next time.
     *
     * @param name name of the resource inside the JAR
     * @param resources the provider of the real resources
     * @return either cached value or the one returned by resources (or null)
     * @throws IOException if something goes wrong
     * @since 2.29
     */
    protected final byte[] fromArchive(ArchiveResources resources, String name) throws IOException {
        return JarClassLoader.archive.getData(resources, name);
    }

    /** Creates a delegating loader for given module. The loader is suitable
     * for use as a delegating loader for a fake OSGi bundle.
     * @param cnb name of the bundle/module
     * @return classloader or null if the module does not exist
     * @since 2.50
     */
    protected final ClassLoader createClassLoader(String cnb) {
        Module m = findModule(cnb);
        return m == null ? null : new NetigsoLoader(m);
    }
    
    /** Finds module for given name.
     * @param cnb code name base of the module
     * @return the module or null
     * @since 2.50
     */
    protected final Module findModule(String cnb) {
        return mgr.get(cnb);
    }
    
    /** Gives OSGi support access to NetBeans bytecode manipulation libraries.
     * They are built over {@link Instrumentation} specification 
     * read more at <a href="@TOP@/architecture-summary.html#usecase-patch">
     * architecture document</a>.
     * 
     * @param l the classloader that is loading the class
     * @param className the class name
     * @param pd protection domain to use
     * @param arr bytecode (must not be modified)
     * @return same or alternative bytecode for the class
     * @throws IllegalStateException if the byte code is not valid
     * @since 2.65
     */
    protected final byte[] patchByteCode(ClassLoader l, String className, ProtectionDomain pd, byte[] arr) {
        try {
            return NbInstrumentation.patchByteCode(l, className, null, arr);
        } catch (IllegalClassFormatException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
}
