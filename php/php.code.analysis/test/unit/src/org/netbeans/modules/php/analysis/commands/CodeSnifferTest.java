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
package org.netbeans.modules.php.analysis.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.analysis.commands.CodeSniffer.StandardsOutputProcessorFactory;

public class CodeSnifferTest extends NbTestCase {

    public CodeSnifferTest(String name) {
        super(name);
    }

    public void testParseStandards() {
        String line = "The installed coding standards are PSR2, PHPCS, Zend, PSR1, Squiz, PEAR and MySource";
        List<String> expected = Arrays.asList(
                "PSR2",
                "PHPCS",
                "Zend",
                "PSR1",
                "Squiz",
                "PEAR",
                "MySource");
        Collections.sort(expected);
        List<String> parsed = StandardsOutputProcessorFactory.parseStandards(line);
        assertEquals(expected, parsed);
    }

    public void testParseIncompleteStandards() {
        String line = "The installed coding standards are  and MySource";
        List<String> expected = Arrays.asList("MySource");
        List<String> parsed = StandardsOutputProcessorFactory.parseStandards(line);
        assertEquals(expected, parsed);
    }

    public void testParseInvalidStandards() {
        String line = "The installed coding standards are PSR2, PHPCS, Zend, PSR1, Squiz, PEAR, MySource";
        assertNull(StandardsOutputProcessorFactory.parseStandards(line));
    }

    public void testIssue239407() {
        String line = "The installed coding standards are CompByte, MySource, MyStandard, PEAR, PHPCS, PSR1, PSR2, Squiz and Zend";
        List<String> expected = Arrays.asList(
                "CompByte",
                "MySource",
                "MyStandard",
                "PEAR",
                "PHPCS",
                "PSR1",
                "PSR2",
                "Squiz",
                "Zend");
        Collections.sort(expected);
        List<String> parsed = StandardsOutputProcessorFactory.parseStandards(line);
        assertEquals(expected, parsed);
    }

    public void testIssue244550() {
        String line = "The installed coding standards are MySource, PEAR, PHPCS, PSR1, PSR2, Squiz and Zend";
        List<String> expected = Arrays.asList(
                "MySource",
                "PEAR",
                "PHPCS",
                "PSR1",
                "PSR2",
                "Squiz",
                "Zend");
        Collections.sort(expected);
        List<String> parsed = StandardsOutputProcessorFactory.parseStandards(line);
        assertEquals(expected, parsed);
    }

}
