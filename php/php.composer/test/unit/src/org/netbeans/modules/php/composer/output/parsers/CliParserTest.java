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
package org.netbeans.modules.php.composer.output.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
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

    public void testParseSearchEmptyDescriptionLegacy() {
        String chunk = "monolog/monolog:";
        List<SearchResult> results = cliParser.parseSearch(chunk);
        assertEquals(1, results.size());
        SearchResult result = results.get(0);
        assertEquals("monolog/monolog", result.getName());
        assertEquals("", result.getDescription());
    }

    public void testParseSearchEmptyDescription() {
        String chunk = "monolog/monolog";
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
