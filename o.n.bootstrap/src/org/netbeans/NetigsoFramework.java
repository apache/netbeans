/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
 * @author Jaroslav Tulach <jtulach@netbeans.org>
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
     * @parma m the module description
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
     * request and later calls {@link #toArchive(java.lang.String, java.lang.String)}
     * method to store it for next time.
     *
     * @param name name of the resource inside the JAR
     * @parma resources the provider of the real resources
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
     * read more at <a href="@TOP@architecture-overview.html#usecase-bytecode.patching">
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
