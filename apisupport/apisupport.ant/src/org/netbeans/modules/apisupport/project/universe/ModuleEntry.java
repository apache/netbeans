/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.net.URL;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.apisupport.project.api.ManifestManager;

/**
 * One known module.
 * Falls into five categories:
 * <ol>
 * <li>Modules inside netbeans.org (with source).
 * <li>Modules with source in an external suite.
 * <li>Standalone external modules with source.
 * <li>JARs from a NB binary. May or may not have an associated source dir.
 * <li>JARs from an external cluster specified in cluster.path.
 * May or may not have an associated source dir <i>(new in NB 6.7)</i>.
 * </ol>
 */
public interface ModuleEntry extends Comparable<ModuleEntry> {

    /**
     * Get a relative source path inside netbeans.org sources.
     * Note that if the entry is from netbeans.org yet was scanned as a
     * side effect of loading an external module that defined netbeans.dest.dir,
     * this will be null - such secondary entries are essentially based on the
     * actual JAR, only adding in a non-null {@link #getSourceLocation}.
     * @return e.g. java/project, or null for external modules
     */
    String getNetBeansOrgPath();
    
    /**
     * Get the source location of this module, if there is one.
     */
    File getSourceLocation();
    
    /**
     * Get a code name base.
     * @return e.g. org.netbeans.modules.java.project
     */
    String getCodeNameBase();
    
    /**
     * Get the directory to which the module is built.
     * @return e.g. .../../nbbuild/netbeans/ide
     */
    File getClusterDirectory();
    
    /**
     * Get the module JAR file.
     * @return e.g. .../../nbbuild/netbeans/ide/modules/org-netbeans-modules-java-project.jar
     */
    File getJarLocation();
    
    /**
     * Get any added class path entries, as a path suffix (may be empty).
     */
    String getClassPathExtensions();
    
    /**
     * Returns either the module release version or <code>null</code> if
     * there isn't any.
     */
    @CheckForNull String getReleaseVersion();
    
    /**
     * Returns either the module specification version or <code>null</code>
     * if there isn't any.
     */
    @CheckForNull String getSpecificationVersion();
    
    /**
     * Returns array of provided tokens by the module. Can be empty.
     */
    String[] getProvidedTokens();
    
    /**
     * Get localized name of this module. Implementations should use
     * lazy-loading from localizing bundle to keep performance up. If the
     * localized name is not found <code>getCodeNameBase()</code> is
     * returned.
     */
    String getLocalizedName();
    
    /**
     * Get category of this module. Implementations should use lazy-loading
     * from localizing bundle to keep performance up.
     */
    String getCategory();
    
    /**
     * Get long description of this module. Implementations should use
     * lazy-loading from localizing bundle to keep performance up.
     */
    String getLongDescription();
    
    /**
     * Get short description of this module. Implementations should use
     * lazy-loading from localizing bundle to keep performance up.
     */
    String getShortDescription();
    
    /**
     * Get array of public packages exported by this module entry.
     * @return array of public packages. May be empty but not <code>null</code>.
     */
    ManifestManager.PackageExport[] getPublicPackages();

    /**
     * Returns javadoc for the module.
     * @return Javadoc for the module. May be <tt>null</tt>.
     */
    @CheckForNull URL getJavadoc(NbPlatform platform);

    /**
     * Get a set of names of all <em>nonempty</em> packages this module
     * contains.
     */
    Set<String> getAllPackageNames();
    
    /**
     * Get array of friends of this module.
     */
    boolean isDeclaredAsFriend(String cnb);
    
    /**
     * Get a set of class names defined in this module's public packages.
     */
    Set<String> getPublicClassNames();
    
    /**
     * Check whether this module is marked as deprecated.
     */
    boolean isDeprecated();

    /**
     * Get a list of code name bases for modules which this module has (runtime) dependencies on.
     */
    String[] getRunDependencies();

}
