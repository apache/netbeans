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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;

/**
 * A class loader that has multiple parents and uses them for loading
 * classes and resources. It is optimized for working in the enviroment 
 * of a deeply nested classloader hierarchy. It uses shared knowledge 
 * about package population to route the loading request directly 
 * to the correct classloader. 
 * It doesn't load classes or resources itself, but allows subclasses
 * to add such functionality.
 * 
 * @author  Petr Nejedly, Jesse Glick
 */
public class ProxyClassLoader extends ClassLoader {

    private static final Logger LOGGER = Logger.getLogger(ProxyClassLoader.class.getName());
    private static final boolean LOG_LOADING;
    private static final ClassLoader TOP_CL = ProxyClassLoader.class.getClassLoader();

    static {
        boolean prop1 = System.getProperty("org.netbeans.ProxyClassLoader.level") != null;
        LOG_LOADING = prop1 || LOGGER.isLoggable(Level.FINE);
    }

    /** All known packages 
     * @GuardedBy("packages")
     */
    private final ConcurrentMap<String, Package> packages = new ConcurrentHashMap<>();

    /** keeps information about parent classloaders, system classloader, etc.*/
    volatile ProxyClassParents parents;
    
    private BiFunction<String, ClassLoader, Boolean> delegatingPredicate;

    /** Create a multi-parented classloader.
     * @param parents all direct parents of this classloader, except system one.
     * @param transitive whether other PCLs depending on this one will
     *                   automatically search through its parent list
     */
    public ProxyClassLoader(ClassLoader[] parents, boolean transitive) {
        super(TOP_CL);
        this.parents = ProxyClassParents.coalesceParents(this, parents, TOP_CL, transitive);
    }
    
    /** Create a multi-parented classloader.
     * @param parents all direct parents of this classloader, except system one.
     * @param transitive whether other PCLs depending on this one will
     *                   automatically search through its parent list
     */
    public ProxyClassLoader(ClassLoader[] parents, boolean transitive, BiFunction<String, ClassLoader, Boolean> delegatingPredicate) {
        super(TOP_CL);
        this.parents = ProxyClassParents.coalesceParents(this, parents, TOP_CL, transitive);
        this.delegatingPredicate = delegatingPredicate;
    }
    
    protected final void addCoveredPackages(Iterable<String> coveredPackages) {
        ProxyClassPackages.addCoveredPackages(this, coveredPackages);
    }
    
    // this is used only by system classloader, maybe we can redesign it a bit
    // to live without this functionality, then destroy may also go away
    /** Add new parents dynamically.
     * @param nueparents the new parents to add (append to list)
     * @throws IllegalArgumentException in case of a null or cyclic parent (duplicate OK)
     */
    public void append(ClassLoader[] nueparents) throws IllegalArgumentException {
        if (nueparents == null) throw new IllegalArgumentException("null parents array"); // NOI18N
        
        for (ClassLoader cl : nueparents) {
            if (cl == null) throw new IllegalArgumentException("null parent: " + Arrays.asList(nueparents)); // NOI18N
        }
        
        ModuleFactory moduleFactory = Lookup.getDefault().lookup(ModuleFactory.class);
        if (moduleFactory != null && moduleFactory.removeBaseClassLoader()) {
            // this hack is here to prevent having the application classloader
            // as parent to all module classloaders.
            parents = ProxyClassParents.coalesceParents(this, nueparents, ClassLoader.getSystemClassLoader(), parents.isTransitive());
        } else {
            parents = parents.append(this, nueparents);
        }
    }
         
    /**
     * Loads the class with the specified name.  The implementation of
     * this method searches for classes in the following order:
     * <ol>
     * <li> Looks for a known package and pass the loading to the ClassLoader 
            for that package. 
     * <li> For unknown packages passes the call directly 
     *      already been loaded.
     * </ol>
     *
     * @param     name the name of the class
     * @param     resolve if <code>true</code> then resolve the class
     * @return	  the resulting <code>Class</code> object
     * @exception ClassNotFoundException if the class could not be found
     */
    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
                                            throws ClassNotFoundException {
        final Class<?> cls = doFindClass(name);
        if (resolve) resolveClass(cls); 
        return cls; 
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        LOGGER.log(Level.FINEST, "{0} finding class {1}", new Object[] {this, name});
        return doFindClass(name);
    }
    
