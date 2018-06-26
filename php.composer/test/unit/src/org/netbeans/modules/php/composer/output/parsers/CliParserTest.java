/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.composer.output.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.composer.output.model.SearchResult;

public class CliParserTest extends NbTestCase {

    private CliParser cliParser;

    public CliParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cliParser = new CliParser();
    }

    public void testParseSearchFileLegacy() throws Exception {
        File searchResults = new File(getDataDir(), "output/search-results-legacy.txt");
        assertTrue(searchResults.isFile());
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(searchResults))) {
            String line;
            while ((line = reader.readLine()) != null) {
                count++;
               List<SearchResult> results = cliParser.parseSearch(line);
                assertNotNull(results);
                assertEquals(1, results.size());
                if (count == 1) {
                    SearchResult result = results.get(0);
                    assertEquals("monolog/monolog", result.getName());
                    assertEquals("Sends your logs to files, sockets, inboxes, databases and various web services", result.getDescription());
                }
            }
        }
        assertEquals(14, count);
    }

    public void testParseSearchFile() throws Exception {
        File searchResults = new File(getDataDir(), "output/search-results.txt");
        assertTrue(searchResults.isFile());
        StringBuilder content = new StringBuilder(500);
        try (BufferedReader reader = new BufferedReader(new FileReader(searchResults))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append("\n");
            }
        }
        List<SearchResult> results = cliParser.parseSearch(content.toString());
        assertNotNull(results);
        assertEquals(15, results.size());
        SearchResult result = results.get(0);
        assertEquals("monolog/monolog", result.getName());
        assertEquals("Sends your logs to files, sockets, inboxes, databases and various web services", result.getDescription());
    }

    public void testParseSearchMoreLinesAtOnce() {
        String chunk = "monolog/monolog: Sends your logs to files, sockets, inboxes, databases and various web services\n"
                + "hydra/hydra:     The cozy RESTfull PHP5.3 micro-framework.                          \n"
                + "cobaia/monologdoctrine: Monolog meets Doctrine as SQL Logger";
        List<SearchResult> results = cliParser.parseSearch(chunk);
        assertEquals(3, results.size());
        SearchResult result = results.get(0);
        assertEquals("monolog/monolog", result.getName());
        assertEquals("Sends your logs to files, sockets, inboxes, databases and various web services", result.getDescription());
        result = results.get(1);
        assertEquals("hydra/hydra", result.getName());
        assertEquals("The cozy RESTfull PHP5.3 micro-framework.", result.getDescription());
        result = results.get(2);
        assertEquals("cobaia/monologdoctrine", result.getName());
        assertEquals("Monolog meets Doctrine as SQL Logger", result.getDescription());
    }

    public void testParseSearchMoreColons() {
        String chunk = "monolog/monolog: Sends your logs to: files, sockets, inboxes, databases and various web services";
        List<SearchResult> results = cliParser.parseSearch(chunk);
        assertEquals(1, results.size());
        SearchResult result = results.get(0);
        assertEquals("monolog/monolog", result.getName());
        assertEquals("Sends your logs to: files, sockets, inboxes, databases and various web services", result.getDescription());
    }

    public void testParseSearchEmptyDescription() {
        String chunk = "monolog/monolog:";
        List<SearchResult> results = cliParser.parseSearch(chunk);
        assertEquals(1, results.size());
        SearchResult result = results.get(0);
        assertEquals("monolog/monolog", result.getName());
        assertEquals("", result.getDescription());
    }

    public void testParseSearchIncorrectLine() {
        String chunk = "No composer.json found in the current directory, showing packages from packagist\n";
        List<SearchResult> results = cliParser.parseSearch(chunk);
        assertTrue(results.isEmpty());
    }

    public void testParseSearchNonLegacyLine() {
        String chunk = "vendor/package This is package with : in its description\n";
        List<SearchResult> results = cliParser.parseSearch(chunk);
        assertEquals(1, results.size());
        SearchResult result = results.get(0);
        assertEquals("vendor/package", result.getName());
        assertEquals("This is package with : in its description", result.getDescription());
    }

}
