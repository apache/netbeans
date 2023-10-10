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
package org.netbeans.spi.project.libraries;

import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import java.io.IOException;

/**
 * LibraryProvider supporting modifications.
 * @param <L> the {@link LibraryImplementation} type
 * @author Tomas Zezula
 * @since 1.48
 */
public interface WritableLibraryProvider<L extends LibraryImplementation> extends LibraryProvider<L> {

    /**
     * Adds a new library.
     * @param library the library to be added
     * @return true when the {@link WritableLibraryProvider} supports given library
     * @throws IOException in case of IO error
     */
    boolean addLibrary(L library) throws IOException;

    /**
     * Removes a library.
     * @param library the library to be removed
     * @return true when the {@link WritableLibraryProvider} owned the library and
     * the library was successfully removed
     * @throws IOException in case of IO error
     */
    boolean removeLibrary(L library) throws IOException;

    /**
     * Updates a library.
     * @param oldLibrary  the library to be updated
     * @param newLibrary the updated library prototype
     * @return true when the {@link WritableLibraryProvider} owned the library and
     * the library was successfully updated
     * @throws IOException in case of IO error
     */
    boolean updateLibrary(L oldLibrary, L newLibrary) throws IOException;
}