    private Class<?> doFindClass(String name) throws ClassNotFoundException {
        if (LOG_LOADING && !name.startsWith("java.")) {
            LOGGER.log(Level.FINEST, "{0} initiated loading of {1}",
                    new Object[] {this, name});
        }
        
        Class<?> cls = null;

        int last = name.lastIndexOf('.');
        if (last == -1) {
            throw new ClassNotFoundException("Will not load classes from default package (" + name + ")"); // NOI18N
        }

        // Strip+intern or use from package coverage
        String pkg = (last >= 0) ? name.substring(0, last) : ""; 

        final String path = pkg.replace('.', '/') + "/";

        Set<ProxyClassLoader> del = ProxyClassPackages.findCoveredPkg(pkg);
 
        Boolean boo = isSystemPackage(pkg);
        if ((boo == null || boo.booleanValue()) && shouldDelegateResource(path, null)) {
            try {
                cls = parents.systemCL().loadClass(name);
                if (boo == null) registerSystemPackage(pkg, true);
                return cls; // try SCL first
            } catch (ClassNotFoundException e) {
                // No dissaster, try other loaders
            }
        }

        if (del == null) {
            // uncovered package, go directly to SCL (may throw the CNFE for us)
            //if (shouldDelegateResource(path, null)) cls = par.systemCL().loadClass(name);
        } else if (del.size() == 1) {
            // simple package coverage
            ProxyClassLoader pcl = del.iterator().next();
            if (pcl == this || (parents.contains(pcl) && shouldDelegateResource(path, pcl))) {
                cls = pcl.selfLoadClass(pkg, name);
                if (cls != null) registerSystemPackage(pkg, false);
            }/* else { // maybe it is also covered by SCL
                if (shouldDelegateResource(path, null)) cls = par.systemCL().loadClass(name);
            }*/
        } else {
            // multicovered package, search in order
            for (ProxyClassLoader pcl : parents.loaders()) { // all our accessible parents
                if (del.contains(pcl) && shouldDelegateResource(path, pcl)) { // that cover given package
                    Class<?> _cls = pcl.selfLoadClass(pkg, name);
                    if (_cls != null) {
                        if (cls == null) {
                            cls = _cls;
                        } else if (cls != _cls) {
                            String message = "Will not load class " + name + " arbitrarily from one of " +
                                    cls.getClassLoader() + " and " + pcl + " starting from " + this +
                                    "; see http://wiki.netbeans.org/DevFaqModuleCCE";
                            ClassNotFoundException cnfe = new ClassNotFoundException(message);
                            if (arbitraryLoadWarnings.add(message)) {
                                if (LOGGER.isLoggable(Level.FINE)) {
                                    LOGGER.log(Level.FINE, null, cnfe);
                                } else {
                                    LOGGER.warning(message);
                                }
                            }
                            throw cnfe;
                        }
                    }
                }
            }
            if (cls == null && del.contains(this)) cls = selfLoadClass(pkg, name); 
            if (cls != null) registerSystemPackage(pkg, false); 
        }
        if (cls == null && shouldDelegateResource(path, null)) {
            try {
                cls = parents.systemCL().loadClass(name);
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException(diagnosticCNFEMessage(e.getMessage(), del), e);
            }
        }
        if (cls == null) {
            throw new ClassNotFoundException(diagnosticCNFEMessage(name, del));
        }
        return cls;
    }
    
