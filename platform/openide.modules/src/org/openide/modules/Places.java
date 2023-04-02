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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 * Provides access to standard file locations.
 * <div class="nonnormative">
 * This class should be used for limited purposes only. You might instead want to use:<ul>
 * <li><a href="@org-openide-filesystems@/org/openide/filesystems/FileUtil.html#getConfigFile(java.lang.String)"><code>FileUtil.getConfigFile</code></a>
 * to find a file declared in an XML layer or created or overridden in the {@code config} subdirectory of the user directory.
 * <li>{@link InstalledFileLocator} to find modules installed as part of an NBM.
 * <li>{@code someClass.getProtectionDomain().getCodeSource().getLocation()} to find resources inside a module class loader.
 * </ul>
 * </div>
 * @since 7.26
 */
public abstract class Places {

    private static final Logger LOG = Logger.getLogger(Places.class.getName());

    /**
     * Locates the NetBeans user directory.
     * This may be used to persist valuable files for which the system filesystem
     * ({@code FileUtil.getConfigFile}) is inappropriate due its being virtual.
     * Each module is responsible for using sufficiently unique filenames within this directory.
     * The system property {@code netbeans.user} is used for compatibility.
     * @return a directory location (need not yet exist), or null if unconfigured
     */
    public static synchronized /*@CheckForNull*/ File getUserDirectory() {
        Places places = Lookup.getDefault().lookup(Places.class);
        if (places != null) {
            return places.findUserDirectory();
        }
        String p = System.getProperty("netbeans.user");
        return p != null ? new File(p) : null;
    }

    /**
     * Locates the NetBeans cache directory.
     * This may be used to store pure performance caches - files which could be safely deleted,
     * since they would be automatically recreated on demand.
     * Each module is responsible for using sufficiently unique filenames within this directory.
     * {@code $userdir/var/cache/} is used as a default when {@link #getUserDirectory} is configured.
     * As a final fallback, a location in the system temporary directory will be returned.
     * @return a directory location (never null but need not yet exist)
     * @see #getCacheSubdirectory
     * @see #getCacheSubfile
     */
    public static synchronized /*@NonNull*/ File getCacheDirectory() {
        Places places = Lookup.getDefault().lookup(Places.class);
        if (places != null) {
            File cache = places.findCacheDirectory();
            if (cache != null) {
                return cache;
            }
        }
        File userdir = getUserDirectory();
        if (userdir != null) {
            return new File(new File(userdir, "var"), "cache");
        }
        return new File(System.getProperty("java.io.tmpdir"), "nbcache");
    }

    /**
     * Convenience method to get a particular subdirectory within {@link #getCacheDirectory}.
     * The directory will be created if it does not yet exist (but a warning logged if permissions do not allow this).
     * @param path a subdirectory path such as {@code stuff} or {@code mymodule/stuff} ({@code /} permitted even on Windows)
     * @return a directory of that name within the general cache directory
     */
    public static /*@NonNull*/ File getCacheSubdirectory(String path) {
        File d = new File(getCacheDirectory(), path);
        if (!d.isDirectory() && !d.mkdirs()) {
            LOG.log(Level.WARNING, "could not create {0}", d);
        }
        return d;
    }

    /**
     * Convenience method to get a particular file within {@link #getCacheDirectory}.
     * The parent directory will be created if it does not yet exist (but a warning logged if permissions do not allow this);
     * the file itself will not be automatically created.
     * @param path a file path such as {@code stuff.ser} or {@code mymodule/stuff.ser} ({@code /} permitted even on Windows)
     * @return a file of that name within the general cache directory
     */
    public static /*@NonNull*/ File getCacheSubfile(String path) {
        File f = new File(getCacheDirectory(), path);
        File d = f.getParentFile();
        if (!d.isDirectory() && !d.mkdirs()) {
            LOG.log(Level.WARNING, "could not create {0}", d);
        }
        return f;
    }

    /** Constructor for those who believe to know 
     * where {@link #getCacheDirectory} or
     * {@link #getUserDirectory()} is. Register your subclass via
     * {@link org.openide.util.lookup.ServiceProvider} annotation. 
     */
    protected Places() {
    }

    /** The cache directory to return from {@link #getCacheDirectory}.
     * If <code>null</code> the caches will be placed below {@link #getUserDirectory()}.
     * @return the file to use for caches or null 
     * @since 7.26
     */
    protected abstract File findCacheDirectory();
    
    /** Finds location of a user directory to return from {@link #getUserDirectory}.
     * @return the user directory or <code>null</code> if no user directory is
     *   supposed to be used
     * @since 7.26
     */
    protected abstract File findUserDirectory();
}
