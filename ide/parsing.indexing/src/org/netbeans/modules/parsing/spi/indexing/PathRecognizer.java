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

package org.netbeans.modules.parsing.spi.indexing;

import java.util.Collections;
import java.util.Set;

/**
 * Enumeration of important path types for given language.
 * Instances of this class are registered in {@link org.openide.util.lookup.ServiceProvider}.
 * @author Tomas Zezula
 */
public abstract class PathRecognizer {

    /**
     * Returns names under which the source paths are registered in
     * the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/GlobalPathRegistry.html">GlobalPathRegistry</a>.
     * @return set of source path names
     */
    public abstract Set<String> getSourcePathIds ();

    /**
     * Returns names under which the library paths are registered in
     * the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/GlobalPathRegistry.html">GlobalPathRegistry</a>.
     * @return set of library path names
     */
    public abstract Set<String> getLibraryPathIds ();

    /**
     * Returns names under which the binary library paths are registered in
     * the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/GlobalPathRegistry.html">GlobalPathRegistry</a>.
     * @return set of binary library path names
     */
    public abstract Set<String> getBinaryLibraryPathIds ();

    /**
     * Returns a mime types of handled files.
     * @return mime type
     */
    public abstract Set<String> getMimeTypes();

    /**
     * XXX
     * @return XXX
     */
    public Set<String> getIndexerFilter() {
        return Collections.emptySet();
    }
}