    private String diagnosticCNFEMessage(String base, Set<ProxyClassLoader> del) {
        int size = parents.size();
        // Too big to show in its entirety - overwhelms the log file.
        StringBuilder b = new StringBuilder();
        b.append(base).append(" starting from ").append(this)
            .append(" with possible defining loaders ").append(del)
            .append(" and declared parents ");
        Iterator<ProxyClassLoader> parentSetI = parents.loaders().iterator();
        for (int i = 0; i < 10 && parentSetI.hasNext(); i++) {
            b.append(i == 0 ? "[" : ", ");
            b.append(parentSetI.next());
        }
        if (parentSetI.hasNext()) {
            b.append(", ...").append(size - 10).append(" more");
        }
        b.append(']');
        return b.toString();
    }
    private static final Set<String> arbitraryLoadWarnings = ConcurrentHashMap.newKeySet();

    /** May return null */ 
    private synchronized Class<?> selfLoadClass(String pkg, String name) { 
        Class<?> cls = findLoadedClass(name); 
        if (cls == null) {
            try {
                cls = doLoadClass(pkg, name);
            } catch (NoClassDefFoundError e) {
                // #145503: we can make a guess as to what triggered this error (since the JRE does not inform you).
                // XXX Exceptions.attachMessage does not seem to work here
                throw (NoClassDefFoundError) new NoClassDefFoundError(e.getMessage() + " while loading " + name +
                        "; see http://wiki.netbeans.org/DevFaqTroubleshootClassNotFound").initCause(e); // NOI18N
            }
            if (LOG_LOADING && !name.startsWith("java.")) LOGGER.log(Level.FINEST, "{0} loaded {1}",
                        new Object[] {this, name});
            }
        return cls; 
    }

    
    /** This ClassLoader can't load anything itself. Subclasses
     * may override this method to do some class loading themselves. The
     * implementation should not throw any exception, just return
     * <CODE>null</CODE> if it can't load required class.
     *
     * @param  name the name of the class
     * @return the resulting <code>Class</code> object or <code>null</code>
     */
    protected Class<?> doLoadClass(String pkg, String name) {
        return null;
    }
    
    private String stripInitialSlash(String resource) { // #90310
        if (resource.startsWith("/")) {
            LOGGER.log(Level.WARNING, "Should not use initial '/' in calls to ClassLoader.getResource(s): {0}", resource);
            return resource.substring(1);
        } else {
            return resource;
        }
    }

    /**
     * Finds the resource with the given name.
     * @param  name a "/"-separated path name that identifies the resource.
     * @return a URL for reading the resource, or <code>null</code> if
     *      the resource could not be found.
     * @see #findResource(String)
     */
    @Override
    public final URL getResource(String name) {
        return getResourceImpl(name);
    }
    
    URL getResourceImpl(String name) {
        URL url = null;
        name = stripInitialSlash(name);

        int last = name.lastIndexOf('/');
        String pkg;
        String fallDef = null;
        if (last >= 0) {
            if (name.startsWith("META-INF/")) {
                pkg = name.substring(8);
                fallDef = name.substring(0, last).replace('/', '.');
            } else {
                pkg = name.substring(0, last).replace('/', '.');
            }
        } else {
            pkg = "default/" + name;
            fallDef = "";
        }
        String path = name.substring(0, last+1);
        
        Boolean systemPackage = isSystemPackage(pkg);
        if ((systemPackage == null || systemPackage) && shouldDelegateResource(path, null)) {
            URL u = parents.systemCL().getResource(name);
            if (u != null) {
                if (systemPackage == null) {
                    registerSystemPackage(pkg, true);
                }
                return u;
            }
            // else try other loaders
        }

        Set<ProxyClassLoader> del = ProxyClassPackages.findCoveredPkg(pkg);
        if (fallDef != null) {
            Set<ProxyClassLoader> snd = ProxyClassPackages.findCoveredPkg(fallDef);
            if (snd != null) {
                if (del != null) {
                    del = new HashSet<ProxyClassLoader>(del);
                    del.addAll(snd);
                } else {
                    del = snd;
                }
            }
        }

        if (del == null) {
            // uncovered package, go directly to SCL
            if (shouldDelegateResource(path, null)) url = parents.systemCL().getResource(name);
        } else if (del.size() == 1) {
            // simple package coverage
            ProxyClassLoader pcl = del.iterator().next();
            if (pcl == this || (parents.contains(pcl) && shouldDelegateResource(path, pcl)))
                url = pcl.findResource(name);
        } else {
            // multicovered package, search in order
            for (ProxyClassLoader pcl : parents.loaders()) { // all our accessible parents
                if (del.contains(pcl) && shouldDelegateResource(path, pcl)) { // that cover given package
                    url = pcl.findResource(name);
                    if (url != null) break;
                }
            }
            if (url == null && del.contains(this)) url = findResource(name); 
        }

        // uncovered package, go directly to SCL
        if (url == null && shouldDelegateResource(path, null)) url = parents.systemCL().getResource(name);
        
        return url;
    }

