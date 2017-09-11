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

package org.openide.execution;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

/** A class loader which is capable of loading classes from the Repository.
 * XXX the only useful thing this class does is effectively make
 * ExecutionEngine.createPermissions public! Consider deprecating this class...
 * @see <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html#getClassLoader(boolean)"><code>ClassPath.getClassLoader(...)</code></a>
* @author Ales Novak, Petr Hamernik, Jaroslav Tulach, Ian Formanek
*/
public class NbClassLoader extends URLClassLoader {
    /** I/O for classes defined by this classloader. May be <code>null</code>. */
    protected InputOutput inout;
    /** Cached PermissionCollections returned from ExecutionEngine. */
    private HashMap permissionCollections;
    /** Default permissions */
    private PermissionCollection defaultPermissions;
    
    /** Works onthe top of file (jar:file) - directly delegates to URLClassLoader*/
    private final boolean fast;
    
    private static ClassLoader systemClassLoader() {
        return (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
    }
    
    /** Create a new class loader retrieving classes from the core IDE as well as the Repository.
     * @see FileSystemCapability#EXECUTE
     * @see FileSystemCapability#fileSystems
     * @deprecated Misuses classpath.
    */
    public NbClassLoader () {
        super(new URL[0], systemClassLoader());
        fast = false;
    }

    /** Create a new class loader retrieving classes from the core IDE as well as the Repository,
    * and redirecting system I/O.
     * @param io an I/O tab in the Output Window
     * @see org.openide.filesystems.Repository#getFileSystems
     * @deprecated Misuses classpath.
     */
    public NbClassLoader(InputOutput io) {
        super(new URL[0], systemClassLoader());
        fast = false;
        inout = io;
    }
    
    /**
     * Create a new class loader retrieving classes from a set of package roots.
     * @param roots a set of package roots
     * @param parent a parent loader
     * @param io an I/O tab in the Output Window, or null
     * @since XXX
     */
    static ThreadLocal<Boolean> f = new ThreadLocal<Boolean>();
    public NbClassLoader(FileObject[] roots, ClassLoader parent, InputOutput io) {
        super(createRootURLs(roots), parent);
        fast = canOptimize(getURLs());
        inout = io;
    }
        
    /** Create a new class loader retrieving classes from the core IDE as well as specified file systems.
     * @param fileSystems file systems to load classes from
     * @deprecated Misuses classpath.
    */
    public NbClassLoader (FileSystem[] fileSystems) {
        super(new URL[0], systemClassLoader(), null);
        fast = false;
        Thread.dumpStack();
    }

    /** Create a new class loader.
     * @param fileSystems file systems to load classes from
     * @param parent fallback class loader
     * @deprecated Misuses classpath.
    */
    public NbClassLoader (FileSystem[] fileSystems, ClassLoader parent) {
        super(new URL[0], parent);
        fast = false;
        Thread.dumpStack();
    }
    
    /** Create a URL to a resource specified by name.
    * Same behavior as in the super method, but handles names beginning with a slash.
    * @param name resource name
    * @return URL to that resource or <code>null</code>
    */
    public URL getResource (String name) {
        return super.getResource (name.startsWith ("/") ? name.substring (1) : name); // NOI18N
    }

    /* Needs to be overridden so that packages are correctly defined
       based on manifest for e.g. JarFileSystem's in the repository.
       Otherwise URLClassLoader, not understanding nbfs:/..../foo.jar,
       would simply define packages loaded from such a URL with no
       particular info. We want it to have specification version and
       all that good stuff. */
    protected Class findClass (final String name) throws ClassNotFoundException {
        if (!fast && name.indexOf ('.') != -1) {
            Logger.getLogger(NbClassLoader.class.getName()).log(Level.FINE, "NBFS used!");
            String pkg = name.substring (0, name.lastIndexOf ('.'));
            if (getPackage (pkg) == null) {
                String resource = name.replace ('.', '/') + ".class"; // NOI18N
                URL[] urls = getURLs ();
                for (int i = 0; i < urls.length; i++) {
                    // System.err.println (urls[i].toString ());
                    FileObject root = URLMapper.findFileObject(urls[i]);

                    if (root == null) {
                        continue;
                    }
                    try {
                        FileObject fo = root.getFileObject(resource);

                        if (fo != null) {
                            // Got it. If there is an associated manifest, load it.
                            FileObject manifo = root.getFileObject("META-INF/MANIFEST.MF");

                            if (manifo == null)
                                manifo = root.getFileObject("meta-inf/manifest.mf");
                            if (manifo != null) {
                                // System.err.println (manifo.toString () + " " + manifo.getClass ().getName () + " " + manifo.isValid ());
                                Manifest mani = new Manifest();
                                InputStream is = manifo.getInputStream();

                                try {
                                    mani.read(is);
                                }
                                finally {
                                    is.close();
                                }
                                definePackage(pkg, mani, urls[i]);
                            }
                            break;
                        }
                    }
                    catch (IOException ioe) {
                        Exceptions.attachLocalizedMessage(ioe,
                                                          urls[i].toString());
                        Exceptions.printStackTrace(ioe);
                        continue;
                    }
                }
            }
        }
        return super.findClass (name);
    }
    
    /** Sets a PermissionsCollectio which will be used
     * for ProtectionDomain of newly created classes.
     *
     * @param defaultPerms
     */
    public void setDefaultPermissions(PermissionCollection defaultPerms) {
        if (defaultPerms != null && !defaultPerms.isReadOnly()) {
            defaultPerms.setReadOnly();
        }
        this.defaultPermissions = defaultPerms;
    }

    /* @return a PermissionCollection for given CodeSource. */
    protected final synchronized PermissionCollection getPermissions(CodeSource cs) {

        if (permissionCollections != null) {
            PermissionCollection pc = (PermissionCollection) permissionCollections.get(cs);
            if (pc != null) {
                return pc;
            }
        }

        return createPermissions(cs, inout);
    }

    /**
    * @param cs CodeSource
    * @param inout InputOutput passed to @seeExecutionEngine#createPermissions(java.security.CodeSource, org.openide.windows.InpuOutput).
    * @return a PermissionCollection for given CodeSource.
    */
    private PermissionCollection createPermissions(CodeSource cs, InputOutput inout) {
        PermissionCollection pc;
        if (inout == null) {
            if (defaultPermissions != null) {
                pc = defaultPermissions;
            } else {
                pc = super.getPermissions(cs);
            }
        } else {
            ExecutionEngine engine = ExecutionEngine.getDefault();
            pc = engine.createPermissions(cs, inout);
            if (defaultPermissions != null) {
                addAllPermissions(pc, defaultPermissions);
            } else {
                pc.add(new AllPermission());
            }
        }
        if (permissionCollections == null) {
            permissionCollections = new HashMap(7);
        }
        permissionCollections.put(cs, pc);
        return pc;
    }
    
    /**
     * Copies all permissions from <tt>src</tt> into <tt>target</tt>
     *
     * @param target To where put permissions
     * @param src From where take paermissions
     */
    private static void addAllPermissions(PermissionCollection target, PermissionCollection src) {
        Enumeration e = src.elements();
        
        while (e.hasMoreElements()) {
            target.add((Permission) e.nextElement());
        }
    }


    /**
     * Creates URLs for file objects.
     * @param roots file roots
     * @return array of URLs
     */
    private static URL[] createRootURLs(FileObject[] roots) {
        URL[] urls = new URL[roots.length];
        for (int i = 0; i < roots.length; i++) {
            urls[i] = roots[i].toURL();
            }
        return urls;
    }
    
    private static boolean canOptimize (URL[] urls) {
        assert urls != null;        
        for (int i=0; i<urls.length; i++) {
            URL url = urls[i];
            URL au = FileUtil.getArchiveFile(url);
            if (au != null) {
                if (!url.toExternalForm().endsWith("!/")) { //NOI18N
                    //Nested path - not supported fast mode 
                    return false;
                }
                url = au;
            }
            if (!"file".equals(url.getProtocol())) {        //NOI18N
                //Not file - not supported fast mode
                return false;
            }            
        }        
        return true;
    }
}
