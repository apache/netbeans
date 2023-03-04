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
package org.netbeans.modules.javascript.cdnjs;

import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 * Tests of a {@code LibraryProvider} class.
 *
 * @author Jan Stola
 */
public class LibraryProviderTest extends NbTestCase {

    public LibraryProviderTest(String name) {
        super(name);
    }

    /**
     * Tests whether the CDNJS server returns the content with the expected
     * structure. So, it is more a sanity check of the CDNJS service.
     * then a test of the class {@code LibraryProvider} itself.
     */
    public void testCDNJSResponseSearchStructure() {
        String searchTerm = "knockout"; // NOI18N
        LibraryProvider provider = LibraryProvider.getInstance();
        Library[] libraries = provider.findLibraries(searchTerm);

        assertNotNull(libraries);
        assertTrue(libraries.length > 0);

        for(Library library: libraries) {
            assertNotNull(library.getVersions());
            assertEquals(0, library.getVersions().length);
        }
    }

    /**
     * Tests whether the CDNJS server returns the content with the expected
     * structure. So, it is more a sanity check of the CDNJS service.
     * then a test of the class {@code LibraryProvider} itself.
     */
    @Test
    public void testCDNJSResponseLibraryStructure() {
        String searchTerm = "knockout"; // NOI18N
        LibraryProvider provider = LibraryProvider.getInstance();
        Library knockoutLibrary = provider.loadLibrary(searchTerm);
        assertNotNull(knockoutLibrary);

        assertNotNull(knockoutLibrary);
        assertNotNull(knockoutLibrary.getDescription());
        assertNotNull(knockoutLibrary.getHomePage());

        Library.Version[] versions = knockoutLibrary.getVersions();
        assertNotNull(versions);
        assertTrue(versions.length > 0);

        provider.updateLibraryVersions(knockoutLibrary);
        versions = knockoutLibrary.getVersions();

        Library.Version version = versions[0];
        assertNotNull(version);
        assertNotNull(version.getName());
        assertSame(version.getLibrary(), knockoutLibrary);

        String[] files = version.getFiles();
        assertNotNull(files);
        assertTrue(files.length > 0);
        assertNotNull(files[0]);
    }

}