    /** This ClassLoader can't load anything itself. Subclasses
     * may override this method to do some resource loading themselves.
     *
     * @param  name the resource name
     * @return a URL for reading the resource, or <code>null</code>
     *      if the resource could not be found.
     */
    @Override
    public URL findResource(String name) {
        return super.findResource(name);
    }
    
    @Override
    public final Enumeration<URL> getResources(String name) throws IOException {
        return getResourcesImpl(name);
    }
    
    synchronized Enumeration<URL> getResourcesImpl(String name) throws IOException {
        name = stripInitialSlash(name);
        final int slashIdx = name.lastIndexOf('/');
        final String path = name.substring(0, slashIdx + 1);
        String pkg;
        String fallDef = null;
        if (slashIdx >= 0) {
            if (name.startsWith("META-INF/")) {
                pkg = name.substring(8);
                fallDef = name.substring(0, slashIdx).replace('/', '.');
            } else {
                pkg = name.substring(0, slashIdx).replace('/', '.');
            }
        } else {
            pkg = "default/" + name;
            fallDef = "";
        }
        List<Enumeration<URL>> sub = new ArrayList<Enumeration<URL>>();

        // always consult SCL first
        if (shouldDelegateResource(path, null)) sub.add(parents.systemCL().getResources(name));
        
        Set<ProxyClassLoader> del = ProxyClassPackages.findCoveredPkg(pkg);
        if (fallDef != null) {
            Set<ProxyClassLoader> snd = ProxyClassPackages.findCoveredPkg(fallDef);
            if (snd != null) {
                if (del != null) {
                    del = new HashSet<ProxyClassLoader>(del);
                    del.addAll(snd);
                } else {
                    del = snd;
                }
            }
        }

        if (del != null) {
            for (ProxyClassLoader pcl : parents.loaders()) { // all our accessible parents
                if (del.contains(pcl) && shouldDelegateResource(path, pcl)) { // that cover given package
                    sub.add(pcl.findResources(name));
                }
            }
            if (del.contains(this)) {
                sub.add(findResources(name));
            }
        }
        // Should not be duplicates, assuming the parent loaders are properly distinct
        // from one another and do not overlap in JAR usage, which they ought not.
        // Anyway MetaInfServicesLookup, the most important client of this method, does
        // its own duplicate filtering already.
        return Enumerations.concat(Collections.enumeration(sub));
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        return super.findResources(name);
    }

    
    /**
     * Returns a Package that has been defined by this class loader or any
     * of its parents.
     *
     * @param  name the package name
     * @return the Package corresponding to the given name, or null if not found
     */
    @Override
    protected Package getPackage(String name) {
        return getPackageFast(name, true);
    }
    
