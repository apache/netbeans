/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
