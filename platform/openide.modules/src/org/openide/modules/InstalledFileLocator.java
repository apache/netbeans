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

package org.openide.modules;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Service providing the ability to locate a module-installed file in
 * the NetBeans application's installation.
 * Zero or more instances may be registered to lookup.
 * <p>For use in declarative formats, or from APIs which require URLs,
 * there is a matching URL protocol {@code nbinst}. The host field (optional but
 * recommended) should be the code name base of the module owning the file; the
 * path is then relative to that module's cluster. For example,
 * {@code nbinst://my.module/docs/README} should refer to the file found by
 * {@code InstalledFileLocator.getDefault().locate("docs/README", "my.module", false)}.</p>
 * @author Jesse Glick
 * @since 3.21
 */
public abstract class InstalledFileLocator {
    private static final InstalledFileLocator DEFAULT = new InstalledFileLocator() {
        public @Override File locate(String rp, String cnb, boolean l) {
            InstalledFileLocator[] ifls = getInstances();
            
            for (int i = 0; i < ifls.length; i++) {
                File f = ifls[i].locate(rp, cnb, l);
                
                if (f != null) {
                    return f;
                }
            }
            
            return null;
        }
        public @Override Set<File> locateAll(String relativePath, String codeNameBase, boolean localized) {
            Set<File> result = null;
            for (InstalledFileLocator ifl : getInstances()) {
                Set<File> added = ifl.locateAll(relativePath, codeNameBase, localized);
                // avoid allocating extra lists, under the assumption there is only one result:
                if (!added.isEmpty()) {
                    if (result == null) {
                        result = added;
                    } else {
                        result = new LinkedHashSet<File>(result);
                        result.addAll(added);
                    }
                }
            }
            return result != null ? result : Collections.<File>emptySet();
        }
    };
    
    private static InstalledFileLocator[] instances = null;
    private static Lookup.Result<InstalledFileLocator> result = null;
    /**
     * Used just for guarding direct access to {@link #instances} and {@link #result}.
     * Should not call foreign code while holding this.
     * Cf. comments in #64710.
     */
    @SuppressWarnings("RedundantStringConstructorCall")
    private static final Object LOCK = new String(InstalledFileLocator.class.getName());
    
    /**
     * No-op constructor for use by subclasses.
     */
    protected InstalledFileLocator() {
    }
    
    /**
     * Try to locate a file.
     * <div class="nonnormative">
     * <p>
     * When using the normal NetBeans installation structure and NBM file format,
     * this path will be relative to the installation directory (or user directory,
     * for a locally installed module). Other possible installation mechanisms, such
     * as JNLP (Java WebStart), might arrange the physical files differently, but
     * generally the path indicated by a module's normal NBM file (beneath <code>netbeans/</code>
     * in the NBM) should be interpreted by the locator implementation to point to the actual
     * location of the file, so the module need not be aware of such details. Some
     * locator implementations may perform the search more accurately or quickly
     * when given a code name base for the module that supplies the file.
     * </p>
     * <p>
     * The file may refer to a directory (no trailing slash!), in which case the locator
     * should attempt to find that directory in the installation. Note that only one
     * file may be located from a given path, so generally this method will not be
     * useful where a directory can contain many items that may be merged between e.g.
     * the installation and user directories. For example, the <code>docs</code> folder
     * (used e.g. for Javadoc) might contain several ZIP files in both the installation and
     * user areas. Use {@link #locateAll} if you need all results. The module may assume
     * that all contained files are in the same relative structure in the directory as in
     * the normal NBM-based installation; unusual locator implementations may need to create
     * temporary directories with matching structures to return from this method, in case the
     * physical file locations are not in such a directory structure.
     * See issue #36701 for details.
     * </p>
     * </div>
     * <p>
     * Localized and branded lookups should follow the normal naming conventions,
     * e.g. <code>docs/OpenAPIs_ja.zip</code> would be used for Japanese Javadoc
     * and <code>locate("docs/OpenAPIs.zip",&nbsp;&#8230;,&nbsp;true)</code>
     * would find it when running in Japanese locale.
     * </p>
     * <div class="nonnormative">
     * <p>
     * For cases where the search is for a module JAR or one of its extensions, client
     * code may prefer to use the code source given by a class loader. This will permit
     * a client to find the base URL (may or may not refer to a file) responsible for loading
     * the contents of the protection domain, typically a JAR file, containing a class
     * which is accessible to the module class loader. For example:
     * </p>
     * <pre>
     <span class="type">Class</span> <span class="variable-name">c</span> = ClassMyModuleDefines.<span class="keyword">class</span>;
     <span class="type">URL</span> <span class="variable-name">u</span> = c.getProtectionDomain().getCodeSource().getLocation();
     * </pre>
     * <p>
     * When running from a JAR file, this will typically give e.g.
     * <code>file:/path/to/archive.jar</code>. This information may be useful,
     * but it is not conclusive, since there is no guarantee what the URL protocol
     * will be, nor that the returned URL uniquely identifies a JAR shipped with
     * the module in its canonical NBM format. <code>InstalledFileLocator</code>
     * provides stronger guarantees than this technique, since you can explicitly
     * name a JAR file to be located on disk.
     * </p>
     * </div>
     * <div class="nonnormative">
     * <p>
     * This class should <em>not</em> be used just to find resources on the system
     * filesystem, which in the normal NetBeans installation structure means the
     * result of merging <code>${netbeans.home}/system/</code> with <code>${netbeans.user}/system/</code>
     * as well as module layers and perhaps project-specific storage. To find data in
     * the system filesystem, use the Filesystems API, e.g. in your layer you can predefine:
     * </p>
     <pre>
     &lt;<span class="function-name">filesystem</span>&gt;
     &lt;<span class="function-name">folder</span> <span class="variable-name">name</span>=<span class="string">"MyModule"</span>&gt;
     &lt;<span class="function-name">file</span> <span class="variable-name">name</span>=<span class="string">"data.xml"</span> <span class="variable-name">url</span>=<span class="string">"contents-in-module-jar.xml"</span>/&gt;
     &lt;/<span class="function-name">folder</span>&gt;
     &lt;/<span class="function-name">filesystem</span>&gt;
     </pre>
     * <p>
     * Then in your code use:
     * </p>
     <pre>
     <span class="type">String</span> <span class="variable-name">path</span> = <span class="string">"MyModule/data.xml"</span>;
     <span class="type">FileObject</span> <span class="variable-name">fo</span> = FileUtil.getConfigFile(path);
     <span class="keyword">if</span> (fo != <span class="constant">null</span>) {
     <span class="comment">// use fo.getInputStream() etc.
     </span>    <span class="comment">// FileUtil.toFile(fo) will often be null, do not rely on it!
     </span>}
     </pre>
     * </div>
     * @param relativePath path from install root, e.g. <code>docs/OpenAPIs.zip</code>
     *                     or <code>modules/ext/somelib.jar</code>
     *                     (always using <code>/</code> as a separator, regardless of platform)
     * @param codeNameBase name of the supplying module, e.g. <code>org.netbeans.modules.foo</code>;
     *                     may be <code>null</code> if unknown
     * @param localized true to perform a localized and branded lookup (useful for documentation etc.)
     * @return the requested <code>File</code>, if it can be found, else <code>null</code>
     */
    public abstract File locate(String relativePath, String codeNameBase, boolean localized);

    /**
     * Similar to {@link #locate} but can return multiple results.
     * The default implementation returns a list with zero or one elements according to {@link #locate}.
     * @param relativePath a path from install root
     * @param codeNameBase name of the supplying module or null
     * @param localized true to perform a localized/branded search
     * @return a (possibly empty) set of files
     * @since org.openide.modules 7.15
     */
    public Set<File> locateAll(String relativePath, String codeNameBase, boolean localized) {
        File f = locate(relativePath, codeNameBase, localized);
        return f != null ? Collections.singleton(f) : Collections.<File>emptySet();
    }
    
    /**
     * Get a master locator.
     * Lookup is searched for all registered locators.
     * They are merged together and called in sequence
     * until one of them is able to service a request.
     * If you use this call, require the token <code>org.openide.modules.InstalledFileLocator</code>
     * to require any autoload modules which can provide locators.
     * @return a master merging locator (never null)
     */
    public static InstalledFileLocator getDefault() {
        return DEFAULT;
    }
    
    private static InstalledFileLocator[] getInstances() {
        synchronized (LOCK) {
            if (instances != null) {
                return instances;
            }
        }
        
        Lookup.Result<InstalledFileLocator> _result;
        synchronized (LOCK) {
            _result = result;
        }
        if (_result == null) {
            _result = Lookup.getDefault().lookupResult(InstalledFileLocator.class);
            _result.addLookupListener(new LookupListener() {
                public @Override void resultChanged(LookupEvent e) {
                    synchronized (LOCK) {
                        instances = null;
                    }
                }
            });
            synchronized (LOCK) {
                result = _result;
            }
        }
        
        Collection<? extends InstalledFileLocator> c = _result.allInstances();
        synchronized (LOCK) {
            return instances = c.toArray(new InstalledFileLocator[0]);
        }
    }
    
}
