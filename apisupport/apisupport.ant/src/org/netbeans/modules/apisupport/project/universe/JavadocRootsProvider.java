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

import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Richard Michalsky
 */
public interface JavadocRootsProvider {

    /**
     * Property name.
     */
    public static final String PROP_JAVADOC_ROOTS = "javadocRoots"; // NOI18N

    public URL[] getDefaultJavadocRoots();

    /**
     * Add given javadoc root to the current javadoc root list and save the
     * result.
     */
    void addJavadocRoot(URL root) throws IOException;

    /**
     * Get associated Javadoc roots.
     * Each root may contain some Javadoc sets in the usual format as subdirectories,
     * where the subdirectory is named acc. to the code name base of the module it
     * is documenting (using '-' in place of '.').
     * @return a list of Javadoc root URLs (may be empty but not null)
     */
    URL[] getJavadocRoots();

    void moveJavadocRootDown(int indexToDown) throws IOException;

    void moveJavadocRootUp(int indexToUp) throws IOException;

    /**
     * Remove given javadoc roots from the current javadoc root list and save
     * the result.
     */
    void removeJavadocRoots(URL[] urlsToRemove) throws IOException;

    void setJavadocRoots(URL[] roots) throws IOException;

}
