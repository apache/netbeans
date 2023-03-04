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

package org.netbeans.spi.java.classpath;

import java.net.URL;

/**
 * SPI interface for a classpath entry which can include or exclude particular files.
 * @author Jesse Glick
 * @see "issue #49026"
 * @since org.netbeans.api.java/1 1.13
 */
public interface FilteringPathResourceImplementation extends PathResourceImplementation {

    /**
     * Property name to fire in case {@link #includes} would change.
     * (The old and new value should be left null.)
     * <p>
     * <strong>Special usage note:</strong>
     * If multiple {@link FilteringPathResourceImplementation}s inside a single
     * {@link ClassPathImplementation} fire changes in this pseudo-property in
     * succession, all using the same non-null {@link java.beans.PropertyChangeEvent#setPropagationId},
     * {@link org.netbeans.api.java.classpath.ClassPath#PROP_INCLUDES} will be fired just once. This can be used
     * to prevent "event storms" from triggering excessive Java source root rescanning.
     */
    String PROP_INCLUDES = "includes"; // NOI18N

    /**
     * Determines whether a given resource is included in the classpath or not.
     * @param root one of the roots given by {@link #getRoots} (else may throw {@link IllegalArgumentException})
     * @param resource a relative resource path within that root; may refer to a file or slash-terminated folder; the empty string refers to the root itself
     * @return true if included (or, in the case of a folder, at least partially included); false if excluded
     */
    boolean includes(URL root, String resource);

}
