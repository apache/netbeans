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
package org.netbeans.modules.javascript.cdnjs;

import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;

/**
 * Tests of a {@code LibraryProvider} class.
 *
 * @author Jan Stola
 */
public class LibraryProviderTest extends NbTestCase {

    public LibraryProviderTest(String name) {
        super(name);
    }

    @BeforeClass
    public static void initProxy() {
        System.setProperty("http.proxyHost", "www-proxy.uk.oracle.com"); // NOI18N
        System.setProperty("http.proxyPort", "80"); // NOI18N
        System.setProperty("https.proxyHost", "www-proxy.uk.oracle.com"); // NOI18N
        System.setProperty("https.proxyPort", "80"); // NOI18N
    }

    /**
     * Tests whether the CDNJS server returns the content with the expected
     * structure. So, it is more a sanity check of the CDNJS service.
     * then a test of the class {@code LibraryProvider} itself.
     */
    @Test
    @RandomlyFails
    public void testCDNJSResponseStructure() {
        String searchTerm = "knockout"; // NOI18N
        LibraryProvider provider = LibraryProvider.getInstance();
        LibraryProvider.SearchTask task = provider.new SearchTask(searchTerm);
        String searchURL = task.getSearchURL();
        String data = task.readUrl(searchURL);
        assertNotNull(data);

        Library[] libraries = task.parse(data);
        assertNotNull(libraries);

        Library knockoutLibrary = null;
        for (Library library : libraries) {
            if (searchTerm.equals(library.getName())) {
                knockoutLibrary = library;
            }
        }
        assertNotNull(knockoutLibrary);
        assertNotNull(knockoutLibrary.getDescription());
        assertNotNull(knockoutLibrary.getHomePage());

        Library.Version[] versions = knockoutLibrary.getVersions();
        assertNotNull(versions);
        assertTrue(versions.length > 0);

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
