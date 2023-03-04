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

package org.netbeans.spi.project.libraries;

import org.netbeans.spi.project.libraries.support.LibrariesSupport;

/**
 * Context class which is passed to library customizer (via <code>JComponent.setObject</code>).
 * Do not extend or instantiate this class directly.
 * 
 * @since org.netbeans.modules.project.libraries/1 1.18
 */
public class LibraryCustomizerContext {

    private LibraryImplementation libraryImplementation;
    private LibraryStorageArea libraryStorageArea;

    public LibraryCustomizerContext(LibraryImplementation libraryImplementation, LibraryStorageArea libraryStorageArea) {
        // prevent subclassing:
        if (!getClass().getName().equals(LibraryCustomizerContext.class.getName()) &&
            !getClass().getName().endsWith("LibraryCustomizerContextWrapper")) {
            throw new IllegalStateException("LibraryCustomizerContext cannot be subclassed");
        }
        this.libraryImplementation = libraryImplementation;
        this.libraryStorageArea = libraryStorageArea;
    }
    
    /**
     * Library implementation to be customized.
     * 
     * @return always non-null
     */
    public LibraryImplementation getLibraryImplementation() {
        return libraryImplementation;
    }

    /**
     * Returns <code>LibraryImplementation2</code> or null if underlying 
     * library implementation does not implement it.
     * 
     * @return can be null
     */
    public LibraryImplementation2 getLibraryImplementation2() {
        return LibrariesSupport.supportsURIContent(libraryImplementation)?
            (LibraryImplementation2)libraryImplementation : null;
    }

    /**
     * Area of library being customized.
     * 
     * @return can be null for global library
     */
    public LibraryStorageArea getLibraryStorageArea() {
        return libraryStorageArea;
    }
    
}