    /**
     * Faster way to find a package.
     * @param name package name in org.netbeans.modules.foo format
     * @param recurse whether to also ask parents
     * @return located package, or null
     */
    protected Package getPackageFast(String name, boolean recurse) {
        Package pkg = packages.get(name);
        if (pkg != null) {
            return pkg;
        }
        if (!recurse) {
            return null;
        }
        synchronized(packages) {
            pkg = packages.get(name);
            String path = name.replace('.', '/');
            for (ProxyClassLoader par : this.parents.loaders()) {
                if (!shouldDelegateResource(path, par)) {
                    continue;
                }
                pkg = par.getPackageFast(name, false);
                if (pkg != null) {
                    break;
                }
            }
            // pretend the resource ends with "/". This works better with hidden package and
            // prefix-based checks.
            if (pkg == null && shouldDelegateResource(path + "/", null)) {
                // Cannot access either Package.getSystemPackages nor ClassLoader.getPackage
                // from here, so do the best we can though it will cause unnecessary
                // duplication of the package cache (PCL.packages vs. CL.packages):
                    pkg = super.getPackage(name);
            }
            if (pkg != null) {
                packages.put(name, pkg);
            }
            return pkg;
        }
    }

    /** This is here just for locking serialization purposes.
     * Delegates to super.definePackage with proper locking.
     * Also tracks the package in our private cache, since
     * getPackageFast(...,...,false) will not call super.getPackage.
     */
    @Override
    protected Package definePackage(String name, String specTitle,
                String specVersion, String specVendor, String implTitle,
		String implVersion, String implVendor, URL sealBase )
		throws IllegalArgumentException {
        Package pkg = super.definePackage(name, specTitle, specVersion, specVendor, implTitle,
                implVersion, implVendor, sealBase);
        packages.put(name, pkg);
        return pkg;
    }

    /**
     * Returns all of the Packages defined by this class loader and its parents.
     *
     * @return the array of <code>Package</code> objects defined by this
     * <code>ClassLoader</code>
     */
    @Override
    protected synchronized Package[] getPackages() {
        return getPackages(new HashSet<ClassLoader>());
    }
    
    /**
     * Returns all of the Packages defined by this class loader and its parents.
     * Do not recurse to parents in addedParents set. It speeds up execution
     * time significantly.
     * @return the array of <code>Package</code> objects defined by this
     * <code>ClassLoader</code>
     */
    private Package[] getPackages(Set<ClassLoader> addedParents) {
        Map<String,Package> all = new HashMap<String, Package>();
        // XXX call shouldDelegateResource on each?
        addPackages(all, super.getPackages());
        for (ClassLoader par : this.parents.loaders()) {
            if (par instanceof ProxyClassLoader && addedParents.add(par)) {
                // XXX should ideally use shouldDelegateResource here...
                addPackages(all, ((ProxyClassLoader)par).getPackages(addedParents));
            }
        }
        synchronized (packages) {
            all.keySet().removeAll(packages.keySet());
            packages.putAll(all);
            return packages.values().toArray(new Package[packages.size()]);
        }
    }
    
    private void addPackages(Map<String,Package> all, Package[] pkgs) {
        // Would be easier if Package.equals() was just defined sensibly...
        for (int i = 0; i < pkgs.length; i++) {
            all.put(pkgs[i].getName(), pkgs[i]);
        }
    }
    
    protected final void setSystemClassLoader(ClassLoader s) {
        parents = parents.changeSystemClassLoader(s);
    }
    
    protected boolean shouldDelegateResource(String pkg, ClassLoader parent) {
         if (delegatingPredicate != null) {
             return delegatingPredicate.apply(pkg, parent);
         } else {
             return true;
         }
    }

    /** Called before releasing the classloader so it can itself unregister
     * from the global ClassLoader pool */
    public void destroy() {
        ProxyClassPackages.removeCoveredPakcages(this);
    }

    final ClassLoader firstParent() {
        Iterator<ProxyClassLoader> it = parents.loaders().iterator();
        return it.hasNext() ? it.next() : null;
    }

    //
    // System Class Loader Packages Support
    //
    
    private static ConcurrentMap<String,Boolean> sclPackages = new ConcurrentHashMap<>();
    private static Boolean isSystemPackage(String pkg) {
        return sclPackages.get(pkg);
    }
    private static void registerSystemPackage(String pkg, boolean isSystemPkg) {
        sclPackages.put(pkg, isSystemPkg);
    }
}
