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
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Richard Michalsky
 */
public interface SourceRootsProvider {

    /**
     * Property name.
     */
    public static final String PROP_SOURCE_ROOTS = "sourceRoots"; // NOI18N

    /**
     * Add given source root to the current source root list and save the
     * result.
     */
    void addSourceRoot(URL root) throws IOException;

    /**
     * Find sources for a module JAR file contained in this destination directory.
     * @param jar a JAR file in the destination directory
     * @return the directory of sources for this module (a project directory), or null
     */
    File getSourceLocationOfModule(File jar);

    /**
     * Get associated source roots for this provider.
     * Each root could be a netbeans.org source checkout or a module suite project directory.
     * @return a list of source root URLs (may be empty but not null)
     */
    URL[] getSourceRoots();

    /**
     * When no source roots are explicitly given, this may return default ones.
     * @return default source roots or <tt>null</tt>
     */
    URL[] getDefaultSourceRoots();

    /**
     * Remove given source roots from the current source root list and save the
     * result.
     */
    void removeSourceRoots(URL[] urlsToRemove) throws IOException;

    /**
     * Set source roots for this provider.
     * Each root could be a netbeans.org source checkout or a module suite project directory.
     * @param roots an array of source root URLs (may be empty but not null)
     */
    void setSourceRoots(URL[] roots) throws IOException;

    /**
     * Moves entry one step up in the list of source roots and saves the result.
     * Does nothing if <tt>indexToUp</tt> is 0 or negative.
     * @param indexToUp index of entry to move
     * @throws java.io.IOException can be thrown when storing new roots.
     */
    void moveSourceRootUp(int indexToUp) throws IOException;
    
    /**
     * Moves entry one step down in the list of source roots and saves the result.
     * Does nothing if <tt>indexToDown</tt> exceeds number of source roots.
     * @param indexToDown index of entry to move
     * @throws java.io.IOException can be thrown when storing new roots.
     */
    void moveSourceRootDown(int indexToDown) throws IOException;
}
